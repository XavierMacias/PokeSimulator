package PokeBattle;

import PokeData.*;

import java.util.Optional;

public class MoveEffects {

    Battle battle;

    public MoveEffects(Battle battle) {
        this.battle = battle;
    }

    public boolean moveEffects(Movement move, Pokemon attacker, Pokemon defender, Movement defenderMove, int damage) {
        int effect = move.getCode();
        if (effect == 1 && !defender.isFainted()) {
            // decreases target attack - GROWL
            defender.changeStat(0, -1, false, move.getAddEffect() == 0);
        } else if (effect == 2 && !defender.isFainted()) {
            // seed the target - LEECH SEED
            if (defender.canSeed()) {
                defender.causeTemporalStatus(TemporalStatus.SEEDED);
            } else {
                return false;
            }
        } else if ((effect == 3 || effect == 65) && !defender.isFainted()) {
            // poisons the target - POISON STING, SLUDGE BOMB, POISON POWDER
            if (defender.canPoison(false)) {
                defender.causeStatus(Status.POISONED);
            } else {
                return false;
            }
        } else if (effect == 4 && !defender.isFainted()) {
            // sleeps the target - SLEEP POWDER, HYPNOSIS
            if (defender.canSleep(false)) {
                defender.causeStatus(Status.ASLEEP);
            } else {
                return false;
            }
        } else if (effect == 5) {
            // recoil damage - TAKE DOWN
            attacker.reduceHP(damage / 4);
        } else if (effect == 6 && !defender.isFainted()) {
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
            // more recoil damage - DOUBLE EDGE, BRAVE BIRD, FLARE BLITZ
            attacker.reduceHP(damage / 3);
            if (0.1 >= Math.random() && defender.canBurn(false) && move.getInternalName().equals("FLAREBLITZ")) {
                defender.causeStatus(Status.BURNED);
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
            if (move.getInternalName().equals("SOLARBEAM")) {
                if (attacker.effectMoves.get(3) == 0 && !battle.weather.hasWeather(Weathers.SUNLIGHT) && !battle.weather.hasWeather(Weathers.HEAVYSUNLIGHT)) {
                    System.out.println(attacker.nickname + " is charging solar energy!");
                    attacker.recover1PP(move);
                    attacker.effectMoves.set(3, 1);
                } else {
                    attacker.effectMoves.set(3, 0);
                }
            } else if (move.getInternalName().equals("SKULLBASH")) {
                if (attacker.effectMoves.get(3) == 0) {
                    System.out.println(attacker.nickname + " bowed its head!");
                    attacker.changeStat(1, 1, true, move.getAddEffect() == 0);
                    attacker.recover1PP(move);
                    attacker.effectMoves.set(3, 1);
                } else {
                    attacker.effectMoves.set(3, 0);
                }
            }
        } else if (effect == 12 && !defender.isFainted()) {
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
        } else if (effect == 22 && !defender.isFainted()) {
            // decreases target accuracy - SMOKE SCREEN, SAND ATTACK
            defender.changeStat(5, -1, false, move.getAddEffect() == 0);
        } else if (effect == 23) {
            // fix damage to 40 PS - DRAGON RAGE
            defender.reduceHP(40);
        }
        if (effect == 24 && !defender.isFainted()) {
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
            // partially trap enemy from 4-5 turns - FIRE SPIN, WHIRL POOL, SAND TOMB...
            if (defender.effectMoves.get(4) == 0) {
                defender.effectMoves.set(4, 1);
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
        } else if (effect == 30 && !defender.isFainted()) {
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
        } else if (effect == 33 && !defender.isFainted()) {
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
        } else if (effect == 39 && !defender.isFainted()) {
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
        } else if (effect == 42 && !defender.isFainted()) {
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
            attacker.getStatChanges().replaceAll(ignored -> 0);
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
        } else if (effect == 56 && !defender.isFainted()) {
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
        } else if (effect == 64 && defender.isFainted()) {
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
                        if (attacker.battleType1.getInternalName().equals("FLYING")) {
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
                    battle.useMove(attacker, defender, attacker.lastMoveReceived, defenderMove, false, false);
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
            //TODO: wipe out team enemy reflect, light screen and aurora veil
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
        } else if (effect == 81 && !defender.isFainted()) {
            // decrease a lot of target defense - SCREECH
            defender.changeStat(1, -2, false, move.getAddEffect() == 0);
        } else if (effect == 82) {
            // use the move that target will use in this turn - ME FIRST
            if(defenderMove == null) {
                return false;
            }
            if (defenderMove.getCategory().equals(Category.STATUS) || (defenderMove.getInternalName().equals("BEAKBLAST") ||
                    defenderMove.getInternalName().equals("BELCH") || defenderMove.getInternalName().equals("CHATTER") ||
                    defenderMove.getInternalName().equals("COUNTER") || defenderMove.getInternalName().equals("COVET") ||
                    defenderMove.getInternalName().equals("FOCUSPUNCH") || defenderMove.getInternalName().equals("METALBURST") ||
                    defenderMove.getInternalName().equals("MIRRORCOAT") || defenderMove.getInternalName().equals("SHELLTRAP") ||
                    defenderMove.getInternalName().equals("STRUGGLE") || defenderMove.getInternalName().equals("THIEF"))) {
                return false;
            } else {
                battle.useMove(attacker, defender, defenderMove, defenderMove, false, true);
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
        }

        return true;
    }
}
