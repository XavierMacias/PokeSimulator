package PokeBattle;

import PokeData.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class MoveEffects {

    Battle battle;
    ArrayList<Integer> attacksWithSecondaryEffects; // attack with secondary effects that affect the opponent (SHIELD DUST)

    public MoveEffects(Battle battle) {
        this.battle = battle;
        attacksWithSecondaryEffects = new ArrayList<>(Arrays.asList(1, 3, 4, 6, 12, 21, 22, 24, 25, 30, 33, 36, 39, 42, 56, 65, 78, 81, 92, 97, 101, 102, 118));
    }

    public boolean moveEffects(Movement move, Pokemon attacker, Pokemon defender, Movement defenderMove, int damage) {
        int effect = move.getCode();
        if(attacksWithSecondaryEffects.contains(effect) && defender.isFainted()) {
            return true;
        }
        if(defender.hasAbility("SHIELDDUST") && move.getAddEffect() > 0 && attacksWithSecondaryEffects.contains(effect)) {
            return true;
        }

        if (effect == 1) {
            // decreases target attack - GROWL
            defender.changeStat(0, -1, false, move.getAddEffect() == 0);
        } else if (effect == 2) {
            // seed the target - LEECH SEED
            if (defender.canSeed()) {
                defender.causeTemporalStatus(TemporalStatus.SEEDED);
            } else {
                return false;
            }
        } else if (effect == 3 || effect == 65) {
            // poisons the target - POISON STING, SLUDGE BOMB, POISON POWDER
            if (defender.canPoison(false)) {
                defender.causeStatus(Status.POISONED);
            } else {
                return false;
            }
        } else if (effect == 4) {
            // sleeps the target - SLEEP POWDER, HYPNOSIS
            if (defender.canSleep(false)) {
                defender.causeStatus(Status.ASLEEP);
            } else {
                return false;
            }
        } else if (effect == 5) {
            // recoil damage - TAKE DOWN
            attacker.reduceHP(damage / 4);
        } else if (effect == 6) {
            // decreases target evasion - SWEET SCENT
            defender.changeStat(6, -1, false, move.getAddEffect() == 0);
        } else if (effect == 7) {
            // increases user attack and special attack - GROWTH
            int quantity = 1;
            if (battle.weather.hasWeather(Weathers.SUNLIGHT) || battle.weather.hasWeather(Weathers.HEAVYSUNLIGHT)) {
                quantity = 2;
            }
            attacker.changeStat(0, quantity, true, move.getAddEffect() == 0);
            attacker.changeStat(2, quantity, true, move.getAddEffect() == 0);
        } else if (effect == 8) {
            // more recoil damage - DOUBLE EDGE, BRAVE BIRD, FLARE BLITZ, VOLT TACKLE
            attacker.reduceHP(damage / 3);
            if (0.1 >= Math.random() && defender.canBurn(false) && move.hasName("FLAREBLITZ")) {
                defender.causeStatus(Status.BURNED);
            } else if (0.1 >= Math.random() && defender.canParalyze(false) && move.hasName("VOLTTACKLE")) {
                defender.causeStatus(Status.PARALYZED);
            }
        } else if (effect == 9) {
            // change ability to Insomnia - WORRY SEED
            if (defender.hasAbility("TRUANT") || defender.hasAbility("MULTITYPE") || defender.hasAbility("STANCECHANGE") ||
                    defender.hasAbility("SCHOOLING") || defender.hasAbility("COMATOSE") || defender.hasAbility("SHIELDSDOWN") ||
                    defender.hasAbility("DISGUISE") || defender.hasAbility("RKSSYSTEM") || defender.hasAbility("BATTLEBOND") ||
                    defender.hasAbility("POWERCONSTRUCT") || defender.hasAbility("ICEFACE") || defender.hasAbility("GULPMISSILE")) {
                return false;
            } else {
                defender.changeAbility("INSOMNIA");
            }
        } else if (effect == 10) {
            // recover HP depending on the weather - SYNTHESIS
            int quantity = attacker.getHP() / 2;

            if (battle.weather.hasWeather(Weathers.SUNLIGHT) || battle.weather.hasWeather(Weathers.HEAVYSUNLIGHT)) {
                quantity = attacker.getHP() * 2 / 3;
            } else if (battle.weather.hasWeather(Weathers.RAIN) || battle.weather.hasWeather(Weathers.HEAVYRAIN) ||
                    battle.weather.hasWeather(Weathers.HAIL) || battle.weather.hasWeather(Weathers.SANDSTORM) ||
                    battle.weather.hasWeather(Weathers.FOG)) {
                quantity = attacker.getHP() / 4;
            }

            return attacker.healHP(quantity, true, true);
        } else if (effect == 11 && !attacker.isFainted()) {
            // first turn: load, second turn: attack - SKULL BASH, SOLAR BEAM
            //TODO: solar beam with weather and herb
            if (move.hasName("SOLARBEAM")) {
                if (attacker.effectMoves.get(3) == 0 && !battle.weather.hasWeather(Weathers.SUNLIGHT) && !battle.weather.hasWeather(Weathers.HEAVYSUNLIGHT)) {
                    System.out.println(attacker.nickname + " is charging solar energy!");
                    attacker.recover1PP(move);
                    attacker.effectMoves.set(3, 1);
                } else {
                    attacker.effectMoves.set(3, 0);
                }
            } else if (move.hasName("SKULLBASH")) {
                if (attacker.effectMoves.get(3) == 0) {
                    System.out.println(attacker.nickname + " bowed its head!");
                    attacker.changeStat(1, 1, true, move.getAddEffect() == 0);
                    attacker.recover1PP(move);
                    attacker.effectMoves.set(3, 1);
                } else {
                    attacker.effectMoves.set(3, 0);
                }
            } else if (move.hasName("RAZORWIND")) {
                if (attacker.effectMoves.get(3) == 0) {
                    System.out.println(attacker.nickname + " raised a whirlwind!");
                    attacker.recover1PP(move);
                    attacker.effectMoves.set(3, 1);
                } else {
                    attacker.effectMoves.set(3, 0);
                }
            } else if (move.hasName("SKYATTACK")) {
                if (attacker.effectMoves.get(3) == 0) {
                    System.out.println(attacker.nickname + " is charging!");
                    attacker.recover1PP(move);
                    attacker.effectMoves.set(3, 1);
                } else {
                    // sky attack can flinch enemy
                    attacker.effectMoves.set(3, 0);
                    if (Math.random() <= 0.3 && attacker.canFlinch()) {
                        defender.causeTemporalStatus(TemporalStatus.FLINCHED);
                    }
                }
            } //TODO: dig
        } else if (effect == 12) {
            // decreases a lot target attack - CHARM, FEATHER DANCE...
            defender.changeStat(0, -2, false, move.getAddEffect() == 0);
        } else if (effect == 13) {
            // curse the enemy if the user is Ghost-type. If not, increase attack, defense and decrease speed - CURSE
            if (attacker.hasType("GHOST")) {
                if (defender.hasTemporalStatus(TemporalStatus.CURSED)) {
                    return false;
                }
                System.out.println(attacker.nickname + " lost some of its HP and cursed " + defender.nickname + "!");
                attacker.reduceHP(attacker.getHP() / 2);
                defender.causeTemporalStatus(TemporalStatus.CURSED);
            } else {
                attacker.changeStat(0, 1, true, move.getAddEffect() == 0);
                attacker.changeStat(1, 1, true, move.getAddEffect() == 0);
                attacker.changeStat(4, -1, true, move.getAddEffect() == 0);
            }
        } else if (effect == 14) {
            // increases a lot the Special Defense of user - AMNESIA
            attacker.changeStat(3, 2, true, move.getAddEffect() == 0);
        } else if (effect == 15) {
            // turns in another move depends on the environment - NATURE POWER
            //TODO: turns in another move depends on the environment
        } else if (effect == 16) {
            // gets ingrain, recovers HPs in every turn - INGRAIN
            if (attacker.effectMoves.get(0) == 1) {
                return false;
            }
            attacker.effectMoves.set(0, 1);
            System.out.println(attacker.nickname + " gets ingrain!");
        } else if (effect == 17) {
            // decreases a lot the Special Attack of user - LEAF STORM, DRACO METEOR
            attacker.changeStat(2, -2, false, move.getAddEffect() == 0);
        } else if (effect == 18) {
            // absorb HP to enemy and recovers 1/2 of the damage - GIGA DRAIN
            attacker.healHP(damage / 2, true, false);
        } else if (effect == 19) {
            // resists an attack than could defeat user - ENDURE
            attacker.effectMoves.set(1, 1);
            attacker.protectTurns++;
            System.out.println(attacker.nickname + " is enduring!");
        } else if (effect == 20) {
            // attack 2-3 turns and gets confuse - PETAL DANCE, OUTRAGE
            if (attacker.effectMoves.get(11) == 0) {
                attacker.effectMoves.set(11, 1);
            } else if (attacker.effectMoves.get(11) == 1 && Math.random() < 0.5) {
                attacker.effectMoves.set(11, 0);
                attacker.recover1PP(move);
                if (attacker.canConfuse(true)) {
                    attacker.causeTemporalStatus(TemporalStatus.CONFUSED);
                }
            } else if (attacker.effectMoves.get(11) == 2) {
                attacker.effectMoves.set(11, 0);
                attacker.recover1PP(move);
                if (attacker.canConfuse(true)) {
                    attacker.causeTemporalStatus(TemporalStatus.CONFUSED);
                }
            } else {
                attacker.effectMoves.set(11, 2);
                attacker.recover1PP(move);
            }
        } else if (effect == 21) {
            // burns the target - EMBER, FLAME WHEEL, FLAMETHROWER...
            if (defender.canBurn(false)) {
                defender.causeStatus(Status.BURNED);
            } else {
                return false;
            }
        } else if (effect == 22) {
            // decreases target accuracy - SMOKE SCREEN, SAND ATTACK
            defender.changeStat(5, -1, false, move.getAddEffect() == 0);
        } else if (effect == 23) {
            // fix damage to 40 PS - DRAGON RAGE
            defender.reduceHP(40);
        }
        if (effect == 24) {
            // decreases a lot target speed - SCARY FACE, STRING SHOT
            defender.changeStat(4, -2, false, move.getAddEffect() == 0);
        } else if (effect == 25 && !defender.isFainted()) {
            // flinched target - BITE, HYPER FANG, AIR SLASH, TWISTER, FAKE OUT...
            if (defender.canFlinch()) {
                defender.causeTemporalStatus(TemporalStatus.FLINCHED);
            } else {
                return false;
            }
        } else if (effect == 26) {
            // damages rival ally - FLAME BURST
            //TODO: flame burst residual damage
        } else if (effect == 27 && !defender.isFainted()) {
            // partially trap enemy from 4-5 turns - FIRE SPIN, WHIRL POOL, SAND TOMB, WRAP...
            if (defender.effectMoves.get(4) == 0 && move.hasName("FIRESPIN")) {
                defender.effectMoves.set(4, 1);
            } else if(defender.effectMoves.get(16) == 0 && move.hasName("WRAP")) {
                defender.effectMoves.set(16, 1);
            } else if(defender.effectMoves.get(21) == 0 && move.hasName("SANDTOMB")) {
                defender.effectMoves.set(21, 1);
            }
            defender.causeTemporalStatus(TemporalStatus.PARTIALLYTRAPPED);
            System.out.println(defender.nickname + " was trapped by " + move.name);
        } else if (effect == 28) {
            // reduces HP but maximizes attack - BELLY DRUM
            if ((attacker.getPsActuales() <= attacker.getHP() / 2) || attacker.getStatChange(0) >= 4.0) {
                return false;
            }
            attacker.reduceHP(attacker.getHP() / 2);
            attacker.changeStat(0, 12, true, move.getAddEffect() == 0);

        } else if (effect == 29) {
            // increase all stats - ANCIENT POWER, SILVER WIND
            attacker.changeStat(0, 1, true, move.getAddEffect() == 0);
            attacker.changeStat(1, 1, true, move.getAddEffect() == 0);
            attacker.changeStat(2, 1, true, move.getAddEffect() == 0);
            attacker.changeStat(3, 1, true, move.getAddEffect() == 0);
            attacker.changeStat(4, 1, true, move.getAddEffect() == 0);
        } else if (effect == 30) {
            // burns or flinches the target - FIRE FANG
            if (defender.canBurn(false) && Math.random() <= 0.1) {
                defender.causeStatus(Status.BURNED);
            }
            if (defender.canFlinch() && Math.random() <= 0.1) {
                defender.causeTemporalStatus(TemporalStatus.FLINCHED);
            }
        } else if (effect == 32) {
            // increase user attack and speed - DRAGON DANCE
            attacker.changeStat(0, 1, true, move.getAddEffect() == 0);
            attacker.changeStat(4, 1, true, move.getAddEffect() == 0);
        } else if (effect == 33) {
            // decreases target defense - CRUNCH, TAIL WHIP, LEER
            defender.changeStat(1, -1, false, move.getAddEffect() == 0);
        } else if (effect == 34) {
            // increase user attack - METAL CLAW
            attacker.changeStat(0, 1, true, move.getAddEffect() == 0);
        } else if (effect == 35) {
            // returns the double of physical damage - COUNTER
            if (attacker.previousDamage > 0 && attacker.lastMoveInThisTurn.getCategory().equals(Category.PHYSICAL) && defender.hasType("GHOST")) {
                defender.reduceHP(attacker.previousDamage * 2);
            } else {
                return false;
            }
        } else if (effect == 36) {
            // paralyzes the target - STUN SPORE, THUNDERBOLT, THUNDER
            if (defender.canParalyze(false)) {
                defender.causeStatus(Status.PARALYZED);
            } else {
                return false;
            }
        } else if (effect == 37) {
            // charges and in the end of turn attacks - FOCUS PUNCH
            attacker.effectMoves.set(14, 0);
        } else if (effect == 38) {
            // increase user defense - WITHDRAW, HARDEN, STEEL WING...
            attacker.changeStat(1, 1, true, move.getAddEffect() == 0);
            if(move.hasName("DEFENSECURL")) {
                attacker.effectMoves.set(20, 1);
            }
        } else if (effect == 39) {
            // decreases target speed - BUBBLE
            defender.changeStat(4, -1, false, move.getAddEffect() == 0);
        } else if (effect == 40) {
            // increases user speed - RAPID SPIN
            attacker.changeStat(4, 1, true, move.getAddEffect() == 0);
        } else if (effect == 41) {
            // protects user - PROTECT, DETECT
            attacker.effectMoves.set(2, 1);
            attacker.protectTurns++;
            System.out.println(attacker.nickname + " is protecting itself!");
        } else if (effect == 42) {
            // confuse target - SUPERSONIC, CONFUSION, CONFUSE RAY, SIGNAL BEAM, WATER PULSE...
            if (defender.canConfuse(false)) {
                defender.causeTemporalStatus(TemporalStatus.CONFUSED);
            } else {
                return false;
            }
        } else if (effect == 43) {
            // increases a lot of user defense - IRON DEFENSE
            attacker.changeStat(1, 2, true, move.getAddEffect() == 0);
        } else if (effect == 44) {
            // starts to rain - RAIN DANCE
            //TODO: check if attacker has roca lluvia
            return battle.weather.changeWeather(Weathers.RAIN, false);
        } else if (effect == 46) {
            // returns the double of special damage - MIRROR COAT
            if (attacker.previousDamage > 0 && attacker.lastMoveInThisTurn.getCategory().equals(Category.SPECIAL) && defender.hasType("DARK")) {
                defender.reduceHP(attacker.previousDamage * 2);
            } else {
                return false;
            }
        } else if (effect == 47) {
            // prevents stat decreasing from all team - MIST
            if (attacker.getTeam().effectTeamMoves.get(0) == 0) {
                attacker.getTeam().effectTeamMoves.set(0, 1);
                System.out.println(attacker.nickname + "'s team is surrounded by Mist!");
            } else {
                return false;
            }
        } else if (effect == 48) {
            // restore all stat changes to 0 - HAZE
            //TODO: restore to 0 ALL pokemon in battle
            attacker.getStatChanges().replaceAll(ignored -> 0);
            defender.getStatChanges().replaceAll(ignored -> 0);
            System.out.println("The stat changes were removed!");
        } else if (effect == 49) {
            // ignores enemy's evasion and user's accuracy and Ghost type can be damaged by Normal/Fighting moves - FORE SIGHT
            if (defender.effectMoves.get(5) == 0) {
                defender.effectMoves.set(5, 1);
                System.out.println(defender.nickname + " was identified!");
            } else {
                return false;
            }
        }
        if (effect == 50) {
            // heal user from poison, paralysis or burn - REFRESH
            if (attacker.hasStatus(Status.POISONED) || attacker.hasStatus(Status.BADLYPOISONED) || attacker.hasStatus(Status.PARALYZED)
                    || attacker.hasStatus(Status.BURNED)) {
                attacker.healPermanentStatus();
            } else {
                return false;
            }
        } else if (effect == 51) {
            // numb the target, and it will sleep in the next turn - YAWN
            if (defender.canDrows()) {
                defender.effectMoves.set(6, 1);
                System.out.println(defender.nickname + " is drowsy!");
            } else {
                return false;
            }
        } else if (effect == 52) {
            // reduces the electric moves power - MUD SPORT
            battle.effectFieldMoves.set(0, 1);
            System.out.println("The power of Electric moves are reduced!");
        } else if (effect == 53) {
            // recover HP in every turn - AQUA RING
            if (attacker.effectMoves.get(7) == 0) {
                attacker.effectMoves.set(7, 1);
                System.out.println(attacker.nickname + " was involved in an aqua ring!");
            } else {
                return false;
            }
        } else if (effect == 56) {
            // decreases the Special Defense of target - FLASH CANNON, BUG BUZZ
            defender.changeStat(3, -1, false, move.getAddEffect() == 0);
        } else if (effect == 57) {
            // steals the equipped berry of target - BUG BITE, PLUCK
            //TODO: bug bite effect
        } else if (effect == 58) {
            // prevents status problems for all team - SAFEGUARD
            if (attacker.getTeam().effectTeamMoves.get(1) == 0) {
                attacker.getTeam().effectTeamMoves.set(1, 1);
                System.out.println(attacker.nickname + "'s team is protected by Safeguard!");
            } else {
                return false;
            }
        } else if (effect == 59) {
            // makes the target flee - WHIRL WIND, ROAR
            if (defender.effectMoves.get(0) > 0 || defender.hasAbility("SUCTIONCUPS")) {
                return false;
            } else {
                defender.effectMoves.set(12, 1);
                System.out.println(defender.nickname + " was expelled of the combat field!");
            }
        } else if (effect == 60) {
            // makes the user the center of attention meanwhile this turn - RAGE POWDER
            //TODO: rage powder effect
        } else if (effect == 61 && !defender.isFainted()) {
            // decreases target special attack if is opposite sex - CAPTIVATE
            if ((attacker.getGender() != defender.getGender()) && (attacker.getGender() != 2 && defender.getGender() != 2) && !defender.hasAbility("OBLIVIOUS")) {
                defender.changeStat(2, -2, false, move.getAddEffect() == 0);
            } else {
                return false;
            }
        } else if (effect == 62) {
            // duplicates all team speed meanwhile 4 turns - TAIL WIND
            if (attacker.getTeam().effectTeamMoves.get(2) == 0) {
                attacker.getTeam().effectTeamMoves.set(2, 1);
                System.out.println(attacker.nickname + "'s team has Tail Wind blowing on their favour!");
            } else {
                return false;
            }
        } else if (effect == 63) {
            // increase user special attack, special defense and speed - QUIVER DANCE
            attacker.changeStat(2, 1, true, move.getAddEffect() == 0);
            attacker.changeStat(3, 1, true, move.getAddEffect() == 0);
            attacker.changeStat(4, 1, true, move.getAddEffect() == 0);
        } else if (effect == 64) {
            // increase a lot the user attack if it faints opponent - FELL STINGER
            attacker.changeStat(0, 3, true, move.getAddEffect() == 0);
        } else if (effect == 67) {
            // increase the attack when is damaged by a contact move - RAGE
            if (attacker.effectMoves.get(8) == 0) {
                attacker.effectMoves.set(8, 1);
                System.out.println(attacker.nickname + " is raging!");
            }
        } else if (effect == 69) {
            // increase the critical move index - FOCUS ENERGY
            System.out.println(attacker.nickname + " is focusing to battle!");
            attacker.criticalIndex += 2;
            if (attacker.criticalIndex > 4) {
                attacker.criticalIndex = 4;
            }
        } else if (effect == 72) {
            // toxic spikes that poison Pokemon entering battlefield - TOXIC SPIKES
            if(defender.getTeam().effectTeamMoves.get(3) < 2) {
                defender.getTeam().increaseEffectMove(3);
            }
            System.out.println("The enemy team field was surrounded by Toxic Spikes!");
        } else if (effect == 73) {
            // increase a lot of user speed - AGILITY
            attacker.changeStat(4, 2, true, move.getAddEffect() == 0);
        } else if (effect == 74) {
            // equals target HP to attacker HP - ENDEAVOR
            if (defender.getPsActuales() <= attacker.getPsActuales()) {
                return false;
            } else {
                defender.setHP(attacker.getPsActuales());
                System.out.println(defender.nickname + " lost HP!");
            }
        } else if (effect == 75) {
            // recovers HP but loses Flying Type - ROOST
            if (attacker.hasAllHP()) {
                return false;
            } else {
                attacker.healHP(attacker.getHP() / 2, true, false);
                if (attacker.hasType("FLYING")) {
                    attacker.effectMoves.set(10, 1);
                    //TODO: burn up and halloween effect
                    if (attacker.battleType2 != null) {
                        if (attacker.battleType1.is("FLYING")) {
                            attacker.battleType1 = attacker.battleType2;
                        }
                        attacker.battleType2 = null;
                    } else if (attacker.battleType2 == null) {
                        attacker.battleType1 = attacker.getType("NORMAL");
                    }
                }
            }
        } else if (effect == 76) {
            // use the last move used to user - MIRROR MOVE
            if (attacker.lastMoveReceived != null) {
                if (!attacker.lastMoveReceived.getFlags().contains("f")) { // forbidden moves
                    return false;
                } else {
                    battle.useMove(attacker, defender, attacker.lastMoveReceived, defenderMove, false, false, true);
                }
            } else {
                return false;
            }
        } else if (effect == 77) {
            // attack 3 turns, will wake up sleep Pokemon and prevent sleep - UPROAR
            if (attacker.effectMoves.get(13) == 0) {
                if (!defender.isFainted()) System.out.println(attacker.nickname + " is making a Uproar!");
                attacker.effectMoves.set(13, 1);
                battle.effectFieldMoves.set(1, 1);
            } else if (attacker.effectMoves.get(13) == 1) {
                if (!defender.isFainted()) System.out.println(attacker.nickname + " is making a Uproar!");
                attacker.effectMoves.set(13, 2);
                attacker.recover1PP(move);
            } else if (attacker.effectMoves.get(13) == 2) {
                attacker.effectMoves.set(13, 0);
                battle.effectFieldMoves.set(1, 0);
                attacker.recover1PP(move);
                System.out.println(attacker.nickname + " calmed down");
            }
        } else if (effect == 78) {
            // decreases target evasion and wipe out field and team effects - DEFOG
            if (!defender.isFainted()) {
                defender.changeStat(6, -1, true, move.getAddEffect() == 0);
            }
            //TODO: wipe out stealth rock, spikes and sticky web
            if(attacker.getTeam().effectTeamMoves.get(3) > 0) {
                attacker.getTeam().removeTeamEffects(attacker,3); // remove yours toxic spikes
            }
            if(defender.getTeam().effectTeamMoves.get(3) > 0) {
                defender.getTeam().removeTeamEffects(defender,3); // remove enemy toxic spikes
            }

            if (defender.getTeam().effectTeamMoves.get(0) > 0) {
                defender.getTeam().removeTeamEffects(defender, 0); // remove rival mist
            }
            if (defender.getTeam().effectTeamMoves.get(1) > 0) {
                defender.getTeam().removeTeamEffects(defender, 1); // remove rival safeguard
            }
            if (defender.getTeam().effectTeamMoves.get(4) > 0) {
                defender.getTeam().removeTeamEffects(defender, 4); // remove rival light screen
            }
            if (defender.getTeam().effectTeamMoves.get(5) > 0) {
                defender.getTeam().removeTeamEffects(defender, 5); // remove rival reflect
            }
            //TODO: wipe out team enemy aurora veil
            if(battle.weather.hasWeather(Weathers.FOG)) { // delete fog weather
                battle.weather.endWeather();
            }
        } else if (effect == 80) {
            // take a half of the remaining HP to target - SUPERFANG
            int dmg = defender.getPsActuales() / 2;
            if (dmg <= 0) {
                dmg = 1;
            }
            defender.reduceHP(dmg);
        } else if (effect == 81) {
            // decrease a lot of target defense - SCREECH
            defender.changeStat(1, -2, false, move.getAddEffect() == 0);
        } else if (effect == 82) {
            // use the move that target will use in this turn - ME FIRST
            if(defenderMove == null) {
                return false;
            }
            if (defenderMove.getCategory().equals(Category.STATUS) || (defenderMove.hasName("BEAKBLAST") ||
                    defenderMove.hasName("BELCH") || defenderMove.hasName("CHATTER") || defenderMove.hasName("COUNTER") ||
                    defenderMove.hasName("COVET") || defenderMove.hasName("FOCUSPUNCH") || defenderMove.hasName("METALBURST") ||
                    defenderMove.hasName("MIRRORCOAT") || defenderMove.hasName("SHELLTRAP") ||
                    defenderMove.hasName("STRUGGLE") || defenderMove.hasName("THIEF"))) {
                return false;
            } else {
                battle.useMove(attacker, defender, defenderMove, defenderMove, false, true, true);
            }
        } else if (effect == 85) {
            // take as much HP as remain user HP and faints itself - FINAL GAMBIT
            System.out.println(attacker.nickname + " sacrifices itself!");
            defender.reduceHP(attacker.getPsActuales());
            attacker.reduceHP(-1);
        } else if (effect == 86) {
            // increase a lot of user attack - SWORDS DANCE
            attacker.changeStat(0, 2, true, move.getAddEffect() == 0);
        } else if(effect == 87) {
            // self damage 25% of user HP - STRUGGLE
            attacker.reduceHP(attacker.getHP()/4);
        } else if (effect == 88 && !defender.isFainted()) {
            // can paralyze, burn or freeze - TRI ATTACK
            if(Math.random() <= 0.33 && defender.canParalyze(false)) {
                defender.causeStatus(Status.PARALYZED);
            } else if(Math.random() <= 0.66 && defender.canBurn(false)) {
                defender.causeStatus(Status.BURNED);
            } else if(defender.canFreeze(false)) {
                defender.causeStatus(Status.FROZEN);
            } else {
                return false;
            }
        } else if (effect == 89) {
            // stock energy and increase user defenses - STOCK PILE
            if(attacker.stockpile == 3) {
                return false;
            }
            attacker.stockpile++;
            System.out.println(attacker.nickname + " stockpiled " + attacker.stockpile + "!");
            attacker.changeStat(1,1,true,false);
            attacker.changeStat(3,1,true,false);
        } else if (effect == 90) {
            // absorb energy accumulated, recover HP and decreases user defenses - SWALLOW
            if(attacker.stockpile == 0 || attacker.hasAllHP()) {
                return false;
            }
            System.out.println(attacker.nickname + " swallowed " + attacker.stockpile + "!");
            int recover = 4;
            if(attacker.stockpile == 2) {
                recover = 2;
            } else if(attacker.stockpile == 3) {
                recover = 1;
            }
            attacker.healHP(attacker.getHP()/recover,true,true);
            for(int i=0;i<attacker.stockpile;i++) {
                attacker.changeStat(1,-attacker.stockpile,true,false);
                attacker.changeStat(3,-attacker.stockpile,true,false);
            }
            attacker.stockpile = 0;
        } else if (effect == 91) {
            // liberate energy accumulated, damage enemy and decreases user defenses - SPIT UP
            System.out.println("The energy of Stockpile disappeared!");
            for(int i=0;i<attacker.stockpile;i++) {
                attacker.changeStat(1,-attacker.stockpile,true,false);
                attacker.changeStat(3,-attacker.stockpile,true,false);
            }
            attacker.stockpile = 0;
        } else if (effect == 92) {
            // decreases a lot the Special Defense of target - ACID SPRAY
            defender.changeStat(3, -2, false, move.getAddEffect() == 0);
        } else if (effect == 93) {
            // remove enemy ability - GASTRO ACID
            if(defender.hasAbility("POWERCONSTRUCT") || defender.hasAbility("SCHOOLING") || defender.hasAbility("STANCECHANGE")
                    || defender.hasAbility("DISGUISE") || defender.hasAbility("SHIELDSDOWN") || defender.hasAbility("BATTLEBOND")
                    || defender.hasAbility("COMATOSE") || defender.hasAbility("MULTITYPE") || defender.hasAbility("RKSSYSTEM")
                    || defender.getAbility() == null) {
                return false;
            }
            defender.changeAbility("null");
            System.out.println(defender.nickname + "'s ability was removed!");
        } else if (effect == 94) {
            // only works if user consumed a berry previously - BELCH
            //TODO: belch effect
        } else if (effect == 95) {
            // increase user attack, defense and accuracy - COIL
            attacker.changeStat(0, 1, true, move.getAddEffect() == 0);
            attacker.changeStat(1, 1, true, move.getAddEffect() == 0);
            attacker.changeStat(5, 1, true, move.getAddEffect() == 0);
        } else if (effect == 96) {
            // disables the last move used by enemy - DISABLE
            if(defender.previousMove == null || defender.effectMoves.get(17) > 0) {
                return false;
            }
            if(defender.previousMove.hasName("STRUGGLE")) {
                return false;
            }
            defender.disabledMove = defender.previousMove;
            defender.increaseEffectMove(17);
            System.out.println(defender.previousMove.name + " of " + defender.nickname + " is disabled!");
        } else if (effect == 97) {
            // badly poisons the target - TOXIC, POISON FANG
            if (defender.canPoison(false)) {
                defender.causeStatus(Status.BADLYPOISONED);
            } else {
                return false;
            }
        } else if (effect == 98) {
            // steals status moves effect - SNATCH
            System.out.println(attacker.nickname + " is waiting that its opponent make a move!");
            attacker.effectMoves.set(18, 1);
        } else if (effect == 99) {
            // reduces 4 PP to the last move used by target - SPITE
            if(defender.previousMove == null) {
                return false;
            }
            if(!defender.hasPP(defender.previousMove) || defender.previousMove.hasName("STRUGGLE")) {
                return false;
            }
            defender.reducePP(defender.previousMove,4);
            System.out.println(defender.previousMove.name + " of " + defender.nickname + " lost 4 PP!");
        } else if (effect == 100) {
            // switches items with target - SWITCHEROO
            //TODO: switcheroo effect
        } else if (effect == 101) {
            // freeze or flinches the target - ICE FANG
            if (defender.canFreeze(false) && Math.random() <= 0.1) {
                defender.causeStatus(Status.FROZEN);
            }
            if (defender.canFlinch() && Math.random() <= 0.1) {
                defender.causeTemporalStatus(TemporalStatus.FLINCHED);
            }
        } else if (effect == 102) {
            // paralyze or flinches the target - THUNDER FANG
            if (defender.canParalyze(false) && Math.random() <= 0.1) {
                defender.causeStatus(Status.PARALYZED);
            }
            if (defender.canFlinch() && Math.random() <= 0.1) {
                defender.causeTemporalStatus(TemporalStatus.FLINCHED);
            }
        }
        if (effect == 104) {
            // breaks protect moves - FEINT
            if(defender.effectMoves.get(2) > 0) {
                defender.effectMoves.set(2, 0);
                System.out.println(defender.nickname + " lost its protection!");
            }
            //TODO: break detection, quick guard, wide guard, spiky shield, kings shield, mat block, baneful bunker and crafty shield
        } else if (effect == 105) {
            // increase user evasion - DOUBLE TEAM
            attacker.changeStat(6, 1, true, move.getAddEffect() == 0);
        } else if (effect == 106) {
            // increase team special defense - LIGHT SCREEN
            if(attacker.getTeam().effectTeamMoves.get(4) > 0) {
                return false;
            }
            attacker.getTeam().effectTeamMoves.set(4, 1);
            System.out.println(attacker.nickname + " team has a " + move.name);
        } else if (effect == 107) {
            // increase team defense - REFLECT
            if(attacker.getTeam().effectTeamMoves.get(5) > 0) {
                return false;
            }
            attacker.getTeam().effectTeamMoves.set(5, 1);
            System.out.println(attacker.nickname + " team has a " + move.name);
        } else if (effect == 108) {
            // give its item to target - BESTOW
            //TODO: bestow effect
        } else if (effect == 109) {
            // charge for 2 turns and liberate energy - BIDE
            if(attacker.effectMoves.get(22) < 2) {
                attacker.increaseEffectMove(22);
                System.out.println(attacker.nickname + " is accumulating energy!");
                attacker.recover1PP(move);
            } else {
                System.out.println(attacker.nickname + " liberated energy!");
                attacker.effectMoves.set(22, 0);
                if(attacker.bideDamage == 0) {
                    return false;
                }
                defender.reduceHP(attacker.bideDamage*2);
                attacker.bideDamage = 0;

            }
        } else if (effect == 110) {
            // powers the next electric move and increase special defense - CHARGE
            attacker.effectMoves.set(23, 1);
            System.out.println(attacker.nickname + " is charging energy!");
            attacker.changeStat(3, 1, true, move.getAddEffect() == 0);
        } else if (effect == 111) {
            // create electric terrain - ELECTRIC TERRAIN
            //TODO: electric terrain effect
        } else if (effect == 112) {
            // create psychic terrain - PSYCHIC TERRAIN
            //TODO: psychic terrain effect
        } else if (effect == 113) {
            // create misty terrain - MISTY TERRAIN
            //TODO: misty terrain effect
        } else if (effect == 114) {
            // create grassy terrain - GRASSY TERRAIN
            //TODO: grassy terrain effect
        } else if (effect == 115) {
            // rival must use the last movement from 3 turns - ENCORE
            if(defender.effectMoves.get(26) > 0 || defender.previousMove == null) {
                return false;
            }
            if(!defender.hasPP(defender.previousMove)) {
                return false;
            }
            if(defender.previousMove.hasName("ENCORE") || defender.previousMove.hasName("SKETCH")
                    ||defender.previousMove.hasName("MIMIC") || defender.previousMove.hasName("MIRRORMOVE")
                    || defender.previousMove.hasName("TRANSFORM") || defender.previousMove.hasName("STRUGGLE")) {
                return false;
            }
            defender.effectMoves.set(26, 1);
            defender.encoreMove = defender.previousMove;
            System.out.println(defender.nickname + " was trapped by " + move.name + "!");

        } else if (effect == 116) {
            // protect team from critical hits - LUCKY CHANT
            if(attacker.getTeam().effectTeamMoves.get(6) > 0) {
                return false;
            }
            attacker.getTeam().effectTeamMoves.set(6, 1);
            System.out.println(attacker.nickname + " team has a " + move.name);
        } else if (effect == 118) {
            // decrease rival attack and defense - TICKLE
            defender.changeStat(0, -1, false, move.getAddEffect() == 0);
            defender.changeStat(1, -1, false, move.getAddEffect() == 0);
        } else if (effect == 119) {
            // heals in the next turn - WISH
            if(attacker.getTeam().effectTeamMoves.get(7) > 0) {
                return false;
            }
            attacker.getTeam().effectTeamMoves.set(7, 1);
            System.out.println(attacker.nickname + " take a wish!");
            attacker.getTeam().wishRecover = attacker.getHP()/2;
        } else if (effect == 120) {
            // attacks while 5 turns increasing power - ROLLOUT
            if (attacker.effectMoves.get(25) < 5) {
                if(attacker.effectMoves.get(25) > 0) {
                    attacker.recover1PP(move);
                }
                attacker.increaseEffectMove(25);
            } else {
                attacker.effectMoves.set(25, 0);
            }
        } else if (effect == 121) {
            // for every consecutive turn make it powerful - FURY CUTTER
            if (attacker.effectMoves.get(24) < 2) {
                attacker.increaseEffectMove(24);
            } else {
                attacker.effectMoves.set(24, 0);
            }
        } else if (effect == 124) {
            // invokes a sandstorm weather - SAND STORM
            //TODO: check if attacker has roca suave
            return battle.weather.changeWeather(Weathers.SANDSTORM, false);
        } else if (effect == 126) {
            // increase user attack and accuracy - HONE CLAWS
            attacker.changeStat(0, 1, true, move.getAddEffect() == 0);
            attacker.changeStat(5, 1, true, move.getAddEffect() == 0);
        } else if (effect == 127) {
            // increase attack and special attack of Grass Pokemon - ROTOTILLER
            int nGrass = 0;
            if(attacker.hasType("GRASS") && !attacker.isLevitating()) {
                attacker.changeStat(0, 1, false, move.getAddEffect() == 0);
                attacker.changeStat(2, 1, false, move.getAddEffect() == 0);
                nGrass++;
            }
            if(defender.hasType("GRASS") && !defender.isLevitating()) {
                defender.changeStat(0, 1, false, move.getAddEffect() == 0);
                defender.changeStat(2, 1, false, move.getAddEffect() == 0);
                nGrass++;
            }
            return nGrass != 0;
        }
        return true;
    }
}
