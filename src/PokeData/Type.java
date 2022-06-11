package PokeData;

import java.util.ArrayList;
import java.util.List;

public class Type {
    private String internalName;
    public String name;
    public List<String> weaknesses, resistances, immunities;

    public Type(String internalName, String name, List<String> weaknesses, List<String> resistances, List<String> immunities) {
        this.internalName = internalName;
        this.name = name;
        this.weaknesses = weaknesses;
        this.resistances = resistances;
        this.immunities = immunities;
    }

    public String getInternalName() { return internalName; }
}
