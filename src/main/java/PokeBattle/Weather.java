package PokeBattle;

public class Weather {

    private Weathers weather;
    boolean natural;
    private int turnCount = 0;
    private int maxTurns = -1;
    private Battle battle;

    public Weather(Battle b) {
        weather = Weathers.CLEARSKIES;
        natural = false;
        battle = b;
    }

    public Weathers getWeather() {
        return weather;
    }

    public boolean hasWeather(Weathers w) {
        if(battle.hasCloudNine() && !w.equals(Weathers.CLEARSKIES)) {
            return false;
        }
        return weather == w;
    }

    public void endWeather() {
        switch (weather.toString()) {
            case "SUNLIGHT":
            case "HEAVYSUNLIGHT":
                System.out.println("The harsh sunlight faded.");
                break;
            case "RAIN":
                System.out.println("The rain stopped.");
                break;
            case "HEAVYRAIN":
                System.out.println("The heavy rain has lifted!");
                break;
            case "SANDSTORM":
                System.out.println("The sandstorm subsided.");
                break;
            case "HAIL":
                System.out.println("The hail stopped.");
                break;
            case "SNOW":
                System.out.println("The snow stopped.");
                break;
            case "FOG":
                System.out.println("The fog was wiped out.");
                break;
            case "STRONGWINDS":
                System.out.println("The mysterious air current has dissipated!");
                break;
        }
        weather = Weathers.CLEARSKIES;
        turnCount = 0;
    }

    public boolean changeWeather(Weathers w, boolean rock) {
        if(w == weather) {
            return false;
        }
        // strong winds, heavy sunlight and heavy rain only can change between them
        if(weather == Weathers.HEAVYSUNLIGHT && w != Weathers.HEAVYRAIN && w != Weathers.STRONGWINDS) {
            System.out.println("The extremely harsh sunlight was not lessened at all!");
            return true;
        }
        if(weather == Weathers.HEAVYRAIN && w != Weathers.HEAVYSUNLIGHT && w != Weathers.STRONGWINDS) {
            System.out.println("There is no relief from this heavy rain!");
            return true;
        }
        if(weather == Weathers.STRONGWINDS && w != Weathers.HEAVYSUNLIGHT && w != Weathers.HEAVYRAIN) {
            System.out.println("The mysterious air current blows on regardless!");
            return true;
        }

        switch (w.toString()) {
            case "CLEARSKIES":
                System.out.println("Weather backs to normal!");
                break;
            case "SUNLIGHT":
                System.out.println("The sunlight got bright!");
                break;
            case "HEAVYSUNLIGHT":
                System.out.println("The sunlight turned extremely harsh!");
                break;
            case "RAIN":
                System.out.println("It started to rain!");
                break;
            case "HEAVYRAIN":
                System.out.println("A heavy rain began to fall!");
                break;
            case "SANDSTORM":
                System.out.println("A sandstorm kicked up!");
                break;
            case "HAIL":
                System.out.println("It started to hail!");
                break;
            case "SNOW":
                System.out.println("It started to snow!");
                break;
            case "FOG":
                System.out.println("The fog is deep...");
                break;
            case "STRONGWINDS":
                System.out.println("A mysterious air current is protecting Flying-type Pokémon!");
                break;
        }
        weather = w;
        if(!natural) {
            maxTurns = 5;
            if(rock) maxTurns = 8;
        }

        turnCount = 0;
        return true;
    }

    public void increaseTurn() {
        turnCount++;
        if(turnCount == maxTurns) {
            endWeather();
        } else {
            switch (weather.toString()) {
                case "SUNLIGHT":
                    System.out.println("The sunlight is strong...");
                    break;
                case "RAIN":
                    System.out.println("Rain continues to fall.");
                    break;
                case "SANDSTORM":
                    System.out.println("The sandstorm rages...");
                    break;
                case "HAIL":
                    System.out.println("Hail continues to fall.");
                    break;
                case "SNOW":
                    System.out.println("Snow continues to fall.");
                    break;
                case "FOG":
                    System.out.println("The fog is deep...");
                    break;
            }
        }
    }
}
