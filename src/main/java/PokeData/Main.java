package PokeData;

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

        Player player = new Player(0,"Xavi",utils);
        System.out.println("Welcome to Pokemon World, " + player.name +"!");
        System.out.println("What starter Pokemon do you want?");

        Pokemon bulbasaur = new Pokemon(utils.getPokemon("BULBASAUR"),12, utils);
        Pokemon charmander = new Pokemon(utils.getPokemon("CHARMANDER"),12, utils);
        Pokemon squirtle = new Pokemon(utils.getPokemon("SQUIRTLE"),12, utils);

        System.out.println("1: "+bulbasaur.specie.name+"\n2: "+charmander.specie.name+"\n3: "+squirtle.specie.name);
        do {
            choose = in.nextLine();
        } while(!choose.equals("1") && !choose.equals("2")  && !choose.equals("3") );

        if(choose.equals("1")) {
            starter = bulbasaur;
        } else if(choose.equals("2")) {
            starter = charmander;
        } else if(choose.equals("3")) {
            starter = squirtle;
        }

        player.getTeam().obtainPokemon(starter);

        System.out.println(starter.nickname);
    }
}