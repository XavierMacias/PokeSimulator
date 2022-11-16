package PokeData;

import java.util.ArrayList;

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
    private Type originalType;
    private int originalPriority;
    private int originalPower;
    private ArrayList<String> compatibleTM;

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
        originalType = type;
        originalPriority = priority;
        originalPower = power;
    }

    public String getInternalName() { return internalName; }

    public boolean hasName(String n) {
        return internalName.equals(n);
    }

    public int getPower() {
        return power;
    }
    public void setPower(int p) {
        power = p;
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
    public void setPriority(int pr) {
        priority = pr;
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

    public boolean targetIsEnemy() {
        return target.equals(Target.FOE) || target.equals(Target.RANDOMFOE) || target.equals(Target.ALLFOES) || target.equals(Target.ALLBATTLERS);
    }

    public boolean multiTarget() {
        return target.equals(Target.ALLFOES) || target.equals(Target.ALLBATTLERS) || target.equals(Target.ALLALLIES) || target.equals(Target.USERANDALLIES);
    }

    public Category getCategory() {
        return category;
    }

    public String getFlags() {
        return flags;
    }
    public void changeType(Type t) {
        type = t;
    }
    public void recoverType() {
        type = originalType;
        priority = originalPriority;
        power = originalPower;
    }
    public boolean typeIsChanged() {
        return !type.equals(originalType);
    }
    public void setCompatibleTM(ArrayList<String> ctm) { compatibleTM = ctm; }
    public boolean isCompatible(String poke) {
        if(compatibleTM == null) return false;
        return compatibleTM.contains(poke);
    }
}
