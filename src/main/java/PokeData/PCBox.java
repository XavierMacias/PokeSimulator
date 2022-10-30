package PokeData;

import javax.swing.*;
import java.util.ArrayList;

public class PCBox {
    int numBoxes = 30;
    private ArrayList<Boxes> boxes;

    public PCBox() {
        boxes = new ArrayList<>();
        for(int i=0;i<numBoxes;i++) {
            Boxes box = new Boxes();
            boxes.add(box);
        }
    }

    public void addToBox(Pokemon poke) {
        //TODO: all boxes are full
        for(int i=0;i<boxes.size();i++) {
            if(boxes.get(i).boxIsFull()) {
                System.out.println("Box " + (i+1)  + "is full!");
                i++;
            } else {
                System.out.println(poke.nickname + " was sent to Box " + (i+1) + "!");
                boxes.get(i).addPoke(poke);
                return;
            }
        }
    }

    // TODO: take and put pokemon
}
