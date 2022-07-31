package PokeData;

import PokeBattle.Battle;
import org.checkerframework.checker.units.qual.A;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Team {
    private ArrayList<Pokemon> pokemonTeam;
    public List<Integer> effectTeamMoves;
    private Scanner in;

    public Team() {
        in = new Scanner(System.in);
        pokemonTeam = new ArrayList<Pokemon>();
        effectTeamMoves = new ArrayList<Integer>();
        /* 0-> mist
           1 -> safeguard
           2 -> tailwind
           3 -> toxic spikes
        */
        for(int i=0;i<5;i++) {
            effectTeamMoves.add(0);
        }
    }

    public ArrayList<Pokemon> getPokemonTeam() {
        return pokemonTeam;
    }

    public void obtainPokemon(Pokemon pkm) {
        //TODO: Check if there are more than 6 pokemon in the team
        pokemonTeam.add(pkm);
        pkm.setTeam(this);
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

    public void showTeam() {
        for(int i=0;i<pokemonTeam.size();i++) {
            System.out.println((i+1) + ": " + pokemonTeam.get(i).nickname + " - " + pokemonTeam.get(i).status.toString());
        }
    }

    public boolean isTeamDefeated() {
        return alivePokemon() == 0;
    }

    public void healTeam() {
        for(int i=0;i<pokemonTeam.size();i++) {
            pokemonTeam.get(i).healPokemon(false);
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

    public ArrayList<Pokemon> getBeatUpTeam(Pokemon attacker) {
        ArrayList<Pokemon> beatUp = new ArrayList<Pokemon>();
        beatUp.add(attacker);
        for(int i=0;i<pokemonTeam.size();i++) {
            Pokemon pk = pokemonTeam.get(i);
            if(pk != attacker && pk.hasStatus(Status.FINE)) {
                beatUp.add(pk);
            }
        }
        return beatUp;
    }

    public void increaseEffectMove(int index) {
        effectTeamMoves.set(index,effectTeamMoves.get(index)+1);
    }

    public void battleEnded() {
        // recover move effects
        for(int i=0;i<effectTeamMoves.size();i++) {
            effectTeamMoves.set(i,0);
        }
        // reset pokemon
        for(int i=0;i<pokemonTeam.size();i++) {
            pokemonTeam.get(i).battleEnded();
        }
    }

    public Pokemon getPokemon(int i) {
        return pokemonTeam.get(i);
    }

    public void addPokemon(Pokemon pkm) {
        pokemonTeam.add(pkm);
        pkm.setTeam(this);
    }

    public Pokemon getFirstAlivePokemon() {
        for(int i=0;i<pokemonTeam.size();i++) {
            if(!pokemonTeam.get(i).status.equals(Status.FAINTED)) {
                return pokemonTeam.get(i);
            }
        }
        return null;
    }

    public void inBattle(Battle battle) {
        for(int i=0;i<pokemonTeam.size();i++) {
            pokemonTeam.get(i).battle = battle;
        }
    }

    public void removeTeamEffects(Pokemon target, int index) {
        effectTeamMoves.set(index, 0);
        if(index == 0) {
            System.out.println("The mist of " + target.nickname + "'s team is gone!");
        }
        else if(index == 1) {
            System.out.println("The Safeguard of " + target.nickname + "'s team is gone!");
        }
        else if(index == 2) {
            System.out.println("The Tail Wind of " + target.nickname + "'s team has gone!");
        }
        else if(index == 3) {
            System.out.println("The Toxic Spikes of " + target.nickname + "'s team are gone!");
        }
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
