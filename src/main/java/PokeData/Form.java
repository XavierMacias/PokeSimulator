package PokeData;

import com.google.common.collect.Multimap;

import java.util.List;

public class Form extends Specie {
    int formIndex;
    String formName;

    public Form(int index, String form, int number, String internalName, String name, Type type1, Type type2,
                List<Integer> stats, Ability ability1, Ability ability2, Ability hiddenAbility, int experience,
                int ratio, int baseHappiness, float genderrate, List<Integer> evs, int stepsToHatch,
                GrowthRate growthRate, EggGroups eggGroups1, EggGroups eggGroups2, float height, float weight,
                String kind, List<Evolution> evos) {
        super(number, internalName, name, type1, type2, stats, ability1, ability2, hiddenAbility, experience, ratio,
                baseHappiness, genderrate, evs, stepsToHatch, growthRate, eggGroups1, eggGroups2, height, weight,
                kind, evos);
        formName = form;
        formIndex = index;
    }

    public void setMoveset(Multimap<Integer, Movement> m) { moveset = m; }
    public void setEggMoves(List<Movement> e) { eggmoves = e; }
}
