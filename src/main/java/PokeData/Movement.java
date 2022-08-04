package PokeData;

public class Movement {
    private String internalName;
    public String name, description;
    public Type type;
    private int power, accuracy, pp, priority, addEffect, code;
    private Target target;
    private Category category;
    /*
    FLAGS ->
     a: is contact move
     b: can use protect to evade this move
     c: can use mirror move to copy that move
     d: affected by magic coat
     e: affected by snatch
     f: affected by kings rock
     g: thaws user when is used
     h: high critical ratio
     i: is a biting move
     j: is a sound based move
     k: is a punching move
     l: is a powder based move
     m: is a pulse based move
     n: is a bomb based move
     o: is a dance based move
     p: cannot use by metronome
     q: affect substitute
    */
    private String flags;

    public Movement(String internalName, String name, Type type, int power, int accuracy, Category category, int pp, int priority,
                    Target target, int addEffect, String flags, int code, String description) {
        this.internalName = internalName;
        this.name = name;
        this.type = type;
        this.power = power;
        this.accuracy = accuracy;
        this.category = category;
        this.pp = pp;
        this.priority = priority;
        this.target = target;
        this.addEffect = addEffect;
        this.flags = flags;
        this.code = code;
        this.description = description;
    }

    public String getInternalName() { return internalName; }

    public boolean hasName(String n) {
        return internalName.equals(n);
    }

    public int getPower() {
        return power;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public int getPP() {
        return pp;
    }

    public int getPriority() {
        return priority;
    }

    public int getAddEffect() {
        return addEffect;
    }

    public int getCode() {
        return code;
    }

    public Target getTarget() {
        return target;
    }

    public Category getCategory() {
        return category;
    }

    public String getFlags() {
        return flags;
    }
}
