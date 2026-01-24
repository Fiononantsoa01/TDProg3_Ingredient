import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DataRetriever {
    Dish findDishById(Integer id) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                            select dish.id as dish_id, dish.name as dish_name, dish_type
                            from dish
                            where dish.id = ?;
                            """);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Dish dish = new Dish();
                dish.setId(resultSet.getInt("dish_id"));
                dish.setName(resultSet.getString("dish_name"));
                dish.setDishType(DishTypeEnum.valueOf(resultSet.getString("dish_type")));
                return dish;
            }
            dbConnection.closeConnection(connection);
            throw new RuntimeException("Dish not found " + id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    List<Ingredient> findIngredients(int page , int size) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
    try {

        List<Ingredient> ingredients = new ArrayList<>();
        int offset = (page - 1) * size;
            PreparedStatement preparedStatement = connection.prepareStatement(
                 "select id, name, price , category ,id_dish from ingredient order by id limit ? offset ?"
            );
            preparedStatement.setInt(1,size);
            preparedStatement.setInt(2,offset);
            ResultSet resultSet=preparedStatement.executeQuery();
            while (resultSet.next()){
                Ingredient ingredient=new Ingredient(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getDouble("price"),
                        CategoryEnum.valueOf(resultSet.getString("category")),
                        null

                );
                ingredients.add(ingredient);
            }
        dbConnection.closeConnection(connection);
        return ingredients;
    }
    catch (SQLException e){
        throw new RuntimeException(e);
    }
}
}
