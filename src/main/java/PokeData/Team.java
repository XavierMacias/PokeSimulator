package PokeData;

import PokeBattle.Battle;
import org.checkerframework.checker.units.qual.A;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Team {
    private ArrayList<Pokemon> pokemonTeam;
    public List<Integer> effectTeamMoves;
    public int wishRecover;
    public Pokemon futureAttackerPoke;
    public int futureAttackId;
    private Scanner in;
    private Player player;

    public Team(Player trainer) {
        in = new Scanner(System.in);
        pokemonTeam = new ArrayList<Pokemon>();
        effectTeamMoves = new ArrayList<Integer>();
        /* 0-> mist
           1 -> safeguard
           2 -> tailwind
           3 -> toxic spikes
           4 -> light screen
           5 -> reflect
           6 -> lucky chant
           7 -> wish
           8 -> healing wish
           9 -> quick guard
           10 -> wide guard
           11 -> pay day
           12 -> future sight
           13 -> retaliate
           14 -> stealth rock
           15 -> spikes
           16 -> sticky web
           17 -> guard spec
           18 -> aurora veil
           19 -> crafty shield
           20 -> lunar dance
           21 -> mat block
        */
        for(int i=0;i<22;i++) {
            effectTeamMoves.add(0);
        }
        player = trainer;
        futureAttackerPoke = null;
        futureAttackId = 0; // 1: future sight, 2: doom desire
    }

    public ArrayList<Pokemon> getPokemonTeam() {
        return pokemonTeam;
    }
    public Player getPlayer() { return player; }
    public PCBox getPCBox() { return player.getPCBox(); }

    public void obtainPokemon(Pokemon pkm) {
        if(player != null) player.getPokedex().registerPokemon(pkm);
        System.out.println("You received a " + pkm.specie.name + "!");
        giveNickname(pkm);
        if(getNumPokemon() < 6) {
            pokemonTeam.add(pkm);
            pkm.setTeam(this);
        } else {
            System.out.println("You already have 6 Pokémon in your team!");
            player.getPCBox().addToBox(pkm);
        }

    }
    public int getNumPokemon() { return pokemonTeam.size(); }

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

    public Pokemon selectPokemon() {
        int chosenIndex = -1;
        do {
            System.out.println("0: Exit");
            showTeam();
            chosenIndex = Integer.parseInt(in.nextLine());
            if(chosenIndex == 0) {
                return null;
            }
        } while(chosenIndex < 0 || chosenIndex > getPokemonTeam().size());

        return getPokemon(chosenIndex-1);
    }

    public int choseRandomAliveMember(Pokemon currentPoke) {
        ArrayList<Integer> indexes = new ArrayList<Integer>();
        Random random = new Random();
        for(int i=0;i<pokemonTeam.size();i++) {
            if(!pokemonTeam.get(i).status.equals(Status.FAINTED) && pokemonTeam.get(i) != currentPoke) {
                indexes.add(i+1);
            }
        }

        return indexes.get(random.nextInt(indexes.size()));
    }

    public void pokeOptions() {
        Pokemon poke = selectPokemon();
        int chosenIndex = -1;
        if(poke != null) {
            do {
                System.out.println("0: Exit");
                System.out.println("1: Info");
                System.out.println("2: Item");
                System.out.println("3: Move");
                chosenIndex = Integer.parseInt(in.nextLine());

            } while(chosenIndex < 0 || chosenIndex > 3);
            switch (chosenIndex) {
                case 1:
                    poke.showInfo();
                    break;
                case 2:
                    String chosenIndex2 = "-1";
                    System.out.println("0: Exit");
                    System.out.println("1: Give");
                    if(poke.item != null) {
                        System.out.println("2: Take");
                    }
                    chosenIndex2 = in.nextLine();
                    if(chosenIndex2.equals("1")) {
                        // give item
                        player.getBag().openBag(true,poke);
                    } else if(chosenIndex2.equals("2") && poke.item != null) {
                        // take item
                        player.getBag().addItem(poke.item, true);
                        poke.loseItem(false, false);
                    }
                    break;
                case 3:
                    System.out.println("Which Pokemon do you want to switch with " + poke.nickname + "?");
                    movePokemon(poke);
                    break;
            }
        }
    }

    public void movePokemon(Pokemon target) {
        int chosenIndex = -1;
        do {
            System.out.println("0: Exit");
            for(int i=0;i<pokemonTeam.size();i++) {
                System.out.println((i+1) + ": " + pokemonTeam.get(i).nickname);
            }
            chosenIndex = Integer.parseInt(in.nextLine());
        } while(chosenIndex < 0 || chosenIndex > pokemonTeam.size());
        if(chosenIndex == 0) return;
        if(pokemonTeam.get(chosenIndex-1).equals(target)) {
            System.out.println("This Pokemon is in this position yet");
        } else {
            int auxIndex = pokemonTeam.indexOf(target);
            pokemonTeam.set(auxIndex, pokemonTeam.get(chosenIndex-1));
            pokemonTeam.set(chosenIndex-1, target);
            System.out.println(target.nickname + " changed position");
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

    public void gainBattleMoney(boolean trainer) {
        if(trainer) {
            // TODO: trainer money
        }
        if(effectTeamMoves.get(11) > 0 && player != null) { // pay day
            System.out.println(player.name + " picked Pay Day coins!");
            player.addMoney(effectTeamMoves.get(11));
        }
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
        if(player != null) player.getPokedex().registerPokemon(pkm);
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
        else if(index == 4) {
            System.out.println("The Light Screen of " + target.nickname + "'s team is gone!");
        }
        else if(index == 5) {
            System.out.println("The Reflect of " + target.nickname + "'s team is gone!");
        }
        else if(index == 6) {
            System.out.println("The Lucky Chant of " + target.nickname + "'s team is gone!");
        } else if(index == 7) {
            wishRecover = 0;
        } else if(index == 14) {
            System.out.println("The Stealth Rocks of " + target.nickname + "'s team are gone!");
        } else if(index == 15) {
            System.out.println("The Spikes of " + target.nickname + "'s team are gone!");
        } else if(index == 16) {
            System.out.println("The Sticky Web of " + target.nickname + "'s team is gone!");
        } else if(index == 17) {
            System.out.println("Guard Spec of " + target.nickname + "'s team is gone!");
        } else if(index == 18) {
            System.out.println("Aurora Veil of " + target.nickname + "'s team is gone!");
        }
    }

    private void giveNickname(Pokemon pkm) {
        System.out.println("Would you like to give a nickname to "+pkm.specie.name+"?");
        System.out.println("1: Yes\n2: No");
        if(in.nextLine().equals("1")) {
            System.out.println("What nickname do you want?");
            pkm.nickname = in.nextLine();
            if(pkm.nickname.replaceAll(" ", "").equals("")) {
                pkm.nickname = pkm.getSpecie().name;
            }
        }
    }
}
