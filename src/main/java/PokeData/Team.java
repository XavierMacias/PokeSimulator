package PokeData;

import java.util.ArrayList;
import java.util.Scanner;

public class Team {
    private ArrayList<Pokemon> pokemonTeam;
    private Scanner in;

    public Team() {
        in = new Scanner(System.in);
        pokemonTeam = new ArrayList<Pokemon>();
    }

    public ArrayList<Pokemon> getPokemonTeam() {
        return pokemonTeam;
    }

    public void obtainPokemon(Pokemon pkm) {
        // TODO: Check if there are more than 6 pokemon in the team
        pokemonTeam.add(pkm);
        System.out.println("You received a "+pkm.specie.name+"!");
        giveNickname(pkm);
    }

    public Pokemon getPokemon(int i) {
        return pokemonTeam.get(i);
    }

    public void addPokemon(Pokemon pkm) {
        pokemonTeam.add(pkm);
    }

    public Pokemon getFirstAlivePokemon() {
        for(int i=0;i<pokemonTeam.size();i++) {
            if(pokemonTeam.get(i).psActuales > 0) {
                return pokemonTeam.get(i);
            }
        }
        return null;
    }

    private void giveNickname(Pokemon pkm) {
        System.out.println("Would you like to give a nickname to "+pkm.specie.name+"?");
        System.out.println("1: Yes\n2: No");
        if(in.nextLine().equals("1")) {
            System.out.println("What nickname do you want?");
            pkm.nickname = in.nextLine();
        }
    }
}
