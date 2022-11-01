package PokeData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class Bag {
    // pockets
    private ArrayList<Item> items;
    private ArrayList<Item> medicine;
    private ArrayList<Item> pokeballs;
    private ArrayList<Item> berries;
    private ArrayList<Item> battleitems;
    private ArrayList<Item> mail;
    private ArrayList<Item> keyitems;
    private ArrayList<Item> mts;
    private Player player;
    Scanner in;

    public Bag(Player pl) {
        items = new ArrayList<>();
        medicine = new ArrayList<>();
        pokeballs = new ArrayList<>();
        berries = new ArrayList<>();
        battleitems = new ArrayList<>();
        mts = new ArrayList<>();
        mail = new ArrayList<>();
        keyitems = new ArrayList<>();
        player = pl;

        in = new Scanner(System.in);
    }

    public boolean openBag(boolean outsideBattle, Pokemon poke) {
        System.out.println("Choose the Pocket");
        String pocketIndex = "-1";
        System.out.println("0: Back");
        System.out.println("1: Items");
        System.out.println("2: Medicine");
        System.out.println("3: Poké Balls");
        System.out.println("4: TM/HMs");
        System.out.println("5: Berries");
        System.out.println("6: Mail");
        System.out.println("7: Battle items");
        System.out.println("8: Key items");

        pocketIndex = in.nextLine();

        if(Integer.parseInt(pocketIndex) > 0 && Integer.parseInt(pocketIndex) <= 8) {
            return openPocket(Integer.parseInt(pocketIndex),outsideBattle, poke);
        } else if(Integer.parseInt(pocketIndex) != 0) {
            return openBag(outsideBattle, poke);
        }
        return false;
    }

    public boolean openPocket(int pocket, boolean outsideBattle, Pokemon pokemon) {
        ArrayList<Item> listItems;
        ArrayList<Item> itemPack = new ArrayList<>();
        switch(pocket) {
            case 1:
                System.out.println("Items Pocket: ");
                listItems = items;
                break;
            case 2:
                System.out.println("Medicine Pocket: ");
                listItems = medicine;
                break;
            case 3:
                System.out.println("Poké Balls Pocket: ");
                listItems = pokeballs;
                break;
            case 4:
                System.out.println("TM/HMs Pocket: ");
                listItems = mts;
                break;
            case 5:
                System.out.println("Berries Pocket: ");
                listItems = berries;
                break;
            case 6:
                System.out.println("Mail Pocket: ");
                listItems = mail;
                break;
            case 7:
                System.out.println("Battle Items Pocket: ");
                listItems = battleitems;
                break;
            default:
                System.out.println("Key Items Pocket: ");
                listItems = keyitems;
                break;
        }
        String itemIndex = "-1";
        String itemsNames = "";
        System.out.println("0: Exit");
        int ind = 1;
        for(int i=0;i<listItems.size();i++) {
            Item it = listItems.get(i);
            if(!itemsNames.contains(it.name)) {
                System.out.println(ind + ": " + it.name + " x" + Collections.frequency(listItems, it));
                ind++;
                itemPack.add(it);
                itemsNames += it.name;
            }
        }
        itemIndex = in.nextLine();
        if(Integer.parseInt(itemIndex) > 0 && Integer.parseInt(itemIndex) <= itemPack.size()) {
            Item it = itemPack.get(Integer.parseInt(itemIndex)-1);
            return interactItem(it, outsideBattle, pocket, pokemon);
        } else if(Integer.parseInt(itemIndex) != 0) {
            return openPocket(pocket,outsideBattle, pokemon);
        } else {
            return openBag(outsideBattle, pokemon);
        }
    }

    private boolean interactItem(Item it, boolean outsideBattle, int pocket, Pokemon pokemon) {
        String itemIndex = "-1";
        int i = 1;
        if(pokemon != null) {
            giveItem(it, pocket, pokemon);
            return true;
        }
        if(outsideBattle) {
            System.out.println(it.name + ": ");
            System.out.println(it.description);
            System.out.println("0: Exit");
            System.out.println(i+": Give");
            i++;
            System.out.println(i+": Toss");
            i++;
            if(!it.getFieldUse().toString().equals("NOFIELDUSE")) {
                System.out.println(i+": Use");
                i++;
            }
            itemIndex = in.nextLine();
            if(Integer.parseInt(itemIndex) > 0 && Integer.parseInt(itemIndex) < i) {
                switch(itemIndex) {
                    case "1":
                        // give
                        giveItem(it, pocket, pokemon);
                        break;
                    case "2":
                        // toss
                        tossItem(it, pocket);
                        break;
                    case "3":
                        // use
                        if(useItemOutside(it)) {
                            loseItem(it, pocket, false);
                            return true;
                        }
                        break;
                }
                return openPocket(pocket,outsideBattle, pokemon);
            } else if(Integer.parseInt(itemIndex) != 0) {
                return interactItem(it, outsideBattle, pocket, pokemon);
            } else {
                return openPocket(pocket,outsideBattle, pokemon);
            }
        } else {
            System.out.println(it.name + ": ");
            System.out.println("0: Exit");
            if(!it.getBattleUse().toString().equals("NOBATTLEUSE")) {
                System.out.println(i+": Use");
                //i++;
            }
            itemIndex = in.nextLine();
            if(itemIndex.equals("1")) {
                if (useItemInside(it)) {
                    loseItem(it, pocket, false);
                    return true;
                }
                return openPocket(pocket,outsideBattle, pokemon);
            } else if(Integer.parseInt(itemIndex) != 0) {
                return interactItem(it, outsideBattle, pocket, pokemon);
            } else {
                return openPocket(pocket,outsideBattle, pokemon);
            }
        }
    }

    private boolean useItemInside(Item item) {
        if(item.getFlags().contains("b")) { // pokeballs
            player.getTeam().getFirstAlivePokemon().battle.usePokeball(item);
            return true;
        } else if(item.getBattleUse().toString().equals("INPOKEMON")) {
            System.out.println("Which do you want to use " + item.name + " to? ");
            Pokemon poke = player.getTeam().selectPokemon();
            if(poke != null) {
                //System.out.println("You use "+ item.name+" to " + poke.nickname + "!");
                if(item.getPocket().toString().equals("MEDICINE") || item.getFlags().contains("c")) { // medicine, berries
                    return item.useMedicine(poke);
                }
            }
        } else if(item.getBattleUse().toString().equals("ONBATTLER")) {
            return item.useBattleItem(player.getTeam().getFirstAlivePokemon().battle.getUser());
        } else if(item.getBattleUse().toString().equals("NOTARGET")) {
            // TODO: things like poke doll
        }
        return false;
    }

    private boolean useItemOutside(Item item) {
        if(item.getFieldUse().toString().equals("DIRECT")) {
            if(item.getFlags().contains("i")) { // repels
                // TODO: repel items
            } else if(item.hasName("SACREDASH")) {
                boolean used = false;
                for(int i=0;i<player.getTeam().getPokemonTeam().size();i++) {
                    if(player.getTeam().getPokemonTeam().get(i).isFainted()) {
                        player.getTeam().getPokemonTeam().get(i).revivePokemon(-1);
                        used = true;
                    }
                }
                return used;
            }
            System.out.println("You use "+ item.name+" !");
        } else if(item.getFieldUse().toString().equals("ONPOKEMON")) {
            System.out.println("Which do you want to use " + item.name + " to? ");
            Pokemon poke = player.getTeam().selectPokemon();
            if(poke != null) {
                if(item.getFlags().contains("e")) { // evolution stone
                    boolean found = false;
                    for(int i=0;i<poke.specie.evos.size();i++) {
                        Evolution evos = poke.specie.evos.get(i);
                        if(evos.complement.equals(item.internalName) && evos.method.equals("Item")) {
                            poke.evolve(poke.utils.getPokemon(evos.evo));
                            found = true;
                        }
                    }
                    if(!found) {
                        System.out.println(poke.nickname + " is not compatible with " + item.name);
                        return false;
                    }
                } else if(item.getPocket().toString().equals("MEDICINE") || item.getFlags().contains("c")) { // medicine and berries
                    return item.useMedicine(poke);
                } else if(item.getInternalName().contains("NECTAR")) { // nectars
                    // TODO: nectars
                } else if(item.hasName("GRACIDEA")) { // gracidea
                    // TODO: gracidea
                } else if(item.hasName("DNASPLICERS")) { // dna splicers
                    // TODO: DNA splicers
                } else if(item.hasName("REVEALGLASS")) { // reveal glass
                    // TODO: reveal glass
                }
            }
        } else if(item.getFieldUse().toString().equals("TM") || item.getFieldUse().toString().equals("HM")) {
            System.out.println(item.name + " contains " + item.move.name + "!");
            System.out.println("Which do you want to learn " + item.move.name + "? ");
            Pokemon poke = player.getTeam().selectPokemon();
            if(poke != null) {
                return poke.learnMove(item.move);
            }
        }
        return false;
    }

    private void giveItem(Item newItem, int pocket, Pokemon pokemon) {
        if(pokemon != null) {
            addItemToPoke(newItem, pocket, pokemon);
        } else {
            System.out.println("Which do you want to give the " + newItem.name + " to? ");
            Pokemon poke = player.getTeam().selectPokemon();
            if(poke != null) {
                addItemToPoke(newItem, pocket, poke);
            }
        }
    }
    private void addItemToPoke(Item newItem, int pocket, Pokemon pokemon) {
        if(pokemon.item != null) {
            System.out.println(pokemon.nickname + " already has "+ pokemon.item.name + "!");
            System.out.println("Do you want switch the items?");
            System.out.println("1: Yes\n2: No");
            if(in.nextLine().equals("1")) {
                System.out.println("You switched "+pokemon.item.name+" by " + newItem.name + "!");
                addItem(pokemon.item, false);
                pokemon.item = newItem;
                loseItem(newItem, pocket, true);
            }
        } else {
            pokemon.item = newItem;
            loseItem(newItem, pocket, true);
            System.out.println("You gave "+newItem.name+" to " + pokemon.nickname + "!");
        }
    }

    private void tossItem(Item item, int pocket) {
        System.out.println("Would you like to toss "+item.name+"?");
        System.out.println("1: Yes\n2: No");
        if(in.nextLine().equals("1") && getPocket(pocket).contains(item)) {
            System.out.println("You tossed "+item.name+"!");
            loseItem(item, pocket, true);
        }
    }

    private void loseItem(Item item, int pocket, boolean toss) {
        if(item.consumable || toss) getPocket(pocket).remove(item);
    }

    public void addItem(Item item, boolean message) {
        switch(item.pocket.toString()) {
            case "ITEMS":
                items.add(item);
                if(message) System.out.println(item.name + " was saved in Items Pocket");
                break;
            case "MEDICINE":
                medicine.add(item);
                if(message) System.out.println(item.name + " was saved in Medicine Pocket");
                break;
            case "POKEBALLS":
                pokeballs.add(item);
                if(message) System.out.println(item.name + " was saved in Poké Balls Pocket");
                break;
            case "TMS":
                mts.add(item);
                if(message) System.out.println(item.name + " was saved in TM/HMs Pocket");
                break;
            case "BERRIES":
                berries.add(item);
                if(message) System.out.println(item.name + " was saved in Berries Pocket");
                break;
            case "MAIL":
                mail.add(item);
                if(message) System.out.println(item.name + " was saved in Mail Pocket");
                break;
            case "BATTLEITEMS":
                battleitems.add(item);
                if(message) System.out.println(item.name + " was saved in Battle Items Pocket");
                break;
            default:
                keyitems.add(item);
                if(message) System.out.println(item.name + " was saved in Key Items Pocket");
                break;
        }
    }

    private ArrayList<Item> getPocket(int pocket) {
        ArrayList<Item> pock;
        switch(pocket) {
            case 1:
                pock = items;
                break;
            case 2:
                pock = medicine;
                break;
            case 3:
                pock = pokeballs;
                break;
            case 4:
                pock = mts;
                break;
            case 5:
                pock = berries;
                break;
            case 6:
                pock = mail;
                break;
            case 7:
                pock = battleitems;
                break;
            default:
                pock = keyitems;
                break;
        }
        return pock;
    }
}
