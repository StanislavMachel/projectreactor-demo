package dev.stanislavmachel;

public class Food {

    private FoodType type;
    private String   name;

    public Food(FoodType type, String name) {
        this.type = type;
        this.name = name;
    }

    public FoodType getType() {
        return type;
    }

    public void setType(FoodType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Food{" +
                   "type=" + type +
                   ", name='" + name + '\'' +
                   '}';
    }

}
