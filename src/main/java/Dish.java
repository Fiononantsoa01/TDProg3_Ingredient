import java.util.List;
import java.util.Objects;

public class Dish {
    private Integer id;
    private String name;
    private DishTypeEnum dishType;
    private Double price; // prix de vente, nullable
    private List<DishIngredient> ingredients;

    public Dish() {}

    public Dish(Integer id, String name, DishTypeEnum dishType, Double price, List<DishIngredient> ingredients) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.price = price;
        this.ingredients = ingredients;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public DishTypeEnum getDishType() { return dishType; }
    public void setDishType(DishTypeEnum dishType) { this.dishType = dishType; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public List<DishIngredient> getIngredients() { return ingredients; }
    public void setIngredients(List<DishIngredient> ingredients) { this.ingredients = ingredients; }

    /**
     * Calcule le coût total du plat en multipliant le prix de chaque ingrédient
     * par la quantité requise dans DishIngredient.
     */
    public Double getDishCost() {
        if (ingredients == null || ingredients.isEmpty()) return 0.0;

        double totalCost = 0;
        for (DishIngredient di : ingredients) {
            if (di.getIngredientPrice() == null) {
                throw new RuntimeException("Ingredient price is null for DishIngredient ID " + di.getId());
            }
            if (di.getQuantity() == null) {
                throw new RuntimeException("Quantity is null for DishIngredient ID " + di.getId());
            }
            totalCost += di.getIngredientPrice() * di.getQuantity();
        }
        return totalCost;
    }

    /**
     * Calcule la marge brute : prix de vente - coût du plat.
     * Si le prix de vente est null, une exception est levée.
     */
    public Double getGrossMargin() {
        if (price == null) {
            throw new RuntimeException("Dish price is null, cannot calculate gross margin for Dish ID " + id);
        }
        return price - getDishCost();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dish dish = (Dish) o;
        return Objects.equals(id, dish.id) &&
                Objects.equals(name, dish.name) &&
                dishType == dish.dishType &&
                Objects.equals(price, dish.price) &&
                Objects.equals(ingredients, dish.ingredients);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, dishType, price, ingredients);
    }

    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dishType=" + dishType +
                ", price=" + price +
                ", ingredients=" + ingredients +
                '}';
    }
}
