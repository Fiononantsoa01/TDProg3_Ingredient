import java.util.List;

public class Main {
public static void main(String[] args) {
    DataRetriever dataRetriever = new DataRetriever();
    Dish dish= dataRetriever.findDishById(4);
    System.out.println(dish);
   // Dish dish2= dataRetriever.findDishById(99);
    //System.out.println(dish2);
    List<Ingredient> ingredinentList1=dataRetriever.findIngredients(2,2);
    System.out.println(ingredinentList1);
    List<Ingredient> ingredinentList2=dataRetriever.findIngredients(3,5);
    System.out.println(ingredinentList2);
}}
