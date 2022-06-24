package PokeData;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.*;
import java.util.stream.Collectors;

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
    Ability ability;
    List<Integer> statChanges; // attack, defense, sp att, sp def, speed, accuracy, evasion
    public int criticalIndex = 0;

    public Pokemon(Specie specie, int level, Utils utils) {
        Random random = new Random();
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
        // choose nature
        nature = natureList[random.nextInt(natureList.length)];
        // calculate experience
        experience = calcExperience(level);
        calcStats();
        psActuales = stats.get(0);
        // set initial moves
        moves = new ArrayList<Pair<Movement,Integer>>();
        remainPPs = new ArrayList<Integer>();
        setMoves();
        //is shiny?
        if(utils.getRandomNumberBetween(1,4097) == 1) {
            isShiny = true;
        }
        // alternative forms
        form = 0;
    }

    public List<Pair<Movement, Integer>> getMoves() {
        return moves;
    }
    public Specie getSpecie() { return specie; }

    public List<Integer> getRemainPPs() {
        return remainPPs;
    }

    public List<Integer> getStats() { return stats; }

    public int getAttack() { return stats.get(1); }
    public int getDefense() { return stats.get(2); }
    public int getSpecialAttack() { return stats.get(3); }
    public int getSpecialDefense() { return stats.get(4); }
    public int getVelocity() { return stats.get(5); }
    public int getHP() { return stats.get(0); }

    public int getLevel() {
        return level;
    }

    public List<Integer> getEvs() { return evs; }

    public boolean hasType(String type) {
        if(specie.type1 != null) {
            if(specie.type1.getInternalName() == type) {
                return true;
            }
        }

        if(specie.type2 != null) {
            if(specie.type2.getInternalName() == type) {
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

    public void setMoves() {
        List<Integer> keySet = specie.moveset.keySet().stream().sorted().toList(); // order the moves list by level
        Iterator keyIterator = keySet.iterator();
        while (keyIterator.hasNext()) {
            int key = (Integer) keyIterator.next();
            if(key <= level) { // check if level is equal or superior
                Iterator iterator = specie.moveset.get(key).iterator();
                while(iterator.hasNext()) {
                    Movement mv = (Movement)iterator.next();
                    if(!hasMove(mv.getInternalName())) { // if the pokemon hasnt that move
                        if(moves.size() < 4) {
                            // add move
                            moves.add(new Pair<>(mv,mv.getPP()));
                            remainPPs.add(mv.getPP());
                        } else {
                            // delete move
                            int rand = ((int)(Math.random() * 5));
                            if(rand < 4) {
                                moves.set(rand, new Pair<>(mv, mv.getPP()));
                                remainPPs.set(rand, mv.getPP());
                            }

                        }
                    }
                }
            }
        }
    }

    public void reducePP(Movement move) {
        int ind = getIndexMove(move.getInternalName());
        if(ind != -1) {
            remainPPs.set(ind,remainPPs.get(ind)-1);
        }
    }

    public void reduceHP(int damage) {
        psActuales -= damage;
        if(psActuales < 0) {
            psActuales = 0;
        }
    }

    public boolean hasPP(Movement move) {
        int ind = getIndexMove(move.getInternalName());
        return remainPPs.get(ind) > 0;
    }

    public boolean isOutPP() {
        for(int i=0;i<remainPPs.size();i++) {
            if(remainPPs.get(i) > 0) {
                return false;
            }
        }
        return true;
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
        stats = new ArrayList<Integer>();
        for(int i=0;i<6;i++) {
            if(i==0) {
                // HP stat
                stats.add((int) ((((specie.stats.get(0)*2)+ivs.get(0)+(evs.get(0)/4.0))*level)/100.0)+level+10);
            } else {
                // other stats
                stats.add((int) ((((((specie.stats.get(i)*2)+ivs.get(i)+(evs.get(i)/4.0))*level)/100.0)+5)*getNat(i)));
            }
        }
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
