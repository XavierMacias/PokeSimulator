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
    private List<Integer> originalstats;
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
    public Item pokeball;
    public Item item;
    public Item originalItem;
    public int criticalIndex = 0;
    boolean participate;
    private Team team;
    // variables for status and moves
    public int pokeTurn = 0;
    public int sleepTurns = 0;
    public int badPoisonTurns = 0;
    public int protectTurns = 0;
    public int destinyBondTurns = 0;
    public Movement previousMove;
    public Movement lastMoveInThisTurn, lastMoveUsedInTurn;
    public Movement lastMoveReceived;
    public Movement disabledMove, encoreMove, chosenMove;
    public Movement cursedBodyMove;
    public int previousDamage;
    public int bideDamage;
    public List<Integer> effectMoves;
    public int stockpile = 0;
    public boolean truant;
    private double weight;
    private Scanner in;
    Random random;
    public Battle battle;
    public int substitute = -1;

    public Pokemon(Specie specie, int level, Utils utils) {
        random = new Random();
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
        // TODO: change this because ability, weight, height and gender in case of azurill can change to evolve
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
        originalstats = stats;
        psActuales = stats.get(0);
        // set initial moves
        moves = new ArrayList<Pair<Movement,Integer>>();
        remainPPs = new ArrayList<Integer>();
        usedMoves = new ArrayList<Boolean>();
        truant = false;
        setMoves(false, false);
        //is shiny?
        if(utils.getRandomNumberBetween(1,4097) == 1) {
            isShiny = true;
        }
        weight = specie.weight;
        pokeball = utils.getItem("POKEBALL");

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
           28 -> flash fire
           29 -> minimize
           30 -> imprison
           31 -> grudge
           32 -> rest
           33 -> perish song
           34 -> mean look
           35 -> sleep talk
           36 -> dig
           37 -> taunt
           38 -> stomping tantrum
           39 -> burn up
           40 -> mind reader
           41 -> miracle eye
           42 -> telekinesis
           43 -> smack down
           44 -> bounce
           45 -> magnet rise
           46 -> dive
           47 -> micle berry (more accuracy in next turn)
           48 -> custap berry (priority in next turn)
           49 -> clamp
           50 -> whirlpool
           51 -> bind
           52 -> cursed body
           53 -> destiny bond
           54 -> nightmare
           55 -> unburden
           56 -> substitute
           57 -> giga impact
           58 -> magic coat
           59 -> sky drop
           60 -> laser focus
           61 -> infestation
           62 -> embargo
           63 -> throat chop
           64 -> heal block
           65 -> phantomp force
           66 -> protean
           67 -> slow start
           68 -> magma storm
        */
        for(int i=0;i<69;i++) {
            effectMoves.add(0);
        }
        // alternative forms
        form = 0;
        participate = false;
        battle = null;
        disabledMove = null;
        cursedBodyMove = null;
        encoreMove = null;
        chosenMove = null;
        originalItem = null;
    }

    public void setMove(String m) { // ONLY FOR DEBUG
        moves.set(0,new Pair<>(utils.getMove(m),utils.getMove(m).getPP()));
        remainPPs.set(0,utils.getMove(m).getPP());
        usedMoves.set(0,false);
    }
    public void setUniqueMove(String m) { // ONLY FOR DEBUG
        moves.clear();
        remainPPs.clear();
        usedMoves.clear();
        moves.add(new Pair<>(utils.getMove(m),utils.getMove(m).getPP()));
        remainPPs.add(utils.getMove(m).getPP());
        usedMoves.add(false);
    }
    public void setAbility(String newAbility) { // ONLY FOR DEBUG
        try {
            ability = utils.getAbility(newAbility);
            originalAbility = utils.getAbility(newAbility);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void setItem(String newItem) { // ONLY FOR DEBUG
        item = utils.getItem(newItem);
        originalItem = item;
    }

    public boolean getNature(String nat) {
        return nature.toString().equals(nat);
    }
    public int getHappiness() { return happiness; }

    public void giveItem(String newItem, boolean temporal) {
        item = utils.getItem(newItem);
        effectMoves.set(55, 0);
        if(!temporal) originalItem = item;
    }

    public void setParticipate(boolean participate) {
        this.participate = participate;
    }

    public List<Pair<Movement, Integer>> getMoves() {
        return moves;
    }

    public void showInfo() {
        // basic info
        System.out.println(nickname + " - " + specie.name);
        if(gender == 0) {
            System.out.print(" male ");
        } else if(gender == 1) {
            System.out.print(" female ");
        }
        System.out.print(" - " + status.toString()+" - ");
        System.out.println(specie.type1.name);
        if(specie.type2 != null) System.out.print(" - " + specie.type2.name + " ");
        // stats, nature and ability
        for(int i=0;i<stats.size();i++) {
            if(i==0) System.out.print("PS: " + psActuales + "/");
            if(i==1) System.out.print("Attack: ");
            if(i==2) System.out.print("Defense: ");
            if(i==3) System.out.print("Special Attack: ");
            if(i==4) System.out.print("Special Defense: ");
            if(i==5) System.out.print("Speed: ");
            System.out.print(stats.get(i) + " - ");
        }
        System.out.println("NATURE: " + nature.toString() + " - ");
        System.out.println("ABILITY: " + ability.name + ": " + ability.description);
        // moves
        System.out.println("MOVES: ");
        for(int i=0;i<moves.size();i++) {
            System.out.println((i+1)+": "+moves.get(i).getMove().name+" - "+getRemainPPs().get(i)+"/"+moves.get(i).getPP() + " - " + moves.get(i).getMove().type.name + " - " + moves.get(i).getMove().getCategory().toString());
            System.out.println(moves.get(i).getMove().description);
        }
    }

    public Movement selectMove() {
        int chosenIndex = -1;
        do {
            System.out.println("0: Exit");
            for(int i=0;i<moves.size();i++) {
                System.out.println((i+1)+": "+moves.get(i).getMove().name+" - "+remainPPs.get(i)+"/"+moves.get(i).getPP() + " - " + moves.get(i).getMove().type.name);
            }
            chosenIndex = Integer.parseInt(in.nextLine());
            if(chosenIndex == 0) {
                return null;
            }
        } while(chosenIndex < 0 || chosenIndex > moves.size());

        return moves.get(chosenIndex-1).getMove();
    }

    public boolean disabledMove(Movement mv) {
        if((hasItem("ASSAULTVEST") || effectMoves.get(37) > 0) && mv.getCategory().equals(Category.STATUS) && !mv.hasName("MEFIRST")) {
            return true; // assault vest and taunt
        }
        if(disabledMove != null) return disabledMove.equals(mv);

        if(battle.moveEffects.attacksToHeal.contains(mv.getCode()) && effectMoves.get(64) > 0) {
            return true;
        }
        if(battle.hasDamp() && (mv.hasName("SELFDESTRUCT") || mv.hasName("EXPLOSION") || mv.hasName("MINDBLOWN") || mv.hasName("MISTYEXPLOSION"))) {
            return true;
        }
        if(cursedBodyMove != null) return cursedBodyMove.equals(mv);

        if(battle.effectFieldMoves.get(2) > 0 && mv.hasName("TELEKINESIS")) { // telekinesis cannot select with gravity
            return true;
        }
        if(effectMoves.get(63) > 0 && mv.getFlags().contains("j")) { // throat chop can't use sound moves
            return true;
        }
        if(chosenMove != null) return choiceMove() && !mv.equals(chosenMove);

        return false;
    }

    public boolean disabledMove(int i) {
        Movement mv = moves.get(i).getMove();
        return disabledMove(mv);
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
        int crit = criticalIndex;
        if((hasItem("SCOPELENS") || hasItem("RAZORCLAW"))) {
            crit += 1;
        }
        if(hasAbility("SUPERLUCK")) crit += 1;
        if(hasItem("LEEK") && (specieNameIs("FARFETCHD") || specieNameIs("SIRFETCHD"))
            || hasItem("LUCKYPUNCH") && specieNameIs("CHANSEY")) {
            crit += 2;
        }

        if(crit > 4) crit = 4;
        return crit;
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

    public boolean canUseItem() {
        if(item == null || battle.effectFieldMoves.get(6) > 0 || effectMoves.get(62) > 0) { // magic room, embargo
            return false;
        }
        if(hasAbility("KLUTZ")) return false;
        return true;
    }

    public boolean useItem(boolean lose, boolean message, boolean temporal) {
        if(canUseItem()) {
            System.out.println(nickname + " used " + item.name + "!");
            if(lose) loseItem(message, temporal);
            return true;
        }
        return false;
    }

    public boolean hasItemWithFlag(String fl) {
        if(item == null) {
            return false;
        }
        return item.getFlags().contains(fl);
    }

    public boolean hasItemWithPocket(String pc) {
        if(item == null) {
            return false;
        }
        return item.pocket.toString().equals(pc);
    }

    public void loseItem(boolean message, boolean temporal) {
        if(item != null) {
            if(message) System.out.println(nickname + " consumed " + item.name + "!");
            item = null;
            if(!temporal) {
                originalItem = null;
            }
            if(hasAbility("UNBURDEN")) effectMoves.set(55,1);
        }
    }
    public List<Integer> getStatChanges() { return statChanges; }

    public void setStatChanges(int index, int value) { statChanges.set(index, value); }
    public void setStatValue(int index, int value) { stats.set(index, value); }

    public int getAttack(boolean critic, boolean ignore, Movement move) {
        int attack = stats.get(1);
        if(hasAbility("HUGEPOWER") || hasAbility("PUREPOWER")) attack *= 2;
        if(ignore) {
            if(hasStatus(Status.BURNED) && !hasAbility("GUTS")) {
                attack /= 2.0;
            }
            return attack;
        }
        if (!(critic && getStatChange(0) < 1.0)) {
            attack = (int) (stats.get(1) * getStatChange(0));
        }
        if(hasStatus(Status.BURNED) && !hasAbility("GUTS") && move.getCode() != 173 && move.getCode() != 250) {
            attack /= 2.0;
        }
        if((hasAbility("SLOWSTART") && effectMoves.get(67) < 5) || (hasAbility("DEFEATIST") && psActuales < getHP()/2)) {
            attack /= 2.0;
        }
        if((hasAbility("GUTS") && (hasStatus(Status.BURNED) || hasStatus(Status.POISONED) || hasStatus(Status.BADLYPOISONED) ||
                hasStatus(Status.PARALYZED) || hasStatus(Status.ASLEEP))) || hasAbility("HUSTLE")) {
            attack *= 1.5; // GUTS and HUSTLE effect
        }
        if(hasAbility("TOXICBOOST") && (hasStatus(Status.BADLYPOISONED) || hasStatus(Status.POISONED))) {
            attack *= 1.5; // TOXIC BOOST
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

    public int getDefense(boolean critic, boolean chipaway, boolean ignore, boolean moldbreaker) {
        int defense = stats.get(2);
        if(battle.effectFieldMoves.get(5) > 0) {
            defense = stats.get(4);
        }
        if(ignore) {
            return defense;
        }
        if((hasAbility("MARVELSCALE") && !moldbreaker && (hasStatus(Status.BURNED) || hasStatus(Status.POISONED) ||
                hasStatus(Status.BADLYPOISONED) || hasStatus(Status.PARALYZED) || hasStatus(Status.ASLEEP)))) {
            defense *= 1.5;
        }
        if ((!(critic && getStatChange(1) > 1.0)) && !chipaway) {
            defense = (int) (stats.get(2)*getStatChange(1));
        }
        if((hasItem("EVIOLITE") && !specie.evos.isEmpty())
                || (hasItem("METALPOWDER") && specieNameIs("DITTO"))) {
            defense *= 1.5;
        }

        return defense;
    }

    public int getSpecialAttack(boolean critic, boolean ignore) {
        int spatk = stats.get(3);;
        if(ignore) {
            return spatk;
        }
        if (!(critic && getStatChange(2) < 1.0)) {
            spatk = (int) (stats.get(3)*getStatChange(2));
        }
        // solar power
        if(hasAbility("SOLARPOWER") && (battle.weather.hasWeather(Weathers.SUNLIGHT) || battle.weather.hasWeather(Weathers.HEAVYSUNLIGHT))) {
            spatk *= 1.5;
        }
        if(hasAbility("FLAREBOOST") && hasStatus(Status.BURNED)) {
            spatk *= 1.5; // FLARE BOOST
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
        if(hasAbility("DEFEATIST") && psActuales < getHP()/2) {
            spatk /= 2.0;
        }

        return spatk;
    }
    public int getSpecialDefense(boolean critic, boolean ignore) {
        int spdef = stats.get(4);
        if(battle.effectFieldMoves.get(5) > 0) {
            spdef = stats.get(2);
        }
        if(ignore) {
            return spdef;
        }
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
        if(hasStatus(Status.PARALYZED) && !hasAbility("QUICKFEET")) {
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
        if((hasAbility("SANDRUSH") && battle.weather.hasWeather(Weathers.SANDSTORM)) ||
                (hasAbility("SWIFTSWIM") && (battle.weather.hasWeather(Weathers.RAIN) || battle.weather.hasWeather(Weathers.HEAVYRAIN)))) {
            speed *= 2.0; // sand rush
        }
        if(hasAbility("QUICKFEET") && (hasStatus(Status.BURNED) || hasStatus(Status.POISONED) || hasStatus(Status.BADLYPOISONED) ||
                hasStatus(Status.PARALYZED) || hasStatus(Status.ASLEEP))) {
            speed *= 1.5; // QUICK FEET
        }
        if(hasItem("CHOICESCARF")) { // choice scarf
            speed *= 1.5;
        }
        if(hasItem("QUICKPOWDER") && specieNameIs("DITTO")) {
            speed *= 2.0;
        }
        if(hasAbility("SLOWSTART") && effectMoves.get(67) < 5) {
            speed /= 2.0;
        }
        if(effectMoves.get(55) > 0) speed *= 2.0;
        return speed;
    }
    public int getHP() { return stats.get(0); }

    public double getAccuracy(boolean ignore) {
        double accuracy = 1.0;
        if(!ignore) {
            accuracy = getStatChange(5);
        }
        if(battle.effectFieldMoves.get(2) > 0) {
            accuracy *= 1.67;
        }
        return accuracy;
    }
    public double getEvasion(boolean ignore) {
        if(ignore) {
            return 1.0;
        }
        return getStatChange(6); }

    public int getLevel() {
        return level;
    }

    public int getPsActuales() {
        return psActuales;
    }
    public double getWeight(boolean moldbreaker) {
        if(hasItem("FLOATSTONE") || (hasAbility("LIGHTMETAL") && !moldbreaker)) {
            return weight/2;
        }
        if(hasAbility("HEAVYMETAL") && !moldbreaker) {
            return weight*2;
        }

        return weight;
    }
    public void setWeight(double w) {
        weight = w;
        if(weight < 0.1) {
            weight = 0.1;
        }
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
            if(moves.get(i).getMove().getInternalName().equals(move)) {
                return true;
            }
        }
        return false;
    }

    public int getIndexMove(String move) {
        for(int i=0;i<moves.size();i++) {
            if(moves.get(i).getMove().getInternalName().equals(move)) {
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
                                deleteMove(mv);
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean learnMove(Movement move) {
        if(!move.isCompatible(specie.getInternalName())) {
            System.out.println(nickname + " can't learn " + move.name + "!");
            return false;
        }
        if(hasMove(move.getInternalName())) {
            System.out.println(nickname + " has already knew " + move.name + "...");
            return false;
        }
        if(moves.size() < 4) {
            // add move
            moves.add(new Pair<>(move,move.getPP()));
            remainPPs.add(move.getPP());
            usedMoves.add(false);
            System.out.println(nickname + " learned " + move.name + "!");
            return true;
        } else {
            // delete move
            return deleteMove(move);
        }
    }

    private boolean deleteMove(Movement move) {
        System.out.println(nickname + " wants to learn " + move.name + "\nBut " + nickname + " already known 4 moves");
        System.out.println("Do you want to forget a move in order to learn " + move.name + "?");
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
                System.out.println(nickname + " didn't learn " + move.name);
            } else {
                System.out.println("1, 2, 3... and... Poof!\n" + nickname +" forgot " + moves.get(chosenIndex-1).getMove().name + "!");
                moves.set(chosenIndex-1, new Pair<>(move, move.getPP()));
                remainPPs.set(chosenIndex-1, move.getPP());
                usedMoves.set(chosenIndex-1, false);
                System.out.println("And...\n" + nickname +" learned " + moves.get(chosenIndex-1).getMove().name + "!");
                return true;
            }
        } else {
            System.out.println(nickname + " didn't learn " + move.name);
        }

        return false;
    }

    public boolean isFainted() {
        return status.equals(Status.FAINTED);
    }

    public void reducePP(Movement move, int quantity, Pokemon other) {
        if(other.hasAbility("PRESSURE") && quantity == 1) quantity = 2;
        int ind = getIndexMove(move.getInternalName());
        if(ind != -1) {
            remainPPs.set(ind,remainPPs.get(ind)-quantity);
            if(remainPPs.get(ind) < 0 || quantity == -1) { // -1 loses all its PPs
                remainPPs.set(ind,0);
            }
        }
    }

    public void moveUsedAdded(Movement move) {
        if(move != null) {
            int ind = getIndexMove(move.getInternalName());
            if (ind != -1) {
                usedMoves.set(ind, true);
            }
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
            team.effectTeamMoves.set(13, 1); // retaliate
            // decrease happiness
            modifyHappiness(-1,-1,-1);
        }
    }

    public void setHP(int hp) {
        psActuales = hp;
    }

    public void causeStatus(Status st, Pokemon other, boolean self) {
        status = st;
        if(st.equals(Status.POISONED)) {
            System.out.println(nickname + " was poisoned!");
            if(canPoison(self,this) && !self && hasAbility("SYNCHRONIZE")) {
                other.status = Status.POISONED;
            }
        } else if(st.equals(Status.BADLYPOISONED)) {
            System.out.println(nickname + " was badly poisoned!");
            badPoisonTurns = 1;
            if(canPoison(self,this) && !self && hasAbility("SYNCHRONIZE")) {
                other.status = Status.BADLYPOISONED;
                other.badPoisonTurns = 1;
            }
        } else if(st.equals(Status.PARALYZED)) {
            System.out.println(nickname + " was paralyzed! Maybe is unable to move!");
            if(canParalyze(self,this) && !self && hasAbility("SYNCHRONIZE")) {
                other.status = Status.PARALYZED;
            }
        } else if(st.equals(Status.ASLEEP)) {
            System.out.println(nickname + " fell sleep!");
            sleepTurns = 1;
        } else if(st.equals(Status.BURNED)) {
            System.out.println(nickname + " was burned!");
            if(canBurn(self,this) && !self && hasAbility("SYNCHRONIZE")) {
                other.status = Status.BURNED;
            }
        } else if(st.equals(Status.FROZEN)) {
            System.out.println(nickname + " was frozen solid!");
        }
    }

    public boolean hasTemporalStatus(TemporalStatus st) {
        return tempStatus.contains(st);
    }
    public boolean hasStatus(Status st) { return status.equals(st); }
    public boolean hasSomeStatus() {
        return (!hasStatus(Status.FINE) && !hasStatus(Status.FAINTED));
    }

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
        //} else if(st.equals(TemporalStatus.PERISHSONG)) {
        //    System.out.println(nickname + " was perished song!");
        } else if(st.equals(TemporalStatus.CENTERATTENTION)) {
            System.out.println(nickname + " is now the center of attention!");
        }
    }

    public Status getStatus() { return status; }

    public void changeAbility(String newAbility) {
        try {
            ability = utils.getAbility(newAbility);
            System.out.println(nickname + " ability changed to " + ability.name + "!");
            battle.outToFieldActivate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean changeStat(int stat, int quantity, boolean selfCaused, boolean message, Pokemon other) {
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
        if(hasAbility("CONTRARY") && !(other.hasAbility("MOLDBREAKER") && !selfCaused)) quantity *= -1;
        if(hasAbility("SIMPLE") && !(other.hasAbility("MOLDBREAKER") && !selfCaused)) {
            quantity *= 2;
            if(quantity > 6) quantity = 6;
            if(quantity < -6) quantity = -6;
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

        if(other != null) {
            if((team.effectTeamMoves.get(0) > 0 || team.effectTeamMoves.get(17) > 0) && quantity < 0 && !selfCaused && !other.hasAbility("INFILTRATOR")) { // MIST effect
                return false;
            }
        }
        if((hasAbility("CLEARBODY") || hasAbility("WHITESMOKE")) && quantity < 0 && !selfCaused) { // CLEAR BODY, WHITE SMOKE effect and substitute
            if(other != null) {
                if(!other.hasAbility("MOLDBREAKER")) return false;
            } else {
                return false;
            }
        }
        if(effectMoves.get(56) > 0 && quantity < 0 && !selfCaused) return false;

        if(hasAbility("KEENEYE") && stat == 5 && quantity < 0) { // KEEN EYE effect
            if(other != null) {
                if(!other.hasAbility("MOLDBREAKER")) return false;
            } else {
                return false;
            }
        }
        if(hasAbility("BIGPECKS") && stat == 1 && quantity < 0 && !selfCaused) { // BIG PECKS effect
            if(other != null) {
                if(!other.hasAbility("MOLDBREAKER")) return false;
            } else {
                return false;
            }
        }
        if(hasAbility("HYPERCUTTER") && stat == 0 && quantity < 0 && !selfCaused) { // HYPER CUTTER effect
            if(other != null) {
                if(!other.hasAbility("MOLDBREAKER")) return false;
            } else {
                return false;
            }
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

        // DEFIANT
        if(hasAbility("DEFIANT") && quantity < 0 && !selfCaused) {
            changeStat(0,2,true,false, other);
        }
        // COMPETITIVE
        if(hasAbility("COMPETITIVE") && quantity < 0 && !selfCaused) {
            changeStat(2,2,true,false, other);
        }
        return true;
    }

    public boolean hasPP(Movement move) {
        int ind = getIndexMove(move.getInternalName());
        return remainPPs.get(ind) > 0;
    }
    public int remainPPOf(Movement move) {
        int ind = getIndexMove(move.getInternalName());
        if(ind < 0) {
            return -1;
        }
        return remainPPs.get(ind);
    }

    public Movement moveWithoutPP() {
        for (int i=0;i<moves.size();i++) {
            if (remainPPs.get(i) == 0) {
                return moves.get(i).getMove();
            }
        }
        return null;
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

    public boolean statsAreMaximum() {
        for(int i=0;i<statChanges.size();i++) {
            if(statChanges.get(i) < 6) {
                return false;
            }
        }
        return true;
    }
    public boolean statsAreMinimum() {
        for(int i=0;i<statChanges.size();i++) {
            if(statChanges.get(i) > -6) {
                return false;
            }
        }
        return true;
    }

    public void healPokemon(boolean message) {
        psActuales = 1;
        healHP(-1, message, message, false, false);
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
            truant = false;
            effectMoves.set(54,0);
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
        if(tempStatus.contains(temp)) {
            tempStatus.remove(temp);
            if(message) { System.out.println(nickname + " recovers its status!"); }
        }
    }

    public boolean hasAllHP() {
        return psActuales >= getHP();
    }
    public boolean hasAllPP() {
        for(int i=0;i<remainPPs.size();i++) {
            if(remainPPs.get(i) < moves.get(i).getPP()) return false;
        }

        return true;
    }

    public boolean isFullHealed() {
        return hasAllHP() && hasStatus(Status.FINE) && hasAllPP();
    }

    public boolean healHP(int hp, boolean message, boolean messageAll, boolean absorb, boolean healblock) {
        if(psActuales <= 0) {
            return false;
        }
        if(healblock && effectMoves.get(64) > 0) {
            System.out.println(nickname + " can't heal due to Heal Block!");
            return false;
        }
        // hp -1 means all the HP will be restored
        if(hasAllHP()) {
            if(messageAll) { System.out.println(nickname + " already has all its HPs!"); }
            return false;
        }
        if(absorb && hasItem("BIGROOT")) {
            hp *= 1.3;
        }
        psActuales += hp;
        if(hp == -1 || psActuales > getHP()) {
            psActuales -= hp;
            hp = getHP() - psActuales;
            psActuales = getHP();
        }
        if(message) { System.out.println(nickname + " recovers " + hp + " HPs!"); }
        return true;
    }

    public void revivePokemon(int hp) {
        if(hp == 0) hp = 1;
        System.out.println(nickname + " is no longer fainted!");
        psActuales = hp;
        if(hp == -1) {
            psActuales = getHP();
        }
        status = Status.FINE;
    }

    public void modifyHappiness(int hap1, int hap2, int hap3) {
        int hap = hap1;
        if(happiness >= 100 && happiness <= 199) {
            hap = hap2;
        } else if(happiness <= 200) {
            hap = hap3;
        }
        if(hap > 0 && pokeball.hasName("LUXURYBALL")) {
            hap += 1;
        }
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

    public void setHappiness(int hap) {
        happiness = hap;
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
            for(int i=0;i<evs.size();i++) {
                int newEvs = rival.specie.evs.get(i);
                gainEVS(i, newEvs, true);
            }
            // raise level
            raiseLevel(false);
        }
    }

    public boolean gainEVS(int i, int newEvs, boolean inBattle) {
        if(totalEvs() < 510) {
            if(hasItem("MACHOBRACE") && inBattle) {
                newEvs *= 2;
            }
            if(inBattle && ((i == 0 && hasItem("POWERWEIGHT")) || (i == 1 && hasItem("POWERBRACER")) || (i == 2 && hasItem("POWERBELT"))
                    || (i == 3 && hasItem("POWERLENS")) || (i == 4 && hasItem("POWERBAND")) || (i == 5 && hasItem("POWERANKLET")))) {
                newEvs += 8; // evs power items
            }
            evs.set(i,evs.get(i)+newEvs);
            if(evs.get(i) > 252) {
                evs.set(i,252);
                if(!inBattle) {
                    System.out.println("EVs of" + nickname + " increased!");
                    modifyHappiness(5, 3, 2);
                }
                return true;
            }
        }
        if(!inBattle) System.out.println("It doesn't have any effect...");
        return false;
    }
    public int getEVs(int i) { return evs.get(i); }
    public void setEVs(int i, int ev) { evs.set(i, ev); }

    public boolean increaseMaxPP(Movement move, double incr) {
        int ind = getIndexMove(move.getInternalName());
        int newPP = (int) (getMoves().get(ind).getPP()*incr);
        if (getMoves().get(ind).getPP() == move.getPP()*1.6) {
            System.out.println("It doesn't have any effect...");
            return false;
        }

        getMoves().get(ind).setPP(newPP);
        System.out.println("PP of " + move.name + " increased!");
        modifyHappiness(5, 3, 2);
        return true;
    }

    public boolean raiseLevel(boolean rarecandy) {
        if(level >= 100) {
            if(rarecandy) System.out.println("It doesn't have any effect...");
            return false;
        }
        while((experience >= calcExperience(level+1)) || rarecandy) {
            if(rarecandy) experience = calcExperience(level+1);
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
            if(!rarecandy) modifyHappiness(5,4,3);
            // check new moves
            setMoves(true,false);
            if(!hasItem("EVERSTONE")) checkEvolution();
            rarecandy = false;
        }
        return true;
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

    public void evolve(Specie evolution) {
        System.out.println("What?\n" + nickname + " is evolving!");
        System.out.println("Congratulations!\nYour " + nickname + " has evolved into " + evolution.name + "!");
        if(nickname.equals(specie.name)) {
            nickname = evolution.name;
        }
        specie = evolution;
        calcStats();
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
                if(specieNameIs("SHEDINJA")) stats.set(0,1);
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
        if(hasAbility("NATURALCURE") && !isFainted()) healStatus(false,false);
        if(hasAbility("REGENERATOR") && !isFainted()) healHP(getHP()/3,false,false,false, false);
        previousMove = null;
        lastMoveInThisTurn = null;
        lastMoveReceived = null;
        disabledMove = null;
        cursedBodyMove = null;
        encoreMove = null;
        chosenMove = null;
        previousDamage = 0;
        bideDamage = 0;
        truant = false;

        criticalIndex = 0;

        protectTurns = 0;
        destinyBondTurns = 0;
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
        weight = specie.weight;
        // restore stats
        stats = originalstats;
        // restore ability
        ability = originalAbility;
        // restore types
        battleType1 = specie.type1;
        battleType2 = specie.type2;
    }

    public void changeType(String type1, String type2) {
        battleType1 = utils.getType(type1);
        battleType2 = utils.getType(type2);
    }

    public void battleEnded() {
        changedPokemon();
        originalItem = null;
        battle = null;
        if(hasStatus(Status.ASLEEP)) {
            sleepTurns = 1;
        }
        stockpile = 0;
        participate = false;
    }
    public boolean canPoison(boolean selfCaused, Pokemon other) {
        // TODO: conditions for poison
        if(hasType("POISON") || hasType("STEEL")) {
            return false;
        }
        if(!getStatus().equals(Status.FINE)) {
            return false;
        }
        if(hasAbility("IMMUNITY")) {
            if(other != null) {
                if(!other.hasAbility("MOLDBREAKER")) {
                    return false;
                } else {
                    return true;
                }
            }
            return false;
        }
        if((battle.weather.hasWeather(Weathers.SUNLIGHT) || battle.weather.hasWeather(Weathers.HEAVYSUNLIGHT)) && hasAbility("LEAFGUARD")) {
            if(other != null) {
                if(!other.hasAbility("MOLDBREAKER")) {
                    return false;
                } else {
                    return true;
                }
            }
            return false;
        }
        if(battle.terrain.hasTerrain(TerrainTypes.MISTY) && !isLevitating()) {
            return false;
        }
        if(getTeam().effectTeamMoves.get(1) > 0 && !selfCaused) { // safeguard
            if(other != null) {
                if(!other.hasAbility("INFILTRATOR")) {
                    return false;
                } else {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public boolean canBurn(boolean selfCaused, Pokemon other) {
        //TODO: conditions for burn
        if(hasType("FIRE")) {
            return false;
        }
        if(!getStatus().equals(Status.FINE)) {
            return false;
        }
        if(hasAbility("WATERVEIL") || hasAbility("WATERBUBBLE")) {
            if(other != null) {
                if(!other.hasAbility("MOLDBREAKER")) {
                    return false;
                } else {
                    return true;
                }
            }
            return false;
        }
        if(battle.terrain.hasTerrain(TerrainTypes.MISTY) && !isLevitating()) {
            return false;
        }
        if((battle.weather.hasWeather(Weathers.SUNLIGHT) || battle.weather.hasWeather(Weathers.HEAVYSUNLIGHT)) && hasAbility("LEAFGUARD")) {
            if(other != null) {
                if(!other.hasAbility("MOLDBREAKER")) {
                    return false;
                } else {
                    return true;
                }
            }
            return false;
        }
        if(getTeam().effectTeamMoves.get(1) > 0 && !selfCaused && !other.hasAbility("INFILTRATOR")) { // safeguard
            return false;
        }
        return true;
    }

    public boolean canParalyze(boolean selfCaused, Pokemon other) {
        //TODO: conditions for paralyze
        if(hasType("ELECTRIC")) {
            return false;
        }
        if(hasAbility("LIMBER")) {
            if(other != null) {
                if(!other.hasAbility("MOLDBREAKER")) {
                    return false;
                } else {
                    return true;
                }
            }
            return false;
        }
        if(!getStatus().equals(Status.FINE)) {
            return false;
        }
        if(battle.terrain.hasTerrain(TerrainTypes.MISTY) && !isLevitating()) {
            return false;
        }
        if((battle.weather.hasWeather(Weathers.SUNLIGHT) || battle.weather.hasWeather(Weathers.HEAVYSUNLIGHT)) && hasAbility("LEAFGUARD")) {
            if(other != null) {
                if(!other.hasAbility("MOLDBREAKER")) {
                    return false;
                } else {
                    return true;
                }
            }
            return false;
        }
        if(getTeam().effectTeamMoves.get(1) > 0 && !selfCaused && !other.hasAbility("INFILTRATOR")) { // safeguard
            return false;
        }
        return true;
    }

    public boolean canSleep(boolean selfCaused, Pokemon other) {
        //TODO: conditions for sleep
        if(!getStatus().equals(Status.FINE) || battle.effectFieldMoves.get(1) > 0) {
            return false;
        }
        if(hasAbility("VITALSPIRIT") || hasAbility("INSOMNIA")) {
            if(other != null) {
                if(!other.hasAbility("MOLDBREAKER")) {
                    return false;
                } else {
                    return true;
                }
            }
            return false;
        }
        if((battle.terrain.hasTerrain(TerrainTypes.ELECTRIC) || battle.terrain.hasTerrain(TerrainTypes.MISTY)) && !isLevitating()) {
            return false;
        }
        if((battle.weather.hasWeather(Weathers.SUNLIGHT) || battle.weather.hasWeather(Weathers.HEAVYSUNLIGHT)) && hasAbility("LEAFGUARD")) {
            if(other != null) {
                if(!other.hasAbility("MOLDBREAKER")) {
                    return false;
                } else {
                    return true;
                }
            }
            return false;
        }
        if(getTeam().effectTeamMoves.get(1) > 0 && !selfCaused && !other.hasAbility("INFILTRATOR")) { // safeguard
            return false;
        }
        return true;
    }

    public boolean canFreeze(boolean selfCaused, Pokemon other) {
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
        if(hasAbility("MAGMAARMOR")) {
            if(other != null) {
                if(!other.hasAbility("MOLDBREAKER")) {
                    return false;
                } else {
                    return true;
                }
            }
            return false;
        }
        if(battle.weather.hasWeather(Weathers.SUNLIGHT) || battle.weather.hasWeather(Weathers.HEAVYSUNLIGHT)) {
            return false;
        }
        if(getTeam().effectTeamMoves.get(1) > 0 && !selfCaused && !other.hasAbility("INFILTRATOR")) { // safeguard
            return false;
        }
        return true;
    }

    public boolean canConfuse(boolean selfCaused, Pokemon other) {
        //TODO: conditions for confusion
        if(hasTemporalStatus(TemporalStatus.CONFUSED) || battle == null) {
            return false;
        }
        if(hasAbility("OWNTEMPO")) {
            if(other != null) {
                if(!other.hasAbility("MOLDBREAKER")) {
                    return false;
                } else {
                    return true;
                }
            }
            return false;
        }
        if(battle.terrain.hasTerrain(TerrainTypes.MISTY) && !isLevitating()) {
            return false;
        }
        if(other != null) {
            if(getTeam().effectTeamMoves.get(1) > 0 && !selfCaused && !other.hasAbility("INFILTRATOR")) { // safeguard
                return false;
            }
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
            if(!other.hasAbility("MOLDBREAKER")) {
                return false;
            } else {
                return true;
            }
        }
        if(getTeam().effectTeamMoves.get(1) > 0 && !selfCaused && !other.hasAbility("INFILTRATOR")) { // safeguard
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

    public boolean canFlinch(Pokemon other) {
        //TODO: conditions for flinch
        if(hasTemporalStatus(TemporalStatus.FLINCHED)) {
            return false;
        }
        if(hasAbility("INNERFOCUS")) {
            if(other != null) {
                if(!other.hasAbility("MOLDBREAKER")) {
                    return false;
                } else {
                    return true;
                }
            }
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
        if(effectMoves.get(36) > 0 || effectMoves.get(46) > 0) { // Pokemon underground/undersea are not affected
            return false;
        }
        return true;
    }

    public boolean affectHail() {
        if(hasType("ICE") || hasItem("SAFETYGOOGLES")) {
            return false;
        }
        if(hasAbility("SNOWCLOAK") || hasAbility("ICEBODY") || hasAbility("MAGICGUARD") || hasAbility("SLUSHRUSH") || hasAbility("OVERCOAT")) {
            return false;
        }
        if(effectMoves.get(36) > 0 || effectMoves.get(46) > 0) { // Pokemon underground/undersea are not affected
            return false;
        }
        return true;
    }
    public boolean isLevitating() {
        if(hasItem("IRONBALL") || battle.effectFieldMoves.get(2) > 0) { // iron ball and ingrain have priority
            return false;
        }
        if(effectMoves.get(43) > 0) { // smack down
            return false;
        }
        if(effectMoves.get(42) > 0 || effectMoves.get(45) > 0) { // telekinesis, magnet rise
            return true;
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
        if(!canPoison(false, null)) {
            return false;
        }
        if(isLevitating()) {
            return false;
        }
        return true;
    }

    public int stealthRockDamage() {
        double damage = 12.5;
        if(battleType1.weaknesses.contains("ROCK")) {
            damage *= 2;
        } else if(battleType1.resistances.contains("ROCK")) {
            damage /= 2;
        }
        if(battleType2.weaknesses.contains("ROCK")) {
            damage *= 2;
        } else if(battleType2.resistances.contains("ROCK")) {
            damage /= 2;
        }

        return (int) ((damage/100)*getStats().get(0));
    }

    public boolean shareTypes(Pokemon other) {
        if(other.hasType(specie.type1.getInternalName())) {
            return true;
        }
        if(specie.type2 != null) {
            if(other.hasType(specie.type2.getInternalName())) {
                return true;
            }
        }
        return false;
    }

    public void multiType() {
        if(hasAbility("MULTITYPE") && canUseItem()) {
            if(hasItem("IRONPLATE")) changeType("STEEL","");
            if(hasItem("SPLASHPLATE")) changeType("WATER","");
            if(hasItem("INSECTPLATE")) changeType("BUG","");
            if(hasItem("DRACOPLATE")) changeType("DRAGON","");
            if(hasItem("ZAPPLATE")) changeType("ELECTRIC","");
            if(hasItem("SPOOKYPLATE")) changeType("GHOST","");
            if(hasItem("FLAMEPLATE")) changeType("FIRE","");
            if(hasItem("PIXIEPLATE")) changeType("FAIRY","");
            if(hasItem("ICICLEPLATE")) changeType("ICE","");
            if(hasItem("FISTPLATE")) changeType("FIGHTING","");
            if(hasItem("MEADOWPLATE")) changeType("GRASS","");
            if(hasItem("MINDPLATE")) changeType("PSYCHIC","");
            if(hasItem("STONEPLATE")) changeType("ROCK","");
            if(hasItem("DREADPLATE")) changeType("DARK","");
            if(hasItem("EARTHPLATE")) changeType("GROUND","");
            if(hasItem("TOXICPLATE")) changeType("POISON","");
            if(hasItem("SKYPLATE")) changeType("FLYING","");
        }
    }

    public Type getHiddenPowerType() {
        Type type;
        int t = 0;
        for(int i=0;i<ivs.size();i++) {
            if(ivs.get(i)%2==1) t += Math.pow(2,i);
        }
        int value = (t*15)/63;

        type = switch (value) {
            case 0 -> utils.getType("FIGHTING");
            case 1 -> utils.getType("FLYING");
            case 2 -> utils.getType("POISON");
            case 3 -> utils.getType("GROUND");
            case 4 -> utils.getType("ROCK");
            case 5 -> utils.getType("BUG");
            case 6 -> utils.getType("GHOST");
            case 7 -> utils.getType("STEEL");
            case 8 -> utils.getType("FIRE");
            case 9 -> utils.getType("WATER");
            case 10 -> utils.getType("GRASS");
            case 11 -> utils.getType("ELECTRIC");
            case 12 -> utils.getType("PSYCHIC");
            case 13 -> utils.getType("ICE");
            case 14 -> utils.getType("DRAGON");
            default -> utils.getType("DARK");
        };
        return type;
    }

    public Movement sleepTalkMove() {
        ArrayList<Movement> posibleMoves = new ArrayList<>();
        for(int i=0;i<moves.size();i++) {
            Movement m = moves.get(i).getMove();
            if(!disabledMove.equals(m) && !cursedBodyMove.equals(m) && !battle.moveEffects.attacksForbiddenBySleepTalk.contains(m.getCode())) {
                posibleMoves.add(m);
            }
        }

        if(posibleMoves.isEmpty()) { return null; }
        return posibleMoves.get(random.nextInt(posibleMoves.size()));
    }

    public void changeTruant() {
        if(hasAbility("TRUANT")) {
            truant = !truant;
        } else {
            truant = false;
        }
    }

    public void rapidSpin() {
        if(hasTemporalStatus(TemporalStatus.PARTIALLYTRAPPED)) {
            healTempStatus(TemporalStatus.PARTIALLYTRAPPED, false);
            System.out.println(nickname + " was freed!");
            effectMoves.set(4, 0); // fire spin
            effectMoves.set(16, 0); // wrap
            effectMoves.set(21, 0); // sand tomb
            effectMoves.set(49, 0); // clamp
            effectMoves.set(50, 0); // whirlpool
            effectMoves.set(51, 0); // bind
            //TODO: rest of partially trapped moves
        }
        if(getTeam().effectTeamMoves.get(3) > 0) {
            getTeam().removeTeamEffects(this,3); // remove toxic spikes
        }
        if(getTeam().effectTeamMoves.get(14) > 0) {
            getTeam().removeTeamEffects(this,14); // remove stealth rock
        }
        if(getTeam().effectTeamMoves.get(15) > 0) {
            getTeam().removeTeamEffects(this,15); // remove spikes
        }
        if(getTeam().effectTeamMoves.get(16) > 0) {
            getTeam().removeTeamEffects(this,16); // remove sticky web
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

    public boolean hasAnticipationMove(Pokemon other) {
        for(int i=0;i<moves.size();i++) {
            Movement mv = moves.get(i).getMove();
            if(mv.getCode() == 133) return true;
            if((mv.getPower() != 0 || mv.getCode() == 231 || mv.getCode() == 35 || mv.getCode() == 46) && battle.getEffectiveness(this,other,mv,false) >= 2.0) return true;
        }
        return false;
    }

    public Movement getMorePowerfulMove() {
        Movement move = getMoves().get(random.nextInt(getMoves().size())).getMove();
        int power = move.getPower();
        for(int i=0;i<getMoves().size();i++) {
            Movement currentMove = getMoves().get(i).getMove();
            if(currentMove.getPower() == -1) {
                if(currentMove.getCode() == 133) power = 160; // OHKO moves
                else if(currentMove.getCode() == 54) power = 150; // water spout, eruption
                else if(currentMove.hasName("COUNTER") || currentMove.hasName("MIRRORCOAT") || currentMove.hasName("METALBURST")) power = 120;
                else if(currentMove.hasName("CRUSHGRIP") || currentMove.hasName("TRUMPCARD") || currentMove.hasName("FLAIL") ||
                        currentMove.hasName("SONICBOOM") || currentMove.hasName("NATURALGIFT") || currentMove.hasName("ENDEAVOR") ||
                        currentMove.hasName("WRINGOUT") || currentMove.hasName("FRUSTRATION") || currentMove.hasName("DRAGONRAGE") ||
                        currentMove.hasName("GYROBALL") || currentMove.hasName("GRASSKNOT") || currentMove.hasName("REVERSAL") ||
                        currentMove.hasName("SEISMICTOSS") || currentMove.hasName("LOWKICK") || currentMove.hasName("HIDDENPOWER") ||
                        currentMove.hasName("PSYWAVE") || currentMove.hasName("RETURN") || currentMove.hasName("NIGHTSHADE")) power = 80;
                else power = 1;
            }
            if(currentMove.getPower() > power) {
               power = currentMove.getPower();
               move = currentMove;
           }
        }
        return move;
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
