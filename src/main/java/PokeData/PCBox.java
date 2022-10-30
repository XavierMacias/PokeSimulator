package PokeData;

import java.util.ArrayList;

public class PCBox {
    int numBoxes = 5;
    private ArrayList<Pokemon> box1;
    private ArrayList<Pokemon> box2;
    private ArrayList<Pokemon> box3;
    private ArrayList<Pokemon> box4;
    private ArrayList<Pokemon> box5;

    public PCBox() {
        box1 = new ArrayList<>();
        box2 = new ArrayList<>();
        box3 = new ArrayList<>();
        box4 = new ArrayList<>();
        box5 = new ArrayList<>();
    }

    public void addToBox(Pokemon poke) {
        //TODO: all boxes are full
        for(int i=0;i<numBoxes;i++) {
            if(box1.size() == 30) {
                System.out.println("Box " + (i+1)  + "is full!");
                i++;
            } else {
                System.out.println(poke.nickname + " was sent to Box 1!");
                box1.add(poke);
                return;
            }
        }
    }
}
