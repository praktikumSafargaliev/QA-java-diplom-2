package ingredients;

import java.util.ArrayList;

public class Ingredient {

    private ArrayList<String> ingredients;

    public Ingredient(ArrayList<String> ingredients) {
        this.ingredients = ingredients;
    }

    public Ingredient() {}

    public ArrayList<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(ArrayList<String> ingredients) {
        this.ingredients = ingredients;
    }
}
