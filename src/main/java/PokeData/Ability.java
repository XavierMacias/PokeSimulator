package PokeData;

public class Ability {

    private String internalName;
    public String name, description;

    public Ability(String internalName, String name, String description) {
        this.internalName = internalName;
        this.name = name;
        this.description = description;
    }

    public String getInternalName() { return internalName; }
}
