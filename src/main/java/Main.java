public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome to Pokemon World!");

        Utils utils = new Utils();
        utils.addTypes(); // types added
        utils.addAbilities(); // abilities added
        utils.addMoves(); // moves added
        utils.addSpecies(); // species added

        for(int i=0;i<utils.getSpecies().size();i++) {
            System.out.println(utils.getSpecies().get(i).name+"\n");
        }

        /*for(int i=0;i<utils.getTypes().size();i++) {
            System.out.println(utils.getTypes().get(i).name+"\n");
            System.out.println(utils.getTypes().get(i).weaknesses.toString()+"\n");
            System.out.println(utils.getTypes().get(i).resistances.toString()+"\n");
            System.out.println(utils.getTypes().get(i).immunities.toString()+"\n-----------------\n");
        }*/
    }
}