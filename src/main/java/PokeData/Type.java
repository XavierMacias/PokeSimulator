package PokeData;

import java.util.ArrayList;
import java.util.List;

public class Type {
    private String internalName;
    public String name;
    public List<String> weaknesses, resistances, immunities;

    public Type(String internalName, String name) {
        this.internalName = internalName;
        this.name = name;
    }
    public void setWeaknesses(List<String> w) { weaknesses = w; }
    public void setResistances(List<String> r) { resistances = r; }
    public void setImmunities(List<String> i) { immunities = i; }
    public String getInternalName() { return internalName; }
}
