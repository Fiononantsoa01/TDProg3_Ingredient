import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DataRetriever {

    // --- FIND DISH BY ID ---
    public Dish findDishById(Integer id) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        try {
            PreparedStatement ps = connection.prepareStatement(
                    """
                    SELECT dish.id as dish_id, dish.name as dish_name, dish_type
                    FROM dish
                    WHERE dish.id = ?;
                    """
            );
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Dish dish = new Dish();
                dish.setId(rs.getInt("dish_id"));
                dish.setName(rs.getString("dish_name"));
                dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));

                // --- récupérer tous les DishIngredient liés ---
                List<DishIngredient> dishIngredients = findDishIngredientsByDishId(id);
                dish.setIngredients(dishIngredients);

                return dish;
            }

            dbConnection.closeConnection(connection);
            throw new RuntimeException("Dish not found: " + id);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // --- SAVE DISH (INSERT/UPDATE) ---
    public Dish saveDish(Dish toSave) {
        String upsertDishSql = """
                INSERT INTO dish (id, name, dish_type)
                VALUES (?, ?, ?::dish_type)
                ON CONFLICT (id) DO UPDATE
                SET name = EXCLUDED.name,
                    dish_type = EXCLUDED.dish_type
                RETURNING id
                """;

        try (Connection conn = new DBConnection().getConnection()) {
            conn.setAutoCommit(false);
            Integer dishId;

            // --- INSERT/UPDATE DISH ---
            try (PreparedStatement ps = conn.prepareStatement(upsertDishSql)) {
                if (toSave.getId() != null) {
                    ps.setInt(1, toSave.getId());
                } else {
                    ps.setInt(1, getNextSerialValue(conn, "dish", "id"));
                }
                ps.setString(2, toSave.getName());
                ps.setString(3, toSave.getDishType().name());

                ResultSet rs = ps.executeQuery();
                rs.next();
                dishId = rs.getInt(1);
            }

            // --- DETACH INGREDIENTS NON UTILISÉS ---
            detachIngredients(conn, dishId, toSave.getIngredients());

            // --- ATTACH/UPDATE DISHINGREDIENTS ---
            attachIngredients(conn, dishId, toSave.getIngredients());

            conn.commit();
            return findDishById(dishId);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // --- CREATE INGREDIENTS ---
    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        if (newIngredients == null || newIngredients.isEmpty()) return List.of();

        List<Ingredient> savedIngredients = new ArrayList<>();
        DBConnection dbConnection = new DBConnection();
        Connection conn = dbConnection.getConnection();
        try {
            conn.setAutoCommit(false);
            String insertSql = """
                    INSERT INTO ingredient (id, name, category, price)
                    VALUES (?, ?, ?::ingredient_category, ?)
                    RETURNING id
                    """;

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (Ingredient ingredient : newIngredients) {
                    if (ingredient.getId() != null) {
                        ps.setInt(1, ingredient.getId());
                    } else {
                        ps.setInt(1, getNextSerialValue(conn, "ingredient", "id"));
                    }
                    ps.setString(2, ingredient.getName());
                    ps.setString(3, ingredient.getCategory().name());
                    ps.setDouble(4, ingredient.getPrice());

                    ResultSet rs = ps.executeQuery();
                    rs.next();
                    int generatedId = rs.getInt(1);
                    ingredient.setId(generatedId);
                    savedIngredients.add(ingredient);
                }
                conn.commit();
                return savedIngredients;
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }

    // --- DETACH INGREDIENTS ---
    private void detachIngredients(Connection conn, Integer dishId, List<DishIngredient> ingredients) throws SQLException {
        if (ingredients == null || ingredients.isEmpty()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM DishIngredient WHERE id_dish = ?"
            )) {
                ps.setInt(1, dishId);
                ps.executeUpdate();
            }
            return;
        }

        // Supprime les DishIngredient qui ne sont pas dans la nouvelle liste
        String baseSql = "DELETE FROM DishIngredient WHERE id_dish = ? AND id NOT IN (%s)";
        String inClause = ingredients.stream().map(di -> "?").collect(Collectors.joining(","));
        String sql = String.format(baseSql, inClause);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dishId);
            int index = 2;
            for (DishIngredient di : ingredients) {
                ps.setInt(index++, di.getId());
            }
            ps.executeUpdate();
        }
    }

    // --- ATTACH / UPDATE DISHINGREDIENTS ---
    private void attachIngredients(Connection conn, Integer dishId, List<DishIngredient> ingredients) throws SQLException {
        if (ingredients == null || ingredients.isEmpty()) return;

        String insertSql = """
                INSERT INTO DishIngredient (id, id_dish, id_ingredient, quantity, unit)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE
                SET id_ingredient = EXCLUDED.id_ingredient,
                    quantity = EXCLUDED.quantity,
                    unit = EXCLUDED.unit
                """;

        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            for (DishIngredient di : ingredients) {
                if (di.getId() != null) {
                    ps.setInt(1, di.getId());
                } else {
                    ps.setInt(1, getNextSerialValue(conn, "DishIngredient", "id"));
                }
                ps.setInt(2, dishId);
                ps.setInt(3, di.getIngredient());
                ps.setDouble(4, di.getQuantity());
                ps.setString(5, di.getUnit().name());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // --- FIND DISHINGREDIENTS BY DISH ID ---
    private List<DishIngredient> findDishIngredientsByDishId(Integer dishId) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        List<DishIngredient> dishIngredients = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(
                    """
                    SELECT di.id as di_id, di.quantity, di.unit,
                           i.id as ingredient_id, i.name as ingredient_name,
                           i.category as ingredient_category, i.price as ingredient_price
                    FROM DishIngredient di
                    JOIN ingredient i ON i.id = di.id_ingredient
                    WHERE di.id_dish = ?
                    """
            );
            ps.setInt(1, dishId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DishIngredient di = new DishIngredient(
                        rs.getInt("di_id"),
                        null, // Dish injecté plus tard
                        rs.getInt("ingredient_id"),
                        rs.getDouble("quantity"),
                        Unit.valueOf(rs.getString("unit"))
                );
                di.setIngredientName(rs.getString("ingredient_name")); // facultatif pour affichage
                di.setIngredientPrice(rs.getDouble("ingredient_price")); // pour calcul getDishCost
                dishIngredients.add(di);
            }

            dbConnection.closeConnection(connection);
            return dishIngredients;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // --- SERIAL UTILS ---
    private String getSerialSequenceName(Connection conn, String tableName, String columnName) throws SQLException {
        String sql = "SELECT pg_get_serial_sequence(?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        }
        return null;
    }

    private int getNextSerialValue(Connection conn, String tableName, String columnName) throws SQLException {
        String sequenceName = getSerialSequenceName(conn, tableName, columnName);
        if (sequenceName == null)
            throw new IllegalArgumentException("No sequence found for " + tableName + "." + columnName);
        updateSequenceNextValue(conn, tableName, columnName, sequenceName);

        String nextValSql = "SELECT nextval(?)";
        try (PreparedStatement ps = conn.prepareStatement(nextValSql)) {
            ps.setString(1, sequenceName);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private void updateSequenceNextValue(Connection conn, String tableName, String columnName, String sequenceName) throws SQLException {
        String setValSql = String.format(
                "SELECT setval('%s', (SELECT COALESCE(MAX(%s), 0) FROM %s))",
                sequenceName, columnName, tableName
        );
        try (PreparedStatement ps = conn.prepareStatement(setValSql)) {
            ps.executeQuery();
        }
    }
}
