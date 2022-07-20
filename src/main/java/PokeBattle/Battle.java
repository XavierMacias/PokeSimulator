package PokeBattle;

import PokeData.*;

import javax.swing.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
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
    public List<Integer> effectFieldMoves;

    public Battle() {
        random = new Random();
        firstAttacker = null;
        secondAttacker = null;
        firstMove = null;
        secondMove = null;
        in = new Scanner(System.in);
        moveEffects = new MoveEffects(this);

        effectFieldMoves = new ArrayList<Integer>();
        // mud sport,
        for(int i=0;i<5;i++) {
            effectFieldMoves.add(0);
        }
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

        user.pokeTurn = 1;
        rival.pokeTurn = 1;
        String battleChoose = "0";

        // battle loop
        do {
            boolean decision = false;

            //TODO: use 2 turns attacks automatically
            if(user.effectMoves.get(3) == 1) {
                decision = true;
                battleChoose = "1";
            } else {
                System.out.println("What "+ user.nickname + " should do?");
                System.out.println("1: Fight\n2: Bag\n3: Pokemon\n4: Run");

                battleChoose = in.nextLine();
            }

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
                fieldEffects();
                turn++;
            }
            //System.out.println("Turn: " + turn);
            //endBattle = true;
        } while(!endBattle);

        endBattle(rival, false);
    }

    private boolean fight(Pokemon user, Pokemon rival) {
        int chosenIndex = -1;
        Movement userMove;

        if(user.effectMoves.get(3) == 1) {
            userMove = user.previousMove;
        } else {
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
            userMove = user.getMoves().get(chosenIndex-1).getMove();
        }

        // choose rival move TODO: AI, for the moment is random
        Movement rivalMove = rival.getMoves().get(random.nextInt(rival.getMoves().size())).getMove();
        if(rival.effectMoves.get(3) == 1) {
            rivalMove = rival.previousMove;
        }


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

        // protect, detect, endure
        if(attackerMove.getCode() != 19 && attackerMove.getCode() != 41) {
            attacker.protectTurns = 0;
        }

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
            attacker.previousMove = attackerMove;
            System.out.println(attacker.nickname + " used " + attackerMove.name + "!");
            int moveAccuracy = attackerMove.getAccuracy();
            // changes in move accuracy
            // protect moves reduce accuracy depending on turns
            if(attackerMove.getCode() == 19 || attackerMove.getCode() == 41) {
                moveAccuracy = (int) (100.0/(Math.pow(2,attacker.protectTurns)));
            }

            double accuracy = attacker.getAccuracy();
            double evasion = defender.getEvasion();
            // changes in attacker accuracy

            //changes in defender evasion

            // fore sight
            if(defender.effectMoves.get(5) > 0) {
                accuracy = 1.0;
                evasion = 1.0;
            }

            double a = (moveAccuracy / 100.0) * (accuracy / evasion);
            // calculate precision
            if (a >= Math.random() || moveAccuracy == 0) {
                // rival is protecting
                if(defender.effectMoves.get(2) == 1 && attackerMove.getFlags().contains("b")) {
                    System.out.println(defender.nickname + " has protected!");
                } // moves that will fail
                else if(attackerMove.getInternalName().equals("FAKEOUT") && attacker.pokeTurn > 1) {
                    System.out.println("But it failed!");
                } else {
                    int hits = 1;
                    ArrayList<Pokemon> beatUp = new ArrayList<Pokemon>();
                    // attack more than one time in 1 turn
                    if(attackerMove.getCode() == 31) {
                        beatUp = attacker.getTeam().getBeatUpTeam(attacker);
                        hits = beatUp.size();
                    } else if(attackerMove.getCode() == 65) { // twin needle
                        hits = 2;
                    } else if(attackerMove.getCode() == 66) { // fury attack, pin missile
                        double prob = Math.random();
                        if(0.125 >= prob) {
                            hits = 5;
                        } else if(0.25 >= prob) {
                            hits = 4;
                        } else if(0.625 >= prob) {
                            hits = 3;
                        } else {
                            hits = 2;
                        }
                    }

                    for(int i=0;i<hits;i++) {
                        // BEAT UP
                        if(attackerMove.getCode() == 31) {
                            attacker = beatUp.get(i);
                        }

                        if ((attackerMove.getPower() != 0 && attackerMove.getCode() != 11) || (attackerMove.getCode() == 11 && attacker.effectMoves.get(3) == 1)) {
                            dmg = CalcDamage(attacker, defender, attackerMove);
                            if(dmg > 0) {
                                defender.reduceHP(dmg);
                                defender.lastMoveInThisTurn = attackerMove;
                                defender.previousDamage = dmg;

                                if (attackerMove.getAddEffect() == 0 || ((attackerMove.getAddEffect() / 100.0) >= Math.random())) {
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
                    }
                    // effects after attacks - LIFE ORB, ROUGH SKIN...
                    // last effects - RAPID SPIN...
                    if(attackerMove.getInternalName().equals("RAPIDSPIN") && !attacker.isFainted()) {
                        attacker.rapidSpin();
                    }
                }

            } else {
                // move failed
                System.out.println(attacker.nickname + "'s move missed!");
                attacker.protectTurns = 0;
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

        // fire spin, etc
        if(0.5 >= Math.random() && target.effectMoves.get(4) == 4) {
            target.healTempStatus(TemporalStatus.PARTIALLYTRAPPED, false);
            System.out.println(target.nickname + " was freed from Fire Spin!");
            target.effectMoves.set(4, 0);
        } else if(target.effectMoves.get(4) >= 5) {
            target.healTempStatus(TemporalStatus.PARTIALLYTRAPPED, false);
            System.out.println(target.nickname + " was freed from Fire Spin!");
            target.effectMoves.set(4, 0);
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

        int changePower = moveChangingPower(attacker,defender,move);
        // moves with variable power
        if(changePower != -1) {
            power = changePower;
        }

        // power change for external reasons
        if(effectFieldMoves.get(0) > 0 && move.type.getInternalName().equals("ELECTRIC")) { // mud sport
            power *= 0.667;
        }

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
        // resists with 1 HP
        if(defender.getPsActuales()-damage <= 0) {
            if(defender.effectMoves.get(1) == 1) { // endure
                damage = defender.getPsActuales()-1;
            }
            //TODO: focus band, false swipe, etc...
        }

        return damage;
    }

    private int moveChangingPower(Pokemon attacker, Pokemon defender, Movement move) {
        int p = -1;
        if(move.getCode() == 31) { // beat up
            p = 5+(attacker.getSpecie().stats.get(1)/10);
        }
        if(move.getCode() == 45) { // flail, reversal
            if(attacker.getPercentHP() >= 68.75) {
                p = 20;
            } else if(attacker.getPercentHP() >= 35.42) {
                p = 40;
            } else if(attacker.getPercentHP() >= 20.83) {
                p = 80;
            } else if(attacker.getPercentHP() >= 10.42) {
                p = 100;
            } else if(attacker.getPercentHP() >= 4.17) {
                p = 150;
            } else {
                p = 200;
            }
        }
        if(move.getCode() == 54) { // water spout
            p = move.getPower()*(attacker.getPsActuales()/attacker.getHP());
            if(p >= 0.0 && p < 1.0) {
                p = 1;
            }
        }
        if(move.getCode() == 55 && defender.getPercentHP() <= 50.0) { // brine
            p = move.getPower()*2;
        }

        return p;
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
                // changes in effectiveness
                if(defender.effectMoves.get(5) > 0 && defender.hasType("GHOST") && // foresight
                        (move.type.getInternalName().equals("NORMAL") || move.type.getInternalName().equals("FIGHTING"))) {
                    effectiveness *= 1.0;
                } else {
                    effectiveness *= 0.0;
                }

            }
        }
        if(defender.getSpecie().type2 != null) {
            if(defender.getSpecie().type2.weaknesses.contains(move.type.getInternalName())) {
                effectiveness *= 2.0;
            } else if(defender.getSpecie().type2.resistances.contains(move.type.getInternalName())) {
                effectiveness *= 0.5;
            } else if(defender.getSpecie().type2.immunities.contains(move.type.getInternalName())) {
                // changes in effectiveness
                if(defender.effectMoves.get(5) > 0 && defender.hasType("GHOST") && // foresight
                        (move.type.getInternalName().equals("NORMAL") || move.type.getInternalName().equals("FIGHTING"))) {
                    effectiveness *= 1.0;
                } else {
                    effectiveness *= 0.0;
                }
            }
        }

        if(effectiveness >= 2.0) {
            System.out.println("It's very effective!");
        } else if(effectiveness <= 0.5 && effectiveness > 0.0) {
            System.out.println("It's not very effective...");
        } else if(effectiveness <= 0.0) {
            System.out.println("It doesn't affect " + defender.nickname + "...");
        }

        return effectiveness;
    }

    private void endTurn(Pokemon target, Pokemon other) {

        target.lastMoveInThisTurn = null;
        target.previousDamage = 0;
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
        // cursed
        if(target.hasTemporalStatus(TemporalStatus.CURSED) && !target.isFainted()) {
            System.out.println(target.nickname + " is affected by Curse!");
            target.reduceHP(target.getHP()/4);
        }
        // leech seed
        if(target.hasTemporalStatus(TemporalStatus.SEEDED) && !target.isFainted()) {
            System.out.println(target.nickname + " is seeded by Leech Seed!");
            target.reduceHP(target.getHP()/8);
            other.healHP(target.getHP()/8, true, false);
        }
        if(target.hasTemporalStatus(TemporalStatus.PARTIALLYTRAPPED) && target.effectMoves.get(4) >= 1) {
            System.out.println(target.nickname + " is hurt by Fire Spin!");
            target.reduceHP(target.getHP()/8);
            target.effectMoves.set(4, target.effectMoves.get(4)+1);
        }

        // ingrain
        if(!target.hasAllHP() && target.effectMoves.get(0) == 1 && !target.isFainted()) {
            System.out.println(target.nickname + " obtains energy from roots!");
            target.healHP(target.getHP()/16, true, true);
        }
        // aqua ring
        if(!target.hasAllHP() && target.effectMoves.get(7) == 1 && !target.isFainted()) {
            System.out.println(target.nickname + " recover HP by Aqua Ring!");
            target.healHP(target.getHP()/16, true, true);
        }

        // remove endure, protect, detect...
        target.effectMoves.set(1, 0); // endure
        target.effectMoves.set(2, 0); // protect

        // count and remove individual effects
        if(target.effectMoves.get(6) > 0) { // yawn
            target.increaseEffectMove(6); // increase turn
            if(target.effectMoves.get(6) > 2) {
                if(moveEffects.canSleep(target,other,true)) {
                    target.causeStatus(Status.ASLEEP);
                }
                target.effectMoves.set(6, 0);
            }
        }

        // count and remove team effects
        if(target.getTeam().effectTeamMoves.get(0) > 0) { // mist
            target.getTeam().increaseEffectMove(0); // increase turn
            if(target.getTeam().effectTeamMoves.get(0) > 5) {
                target.getTeam().effectTeamMoves.set(0, 0);
                System.out.println("The mist of " + target.nickname + "'s team is gone!");
            }
        }
        if(target.getTeam().effectTeamMoves.get(1) > 0) { // safeguard
            target.getTeam().increaseEffectMove(1); // increase turn
            if(target.getTeam().effectTeamMoves.get(1) > 5) {
                target.getTeam().effectTeamMoves.set(1, 0);
                System.out.println("The Safeguard of " + target.nickname + "'s team is gone!");
            }
        }
        if(target.getTeam().effectTeamMoves.get(2) > 0) { // tailwind
            target.getTeam().increaseEffectMove(2); // increase turn
            if(target.getTeam().effectTeamMoves.get(2) > 4) {
                target.getTeam().effectTeamMoves.set(2, 0);
                System.out.println("The Tail Wind of " + target.nickname + "'s team has gone!");
            }
        }

        // turn counter
        if(target.hasStatus(Status.BADLYPOISONED)) {
            target.badPoisonTurns++;
        }
        if(target.hasStatus(Status.ASLEEP)) {
            target.sleepTurns++;
        }

        target.pokeTurn++;
        System.out.println("Turn: " + turn);
    }

    private void increaseEffectMove(int index) {
        effectFieldMoves.set(index,effectFieldMoves.get(index)+1);
    }

    private void fieldEffects() {
        // count and remove battle effects
        if(effectFieldMoves.get(0) > 0) { // mud sport
            increaseEffectMove(0); // increase turn
            if(effectFieldMoves.get(0) > 5) {
                effectFieldMoves.set(0, 0);
            }
        }
    }

    private void endBattle(Pokemon rival, boolean trainer) {
        // remove field effects
        for(int i=0;i<effectFieldMoves.size();i++) {
            effectFieldMoves.set(i,0);
        }

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
