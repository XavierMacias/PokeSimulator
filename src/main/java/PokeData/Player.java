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
    // TODO: pokedex, PC boxes

    public Player(int genre, String name, Utils utils) {
        this.utils = utils;
        this.genre = genre;
        this.name = name;
        // generate trainer ID
        id = utils.getRandomNumberBetween(0,65536);
        money = 0;
        // initialize team
        team = new Team();
        // initialize bag
        bag = new Bag(this);
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

    public Bag getBag() { return bag; }
}
