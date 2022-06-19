package PokeData;

public class Player {
    private int id, money;
    public int genre; // 0-> male, 1-> female, 2-> other
    public String name;
    private Utils utils;
    private Team team;
    // TODO: pokedex, bag, PC boxes

    public Player(int genre, String name, Utils utils) {
        this.utils = utils;
        this.genre = genre;
        this.name = name;
        // generate trainer ID
        id = utils.getRandomNumberBetween(0,65536);
        money = 0;
        // initialize team
        team = new Team();
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
}
