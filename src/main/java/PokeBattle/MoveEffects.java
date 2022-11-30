package PokeBattle;

import PokeData.*;

import java.util.*;

public class MoveEffects {

    Battle battle;
    public ArrayList<Integer> attacksForbiddenBySleepTalk; // attacks that cannot be selected by SLEEP TALK
    public ArrayList<Integer> attacksWithSecondaryEffects; // attack with secondary effects that affect the opponent (SHIELD DUST)
    public ArrayList<Integer> attacksToHeal; // attack that can heal immediatly or HP-draining moves
    double probability = 0.1;

    public MoveEffects(Battle battle) {
        this.battle = battle;
        attacksWithSecondaryEffects = new ArrayList<>(Arrays.asList(1, 3, 4, 6, 12, 21, 22, 24, 25, 30, 33, 36, 39, 42, 56, 65, 78, 81, 92, 97, 101, 102, 118, 141, 154, 226, 241, 260, 261, 268));
        attacksForbiddenBySleepTalk = new ArrayList<>(Arrays.asList(11, 37, 76, 77, 82, 109, 139, 161, 172));
        attacksToHeal = new ArrayList<>(Arrays.asList(10, 18, 75, 158, 165, 237));
        // TODO: complete lists
    }

    public boolean changeAbility(Pokemon attacker, Pokemon defender) {
        if (defender.getAbility().equals(attacker.getAbility()) || defender.hasAbility("POWERCONSTRUCT")
                || defender.hasAbility("SCHOOLING") || defender.hasAbility("STANCECHANGE") || defender.hasAbility("DISGUISE")
                || defender.hasAbility("FLOWERGIFT") || defender.hasAbility("SHIELDSDOWN") || defender.hasAbility("BATTLEBOND")
                || defender.hasAbility("ILLUSION") || defender.hasAbility("IMPOSTER") || defender.hasAbility("COMATOSE")
                || defender.hasAbility("ZENMODE") || defender.hasAbility("MULTITYPE") || defender.hasAbility("FORECAST")
                || defender.hasAbility("TRACE") || defender.hasAbility("POWEROFALCHEMY") || defender.hasAbility("RECEIVER")
                || defender.hasAbility("RKSSYSTEM") || defender.hasAbility("WONDERGUARD")) {
            return false;
        } else {
            attacker.changeAbility(defender.getAbility().getInternalName());
            return true;
        }
    }

    public boolean stealItem(Pokemon attacker, Pokemon defender) {
        if(attacker.item != null) {
            return false;
        }
        if(defender.item != null || (defender.hasAbility("STICKYHOLD") && !attacker.hasAbility("MOLDBREAKER"))) {
            return false;
        }
        if((defender.hasItem("GRISEOUSORB") && defender.specieNameIs("GIRATINA")) ||
                (defender.item.getFlags().contains("l") && defender.hasAbility("MULTITYPE")) ||
                ((defender.hasItem("DOUSEDRIVE") || defender.hasItem("SHOCKDRIVE") || defender.hasItem("CHILLDRIVE") ||
                        defender.hasItem("BURNDRIVE")) && defender.specieNameIs("GENESECT")) ||
                (defender.item.getFlags().contains("m") && defender.hasAbility("RKSSYSTEM"))) {
            return false;
        } // TODO: kyogre with blue orb, groudon with red orb, mail and mega stones

        System.out.println(attacker.nickname + " steals " + defender.item.name + " from " + defender.nickname + "!");
        attacker.giveItem(defender.item.getInternalName(), false);
        defender.loseItem(false, false);
        return true;
    }

    public void nullProtections(Pokemon attacker, Pokemon defender) {
        if(defender.effectMoves.get(2) > 0 || defender.getTeam().effectTeamMoves.get(9) > 0 ||
                defender.getTeam().effectTeamMoves.get(10) > 0 || defender.getTeam().effectTeamMoves.get(19) > 0) { // protect, quick guard, wide guard, crafty shield
            defender.effectMoves.set(2, 0);
            defender.getTeam().effectTeamMoves.set(9, 0);
            defender.getTeam().effectTeamMoves.set(10, 0);
            defender.getTeam().effectTeamMoves.set(19, 0);
            defender.getTeam().effectTeamMoves.set(21, 0);
            System.out.println(defender.nickname + " lost its protection!");
        }
        //TODO: breaks kings shield and baneful bunker
    }

    public boolean moveEffects(Movement move, Pokemon attacker, Pokemon defender, Movement defenderMove, int damage) {
        int effect = move.getCode();
        if(attacker.hasAbility("SERENEGRACE")) probability *= 2;
        if(attacksWithSecondaryEffects.contains(effect) && defender.isFainted()) {
            return true;
        }
        if(defender.hasAbility("SHIELDDUST") && !attacker.hasAbility("MOLDBREAKER") && move.getAddEffect() > 0 && attacksWithSecondaryEffects.contains(effect)) {
            return true;
        }

        // TODO: set all magic guard inmunities
        if (effect == 1) {
            // decreases target attack - GROWL
            defender.changeStat(0, -1, false, move.getAddEffect() == 0, attacker);
        } else if (effect == 2) {
            // seed the target - LEECH SEED
            if (defender.canSeed()) {
                defender.causeTemporalStatus(TemporalStatus.SEEDED, attacker);
            } else {
                return false;
            }
        } else if (effect == 3 || effect == 65) {
            // poisons the target - POISON STING, SLUDGE BOMB, POISON POWDER
            if (defender.canPoison(false, attacker)) {
                defender.causeStatus(Status.POISONED, attacker, false);
            } else {
                return false;
            }
        } else if (effect == 4) {
            // sleeps the target - SLEEP POWDER, HYPNOSIS, SING, GRASS WHISTLE, DARK VOID...
            if (defender.canSleep(false, attacker)) {
                defender.causeStatus(Status.ASLEEP, attacker, false);
            } else {
                return false;
            }
        } else if (effect == 5 && !attacker.hasAbility("MAGICGUARD") && !attacker.hasAbility("ROCKHEAD")) {
            // recoil damage - TAKE DOWN
            attacker.reduceHP(damage / 4);
        } else if (effect == 6) {
            // decreases target evasion - SWEET SCENT
            defender.changeStat(6, -1, false, move.getAddEffect() == 0, attacker);
        } else if (effect == 7) {
            // increases user attack and special attack - GROWTH, WORK UP
            int quantity = 1;
            if (move.hasName("GROWTH") && (battle.weather.hasWeather(Weathers.SUNLIGHT) || battle.weather.hasWeather(Weathers.HEAVYSUNLIGHT))) {
                quantity = 2;
            }
            attacker.changeStat(0, quantity, true, move.getAddEffect() == 0, defender);
            attacker.changeStat(2, quantity, true, move.getAddEffect() == 0, defender);
        } else if (effect == 8) {
            // more recoil damage - DOUBLE EDGE, WOOD HAMMER, BRAVE BIRD, FLARE BLITZ, VOLT TACKLE
            double prob = 0.1;
            if(attacker.hasAbility("SERENEGRACE")) prob *= 2;
            if(!attacker.hasAbility("ROCKHEAD") && !attacker.hasAbility("MAGICGUARD")) {
                attacker.reduceHP(damage / 3);
            }
            if (prob >= Math.random() && defender.canBurn(false, attacker) && move.hasName("FLAREBLITZ")) {
                defender.causeStatus(Status.BURNED, attacker, false);
            } else if (prob >= Math.random() && defender.canParalyze(false, attacker) && move.hasName("VOLTTACKLE")) {
                defender.causeStatus(Status.PARALYZED, attacker, false);
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

            return attacker.healHP(quantity, true, true, false, true);
        } else if (effect == 11 && !attacker.isFainted()) {
            // first turn: load, second turn: attack - SKULL BASH, SOLAR BEAM, RAZOR WIND, SKY ATTACK, DIG, BOUNCE, DIVE, FLY...
            double prob = 0.3;
            if(attacker.hasAbility("SERENEGRACE")) prob *= 2;

            if (move.hasName("SOLARBEAM")) {
                if (!(attacker.hasItem("POWERHERB") && attacker.canUseItem()) && attacker.effectMoves.get(3) == 0 && !battle.weather.hasWeather(Weathers.SUNLIGHT) && !battle.weather.hasWeather(Weathers.HEAVYSUNLIGHT)) {
                    if(attacker.hasAbility("TRUANT")) return false;
                    System.out.println(attacker.nickname + " is charging solar energy!");
                    attacker.recover1PP(move);
                    attacker.effectMoves.set(3, 1);
                } else {
                    attacker.effectMoves.set(3, 0);
                }
            } else if (move.hasName("SKULLBASH")) {
                if (!(attacker.hasItem("POWERHERB") && attacker.canUseItem()) && attacker.effectMoves.get(3) == 0) {
                    if(attacker.hasAbility("TRUANT")) return false;
                    System.out.println(attacker.nickname + " bowed its head!");
                    attacker.changeStat(1, 1, true, move.getAddEffect() == 0, defender);
                    attacker.recover1PP(move);
                    attacker.effectMoves.set(3, 1);
                } else {
                    attacker.effectMoves.set(3, 0);
                }
            } else if (move.hasName("RAZORWIND")) {
                if (!(attacker.hasItem("POWERHERB") && attacker.canUseItem()) && attacker.effectMoves.get(3) == 0) {
                    if(attacker.hasAbility("TRUANT")) return false;
                    System.out.println(attacker.nickname + " raised a whirlwind!");
                    attacker.recover1PP(move);
                    attacker.effectMoves.set(3, 1);
                } else {
                    attacker.effectMoves.set(3, 0);
                }
            } else if (move.hasName("SKYATTACK")) {
                if (!(attacker.hasItem("POWERHERB") && attacker.canUseItem()) && attacker.effectMoves.get(3) == 0) {
                    if(attacker.hasAbility("TRUANT")) return false;
                    System.out.println(attacker.nickname + " is charging!");
                    attacker.recover1PP(move);
                    attacker.effectMoves.set(3, 1);
                } else {
                    // sky attack can flinch enemy
                    attacker.effectMoves.set(3, 0);
                    if (Math.random() <= prob && attacker.canFlinch(defender)) {
                        defender.causeTemporalStatus(TemporalStatus.FLINCHED, attacker);
                    }
                }
            } else if (move.hasName("DIG")) {
                if (!(attacker.hasItem("POWERHERB") && attacker.canUseItem()) && attacker.effectMoves.get(3) == 0) {
                    if(attacker.hasAbility("TRUANT")) return false;
                    System.out.println(attacker.nickname + " dug underground!");
                    attacker.recover1PP(move);
                    attacker.effectMoves.set(3, 1);
                    attacker.effectMoves.set(36, 1);
                } else {
                    attacker.effectMoves.set(3, 0);
                    attacker.effectMoves.set(36, 0);
                }
            } else if (move.hasName("BOUNCE") || move.hasName("FLY")) {
                if (!(attacker.hasItem("POWERHERB") && attacker.canUseItem()) && attacker.effectMoves.get(3) == 0) {
                    if(attacker.hasAbility("TRUANT")) return false;
                    if(move.hasName("BOUNCE")) {
                        System.out.println(attacker.nickname + " jumped very high!");
                    } else {
                        System.out.println(attacker.nickname + " flew very high!");
                    }
                    attacker.recover1PP(move);
                    attacker.effectMoves.set(3, 1);
                    attacker.effectMoves.set(44, 1);
                } else {
                    attacker.effectMoves.set(3, 0);
                    attacker.effectMoves.set(44, 0);
                    if (defender.canParalyze(false, attacker) && prob >= Math.random() && move.hasName("BOUNCE")) {
                        defender.causeStatus(Status.PARALYZED, attacker, false);
                    }
                }
            } else if (move.hasName("DIVE")) {
                if (!(attacker.hasItem("POWERHERB") && attacker.canUseItem()) && attacker.effectMoves.get(3) == 0) {
                    if(attacker.hasAbility("TRUANT")) return false;
                    System.out.println(attacker.nickname + " dived underwater!");
                    attacker.recover1PP(move);
                    attacker.effectMoves.set(3, 1);
                    attacker.effectMoves.set(46, 1);
                } else {
                    attacker.effectMoves.set(3, 0);
                    attacker.effectMoves.set(46, 0);
                }
            } else if (move.hasName("PHANTOMFORCE") || move.hasName("SHADOWFORCE")) {
                if (!(attacker.hasItem("POWERHERB") && attacker.canUseItem()) && attacker.effectMoves.get(3) == 0) {
                    if(attacker.hasAbility("TRUANT")) return false;
                    System.out.println(attacker.nickname + " disappeared!");
                    attacker.recover1PP(move);
                    attacker.effectMoves.set(3, 1);
                    attacker.effectMoves.set(65, 1);
                } else {
                    attacker.effectMoves.set(3, 0);
                    attacker.effectMoves.set(65, 0);
                }
            } else if (move.hasName("SKYDROP")) {
                if (attacker.effectMoves.get(3) == 0) {
                    if(attacker.hasAbility("TRUANT")) return false;
                    System.out.println(attacker.nickname + " took " + defender.nickname + " with it!");
                    attacker.recover1PP(move);
                    attacker.effectMoves.set(3, 1);
                    attacker.effectMoves.set(59, 1);
                    defender.effectMoves.set(59, 1);
                } else {
                    attacker.effectMoves.set(3, 0);
                    attacker.effectMoves.set(59, 0);
                    defender.effectMoves.set(59, 0);
                }
            }

            if(attacker.hasItem("POWERHERB") && attacker.canUseItem() && !move.hasName("SKYDROP")) { // use power herb
                attacker.loseItem(true, true);
            }
        } else if (effect == 12) {
            // decreases a lot target attack - CHARM, FEATHER DANCE...
            defender.changeStat(0, -2, false, move.getAddEffect() == 0, attacker);
        } else if (effect == 13) {
            // curse the enemy if the user is Ghost-type. If not, increase attack, defense and decrease speed - CURSE
            if (attacker.hasType("GHOST")) {
                if (defender.hasTemporalStatus(TemporalStatus.CURSED)) {
                    return false;
                }
                System.out.println(attacker.nickname + " lost some of its HP and cursed " + defender.nickname + "!");
                attacker.reduceHP(attacker.getHP() / 2);
                defender.causeTemporalStatus(TemporalStatus.CURSED, attacker);
            } else {
                attacker.changeStat(0, 1, true, move.getAddEffect() == 0, defender);
                attacker.changeStat(1, 1, true, move.getAddEffect() == 0, defender);
                attacker.changeStat(4, -1, true, move.getAddEffect() == 0, defender);
            }
        } else if (effect == 14) {
            // increases a lot the Special Defense of user - AMNESIA
            attacker.changeStat(3, 2, true, move.getAddEffect() == 0, defender);
        } else if (effect == 15) {
            // turns in another move depends on the environment - NATURE POWER
            //TODO: turns in another move depends on the environment
            if(battle.terrain.hasTerrain(TerrainTypes.GRASSY)) {
                battle.useMove(attacker, defender, attacker.utils.getMove("ENERGYBALL"), defenderMove, false, false, true);
            } else if(battle.terrain.hasTerrain(TerrainTypes.ELECTRIC)) {
                battle.useMove(attacker, defender, attacker.utils.getMove("THUNDERBOLT"), defenderMove, false, false, true);
            } else if(battle.terrain.hasTerrain(TerrainTypes.MISTY)) {
                battle.useMove(attacker, defender, attacker.utils.getMove("MOONBLAST"), defenderMove, false, false, true);
            } else if(battle.terrain.hasTerrain(TerrainTypes.PSYCHIC)) {
                battle.useMove(attacker, defender, attacker.utils.getMove("PSYCHIC"), defenderMove, false, false, true);
            }
        } else if (effect == 16) {
            // gets ingrain, recovers HPs in every turn - INGRAIN
            if (attacker.effectMoves.get(0) == 1) {
                return false;
            }
            attacker.effectMoves.set(0, 1);
            attacker.effectMoves.set(45, 0); // no magnet rise
            attacker.effectMoves.set(42, 0); // no telekinesis
            System.out.println(attacker.nickname + " gets ingrain!");
        } else if (effect == 17) {
            // decreases a lot the Special Attack of user - LEAF STORM, DRACO METEOR
            attacker.changeStat(2, -2, true, move.getAddEffect() == 0, defender);
        } else if (effect == 18) {
            // absorb HP to enemy and recovers 1/2 of the damage - ABSORB, MEGA DRAIN, GIGA DRAIN, DREAM EATER
            if(!defender.hasAbility("LIQUIDOOZE")) {
                attacker.healHP(damage / 2, true, false, true, true);
            } else {
                attacker.reduceHP(damage / 2);
            }
        } else if (effect == 19) {
            // resists an attack than could defeat user - ENDURE
            attacker.effectMoves.set(1, 1);
            attacker.protectTurns++;
            System.out.println(attacker.nickname + " is enduring!");
        } else if (effect == 20) {
            // attack 2-3 turns and gets confuse - PETAL DANCE, OUTRAGE
            if(attacker.effectMoves.get(35) > 0) { // sleep talk only use one time this move
                return true;
            }
            if (attacker.effectMoves.get(11) == 0) {
                attacker.effectMoves.set(11, 1);
            } else if (attacker.effectMoves.get(11) == 1 && Math.random() < 0.5) {
                attacker.effectMoves.set(11, 0);
                attacker.recover1PP(move);
                if (attacker.canConfuse(true, defender)) {
                    attacker.causeTemporalStatus(TemporalStatus.CONFUSED, defender);
                }
            } else if (attacker.effectMoves.get(11) == 2) {
                attacker.effectMoves.set(11, 0);
                attacker.recover1PP(move);
                if (attacker.canConfuse(true, defender)) {
                    attacker.causeTemporalStatus(TemporalStatus.CONFUSED, defender);
                }
            } else {
                attacker.effectMoves.set(11, 2);
                attacker.recover1PP(move);
            }
        } else if (effect == 21) {
            // burns the target - EMBER, FLAME WHEEL, FLAMETHROWER...
            if (defender.canBurn(false, attacker)) {
                defender.causeStatus(Status.BURNED, attacker, false);
            } else {
                return false;
            }
        } else if (effect == 22) {
            // decreases target accuracy - SMOKE SCREEN, SAND ATTACK
            defender.changeStat(5, -1, false, move.getAddEffect() == 0, attacker);
        } else if (effect == 23) {
            // fix damage moves - DRAGON RAGE, SONIC BOOM
            int ps = 20;
            if(move.hasName("DRAGONRAGE")) {
                ps = 40;
            }
            if(battle.resistsWith1HP(defender, attacker, ps, move)) {
                ps = ps-1;
            }
            defender.reduceHP(ps);
        }
        if (effect == 24) {
            // decreases a lot target speed - SCARY FACE, STRING SHOT
            defender.changeStat(4, -2, false, move.getAddEffect() == 0, attacker);
        } else if (((effect == 25 || effect == 211) && !defender.isFainted()) || ((attacker.hasItem("KINGSROCK") || attacker.hasItem("RAZORFANG")) && attacker.canUseItem() && Math.random() < probability)) {
            // flinched target - BITE, HYPER FANG, AIR SLASH, TWISTER, FAKE OUT...
            if (defender.canFlinch(attacker)) {
                defender.causeTemporalStatus(TemporalStatus.FLINCHED, attacker);
            } else {
                return false;
            }
        } else if (effect == 26) {
            // damages rival ally - FLAME BURST
            //TODO: flame burst residual damage
        } else if (effect == 27 && !defender.isFainted()) {
            // partially trap enemy from 4-5 turns - FIRE SPIN, WHIRLPOOL, CLAMP, SAND TOMB, WRAP, BIND, INFESTATION, MAGMA STORM...
            if (defender.effectMoves.get(4) == 0 && move.hasName("FIRESPIN")) {
                defender.effectMoves.set(4, 1);
            } else if(defender.effectMoves.get(16) == 0 && move.hasName("WRAP")) {
                defender.effectMoves.set(16, 1);
            } else if(defender.effectMoves.get(21) == 0 && move.hasName("SANDTOMB")) {
                defender.effectMoves.set(21, 1);
            } else if(defender.effectMoves.get(49) == 0 && move.hasName("CLAMP")) {
                defender.effectMoves.set(49, 1);
            } else if(defender.effectMoves.get(50) == 0 && move.hasName("WHIRLPOOL")) {
                defender.effectMoves.set(50, 1);
            } else if(defender.effectMoves.get(51) == 0 && move.hasName("BIND")) {
                defender.effectMoves.set(51, 1);
            } else if(defender.effectMoves.get(61) == 0 && move.hasName("INFESTATION")) {
                defender.effectMoves.set(61, 1);
            } else if(defender.effectMoves.get(68) == 0 && move.hasName("MAGMASTORM")) {
                defender.effectMoves.set(68, 1);
            }
            defender.causeTemporalStatus(TemporalStatus.PARTIALLYTRAPPED, attacker);
            System.out.println(defender.nickname + " was trapped by " + move.name);
        } else if (effect == 28) {
            // reduces HP but maximizes attack - BELLY DRUM
            if ((attacker.getPsActuales() <= attacker.getHP() / 2) || attacker.getStatChange(0) >= 4.0) {
                return false;
            }
            attacker.reduceHP(attacker.getHP() / 2);
            attacker.changeStat(0, 12, true, move.getAddEffect() == 0, defender);

        } else if (effect == 29) {
            // increase all stats - ANCIENT POWER, SILVER WIND
            attacker.changeStat(0, 1, true, move.getAddEffect() == 0, defender);
            attacker.changeStat(1, 1, true, move.getAddEffect() == 0, defender);
            attacker.changeStat(2, 1, true, move.getAddEffect() == 0, defender);
            attacker.changeStat(3, 1, true, move.getAddEffect() == 0, defender);
            attacker.changeStat(4, 1, true, move.getAddEffect() == 0, defender);
        } else if (effect == 30) {
            // burns or flinches the target - FIRE FANG
            double prob = 0.1;
            if(attacker.hasAbility("SERENEGRACE")) prob *= 2;
            if (defender.canBurn(false, attacker) && Math.random() <= prob) {
                defender.causeStatus(Status.BURNED, attacker, false);
            }
            if (defender.canFlinch(attacker) && Math.random() <= prob) {
                defender.causeTemporalStatus(TemporalStatus.FLINCHED, attacker);
            }
        } else if (effect == 32) {
            // increase user attack and speed - DRAGON DANCE
            attacker.changeStat(0, 1, true, move.getAddEffect() == 0, defender);
            attacker.changeStat(4, 1, true, move.getAddEffect() == 0, defender);
        } else if (effect == 33) {
            // decreases target defense - CRUNCH, TAIL WHIP, LEER
            defender.changeStat(1, -1, false, move.getAddEffect() == 0, attacker);
        } else if (effect == 34) {
            // increase user attack - METAL CLAW
            attacker.changeStat(0, 1, true, move.getAddEffect() == 0, defender);
        } else if (effect == 35) {
            // returns the double of physical damage - COUNTER
            if(attacker.lastMoveInThisTurn == null) return false;
            if (attacker.previousDamage > 0 && attacker.lastMoveInThisTurn.getCategory().equals(Category.PHYSICAL) && defender.hasType("GHOST")) {
                int dmg = attacker.previousDamage*2;
                if(battle.resistsWith1HP(defender, attacker, dmg, move)) {
                    dmg -= 1;
                }
                defender.reduceHP(dmg);
            } else {
                return false;
            }
        } else if (effect == 36) {
            // paralyzes the target - STUN SPORE, THUNDERBOLT, THUNDER
            if (defender.canParalyze(false, attacker)) {
                defender.causeStatus(Status.PARALYZED, attacker, false);
            } else {
                return false;
            }
        } else if (effect == 37) {
            // charges and in the end of turn attacks - FOCUS PUNCH
            attacker.effectMoves.set(14, 0);
        } else if (effect == 38) {
            // increase user defense - WITHDRAW, HARDEN, STEEL WING...
            attacker.changeStat(1, 1, true, move.getAddEffect() == 0, defender);
            if(move.hasName("DEFENSECURL")) {
                attacker.effectMoves.set(20, 1);
            }
        } else if (effect == 39) {
            // decreases target speed - BUBBLE
            defender.changeStat(4, -1, false, move.getAddEffect() == 0, attacker);
        } else if (effect == 40) {
            // increases user speed - RAPID SPIN
            attacker.changeStat(4, 1, true, move.getAddEffect() == 0, defender);
        } else if (effect == 41) {
            // protects user - PROTECT, DETECT
            attacker.effectMoves.set(2, 1);
            attacker.protectTurns++;
            System.out.println(attacker.nickname + " is protecting itself!");
        } else if (effect == 42) {
            // confuse target - SUPERSONIC, CONFUSION, CONFUSE RAY, SIGNAL BEAM, WATER PULSE...
            if (defender.canConfuse(false, attacker)) {
                defender.causeTemporalStatus(TemporalStatus.CONFUSED, attacker);
            } else {
                return false;
            }
        } else if (effect == 43) {
            // increases a lot of user defense - IRON DEFENSE
            attacker.changeStat(1, 2, true, move.getAddEffect() == 0, defender);
        } else if (effect == 44) {
            // starts to rain - RAIN DANCE
            return battle.weather.changeWeather(Weathers.RAIN, attacker.hasItem("DAMPROCK"));
        } else if (effect == 46) {
            // returns the double of special damage - MIRROR COAT
            if(attacker.lastMoveInThisTurn == null) return false;
            if (attacker.previousDamage > 0 && attacker.lastMoveInThisTurn.getCategory().equals(Category.SPECIAL) && defender.hasType("DARK")) {
                int dmg = attacker.previousDamage*2;
                if(battle.resistsWith1HP(defender, attacker, dmg, move)) {
                    dmg -= 1;
                }
                defender.reduceHP(dmg);
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
            // ignores enemy's evasion and user's accuracy and Ghost type can be damaged by Normal/Fighting moves - FORE SIGHT, ODOR SLEUTH
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
            defender.changeStat(3, -1, false, move.getAddEffect() == 0, attacker);
        } else if (effect == 57) {
            // steals the equipped berry of target - BUG BITE, PLUCK
            if(defender.hasItemWithFlag("c") && !defender.hasAbility("STICKYHOLD") && !attacker.hasAbility("MOLDBREAKER")) {
                Item auxItem = attacker.item;
                attacker.setItem(defender.item.getInternalName());
                battle.useBerry(attacker,false);
                attacker.setItem(auxItem.getInternalName());
                defender.loseItem(false,false);
            }
        } else if (effect == 58) {
            // prevents status problems for all team - SAFEGUARD
            if (attacker.getTeam().effectTeamMoves.get(1) == 0) {
                attacker.getTeam().effectTeamMoves.set(1, 1);
                System.out.println(attacker.nickname + "'s team is protected by Safeguard!");
            } else {
                return false;
            }
        } else if (effect == 59) {
            // makes the target flee - WHIRL WIND, ROAR, CIRCLE THROW, DRAGON TAIL
            if (defender.effectMoves.get(0) > 0 || (defender.hasAbility("SUCTIONCUPS") && !attacker.hasAbility("MOLDBREAKER"))) {
                return false;
            } else {
                defender.effectMoves.set(12, 1);
                System.out.println(defender.nickname + " was expelled of the combat field!");
            }
        } else if (effect == 60) {
            // makes the user the center of attention meanwhile this turn - RAGE POWDER
            //TODO: rage powder effect
            return false;
        } else if (effect == 61 && !defender.isFainted()) {
            // decreases target special attack if is opposite sex - CAPTIVATE
            if ((attacker.getGender() != defender.getGender()) && (attacker.getGender() != 2 && defender.getGender() != 2) &&
                    !defender.hasAbility("OBLIVIOUS") && !attacker.hasAbility("MOLDBREAKER")) {
                defender.changeStat(2, -2, false, move.getAddEffect() == 0, attacker);
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
            attacker.changeStat(2, 1, true, move.getAddEffect() == 0, defender);
            attacker.changeStat(3, 1, true, move.getAddEffect() == 0, defender);
            attacker.changeStat(4, 1, true, move.getAddEffect() == 0, defender);
        } else if (effect == 64) {
            // increase a lot the user attack if it faints opponent - FELL STINGER
            attacker.changeStat(0, 3, true, move.getAddEffect() == 0, defender);
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
            if (attacker.getCriticalIndex() > 4) {
                attacker.criticalIndex = 4;
            }
        } else if (effect == 72) {
            // toxic spikes/rocks that poison/damage Pokemon entering battlefield - TOXIC SPIKES, SPIKES, STEALTH ROCK, STICKY WEB
            if(move.hasName("TOXICSPIKES")) {
                if(defender.getTeam().effectTeamMoves.get(3) < 2) {
                    defender.getTeam().increaseEffectMove(3);
                } else {
                    return false;
                }
            } else if(move.hasName("STEALTHROCK")) {
                if(defender.getTeam().effectTeamMoves.get(14) < 1) {
                    defender.getTeam().increaseEffectMove(14);
                } else {
                    return false;
                }
            } else if(move.hasName("SPIKES")) {
                if(defender.getTeam().effectTeamMoves.get(15) < 2) {
                    defender.getTeam().increaseEffectMove(15);
                } else {
                    return false;
                }
            } else if(move.hasName("STICKYWEB")) {
                if(defender.getTeam().effectTeamMoves.get(16) < 1) {
                    defender.getTeam().increaseEffectMove(16);
                } else {
                    return false;
                }
            }

            System.out.println("The enemy team field was surrounded by " + move.name + "!");
        } else if (effect == 73) {
            // increase a lot of user speed - AGILITY, ROCK POLIH, AUTOTOMIZE
            attacker.changeStat(4, 2, true, move.getAddEffect() == 0, defender);
            // TODO: AUTOTOMIZE reduces weight
            if(move.hasName("AUTOTOMIZE")) {
                System.out.println(attacker.nickname + " is lighter now!");
                attacker.setWeight(attacker.getWeight(false)-100);
            }
        } else if (effect == 74) {
            // equals target HP to attacker HP - ENDEAVOR
            if (defender.getPsActuales() <= attacker.getPsActuales()) {
                return false;
            } else {
                defender.setHP(attacker.getPsActuales());
                System.out.println(defender.nickname + " lost HP!");
            }
        } else if (effect == 75) {
            // recovers half of max HP - ROOST, RECOVER
            if (attacker.hasAllHP()) {
                return false;
            } else {
                attacker.healHP(attacker.getHP() / 2, true, false, false, true);
                if (attacker.hasType("FLYING") && move.hasName("ROOST")) {
                    attacker.effectMoves.set(10, 1);
                    //TODO: forest curse and halloween effect
                    if (attacker.battleType2 != null) { // double type
                        if (attacker.battleType1.is("FLYING")) {
                            attacker.battleType1 = attacker.battleType2;
                        }
                        attacker.battleType2 = null;
                    } else if (attacker.effectMoves.get(39) == 0) { // pure flying type
                        attacker.changeType("NORMAL","");
                    } else { // user used BURN UP
                        attacker.changeType("UNKNOWN","");
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
                defender.changeStat(6, -1, true, move.getAddEffect() == 0, attacker);
            }
            if(!battle.terrain.hasTerrain(TerrainTypes.NONE)) { // end terrains
                battle.terrain.endTerrain();
            }
            if(attacker.getTeam().effectTeamMoves.get(3) > 0) {
                attacker.getTeam().removeTeamEffects(attacker,3); // remove yours toxic spikes
            }
            if(defender.getTeam().effectTeamMoves.get(3) > 0) {
                defender.getTeam().removeTeamEffects(defender,3); // remove enemy toxic spikes
            }
            if(attacker.getTeam().effectTeamMoves.get(15) > 0) {
                attacker.getTeam().removeTeamEffects(attacker,15); // remove yours spikes
            }
            if(defender.getTeam().effectTeamMoves.get(15) > 0) {
                defender.getTeam().removeTeamEffects(defender,15); // remove enemy spikes
            }
            if(attacker.getTeam().effectTeamMoves.get(14) > 0) {
                attacker.getTeam().removeTeamEffects(attacker,14); // remove yours stealth rock
            }
            if(defender.getTeam().effectTeamMoves.get(14) > 0) {
                defender.getTeam().removeTeamEffects(defender,14); // remove enemy stealth rock
            }
            if(attacker.getTeam().effectTeamMoves.get(16) > 0) {
                attacker.getTeam().removeTeamEffects(attacker,16); // remove yours sticky web
            }
            if(defender.getTeam().effectTeamMoves.get(16) > 0) {
                defender.getTeam().removeTeamEffects(defender,16); // remove enemy sticky web
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
            if (defender.getTeam().effectTeamMoves.get(18) > 0) {
                defender.getTeam().removeTeamEffects(defender, 18); // remove rival aurora veil
            }
            if(battle.weather.hasWeather(Weathers.FOG)) { // delete fog weather
                battle.weather.endWeather();
            }
        } else if (effect == 80) {
            // take a half of the remaining HP to target - SUPERFANG
            int dmg = defender.getPsActuales() / 2;
            if (dmg <= 0) {
                dmg = 1;
            }
            if(battle.resistsWith1HP(defender, attacker, dmg, move)) {
                dmg -= 1;
            }
            defender.reduceHP(dmg);
        } else if (effect == 81) {
            // decrease a lot of target defense - SCREECH
            defender.changeStat(1, -2, false, move.getAddEffect() == 0, attacker);
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
            attacker.changeStat(0, 2, true, move.getAddEffect() == 0, defender);
        } else if(effect == 87) {
            // self damage 25% of user HP - STRUGGLE
            attacker.reduceHP(attacker.getHP()/4);
        } else if (effect == 88 && !defender.isFainted()) {
            // can paralyze, burn or freeze - TRI ATTACK
            if(Math.random() <= 0.33 && defender.canParalyze(false, attacker)) {
                defender.causeStatus(Status.PARALYZED, attacker, false);
            } else if(Math.random() <= 0.66 && defender.canBurn(false, attacker)) {
                defender.causeStatus(Status.BURNED, attacker, false);
            } else if(defender.canFreeze(false, attacker)) {
                defender.causeStatus(Status.FROZEN, attacker, false);
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
            attacker.changeStat(1,1,true,false, defender);
            attacker.changeStat(3,1,true,false, defender);
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
            attacker.healHP(attacker.getHP()/recover,true,true, false, true);
            for(int i=0;i<attacker.stockpile;i++) {
                attacker.changeStat(1,-attacker.stockpile,true,false, defender);
                attacker.changeStat(3,-attacker.stockpile,true,false, defender);
            }
            attacker.stockpile = 0;
        } else if (effect == 91) {
            // liberate energy accumulated, damage enemy and decreases user defenses - SPIT UP
            System.out.println("The energy of Stockpile disappeared!");
            for(int i=0;i<attacker.stockpile;i++) {
                attacker.changeStat(1,-attacker.stockpile,true,false, defender);
                attacker.changeStat(3,-attacker.stockpile,true,false, defender);
            }
            attacker.stockpile = 0;
        } else if (effect == 92) {
            // decreases a lot the Special Defense of target - ACID SPRAY, SEED FLARE
            defender.changeStat(3, -2, false, move.getAddEffect() == 0, attacker);
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
            attacker.changeStat(0, 1, true, move.getAddEffect() == 0, defender);
            attacker.changeStat(1, 1, true, move.getAddEffect() == 0, defender);
            attacker.changeStat(5, 1, true, move.getAddEffect() == 0, defender);
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
            if (defender.canPoison(false, attacker)) {
                defender.causeStatus(Status.BADLYPOISONED, attacker, false);
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
            defender.reducePP(defender.previousMove,4, attacker);
            System.out.println(defender.previousMove.name + " of " + defender.nickname + " lost 4 PP!");
        } else if (effect == 100) {
            // switches items with target - SWITCHEROO, TRICK
            // TODO: kyogre with blue orb, groudon with red orb, mail and mega stones
            if(attacker.item == null && defender.item == null) {
                return false;
            }
            if((defender.hasAbility("STICKYHOLD") && !attacker.hasAbility("MOLDBREAKER")) || defender.hasAbility("MULTITYPE")) {
                return false;
            }
            if(attacker.item != null) {
                if((attacker.hasItem("GRISEOUSORB") && attacker.specieNameIs("GIRATINA")) ||
                        (attacker.item.getFlags().contains("l") && attacker.specieNameIs("ARCEUS")) ||
                        ((attacker.hasItem("DOUSEDRIVE") || attacker.hasItem("SHOCKDRIVE") || attacker.hasItem("CHILLDRIVE") ||
                                attacker.hasItem("BURNDRIVE")) && attacker.specieNameIs("GENESECT")) ||
                        (attacker.item.getFlags().contains("m") && attacker.specieNameIs("SILVALLY"))) {
                    return false;
                }
            }
            if(defender.item != null) {
                if((defender.hasItem("GRISEOUSORB") && defender.specieNameIs("GIRATINA")) ||
                        (defender.item.getFlags().contains("l") && defender.specieNameIs("ARCEUS")) ||
                        ((defender.hasItem("DOUSEDRIVE") || defender.hasItem("SHOCKDRIVE") || defender.hasItem("CHILLDRIVE") ||
                                defender.hasItem("BURNDRIVE")) && defender.specieNameIs("GENESECT")) ||
                        (defender.item.getFlags().contains("m") && defender.specieNameIs("SILVALLY"))) {
                    return false;
                }
            }
            System.out.println(attacker.nickname + " and " + defender.nickname + " switch items!");
            Item itemAux = attacker.item;
            attacker.giveItem(defender.item.getInternalName(), false);
            defender.giveItem(itemAux.getInternalName(), false);

        } else if (effect == 101) {
            // freeze or flinches the target - ICE FANG
            double prob = 0.1;
            if(attacker.hasAbility("SERENEGRACE")) prob *= 2;
            if (defender.canFreeze(false, attacker) && Math.random() <= prob) {
                defender.causeStatus(Status.FROZEN, attacker, false);
            }
            if (defender.canFlinch(attacker) && Math.random() <= prob) {
                defender.causeTemporalStatus(TemporalStatus.FLINCHED, attacker);
            }
        } else if (effect == 102) {
            // paralyze or flinches the target - THUNDER FANG
            double prob = 0.1;
            if(attacker.hasAbility("SERENEGRACE")) prob *= 2;
            if (defender.canParalyze(false, attacker) && Math.random() <= prob) {
                defender.causeStatus(Status.PARALYZED, attacker, false);
            }
            if (defender.canFlinch(attacker) && Math.random() <= prob) {
                defender.causeTemporalStatus(TemporalStatus.FLINCHED, attacker);
            }
        }
        if (effect == 104) {
            // breaks protect moves - FEINT
            nullProtections(attacker,defender);
        } else if (effect == 105) {
            // increase user evasion - DOUBLE TEAM
            attacker.changeStat(6, 1, true, move.getAddEffect() == 0, defender);
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
            if(attacker.item == null) {
                return false;
            }
            if(defender.item != null) {
                return false;
            }
            if((attacker.hasItem("GRISEOUSORB") && attacker.specieNameIs("GIRATINA")) ||
                    (attacker.item.getFlags().contains("l") && attacker.specieNameIs("ARCEUS")) ||
                    ((attacker.hasItem("DOUSEDRIVE") || attacker.hasItem("SHOCKDRIVE") || attacker.hasItem("CHILLDRIVE") ||
                            attacker.hasItem("BURNDRIVE")) && attacker.specieNameIs("GENESECT")) ||
                    (attacker.item.getFlags().contains("m") && attacker.specieNameIs("SILVALLY"))) {
                return false;
            } // TODO: kyogre with blue orb, groudon with red orb, mail and mega stones
            defender.giveItem(attacker.item.getInternalName(), false);
            System.out.println(attacker.nickname + " gives its " + attacker.item.name + " to " + defender.nickname + "!");
            attacker.loseItem(false, false);
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
                int dmg = attacker.bideDamage*2;
                if(battle.resistsWith1HP(defender, attacker, dmg, move)) {
                    dmg -= 1;
                }
                defender.reduceHP(dmg);
                attacker.bideDamage = 0;

            }
        } else if (effect == 110) {
            // powers the next electric move and increase special defense - CHARGE
            attacker.effectMoves.set(23, 1);
            System.out.println(attacker.nickname + " is charging energy!");
            attacker.changeStat(3, 1, true, move.getAddEffect() == 0, defender);
        } else if (effect == 111) {
            // create electric terrain - ELECTRIC TERRAIN
            return battle.terrain.activateTerrain(attacker,TerrainTypes.ELECTRIC,attacker.hasItem("TERRAINEXTENDER"));
        } else if (effect == 112) {
            // create psychic terrain - PSYCHIC TERRAIN
            return battle.terrain.activateTerrain(attacker,TerrainTypes.PSYCHIC,attacker.hasItem("TERRAINEXTENDER"));
        } else if (effect == 113) {
            // create misty terrain - MISTY TERRAIN
            return battle.terrain.activateTerrain(attacker,TerrainTypes.MISTY,attacker.hasItem("TERRAINEXTENDER"));
        } else if (effect == 114) {
            // create grassy terrain - GRASSY TERRAIN
            return battle.terrain.activateTerrain(attacker,TerrainTypes.GRASSY,attacker.hasItem("TERRAINEXTENDER"));
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
            defender.changeStat(0, -1, false, move.getAddEffect() == 0, attacker);
            defender.changeStat(1, -1, false, move.getAddEffect() == 0, attacker);
        } else if (effect == 119) {
            // heals in the next turn - WISH
            if(attacker.getTeam().effectTeamMoves.get(7) > 0) {
                return false;
            }
            attacker.getTeam().effectTeamMoves.set(7, 1);
            System.out.println(attacker.nickname + " take a wish!");
            attacker.getTeam().wishRecover = attacker.getHP()/2;
        } else if (effect == 120) {
            // attacks while 5 turns increasing power - ROLLOUT, ICE BALL
            if (attacker.effectMoves.get(25) < 5 && attacker.effectMoves.get(35) == 0) { // if sleep talk, no increase
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
            return battle.weather.changeWeather(Weathers.SANDSTORM, attacker.hasItem("SMOOTHROCK"));
        } else if (effect == 126) {
            // increase user attack and accuracy - HONE CLAWS
            attacker.changeStat(0, 1, true, move.getAddEffect() == 0, defender);
            attacker.changeStat(5, 1, true, move.getAddEffect() == 0, defender);
        } else if (effect == 127) {
            // increase attack and special attack of Grass Pokemon - ROTOTILLER
            int nGrass = 0;
            if(attacker.hasType("GRASS") && !attacker.isLevitating()) {
                attacker.changeStat(0, 1, false, move.getAddEffect() == 0, defender);
                attacker.changeStat(2, 1, false, move.getAddEffect() == 0, defender);
                nGrass++;
            }
            if(defender.hasType("GRASS") && !defender.isLevitating()) {
                defender.changeStat(0, 1, false, move.getAddEffect() == 0, attacker);
                defender.changeStat(2, 1, false, move.getAddEffect() == 0, attacker);
                nGrass++;
            }
            return nGrass != 0;
        }
        if (effect == 129) {
            // increase allay moves power in this turn, only works in double battles - HELPING HAND
            // TODO: helping hand effect
            return false;
        } else if (effect == 130) {
            // confuses the target and increase it Special Attack - FLATTER
            defender.changeStat(2, 1, false, move.getAddEffect() == 0, attacker);
            if (defender.canConfuse(false, attacker)) {
                defender.causeTemporalStatus(TemporalStatus.CONFUSED, attacker);
            }
        } else if (effect == 131) {
            // decreases Attack, Special Attack and Speed of poisoned enemies - VENOM DRENCH
            if(defender.hasStatus(Status.POISONED) || defender.hasStatus(Status.BADLYPOISONED)) {
                defender.changeStat(0, -1, false, move.getAddEffect() == 0, attacker);
                defender.changeStat(2, -1, false, move.getAddEffect() == 0, attacker);
                defender.changeStat(4, -1, false, move.getAddEffect() == 0, attacker);
                return true;
            }
            return false;
        } else if (effect == 132) {
            // decreases the Attack and Defense of user - SUPERPOWER
            attacker.changeStat(0, -1, true, move.getAddEffect() == 0, defender);
            attacker.changeStat(1, -1, true, move.getAddEffect() == 0, defender);
        } else if(effect == 133) {
            // KO move - HORN DRILL, FISSURE, GUILLOTINE, SHEER COLD
            System.out.println("One hit KO!");
            if(battle.resistsWith1HP(defender, attacker, defender.getPsActuales(),move)) { // target can resist?
                defender.reduceHP(defender.getPsActuales()-1);
            } else {
                defender.reduceHP(defender.getPsActuales());
            }
        } else if (effect == 134 && !attacker.hasAbility("MAGICGUARD") && !attacker.hasAbility("ROCKHEAD")) {
            // recoil a lot of damage - HEAD SMASH
            attacker.reduceHP(damage / 2);
        } else if (effect == 135) {
            // makes the rival the center of attention meanwhile this turn - SPOT LIGHT
            // TODO: spot light effect
            return false;
        } else if (effect == 137) {
            // increase a lot of user evasion - MINIMIZE
            attacker.changeStat(6, 2, true, move.getAddEffect() == 0, defender);
            attacker.effectMoves.set(29, 1);
        } else if (effect == 139) {
            // use a random move - METRONOME
            Random rand = new Random();
            Movement randomMove = null;
            do {
                randomMove = attacker.utils.getMoves().get(rand.nextInt(attacker.utils.getMoves().size()));
            } while(randomMove.getFlags().contains("p") || randomMove.hasName("RELICSONG") || randomMove.hasName("CHATTER") ||
                    randomMove.hasName("ICEBURN") || randomMove.hasName("VCREATE") || randomMove.hasName("FREEZESHOCK") ||
                    randomMove.hasName("SECRETSWORD") || randomMove.hasName("TECHNOBLAST")); // forbidden moves

            battle.useMove(attacker, defender, randomMove, defenderMove, false, false, true);
        } else if (effect == 140) {
            // increase a defense and special defense of user - COSMIC POWER
            attacker.changeStat(1, 1, true, move.getAddEffect() == 0, defender);
            attacker.changeStat(3, 1, true, move.getAddEffect() == 0, defender);
        } else if (effect == 141) {
            // decreases Special Attack of target - MOON BLAST
            defender.changeStat(2, -1, false, move.getAddEffect() == 0, attacker);
        } else if (effect == 142) {
            // increase gravity - GRAVITY
            if(battle.effectFieldMoves.get(2) > 0) {
                return false;
            }
            battle.effectFieldMoves.set(2, 1);
            // telekinesis ends
            attacker.effectMoves.set(42, 0);
            defender.effectMoves.set(42, 0);
            // magnet rise ends
            attacker.effectMoves.set(45, 0);
            defender.effectMoves.set(45, 0);
            System.out.println("Gravity incremented!");
            // TODO: interrupt fly
            if(defender.effectMoves.get(44) > 0) { // bounce
                defender.effectMoves.set(44, 0);
                defender.effectMoves.set(3, 0);
                System.out.println(defender.nickname + " dropped to ground!");
                defender.reducePP(defenderMove, 1, attacker);
            }
            if(defender.effectMoves.get(59) > 0) { // sky drop
                defender.effectMoves.set(59, 0);
                attacker.effectMoves.set(59, 0);
                defender.effectMoves.set(3, 0);
                defender.reducePP(defenderMove, 1, attacker);
            }
        } else if (effect == 143) {
            // user sacrifices itself and the next Pokemon is healed - HEALING WISH, LUNAR DANCE
            if(attacker.getTeam().alivePokemon() == 1) {
                return false;
            }
            System.out.println(attacker.nickname + " sacrifices itself!");
            if(move.hasName("LUNARDANCE")) {
                attacker.getTeam().effectTeamMoves.set(20, 1);
            } else {
                attacker.getTeam().effectTeamMoves.set(8, 1);
            }
            attacker.reduceHP(-1);
        } else if (effect == 144) {
            // makes the target moves immediately after user - AFTER YOU
            //TODO: after you effect
            return false;
        } else if (effect == 145) {
            // heal user team from poison, paralysis, burn, frozen or sleep - AROMATHERAPY
            System.out.println("A soothing aroma wafted through the area!");
            for(int i=0;i<attacker.getTeam().getPokemonTeam().size();i++) {
                Pokemon poke = attacker.getTeam().getPokemon(i);
                if (poke.hasStatus(Status.POISONED) || poke.hasStatus(Status.BADLYPOISONED) || poke.hasStatus(Status.PARALYZED)
                        || poke.hasStatus(Status.BURNED) || poke.hasStatus(Status.FROZEN) || poke.hasStatus(Status.ASLEEP)) {
                    attacker.healPermanentStatus();
                }
            }
        } else if (effect == 146) {
            // steals target item - COVET, THIEF
            return stealItem(attacker,defender);
        } else if (effect == 147) {
            // recover 50% of HP to target - HEAL PULSE
            int quantity = defender.getHP()/2;
            if(attacker.hasAbility("MEGALAUNCHER")) {
                quantity = (int) (defender.getHP()*0.75);
            }
            return attacker.healHP(quantity, true, true, false, true);
        } else if (effect == 148) {
            // copies the last move used by target - MIMIC
            // TODO: mimic effect
        } else if (effect == 149) {
            // does nothing - SPLASH
            System.out.println("But nothing happened!");
        } else if (effect == 151) {
            // impedes that rivals use moves known by user - IMPRISON
            System.out.println(attacker.nickname + " imprisons " + defender.nickname + "'s moves!");
            attacker.effectMoves.set(30, 1);
        } else if (effect == 152) {
            // if user faints for an opponent attack, it wipes out from PP - GRUDGE
            System.out.println(attacker.nickname + " is grudging " + defender.nickname + "!");
            attacker.effectMoves.set(31, 1);
        } else if (effect == 153) {
            // switches the stat changes of Attack and Sp Attack between user and target - POWER SWAP
            List<Integer> aux = defender.getStatChanges();
            defender.setStatChanges(0, attacker.getStatChanges().get(0));
            defender.setStatChanges(2, attacker.getStatChanges().get(2));
            attacker.setStatChanges(0, aux.get(0));
            attacker.setStatChanges(2, aux.get(2));
            System.out.println("The Attack and Sp. Attack changes from " + attacker.nickname + " and " + defender.nickname + " swapped!");
        } else if (effect == 154) {
            // change secondary effect depending on the environment - SECRET POWER
            // TODO: turns in another secondary effect depends on the environment
            if(battle.terrain.hasTerrain(TerrainTypes.GRASSY)) {
                if (defender.canSleep(false, attacker)) { defender.causeStatus(Status.ASLEEP, attacker, false); }
            } else if(battle.terrain.hasTerrain(TerrainTypes.ELECTRIC)) {
                if (defender.canParalyze(false, attacker)) { defender.causeStatus(Status.PARALYZED, attacker, false); }
            } else if(battle.terrain.hasTerrain(TerrainTypes.MISTY)) {
                defender.changeStat(2, -1, false, move.getAddEffect() == 0, attacker);
            } else if(battle.terrain.hasTerrain(TerrainTypes.PSYCHIC)) {
                defender.changeStat(4, -1, false, move.getAddEffect() == 0, attacker);
            }
        } else if (effect == 155) {
            // increases a lot the Special Attack of user - NASTY PLOT
            attacker.changeStat(2, 2, true, move.getAddEffect() == 0, defender);
        } else if (effect == 156) {
            // switches the stat changes of Defense and Sp Defense between user and target - GUARD SWAP
            List<Integer> aux = defender.getStatChanges();
            defender.setStatChanges(1, attacker.getStatChanges().get(1));
            defender.setStatChanges(3, attacker.getStatChanges().get(3));
            attacker.setStatChanges(1, aux.get(1));
            attacker.setStatChanges(3, aux.get(3));
            System.out.println("The Defense and Sp. Defense changes from " + attacker.nickname + " and " + defender.nickname + " swapped!");
        } else if (effect == 157) {
            // if other Pokemon uses this move in the turn the power is incremented - ROUND
            battle.effectFieldMoves.set(3, 1);
        } else if (effect == 158) {
            // sleeps and recover HP - REST
            if(attacker.hasAllHP()) {
                return false;
            }
            if(!attacker.canSleep(true,defender)) {
                return false;
            }
            attacker.healHP(-1,true,false, false, true);
            attacker.causeStatus(Status.ASLEEP, defender, true);
            attacker.effectMoves.set(32, 1);
        } else if (effect == 159) {
            // a song that faint every Pokemon in 3 turns - PERISH SONG
            System.out.println("All Pokemon that hear the song will faint in three turns!");
            if(attacker.effectMoves.get(33) == 0) {
                attacker.effectMoves.set(33, 1);
            }
            if(defender.effectMoves.get(33) == 0) {
                defender.effectMoves.set(33, 1);
            }
        } else if (effect == 161) {
            // if is slept, user will use a random move between its move set - SLEEP TALK
            attacker.effectMoves.set(35, 1);
            Movement m = attacker.sleepTalkMove();
            if(m.hasName("REST")) {
                return false;
            } else {
                battle.useMove(attacker,defender,m,defenderMove,false,false,true);
            }
        } else if (effect == 162) {
            // impedes rival to scape - MEAN LOOK
            if(defender.hasType("GHOST")) {
                return false;
            }
            System.out.println(defender.nickname + " can't scape now!");
            defender.effectMoves.set(34, 1);
        } else if (effect == 163) {
            // protect team from priority moves - QUICK GUARD
            if (attacker.getTeam().effectTeamMoves.get(9) > 0) {
                return false;
            }
            attacker.getTeam().effectTeamMoves.set(9, 1);
            attacker.protectTurns++;
            System.out.println(attacker.nickname + "'s team was protected by " + move.name);
        } else if(effect == 164) { // NATURAL GIFT
            attacker.loseItem(true, true);
        } else if (effect == 165) {
            // reduces the Attack and absorb HP - STRENGTH SAP
            if(defender.getStats().get(0) == -6) {
                System.out.println("Attack of " + defender.nickname + " can't decrease more!");
                return false;
            }
            int ps = (int) (defender.getStats().get(1) * defender.getStatChange(0));
            defender.changeStat(0, -1, false, move.getAddEffect() == 0, attacker);
            if(!defender.hasAbility("LIQUIDOOZE")) {
                attacker.healHP(ps,true,true,true, true);
            } else {
                attacker.reduceHP(ps);
            }

        } else if (effect == 166) {
            // protect team from multi target moves - WIDE GUARD
            if(attacker.getTeam().effectTeamMoves.get(10) > 0) {
                return false;
            }
            attacker.getTeam().effectTeamMoves.set(10, 1);
            attacker.protectTurns++;
            System.out.println(attacker.nickname + "'s team was protected by " + move.name);
        } else if (effect == 167) {
            // switch Pokemon and remain all its stat changes - BATON PASS
            // TODO: baton pass effect
            return false;
        } else if (effect == 168) {
            // swap abilities between user and target - SKILL SWAP
            if (defender.hasAbility("POWERCONSTRUCT") || defender.hasAbility("SCHOOLING") || defender.hasAbility("STANCECHANGE") ||
                    defender.hasAbility("DISGUISE") || defender.hasAbility("SHIELDSDOWN") || defender.hasAbility("BATTLEBOND") ||
                    defender.hasAbility("ILLUSION") || defender.hasAbility("COMATOSE") || defender.hasAbility("ZENMODE") ||
                    defender.hasAbility("MULTITYPE") || defender.hasAbility("RKSSYSTEM") || defender.hasAbility("WONDERGUARD")) {
                return false;
            } else if(attacker.hasAbility("POWERCONSTRUCT") || attacker.hasAbility("SCHOOLING") || attacker.hasAbility("STANCECHANGE") ||
                    attacker.hasAbility("DISGUISE") || attacker.hasAbility("SHIELDSDOWN") || attacker.hasAbility("BATTLEBOND") ||
                    attacker.hasAbility("ILLUSION") || attacker.hasAbility("COMATOSE") || attacker.hasAbility("ZENMODE") ||
                    attacker.hasAbility("MULTITYPE") || attacker.hasAbility("RKSSYSTEM") || attacker.hasAbility("WONDERGUARD")) {
                return false;
            } else {
                Ability auxAbility = defender.getAbility();
                defender.setAbility(attacker.getAbility().getInternalName());
                attacker.setAbility(auxAbility.getInternalName());
                System.out.println(attacker.nickname + " and " + defender.nickname + " swapped abilities!");
            }
        } else if (effect == 169) {
            // faints itself and decrease Attack and Special Attack of target - MEMENTO
            System.out.println(attacker.nickname + " sacrifices itself!");
            attacker.reduceHP(-1);
            defender.changeStat(0, -2, false, move.getAddEffect() == 0, attacker);
            defender.changeStat(2, -2, false, move.getAddEffect() == 0, attacker);
        } else if (effect == 170) {
            // impedes that rivals use status moves - TAUNT
            if((defender.hasAbility("OBLIVIOUS") || defender.hasAbility("AROMAVEIL")) && !attacker.hasAbility("MOLDBREAKER")) {
                return false;
            }
            System.out.println(defender.nickname + " was taunted by " + attacker.nickname + "!");
            defender.effectMoves.set(37, 1);
        } else if (effect == 171) {
            // spread money in field - PAY DAY
            attacker.getTeam().effectTeamMoves.set(11, attacker.getTeam().effectTeamMoves.get(11)+(5* attacker.getLevel()));
            System.out.println("There is money everywhere!");
        } else if (effect == 172) {
            // use a random move of the team - ASSIST
            Random rand = new Random();
            Movement randomMove = null;
            ArrayList<Movement> teamMoves = new ArrayList<>();
            for(int i=0;i<attacker.getTeam().getPokemonTeam().size();i++) {
                Pokemon pk = attacker.getTeam().getPokemonTeam().get(i);
                if(pk != attacker) {
                    for(int j=0;j<pk.getMoves().size();j++) {
                        Movement mv = pk.getMoves().get(j).getMove();
                        if(!mv.hasName("ENDURE") && !mv.hasName("COVET") && !mv.hasName("ASSIST") && !mv.hasName("STRUGGLE")
                                && !mv.hasName("COUNTER") && !mv.hasName("DETECT") && !mv.hasName("SKETCH")
                                && !mv.hasName("THIEF") && !mv.hasName("MIRRORCOAT") && !mv.hasName("METRONOME")
                                && !mv.hasName("MIMIC") && !mv.hasName("DESTINYBOND") && !mv.hasName("PROTECT")
                                && !mv.hasName("MIRRORMOVE") && !mv.hasName("MIRRORCOAT") && !mv.hasName("METRONOME")
                                && !mv.hasName("FOCUSPUNCH") && !mv.hasName("HELPINGHAND") && !mv.hasName("SNATCH")
                                && !mv.hasName("FOLLOWME") && !mv.hasName("SLEEPTALK") && !mv.hasName("TRICK")
                                && !mv.hasName("FEINT") && !mv.hasName("CHATTER") && !mv.hasName("COPYCAT")
                                && !mv.hasName("SWITCHEROO") && !mv.hasName("MEFIRST") && !mv.hasName("NATUREPOWER")
                                && !mv.hasName("DRAGONTAIL") && !mv.hasName("CIRCLETHROW") && !mv.hasName("TRANSFORM")
                                && !mv.hasName("SPIKYSHIELD") && !mv.hasName("BOUNCE") && !mv.hasName("DIVE")
                                && !mv.hasName("SKYDROP") && !mv.hasName("CELEBRATE") && !mv.hasName("BELCH")
                                && !mv.hasName("KINGSSHIELD") && !mv.hasName("MATBLOCK") && !mv.hasName("DIG")
                                && !mv.hasName("PHANTOMFORCE") && !mv.hasName("SHADOWFORCE") && !mv.hasName("HOLDHANDS")
                                && !mv.hasName("WHIRLWIND") && !mv.hasName("ROAR") && !mv.hasName("FLY")
                                && !mv.hasName("BANEFULBUNKER") && !mv.hasName("SHELLTRAP") && !mv.hasName("SPOTLIGHT")
                                && !mv.hasName("BEAKBLAST")) {
                            teamMoves.add(mv);
                        }
                    }
                }
            }
            if(teamMoves.isEmpty()) {
                return false;
            }

            randomMove = attacker.utils.getMoves().get(rand.nextInt(attacker.utils.getMoves().size()));
            battle.useMove(attacker, defender, randomMove, defenderMove, false, false, true);
        } else if (effect == 174) {
            // reduces the fire moves power - WATER SPORT
            battle.effectFieldMoves.set(4, 1);
            System.out.println("The power of Fire moves are reduced!");
        } else if (effect == 175) {
            // transforms target to Water Type - SOAK
            if ((defender.battleType2 == null && defender.battleType1.is("WATER")) || defender.hasAbility("MULTITYPE")
                    || defender.hasAbility("RKSSYSTEM")) { // pure Water type
                return false;
            } else {
                System.out.println(defender.nickname + " change to Water-Type!");
                defender.changeType("WATER","");
            }
        } else if (effect == 176) {
            // copies target's stat changes - PSYCH UP
            System.out.println(attacker.nickname + " copied " + defender.nickname + "'s stat changes!");
            for(int i=0;i<attacker.getStatChanges().size();i++) {
                attacker.setStatChanges(i, defender.getStatChanges().get(i));
            }
        } else if (effect == 177) {
            // switch defense and special defense of every battler - WONDER ROOM
            if(battle.effectFieldMoves.get(5) == 0) {
                battle.effectFieldMoves.set(5, 1);
            } else {
                battle.effectFieldMoves.set(5, 0);
            }
            System.out.println("Defense and Special Defense was switched!");
        } else if (effect == 178) {
            // restores stat changes of target - CLEAR SMOG
            defender.getStatChanges().replaceAll(ignored -> 0);
            System.out.println("The stat changes of " + defender.nickname + " were removed!");
        } else if (effect == 179) {
            // attacks after two turns - FUTURE SIGHT, DOOM DESIRE
            if(defender.getTeam().effectTeamMoves.get(12) > 0) {
                return false;
            }
            defender.getTeam().effectTeamMoves.set(12, 1);
            if(move.hasName("FUTURESIGHT")) {
                System.out.println(attacker.nickname + " is preventing an attack!");
                defender.getTeam().futureAttackId = 1;
            } else if(move.hasName("DOOMDESIRE")) {
                System.out.println(attacker.nickname + " take a wish!");
                defender.getTeam().futureAttackId = 2;
            }
            defender.getTeam().futureAttackerPoke = attacker;
        } else if (effect == 180) {
            // change ability to Simple - SIMPLE BEAM
            if (defender.hasAbility("TRUANT") || defender.hasAbility("SCHOOLING") || defender.hasAbility("STANCECHANGE") ||
                    defender.hasAbility("MULTITYPE") || defender.hasAbility("ICEFACE") || defender.hasAbility("DISGUISE") ||
                    defender.hasAbility("SHIELDSDOWN") || defender.hasAbility("BATTLEBOND") || defender.hasAbility("POWERCONSTRUCT") ||
                    defender.hasAbility("COMATOSE") || defender.hasAbility("RKSSYSTEM") || defender.hasAbility("GULPMISSILE")) {
                return false;
            } else {
                defender.changeAbility("SIMPLE");
            }
        } else if (effect == 183) {
            // equals target HP to attacker level - SISMIC, NIGHT SHADE
            int dmg = attacker.getLevel();
            if(battle.resistsWith1HP(defender, attacker, dmg, move)) {
                dmg -= 1;
            }
            defender.reduceHP(dmg);
        } else if (effect == 184) {
            // confuses the target and increase a lot it Attack - SWAGGER
            defender.changeStat(0, 2, false, move.getAddEffect() == 0, attacker);
            if (defender.canConfuse(false, attacker)) {
                defender.causeTemporalStatus(TemporalStatus.CONFUSED, attacker);
            }
        } else if (effect == 185) {
            // decreases the Defense and Special Defense of user - CLOSE COMBAT
            attacker.changeStat(1, -1, true, move.getAddEffect() == 0, defender);
            attacker.changeStat(3, -1, true, move.getAddEffect() == 0, defender);
        } else if (effect == 187) {
            // depends of the item has a different effect - FLING
            if(attacker.hasItem("POISONBARB") && defender.canPoison(false, attacker)) {
                defender.causeStatus(Status.POISONED, attacker, false);
            } else if(attacker.hasItem("TOXICORB") && defender.canPoison(false, attacker)) {
                defender.causeStatus(Status.BADLYPOISONED, attacker, false);
            } else if(attacker.hasItem("LIGHTBALL") && defender.canParalyze(false, attacker)) {
                defender.causeStatus(Status.PARALYZED, attacker, false);
            } else if(attacker.hasItem("FLAMEORB") && defender.canBurn(false, attacker)) {
                defender.causeStatus(Status.BURNED, attacker, false);
            } else if((attacker.hasItem("RAZORFANG") || attacker.hasItem("KINGSROCK")) && defender.canFlinch(attacker)) {
                defender.causeTemporalStatus(TemporalStatus.FLINCHED, attacker);
            } else if(attacker.hasItem("MENTALHERB")) {
                if(defender.hasTemporalStatus(TemporalStatus.INFATUATED)) {
                    defender.healTempStatus(TemporalStatus.INFATUATED, true);
                }
                defender.effectMoves.set(37, 0); // taunt
                defender.effectMoves.set(26, 0); // encore
                defender.encoreMove = null;
                defender.effectMoves.set(17, 0); // disable
                defender.disabledMove = null;
                defender.cursedBodyMove = null;
                // TODO: erase torment and cursed body

            } else if(attacker.hasItem("WHITEHERB")) {
                for(int i=0;i<defender.getStatChanges().size();i++) {
                    if(defender.getStatChanges().get(i) < 0) {
                        defender.getStatChanges().set(i, 0);
                    }
                }
            } else if(attacker.hasItemWithFlag("c")) { // has a berry
                Item auxItem = defender.item;
                defender.setItem(attacker.item.getInternalName());
                battle.useBerry(defender,false);
                defender.setItem(auxItem.getInternalName());
            }

            attacker.loseItem(true, true);
        } else if (effect == 189) {
            // attacks and loses its Fire Type - BURN UP
            if (attacker.battleType2 == null && attacker.battleType1.is("FIRE")) { // pure Fire type
                attacker.changeType("UNKNOWN","");
            } else if(attacker.battleType2.is("FIRE")) { // Fire type is secondary
                defender.battleType2 = null;
            } else { // Fire type is primary
                attacker.battleType1 = attacker.battleType2;
                attacker.battleType2 = null;
            }
            System.out.println(attacker.nickname + " loses its Fire-Type!");
            attacker.effectMoves.set(39, 1);
        } else if (effect == 190) {
            // the next move will hit - MIND READER, LOCK-ON
            defender.effectMoves.set(40, 1);
            System.out.println(defender.nickname + " is focused!");
        } else if (effect == 191) {
            // runs of a wild battle - TELEPORT
            if(battle.canScape(attacker,defender)) {
                attacker.effectMoves.set(12, 1);
            } else {
                return false;
            }
        } else if (effect == 192) {
            // switch positions between Pokemon - ALLY SWITCH
            // TODO: ally switch effect
            return false;
        }
        if (effect == 193) {
            // share the sum of Defense and Special Defense between user and target - GUARD SPLIT
            int defenses = (attacker.getStats().get(2) + defender.getStats().get(2))/2;
            int specialDef = (attacker.getStats().get(4) + defender.getStats().get(4))/2;
            attacker.setStatValue(2, defenses);
            attacker.setStatValue(4, specialDef);
            defender.setStatValue(2, defenses);
            defender.setStatValue(4, specialDef);
            System.out.println("The defenses of " + attacker.nickname + " and " + defender.nickname + " were shared between them!");
        } else if (effect == 194) {
            // switches the stat changes of Defense and Sp Defense between user and target - GUARD SWAP
            List<Integer> aux = defender.getStatChanges();
            defender.setStatChanges(1, attacker.getStatChanges().get(1));
            defender.setStatChanges(3, attacker.getStatChanges().get(3));
            attacker.setStatChanges(1, aux.get(1));
            attacker.setStatChanges(3, aux.get(3));
            System.out.println("The Defense and Sp. Defense changes from " + attacker.nickname + " and " + defender.nickname + " swapped!");
        } else if (effect == 195) {
            // drop target item - KNOCK OFF
            if(attacker.item != null) {
                return false;
            }
            if(defender.item != null || (defender.hasAbility("STICKYHOLD") && !attacker.hasAbility("MOLDBREAKER"))) {
                return false;
            }
            if((defender.hasItem("GRISEOUSORB") && defender.specieNameIs("GIRATINA")) ||
                    (defender.item.getFlags().contains("l") && defender.hasAbility("MULTITYPE")) ||
                    ((defender.hasItem("DOUSEDRIVE") || defender.hasItem("SHOCKDRIVE") || defender.hasItem("CHILLDRIVE") ||
                            defender.hasItem("BURNDRIVE")) && defender.specieNameIs("GENESECT")) ||
                    (defender.item.getFlags().contains("m") && defender.specieNameIs("SILVALLY"))) {
                return false;
            } // TODO: kyogre with blue orb, groudon with red orb, mail and mega stones

            System.out.println(defender.item.name + " dropped from " + defender.nickname + "!");
            defender.loseItem(false, false);
        } else if (effect == 196) {
            // switches the Attack and Defense of user - POWER TRICK
            int attack = attacker.getStats().get(1);
            attacker.setStatValue(1, attacker.getStats().get(2));
            attacker.setStatValue(2, attack);
            System.out.println("The Attack and Defense from " + attacker.nickname + " switched!");
        } else if (effect == 197) {
            // transfers status changes to target - PSYCHO SHIFT
            if(attacker.hasStatus(Status.BURNED) && defender.canBurn(false,attacker)) {
                defender.causeStatus(Status.BURNED,attacker,false);
            }
            if(attacker.hasStatus(Status.PARALYZED) && defender.canParalyze(false,attacker)) {
                defender.causeStatus(Status.PARALYZED,attacker,false);
            }
            if(attacker.hasStatus(Status.ASLEEP) && defender.canSleep(false,attacker)) {
                defender.causeStatus(Status.ASLEEP,attacker,false);
            }
            if(attacker.hasStatus(Status.POISONED) && defender.canPoison(false,attacker)) {
                defender.causeStatus(Status.POISONED,attacker,false);
            }
            if(attacker.hasStatus(Status.BADLYPOISONED) && defender.canPoison(false,attacker)) {
                defender.causeStatus(Status.BADLYPOISONED,attacker,false);
            }
            return false;
        } else if (effect == 198) {
            // ignores enemy's evasion and user's accuracy and Dark type can be damaged by Psychic moves - MIRACLE EYE
            if (defender.effectMoves.get(41) == 0) {
                defender.effectMoves.set(41, 1);
                System.out.println(defender.nickname + " was identified!");
            } else {
                return false;
            }
        } else if (effect == 199) {
            // makes the Pokemon levitate for 3 turns - TELEKINESIS
            if(defender.effectMoves.get(42) > 0 || defender.effectMoves.get(0) > 0 || (defender.hasItem("IRONBALL") && defender.canUseItem())) {
                return false;
            }
            if(defender.specieNameIs("DIGLETT") || defender.specieNameIs("DUGTRIO") || defender.specieNameIs("SANDYGAST") ||
                    defender.specieNameIs("PALOSSAND")) { //TODO: MEGA GENGAR also
                return false;
            }
            if(defender.effectMoves.get(43) > 0) { // smack down makes fail
                return false;
            }
            // TODO: also fails if is affected with THOUSAND ARROWS
            defender.effectMoves.set(42, 1);
            System.out.println(defender.nickname + " is levitating with " + move.name + "!");
        } else if (effect == 200) {
            // copy target's ability - ROLE PLAY
            return changeAbility(attacker,defender);
        } else if (effect == 201) {
            // increases the Special Attack and Special Defense of user - CALM MIND
            attacker.changeStat(2, 1, true, move.getAddEffect() == 0, defender);
            attacker.changeStat(3, 1, true, move.getAddEffect() == 0, defender);
        } else if (effect == 202) {
            // increases the Attack and Defense of user - BULK UP
            attacker.changeStat(0, 1, true, move.getAddEffect() == 0, defender);
            attacker.changeStat(1, 1, true, move.getAddEffect() == 0, defender);
        } else if (effect == 207) {
            // transforms user to target types - REFLECT TYPE
            attacker.changeType(defender.battleType1.getInternalName(),defender.battleType2.getInternalName());
            // TODO: forest curse or trick or treat
        } else if (effect == 208) {
            // throw the target to ground - SMACK DOWN
            defender.effectMoves.set(43, 1);
            System.out.println(defender.nickname + " was thrown to ground!");
            defender.effectMoves.set(42, 0); // ends telekinesis
            defender.effectMoves.set(45, 0); // ends magnet rise
            // TODO: interrupt fly
            if(defender.effectMoves.get(44) > 0) { // bounce
                defender.effectMoves.set(44, 0);
                defender.effectMoves.set(3, 0);
                defender.reducePP(defenderMove, 1, attacker);
            }
        } else if (effect == 210) {
            // decreases user speed - HAMMER ARM
            attacker.changeStat(4, -1, true, move.getAddEffect() == 0, defender);
        } else if (effect == 212) {
            // makes user levitate - MAGNET RISE
            if(attacker.effectMoves.get(45) > 0 || attacker.effectMoves.get(0) > 0 || (attacker.hasItem("IRONBALL") && attacker.canUseItem())) {
                return false;
            }
            attacker.effectMoves.set(45, 1);
            System.out.println(attacker.nickname + " is levitating!");
        } else if (effect == 216) {
            // increases a lot a random stat of user - ACUPRESSURE
            if(attacker.statsAreMaximum()) {
                return false;
            }
            Random rand = new Random();
            int randomStat = -1;
            do {
                randomStat = rand.nextInt(attacker.getStatChanges().size());
            } while(attacker.getStatChanges().get(randomStat) == 6);

            attacker.changeStat(randomStat, 2, true, move.getAddEffect() == 0, defender);
        } else if (effect == 218) {
            // starts to hail - HAIL
            return battle.weather.changeWeather(Weathers.HAIL, attacker.hasItem("ICYROCK"));
        } else if (effect == 219) {
            // change target ability to user ability - ENTRAINMENT
            if(attacker.getAbility() == defender.getAbility()) {
                return false;
            }
            if (defender.hasAbility("TRUANT") || defender.hasAbility("MULTITYPE") || defender.hasAbility("STANCECHANGE") ||
                    defender.hasAbility("SCHOOLING") || defender.hasAbility("COMATOSE") || defender.hasAbility("SHIELDSDOWN") ||
                    defender.hasAbility("DISGUISE") || defender.hasAbility("RKSSYSTEM") || defender.hasAbility("BATTLEBOND") ||
                    defender.hasAbility("POWERCONSTRUCT") || defender.hasAbility("ICEFACE") || defender.hasAbility("GULPMISSILE")) {
                return false;
            }
            if (attacker.hasAbility("FLOWERGIFT") || attacker.hasAbility("ILLUSION") || attacker.hasAbility("NEUTRALIZINGGAS") ||
                    attacker.hasAbility("IMPOSTER") || attacker.hasAbility("ZENMODE") || attacker.hasAbility("HUNGERSWITCH") ||
                    attacker.hasAbility("DISGUISE") || attacker.hasAbility("FORECAST") || attacker.hasAbility("TRACE") ||
                    attacker.hasAbility("POWERCONSTRUCT") || attacker.hasAbility("ICEFACE") || attacker.hasAbility("GULPMISSILE") ||
                    attacker.hasAbility("POWEROFALCHEMY") || attacker.hasAbility("RECEIVER")) {
                return false;
            }
            defender.changeAbility(attacker.getAbility().getInternalName());
        } else if (effect == 220) {
            // increases a lot the Attack, Special Attack and Speed, but decreases Defense and Special Defense of user - SHELL SMASH
            attacker.changeStat(1, -1, true, move.getAddEffect() == 0, defender);
            attacker.changeStat(3, -1, true, move.getAddEffect() == 0, defender);
            attacker.changeStat(0, 2, true, move.getAddEffect() == 0, defender);
            attacker.changeStat(2, 2, true, move.getAddEffect() == 0, defender);
            attacker.changeStat(4, 2, true, move.getAddEffect() == 0, defender);
        } else if (effect == 221) {
            // if the user faints in this turn, the defender faints also - DESTINY BOND
            if(attacker.destinyBondTurns > 0) {
                attacker.destinyBondTurns = 0;
                attacker.effectMoves.set(53, 0);
                return false;
            }
            attacker.effectMoves.set(53, 1);
            System.out.println(attacker.nickname + " is trying to take its rival with it!");
            attacker.destinyBondTurns++;
        } else if (effect == 222) {
            // reduce HP every turn if target is asleep - NIGHTMARE
            if(defender.effectMoves.get(54) > 0 || !defender.hasStatus(Status.ASLEEP)) {
                return false;
            }
            defender.effectMoves.set(54, 1);
            System.out.println(defender.nickname + " is suffering a nightmare!");
        } else if (effect == 223) {
            // inflicts a variable damage - PSYWAVE
            double rand = 0.5 + battle.random.nextDouble();
            int dmg = (int) (attacker.getLevel()*rand);
            if(battle.resistsWith1HP(defender, attacker, dmg, move)) {
                dmg -= 1;
            }
            defender.reduceHP(dmg);
        } else if (effect == 225) {
            // share the sum of Attack and Special Attack between user and target - POWER SPLIT
            int attacks = (attacker.getStats().get(1) + defender.getStats().get(1))/2;
            int specialAtt = (attacker.getStats().get(3) + defender.getStats().get(3))/2;
            attacker.setStatValue(1, attacks);
            attacker.setStatValue(3, specialAtt);
            defender.setStatValue(1, attacks);
            defender.setStatValue(3, specialAtt);
            System.out.println("The attacks of " + attacker.nickname + " and " + defender.nickname + " were shared between them!");
        } else if (effect == 226) {
            // decreases a lot Special Attack of rival - EERIE IMPULSE
            defender.changeStat(2, -2, false, move.getAddEffect() == 0, attacker);
        } else if (effect == 227) {
            // increases Special Attack of user - CHARGE BEAM
            attacker.changeStat(2, 1, true, move.getAddEffect() == 0, defender);
        } else if (effect == 228) {
            // increases Defense and Special Defense of allies if has Plus and Minus ability - MAGNETIC FLUX
            System.out.println("There is a magnetic flux in the team!");
            if(defender.hasAbility("PLUS") || defender.hasAbility("MINUS")) {
                attacker.changeStat(1, 1, true, move.getAddEffect() == 0, defender);
                attacker.changeStat(3, 1, true, move.getAddEffect() == 0, defender);
            }
        } else if (effect == 230) {
            // sum current HP of rival and user and share equally - PAIN SPLIT
            System.out.println("HP of " + attacker.nickname + " and " + defender.nickname + " were equally shared!");
            int totalHP = attacker.getPsActuales() + defender.getPsActuales();
            attacker.setHP(totalHP/2);
            defender.setHP(totalHP/2);
        } else if (effect == 231) {
            // returns the 150% of damage - METAL BURST
            if (attacker.previousDamage > 0 && attacker.lastMoveInThisTurn != null) {
                int dmg = (int)(attacker.previousDamage*1.5);
                if(battle.resistsWith1HP(defender, attacker, dmg, move)) {
                    dmg -= 1;
                }
                defender.reduceHP(dmg);
            } else {
                return false;
            }
        } else if (effect == 232) {
            // turns user to another type depends on the environment - CAMOUFLAGE
            //TODO: turns user to another type depends on the environment
            if (battle.terrain.hasTerrain(TerrainTypes.GRASSY)) {
                attacker.changeType("GRASS","");
                System.out.println("Type of " + attacker.nickname + " changed to Grass!");
            } else if (battle.terrain.hasTerrain(TerrainTypes.ELECTRIC)) {
                attacker.changeType("ELECTRIC","");
                System.out.println("Type of " + attacker.nickname + " changed to Electric!");
            } else if (battle.terrain.hasTerrain(TerrainTypes.MISTY)) {
                attacker.changeType("FAIRY","");
                System.out.println("Type of " + attacker.nickname + " changed to Fairy!");
            } else if (battle.terrain.hasTerrain(TerrainTypes.PSYCHIC)) {
                attacker.changeType("PSYCHIC","");
                System.out.println("Type of " + attacker.nickname + " changed to Psychic!");
            }
        } else if (effect == 233) {
            // use the last move used in battle - COPYCAT
            if (battle.lastMoveUsed != null) {
                if (battle.lastMoveUsed.getFlags().contains("f") || battle.lastMoveUsed.getCode() == 59) { // forbidden moves
                    return false;
                } else {
                    battle.useMove(attacker, defender, battle.lastMoveUsed, defenderMove, false, false, true);
                }
            } else {
                return false;
            }
        } else if (effect == 234) {
            // creates a substitute that receives the damage - SUBSTITUTE
            if(attacker.getPercentHP() <= 25 || attacker.effectMoves.get(56) > 0 || attacker.getPsActuales() == 1) return false;
            attacker.effectMoves.set(56, 1);
            System.out.println(attacker.nickname + " created a substitute!");
            attacker.substitute = attacker.getHP()/4;
        } else if (effect == 235) {
            // recovers the user's item - RECYCLE
            if(attacker.item != null || attacker.originalItem == null) return false;
            System.out.println(attacker.nickname + " recovers " + attacker.originalItem.name + "!");
            attacker.giveItem(attacker.originalItem.getInternalName(),false);
        } else if (effect == 236) {
            // cancels equipped item effects for 5 turns - MAGIC ROOM
            if(battle.effectFieldMoves.get(6) == 0) {
                battle.effectFieldMoves.set(6, 1);
                System.out.println(move.name + " is activated!");
            } else {
                battle.effectFieldMoves.set(6, 0);
                System.out.println(move.name + " is disabled!");
            }
        } else if (effect == 237) {
            // absorb HP to enemy and recovers 75% of the damage - DRAINING KISS
            if (!defender.hasAbility("LIQUIDOOZE")) {
                attacker.healHP((int) (damage * 0.75), true, false, true, true);
            } else {
                attacker.reduceHP((int) (damage * 0.75));
            }
        } else if (effect == 238) {
            // starts to sunlight - SUNNY DAY
            return battle.weather.changeWeather(Weathers.SUNLIGHT, attacker.hasItem("HEATROCK"));
        } else if (effect == 240) {
            // makes user repose in the next turn - GIGA IMPACT, HYPER BEAM, ROCK WRECKER...
            attacker.effectMoves.set(57, 1);
        } else if (effect == 241) {
            // freezes the target - ICE BEAM, BLIZZARD, ICE PUNCH, POWDER SNOW, FREEZE DRY...
            if (defender.canFreeze(false, attacker)) {
                defender.causeStatus(Status.FROZEN, attacker, false);
            } else {
                return false;
            }
        } else if (effect == 242) {
            // transforms in target - TRANSFORM
            // TODO: transform effect
            return false;
        } else if (effect == 243) {
            // change user type to its first attack's type - CONVERSION
            if(attacker.getMoves().get(0).getMove().type.equals(attacker.battleType1) && attacker.battleType2 == null) return false;
            attacker.changeType(attacker.getMoves().get(0).getMove().type.getInternalName(),"");
            System.out.println(attacker.nickname + " changed to " + attacker.getMoves().get(0).getMove().type.name + "-Type!");
        } else if (effect == 244) {
            // change user type to a resistance to last rival move - CONVERSION2
            Random rand = new Random();
            if(defender.previousMove == null) return false;

            String type = defender.previousMove.type.resistAndInmun().get(rand.nextInt(defender.previousMove.type.resistAndInmun().size()));
            attacker.changeType(type,"");
            System.out.println(defender.nickname + " changed to " + type + "-Type!");
        } else if (effect == 245) {
            // return the status move used in this turn - MAGIC COAT
            attacker.effectMoves.set(58, 1);
            System.out.println(attacker.nickname + " is protected by " + move.name + "!");
        } else if (effect == 246) {
            // the next turn will be a critical hit - LASER FOCUS
            attacker.effectMoves.set(60, 1);
            System.out.println(attacker.nickname + " is focusing!");
        } else if (effect == 249) {
            // impedes that target uses a move two turns consecutive - TORMENT
            // TODO: torment effect
            return false;
        } else if (effect == 251) {
            // infatuates target - ATTRACT
            if (defender.canInfatuate(false, attacker)) {
                defender.causeTemporalStatus(TemporalStatus.INFATUATED, attacker);
            } else {
                return false;
            }
        } else if (effect == 252) {
            // makes the target move the last in the turn - QUASH
            // TODO: quash effect
            return false;
        } else if (effect == 253) {
            // impedes that target uses items for 5 turns - EMBARGO
            if(defender.effectMoves.get(62) > 0) {
                return false;
            }
            defender.effectMoves.set(62, 1);
            System.out.println(defender.nickname + " can't use items!");
        } else if (effect == 254) {
            // increase team defense and special defense - AURORA VEIL
            if(attacker.getTeam().effectTeamMoves.get(18) > 0) {
                return false;
            }
            attacker.getTeam().effectTeamMoves.set(18, 1);
            System.out.println(attacker.nickname + " team has a " + move.name);
        } else if (effect == 256) {
            // the slowest Pokémon will be the first to attack - TRICK ROOM
            if(battle.effectFieldMoves.get(7) == 0) {
                battle.effectFieldMoves.set(7, 1);
            } else {
                battle.effectFieldMoves.set(7, 0);
            }
            System.out.println("The slowest Pokémon will be the first to attack!");
        } else if (effect == 258) {
            // decreases target speed and poison it - TOXIC THREAD
            boolean ef1 = defender.changeStat(4, -1, false, move.getAddEffect() == 0, attacker);
            if(defender.canPoison(false,attacker)) {
                defender.causeStatus(Status.POISONED, attacker, false);
            } else return ef1;
        } else if (effect == 259) {
            // turns Normal Type attacks to Electric Type during this turn - ION DELUGE
            if(battle.effectFieldMoves.get(8) > 0) return false;
            battle.effectFieldMoves.set(8,1);
            System.out.println(attacker.nickname + " creates a " + move.name);
        } else if (effect == 260) {
            // increase +3 user defense - COTTON GUARD...
            attacker.changeStat(1, 3, true, move.getAddEffect() == 0, defender);
        } else if (effect == 261) {
            // decreases target attack and special attack - TEARFUL-LOOK, NOBLE ROAR...
            defender.changeStat(0, 1, false, move.getAddEffect() == 0, attacker);
            defender.changeStat(2, 1, false, move.getAddEffect() == 0, attacker);
        } else if (effect == 262) {
            // increase Defense of Grass Pokemon - FLOWER SHIELD
            int nGrass = 0;
            if(attacker.hasType("GRASS") && attacker.effectMoves.get(36) == 0 && attacker.effectMoves.get(44) == 0 &&
                    attacker.effectMoves.get(59) == 0 && attacker.effectMoves.get(46) == 0) {
                attacker.changeStat(1, 1, false, move.getAddEffect() == 0, defender);
                nGrass++;
            }
            if(defender.hasType("GRASS") && defender.effectMoves.get(36) == 0 && defender.effectMoves.get(44) == 0 &&
                    defender.effectMoves.get(59) == 0 && defender.effectMoves.get(46) == 0) {
                defender.changeStat(1, 1, false, move.getAddEffect() == 0, attacker);
                nGrass++;
            }
            return nGrass != 0;
        } else if (effect == 263) {
            // impedes sound moves - THROAT CHOP
            defender.effectMoves.set(63, 1);
        } else if (effect == 264) {
            // burns the equipped berry/gem of target - INCINERATE
            if(defender.hasItemWithFlag("c") || defender.hasItemWithFlag("g")) {
                System.out.println(attacker.nickname + " burns " + defender.item.name + " of " + defender.nickname + "!");
                defender.loseItem(false,false);
            }
        } else if (effect == 265) {
            // copies permanently the last move used by target - SKETCH
            // TODO: SKETCH effect
            return false;
        } else if (effect == 267) {
            // impedes target to heal - HEAL BLOCK
            defender.effectMoves.set(64, 1);
            System.out.println(defender.nickname + " can't heal now!");
        } else if (effect == 268) {
            // increase +3 user special attack - TAIL GLOW...
            attacker.changeStat(2, 3, true, move.getAddEffect() == 0, defender);
        }
        if (effect == 269) {
            // protect team from status moves - CRAFTY SHIELD
            if (attacker.getTeam().effectTeamMoves.get(19) > 0) {
                return false;
            }
            attacker.getTeam().effectTeamMoves.set(19, 1);
            System.out.println(attacker.nickname + "'s team was protected by " + move.name);
        } else if (effect == 270) {
            // switch target and user stat changes - HEART SWAP
            System.out.println(attacker.nickname + " switched stats changes with " + defender.nickname + "!");
            for(int i=0;i<attacker.getStatChanges().size();i++) {
                int auxAttackerStat = attacker.getStatChanges().get(i);
                attacker.setStatChanges(i, defender.getStatChanges().get(i));
                defender.setStatChanges(i, auxAttackerStat);
            }
        } else if (effect == 272) {
            // decrease user defense, special defense and speed - V-CREATE
            attacker.changeStat(1, -1, true, move.getAddEffect() == 0, defender);
            attacker.changeStat(3, -1, true, move.getAddEffect() == 0, defender);
            attacker.changeStat(4, -1, true, move.getAddEffect() == 0, defender);
        } else if (effect == 273) {
            // protect team from non-status moves in the first turn - MAT BLOCK
            if (attacker.getTeam().effectTeamMoves.get(21) > 0) {
                return false;
            }
            attacker.getTeam().effectTeamMoves.set(21, 1);
            System.out.println(attacker.nickname + "'s team was protected by " + move.name);
        }

        return true;
    }
}
