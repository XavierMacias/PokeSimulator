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

    public void openBag(boolean outsideBattle) {
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
            openPocket(Integer.parseInt(pocketIndex),outsideBattle);
        } else if(Integer.parseInt(pocketIndex) != 0) {
            openBag(outsideBattle);
        }

    }

    public void openPocket(int pocket, boolean outsideBattle) {
        ArrayList<Item> listItems;
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
                itemsNames += it.name;
            }
        }
        itemIndex = in.nextLine();
        if(Integer.parseInt(itemIndex) > 0 && Integer.parseInt(itemIndex) <= listItems.size()) {
            Item it = listItems.get(Integer.parseInt(itemIndex)-1);
            interactItem(it, outsideBattle, pocket);
        } else if(Integer.parseInt(itemIndex) != 0) {
            openPocket(pocket,outsideBattle);
        } else {
            openBag(outsideBattle);
        }
    }

    private void interactItem(Item it, boolean outsideBattle, int pocket) {
        String itemIndex = "-1";
        int i = 1;
        if(outsideBattle) {
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
                        giveItem(it, pocket);
                        break;
                    case "2":
                        // toss
                        tossItem(it, pocket);
                        break;
                    case "3":
                        // use
                        //TODO: use item
                        break;
                }
                openPocket(pocket,outsideBattle);
            } else if(Integer.parseInt(itemIndex) != 0) {
                interactItem(it, outsideBattle, pocket);
            } else {
                openPocket(pocket,outsideBattle);
            }


        } else {
            //TODO: items inside battle
        }
    }

    private void giveItem(Item newItem, int pocket) {
        System.out.println("Which do you want to give the " + newItem.name + " to? ");
        Pokemon poke = player.getTeam().selectPokemon();
        if(poke != null) {
            if(poke.item != null) {
                System.out.println(poke.nickname + " already has "+ poke.item.name + "!");
                System.out.println("Do you want switch the items?");
                System.out.println("1: Yes\n2: No");
                if(in.nextLine().equals("1")) {
                    System.out.println("You switched "+poke.item.name+" by " + newItem.name + "!");
                    addItem(poke.item, false);
                    poke.item = newItem;
                    getPocket(pocket).remove(newItem);
                }
            } else {
                poke.item = newItem;
                getPocket(pocket).remove(newItem);
                System.out.println("You gave "+newItem.name+" to " + poke.nickname + "!");
            }
        }

    }
    private void tossItem(Item item, int pocket) {
        System.out.println("Would you like to toss "+item.name+"?");
        System.out.println("1: Yes\n2: No");
        if(in.nextLine().equals("1") && getPocket(pocket).contains(item)) {
            System.out.println("You tossed "+item.name+"!");
            getPocket(pocket).remove(item);
        }
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
