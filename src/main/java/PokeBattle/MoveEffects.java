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
        if(target.hasType("POISON") || target.hasType("STEEL")) {
            return false;
        }
        if(!target.getStatus().equals(Status.FINE)) {
            return false;
        }
        return true;
    }

    private boolean canBurn(Pokemon target, Pokemon other) {
        if(target.hasType("FIRE")) {
            return false;
        }
        if(!target.getStatus().equals(Status.FINE)) {
            return false;
        }
        return true;
    }

    private boolean canSleep(Pokemon target, Pokemon other) {
        if(!target.getStatus().equals(Status.FINE)) {
            return false;
        }
        return true;
    }

    private boolean canSeed(Pokemon target, Pokemon other) {
        if(target.hasType("GRASS") || target.hasTemporalStatus(TemporalStatus.SEEDED)) {
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
            defender.changeStat(6,-1);
        }
        else if(effect == 7) {
            // increases user attack and special attack - GROWTH
            attacker.changeStat(0,1);
            attacker.changeStat(2,1);
        }
        else if(effect == 8) {
            // more recoil damage - DOUBLE EDGE, BRAVE BIRD
            // TODO: if flare blitz, can also burn
            attacker.reduceHP(damage/3);
        }
        else if(effect == 9) {
            // change ability to Insomnia - WORRY SEED
        }
        else if(effect == 10) {
            // recover HP depending on the weather - SYNTHESIS
            attacker.healHP(attacker.getHP()/4, true);
        }
        else if(effect == 21) {
            // burns the target - EMBER, FLAME WHEEL, FLAMETHROWER...
            if(canBurn(defender, attacker)) {
                defender.causeStatus(Status.BURNED);
            } else {
                return false;
            }
        }
        if(effect == 22) {
            // decreases target accuracy - SMOKE SCREEN, SAND ATTACK
            defender.changeStat(5,-1);
        }

        return true;
    }
}
