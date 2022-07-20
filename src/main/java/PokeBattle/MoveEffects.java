package PokeBattle;

import PokeData.*;

import java.util.Optional;

public class MoveEffects {

    Battle battle;

    public MoveEffects(Battle battle) {
        this.battle = battle;
    }

    private boolean canPoison(Pokemon target, Pokemon other, boolean selfCaused) {
        //TODO: conditions for poison
        if(target.hasType("POISON") || target.hasType("STEEL")) {
            return false;
        }
        if(!target.getStatus().equals(Status.FINE)) {
            return false;
        }
        if(target.getTeam().effectTeamMoves.get(1) > 0 && !selfCaused) { // safeguard
            return false;
        }
        return true;
    }

    private boolean canBurn(Pokemon target, Pokemon other, boolean selfCaused) {
        //TODO: conditions for burn
        if(target.hasType("FIRE")) {
            return false;
        }
        if(!target.getStatus().equals(Status.FINE)) {
            return false;
        }
        if(target.getTeam().effectTeamMoves.get(1) > 0 && !selfCaused) { // safeguard
            return false;
        }
        return true;
    }

    private boolean canParalyze(Pokemon target, Pokemon other, boolean selfCaused) {
        //TODO: conditions for paralyze
        if(target.hasType("ELECTRIC")) {
            return false;
        }
        if(target.getAbility().getInternalName().equals("LIMBER")) {
            return false;
        }
        if(!target.getStatus().equals(Status.FINE)) {
            return false;
        }
        if(target.getTeam().effectTeamMoves.get(1) > 0 && !selfCaused) { // safeguard
            return false;
        }
        return true;
    }

    public boolean canSleep(Pokemon target, Pokemon other, boolean selfCaused) {
        //TODO: conditions for sleep
        if(!target.getStatus().equals(Status.FINE)) {
            return false;
        }
        if(target.getTeam().effectTeamMoves.get(1) > 0 && !selfCaused) { // safeguard
            return false;
        }
        return true;
    }

    public boolean canFreeze(Pokemon target, Pokemon other, boolean selfCaused) {
        //TODO: conditions for freeze
        if(target.hasType("ICE")) {
            return false;
        }
        if(!target.getStatus().equals(Status.FINE)) {
            return false;
        }
        if(target.getTeam().effectTeamMoves.get(1) > 0 && !selfCaused) { // safeguard
            return false;
        }
        return true;
    }

    private boolean canConfuse(Pokemon target, Pokemon other, boolean selfCaused) {
        //TODO: conditions for confusion
        if(target.hasTemporalStatus(TemporalStatus.CONFUSED)) {
            return false;
        }
        if(target.getAbility().getInternalName().equals("OWNTEMPO")) {
            return false;
        }
        if(target.getTeam().effectTeamMoves.get(1) > 0 && !selfCaused) { // safeguard
            return false;
        }
        return true;
    }

    private boolean canSeed(Pokemon target, Pokemon other) {
        //TODO: conditions for seed
        if(target.hasType("GRASS") || target.hasTemporalStatus(TemporalStatus.SEEDED)) {
            return false;
        }

        return true;
    }

    private boolean canFlinch(Pokemon target, Pokemon other) {
        //TODO: conditions for flinch
        if(target.hasTemporalStatus(TemporalStatus.FLINCHED)) {
            return false;
        }
        return true;
    }

    private boolean canDrows(Pokemon target, Pokemon other) {
        //TODO: conditions for drowsy
        if(target.effectMoves.get(6) > 0) {
            return false;
        }
        if(!target.getStatus().equals(Status.FINE)) {
            return false;
        }
        if(target.getTeam().effectTeamMoves.get(1) > 0) { // safeguard
            return false;
        }
        return true;
    }

    public boolean moveEffects(Movement move, Pokemon attacker, Pokemon defender, Movement defenderMove, int damage) {
        int effect = move.getCode();
        if(effect == 1 && !defender.isFainted()) {
            // decreases target attack - GROWL
            defender.changeStat(0,-1, false);
        }
        else if(effect == 2 && !defender.isFainted()) {
            // seed the target - LEECH SEED
            if(canSeed(defender, attacker)) {
                defender.causeTemporalStatus(TemporalStatus.SEEDED);
            } else {
                return false;
            }
        }
        else if((effect == 3 || effect == 65) && !defender.isFainted()) {
            // poisons the target - POISON STING, SLUDGE BOMB, POISON POWDER
            if(canPoison(defender, attacker, false)) {
                defender.causeStatus(Status.POISONED);
            } else {
                return false;
            }
        }
        else if(effect == 4 && !defender.isFainted()) {
            // sleeps the target - SLEEP POWDER, HYPNOSIS
            if(canSleep(defender, attacker, false)) {
                defender.causeStatus(Status.ASLEEP);
            } else {
                return false;
            }
        }
        else if(effect == 5) {
            // recoil damage - TAKE DOWN
            attacker.reduceHP(damage/4);
        }
        else if(effect == 6 && !defender.isFainted()) {
            // decreases target evasion - SWEET SCENT
            defender.changeStat(6,-1, false);
        }
        else if(effect == 7) {
            // increases user attack and special attack - GROWTH
            //TODO: weather increases more
            attacker.changeStat(0,1, true);
            attacker.changeStat(2,1, true);
        }
        else if(effect == 8) {
            // more recoil damage - DOUBLE EDGE, BRAVE BIRD, FLARE BLITZ
            attacker.reduceHP(damage/3);
            if(0.1 >= Math.random() && canBurn(defender, attacker, false) && move.getInternalName().equals("FLAREBLITZ")) {
                defender.causeStatus(Status.BURNED);
            }
        }
        else if(effect == 9) {
            // change ability to Insomnia - WORRY SEED
            if(defender.getAbility().getInternalName().equals("TRUANT") ||
                    defender.getAbility().getInternalName().equals("MULTITYPE") ||
                    defender.getAbility().getInternalName().equals("STANCECHANGE") ||
                    defender.getAbility().getInternalName().equals("SCHOOLING") ||
                    defender.getAbility().getInternalName().equals("COMATOSE") ||
                    defender.getAbility().getInternalName().equals("SHIELDSDOWN") ||
                    defender.getAbility().getInternalName().equals("DISGUISE") ||
                    defender.getAbility().getInternalName().equals("RKSSYSTEM") ||
                    defender.getAbility().getInternalName().equals("BATTLEBOND") ||
                    defender.getAbility().getInternalName().equals("POWERCONSTRUCT") ||
                    defender.getAbility().getInternalName().equals("ICEFACE") ||
                    defender.getAbility().getInternalName().equals("GULPMISSILE"))
            {
                return false;
            } else {
                defender.changeAbility("INSOMNIA");
            }
        }
        else if(effect == 10) {
            // recover HP depending on the weather - SYNTHESIS
            //TODO: weather recovers more or less
            return attacker.healHP(attacker.getHP()/4, true, true);
        }
        else if(effect == 11 && !attacker.isFainted()) {
            // first turn: load, second turn: attack - SKULL BASH, SOLAR BEAM
            //TODO: solar beam with weather and herb
            if(move.getInternalName().equals("SOLARBEAM")) {
                if(attacker.effectMoves.get(3) == 0) {
                    System.out.println(attacker.nickname + " is charging solar energy!");
                    attacker.recover1PP(move);
                    attacker.effectMoves.set(3,1);
                } else {
                    attacker.effectMoves.set(3,0);
                }
            } else if(move.getInternalName().equals("SKULLBASH")) {
                if(attacker.effectMoves.get(3) == 0) {
                    System.out.println(attacker.nickname + " bowed its head!");
                    attacker.changeStat(1,1, true);
                    attacker.recover1PP(move);
                    attacker.effectMoves.set(3,1);
                } else {
                    attacker.effectMoves.set(3,0);
                }
            }
        }
        else if(effect == 12 && !defender.isFainted()) {
            // decreases a lot target attack - CHARM, FEATHER DANCE...
            defender.changeStat(0,-2, false);
        }
        else if(effect == 13) {
            // curse the enemy if the user is Ghost-type. If not, increase attack, defense and decrease speed - CURSE
            if(attacker.hasType("GHOST")) {
                if(defender.hasTemporalStatus(TemporalStatus.CURSED)) {
                    return false;
                }
                System.out.println(attacker.nickname + " lost some of its HP and cursed " + defender.nickname + "!");
                attacker.reduceHP(attacker.getHP()/2);
                defender.causeTemporalStatus(TemporalStatus.CURSED);
            } else {
                attacker.changeStat(0,1, true);
                attacker.changeStat(1,1, true);
                attacker.changeStat(4,-1, true);
            }
        }
        else if(effect == 14) {
            // increases a lot the Special Defense of user - AMNESIA
            attacker.changeStat(3,2, true);
        }
        else if(effect == 15) {
            // turns in another move depends on the environment - NATURE POWER
            //TODO: turns in another move depends on the environment
        }
        else if(effect == 16) {
            // gets ingrain, recovers HPs in every turn - INGRAIN
            if(attacker.effectMoves.get(0) == 1) {
                return false;
            }
            attacker.effectMoves.set(0, 1);
            System.out.println(attacker.nickname + " gets ingrain!");
        }
        else if(effect == 17) {
            // decreases a lot the Special Attack of user - LEAF STORM, DRACO METEOR
            attacker.changeStat(2,-2, false);
        }
        else if(effect == 18) {
            // absorb HP to enemy and recovers 1/2 of the damage - GIGA DRAIN
            attacker.healHP(damage/2,true,false);
        }
        else if(effect == 19) {
            // resists an attack than could defeat user - ENDURE
            attacker.effectMoves.set(1, 1);
            attacker.protectTurns++;
            System.out.println(attacker.nickname + " is enduring!");
        }
        else if(effect == 20) {
            // attack 2-3 turns and gets confuse - PETAL DANCE, OUTRAGE
            //TODO: petal dance effect
        }
        else if(effect == 21) {
            // burns the target - EMBER, FLAME WHEEL, FLAMETHROWER...
            if(canBurn(defender, attacker, false)) {
                defender.causeStatus(Status.BURNED);
            } else {
                return false;
            }
        }
        else if(effect == 22 && !defender.isFainted()) {
            // decreases target accuracy - SMOKE SCREEN, SAND ATTACK
            defender.changeStat(5,-1, false);
        }
        else if(effect == 23) {
            // fix damage to 40 PS - DRAGON RAGE
            defender.reduceHP(40);
        }
        if(effect == 24 && !defender.isFainted()) {
            // decreases a lot target speed - SCARY FACE, STRING SHOT
            defender.changeStat(4,-2, false);
        }
        else if(effect == 25 && !defender.isFainted()) {
            // flinched target - BITE, HYPER FANG, AIR SLASH, TWISTER, FAKE OUT...
            if (canFlinch(defender, attacker)) {
                defender.causeTemporalStatus(TemporalStatus.FLINCHED);
            } else {
                return false;
            }
        } else if(effect == 26) {
            // damages rival ally - FLAME BURST
            //TODO: flame burst residual damage
        } else if(effect == 27 && !defender.isFainted()) {
            // partially trap enemy from 4-5 turns - FIRE SPIN, WHIRL POOL, SAND TOMB...
            if(defender.effectMoves.get(4) == 0) {
                defender.effectMoves.set(4, 1);
            }
            defender.causeTemporalStatus(TemporalStatus.PARTIALLYTRAPPED);
            System.out.println(defender.nickname + " was trapped by " + move.name);
        } else if(effect == 28) {
            // reduces HP but maximizes attack - BELLY DRUM
            if((attacker.getPsActuales() <= attacker.getHP()/2) || attacker.getStatChange(0) >= 4.0) {
                return false;
            }
            attacker.reduceHP(attacker.getHP()/2);
            attacker.changeStat(0, 12, true);

        } else if(effect == 29) {
            // increase all stats - ANCIENT POWER, SILVER WIND
            attacker.changeStat(0, 1, true);
            attacker.changeStat(1, 1, true);
            attacker.changeStat(2, 1, true);
            attacker.changeStat(3, 1, true);
            attacker.changeStat(4, 1, true);
        } else if(effect == 30 && !defender.isFainted()) {
            // burns or flinches the target - FIRE FANG
            if (canBurn(defender, attacker, false) && Math.random() <= 0.1) {
                defender.causeStatus(Status.BURNED);
            }
            if (canFlinch(defender, attacker) && Math.random() <= 0.1) {
                defender.causeTemporalStatus(TemporalStatus.FLINCHED);
            }
        } else if(effect == 32) {
            // increase user attack and speed - DRAGON DANCE
            attacker.changeStat(0,1, true);
            attacker.changeStat(4,1, true);
        } else if(effect == 33 && !defender.isFainted()) {
            // decreases target defense - CRUNCH, TAIL WHIP, LEER
            defender.changeStat(1,-1, false);
        } else if(effect == 34) {
            // increase user attack - METAL CLAW
            attacker.changeStat(0, 1, true);
        } else if(effect == 35) {
            // returns the double of physical damage - COUNTER
            if(attacker.previousDamage > 0 && attacker.lastMoveInThisTurn.getCategory().equals(Category.PHYSICAL) && defender.hasType("GHOST")) {
                defender.reduceHP(attacker.previousDamage*2);
            } else {
                return false;
            }
        } else if(effect == 36) {
            // paralyzes the target - STUN SPORE, THUNDERBOLT, THUNDER
            if(canParalyze(defender, attacker, false)) {
                defender.causeStatus(Status.PARALYZED);
            } else {
                return false;
            }
        } else if(effect == 37) {
            // charges and in the end of turn attacks - FOCUS PUNCH
            //TODO: focus punch
        } else if(effect == 38) {
            // increase user defense - WITHDRAW, HARDEN, STEEL WING...
            attacker.changeStat(1, 1, true);
        } else if(effect == 39 && !defender.isFainted()) {
            // decreases target speed - BUBBLE
            defender.changeStat(4,-1, false);
        } else if(effect == 40) {
            // increases user speed - RAPID SPIN
            attacker.changeStat(4,1, true);
        } else if(effect == 41) {
            // protects user - PROTECT, DETECT
            attacker.effectMoves.set(2, 1);
            attacker.protectTurns++;
            System.out.println(attacker.nickname + " is protecting itself!");
        } else if(effect == 42 && !defender.isFainted()) {
            // confuse target - SUPERSONIC, CONFUSION, CONFUSE RAY, SIGNAL BEAM, WATER PULSE...
            if (canConfuse(defender, attacker, false)) {
                defender.causeTemporalStatus(TemporalStatus.CONFUSED);
            } else {
                return false;
            }
        } else if(effect == 43) {
            // increases a lot of user defense - IRON DEFENSE
            attacker.changeStat(1,2, true);
        } else if(effect == 44) {
            // starts to rain - RAIN DANCE
            //TODO: rain dance
        } else if(effect == 46) {
            // returns the double of special damage - MIRROR COAT
            if(attacker.previousDamage > 0 && attacker.lastMoveInThisTurn.getCategory().equals(Category.SPECIAL) && defender.hasType("DARK")) {
                defender.reduceHP(attacker.previousDamage*2);
            } else {
                return false;
            }
        } else if(effect == 47) {
            // prevents stat decreasing from all team - MIST
            if(attacker.getTeam().effectTeamMoves.get(0) == 0) {
                attacker.getTeam().effectTeamMoves.set(0, 1);
                System.out.println(attacker.nickname + "'s team is surrounded by Mist!");
            } else {
                return false;
            }
        } else if(effect == 48) {
            // restore all stat changes to 0 - HAZE
            attacker.getStatChanges().replaceAll(ignored -> 0);
            System.out.println("The stat changes were removed!");
        } else if(effect == 49) {
            // ignores enemy's evasion and user's accuracy and Ghost type can be damaged by Normal/Fighting moves - FORE SIGHT
            if (defender.effectMoves.get(5) == 0) {
                defender.effectMoves.set(5, 1);
                System.out.println(defender.nickname + " was identified!");
            } else {
                return false;
            }
        }
        if(effect == 50) {
            // heal user from poison, paralysis or burn - REFRESH
            if (attacker.hasStatus(Status.POISONED) || attacker.hasStatus(Status.BADLYPOISONED) || attacker.hasStatus(Status.PARALYZED)
                    || attacker.hasStatus(Status.BURNED)) {
                attacker.healPermanentStatus();
            } else {
                return false;
            }
        } else if(effect == 51) {
            // numb the target, and it will sleep in the next turn - YAWN
            if (canDrows(defender,attacker)) {
                defender.effectMoves.set(6, 1);
                System.out.println(defender.nickname + " is drowsy!");
            } else {
                return false;
            }
        } else if(effect == 52) {
            // reduces the electric moves power - MUD SPORT
            battle.effectFieldMoves.set(0, 1);
            System.out.println("The power of Electric moves are reduced!");
        } else if(effect == 53) {
            // recover HP in every turn - AQUA RING
            if (attacker.effectMoves.get(7) == 0) {
                attacker.effectMoves.set(7, 1);
                System.out.println(attacker.nickname + " was involved in an aqua ring!");
            } else {
                return false;
            }
        } else if(effect == 56 && !defender.isFainted()) {
            // decreases the Special Defense of target - FLASH CANNON, BUG BUZZ
            defender.changeStat(3,-1, false);
        } else if(effect == 57) {
            // steals the equipped berry of target - BUG BITE, PLUCK
            //TODO: bug bite effect
        } else if(effect == 58) {
            // prevents status problems for all team - SAFEGUARD
            if(attacker.getTeam().effectTeamMoves.get(1) == 0) {
                attacker.getTeam().effectTeamMoves.set(1, 1);
                System.out.println(attacker.nickname + "'s team is protected by Safeguard!");
            } else {
                return false;
            }
        } else if(effect == 59) {
            // makes the target flee - WHIRL WIND, ROAR
            //TODO: whirl wind effect
        } else if(effect == 60) {
            // makes the user the center of attention meanwhile this turn - RAGE POWDER
            //TODO: rage powder effect
        } else if(effect == 61 && !defender.isFainted()) {
            // decreases target special attack if is opposite sex - CAPTIVATE
            if((attacker.getGender() != defender.getGender()) && (attacker.getGender() != 2 && defender.getGender() != 2) &&
            !defender.getAbility().getInternalName().equals("OBLIVIOUS")) {
                defender.changeStat(2,-2, false);
            } else {
                return false;
            }
        } else if(effect == 62) {
            // duplicates all team speed meanwhile 4 turns - TAIL WIND
            if(attacker.getTeam().effectTeamMoves.get(2) == 0) {
                attacker.getTeam().effectTeamMoves.set(2, 1);
                System.out.println(attacker.nickname + "'s team has Tail Wind blowing on their favour!");
            } else {
                return false;
            }
        } else if(effect == 63) {
            // increase user special attack, special defense and speed - QUIVER DANCE
            attacker.changeStat(2,1, true);
            attacker.changeStat(3,1, true);
            attacker.changeStat(4,1, true);
        } else if(effect == 64 && defender.isFainted()) {
            // increase a lot the user attack if it faints opponent - FELL STINGER
            attacker.changeStat(0,3, true);
        } else if(effect == 86) {
            // increase a lot of user attack - SWORDS DANCE
            attacker.changeStat(0,2, true);
        }

        return true;
    }
}
