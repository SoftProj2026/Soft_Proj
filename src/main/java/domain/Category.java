package domain;


public class Category {

    private String name;
    private Category category;
    
    public Category(String name) {
        this.name = name;
        this.category = category;
    }
    public Category getCategory() {
        return category;
    }
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}