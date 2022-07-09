package PokeBattle;

import PokeData.*;

import javax.swing.*;
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
    MoveEffects moveEffects;

    public Battle() {
        random = new Random();
        firstAttacker = null;
        secondAttacker = null;
        firstMove = null;
        secondMove = null;
        in = new Scanner(System.in);
        moveEffects = new MoveEffects(this);
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
            if(decision) {
                if(checkFaint() != 0) {
                    break;
                }
                // end turn
                endTurn(firstAttacker,secondAttacker);
                if(checkFaint() != 0) {
                    break;
                }
                endTurn(secondAttacker,firstAttacker);
                if(checkFaint() != 0) {
                    break;
                }
                turn++;
            }
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
            //TODO: check if it must use STRUGGLE
            for(int i=0;i<user.getMoves().size();i++) {
                System.out.println((i+1)+": "+user.getMoves().get(i).getMove().name+" - "+user.getRemainPPs().get(i)+"/"+user.getMoves().get(i).getPP());
            }
            chosenIndex = Integer.parseInt(in.nextLine());
            if(user.hasPPByIndex(chosenIndex-1) == 0) {
                System.out.println("There are no PPs for this move!");
                chosenIndex = -1;
            }
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
        int dmg = 0;
        // StartTurn
        startTurn(attacker,defender);

        // flinched
        if(attacker.hasTemporalStatus(TemporalStatus.FLINCHED)) {
            System.out.println(attacker.nickname + " flinched!");
        // paralyzed
        } else if(attacker.hasStatus(Status.PARALYZED) && 0.25 >= Math.random()) {
            System.out.println(attacker.nickname + " is paralyzed! It's unable to move!");
        } else if(attacker.hasStatus(Status.FROZEN)) {
            System.out.println(attacker.nickname + " is frozen!");
            // asleep
        } else if(attacker.hasStatus(Status.ASLEEP)) {
            System.out.println(attacker.nickname + " is asleep!");
        } else {
            // infatuated
            if(attacker.hasTemporalStatus(TemporalStatus.INFATUATED)) {
                System.out.println(attacker.nickname + " is infatuated of " + defender.nickname + "!");
                if(0.5 >= Math.random()) {
                    System.out.println("Love prevents " + attacker.nickname + " from attacking!");
                    return;
                }
            }
            // confused
            if(attacker.hasTemporalStatus(TemporalStatus.CONFUSED)) {
                System.out.println(attacker.nickname + " is confused!");
                if(0.33 >= Math.random()) {
                    System.out.println("It's so confused hurts itself!");
                    attacker.reduceHP(CalcDamageConfuse(attacker));
                    System.out.println(attacker.nickname + " HP: " + attacker.getPsActuales() + "/" + attacker.getHP());
                    return;
                }
            }
            System.out.println(attacker.nickname + " used " + attackerMove.name + "!");
            int moveAccuracy = attackerMove.getAccuracy();
            double a = (moveAccuracy / 100.0) * (attacker.getAccuracy() / defender.getEvasion());

            // calculate precision
            if (a >= Math.random() || moveAccuracy == 0) {
                if (attackerMove.getPower() > 0) {
                    dmg = CalcDamage(attacker, defender, attackerMove);
                    if (dmg > 0) {
                        defender.reduceHP(dmg);
                        if (checkFaint() == 0 && (attackerMove.getAddEffect() == 0 || ((attackerMove.getAddEffect() / 100.0) >= Math.random()))) {
                            moveEffects.moveEffects(attackerMove, attacker, defender, defenderMove, dmg);
                        }
                    }
                } else {
                    if (attackerMove.getAddEffect() == 0 || ((attackerMove.getAddEffect() / 100.0) >= Math.random())) {
                        if (!moveEffects.moveEffects(attackerMove, attacker, defender, defenderMove, dmg)) {
                            System.out.println("But it failed!");
                        }
                    }
                }
            } else {
                // move failed
                System.out.println(attacker.nickname + "'s move missed!");
            }
            attacker.reducePP(attackerMove);
            System.out.println(defender.nickname + " HP: " + defender.getPsActuales() + "/" + defender.getHP());
        }
    }

    private void startTurn(Pokemon target, Pokemon other) {

        // thaw
        if(0.2 >= Math.random() && target.hasStatus(Status.FROZEN)) {
            System.out.println(target.nickname + " thaws!");
            target.healPermanentStatus();
        }
        // wake up
        if(0.33 >= Math.random() && target.sleepTurns == 1) {
            target.healPermanentStatus();
        } else if(0.66 >= Math.random() && target.sleepTurns == 2) {
            target.healPermanentStatus();
        } else if(target.sleepTurns >= 3) {
            target.healPermanentStatus();
        }
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
        double dmg = (0.01*stab*effectiveness*variation*(((attack*power*(0.2*attacker.getLevel()+1))/(25*defense))+2));
        // critical hit
        if(critical) {
            dmg *= 1.5;
        }

        // the minimum damage is always 1
        damage = (int) dmg;
        if(dmg < 1.0 && dmg > 0.0) {
            damage = 1;
        }
        return damage;
    }

    private int CalcDamageConfuse(Pokemon attacker) {
        int damage = 0;
        int attack = attacker.getAttack(false);
        int defense = attacker.getDefense(false);
        int variation = attacker.utils.getRandomNumberBetween(85,101);

        // calculate damage
        double dmg = (0.01*variation*(((attack*40*(0.2*attacker.getLevel()+1))/(25*defense))+2));

        // the minimum damage is always 1
        damage = (int) dmg;
        if(dmg < 1.0 && dmg > 0.0) {
            damage = 1;
        }
        return damage;
    }

    private boolean isCriticalHit(Pokemon attacker, Pokemon defender, Movement move) {
        int rand = attacker.utils.getRandomNumberBetween(0,101);
        return (attacker.criticalIndex == 0 && rand <= 4.167) || (attacker.criticalIndex == 1 && rand <= 12.5) ||
                (attacker.criticalIndex == 2 && rand <= 50) || attacker.criticalIndex >= 3;
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
        } else if(effectiveness <= 0.5 && effectiveness > 0.0) {
            System.out.println("It's not very effective...");
        } else if(effectiveness == 0.0) {
            System.out.println("It doesn't affect " + defender.nickname + "...");
        }

        return effectiveness;
    }

    private void endTurn(Pokemon target, Pokemon other) {

        // remove flinch
        target.healTempStatus(TemporalStatus.FLINCHED,false);
        // weather

        // poison
        if(target.hasStatus(Status.POISONED) && !target.isFainted()) {
            System.out.println(target.nickname + " is affected by the poison!");
            target.reduceHP(target.getHP()/8);
        }
        // badly poison
        if(target.hasStatus(Status.BADLYPOISONED) && !target.isFainted()) {
            System.out.println(target.nickname + " is affected by the poison!");
            target.reduceHP((int) (target.getHP()*(target.badPoisonTurns/16.0)));
        }
        // burn
        if(target.hasStatus(Status.BURNED) && !target.isFainted()) {
            System.out.println(target.nickname + " is affected by the burn!");
            target.reduceHP(target.getHP()/16);
        }
        // leech seed
        if(target.hasTemporalStatus(TemporalStatus.SEEDED) && !target.isFainted()) {
            System.out.println(target.nickname + " is seeded by Leech Seed!");
            target.reduceHP(target.getHP()/8);
            other.healHP(target.getHP()/8, true, false);
        }

        // turn counter
        if(target.hasStatus(Status.BADLYPOISONED)) {
            target.badPoisonTurns++;
        }
        if(target.hasStatus(Status.ASLEEP)) {
            target.sleepTurns++;
        }

        System.out.println("Turn: " + turn);
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
