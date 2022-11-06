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

    public boolean is(String t) {
        return internalName.equals(t);
    }
    public void setWeaknesses(List<String> w) { weaknesses = w; }
    public void setResistances(List<String> r) { resistances = r; }
    public void setImmunities(List<String> i) { immunities = i; }
    public ArrayList<String> resistAndInmun() {
        ArrayList<String> resultList = new ArrayList<>();
        resultList.addAll(resistances);
        resultList.addAll(immunities);

        return resultList;
    }
    public String getInternalName() { return internalName; }
}
