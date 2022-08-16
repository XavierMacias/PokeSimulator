package PokeData;

import java.util.ArrayList;
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
    Scanner in;

    public Bag() {
        items = new ArrayList<>();
        medicine = new ArrayList<>();
        pokeballs = new ArrayList<>();
        berries = new ArrayList<>();
        battleitems = new ArrayList<>();
        mts = new ArrayList<>();
        mail = new ArrayList<>();
        keyitems = new ArrayList<>();

        in = new Scanner(System.in);
    }

    public void openBag() {
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
            openPocket(Integer.parseInt(pocketIndex));
        }

    }

    public void openPocket(int pocket) {
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
        System.out.println("0: Exit");
        for(int i=0;i<listItems.size();i++) {
            System.out.println((i+1) + ": " + listItems.get(i).name);
        }
        itemIndex = in.nextLine();
        if(Integer.parseInt(itemIndex) > 0 && Integer.parseInt(itemIndex) <= listItems.size()) {
            // interact item
        }
    }

    public void addItem(Item item) {
        switch(item.pocket.toString()) {
            case "ITEMS":
                items.add(item);
                System.out.println(item.name + " was saved in Items Pocket");
                break;
            case "MEDICINE":
                medicine.add(item);
                System.out.println(item.name + " was saved in Medicine Pocket");
                break;
            case "POKEBALLS":
                System.out.println(item.name + " was saved in Poké Balls Pocket");
                pokeballs.add(item);
                break;
            case "TMS":
                System.out.println(item.name + " was saved in TM/HMs Pocket");
                mts.add(item);
                break;
            case "BERRIES":
                System.out.println(item.name + " was saved in Berries Pocket");
                berries.add(item);
                break;
            case "MAIL":
                System.out.println(item.name + " was saved in Mail Pocket");
                mail.add(item);
                break;
            case "BATTLEITEMS":
                System.out.println(item.name + " was saved in Battle Items Pocket");
                battleitems.add(item);
                break;
            default:
                System.out.println(item.name + " was saved in Key Items Pocket");
                keyitems.add(item);
                break;
        }
    }
}
