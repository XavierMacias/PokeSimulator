package PokeData;

import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
Here are defined the properties of a Pokemon specie, that all the Pokemon of this specie share between them.
 */
public class Specie {
    private String internalName;
    public String name, kind;
    public int number, experience, ratio, baseHappiness, stepsToHatch;
    public Type type1, type2;
    public Ability ability1, ability2, hiddenAbility;
    public float genderrate, height, weight;
    public List<Integer> stats, evs;
    public GrowthRate growthRate;
    public EggGroups eggGroups1, eggGroups2;
    public Multimap<Integer, Movement> moveset;
    public List<Movement> eggmoves;
    public List<Evolution> evos;
    public ArrayList<Form> formLists;

    public Specie(int number, String internalName, String name, Type type1, Type type2, List<Integer> stats, Ability ability1,
                  Ability ability2, Ability hiddenAbility, int experience, int ratio, int baseHappiness, float genderrate, List<Integer> evs,
                  int stepsToHatch, GrowthRate growthRate, EggGroups eggGroups1, EggGroups eggGroups2, float height, float weight, String kind,
                  List<Evolution> evos) {
        this.number = number;
        this.internalName = internalName;
        this.name = name;
        this.kind = kind;
        this.experience = experience;
        this.ratio = ratio;
        this.baseHappiness = baseHappiness;
        this.type1 = type1;
        this.type2 = type2;
        this.ability1 = ability1;
        this.ability2 = ability2;
        this.hiddenAbility = hiddenAbility;
        this.genderrate = genderrate;
        this.stepsToHatch = stepsToHatch;
        this.height = height;
        this.weight = weight;
        this.stats = stats;
        this.evs = evs;
        this.growthRate = growthRate;
        this.eggGroups1 = eggGroups1;
        this.eggGroups2 = eggGroups2;
        this.evos = evos;
        formLists = new ArrayList<>();
    }

    public String getInternalName() { return internalName; }

    public void setMoveset(Multimap<Integer, Movement> m) { moveset = m; }
    public void setEggMoves(List<Movement> e) { eggmoves = e; }

    public void changeForm(int index) {
        Form newForm = formLists.get(index);
        kind = newForm.kind;
        experience = newForm.experience;
        ratio = newForm.ratio;
        baseHappiness = newForm.baseHappiness;
        type1 = newForm.type1;
        type2 = newForm.type2;
        ability1 = newForm.ability1;
        ability2 = newForm.ability2;
        hiddenAbility = newForm.hiddenAbility;
        genderrate = newForm.genderrate;
        stepsToHatch = newForm.stepsToHatch;
        height = newForm.height;
        weight = newForm.weight;
        stats = newForm.stats;
        evs = newForm.evs;
        growthRate = newForm.growthRate;
        eggGroups1 = newForm.eggGroups1;
        eggGroups2 = newForm.eggGroups2;
        evos = newForm.evos;
    }

}
