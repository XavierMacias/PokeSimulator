package PokeData;

import java.io.File;
import java.io.FileNotFoundException;

import PokeData.*;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.*;

public class Utils {
    private ArrayList<Type> types;
    private ArrayList<Movement> moves;
    private ArrayList<Ability> abilities;
    private ArrayList<Item> items;
    private ArrayList<Specie> species;
    public Utils() {
        types = new ArrayList<Type>();
        moves = new ArrayList<Movement>();
        abilities = new ArrayList<Ability>();
        items = new ArrayList<Item>();
        species = new ArrayList<Specie>();
    }

    public void addTypes() {
        int i = 0;
        String t = "";
        try {
            File myObj = new File("TYPES.txt");
            Scanner myReader = new Scanner(myObj,"iso-8859-1");
            while (myReader.hasNextLine()) {
                String[] data = myReader.nextLine().split(",");
                if(i%4==0) {
                    // type info
                    Type type = new Type(data[0],data[1]);
                    types.add(type);
                    t = type.getInternalName();
                } else if(i%4==1) {
                    // type weaknesses
                    if(getType(t) != null) {
                        List<String> w = new ArrayList<String>();
                        for(int j=0;j<data.length;j++) {
                            w.add(data[j]);
                        }
                        getType(t).setWeaknesses(w);
                    }
                } else if(i%4==2) {
                    // type resistances
                    if(getType(t) != null) {
                        List<String> r = new ArrayList<String>();
                        for(int j=0;j<data.length;j++) {
                            r.add(data[j]);
                        }
                        getType(t).setResistances(r);
                    }
                } else if(i%4==3) {
                    // type immunities
                    if(getType(t) != null) {
                        List<String> im = new ArrayList<String>();
                        for(int j=0;j<data.length;j++) {
                            im.add(data[j]);
                        }
                        getType(t).setImmunities(im);
                    }
                }
            i++;
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void addAbilities() {
        try {
            File myObj = new File("ABILITIES.txt");
            Scanner myReader = new Scanner(myObj,"iso-8859-1");
            while (myReader.hasNextLine()) {
                String[] data = myReader.nextLine().split(",");
                Ability ability = new Ability(data[0], data[1], data[2]);
                abilities.add(ability);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
    public void addItems() {
        try {
            File myObj = new File("ITEMS.txt");
            Scanner myReader = new Scanner(myObj,"iso-8859-1");
            while (myReader.hasNextLine()) {
                String[] data = myReader.nextLine().split(",");
                Item item = new Item(data[0], data[1], Pocket.valueOf(data[2]),Double.valueOf(data[3]),FieldUse.valueOf(data[4]),
                        BattleUse.valueOf(data[5]), Boolean.valueOf(data[6]), data[7], data[8], existsMove(data[9]));
                items.add(item);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void addMoves() {
        try {
            File myObj = new File("MOVES.txt");
            Scanner myReader = new Scanner(myObj,"iso-8859-1");
            while (myReader.hasNextLine()) {
                String[] data = myReader.nextLine().split(",");
                Movement movement = new Movement(data[0],data[1],getType(data[2]),Integer.parseInt(data[3]),
                        Integer.parseInt(data[4]), Category.valueOf(data[5]),Integer.parseInt(data[6]),
                        Integer.parseInt(data[7]),Target.valueOf(data[8]),Integer.parseInt(data[9]),data[10],
                        Integer.parseInt(data[11]),data[12]);
                moves.add(movement);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void addSpecies() {
        int i = 0;
        String p = "";
        try {
            File myObj = new File("POKEMON.txt");
            Scanner myReader = new Scanner(myObj,"iso-8859-1");
            while (myReader.hasNextLine()) {
                String[] data = myReader.nextLine().split(",");
                if(i%3==0) {
                    // specie info
                    //System.out.println(data[1]);
                    List<Integer> st = new ArrayList<Integer>();
                    for(int j=0;j<6;j++) {
                        st.add(Integer.parseInt(data[5+j]));
                    }
                    List<Integer> evs = new ArrayList<Integer>();
                    for(int j=0;j<6;j++) {
                        evs.add(Integer.parseInt(data[18+j]));
                    }
                    List<Evolution> evos = new ArrayList<Evolution>();
                    for(int k=31;k<data.length;k+=3) {
                        Evolution e = new Evolution(data[k],data[k+1],data[k+2]);
                        evos.add(e);
                    }
                    EggGroups eggGroup2;
                    if(Objects.equals(data[27], "")) {
                        eggGroup2 = null;
                    } else {
                        eggGroup2 =  EggGroups.valueOf(data[27]);
                    }
                    Specie specie = new Specie(Integer.parseInt(data[0]),data[1],data[2],getType(data[3]),getType(data[4]),
                            st,getAbility(data[11]),getAbility(data[12]),getAbility(data[13]),Integer.parseInt(data[14]),
                            Integer.parseInt(data[15]),Integer.parseInt(data[16]),Float.parseFloat(data[17]),evs,
                            Integer.parseInt(data[24]), GrowthRate.valueOf(data[25]), EggGroups.valueOf(data[26]),
                            eggGroup2, Float.parseFloat(data[28]), Float.parseFloat(data[29]),data[30],evos);
                    species.add(specie);
                    p = specie.getInternalName();
                    Form form0 = new Form(0,data[2],Integer.parseInt(data[0]),data[1],data[2],getType(data[3]),getType(data[4]),
                            st,getAbility(data[11]),getAbility(data[12]),getAbility(data[13]),Integer.parseInt(data[14]),
                            Integer.parseInt(data[15]),Integer.parseInt(data[16]),Float.parseFloat(data[17]),evs,
                            Integer.parseInt(data[24]), GrowthRate.valueOf(data[25]), EggGroups.valueOf(data[26]),
                            eggGroup2, Float.parseFloat(data[28]), Float.parseFloat(data[29]),data[30],evos);
                    specie.formLists.add(form0);
                } else if(i%3==1) {
                    // moveset
                    Multimap<Integer, Movement> mvs = ArrayListMultimap.create();
                    for(int k=0;k<data.length;k+=2) {
                        mvs.put(Integer.parseInt(data[k]),existsMove(data[k+1]));
                    }
                    if(getPokemon(p) != null) {
                        getPokemon(p).setMoveset(mvs);
                        getPokemon(p).formLists.get(0).setMoveset(mvs);
                    }
                } else if(i%3==2) {
                    // egg moves
                    List<Movement> egg = new ArrayList<Movement>();
                    for(int k=0;k<data.length;k++) {
                        egg.add(existsMove(data[k]));
                    }
                    if(getPokemon(p) != null) {
                        getPokemon(p).setEggMoves(egg);
                        getPokemon(p).formLists.get(0).setEggMoves(egg);
                    }
                }
                i++;
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        addTMCompatibility();
        addForms();
    }

    public void addForms() {
        int i = 0;
        String p = "";
        try {
            File myObj = new File("FORMS.txt");
            Scanner myReader = new Scanner(myObj,"iso-8859-1");
            int line = 0;
            int index = 0;
            Specie poke = null;
            Form form = null;
            while (myReader.hasNextLine()) {
                String[] data = myReader.nextLine().split(",");
                if(getPokemon(data[0]) != null) {
                    poke = getPokemon(data[0]);
                    line = 0;
                    index = 0;
                    poke.formLists.get(0).formName = data[1];
                } else {
                    if(line == 0) {
                        // specie info
                        List<Integer> st = new ArrayList<Integer>();
                        for(int j=0;j<6;j++) {
                            st.add(Integer.parseInt(data[5+j]));
                        }
                        List<Integer> evs = new ArrayList<Integer>();
                        for(int j=0;j<6;j++) {
                            evs.add(Integer.parseInt(data[18+j]));
                        }
                        List<Evolution> evos = new ArrayList<Evolution>();
                        for(int k=31;k<data.length;k+=3) {
                            Evolution e = new Evolution(data[k],data[k+1],data[k+2]);
                            evos.add(e);
                        }
                        EggGroups eggGroup2;
                        if(Objects.equals(data[27], "")) {
                            eggGroup2 = null;
                        } else {
                            eggGroup2 =  EggGroups.valueOf(data[27]);
                        }
                        form = new Form(index,data[0],poke.number,poke.getInternalName(),poke.name,getType(data[3]),getType(data[4]),
                                st,getAbility(data[11]),getAbility(data[12]),getAbility(data[13]),Integer.parseInt(data[14]),
                                Integer.parseInt(data[15]),Integer.parseInt(data[16]),Float.parseFloat(data[17]),evs,
                                Integer.parseInt(data[24]), GrowthRate.valueOf(data[25]), EggGroups.valueOf(data[26]),
                                eggGroup2, Float.parseFloat(data[28]), Float.parseFloat(data[29]),data[30],evos);
                        poke.formLists.add(form);
                        line++;
                    } else if(line == 1) {
                        // moveset
                        Multimap<Integer, Movement> mvs = ArrayListMultimap.create();
                        for(int k=0;k<data.length;k+=2) {
                            mvs.put(Integer.parseInt(data[k]),existsMove(data[k+1]));
                        }
                        form.setMoveset(mvs);
                        line++;
                    } else if(line == 2) {
                        // egg moves
                        List<Movement> egg = new ArrayList<Movement>();
                        for(int k=0;k<data.length;k++) {
                            egg.add(existsMove(data[k]));
                        }
                        form.setEggMoves(egg);
                        line = 0;
                        index++;
                    }
                }
                i++;
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addTMCompatibility() {
        try {
            File myObj = new File("COMPATIBLE.txt");
            int i = 0;
            Scanner myReader = new Scanner(myObj,"iso-8859-1");
            Movement mv = null;
            while (myReader.hasNextLine()) {
                String[] data = myReader.nextLine().split(",");
                if(i%2==0) {
                    mv = existsMove(data[0]);
                } else if(i%2 == 1) {
                    ArrayList<String> compatiblePokes = new ArrayList<>();
                    for(int k=0;k<data.length;k++) {
                        compatiblePokes.add(data[k]);
                    }
                    if(mv != null) mv.setCompatibleTM(compatiblePokes);
                }
                i++;
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Type getType(String t) {
        for(int i=0;i<types.size();i++) {
            if(types.get(i).getInternalName().equals(t)) {
                return types.get(i);
            }
        }
        return null;
    }

    public Ability getAbility(String a) throws Exception {
        if(a == null) {
            return null;
        }
        if(a.replaceAll("\\s+","").equals("")) {
            return null;
        }
        for(int i=0;i<abilities.size();i++) {
            if(abilities.get(i).getInternalName().equals(a)) {
                return abilities.get(i);
            }
        }
        throw new Exception("Ability " + a + " doesnt exist");
    }

    public Movement existsMove(String m) throws Exception {
        if(m == null) {
            return null;
        }
        if(m.replaceAll("\\s+","").equals("")) {
            return null;
        }
        for(int i=0;i<moves.size();i++) {
            if(moves.get(i).getInternalName().equals(m)) {
                return moves.get(i);
            }
        }
        throw new Exception("Move " + m + " doesnt exist");
    }

    public Movement getMove(String m) {
        if(m == null) {
            return null;
        }
        if(m.equals("")) {
            return null;
        }
        for(int i=0;i<moves.size();i++) {
            if(moves.get(i).getInternalName().equals(m)) {
                return moves.get(i);
            }
        }
        return null;
    }

    public Specie getPokemon(String p) {
        for(int i=0;i<species.size();i++) {
            if(species.get(i).getInternalName().equals(p)) {
                return species.get(i);
            }
        }
        return null;
    }
    public Item getItem(String it) {
        for(int i=0;i<items.size();i++) {
            if(items.get(i).getInternalName().equals(it)) {
                return items.get(i);
            }
        }
        return null;
    }

    public Specie getPokemonByNumber(int n) {
        for(int i=0;i<species.size();i++) {
            if(species.get(i).number == n) {
                return species.get(i);
            }
        }
        return null;
    }

    public int getRandomNumberBetween(int min, int max) { // min is inclusive, max not
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    public ArrayList<Type> getTypes() {
        return types;
    }

    public ArrayList<Movement> getMoves() {
        return moves;
    }

    public ArrayList<Ability> getAbilities() {
        return abilities;
    }

    public ArrayList<Specie> getSpecies() {
        return species;
    }
    public ArrayList<Item> getItems() {
        return items;
    }
}
