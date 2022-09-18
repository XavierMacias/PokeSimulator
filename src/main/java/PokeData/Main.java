package PokeData;

import PokeBattle.Battle;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);
        Utils utils = new Utils();
        utils.addTypes(); // types added
        utils.addAbilities(); // abilities added
        utils.addMoves(); // moves added
        utils.addItems(); // moves items
        utils.addSpecies(); // species added

        Pokemon starter = null;
        String choose = "0";
        String menu = "0";

        Player player = new Player(0,"Xavi",utils);
        System.out.println("Welcome to Pokemon World, " + player.name +"!");
        System.out.println("What starter Pokemon do you want?");

        System.out.println("1: Bulbasaur\n2: Charmander\n3: Squirtle");
        do {
            choose = in.nextLine();
        } while(!choose.equals("1") && !choose.equals("2")  && !choose.equals("3"));

        if(choose.equals("1")) {
            starter = new Pokemon(utils.getPokemon("BULBASAUR"),14, utils);
        } else if(choose.equals("2")) {
            starter = new Pokemon(utils.getPokemon("CHARMANDER"),14, utils);
        } else if(choose.equals("3")) {
            starter = new Pokemon(utils.getPokemon("SQUIRTLE"),14, utils);
        }

        player.getTeam().obtainPokemon(starter);
        player.getTeam().addPokemon(new Pokemon(utils.getPokemon("RATTATA"),11, utils));
        player.getTeam().addPokemon(new Pokemon(utils.getPokemon("PIDGEY"),11, utils));
        player.getTeam().addPokemon(new Pokemon(utils.getPokemon("PIKACHU"),11, utils));
        //player.getTeam().obtainPokemon(new Pokemon(utils.getPokemon("SQUIRTLE"),13, utils));
        //starter.setMove("YAWN");
        player.getBag().addItem(utils.getItem("POTION"), true);
        player.getBag().addItem(utils.getItem("SUPERPOTION"), true);
        player.getBag().addItem(utils.getItem("WATERSTONE"), true);
        player.getBag().addItem(utils.getItem("ORANBERRY"), true);
        player.getBag().addItem(utils.getItem("ORANBERRY"), true);
        player.getBag().addItem(utils.getItem("REPEL"), true);
        player.getBag().addItem(utils.getItem("REPEL"), true);
        player.getBag().addItem(utils.getItem("REPEL"), true);
        player.getBag().addItem(utils.getItem("CHERIBERRY"), true);
        player.getBag().addItem(utils.getItem("THUNDERSTONE"), true);
        player.getBag().addItem(utils.getItem("POTION"), true);
        player.getBag().addItem(utils.getItem("MAXREPEL"), true);
        player.getBag().addItem(utils.getItem("MAXREPEL"), true);
        player.getBag().addItem(utils.getItem("PEARL"), true);
        player.getBag().addItem(utils.getItem("BIGPEARL"), true);
        player.getBag().addItem(utils.getItem("BIGPEARL"), true);
        player.getBag().addItem(utils.getItem("BINDINGBAND"), true);

        while(!menu.equals("-1")) {
            System.out.println("1: Battle against wild Pokemon\n2: Bag\n3: Party\n4: Pokedex\n-1: Exit");
            menu = in.nextLine();
            switch(menu) {
                case "1":
                    // battle
                    Battle battle = new Battle();
                    System.out.println("Choose the Pokemon name");
                    String pkmnIndex = "0";
                    pkmnIndex = in.nextLine();
                    if(utils.getPokemon(pkmnIndex) != null) {
                        Team rivalTeam = new Team();
                        rivalTeam.addPokemon(new Pokemon(utils.getPokemon(pkmnIndex),13,utils));
                        battle.WildSingleBattle(player.getTeam(),rivalTeam);
                    }
                    break;
                case "2":
                    // bag
                    player.getBag().openBag(true);
                    break;
                case "3":
                    // party
                    player.getTeam().pokeOptions();
                    break;
                case "4":
                    // pokedex
                    break;
                case "5":
                    starter.participate = true;
                    starter.gainExperience(starter,1,true);
            }
        }

    }
}