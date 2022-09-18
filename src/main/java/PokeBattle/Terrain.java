package PokeBattle;

import PokeData.Movement;
import PokeData.Pokemon;

public class Terrain {
    private TerrainTypes terrain;
    private int turnCount = 0;
    private int maxTurns = -1;

    public Terrain() {
        terrain = TerrainTypes.NONE;
    }

    public TerrainTypes getTerrain() { return terrain; }

    public boolean hasTerrain(TerrainTypes t) {
        return terrain == t;
    }

    public boolean activateTerrain(Pokemon attacker, TerrainTypes t, boolean terrainExtend) {
        if(t == terrain) {
            return false;
        }

        String terrainName = "";
        switch (t.toString()) {
            case "GRASSY":
                terrain = TerrainTypes.GRASSY;
                terrainName = "Grassy Terrain";
                break;
            case "ELECTRIC":
                terrain = TerrainTypes.ELECTRIC;
                terrainName = "Electric Terrain";
                break;
            case "MISTY":
                terrain = TerrainTypes.MISTY;
                terrainName = "Misty Terrain";
                break;
            case "PSYCHIC":
                terrain = TerrainTypes.PSYCHIC;
                terrainName = "Psychic Terrain";
                break;
        }
        maxTurns = 5; if(terrainExtend) { maxTurns = 8; }
        turnCount = 0;

        System.out.println(attacker.nickname + " activated a " + terrainName + "!");
        return true;
    }

    public void endTerrain() {
        switch (terrain.toString()) {
            case "GRASSY":
                System.out.println("Grassy Terrain faded...");
                break;
            case "ELECTRIC":
                System.out.println("Electric Terrain faded...");
                break;
            case "MISTY":
                System.out.println("Misty Terrain faded...");
                break;
            case "PSYCHIC":
                System.out.println("Psychic Terrain faded...");
                break;
        }
        terrain = TerrainTypes.NONE;
        turnCount = 0;
    }

    public void increaseTurn() {
        turnCount++;
        if(turnCount == maxTurns) {
            endTerrain();
        }
    }
}
