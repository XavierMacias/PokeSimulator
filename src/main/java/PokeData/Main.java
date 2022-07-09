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
            starter = new Pokemon(utils.getPokemon("BULBASAUR"),13, utils);
        } else if(choose.equals("2")) {
            starter = new Pokemon(utils.getPokemon("CHARMANDER"),13, utils);
        } else if(choose.equals("3")) {
            starter = new Pokemon(utils.getPokemon("SQUIRTLE"),13, utils);
        }

        player.getTeam().obtainPokemon(starter);
        starter.setMove("GROWTH");

        while(!menu.equals("-1")) {
            System.out.println("1: Battle against wild Pokemon\n2: Bag\n3: Pokedex\n-1: Exit");
            menu = in.nextLine();
            switch(menu) {
                case "1":
                    // battle
                    Battle battle = new Battle();
                    System.out.println("Choose the Pokemon number");
                    String pkmnIndex = "0";
                    pkmnIndex = in.nextLine();
                    if(utils.getPokemonByNumber(Integer.valueOf(pkmnIndex)) != null) {
                        Team rivalTeam = new Team();
                        rivalTeam.addPokemon(new Pokemon(utils.getPokemonByNumber(Integer.valueOf(pkmnIndex)),12,utils));
                        battle.WildSingleBattle(player.getTeam(),rivalTeam);
                    }
                    break;
                case "2":
                    // bag
                    break;
                case "3":
                    // pokedex
                    break;
                case "4":
                    starter.participate = true;
                    starter.gainExperience(starter,1,true);
            }
        }

    }
}