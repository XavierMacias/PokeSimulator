package PokeData;

import java.util.List;

enum GrowthRate {
    ERRATIC,
    FAST,
    MEDIUMFAST,
    MEDIUMSLOW,
    SLOW,
    FLUCTUATING
}

public class Specie {
    private String internalName;
    public String name, kind;
    public int number, experience, ratio;
    public Type type1, type2;
    public Ability ability1, ability2, hiddenAbility;
    public float genderrate, height, weight;
    public List<Integer> stats, evs;
    public GrowthRate growthRate;
    public List<Movement> moveset, eggmoves;

    public Specie(String internalName, String name, String kind, int number, int experience, int ratio, Type type1, Type type2, Ability ability1, Ability ability2, Ability hiddenAbility, float genderrate, float height, float weight, List<Integer> stats, List<Integer> evs, GrowthRate growthRate, List<Movement> moveset, List<Movement> eggmoves) {
        this.internalName = internalName;
        this.name = name;
        this.kind = kind;
        this.number = number;
        this.experience = experience;
        this.ratio = ratio;
        this.type1 = type1;
        this.type2 = type2;
        this.ability1 = ability1;
        this.ability2 = ability2;
        this.hiddenAbility = hiddenAbility;
        this.genderrate = genderrate;
        this.height = height;
        this.weight = weight;
        this.stats = stats;
        this.evs = evs;
        this.growthRate = growthRate;
        this.moveset = moveset;
        this.eggmoves = eggmoves;
    }

    public String getInternalName() { return internalName; }

}
