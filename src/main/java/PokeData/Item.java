package PokeData;

import java.util.Random;

enum Pocket {
    ITEMS,
    MEDICINE,
    POKEBALLS,
    TMS,
    BERRIES,
    MAIL,
    BATTLEITEMS,
    KEYITEMS
}
enum FieldUse {
    NOFIELDUSE,
    ONPOKEMON,
    DIRECT,
    TM,
    HM
}
enum BattleUse {
    NOBATTLEUSE,
    INPOKEMON,
    ONMOVE,
    ONBATTLER,
    ONFOE,
    NOTARGET
}

public class Item {
    String internalName;
    public String name, description;
    Pocket pocket;
    double price;
    FieldUse fieldUse;
    BattleUse battleUse;
    boolean consumable;
    /*
    FLAGS ->
     a: is a mail item
     b: is a poke ball item
     c: is a berry item
     d: is a key item
     e: is an evolution stone
     f: is a fossil
     g: is an elemental power-raising gem
     h: is mulch to plant berries
     i: is a repel item
     j: is a mega stone
     k: is a mega ring
     l: is a plate
     m: is a memory
    */
    String flags;
    Movement move; // only for TMs/HMs

    public Item(String internalName, String name, Pocket pocket, double price, FieldUse fieldUse, BattleUse battleUse, boolean consumable,
                String flags, String description, Movement move) {
        this.internalName = internalName;
        this.name = name;
        this.description = description;
        this.pocket = pocket;
        this.price = price;
        this.fieldUse = fieldUse;
        this.battleUse = battleUse;
        this.consumable = consumable;
        this.flags = flags;
        this.move = move;
    }

    public String getInternalName() {
        return internalName;
    }

    public Pocket getPocket() {
        return pocket;
    }

    public double getPrice() {
        return price;
    }

    public double getSellPrice() {
        return price/2;
    }

    public FieldUse getFieldUse() {
        return fieldUse;
    }

    public BattleUse getBattleUse() {
        return battleUse;
    }

    public boolean isConsumable() {
        return consumable;
    }

    public String getFlags() {
        return flags;
    }

    public Movement getMove() {
        return move;
    }

    public boolean useMedicine(Pokemon target) {
        // recover HP
        if(hasName("POTION") || hasName("SWEETHEART") || hasName("BERRYJUICE")) {
            return target.healHP(20,true,true,false);
        } else if(hasName("FRESHWATER")) {
            return target.healHP(30,true,true,false);
        } else if(hasName("SODAPOP") || hasName("ENERGYPOWDER")) {
            boolean works = target.healHP(50,true,true,false);
            if(works) {
                if(hasName("ENERGYPOWDER")) target.modifyHappiness(-5,-5,-10);
                return true;
            }
        } else if(hasName("SUPERPOTION")) {
            return target.healHP(60,true,true,false);
        } else if(hasName("LEMONADE")) {
            return target.healHP(70,true,true,false);
        } else if(hasName("MOOMOOMILK")) {
            return target.healHP(100,true,true,false);
        } else if(hasName("HYPERPOTION") || hasName("ENERGYROOT")) {
            boolean works = target.healHP(120,true,true,false);
            if(works) {
                if(hasName("ENERGYROOT")) target.modifyHappiness(-10,-10,-15);
                return true;
            }
        } else if(hasName("MAXPOTION")) {
            return target.healHP(-1,true,true,false);
        } else if(hasName("FULLRESTORE")) {
            if(target.hasAllHP() && !target.hasSomeStatus() && !target.hasTemporalStatus(TemporalStatus.CONFUSED)) {
                return false;
            }
            target.healHP(-1,true,false,false);
            target.healPermanentStatus();
            target.healTempStatus(TemporalStatus.CONFUSED, true);
            return true;
        }
        // restore status
        if((hasName("PARLYZHEAL") && target.hasStatus(Status.PARALYZED)) ||
                (hasName("AWAKENING") && target.hasStatus(Status.ASLEEP)) ||
                (hasName("ANTIDOTE") && (target.hasStatus(Status.POISONED) || target.hasStatus(Status.BADLYPOISONED))) ||
                (hasName("BURNHEAL") && target.hasStatus(Status.BURNED)) ||
                (hasName("ICEHEAL") && target.hasStatus(Status.FROZEN))) {
            target.healPermanentStatus();
            return true;
        } else if(hasName("FULLHEAL") || hasName("RAGECANDYBAR") || hasName("LAVACOOKIE") || hasName("OLDGATEAU")
                || hasName("CASTELIACONE") || hasName("LUMIOSEGALETTE") || hasName("SHALOURSABLE") || hasName("BIGMALASADA")
                || hasName("PEWTERCRUNCHIES") || hasName("HEALPOWDER")) {
            if(!target.hasSomeStatus() && !target.hasTemporalStatus(TemporalStatus.CONFUSED)) {
                return false;
            }
            target.healPermanentStatus();
            target.healTempStatus(TemporalStatus.CONFUSED, true);
            if(hasName("HEALPOWDER")) {
                target.modifyHappiness(-5,-5,-10);
            }
            return true;
        }
        // revive
        if(target.isFainted()) {
            if(hasName("REVIVE")) {
                target.revivePokemon(target.getHP()/2);
                return true;
            } else if(hasName("MAXREVIVE") || hasName("REVIVALHERB")) {
                target.revivePokemon(-1);
                if(hasName("REVIVALHERB")) {
                    target.modifyHappiness(-15,-15,-20);
                }
                return true;
            }
        }
        // restore PP
        if(target.moveWithoutPP() != null) {
            if(hasName("ELIXIR")) {
                target.healPP(-1, 10);
                return true;
            } else if(hasName("MAXELIXIR")) {
                target.healPP(-1, -1);
                return true;
            }
        }
        if(battleUse.equals(BattleUse.ONMOVE)) {
            System.out.println("Select a move: ");
            int pps = 10;
            Movement move = target.selectMove();
            if(target.remainPPOf(move) >= move.getPP()) {
                return false;
            }
            if(hasName("MAXETHER")) {
                pps = -1;
            }
            target.healPP(target.getIndexMove(move.getInternalName()), pps);
            return true;
        }
        // vitamins
        if(hasName("HPUP")) {
            return target.gainEVS(0, 10, false);
        } else if(hasName("PROTEIN")) {
            return target.gainEVS(1, 10, false);
        } else if(hasName("IRON")) {
            return target.gainEVS(2, 10, false);
        } else if(hasName("CALCIUM")) {
            return target.gainEVS(3, 10, false);
        } else if(hasName("ZINC")) {
            return target.gainEVS(4, 10, false);
        } else if(hasName("CARBOS")) {
            return target.gainEVS(5, 10, false);
        } else if(hasName("RARECANDY")) {
            return target.raiseLevel(true);
        }
        if(hasName("PPUP") || hasName("PPMAX")) {
            System.out.println("Select a move: ");
            double incr = 1.2;
            Movement move = target.selectMove();
            if (hasName("PPMAX")) {
                incr = 1.6;
            }
            return target.increaseMaxPP(move,incr);
        }

        return false;
    }

    public boolean hasName(String n) {
        return internalName.equals(n);
    }

    public boolean nameContains(String n) {
        return internalName.contains(n);
    }
}
