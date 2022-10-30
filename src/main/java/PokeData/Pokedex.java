package PokeData;

import java.util.ArrayList;

public class Pokedex {
    int numPokemon;
    private ArrayList<Pokemon> seen;
    private ArrayList<Pokemon> captured;

    public Pokedex() {
        seen = new ArrayList<>();
        captured = new ArrayList<>();
    }

    public void registerPokemon(Pokemon poke) { if(!captured.contains(poke)) captured.add(poke); }
    public boolean isCaptured(Pokemon poke) { return captured.contains(poke); }
    public int numCaptured() { return captured.size(); }
}
