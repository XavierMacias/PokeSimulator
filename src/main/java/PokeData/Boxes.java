package PokeData;

import java.util.ArrayList;

public class Boxes {
    private int max = 30;
    private ArrayList<Pokemon> box;
    private int numPokes;

    public Boxes() {
        box = new ArrayList<>();
        numPokes = 0;
    }

    public int getNumPokes() { return numPokes; }
    public boolean boxIsFull() { return numPokes == max; }
    public void addPoke(Pokemon poke) { box.add(poke); }
}
