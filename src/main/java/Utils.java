import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;

import PokeData.Ability;
import PokeData.Movement;
import PokeData.Specie;
import PokeData.Type;
import org.checkerframework.common.value.qual.EnsuresMinLenIf;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Utils {

    private ArrayList<Type> types;
    private ArrayList<Movement> moves;
    private ArrayList<Ability> abilities;
    private ArrayList<Specie> species;
    public Utils() {
        types = new ArrayList<Type>();
        moves = new ArrayList<Movement>();
        abilities = new ArrayList<Ability>();
        species = new ArrayList<Specie>();
    }

    public void addTypes() {
        int i = 0;
        String t = "";
        try {
            File myObj = new File("TYPES.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String[] data = myReader.nextLine().split(",");
                if(i%4==0) {
                    Type type = new Type(data[0],data[1]);
                    types.add(type);
                    t = type.getInternalName();
                } else if(i%4==1) {
                    if(getType(t) != null) {
                        List<String> w = new ArrayList<String>();
                        for(int j=0;j<data.length;j++) {
                            w.add(data[j]);
                        }
                        getType(t).setWeaknesses(w);
                    }
                } else if(i%4==2) {
                    if(getType(t) != null) {
                        List<String> r = new ArrayList<String>();
                        for(int j=0;j<data.length;j++) {
                            r.add(data[j]);
                        }
                        getType(t).setResistances(r);
                    }
                } else if(i%4==3) {
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

    public Type getType(String t) {
        for(int i=0;i<types.size();i++) {
            if(types.get(i).getInternalName() == t) {
                return types.get(i);
            }
        }
        return null;
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
}
