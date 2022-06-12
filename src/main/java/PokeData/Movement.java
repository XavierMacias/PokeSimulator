package PokeData;

public class Movement {
    private String internalName;
    public String name, description;
    public Type type;
    public int power, accuracy, category;

    public Movement(String internalName, String name, String description, Type type, int power, int accuracy, int category) {
        this.internalName = internalName;
        this.name = name;
        this.description = description;
        this.type = type;
        this.power = power;
        this.accuracy = accuracy;
        this.category = category;
    }

    public String getInternalName() { return internalName; }
}
