package PokeData;

import PokeBattle.Battle;
import PokeBattle.TerrainTypes;
import PokeBattle.Weathers;

import java.util.*;

enum Natures {
    BASHFUL,
    LONELY,
    ADAMANT,
    NAUGHTY,
    BRAVE,
    BOLD,
    DOCILE,
    IMPISH,
    LAX,
    RELAXED,
    MODEST,
    MILD,
    HARDY,
    RASH,
    QUIET,
    CALM,
    GENTLE,
    CAREFUL,
    QUIRKY,
    SASSY,
    TIMID,
    HASTY,
    JOLLY,
    NAIVE,
    SERIOUS
}

public class Pokemon {
    Specie specie;
    public Utils utils;
    int level, gender, form, psActuales;
    int experience = 0;
    public String nickname;
    private List<Integer> stats, evs, ivs;
    int happiness;
    boolean isShiny = false;
    Natures nature;
    Natures[] natureList = Natures.values();
    private List<Pair<Movement,Integer>> moves;
    private List<Integer> remainPPs;
    public List<Boolean> usedMoves;
    public Type battleType1, battleType2;
    Ability ability, originalAbility;
    List<Integer> statChanges; // attack, defense, sp att, sp def, speed, accuracy, evasion
    Status status;
    List<TemporalStatus> tempStatus;
    public Item item;
    public int criticalIndex = 0;
    boolean participate;
    private Team team;
    // variables for status and moves
    public int pokeTurn = 0;
    public int sleepTurns = 0;
    public int badPoisonTurns = 0;
    public int protectTurns = 0;
    public Movement previousMove;
    public Movement lastMoveInThisTurn;
    public Movement lastMoveReceived;
    public Movement disabledMove, encoreMove, chosenMove;
    public int previousDamage;
    public int bideDamage;
    public List<Integer> effectMoves;
    public int stockpile = 0;
    private Scanner in;
    public Battle battle;

    public Pokemon(Specie specie, int level, Utils utils) {
        Random random = new Random();
        in = new Scanner(System.in);
        this.utils = utils;
        this.specie = specie;
        this.level = level;
        nickname = specie.name;
        happiness = specie.baseHappiness;
        // ability
        ability = specie.ability1;
        if(specie.ability2 != null && Math.random() < 0.5) {
            ability = specie.ability2;
        }
        originalAbility = ability;
        battleType1 = specie.type1;
        battleType2 = specie.type2;

        // gender
        gender = 1; // female
        if((int)(Math.random() * 100) < specie.genderrate) {
            gender = 0; // male
        }
        if(specie.genderrate < 0.0) {
            gender = 2; // no gender
        }
        evs = new ArrayList<Integer>();
        // set EVs (default are 0)
        for(int i=0;i<6;i++) {
            evs.add(0);
        }
        ivs = new ArrayList<Integer>();
        // set random IVs
        for(int i=0;i<6;i++) {
            ivs.add((int) (Math.random() * 32));
        }
        statChanges = new ArrayList<Integer>();
        // set stat changes
        for(int i=0;i<7;i++) {
            statChanges.add(0);
        }
        status = Status.FINE;
        tempStatus = new ArrayList<TemporalStatus>();

        // choose nature
        nature = natureList[random.nextInt(natureList.length)];
        // calculate experience
        experience = calcExperience(level);
        // calculate initial stats
        stats = new ArrayList<Integer>();
        for(int i=0;i<6;i++) {
            stats.add(0);
        }
        calcStats();
        psActuales = stats.get(0);
        // set initial moves
        moves = new ArrayList<Pair<Movement,Integer>>();
        remainPPs = new ArrayList<Integer>();
        usedMoves = new ArrayList<Boolean>();
        setMoves(false, false);
        //is shiny?
        if(utils.getRandomNumberBetween(1,4097) == 1) {
            isShiny = true;
        }

        effectMoves = new ArrayList<Integer>();
        /* 0 -> ingrain
           1 -> encore
           2 -> protect
           3 -> two turn attack (solar beam, skull bash...)
           4 -> fire spin
           5 -> foresight
           6 -> yawn
           7 -> aqua ring
           8 -> rage
           9 -> assurance
           10 -> roost
           11 -> petal dance, outrage...
           12 -> whirlwind
           13 -> uproar
           14 -> focus punch
           15 -> pursuit
           16 -> wrap
           17 -> disable
           18 -> snatch
           19 -> intimidate
           20 -> defense curl
           21 -> sand tomb
           22 -> bide
           23 -> charge
           24 -> fury cutter
           25 -> rollout
           26 -> encore
           27 -> metronome (item)
        */
        for(int i=0;i<28;i++) {
            effectMoves.add(0);
        }
        // alternative forms
        form = 0;
        participate = false;
        battle = null;
        disabledMove = null;
        encoreMove = null;
        chosenMove = null;
    }

    public void setMove(String m) { // ONLY FOR DEBUG
        moves.set(0,new Pair<>(utils.getMove(m),utils.getMove(m).getPP()));
        remainPPs.set(0,utils.getMove(m).getPP());
        usedMoves.set(0,false);
    }
    public void setAbility(String newAbility) { // ONLY FOR DEBUG
        ability = utils.getAbility(newAbility);
        originalAbility = utils.getAbility(newAbility);
    }
    public void setItem(String newItem) { // ONLY FOR DEBUG
        item = utils.getItem(newItem);
    }

    public void setParticipate(boolean participate) {
        this.participate = participate;
    }

    public List<Pair<Movement, Integer>> getMoves() {
        return moves;
    }

    public List<Movement> getMovesWithPP() {
        List<Movement> m = new ArrayList<Movement>();

        for(int i=0;i<moves.size();i++) {
            if(hasPPByIndex(i) > 0 && disabledMove != moves.get(i).getMove() && !(choiceMove() && moves.get(i).getMove() != chosenMove) &&
                    !(hasItem("ASSAULTVEST") && moves.get(i).getMove().getCategory().equals(Category.STATUS) && !moves.get(i).getMove().getInternalName().equals("MEFIRST"))) {
                m.add(moves.get(i).getMove());
            }
        }
        return m;
    }
    public boolean choiceMove() {
        if((hasItem("CHOICEBAND") || hasItem("CHOICESPECS") || hasItem("CHOICESCARF")) && chosenMove != null) {
            return true;
        }
        chosenMove = null;
        return false;
    }
    public Specie getSpecie() { return specie; }
    public Ability getAbility() { return ability; }
    public boolean hasAbility(String ab) {
        if(ability == null) return false;
        return ability.getInternalName().equals(ab); }
    public int getGender() { return gender; }
    public int getCriticalIndex() {
        if((hasItem("SCOPELENS") || hasItem("RAZORCLAW")) && criticalIndex < 4) {
            return criticalIndex + 1;
        }
        if(hasItem("LEEK") && (specieNameIs("FARFETCHD") || specieNameIs("SIRFETCHD"))
            || hasItem("LUCKYPUNCH") && specieNameIs("CHANSEY")) {
            int crit = criticalIndex + 2;
            if(crit > 4) crit = 4;
            return crit;
        }
        return criticalIndex;
    }

    public Team getTeam() { return team; }
    public void setTeam(Team t) { team = t; }

    public List<Integer> getRemainPPs() {
        return remainPPs;
    }

    public List<Integer> getStats() { return stats; }
    public boolean hasItem(String it) {
        if(item == null) {
            return false;
        }
        return item.getInternalName().equals(it);
    }

    public void loseItem(boolean message) {
        if(item != null) {
            if(message) System.out.println(nickname + " consumed " + item.name + "!");
            item = null;
        }
    }
    public List<Integer> getStatChanges() { return statChanges; }

    public int getAttack(boolean critic) {
        int attack = stats.get(1);
        if (!(critic && getStatChange(0) < 1.0)) {
            attack = (int) (stats.get(1) * getStatChange(0));
        }
        if(hasStatus(Status.BURNED) && !hasAbility("GUTS")) {
            attack /= 2.0;
        }
        if((hasAbility("GUTS") && (hasStatus(Status.BURNED) || hasStatus(Status.POISONED) || hasStatus(Status.BADLYPOISONED) ||
                hasStatus(Status.PARALYZED) || hasStatus(Status.ASLEEP))) || hasAbility("HUSTLE")) {
            attack *= 1.5; // GUTS and HUSTLE effect
        }
        if(hasItem("MUSCLEBAND")) { // muscle band
            attack *= 1.1;
        }
        if(hasItem("CHOICEBAND")) { // choice band
            attack *= 1.5;
        }
        // light ball
        if((hasItem("LIGHTBALL") && specieNameIs("PIKACHU")) ||
                (hasItem("THICKCLUB") && (specieNameIs("MAROWAK") || specieNameIs("CUBONE")))) {
            attack *= 2;
        }

        return attack;
    }

    public int getDefense(boolean critic, boolean chipaway) {
        int defense = stats.get(2);
        if ((!(critic && getStatChange(1) > 1.0)) && !chipaway) {
            defense = (int) (stats.get(2)*getStatChange(1));
        }
        if((hasItem("EVIOLITE") && !specie.evos.isEmpty())
                || (hasItem("METALPOWDER") && specieNameIs("DITTO"))) {
            defense *= 1.5;
        }

        return defense;
    }
    public int getSpecialAttack(boolean critic) {
        int spatk = stats.get(3);;
        if (!(critic && getStatChange(2) < 1.0)) {
            spatk = (int) (stats.get(3)*getStatChange(2));
        }
        // solar power
        if(hasAbility("SOLARPOWER") && (battle.weather.hasWeather(Weathers.SUNLIGHT) || battle.weather.hasWeather(Weathers.HEAVYSUNLIGHT))) {
            spatk *= 1.5;
        }
        // deep sea tooth
        if((hasItem("DEEPSEATOOTH") && specieNameIs("CLAMPERL"))
                || (hasItem("LIGHTBALL") && specieNameIs("PIKACHU"))) {
            spatk *= 2;
        }
        if(hasItem("WISEGLASSES")) { // wise glasses
            spatk *= 1.1;
        }
        if(hasItem("CHOICESPECS")) { // choice specs
            spatk *= 1.5;
        }

        return spatk;
    }
    public int getSpecialDefense(boolean critic) {
        int spdef = stats.get(3);
        if (!(critic && getStatChange(3) > 1.0)) {
            spdef = (int) (stats.get(4)*getStatChange(3));
        }
        // rock type with sandstorm
        if(hasType("ROCK") && battle.weather.hasWeather(Weathers.SANDSTORM)) {
            spdef *= 1.5;
        }
        // assault vest and eviolite
        if(hasItem("ASSAULTVEST") || (hasItem("EVIOLITE") && !specie.evos.isEmpty())) {
            spdef *= 1.5;
        }
        // deep sea scale
        if(hasItem("DEEPSEASCALE") && specieNameIs("CLAMPERL")) {
            spdef *= 2;
        }
        return spdef;
    }
    public int getVelocity() {
        int speed = (int) (stats.get(5)*getStatChange(4));
        if(hasStatus(Status.PARALYZED)) {
            speed /= 2.0;
        }
        if(hasItem("IRONBALL") || hasItem("POWERWEIGHT") || hasItem("POWERBRACER") || hasItem("POWERBELT") || hasItem("POWERLENS")
            || hasItem("POWERBAND") || hasItem("POWERANKLET") || hasItem("MACHOBRACE")) {
            speed /= 2.0;
        }
        if(team.effectTeamMoves.get(2) > 0) { // tailwind
            speed *= 2.0;
        }
        if(hasAbility("CHLOROPHYLL") && (battle.weather.hasWeather(Weathers.SUNLIGHT) || battle.weather.hasWeather(Weathers.HEAVYSUNLIGHT))) {
            speed *= 2.0; // chlorophyll
        }
        if(hasAbility("SANDRUSH") && battle.weather.hasWeather(Weathers.SANDSTORM)) {
            speed *= 2.0; // sand rush
        }
        if(hasItem("CHOICESCARF")) { // choice scarf
            speed *= 1.5;
        }
        if(hasItem("QUICKPOWDER") && specieNameIs("DITTO")) {
            speed *= 2.0;
        }
        return speed;
    }
    public int getHP() { return stats.get(0); }

    public double getAccuracy() {
        return getStatChange(5);
    }
    public double getEvasion(boolean chipaway) {
        if(chipaway) {
            return 1.0;
        }
        return getStatChange(6); }

    public int getLevel() {
        return level;
    }

    public int getPsActuales() {
        return psActuales;
    }
    public double getWeight() {
        if(hasItem("FLOATSTONE")) {
            return specie.weight/2;
        }
        return specie.weight;
    }

    public double getPercentHP() {
        return ((double) (psActuales/getHP())*100.0);
    }

    private int totalEvs() {
        int total = 0;
        for(int i=0;i<evs.size();i++) {
            total += evs.get(i);
        }
        return total;
    }

    public boolean hasType(String type) {
        if(battleType1 != null) {
            if(battleType1.getInternalName().equals(type)) {
                return true;
            }
        }

        if(battleType2 != null) {
            if(battleType2.getInternalName().equals(type)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasMove(String move) {
        for(int i=0;i<moves.size();i++) {
            if(moves.get(i).getMove().getInternalName() == move) {
                return true;
            }
        }
        return false;
    }

    public int getIndexMove(String move) {
        for(int i=0;i<moves.size();i++) {
            if(moves.get(i).getMove().getInternalName() == move) {
                return i;
            }
        }
        return -1;
    }

    public boolean specieNameIs(String n) {
        return specie.getInternalName().equals(n);
    }

    public void setMoves(boolean learn, boolean evol) {
        List<Integer> keySet = specie.moveset.keySet().stream().sorted().toList(); // order the moves list by level
        Iterator<Integer> keyIterator = keySet.iterator();
        while (keyIterator.hasNext()) {
            int key = keyIterator.next();
            if((key <= level && !learn) || (key == level && learn) || (key == 0 && evol)) { // check if level is equal or superior
                Iterator iterator = specie.moveset.get(key).iterator();
                while(iterator.hasNext()) {
                    Movement mv = (Movement)iterator.next();
                    if(!hasMove(mv.getInternalName())) { // if the pokemon hasnt that move
                        if(moves.size() < 4) {
                            // add move
                            moves.add(new Pair<>(mv,mv.getPP()));
                            remainPPs.add(mv.getPP());
                            usedMoves.add(false);
                            if(learn || evol) System.out.println(nickname + " learned " + mv.name + "!");
                        } else {
                            // delete move
                            // if the moves are set initially, the deleted move is random
                            if(!learn) {
                                int rand = ((int)(Math.random() * 5));
                                if(rand < 4) {
                                    moves.set(rand, new Pair<>(mv, mv.getPP()));
                                    remainPPs.set(rand, mv.getPP());
                                    usedMoves.set(rand, false);
                                }
                            } else {
                                // if not, is decided by the player
                                System.out.println(nickname + " wants to learn " + mv.name + "\nBut " + nickname + " already known 4 moves");
                                System.out.println("Do you want to forget a move in order to learn " + mv.name + "?");
                                System.out.println("1: Yes\n2: No");
                                if(in.nextLine().equals("1")) {
                                    System.out.println("What move do you want to remove?");
                                    int chosenIndex = -1;
                                    do {
                                        System.out.println("0: Exit");
                                        for(int i=0;i<moves.size();i++) {
                                            System.out.println((i+1)+": "+moves.get(i).getMove().name);
                                        }
                                        chosenIndex = Integer.parseInt(in.nextLine());
                                    } while(chosenIndex < 0 || chosenIndex > getMoves().size());
                                    if(chosenIndex == 0) {
                                        System.out.println(nickname + " didn't learn " + mv.name);
                                    } else {
                                        System.out.println("1, 2, 3... and... Poof!\n" + nickname +" forgot " + moves.get(chosenIndex-1).getMove().name + "!");
                                        moves.set(chosenIndex-1, new Pair<>(mv, mv.getPP()));
                                        remainPPs.set(chosenIndex-1, mv.getPP());
                                        usedMoves.set(chosenIndex-1, false);
                                        System.out.println("And...\n" + nickname +" learned " + moves.get(chosenIndex-1).getMove().name + "!");
                                    }
                                } else {
                                    System.out.println(nickname + " didn't learn " + mv.name);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean isFainted() {
        return status.equals(Status.FAINTED);
    }

    public void reducePP(Movement move, int quantity) {
        int ind = getIndexMove(move.getInternalName());
        if(ind != -1) {
            remainPPs.set(ind,remainPPs.get(ind)-quantity);
            if(remainPPs.get(ind) < 0) {
                remainPPs.set(ind,0);
            }
        }
    }

    public void moveUsedAdded(Movement move) {
        int ind = getIndexMove(move.getInternalName());
        if(ind != -1) {
            usedMoves.set(ind,true);
        }
    }

    public void recover1PP(Movement move) {
        int ind = getIndexMove(move.getInternalName());
        if(ind != -1) {
            remainPPs.set(ind,remainPPs.get(ind)+1);
        }
    }

    public void reduceHP(int damage) {
        if(damage > psActuales || damage == -1) { // -1 take all remain HP
            damage = psActuales;
        }
        psActuales -= damage;
        effectMoves.set(9, 1);
        System.out.println(nickname + " lost " + damage + " HP!");
        if(psActuales <= 0) {
            psActuales = 0;
            status = Status.FAINTED;
            System.out.println(nickname + " fainted!");
            // decrease happiness
            modifyHappiness(-1);
        }
    }

    public void setHP(int hp) {
        psActuales = hp;
    }

    public void causeStatus(Status st) {
        status = st;
        if(st.equals(Status.POISONED)) {
            System.out.println(nickname + " was poisoned!");
        } else if(st.equals(Status.BADLYPOISONED)) {
            System.out.println(nickname + " was badly poisoned!");
            badPoisonTurns = 1;
        } else if(st.equals(Status.PARALYZED)) {
            System.out.println(nickname + " was paralyzed! Maybe is unable to move!");
        } else if(st.equals(Status.ASLEEP)) {
            System.out.println(nickname + " fell sleep!");
            sleepTurns = 1;
        } else if(st.equals(Status.BURNED)) {
            System.out.println(nickname + " was burned!");
        } else if(st.equals(Status.FROZEN)) {
            System.out.println(nickname + " was frozen solid!");
        }
    }

    public boolean hasTemporalStatus(TemporalStatus st) {
        return tempStatus.contains(st);
    }
    public boolean hasStatus(Status st) { return status.equals(st); }

    public void causeTemporalStatus(TemporalStatus st, Pokemon other) {
        if(!tempStatus.contains(st)) {
            tempStatus.add(st);
        }
        if(st.equals(TemporalStatus.CONFUSED)) {
            System.out.println(nickname + " was confused!");
        } else if(st.equals(TemporalStatus.INFATUATED)) {
            System.out.println(nickname + " was infatuated!");
            if(hasItem("DESTINYKNOT") && other != null) {
                if(other.canInfatuate(false,this)) other.causeTemporalStatus(TemporalStatus.INFATUATED, null);
            }
        } else if(st.equals(TemporalStatus.TRAPPED) || st.equals(TemporalStatus.PARTIALLYTRAPPED)) {
            System.out.println(nickname + " was trapped!");
        } else if(st.equals(TemporalStatus.CURSED)) {
            System.out.println(nickname + " was cursed!");
        } else if(st.equals(TemporalStatus.SEEDED)) {
            System.out.println(nickname + " was seeded!");
        } else if(st.equals(TemporalStatus.PERISHSONG)) {
            System.out.println(nickname + " was perished song!");
        } else if(st.equals(TemporalStatus.CENTERATTENTION)) {
            System.out.println(nickname + " is now the center of attention!");
        }
    }

    public Status getStatus() { return status; }

    public void changeAbility(String newAbility) {
        ability = utils.getAbility(newAbility);
        battle.outToFieldActivate();
    }

    public boolean changeStat(int stat, int quantity, boolean selfCaused, boolean message) {
        String st = "";
        String raise = "";
        switch (stat) {
            case 0 -> st = "Attack";
            case 1 -> st = "Defense";
            case 2 -> st = "Special attack";
            case 3 -> st = "Special defense";
            case 4 -> st = "Speed";
            case 5 -> st = "Accuracy";
            case 6 -> st = "Evasion";
        }
        if(quantity == 1) {
            raise = "raised";
        } else if(quantity == 2) {
            raise = "raised a lot";
        } else if(quantity > 2 && quantity < 6) {
            raise = "raised incredibly";
        } else if(quantity >= 6) {
            raise = "maximized";
        } else if(quantity == -1) {
            raise = "decreased";
        } else if(quantity == -2) {
            raise = "decreased a lot";
        } else if(quantity < -2 && quantity > -6) {
            raise = "decreased incredibly";
        } else if(quantity <= -6) {
            raise = "minimized";
        }

        if(team.effectTeamMoves.get(0) > 0 && quantity < 0 && !selfCaused) { // MIST effect
            return false;
        }
        if(hasAbility("KEENEYE") && stat == 5 && quantity < 0) { // KEEN EYE effect
            return false;
        }
        if(hasAbility("BIGPECKS") && stat == 1 && quantity < 0 && !selfCaused) { // BIG PECKS effect
            return false;
        }

        if(statChanges.get(stat) == -6 && quantity < 0) {
            if(message) { System.out.println(st + " of " + nickname + " can't decrease more!"); }
            return false;
        } else if(statChanges.get(stat) == 6 && quantity > 0) {
            if(message) { System.out.println(st + " of " + nickname + " can't increase more!"); }
            return false;
        }

        statChanges.set(stat,statChanges.get(stat)+quantity);
        if(statChanges.get(stat)+quantity > 6) {
            statChanges.set(stat,6);
        } else if(statChanges.get(stat)+quantity < -6) {
            statChanges.set(stat,-6);
        }

        System.out.println(st + " of " + nickname + " " + raise + "!");
        return true;
    }

    public boolean hasPP(Movement move) {
        int ind = getIndexMove(move.getInternalName());
        return remainPPs.get(ind) > 0;
    }
    public int hasPPByIndex(int id) {
        if(id < 0 || id >= remainPPs.size()) {
            return -1;
        }
        return remainPPs.get(id);
    }

    public boolean isOutPP() {
        for (Integer remainPP : remainPPs) {
            if (remainPP > 0) {
                return false;
            }
        }
        return true;
    }

    public void healPokemon(boolean message) {
        healHP(-1, message, message);
        healStatus(true, message);
        healPP(-1,-1);
    }
    public void healPP(int move, int pps) {
        // move -1 means all the moves will be restored
        // pps -1 means all the PPs will be restored
        for(int i=0;i<remainPPs.size();i++) {
            if(i==move || move == -1) {
                if(pps == -1) {
                    remainPPs.set(i,getMoves().get(i).getPP());
                } else {
                    remainPPs.set(i, remainPPs.get(i)+pps);
                    // check if max PPs are not overed
                    if(remainPPs.get(i) > moves.get(i).getPP()) {
                        remainPPs.set(i, moves.get(i).getPP());
                    }
                }
            }
        }
    }

    public void healStatus(boolean fainted, boolean message) {
        tempStatus.clear();
        if(fainted || !status.equals(Status.FAINTED)) {
            status = Status.FINE;
        }
        if(message) { System.out.println(nickname + " recovers its status!"); }
    }

    public void healPermanentStatus() {
        if(hasStatus(Status.POISONED) || hasStatus(Status.BADLYPOISONED)) {
            System.out.println(nickname + " recovers from Poison!");
        } else if(hasStatus(Status.ASLEEP)) {
            System.out.println(nickname + " woke up!");
        } else if(hasStatus(Status.BURNED)) {
            System.out.println(nickname + " is no longer burned!");
        } else if(hasStatus(Status.FROZEN)) {
            System.out.println(nickname + " thawed!");
        } else if(hasStatus(Status.PARALYZED)) {
            System.out.println(nickname + " is no longer paralyzed!");
        }

        status = Status.FINE;
        badPoisonTurns = 0;
        sleepTurns = 0;
    }

    public Type getType(String name) {
        return utils.getType(name);
    }

    public void healTempStatus(TemporalStatus temp, boolean message) {
        tempStatus.remove(temp);
        if(message) { System.out.println(nickname + " recovers its status!"); }
    }

    public boolean hasAllHP() {
        return psActuales >= getHP();
    }

    public boolean healHP(int hp, boolean message, boolean messageAll) {
        // hp -1 means all the HP will be restored
        if(hasAllHP()) {
            if(messageAll) { System.out.println(nickname + " already has all its HPs!"); }
            return false;
        }
        psActuales += hp;
        if(hp == -1 || psActuales > getHP()) {
            psActuales = getHP();
        }
        if(message) { System.out.println(nickname + " recovers " + hp + " HPs!"); }
        return true;
    }

    public void modifyHappiness(int hap) {
        if(hap > 0 && hasItem("SOOTHEBELL")) {
            hap *= 1.5;
        }
        if(happiness < 255) {
            happiness += hap;
            if(happiness < 0) {
                happiness = 0;
            } else if(happiness > 255) {
                happiness = 255;
            }
        }
    }

    public void increaseEffectMove(int index) {
        effectMoves.set(index,effectMoves.get(index)+1);
    }

    public void gainExperience(Pokemon rival, int participants, boolean isTrainer) {
        if(participate && !isFainted()) {
            double base = rival.specie.experience*rival.level/participants/5.0;
            double a = Math.pow((2*rival.level+10),(5/2));
            double b = Math.pow((level+rival.level+10),(5/2));
            double bonus = 1.0;
            if(isTrainer) {
                bonus *= 1.5;
            }
            if(hasItem("LUCKYEGG")) {
                bonus *= 1.5;
            }

            int exp = (int)((base*a/b + 1)*bonus);
            experience += exp;
            System.out.println(nickname + " gained " + exp + " points of experience!");
            // gain evs
            if(totalEvs() < 510) {
                for(int i=0;i<evs.size();i++) {
                    int newEvs = rival.specie.evs.get(i);
                    if(hasItem("MACHOBRACE")) {
                        newEvs *= 2;
                    }
                    if((i == 0 && hasItem("POWERWEIGHT")) || (i == 1 && hasItem("POWERBRACER")) || (i == 2 && hasItem("POWERBELT"))
                            || (i == 3 && hasItem("POWERLENS")) || (i == 4 && hasItem("POWERBAND")) || (i == 5 && hasItem("POWERANKLET"))) {
                        newEvs += 8; // evs power items
                    }
                    evs.set(i,evs.get(i)+newEvs);
                    if(evs.get(i) > 252) {
                        evs.set(i,252);
                    }
                }
            }
            // raise level
            raiseLevel();
        }
    }

    private void raiseLevel() {
        while(experience >= calcExperience(level+1)) {
            level++;
            System.out.println(nickname + " raised to level " + level + "!");
            List<Integer> tempStats = new ArrayList<Integer>();
            for(int i=0;i<6;i++) {
                tempStats.add(stats.get(i));
            }
            calcStats();
            // show new stats
            System.out.println("HP +" + (stats.get(0)-tempStats.get(0)) + ": " + stats.get(0));
            System.out.println("Attack +" + (stats.get(1)-tempStats.get(1)) + ": " + stats.get(1));
            System.out.println("Defense +" + (stats.get(2)-tempStats.get(2)) + ": " + stats.get(2));
            System.out.println("Sp. At +" + (stats.get(3)-tempStats.get(3)) + ": " + stats.get(3));
            System.out.println("Sp. Def +" + (stats.get(4)-tempStats.get(4)) + ": " + stats.get(4));
            System.out.println("Speed +" + (stats.get(5)-tempStats.get(5)) + ": " + stats.get(5));
            tempStats.clear();
            // increase happiness
            if(happiness <= 90) {
                modifyHappiness(5);
            } else if(happiness <= 199) {
                modifyHappiness(4);
            } else {
                modifyHappiness(3);
            }
            // check new moves
            setMoves(true,false);
            if(!hasItem("EVERSTONE")) checkEvolution();
        }
    }

    public void checkEvolution() {
        for(int i=0;i<specie.evos.size();i++) {
            Evolution ev = specie.evos.get(i);
            // check evolution method -> LEVEL
            if(ev.method.equals("Level")) {
                if(level >= Integer.parseInt(ev.complement)) {
                    evolve(utils.getPokemon(ev.evo));
                }
            }
            // TODO: rest of evolution methods
        }
    }

    private void evolve(Specie evolution) {
        System.out.println("What?\n" + nickname + " is evolving!");
        if(nickname.equals(specie.name)) {
            nickname = evolution.name;
        }
        specie = evolution;
        calcStats();
        System.out.println("Congratulations!\nYour " + nickname + " has evolved into " + evolution.name + "!");
        setMoves(true,true);
    }

    public int calcExperience(int l) {
        int exp = 0;
        switch (specie.growthRate.toString()) {
            case "ERRATIC":
                if(l < 50) {
                    exp = (int)((Math.pow(l,3)*(100-l))/50.0);
                } else if(l < 68) {
                    exp = (int)((Math.pow(l,3)*(150-l))/100.0);
                } else if(l < 98) {
                    exp = (int)((Math.pow(l,3)*((1911-10*l)/3.0))/500.0);
                } else {
                    exp = (int)((Math.pow(l,3)*(160-l))/100.0);
                }
                break;
            case "FAST":
                exp = (int)(0.8*Math.pow(l,3));
                break;
            case "MEDIUMFAST":
                exp = (int)(Math.pow(l,3));
                break;
            case "MEDIUMSLOW":
                exp = (int)((1.2*Math.pow(l,3))-(15*Math.pow(l,2))+100*l-140);
                break;
            case "SLOW":
                exp = (int)((5.0*Math.pow(l,3))/4.0);
                break;
            case "FLUCTUATING":
                if(l < 15) {
                    exp = (int)(((((l+1)/3.0)+24)/50.0)*Math.pow(l,3));
                } else if(l < 36) {
                    exp = (int)(((l+14)/50.0)*Math.pow(l,3));
                } else {
                    exp = (int)((((l/3.0)+32)/50.0)*Math.pow(l,3));
                }
                break;
        }

        return exp;
    }

    public void calcStats() {
        for(int i=0;i<6;i++) {
            if(i==0) {
                // HP stat
                stats.set(0,(int) (((((specie.stats.get(0)*2)+ivs.get(0)+(evs.get(0)/4.0))*level)/100.0)+level+10));
            } else {
                // other stats
                stats.set(i,(int) (((((((specie.stats.get(i)*2)+ivs.get(i)+(evs.get(i)/4.0))*level)/100.0)+5)*getNat(i))));
            }
        }
    }

    public void changedPokemon() {
        tempStatus.clear();
        if(hasStatus(Status.BADLYPOISONED)) {
            badPoisonTurns = 1;
        }
        previousMove = null;
        lastMoveInThisTurn = null;
        lastMoveReceived = null;
        disabledMove = null;
        encoreMove = null;
        chosenMove = null;
        previousDamage = 0;
        bideDamage = 0;

        criticalIndex = 0;

        protectTurns = 0;
        pokeTurn = 0;
        // recover move effects
        for(int i=0;i<effectMoves.size();i++) {
            effectMoves.set(i,0);
        }
        // restore used moves
        for(int i=0;i<usedMoves.size();i++) {
            usedMoves.set(i,false);
        }

        // restore stat changes
        for(int i=0;i<statChanges.size();i++) {
            statChanges.set(i,0);
        }
        // restore ability
        ability = originalAbility;
        // restore types
        battleType1 = specie.type1;
        battleType2 = specie.type2;
    }

    public void battleEnded() {
        changedPokemon();
        battle = null;
        if(hasStatus(Status.ASLEEP)) {
            sleepTurns = 1;
        }
        stockpile = 0;
        participate = false;
    }
    public boolean canPoison(boolean selfCaused) {
        //TODO: conditions for poison
        if(hasType("POISON") || hasType("STEEL")) {
            return false;
        }
        if(!getStatus().equals(Status.FINE)) {
            return false;
        }
        if(hasAbility("IMMUNITY")) {
            return false;
        }
        if(battle.terrain.hasTerrain(TerrainTypes.MISTY) && !isLevitating()) {
            return false;
        }
        if(getTeam().effectTeamMoves.get(1) > 0 && !selfCaused) { // safeguard
            return false;
        }
        return true;
    }

    public boolean canBurn(boolean selfCaused) {
        //TODO: conditions for burn
        if(hasType("FIRE")) {
            return false;
        }
        if(!getStatus().equals(Status.FINE)) {
            return false;
        }
        if(battle.terrain.hasTerrain(TerrainTypes.MISTY) && !isLevitating()) {
            return false;
        }
        if(getTeam().effectTeamMoves.get(1) > 0 && !selfCaused) { // safeguard
            return false;
        }
        return true;
    }

    public boolean canParalyze(boolean selfCaused) {
        //TODO: conditions for paralyze
        if(hasType("ELECTRIC")) {
            return false;
        }
        if(hasAbility("LIMBER")) {
            return false;
        }
        if(!getStatus().equals(Status.FINE)) {
            return false;
        }
        if(battle.terrain.hasTerrain(TerrainTypes.MISTY) && !isLevitating()) {
            return false;
        }
        if(getTeam().effectTeamMoves.get(1) > 0 && !selfCaused) { // safeguard
            return false;
        }
        return true;
    }

    public boolean canSleep(boolean selfCaused) {
        //TODO: conditions for sleep
        if(!getStatus().equals(Status.FINE) || battle.effectFieldMoves.get(1) > 0) {
            return false;
        }
        if((battle.terrain.hasTerrain(TerrainTypes.ELECTRIC) || battle.terrain.hasTerrain(TerrainTypes.MISTY)) && !isLevitating()) {
            return false;
        }
        if(getTeam().effectTeamMoves.get(1) > 0 && !selfCaused) { // safeguard
            return false;
        }
        return true;
    }

    public boolean canFreeze(boolean selfCaused) {
        //TODO: conditions for freeze
        if(hasType("ICE")) {
            return false;
        }
        if(!getStatus().equals(Status.FINE)) {
            return false;
        }
        if(battle.terrain.hasTerrain(TerrainTypes.MISTY) && !isLevitating()) {
            return false;
        }
        if(battle.weather.hasWeather(Weathers.SUNLIGHT) || battle.weather.hasWeather(Weathers.HEAVYSUNLIGHT)) {
            return false;
        }
        if(getTeam().effectTeamMoves.get(1) > 0 && !selfCaused) { // safeguard
            return false;
        }
        return true;
    }

    public boolean canConfuse(boolean selfCaused) {
        //TODO: conditions for confusion
        if(hasTemporalStatus(TemporalStatus.CONFUSED)) {
            return false;
        }
        if(hasAbility("OWNTEMPO")) {
            return false;
        }
        if(battle.terrain.hasTerrain(TerrainTypes.MISTY) && !isLevitating()) {
            return false;
        }
        if(getTeam().effectTeamMoves.get(1) > 0 && !selfCaused) { // safeguard
            return false;
        }
        return true;
    }
    public boolean canInfatuate(boolean selfCaused, Pokemon other) {
        //TODO: conditions for infatuate
        if(gender == 2 || other.gender == 2) { // genderless pokemon can't infatuate
            return false;
        }
        if((gender == 1 && other.gender == 1) || (gender == 0 && other.gender == 0)) { // same gender can't infatuate
            return false;
        }
        if(hasTemporalStatus(TemporalStatus.INFATUATED)) {
            return false;
        }
        if(hasAbility("OBLIVIOUS")) {
            return false;
        }
        if(getTeam().effectTeamMoves.get(1) > 0 && !selfCaused) { // safeguard
            return false;
        }
        return true;
    }

    public boolean canSeed() {
        //TODO: conditions for seed
        if(hasType("GRASS") || hasTemporalStatus(TemporalStatus.SEEDED)) {
            return false;
        }

        return true;
    }

    public boolean canFlinch() {
        //TODO: conditions for flinch
        if(hasTemporalStatus(TemporalStatus.FLINCHED)) {
            return false;
        }
        if(hasAbility("INNERFOCUS")) {
            return false;
        }
        return true;
    }

    public boolean canDrows() {
        //TODO: conditions for drowsy
        if(effectMoves.get(6) > 0) {
            return false;
        }
        if(battle.terrain.hasTerrain(TerrainTypes.ELECTRIC) && !isLevitating()) {
            return false;
        }
        if(!getStatus().equals(Status.FINE)) {
            return false;
        }
        if(getTeam().effectTeamMoves.get(1) > 0) { // safeguard
            return false;
        }
        return true;
    }

    public boolean canIntimidate() {
        //TODO: substitute cant intimidate
        if(hasAbility("CLEARBODY") || hasAbility("WHITESMOKE") || hasAbility("HYPERCUTTER") || hasAbility("INNERFOCUS") ||
                hasAbility("OBLIVIOUS") || hasAbility("SCRAPPY") || hasAbility("OWNTEMPO")) {
            System.out.println(nickname + " evades Intimidate thanks to " + ability.name + "!");
            return false;
        }
        return true;
    }

    public boolean affectSandstorm() {
        if(hasType("ROCK") || hasType("GROUND") || hasType("STEEL") || hasItem("SAFETYGOOGLES")) {
            return false;
        }
        if(hasAbility("SANDVEIL") || hasAbility("MAGICGUARD") || hasAbility("SANDFORCE") || hasAbility("OVERCOAT")
                || hasAbility("SANDRUSH")) {
            return false;
        }
        return true;
    }

    public boolean affectHail() {
        if(hasType("ICE") || hasItem("SAFETYGOOGLES")) {
            return false;
        }
        if(hasAbility("SNOWCLOAK") || hasAbility("MAGICGUARD") || hasAbility("SLUSHRUSH") || hasAbility("OVERCOAT")) {
            return false;
        }
        return true;
    }
    public boolean isLevitating() {
        if(hasItem("IRONBALL")) {
            return false;
        }
        if(hasType("FLYING") || hasItem("AIRBALLOON")) {
            return true;
        }
        if(hasAbility("LEVITATE")) {
            return true;
        }
        return false;
    }
    public boolean affectToxicSpikes() {
        if(!canPoison(false)) {
            return false;
        }
        if(isLevitating()) {
            return false;
        }
        return true;
    }
    public void rapidSpin() {
        if(hasTemporalStatus(TemporalStatus.PARTIALLYTRAPPED)) {
            healTempStatus(TemporalStatus.PARTIALLYTRAPPED, false);
            System.out.println(nickname + " was freed!");
            effectMoves.set(4, 0); // fire spin
            //TODO: rest of partially trapped moves
        }
        //TODO: spikes, stealth rock, etc...
        if(getTeam().effectTeamMoves.get(3) > 0) {
            getTeam().removeTeamEffects(this,3); // remove toxic spikes
        }
    }

    private boolean usedAllMoves() {
        for(int i=0;i<usedMoves.size();i++) {
            if(!usedMoves.get(i) && getIndexMove("LASTRESORT") != i) {
                return false;
            }
        }

        return true;
    }

    private boolean hasSomePPIsNotLastResort() {
        for(int i=0;i<moves.size();i++) {
            if(!moves.get(i).getMove().hasName("LASTRESORT")) {
                if(hasPPByIndex(i) > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean canUseLastResort() {
        if(moves.size() == 1) {
            return false;
        }
        if(!usedAllMoves()) {
            return false;
        }
        if(!hasSomePPIsNotLastResort()) {
            return false;
        }

        return true;
    }

    public double getStatChange(int i) {
        switch (statChanges.get(i)) {
            case 1:
                if(i <= 4) {
                    return 1.5;
                } else {
                    return 1.33;
                }
            case 2:
                if(i <= 4) {
                    return 2.0;
                } else {
                    return 1.67;
                }
            case 3:
                if(i <= 4) {
                    return 2.5;
                } else {
                    return 2.0;
                }
            case 4:
                if(i <= 4) {
                    return 3.0;
                } else {
                    return 2.33;
                }
            case 5:
                if(i <= 4) {
                    return 3.5;
                } else {
                    return 2.67;
                }
            case 6:
                if(i <= 4) {
                    return 4.0;
                } else {
                    return 3.0;
                }
            case -1:
                if(i <= 4) {
                    return 0.67;
                } else {
                    return 0.75;
                }
            case -2:
                if(i <= 4) {
                    return 0.5;
                } else {
                    return 0.6;
                }
            case -3:
                if(i <= 4) {
                    return 0.4;
                } else {
                    return 0.5;
                }
            case -4:
                if(i <= 4) {
                    return 0.33;
                } else {
                    return 0.43;
                }
            case -5:
                if(i <= 4) {
                    return 0.29;
                } else {
                    return 0.38;
                }
            case -6:
                if(i <= 4) {
                    return 0.25;
                } else {
                    return 0.33;
                }
        }

        return 1.0;
    }

    public double getNat(int st) {
        double nat = 1.0;
        if(st == 1) { // attack
            if(nature.equals(Natures.ADAMANT) || nature.equals(Natures.LONELY) || nature.equals(Natures.NAUGHTY) ||
                    nature.equals(Natures.BRAVE)) {
                nat = 1.1;
            } else if(nature.equals(Natures.BOLD) || nature.equals(Natures.MODEST) || nature.equals(Natures.CALM) ||
                    nature.equals(Natures.TIMID)) {
                nat = 0.9;
            }
        } else if(st == 2) { // defense
            if(nature.equals(Natures.BOLD) || nature.equals(Natures.IMPISH) || nature.equals(Natures.LAX) ||
                    nature.equals(Natures.RELAXED)) {
                nat = 1.1;
            } else if(nature.equals(Natures.LONELY) || nature.equals(Natures.MILD) || nature.equals(Natures.GENTLE) ||
                    nature.equals(Natures.HASTY)) {
                nat = 0.9;
            }
        } else if(st == 3) { // special attack
            if(nature.equals(Natures.MODEST) || nature.equals(Natures.MILD) || nature.equals(Natures.RASH) ||
                    nature.equals(Natures.QUIET)) {
                nat = 1.1;
            } else if(nature.equals(Natures.ADAMANT) || nature.equals(Natures.IMPISH) || nature.equals(Natures.CAREFUL) ||
                    nature.equals(Natures.JOLLY)) {
                nat = 0.9;
            }
        } else if(st == 4) { // special defense
            if(nature.equals(Natures.CALM) || nature.equals(Natures.GENTLE) || nature.equals(Natures.CAREFUL) ||
                    nature.equals(Natures.SASSY)) {
                nat = 1.1;
            } else if(nature.equals(Natures.NAUGHTY) || nature.equals(Natures.LAX) || nature.equals(Natures.RASH) ||
                    nature.equals(Natures.NAIVE)) {
                nat = 0.9;
            }
        }else { // speed
            if(nature.equals(Natures.TIMID) || nature.equals(Natures.HASTY) || nature.equals(Natures.JOLLY) ||
                    nature.equals(Natures.NAIVE)) {
                nat = 1.1;
            } else if(nature.equals(Natures.BRAVE) || nature.equals(Natures.RELAXED) || nature.equals(Natures.QUIET) ||
                    nature.equals(Natures.SASSY)) {
                nat = 0.9;
            }
        }
        return nat;
    }
}
