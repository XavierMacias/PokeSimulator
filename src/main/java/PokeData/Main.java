package PokeData;

public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome to Pokemon World!");

        Utils utils = new Utils();
        utils.addTypes(); // types added
        utils.addAbilities(); // abilities added
        utils.addMoves(); // moves added
        utils.addSpecies(); // species added

        Pokemon starter = new Pokemon(utils.getPokemon("BULBASAUR"),12, utils);
        /*for(int i=0;i<utils.getSpecies().size();i++) {
            System.out.println(utils.getSpecies().get(i).name+"\n");
        }*/
    }
}