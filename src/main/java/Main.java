import java.util.List;

public class Main {
    public static void main(String[] args) {
        DataRetriever d = new DataRetriever();
        Dish findDish = d.findDishById(1);
        System.out.println(findDish);
        List<Ingredient> ingredients = d.findIngredients(2,2);
        System.out.println(ingredients);
        List<Ingredient> ingredients2 = d.findIngredients(3,5);
        System.out.println(ingredients2);
        List<Dish> dishes=d.findDishsByIngredientName("eur");
        System.out.println(dishes);
    }
}
