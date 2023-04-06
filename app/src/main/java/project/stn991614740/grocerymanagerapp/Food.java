package project.stn991614740.grocerymanagerapp;

public class Food {

    String Category, Description, ExpirationDate;

    public Food(){}

    public Food(String category, String description, String expirationDate) {
        Category = category;
        Description = description;
        ExpirationDate = expirationDate;
    }

    public String getCategory() {
        return Category;
    }

    public void setCategory(String category) {
        Category = category;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getExpirationDate() {
        return ExpirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        ExpirationDate = expirationDate;
    }
}
