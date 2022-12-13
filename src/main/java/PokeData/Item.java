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
        if(hasName("POTION") || hasName("SWEETHEART") || hasName("BERRYJUICE") || hasName("ORANBERRY")) {
            if(hasName("ORANBERRY") && target.hasAbility("RIPEN")) {
                return target.healHP(40,true,true,false, false);
            } else {
                return target.healHP(20,true,true,false, false);
            }
        } else if(hasName("FRESHWATER")) {
            return target.healHP(30,true,true,false, false);
        } else if(hasName("SODAPOP") || hasName("ENERGYPOWDER")) {
            boolean works = target.healHP(50,true,true,false, false);
            if(works) {
                if(hasName("ENERGYPOWDER")) target.modifyHappiness(-5,-5,-10);
                return true;
            }
        } else if(hasName("SUPERPOTION")) {
            return target.healHP(60,true,true,false, false);
        } else if(hasName("LEMONADE")) {
            return target.healHP(70,true,true,false, false);
        } else if(hasName("MOOMOOMILK")) {
            return target.healHP(100,true,true,false, false);
        } else if(hasName("HYPERPOTION") || hasName("ENERGYROOT")) {
            boolean works = target.healHP(120,true,true,false, false);
            if(works) {
                if(hasName("ENERGYROOT")) target.modifyHappiness(-10,-10,-15);
                return true;
            }
        } else if(hasName("MAXPOTION")) {
            return target.healHP(-1,true,true,false, false);
        } else if(hasName("FULLRESTORE")) {
            if(target.isFainted() || (target.hasAllHP() && !target.hasSomeStatus() && !target.hasTemporalStatus(TemporalStatus.CONFUSED))) {
                return false;
            }
            target.healHP(-1,true,false,false, false);
            target.healPermanentStatus();
            target.healTempStatus(TemporalStatus.CONFUSED, true);
            return true;
        } else if(hasName("SITRUSBERRY")) {
            int hp = target.getHP()/4;
            if(target.hasAbility("RIPEN")) {
                hp *= 2;
            }
            return target.healHP(hp,true,true,false, false);
        }
        // restore status
        if(((hasName("PARLYZHEAL") || hasName("CHERIBERRY")) && target.hasStatus(Status.PARALYZED)) ||
                ((hasName("AWAKENING") || hasName("CHESTOBERRY")) && target.hasStatus(Status.ASLEEP)) ||
                ((hasName("ANTIDOTE") || hasName("PECHABERRY")) && ((target.hasStatus(Status.POISONED)) || target.hasStatus(Status.BADLYPOISONED))) ||
                ((hasName("BURNHEAL") || hasName("RAWSTBERRY")) && target.hasStatus(Status.BURNED)) ||
                ((hasName("ICEHEAL") || hasName("ASPEARBERRY")) && target.hasStatus(Status.FROZEN))) {
            if(target.isFainted()) {
                return false;
            }
            target.healPermanentStatus();
            return true;
        } else if(hasName("FULLHEAL") || hasName("RAGECANDYBAR") || hasName("LAVACOOKIE") || hasName("OLDGATEAU")
                || hasName("CASTELIACONE") || hasName("LUMIOSEGALETTE") || hasName("SHALOURSABLE") || hasName("BIGMALASADA")
                || hasName("PEWTERCRUNCHIES") || hasName("HEALPOWDER") || hasName("LUMBERRY")) {
            if(!target.hasSomeStatus() && !target.hasTemporalStatus(TemporalStatus.CONFUSED)) {
                return false;
            }
            if(target.isFainted()) {
                return false;
            }
            target.healPermanentStatus();
            target.healTempStatus(TemporalStatus.CONFUSED, true);
            if(hasName("HEALPOWDER")) {
                target.modifyHappiness(-5,-5,-10);
            }
            return true;
        }
        if(hasName("PERSIMBERRY") && target.hasTemporalStatus(TemporalStatus.CONFUSED)) {
            target.healTempStatus(TemporalStatus.CONFUSED, true); // heal confusion
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
        // berries
        int h = target.getHP()/3;
        if(target.hasAbility("RIPEN")) {
            h *= 2;
        }
        if(hasName("FIGYBERRY")) {
            if(!target.healHP(h,true,true,false, false)) {
                return false;
            }
            if(target.canConfuse(false,null) && (target.getNature("MODEST") || target.getNature("TIMID") || target.getNature("CALM") || target.getNature("BOLD"))) {
                target.causeTemporalStatus(TemporalStatus.CONFUSED,null);
            }
            return true;
        }
        if(hasName("WIKIBERRY")) {
            if(!target.healHP(h,true,true,false, false)) {
                return false;
            }
            if(target.canConfuse(false,null) && (target.getNature("ADAMANT") || target.getNature("IMPISH") || target.getNature("CAREFUL") || target.getNature("JOLLY"))) {
                target.causeTemporalStatus(TemporalStatus.CONFUSED,null);
            }
            return true;
        }
        if(hasName("MAGOBERRY")) {
            if(!target.healHP(h,true,true,false, false)) {
                return false;
            }
            if(target.canConfuse(false,null) && (target.getNature("BRAVE") || target.getNature("RELAXED") || target.getNature("QUIET") || target.getNature("SASSY"))) {
                target.causeTemporalStatus(TemporalStatus.CONFUSED,null);
            }
            return true;
        }
        if(hasName("AGUAVBERRY")) {
            if(!target.healHP(h,true,true,false, false)) {
                return false;
            }
            if(target.canConfuse(false,null) && (target.getNature("NAUGHTY") || target.getNature("LAX") || target.getNature("RASH") || target.getNature("NAIVE"))) {
                target.causeTemporalStatus(TemporalStatus.CONFUSED,null);
            }
            return true;
        }
        if(hasName("IAPAPABERRY")) {
            if(!target.healHP(h,true,true,false, false)) {
                return false;
            }
            if(target.canConfuse(false,null) && (target.getNature("LONELY") || target.getNature("MILD") || target.getNature("GENTLE") || target.getNature("HASTY"))) {
                target.causeTemporalStatus(TemporalStatus.CONFUSED,null);
            }
            return true;
        }
        if(hasName("POMEGBERRY") || hasName("KELPSYBERRY") || hasName("QUALOTBERRY") || hasName("HONDEWBERRY")
                || hasName("GREPABERRY") || hasName("TAMATOBERRY")) {
            int i = 0;
            if(hasName("KELPSYBERRY")) i = 1;
            if(hasName("QUALOTBERRY")) i = 2;
            if(hasName("HONDEWBERRY")) i = 3;
            if(hasName("GREPABERRY")) i = 4;
            if(hasName("TAMATOBERRY")) i = 5;

            if(target.getEVs(i) == 0) {
                int ev = target.getEVs(i)-10;
                if(ev < 0) ev = 0;
                if(target.happiness == 255) {
                    System.out.println("This doesn't have any effect.");
                    return false;
                }
                target.setEVs(i, ev);
                target.modifyHappiness(10,5,1);
                System.out.println(target.nickname + " is now more friendly.");
                return true;
            }
            System.out.println("This doesn't have any effect.");
            return false;
        }

        return false;
    }

    public boolean useBattleItem(Pokemon target) {
        if(target.effectMoves.get(62) > 0) {
            System.out.println("Can't use items in " + target.nickname);
            return false;
        }
        // flutes
        if(hasName("YELLOWFLUTE") && target.hasTemporalStatus(TemporalStatus.CONFUSED)) {
            target.healTempStatus(TemporalStatus.CONFUSED, true); // heal confusion
            return true;
        }
        if(hasName("REDFLUTE") && target.hasTemporalStatus(TemporalStatus.INFATUATED)) {
            target.healTempStatus(TemporalStatus.INFATUATED, true); // heal infatuate
            return true;
        }
        if(hasName("BLUEFLUTE") && target.hasStatus(Status.ASLEEP)) {
            target.healPermanentStatus();
            return true;
        }
        // X items
        if(hasName("XATTACK")) {
            return target.changeStat(0, 2, false, true, null);
        } else if(hasName("XDEFENSE")) {
            return target.changeStat(1, 2, false, true, null);
        } else if(hasName("XSPATK")) {
            return target.changeStat(2, 2, false, true, null);
        } else if(hasName("XSPDEF")) {
            return target.changeStat(3, 2, false, true, null);
        } else if(hasName("XSPEED")) {
            return target.changeStat(4, 2, false, true, null);
        } else if(hasName("XACCURACY")) {
            return target.changeStat(5, 2, false, true, null);
        } else if(hasName("DIREHIT")) {
            System.out.println(target + " is focusing on battle!");
            if(target.getCriticalIndex() == 4) return false;
            target.criticalIndex += 1;
            if (target.getCriticalIndex() > 4) {
                target.criticalIndex = 4;
            }
            return true;
        } else if(hasName("GUARDSPEC")) {
            System.out.println(target + "'s team is protected by " + name + "!");
            target.getTeam().effectTeamMoves.set(17, 1);
            return true;
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
