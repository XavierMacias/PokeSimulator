package PokeBattle;

import PokeData.Category;
import PokeData.Movement;
import PokeData.Pokemon;
import PokeData.Team;

import java.util.Random;
import java.util.Scanner;

public class Battle {
    Team userTeam, rivalTeam;
    Pokemon firstAttacker, secondAttacker;
    Movement firstMove, secondMove;
    private boolean endBattle;
    public int turn;
    Scanner in;
    Random random;
    int battleResult;

    public Battle() {
        random = new Random();
        firstAttacker = null;
        secondAttacker = null;
        firstMove = null;
        secondMove = null;
        in = new Scanner(System.in);
    }

    public void WildSingleBattle(Team team1, Team team2) {
        battleResult = 0;
        turn = 1;
        endBattle = false;
        userTeam = team1;
        rivalTeam = team2;
        Pokemon rival = team2.getFirstAlivePokemon();

        System.out.println("A wild "+rival.nickname +"!");
        Pokemon user = team1.getFirstAlivePokemon();
        user.setParticipate(true);
        System.out.println("Go, "+user.nickname +"!");

        String battleChoose = "0";

        // battle loop
        do {
            System.out.println("What "+ user.nickname + " should do?");
            System.out.println("1: Fight\n2: Bag\n3: Pokemon\n4: Run");

            boolean decision = false;
            battleChoose = in.nextLine();
            switch (battleChoose) {
                case "1":
                    // fight
                    decision = fight(user,rival);
                    break;
                case "2":
                    // bag
                    break;
                case "3":
                    // pokemon
                    break;
                case "4":
                    // run
                    break;
            }
            if(decision) turn++;
            //System.out.println("Turn: " + turn);
            //endBattle = true;
        } while(!endBattle);

        endBattle(rival, false);
    }

    private boolean fight(Pokemon user, Pokemon rival) {
        int chosenIndex = -1;
        do {
            System.out.println("0: Exit");
            // moves list
            //TODO: check if there are remain PPs and if it must use STRUGGLE
            for(int i=0;i<user.getMoves().size();i++) {
                System.out.println((i+1)+": "+user.getMoves().get(i).getMove().name+" - "+user.getRemainPPs().get(i)+"/"+user.getMoves().get(i).getPP());
            }
            chosenIndex = Integer.parseInt(in.nextLine());
        } while(chosenIndex < 0 || chosenIndex > user.getMoves().size());

        // if we cancel, return to previous menu
        if(chosenIndex == 0) {
            return false;
        }
        Movement userMove = user.getMoves().get(chosenIndex-1).getMove();

        // choose rival move TODO: AI, for the moment is random
        Movement rivalMove = rival.getMoves().get(random.nextInt(rival.getMoves().size())).getMove();

        determinePriority(user,rival,userMove,rivalMove);
        useMove(firstAttacker,secondAttacker,firstMove,secondMove);
        if(checkFaint() != 0) {
            return true;
        }
        useMove(secondAttacker,firstAttacker,secondMove,firstMove);
        if(checkFaint() != 0) {
            return true;
        }

        return true;
    }

    private void useMove(Pokemon attacker, Pokemon defender, Movement attackerMove, Movement defenderMove) {
        System.out.println(attacker.nickname + " used " + attackerMove.name + "!");
        if(attackerMove.getPower() > 0) {
            int dmg = CalcDamage(attacker,defender,attackerMove);
            defender.reduceHP(dmg);
        }
        // TODO: get the secondary effects
        attacker.reducePP(attackerMove);

        System.out.println(defender.nickname + " HP: " + defender.getPsActuales() + "/" + defender.getHP());
    }

    private int checkFaint() {
        /* 0 -> nothing
           1 -> win
           2 -> lose
           3 -> run (only for wild battles)
           4 -> caught (only for wild battles)
        */
        if(userTeam.isTeamDefeated()) {
            // lose
            endBattle = true;
            battleResult = 2;
        } else if(rivalTeam.isTeamDefeated()) {
            // win
            endBattle = true;
            battleResult = 1;
        }
        return battleResult;
    }

    private void determinePriority(Pokemon user, Pokemon rival, Movement userMove, Movement rivalMove) {
        boolean userFirst;
        // moves priority
        if(userMove.getPriority() > rivalMove.getPriority()) {
            userFirst = true;
        } else if(userMove.getPriority() < rivalMove.getPriority()) {
            userFirst = false;
        // pokemon speed
        } else if(user.getVelocity() > rival.getVelocity()) {
            userFirst = true;
        } else if(user.getVelocity() < rival.getVelocity()) {
            userFirst = false;
        // random
        } else if(Math.random() < 0.5) {
            userFirst = true;
        } else {
            userFirst = false;
        }

        if(userFirst) {
            firstAttacker = user;
            secondAttacker = rival;
            firstMove = userMove;
            secondMove = rivalMove;
        } else {
            firstAttacker = rival;
            secondAttacker = user;
            firstMove = rivalMove;
            secondMove = userMove;
        }
    }

    private int CalcDamage(Pokemon attacker, Pokemon defender, Movement move) {
        int damage = 0;
        int attack = 0;
        int defense = 0;
        double stab = 1.0;
        int variation = attacker.utils.getRandomNumberBetween(85,101);
        boolean critical = isCriticalHit(attacker,defender,move);
        // get effectiveness
        double effectiveness = getEffectiveness(attacker, defender, move);
        // move power
        int power = move.getPower();
        // STAB
        if(attacker.hasType(move.type.getInternalName())) {
            stab = 1.5;
        }

        // move category, physical or special?
        if(move.getCategory() == Category.PHYSICAL) {
            attack = attacker.getAttack(critical);
            defense = defender.getDefense(critical);
        } else if(move.getCategory() == Category.SPECIAL) {
            attack = attacker.getSpecialAttack(critical);
            defense = defender.getSpecialDefense(critical);
        }
        // calculate damage
        damage = (int) (0.01*stab*effectiveness*variation*(((attack*power*(0.2*attacker.getLevel()+1))/(25*defense))+2));
        // critical hit
        if(critical) {
            damage *= 1.5;
        }

        return damage;
    }

    private boolean isCriticalHit(Pokemon attacker, Pokemon defender, Movement move) {
        int rand = attacker.utils.getRandomNumberBetween(0,101);
        if((attacker.criticalIndex == 0 && rand <= 4.167) || (attacker.criticalIndex == 1 && rand <= 12.5) ||
                (attacker.criticalIndex == 2 && rand <= 50) || attacker.criticalIndex >= 3) {
            return true;
        }
        return false;
    }
    private double getEffectiveness(Pokemon attacker, Pokemon defender, Movement move) {
        double effectiveness = 1.0;
        if(defender.getSpecie().type1 != null) {
            if(defender.getSpecie().type1.weaknesses.contains(move.type.getInternalName())) {
                effectiveness *= 2.0;
            } else if(defender.getSpecie().type1.resistances.contains(move.type.getInternalName())) {
                effectiveness *= 0.5;
            } else if(defender.getSpecie().type1.immunities.contains(move.type.getInternalName())) {
                effectiveness *= 0.0;
            }
        }
        if(defender.getSpecie().type2 != null) {
            if(defender.getSpecie().type2.weaknesses.contains(move.type.getInternalName())) {
                effectiveness *= 2.0;
            } else if(defender.getSpecie().type2.resistances.contains(move.type.getInternalName())) {
                effectiveness *= 0.5;
            } else if(defender.getSpecie().type2.immunities.contains(move.type.getInternalName())) {
                effectiveness *= 0.0;
            }
        }

        if(effectiveness >= 2.0) {
            System.out.println("It's very effective!");
        } else if(effectiveness <= 0.5) {
            System.out.println("It's not very effective...");
        } else if(effectiveness == 0.0) {
            System.out.println("It doesn't affect " + defender.nickname + "...");
        }

        return effectiveness;
    }

    private void endBattle(Pokemon rival, boolean trainer) {
        if(battleResult == 1) {
            // get experience
            userTeam.gainTeamExperience(rival,trainer);
            userTeam.battleEnded();
        } else if(battleResult == 2) {
            System.out.println("You lose!");
            // heal team
            userTeam.healTeam();
        }
    }
}
