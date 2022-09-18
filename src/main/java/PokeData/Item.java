package PokeData;

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
}
