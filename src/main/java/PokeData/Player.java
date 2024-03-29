package PokeData;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Player {
    private int id, money;
    public int genre; // 0-> male, 1-> female, 2-> other
    public String name;
    private Utils utils;
    private Team team;
    private Bag bag;
    private PCBox pc;
    private Pokedex pokedex;

    public Player(int genre, String name, Utils utils) {
        this.utils = utils;
        this.genre = genre;
        this.name = name;
        // generate trainer ID
        id = utils.getRandomNumberBetween(0,65536);
        money = 0;
        // initialize team
        team = new Team(this);
        // initialize bag
        bag = new Bag(this);
        pokedex = new Pokedex();
        pc = new PCBox();
    }

    public int getId() {
        return id;
    }

    public int getMoney() {
        return money;
    }

    public Team getTeam() {
        return team;
    }
    public PCBox getPCBox() { return pc; }
    public Pokedex getPokedex() { return pokedex; }
    public Bag getBag() { return bag; }

    public void addMoney(int quantity) {
        money += quantity;
        System.out.println(name + " gains " + quantity + "$!");
        if(money > 999999) {
            money = 999999;
        }
    }

}
