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

    public int alivePokemon() {
        int total = 0;
        for(int i=0;i<pokemonTeam.size();i++) {
            if(!pokemonTeam.get(i).status.equals(Status.FAINTED)) {
                total++;
            }
        }
        return total;
    }

    public boolean isTeamDefeated() {
        return alivePokemon() == 0;
    }

    public void healTeam() {
        for(int i=0;i<pokemonTeam.size();i++) {
            pokemonTeam.get(i).healPokemon();
        }
    }

    private int getParticipants() {
        int total = 0;
        for(int i=0;i<pokemonTeam.size();i++) {
            if(pokemonTeam.get(i).participate && !pokemonTeam.get(i).isFainted()) {
                total++;
            }
        }
        return total;
    }

    public void gainTeamExperience(Pokemon rival, boolean trainer) {
        for(int i=0;i<pokemonTeam.size();i++) {
            pokemonTeam.get(i).gainExperience(rival,getParticipants(),trainer);
        }
    }

    public void battleEnded() {
        for(int i=0;i<pokemonTeam.size();i++) {
            pokemonTeam.get(i).battleEnded();
        }
    }

    public Pokemon getPokemon(int i) {
        return pokemonTeam.get(i);
    }

    public void addPokemon(Pokemon pkm) {
        pokemonTeam.add(pkm);
    }

    public Pokemon getFirstAlivePokemon() {
        for(int i=0;i<pokemonTeam.size();i++) {
            if(!pokemonTeam.get(i).status.equals(Status.FAINTED)) {
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
