import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {
    //question 6-a
public Dish findDishById(Integer id) {
    DBConnection db = new DBConnection();
    Connection conn = db.getConnection();
    Dish findDish= null;
    try {
        PreparedStatement ps= conn.prepareStatement(
                "select  public.dish.id , public.dish.name, i.name from dish inner join public.ingredient i on dish.id = i.id_dish where public.dish.id=?"
        );
        ps.setInt(1, id);
        ResultSet rs= ps.executeQuery();
        List<Ingredient> ingredients = new ArrayList<>();
        while (rs.next()) {
            // On crée le Dish une seule fois
            if (findDish == null) {
                findDish = new Dish();
                findDish.setId(rs.getInt("id"));
                findDish.setName(rs.getString("name"));
            }
            // On ajoute chaque ingrédient
            Ingredient ingredient = new Ingredient();
            ingredient.setId(rs.getInt("id"));
            ingredient.setName(rs.getString("name"));

            ingredients.add(ingredient);
        }

        if (findDish != null) {
            findDish.setIngredients(ingredients);
        }

        conn.close();
        return findDish;
}catch (SQLException e) {
    throw new RuntimeException("error finding dish", e);}
}
    // question 6-b
public List<Ingredient> findIngredients(int page, int size) {
    DBConnection db = new DBConnection();
    Connection conn = db.getConnection();
    List<Ingredient> ingredients = new ArrayList<Ingredient>();
    if (page<0 ){page=1;}
    if(size<0){size=10;}
    if (size>80){size =40;}
    int offset = (page-1)*size;
    try {
        PreparedStatement ps= conn.prepareStatement(
        "select id , name  from ingredient order by id limit ? offset ?"
        );
        ps.setInt(1,size);
        ps.setInt(2,offset);
        ResultSet rs= ps.executeQuery();
        while (rs.next()){
            Ingredient ingredient = new Ingredient();
            ingredient.setName(rs.getString("name"));
            ingredients.add(ingredient);
        }
        conn.close();
        return ingredients;

    }catch (SQLException e){
        throw new RuntimeException("error in findIngredients "+e);
    }

}
    // question 6-e
public List<Dish>  findDishsByIngredientName(String ingredientName) {
    DBConnection db = new DBConnection();
    Connection conn = db.getConnection();
    List<Dish> findedDish = new ArrayList<>();
    try {
        PreparedStatement ps= conn.prepareStatement(
                "select public.dish.id , public.dish.name from dish inner join ingredient on dish.id = ingredient.id_dish where ingredient.name like ?"
        );
        ps.setString(1,"%"+ingredientName+"%");
        ResultSet rs= ps.executeQuery();
        while(rs.next()){
            Dish dish = new Dish();
            dish.setId(rs.getInt("id"));
            dish.setName(rs.getString("name"));
            findedDish.add(dish);
        }
        conn.close();
        return findedDish;
    }catch (SQLException e){
        throw new RuntimeException("error in findDishsByIngredientName "+e);
    }
}
    // question 6-f
public List<Ingredient> findIngredientByCriteria( String ingredientName, String dishName , CategoryEnum category, int page, int size) {
    DBConnection db = new DBConnection();
    Connection conn = db.getConnection();
    if (page<0 ){page=1;}
    if(size<0){size=10;}
    if (size>80){size =40;}
    int offset = (page-1)*size;
    List<Ingredient> ingredients = new ArrayList<>();
    try {
        String sql = """
            SELECT i.id, i.name, i.category,
                   d.id AS dish_id, d.name AS dish_name
            FROM ingredient i
            INNER JOIN dish d ON d.id = i.id_dish
            WHERE 1=1
        """;
        if(ingredientName!=null&&!ingredientName.isEmpty()){
            sql+= " AND LOWER(i.name) LIKE LOWER(?)";
        }
        if (category != null) {
            sql += " AND i.category = ? ";
        }

        if (dishName != null && !dishName.isEmpty()) {
            sql += " AND LOWER(d.name) LIKE LOWER(?) ";
        }

        sql += " LIMIT ? OFFSET ? ";

        PreparedStatement ps = conn.prepareStatement(sql);
        int index = 1;

        if (ingredientName != null && !ingredientName.isEmpty()) {
            ps.setString(index++, "%" + ingredientName + "%");
        }

        if (category != null) {
            ps.setObject(index++, category.name(), java.sql.Types.OTHER);
        }

        if (dishName != null && !dishName.isEmpty()) {
            ps.setString(index++, "%" + dishName + "%");
        }

        ps.setInt(index++, size);
        ps.setInt(index, offset);

        ResultSet rs = ps.executeQuery();
        while (rs.next()){
            Dish dish = new Dish();
            dish.setId(rs.getInt("id"));
            dish.setName(rs.getString("dish_name"));
            Ingredient ingredient = new Ingredient();
            ingredient.setName(rs.getString("name"));
            ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));
            ingredient.setDish(dish);
            ingredients.add(ingredient);
        }
        conn.close();
    }catch (SQLException e){
        throw  new RuntimeException("error in findIngredientByCriteria "+e);
    }
    return ingredients;
}
}
