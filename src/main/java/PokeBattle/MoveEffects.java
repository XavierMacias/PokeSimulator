package PokeBattle;

import PokeData.Movement;
import PokeData.Pokemon;
import PokeData.Status;
import PokeData.TemporalStatus;

public class MoveEffects {

    Battle battle;

    public MoveEffects(Battle battle) {
        this.battle = battle;
    }

    private boolean canPoison(Pokemon target, Pokemon other) {
        //TODO: conditions for poison
        if(target.hasType("POISON") || target.hasType("STEEL")) {
            return false;
        }
        if(!target.getStatus().equals(Status.FINE)) {
            return false;
        }
        return true;
    }

    private boolean canBurn(Pokemon target, Pokemon other) {
        //TODO: conditions for burn
        if(target.hasType("FIRE")) {
            return false;
        }
        if(!target.getStatus().equals(Status.FINE)) {
            return false;
        }
        return true;
    }

    private boolean canSleep(Pokemon target, Pokemon other) {
        //TODO: conditions for sleep
        if(!target.getStatus().equals(Status.FINE)) {
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

    public boolean moveEffects(Movement move, Pokemon attacker, Pokemon defender, Movement defenderMove, int damage) {
        int effect = move.getCode();
        if(effect == 1) {
            // decreases target attack - GROWL
            defender.changeStat(0,-1);
        }
        else if(effect == 2) {
            // seed the target - LEECH SEED
            if(canSeed(defender, attacker)) {
                defender.causeTemporalStatus(TemporalStatus.SEEDED);
            } else {
                return false;
            }
        }
        else if(effect == 3) {
            // poisons the target - POISON STING, SLUDGE BOMB, POISON POWDER
            if(canPoison(defender, attacker)) {
                defender.causeStatus(Status.POISONED);
            } else {
                return false;
            }
        }
        else if(effect == 4) {
            // sleeps the target - SLEEP POWDER, HYPNOSIS
            if(canSleep(defender, attacker)) {
                defender.causeStatus(Status.ASLEEP);
            } else {
                return false;
            }
        }
        else if(effect == 5) {
            // recoil damage - TAKE DOWN
            attacker.reduceHP(damage/4);
        }
        else if(effect == 6) {
            // decreases target evasion - SWEET SCENT
            return defender.changeStat(6,-1);
        }
        else if(effect == 7) {
            // increases user attack and special attack - GROWTH
            //TODO: weather increases more
            attacker.changeStat(0,1);
            attacker.changeStat(2,1);
        }
        else if(effect == 8) {
            // more recoil damage - DOUBLE EDGE, BRAVE BIRD, FLARE BLITZ
            attacker.reduceHP(damage/3);
            if(0.1 >= Math.random() && canBurn(defender, attacker) && move.getInternalName().equals("FLAREBLITZ")) {
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
        else if(effect == 21) {
            // burns the target - EMBER, FLAME WHEEL, FLAMETHROWER...
            if(canBurn(defender, attacker)) {
                defender.causeStatus(Status.BURNED);
            } else {
                return false;
            }
        }
        else if(effect == 22) {
            // decreases target accuracy - SMOKE SCREEN, SAND ATTACK
            defender.changeStat(5,-1);
        }
        else if(effect == 23) {
            // fix damage to 40 PS - DRAGON RAGE
            defender.reduceHP(40);
        }
        if(effect == 24) {
            // decreases a lot target speed - SCARY FACE, STRING SHOT
            defender.changeStat(4,-2);
        }
        else if(effect == 25) {
            // flinched target - BITE, HYPER FANG, AIR SLASH, TWISTER, FAKE OUT...
            if(canFlinch(defender, attacker)) {
                defender.causeTemporalStatus(TemporalStatus.FLINCHED);
            } else {
                return false;
            }
        } else if(effect == 29) {
            // increase all stats - ANCIENT POWER, SILVER WIND
            attacker.changeStat(0,1);
            attacker.changeStat(1,1);
            attacker.changeStat(2,1);
            attacker.changeStat(3,1);
            attacker.changeStat(4,1);
        } else if(effect == 86) {
            // increase a lot of user attack - SWORDS DANCE
            attacker.changeStat(0,2);
        }

        return true;
    }
}
