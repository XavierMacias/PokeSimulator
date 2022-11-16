package PokeBattle;

import PokeData.*;

import java.util.*;

public class Battle {
    Team userTeam, rivalTeam;
    Pokemon user, rival, firstAttacker, secondAttacker;
    Movement firstMove, secondMove;
    private boolean endBattle;
    public int turn, c;
    int battleResult;
    public MoveEffects moveEffects;
    public Weather weather;
    public Terrain terrain;
    public List<Integer> effectFieldMoves;
    public Movement lastMoveUsed;
    Scanner in;
    Random random;
    //TODO: MOLD BREAKER ability affects: ICEFACE, FRIENDGUARD, MIRRORARMOR, DAZZLING, AROMAVEIL, DISGUISE, FLOWERGIFT, PUNKROCK
    //TODO: ICESCALES, FLOWERVEIL, SWEETVEIL, PASTELVEIL, TELEPATHY, SIMPLE, QUEENLYMAJESTY

    public Battle() {
        random = new Random();
        firstAttacker = null;
        secondAttacker = null;
        firstMove = null;
        c = 0;
        secondMove = null;
        in = new Scanner(System.in);
        moveEffects = new MoveEffects(this);
        weather = new Weather(this);
        terrain = new Terrain();
        lastMoveUsed = null;

        effectFieldMoves = new ArrayList<Integer>();
        /* 0 -> mud sport
           1 -> uproar
           2 -> gravity
           3 -> round
           4 -> water sport
           5 -> wonder room
           6 -> magic room
           7 -> trick room
           8 -> ion deluge
        */
        for(int i=0;i<9;i++) {
            effectFieldMoves.add(0);
        }
    }

    public Pokemon getUser() { return user; }
    public Pokemon getRival() { return rival; }

    public void WildSingleBattle(Team team1, Team team2) {
        team1.inBattle(this);
        team2.inBattle(this);
        battleResult = 0;
        turn = 1;
        endBattle = false;
        userTeam = team1;
        rivalTeam = team2;
        rival = team2.getFirstAlivePokemon();

        System.out.println("A wild "+rival.nickname +"!");
        user = team1.getFirstAlivePokemon();
        user.setParticipate(true);
        System.out.println("Go, "+user.nickname +"!");

        user.pokeTurn = 1;
        rival.pokeTurn = 1;
        String battleChoose = "0";
        //rival.setAbility("FLASHFIRE");
        //user.setItem("ROCKYHELMET");
        //user.setMove("TRIPLEKICK");
        //rival.setUniqueMove("RAGE");
        //rival.setItem("ORANBERRY");
        //weather.changeWeather(Weathers.HAIL, false);

        // battle loop
        do {
            boolean decision = false;

            outToFieldActivate();
            if(mustFight(user)) {
                decision = true;
                battleChoose = "1";
            } else {
                System.out.println("What "+ user.nickname + " should do?");
                System.out.println("1: Fight\n2: Bag\n3: Pokemon\n4: Run");

                battleChoose = in.nextLine();
            }

            switch (battleChoose) {
                case "1":
                    // fight
                    decision = fight();
                    break;
                case "2":
                    // bag
                    decision = useItem(false);
                    break;
                case "3":
                    // pokemon
                    decision = changePokemon(false);
                    break;
                case "4":
                    // run
                    decision = run();
                    if(decision) {
                        endBattle = true;
                    } else {
                        // rival attacks you
                        firstAttacker = user;
                        secondAttacker = rival;
                        firstMove = user.utils.getMove("STRUGGLE");
                        secondMove = chooseRivalMove();
                        useMove(secondAttacker,firstAttacker,secondMove,firstMove, true, false, false);
                        if(checkFaint() != 0) {
                            break;
                        }
                    }
                    break;
            }
            if(decision && !endBattle) {
                if(checkFaint() != 0) {
                    break;
                }
                fieldEffects();
                // end turn
                endTurn(firstAttacker,secondAttacker);
                if(checkFaint() != 0) {
                    break;
                }
                endTurn(secondAttacker,firstAttacker);
                if(checkFaint() != 0) {
                    break;
                }
                turn++;
            }
            //System.out.println("Turn: " + turn);
            //endBattle = true;
        } while(!endBattle);

        endBattle(false);
    }

    public void outToFieldActivate() {
        // TODO: this executes for speed priority
        // INTIMIDATE
        if(user.hasAbility("INTIMIDATE") && user.effectMoves.get(19) == 0) {
            if(rival.canIntimidate()) {
                System.out.println(user.nickname + " uses " + user.getAbility().name);
                rival.changeStat(0,-1,false,true, user);
                // rattled
                if(rival.hasAbility("RATTLED")) {
                    rival.changeStat(4,1,false,true, user);
                }
            }
            user.effectMoves.set(19, 1);
        }
        if(rival.hasAbility("INTIMIDATE") && rival.effectMoves.get(19) == 0) {
            if(user.canIntimidate()) {
                System.out.println(rival.nickname + " uses " + rival.getAbility().name);
                user.changeStat(0,-1,false,true, rival);
                // rattled
                if(user.hasAbility("RATTLED")) {
                    user.changeStat(4,1,false,true, rival);
                }
            }
            rival.effectMoves.set(19, 1);
        }
        // DROUGHT
        if(user.hasAbility("DROUGHT")) {
            weather.changeWeather(Weathers.SUNLIGHT,user.hasItem("HEATROCK"));
        }
        if(rival.hasAbility("DROUGHT")) {
            weather.changeWeather(Weathers.SUNLIGHT,rival.hasItem("HEATROCK"));
        }
        // SAND STREAM
        if(user.hasAbility("SANDSTREAM")) {
            weather.changeWeather(Weathers.SANDSTORM,user.hasItem("SMOOTHROCK"));
        }
        if(rival.hasAbility("SANDSTREAM")) {
            weather.changeWeather(Weathers.SANDSTORM,rival.hasItem("SMOOTHROCK"));
        }
        // DRIZZLE
        if(user.hasAbility("DRIZZLE")) {
            weather.changeWeather(Weathers.RAIN,user.hasItem("DAMPROCK"));
        }
        if(rival.hasAbility("DRIZZLE")) {
            weather.changeWeather(Weathers.RAIN,rival.hasItem("DAMPROCK"));
        }
        // SNOW WARNING
        if(user.hasAbility("SNOWWARNING")) {
            weather.changeWeather(Weathers.HAIL,user.hasItem("ICYROCK"));
        }
        if(rival.hasAbility("SNOWWARNING")) {
            weather.changeWeather(Weathers.HAIL,rival.hasItem("ICYROCK"));
        }
        // TRACE
        if(user.hasAbility("TRACE")) {
            moveEffects.changeAbility(user,rival);
        }
        if(rival.hasAbility("TRACE")) {
            moveEffects.changeAbility(rival,user);
        }
        // DOWNLOAD
        if(user.hasAbility("DOWNLOAD")) {
            if(rival.getStats().get(2)*rival.getStatChange(1) > rival.getStats().get(4)*rival.getStatChange(3)) {
                user.changeStat(0,1,false,true,rival);
            } else {
                user.changeStat(2,1,false,true,rival);
            }
        }
        if(rival.hasAbility("DOWNLOAD")) {
            if(user.getStats().get(2)*user.getStatChange(1) > user.getStats().get(4)*user.getStatChange(3)) {
                rival.changeStat(0,1,false,true,user);
            } else {
                rival.changeStat(2,1,false,true,user);
            }
        }

        // FRISK
        if(user.hasAbility("FRISK") && rival.item != null) {
            System.out.println(user.nickname + " frisked " + rival.item.name + "!");
        }
        if(rival.hasAbility("FRISK") && user.item != null) {
            System.out.println(rival.nickname + " frisked " + user.item.name + "!");
        }
        // ANTICIPATION
        if(user.hasAbility("ANTICIPATION") && rival.hasAnticipationMove(user)) {
            System.out.println(user.nickname + " is shuddered!");
        }
        if(rival.hasAbility("ANTICIPATION") && user.hasAnticipationMove(rival)) {
            System.out.println(rival.nickname + " is shuddered!");
        }
        // FOREWARN
        if(user.hasAbility("FOREWARN")) {
            System.out.println(user.nickname + " forewarned " + rival.getMorePowerfulMove().name + "!");
        }
        if(rival.hasAbility("FOREWARN")) {
            System.out.println(rival.nickname + " forewarned " + user.getMorePowerfulMove().name + "!");
        }
        // AIR BALLOON
        if(user.hasItem("AIRBALLOON")) {
            System.out.println(user.nickname + " has " + user.item.name);
        }
        if(rival.hasItem("AIRBALLOON")) {
            System.out.println(rival.nickname + " has " + rival.item.name);
        }
        // healing wish
        if(user.getTeam().effectTeamMoves.get(8) > 0 && (!user.hasAllHP() || !user.hasStatus(Status.FINE))) {
            System.out.println(user.nickname + " is affected by Healing Wish!");
            user.healHP(-1,true, false, false, true);
            user.healPermanentStatus();
            user.getTeam().effectTeamMoves.set(8, 0);
        }
        if(rival.getTeam().effectTeamMoves.get(8) > 0 && (!user.hasAllHP() || !user.hasStatus(Status.FINE))) {
            System.out.println(rival.nickname + " is affected by Healing Wish!");
            rival.healHP(-1,true, false, false, true);
            rival.healPermanentStatus();
            rival.getTeam().effectTeamMoves.set(8, 0);
        }
    }

    private boolean run() {
        c++;
        int a = user.getVelocity();
        int b = rival.getVelocity();
        if(b == 0) {
            b = 1;
        }
        int f = (((a*128)/b)+30*c)%256;

        if((user.utils.getRandomNumberBetween(0, 256) < f && canScape(user,rival)) || user.hasAbility("RUNAWAY") || user.hasType("GHOST")) {
            System.out.println("Run successfully!");
            return true;
        }
        System.out.println("You can't scape!");
        return false;
    }

    private boolean useItem(boolean fainted) {
        if(userTeam.getPlayer().getBag().openBag(false,null)) {
            firstAttacker = rival;
            secondAttacker = user;
            if(checkFaint() != 0) {
                return true;
            }
            Movement rivalMove = chooseRivalMove();
            rivalAttacksYou(fainted, rivalMove);
            if(checkFaint() != 0) {
                return true;
            }

            return true;
        }
        return false;
    }

    public void usePokeball(Item pokeball) {
        // TODO: for trainers you cant capture
        if(pokeball.hasName("MASTERBALL")) {
            capturedPokemon(rival, pokeball); // master ball ALWAYS capture
        } else {
            boolean critic = false;
            int psmax = rival.getHP();
            int psactual = rival.getPsActuales();
            double e = 1.0; // status modifier
            if(rival.hasStatus(Status.FROZEN) || rival.hasStatus(Status.ASLEEP)) {
                e = 2.5;
            } else if(rival.hasStatus(Status.BURNED) || rival.hasStatus(Status.PARALYZED) || rival.hasStatus(Status.POISONED)
                    || rival.hasStatus(Status.BADLYPOISONED)) {
                e = 1.5;
            }

            // TODO: LUREBALL, DUSKBALL, DIVEBALL
            int r = rival.getSpecie().ratio; // capture ratio
            double b = 1.0; // bonus for pokeball type
            if(pokeball.hasName("GREATBALL")) {
                b = 1.5;
            } else if(pokeball.hasName("ULTRABALL")) {
                b = 2;
            } else if(pokeball.hasName("ULTRABALL")) {
                b = 2;
            } else if(pokeball.hasName("RAPIDBALL") && rival.getSpecie().stats.get(5) >= 100) {
                b = 4;
            } else if(pokeball.hasName("LEVELBALL")) {
                if(user.getLevel() >= 4*rival.getLevel()) {
                    b = 8;
                } else if(user.getLevel() >= 2*rival.getLevel()) {
                    b = 4;
                } else if(user.getLevel() > rival.getLevel()) {
                    b = 2;
                }
            } else if(pokeball.hasName("HEAVYBALL")) {
                if (rival.getWeight(false) > 300.0) {
                    r += 30;
                } else if (rival.getWeight(false) > 200.0) {
                    r = 20;
                } else if (rival.getWeight(false) < 100.0) {
                    r -= 20;
                    if (r <= 0) r = 1;
                }
            } else if(pokeball.hasName("LOVEBALL") && rival.getSpecie().equals(user.getSpecie()) && rival.getGender() != user.getGender()
                    && rival.getGender() != 2 && user.getGender() != 2) {
                b = 8;
            } else if(pokeball.hasName("MOONBALL") && (rival.specieNameIs("NIDORANf") || rival.specieNameIs("NIDORINA") ||
                    rival.specieNameIs("NIDOQUEEN") || rival.specieNameIs("NIDORANm") || rival.specieNameIs("NIDORINO") ||
                    rival.specieNameIs("NIDOKING") || rival.specieNameIs("CLEFFA") || rival.specieNameIs("CLEFAIRY") ||
                    rival.specieNameIs("CLEFABLE") || rival.specieNameIs("IGGLYBUFF") || rival.specieNameIs("JIGGLYPUFF") ||
                    rival.specieNameIs("WIGGLYTUFF") || rival.specieNameIs("SKITTY") || rival.specieNameIs("DELCATTY") ||
                    rival.specieNameIs("MUNNA") || rival.specieNameIs("MUSHARNA"))) {
                b = 4;
            } else if(pokeball.hasName("NETBALL") && (rival.hasType("BUG") || rival.hasType("WATER"))) {
                b = 3.5;
            } else if(pokeball.hasName("TIMERBALL")) {
                b = 1 + turn*(1229.0/4096.0);
                if(b >= 4) b = 4;
            } else if(pokeball.hasName("NESTBALL")) {
                if(rival.getLevel() <= 19) {
                    b = 3.9;
                } else if(rival.getLevel() <= 29) {
                    b = 2;
                }
            } else if(pokeball.hasName("QUICKBALL") && turn <= 1) {
                b = 5;
            } else if(pokeball.hasName("BEASTBALL")) {
                if((rival.specieNameIs("NIHILEGO") || rival.specieNameIs("BUZZWOLE") || rival.specieNameIs("PHEROMOSA") ||
                        rival.specieNameIs("XURKITREE") || rival.specieNameIs("CELESTEELA") || rival.specieNameIs("KARTANA") ||
                        rival.specieNameIs("GUZZLORD") || rival.specieNameIs("POIPOLE") || rival.specieNameIs("NAGANADEL") ||
                        rival.specieNameIs("BLACEPHALON") || rival.specieNameIs("STAKATAKA"))) {
                    b = 5;
                } else {
                    b = 0.1;
                }
            } else if(pokeball.hasName("REPEATBALL") && userTeam.getPlayer().getPokedex().isCaptured(rival)) {
                b = 3.5;
            }

            double x = ((r*b*(psmax*3-psactual*2))/(psmax*3))*e;
            // critic capture
            double p = 0;
            if(userTeam.getPlayer().getPokedex().numCaptured() >= 600) {
                p = 2.5;
            } else if(userTeam.getPlayer().getPokedex().numCaptured() >= 451) {
                p = 2.0;
            } else if(userTeam.getPlayer().getPokedex().numCaptured() >= 301) {
                p = 1.5;
            } else if(userTeam.getPlayer().getPokedex().numCaptured() >= 151) {
                p = 1.0;
            } else if(userTeam.getPlayer().getPokedex().numCaptured() >= 31) {
                p = 0.5;
            }
            double cc = (Math.min(255, x)*p)/6.0;
            int randcc = user.utils.getRandomNumberBetween(0,256);
            if(randcc < cc) critic = true;
            int ticks = 3; if(critic) ticks = 1;
            if(x > 255) {
                for(int i=0;i<ticks;i++) {
                    System.out.println("Tick...");
                }
                capturedPokemon(rival, pokeball);
            } else {
                double y = 65536.0/(Math.pow((255.0/x),(3.0/16.0)));
                for(int i=0;i<ticks+1;i++) {
                    int rand = user.utils.getRandomNumberBetween(0,65537);
                    if(y > rand) {
                        if(i == 3 || (i==1 && critic)) {
                            capturedPokemon(rival, pokeball);
                        } else {
                            System.out.println("Tick...");
                        }
                    } else {
                        if(i == 0) System.out.println("Oh no! The Pokémon broke free!");
                        if(i == 1) System.out.println("Aww! It appeared to be caught!");
                        if(i == 2) System.out.println("Aargh! Almost had it!");
                        if(i == 3) System.out.println("Gah! It was so close, too!");
                        i = ticks+1;
                    }
                }
            }
        }
    }

    public void capturedPokemon(Pokemon captured, Item pokeball) {
        System.out.println("Gotcha! " + captured.nickname + " was caught!");
        if(pokeball.hasName("FRIENDBALL")) {
            captured.setHappiness(200);
        }
        else if(pokeball.hasName("HEALBALL")) {
            captured.healPokemon(false);
        }
        captured.pokeball = pokeball;
        userTeam.obtainPokemon(captured);

        battleResult = 4;
        checkFaint();
    }

    private boolean changePokemon(boolean fainted) {
        int chosenIndex = -1;
        do {
            if(fainted) {
                System.out.println("0: Run");
            } else {
                System.out.println("0: Exit");
            }
            userTeam.showTeam();
            chosenIndex = Integer.parseInt(in.nextLine());
            if(chosenIndex != 0) {
                if(userTeam.getPokemon(chosenIndex-1).isFainted()) {
                    System.out.println(userTeam.getPokemon(chosenIndex-1).nickname + " has no energy for fight!");
                    chosenIndex = -1;
                } else if(userTeam.getPokemon(chosenIndex-1) == user) {
                    System.out.println(userTeam.getPokemon(chosenIndex-1).nickname + " is already in battle!");
                    chosenIndex = -1;
                }
            }
        } while(chosenIndex < 0 || chosenIndex > userTeam.getPokemonTeam().size());

        if(chosenIndex == 0) {
            if(fainted && run()) {
                endBattle = true;
                return true;
            }
            return false;
        }
        if(!canSwitch(user,rival)) {
            System.out.println(user.nickname + " can't be switched!");
            return false;
        }

        Movement rivalMove = chooseRivalMove();
        // pursuit
        System.out.println("Come back! " + user.nickname + "!");
        if(rivalMove.getCode() == 68 && !fainted) { //pursuit
            rival.effectMoves.set(15, 1);
            useMove(rival,user,rivalMove,user.utils.getMove("STRUGGLE"),true,false, false);
            if(checkFaint() != 0) {
                return true;
            }
        }

        doChange(true, chosenIndex);
        rivalAttacksYou(fainted, rivalMove);
        if(checkFaint() != 0) {
            return true;
        }

        return true;
    }

    private void rivalAttacksYou(boolean fainted, Movement rivalMove) {
        //rival attacks you
        if(!fainted) {
            firstAttacker = rival;
            secondAttacker = user;
            if(rivalMove.getCode() != 68) {
                useMove(firstAttacker,secondAttacker,rivalMove,user.utils.getMove("STRUGGLE"),true,false, false);
            }
        }
    }

    private void doChange(boolean isUser, int chosenIndex) {
        if(isUser) {
            if(user.hasAbility("REGENERATOR") && !user.isFainted()) {
                user.healHP(user.getStats().get(0)/3,false,false,false, false);
            }
            user.changedPokemon();
            // target is changed
            user = user.getTeam().getPokemon(chosenIndex-1);
            System.out.println("Go! " + user.nickname + "!");
            user.setParticipate(true);
            user.pokeTurn = 0;
        } else {
            if(rival.hasAbility("REGENERATOR") && !rival.isFainted()) {
                rival.healHP(rival.getStats().get(0)/3,false,false,false, false);
            }
            rival.changedPokemon();
            // target is changed
            rival = rival.getTeam().getPokemon(chosenIndex-1);
            System.out.println("Go! " + rival.nickname + "!");
            rival.setParticipate(true);
            rival.pokeTurn = 0;
        }
        // INTIMIDATE
        outToFieldActivate();
        //toxic spikes, spikes, stealth rock, etc...
        if(isUser) {
            fieldTramps(user, rival);
        } else {
            fieldTramps(rival, user);
        }
    }

    private boolean mustFight(Pokemon target) {
        if(target.effectMoves.get(3) == 1 || target.effectMoves.get(11) > 0 || target.effectMoves.get(13) > 0
                || target.effectMoves.get(22) > 0 || target.effectMoves.get(25) > 0) { // two turn attacks, petal dance, uproar, bide, rollout and ice ball
            return true;
        }
        if(target.effectMoves.get(57) > 0 || target.effectMoves.get(59) > 0) return true; // must repose for giga impact, hyper beam..., sky drop
        return false;
    }

    private boolean encoreMove(Pokemon target, Pokemon other) {
        if(target.effectMoves.get(26) > 0 && target.encoreMove != null) {
            if(getMovesWithPP(target,other).contains(target.encoreMove)) {
                return true;
            }
        }
        target.effectMoves.set(26, 0);
        target.encoreMove = null;
        return false;
    }

    private boolean fight() {
        int chosenIndex = -1;
        Movement userMove;

        // truant
        if(user.hasAbility("TRUANT") && user.truant) {
            System.out.println(user.nickname + " is loafing around...");
            userMove = null;
        } else if(mustFight(user)) {
            userMove = user.previousMove;
        } else if(encoreMove(user,rival)) {
            userMove = user.encoreMove;
            metronomeItemEffect(user, userMove);
        } else if(getMovesWithPP(user,rival).isEmpty()) {
            // use struggle
            userMove = user.utils.getMove("STRUGGLE");
            user.effectMoves.set(27, 0);
        } else {
            do {
                System.out.println("0: Exit");
                // moves list
                for(int i=0;i<user.getMoves().size();i++) {
                    System.out.println((i+1)+": "+user.getMoves().get(i).getMove().name+" - "+user.getRemainPPs().get(i)+"/"+user.getMoves().get(i).getPP() + " - " + user.getMoves().get(i).getMove().type.name);
                }
                chosenIndex = Integer.parseInt(in.nextLine());
                // can't use this move
                if(chosenIndex > 0 && chosenIndex <= user.getMoves().size()) {
                    if(user.hasPPByIndex(chosenIndex-1) == 0) {
                        System.out.println("There are no PPs for this move!");
                        chosenIndex = -1;
                    } else if(!getMovesWithPP(user,rival).contains(user.getMoves().get(chosenIndex-1).getMove())) {
                        System.out.println("This move can't be selected!"); // check disabled moves
                        chosenIndex = -1;
                    }
                }
                if(chosenIndex > 0 && chosenIndex <= user.getMoves().size()) {
                    metronomeItemEffect(user, user.getMoves().get(chosenIndex-1).getMove());
                }
            } while(chosenIndex < 0 || chosenIndex > user.getMoves().size());

            // if we cancel, return to previous menu
            if(chosenIndex == 0) {
                return false;
            }
            userMove = user.getMoves().get(chosenIndex-1).getMove();
        }
        Movement rivalMove = chooseRivalMove();

        // focus punch initial message
        if(userMove != null) {
            if(userMove.getCode() == 37) {
                System.out.println(user.nickname + " is strengthening its focusing!");
                user.effectMoves.set(14, 1);
            }
            if(user.hasAbility("PRANKSTER") && userMove.getCategory().equals(Category.STATUS)) userMove.setPriority(userMove.getPriority()+1);
        }
        if(rivalMove != null) {
            if(rivalMove.getCode() == 37) {
                System.out.println(rival.nickname + " is strengthening its focusing!");
                rival.effectMoves.set(14, 1);
            }
            if(rival.hasAbility("PRANKSTER") && rivalMove.getCategory().equals(Category.STATUS)) rivalMove.setPriority(rivalMove.getPriority()+1);
        }

        // determine priority
        if(rivalMove == null) {
            firstAttacker = user;
            secondAttacker = rival;
            firstMove = userMove;
            secondMove = rivalMove;
        } else if(userMove == null) {
            firstAttacker = rival;
            secondAttacker = user;
            firstMove = rivalMove;
            secondMove = userMove;
        } else {
            determinePriority(userMove,rivalMove);
        }

        useMove(firstAttacker,secondAttacker,firstMove,secondMove, true, false, false);
        if(checkFaint() != 0) {
            return true;
        }
        useMove(secondAttacker,firstAttacker,secondMove,firstMove, true, false, false);
        if(checkFaint() != 0) {
            return true;
        }

        return true;
    }

    private Movement chooseRivalMove() {
        // choose rival move TODO: AI, for the moment is random
        Movement rivalMove;
        // truant
        if(rival.hasAbility("TRUANT") && rival.truant) {
            System.out.println(rival.nickname + " is loafing around...");
            rivalMove = null;
        } else if(mustFight(rival)) {
            rivalMove = rival.previousMove;
        } else if(encoreMove(rival,user)) {
            rivalMove = rival.encoreMove;
            metronomeItemEffect(rival, rivalMove);
        } else if(getMovesWithPP(rival,user).isEmpty()) {
            // use struggle
            rivalMove = rival.utils.getMove("STRUGGLE");
            rival.effectMoves.set(27, 0);
        } else {
            rivalMove = getMovesWithPP(rival,user).get(random.nextInt(getMovesWithPP(rival,user).size()));
            metronomeItemEffect(rival, rivalMove);
        }

        return rivalMove;
    }

    public List<Movement> getMovesWithPP(Pokemon target, Pokemon other) {
        List<Movement> m = new ArrayList<Movement>();

        for(int i=0;i<target.getMoves().size();i++) {
            if(target.hasPPByIndex(i) > 0 && !target.disabledMove(i) && !isImprisonMove(other,target.getMoves().get(i).getMove())) {
                m.add(target.getMoves().get(i).getMove());
            }
        }
        return m;
    }

    public boolean isImprisonMove(Pokemon defender, Movement move) {
        return (defender.effectMoves.get(30) > 0 && defender.hasMove(move.getInternalName()));
    }

    private void metronomeItemEffect(Pokemon target, Movement move) {
        if(target.hasItem("METRONOME") && target.canUseItem()) {
            if(move == target.previousMove) {
                target.effectMoves.set(27, target.effectMoves.get(27)+1);
                if(target.effectMoves.get(27) > 5) {
                    target.effectMoves.set(27, 0);
                }
            }
        }
    }

    public boolean canScape(Pokemon target, Pokemon other) {
        //TODO: can scape? look for abilities, etc...
        if((target.hasItem("SMOKEBALL") && target.canUseItem()) || target.hasType("GHOST")) {
            return true;
        }
        if(other.hasAbility("ARENATRAP") && !target.isLevitating()) {
            return false;
        }
        if(other.hasAbility("SHADOWTAG") && !target.hasAbility("SHADOWTAG")) {
            return false;
        }
        if(other.hasAbility("MAGNETPULL") && !target.hasType("STEEL")) {
            return false;
        }
        if(target.effectMoves.get(34) > 0) {
            return false;
        }
        if(target.hasTemporalStatus(TemporalStatus.TRAPPED) || target.hasTemporalStatus(TemporalStatus.PARTIALLYTRAPPED)
                || target.effectMoves.get(0) > 0) { // bind moves and ingrain
            return false;
        }

        return true;
    }

    private boolean canSwitch(Pokemon target, Pokemon other) {
        //TODO: can switch? look for abilities, items, etc...
        if(target.hasType("GHOST") || (target.hasItem("SHEDSHELL") && target.canUseItem())) {
            return true;
        }
        if(other.hasAbility("ARENATRAP") && !target.isLevitating()) {
            return false;
        }
        if(other.hasAbility("SHADOWTAG") && !target.hasAbility("SHADOWTAG")) {
            return false;
        }
        if(other.hasAbility("MAGNETPULL") && !target.hasType("STEEL")) {
            return false;
        }
        if(target.effectMoves.get(34) > 0) {
            return false;
        }
        if(target.hasTemporalStatus(TemporalStatus.TRAPPED) || target.hasTemporalStatus(TemporalStatus.PARTIALLYTRAPPED)
                || target.effectMoves.get(0) > 0) { // bind moves and ingrain
            return false;
        }

        return true;
    }

    private void fieldTramps(Pokemon target, Pokemon other) {
        // absorb toxic spikes
        if(target.hasType("POISON") && !target.isLevitating() && target.getTeam().effectTeamMoves.get(3) > 0) {
            System.out.println(target.nickname + " absorbed the Toxic Spikes!");
            target.getTeam().removeTeamEffects(target,3);
        }
        if(!(target.hasItem("HEAVYDUTYBOOTS") && target.canUseItem())) {
            // toxic spikes
            if(target.getTeam().effectTeamMoves.get(3) == 1 && target.affectToxicSpikes()) {
                target.causeStatus(Status.POISONED, other, true);
            } else if(target.getTeam().effectTeamMoves.get(3) >= 2 && target.affectToxicSpikes()) {
                target.causeStatus(Status.BADLYPOISONED, other, true);
            }
            // spikes
            // stealth rock
            if(target.getTeam().effectTeamMoves.get(14) > 1 && !target.hasAbility("MAGICGUARD")) {
                System.out.println(target.nickname + " was damaged by Stealth Rock!");
                target.reduceHP(target.stealthRockDamage());
            }
            // sticky web
        }

    }

    private boolean twoTurnAttacks(Pokemon attacker, Movement attackerMove) {
        if ((!attackerMove.getCategory().equals(Category.STATUS) && attackerMove.getCode() != 11)) { // if it is damaging move and is not two turn attack
            return true;
        }
        if (attackerMove.getCode() == 11 && (attacker.effectMoves.get(3) == 1 || (attacker.hasItem("POWERHERB") && attacker.canUseItem()))) { // if it is two turn attack and is charged
            return true;
        }
        if (attackerMove.hasName("SOLARBEAM") && (weather.hasWeather(Weathers.SUNLIGHT) || weather.hasWeather(Weathers.HEAVYSUNLIGHT))) {
            return true; // if it is solar beam and the weather is sunlight
        }

        return false;
    }

    public int moveAccuracyChanges(Pokemon attacker, Pokemon defender, Movement attackerMove) {
        int moveAccuracy = attackerMove.getAccuracy();
        // protect moves reduce accuracy depending on turns
        if(attackerMove.getCode() == 19 || attackerMove.getCode() == 41) {
            moveAccuracy = (int) (100.0/(Math.pow(2,attacker.protectTurns)));
        }
        // One Hit KO moves accuracy
        if(attackerMove.getCode() == 133) {
            moveAccuracy = (attacker.getLevel() - defender.getLevel()) + 30;
        }
        // HURRICANE and THUNDER low accuracy with sun
        if((attackerMove.hasName("HURRICANE") || attackerMove.hasName("THUNDER")) &&
                (weather.hasWeather(Weathers.SUNLIGHT) || weather.hasWeather(Weathers.HEAVYSUNLIGHT))) {
            moveAccuracy = 50;
        }
        // wonder skin
        if(defender.hasAbility("WONDERSKIN") && attackerMove.targetIsEnemy() && attackerMove.getCategory().equals(Category.STATUS) &&
                moveAccuracy > 50 && !attacker.hasAbility("MOLDBREAKER")) {
            moveAccuracy = 50;
        }
        // compound eyes
        if(attacker.hasAbility("COMPOUNDEYES")) {
            moveAccuracy *= 1.3;
        }
        // wide lens
        if(attacker.hasItem("WIDELENS") && attacker.canUseItem()) {
            moveAccuracy *= 1.1;
        }
        // zoom lens
        if(attacker.hasItem("ZOOMLENS") && defender == firstAttacker && attacker.canUseItem()) {
            moveAccuracy *= 1.2;
        }
        // tangled feet
        if(defender.hasAbility("TANGLEDFEET") && !attacker.hasAbility("MOLDBREAKER") && defender.hasTemporalStatus(TemporalStatus.CONFUSED)) {
            moveAccuracy *= 0.5;
        }
        // hustle
        if(attacker.hasAbility("HUSTLE") && attackerMove.getCategory().equals(Category.PHYSICAL)) {
            moveAccuracy *= 0.8;
        }
        // sand veil
        if(defender.hasAbility("SANDVEIL") && !attacker.hasAbility("MOLDBREAKER") && weather.hasWeather(Weathers.SANDSTORM)) {
            moveAccuracy *= 0.8;
        }
        // snow cloak
        if(defender.hasAbility("SNOWCLOAK") && !attacker.hasAbility("MOLDBREAKER") && weather.hasWeather(Weathers.HAIL)) {
            moveAccuracy *= 0.8;
        }
        // micle berry
        if(attacker.effectMoves.get(47) > 0) {
            moveAccuracy *= 1.2;
        }
        if(moveAccuracy == 0 && attackerMove.getAccuracy() != 0) moveAccuracy = 1;

        // BLIZZARD never will fail with hail
        if(attackerMove.hasName("BLIZZARD") && weather.hasWeather(Weathers.HAIL)) {
            moveAccuracy = 0;
        }
        // HURRICANE and THUNDER never fails with rain
        if((attackerMove.hasName("HURRICANE") || attackerMove.hasName("THUNDER")) &&
                (weather.hasWeather(Weathers.RAIN) || weather.hasWeather(Weathers.HEAVYRAIN))) {
            moveAccuracy = 0;
        }
        // TOXIC used by Poison Pokemon
        if(attackerMove.hasName("TOXIC") && attacker.hasType("POISON")) {
            moveAccuracy = 0;
        }
        // TWO TURN ATTACKS always charge
        if(attackerMove.getCode() == 11 && attacker.effectMoves.get(3) == 0) {
            moveAccuracy = 0;
        }
        // MINIMIZE MAKES INFALLIBLE THESE MOVES
        if((attackerMove.hasName("BODYSLAM") || attackerMove.hasName("STOMP") || attackerMove.hasName("STEAMROLLER")
                || attackerMove.hasName("HEATCRASH") || attackerMove.hasName("DRAGONRUSH") || attackerMove.hasName("PHANTOMFORCE")
                || attackerMove.hasName("FLYINGPRESS") || attackerMove.hasName("HEAVYSLAM")
                || attackerMove.hasName("DOUBLEIRONBASH")) && defender.effectMoves.get(29) > 0) {
            moveAccuracy = 0;
        }
        // mind reader/lock-on never fails, NO GUARD ability
        if(defender.effectMoves.get(40) > 0 || attacker.hasAbility("NOGUARD") || defender.hasAbility("NOGUARD")) {
            moveAccuracy = 0;
        }
        // telekinesis makes always hit, except OHKO moves
        if(defender.effectMoves.get(42) > 0 && attackerMove.getCode() != 133) {
            moveAccuracy = 0;
        }

        if(moveAccuracy > 100) moveAccuracy = 100;
        return moveAccuracy;
    }

    public boolean inmunityToAttack(Pokemon attacker, Pokemon defender, Movement attackerMove) {
        // heavy rain and heavy sun prevents fire/water attacks
        if(weather.hasWeather(Weathers.HEAVYSUNLIGHT) && attackerMove.type.is("WATER") && !attackerMove.getCategory().equals(Category.STATUS)) {
            System.out.println("Heavy sunlight prevents Water-Type attacks!");
            return true;
        } else if(weather.hasWeather(Weathers.HEAVYRAIN) && attackerMove.type.is("FIRE") && !attackerMove.getCategory().equals(Category.STATUS)) {
            System.out.println("Heavy rain prevents Fire-Type attacks!");
            return true;
        }
        if(attackerMove.getCode() == 254 && !weather.hasWeather(Weathers.HAIL)) { // aurora veil fails if is not hailing
            return true;
        }
        // levitate
        if(defender.isLevitating() && attackerMove.type.is("GROUND") && attackerMove.targetIsEnemy() &&
                !attackerMove.hasName("SANDATTACK") && !attacker.hasAbility("MOLDBREAKER")) {
            return true;
        }
        // powder moves
        if(attackerMove.getFlags().contains("l") && attackerMove.targetIsEnemy()) {
            if(defender.hasItem("SAFETYGOOGLES") && defender.canUseItem()) {
                System.out.println(defender.item.name + " prevents " + defender.nickname + " from " + attackerMove.name + "!");
                return true;
            } else if(defender.hasType("GRASS")) {
                System.out.println(attackerMove.name + " doesn't affect " + defender.nickname + "!");
                return true;
            } else if(defender.hasAbility("OVERCOAT")) {
                System.out.println(defender.getAbility().name + " prevents " + defender.nickname + " from " + attackerMove.name + "!");
                return true;
            }
        }

        // abilities
        if(defender.hasAbility("LIGHTNINGROD") && attackerMove.type.is("ELECTRIC") && attackerMove.targetIsEnemy() &&
                !attacker.hasAbility("MOLDBREAKER")) {
            System.out.println(defender.getAbility().name + " from " + defender.nickname + " prevents " + attackerMove.name + "!");
            defender.changeStat(2,1,false,true, attacker);
            return true;
        }
        if(defender.hasAbility("MOTORDRIVE") && attackerMove.type.is("ELECTRIC") && attackerMove.targetIsEnemy() &&
                !attacker.hasAbility("MOLDBREAKER")) {
            System.out.println(defender.getAbility().name + " from " + defender.nickname + " prevents " + attackerMove.name + "!");
            defender.changeStat(4,1,false,true, attacker);
            return true;
        }
        if(defender.hasAbility("VOLTABSORB") && attackerMove.type.is("ELECTRIC") && attackerMove.targetIsEnemy() &&
                !attacker.hasAbility("MOLDBREAKER")) {
            System.out.println(defender.getAbility().name + " from " + defender.nickname + " prevents " + attackerMove.name + "!");
            defender.healHP(defender.getHP()/4,true,false, false, true);
            return true;
        }
        if(defender.hasAbility("SAPSIPPER") && attackerMove.type.is("GRASS") && attackerMove.targetIsEnemy() &&
                !attacker.hasAbility("MOLDBREAKER")) {
            System.out.println(defender.getAbility().name + " from " + defender.nickname + " prevents " + attackerMove.name + "!");
            defender.changeStat(0,1,false,true, attacker);
            return true;
        }
        if(defender.hasAbility("STORMDRAIN") && attackerMove.type.is("WATER") && attackerMove.targetIsEnemy() &&
                !attacker.hasAbility("MOLDBREAKER")) {
            System.out.println(defender.getAbility().name + " from " + defender.nickname + " prevents " + attackerMove.name + "!");
            defender.changeStat(2,1,false,true, attacker);
            return true;
        }
        if(defender.hasAbility("FLASHFIRE") && attackerMove.type.is("FIRE") && attackerMove.targetIsEnemy() &&
                !attacker.hasAbility("MOLDBREAKER")) {
            System.out.println(defender.getAbility().name + " from " + defender.nickname + " prevents " + attackerMove.name + "!");
            defender.effectMoves.set(28, 1);
            return true;
        }
        if((defender.hasAbility("DRYSKIN") || defender.hasAbility("WATERABSORB")) && attackerMove.type.is("WATER") &&
                attackerMove.targetIsEnemy() && !attacker.hasAbility("MOLDBREAKER")) {
            System.out.println(defender.getAbility().name + " from " + defender.nickname + " prevents " + attackerMove.name + "!");
            defender.healHP(defender.getHP()/4,true,false, false, true);
            return true;
        }
        if(defender.hasAbility("SOUNDPROOF") && attackerMove.getFlags().contains("j") && attackerMove.targetIsEnemy() &&
                !attacker.hasAbility("MOLDBREAKER")) {
            System.out.println(defender.getAbility().name + " from " + defender.nickname + " prevents " + attackerMove.name + "!");
            return true;
        }
        if(defender.hasAbility("BULLETPROOF") && attackerMove.getFlags().contains("n") && attackerMove.targetIsEnemy() &&
                !attacker.hasAbility("MOLDBREAKER")) {
            System.out.println(defender.getAbility().name + " from " + defender.nickname + " prevents " + attackerMove.name + "!");
            return true;
        }
        if(defender.hasAbility("WONDERGUARD") && getEffectiveness(attacker,defender,attackerMove,false) < 2.0 && attackerMove.targetIsEnemy() &&
                !attacker.hasAbility("MOLDBREAKER")) {
            System.out.println(defender.getAbility().name + " from " + defender.nickname + " prevents " + attackerMove.name + "!");
            return true;
        }
        // moves inmunities
        if(defender.hasType("GROUND") && attackerMove.hasName("THUNDERWAVE") && attackerMove.type.is("ELECTRIC")) return true; // thunder wave doesn't affect ground pokemon
        if(attackerMove.getCode() == 133 && ((attacker.getLevel() < defender.getLevel()) || (defender.hasAbility("STURDY") &&
                !attacker.hasAbility("MOLDBREAKER")))) { // One Hit KO moves fails for level and sturdy
            return true;
        }
        if(attackerMove.getCode() == 181 && !defender.shareTypes(attacker)) { // synchronoise only affect same type Pokemon
            return true;
        }
        if(attackerMove.hasName("DREAMEATER") && !defender.hasStatus(Status.ASLEEP)) { // dream eater only works if target is asleep
            return true;
        }
        if(attackerMove.getCode() == 189 && !attacker.hasType("FIRE")) { // burn up is only usable for Fire type Pokemon
            return true;
        }
        if(attacker.getTeam().effectTeamMoves.get(9) > 0 && attackerMove.getPriority() > 0 && !attackerMove.hasName("FEINT")) { // priority attacks blocked by Quick Guard
            return true;
        }
        if(attacker.getTeam().effectTeamMoves.get(10) > 0 && attackerMove.multiTarget()) { // multi target attacks blocked by Wide Guard
            return true;
        }
        if(attackerMove.getCode() == 164 && (!attacker.hasItemWithFlag("c") || defender.hasAbility("UNNERVE") || !attacker.canUseItem()) || effectFieldMoves.get(6) > 0) { // if natural gift attacker doesnt have a berry, it fails
            return true;
        }
        if(attackerMove.getCode() == 211 && !attacker.hasStatus(Status.ASLEEP)) { // if user is not slept, snore fails
            return true;
        }
        if(attackerMove.getCode() == 187) { // if fling attacker doesnt have item or has forbidden items, it fails
            if(attacker.item == null || !attacker.canUseItem() || effectFieldMoves.get(6) > 0) {
                return true;
            }
            if((attacker.hasItem("GRISEOUSORB") && attacker.specieNameIs("GIRATINA")) ||
                    (attacker.item.getFlags().contains("l") && attacker.specieNameIs("ARCEUS")) ||
                    ((attacker.hasItem("DOUSEDRIVE") || attacker.hasItem("SHOCKDRIVE") || attacker.hasItem("CHILLDRIVE") ||
                            attacker.hasItem("BURNDRIVE")) && attacker.specieNameIs("GENESECT")) ||
                    (attacker.item.getFlags().contains("m") && attacker.specieNameIs("SILVALLY"))) {
                return true;
            } // TODO: kyogre with blue orb, groudon with red orb, mail and mega stones
            if(attacker.hasItemWithFlag("a") || attacker.hasItemWithFlag("b") || attacker.hasItemWithFlag("g")
                    || attacker.hasItemWithPocket("TMS")) {
                return true;
            }
        }
        // terrains
        if(terrain.hasTerrain(TerrainTypes.PSYCHIC) && !defender.isLevitating() && attackerMove.getPriority() > 0 && attackerMove.targetIsEnemy()) {
            System.out.println("Psychic Terrain protect from " + attackerMove.name + "!");
            return true;
        }

        return false;
    }

    private boolean affectDig(Pokemon target, Pokemon other, Movement move) {
        if(target.hasAbility("NOGUARD") || other.hasAbility("NOGUARD")) {
            return true;
        }
        if(move.hasName("EARTHQUAKE") || move.hasName("MAGNITUDE")) {
            return true;
        }
        if(target.hasType("POISON") && move.hasName("TOXIC")) {
            return true;
        }
        if(target.effectMoves.get(40) > 0) { // lock-on and mind reader
            return true;
        }
        return false;
    }

    private boolean affectDive(Pokemon target, Pokemon other, Movement move) {
        if(target.hasAbility("NOGUARD") || other.hasAbility("NOGUARD")) {
            return true;
        }
        if(move.hasName("WHIRLPOOL") || move.hasName("SURF")) {
            return true;
        }
        if(target.hasType("POISON") && move.hasName("TOXIC")) {
            return true;
        }
        if(target.effectMoves.get(40) > 0) { // lock-on and mind reader
            return true;
        }
        return false;
    }

    private boolean affectFly(Pokemon target, Pokemon other, Movement move) {
        if(target.hasAbility("NOGUARD") || other.hasAbility("NOGUARD")) {
            return true;
        }
        if(move.hasName("TWISTER") || move.hasName("SKYUPPERCUT") || move.hasName("GUST") || move.hasName("THUNDER") ||
                move.hasName("WHIRLWIND") || move.hasName("HURRICANE") || move.hasName("THOUSANDARROWS") ||
                move.hasName("SMACKDOWN")) {
            return true;
        }
        if(target.hasType("POISON") && move.hasName("TOXIC")) {
            return true;
        }
        if(target.effectMoves.get(40) > 0) { // lock-on and mind reader
            return true;
        }
        return false;
    }
    private boolean affectPhantomForce(Pokemon target, Pokemon other, Movement move) {
        if(target.hasAbility("NOGUARD") || other.hasAbility("NOGUARD")) {
            return true;
        }
        if(target.hasType("POISON") && move.hasName("TOXIC")) {
            return true;
        }
        if(target.effectMoves.get(40) > 0) { // lock-on and mind reader
            return true;
        }
        return false;
    }

    private void ifMoveFailed(Pokemon attacker, Movement attackerMove, boolean stomping) {
        if(stomping) {
            attacker.effectMoves.set(38, 1); // stomping tantrum
        }
        removeMultiTurnAttackEffects(attacker, attackerMove);
        if(attackerMove.getCode() == 217) { // jump kick, high jump kick
            System.out.println(attacker.nickname + " fell and crashed!");
            attacker.reduceHP(attacker.getStats().get(0));
        }
    }

    public void useMove(Pokemon attacker, Pokemon defender, Movement attackerMove, Movement defenderMove, boolean reducePP, boolean mefirst, boolean indirect) {
        c = 0;
        int dmg = 0;
        if(attackerMove == null) return;
        if(defenderMove == null) defenderMove = attacker.utils.getMove("STRUGGLE");
        // StartTurn
        startMove(attacker,defender, attackerMove);
        // change attack
        if(attacker.effectMoves.get(26) > 0 && attacker.encoreMove != null) {
            if(attacker.hasPP(attacker.encoreMove)) {
                attackerMove = attacker.encoreMove; // encore
            }
        }

        // protect/detect, endure, quick guard, wide guard
        if(attackerMove.getCode() != 19 && attackerMove.getCode() != 41 && attackerMove.getCode() != 163 && attackerMove.getCode() != 166) {
            attacker.protectTurns = 0;
        }
        // destiny bond
        if(attackerMove.getCode() != 221) {
            attacker.destinyBondTurns = 0;
            attacker.effectMoves.set(53, 0);
        }
        // rage
        if(attackerMove.getCode() != 67) {
            attacker.effectMoves.set(8,0);
        }
        // fury cutter
        if(attackerMove.getCode() != 121) {
            attacker.effectMoves.set(24, 0);
        }
        // metronome (item)
        if(attackerMove != attacker.previousMove) {
            attacker.effectMoves.set(27, 0);
        }

        if(!willNotAttack(attacker,defender,attackerMove) || indirect) {
            if(attacker.chosenMove == null) attacker.chosenMove = attackerMove;
            attacker.previousMove = attackerMove;
            attacker.moveUsedAdded(attackerMove);
            System.out.println(attacker.nickname + " used " + attackerMove.name + "!");

            // changes in move accuracy
            int moveAccuracy = moveAccuracyChanges(attacker,defender,attackerMove);
            double accuracy = attacker.getAccuracy(defender.hasAbility("UNAWARE") && !attacker.hasAbility("MOLDBREAKER"));
            double evasion = defender.getEvasion(attackerMove.getCode() == 125 || (attacker.hasAbility("UNAWARE") && !attacker.hasAbility("MOLDBREAKER"))); // chip away
            // changes in attacker accuracy
            if(weather.hasWeather(Weathers.FOG)) {
                accuracy *= 0.6;
            }
            if((defender.hasItem("BRIGHTPOWDER") || defender.hasItem("LAXINCENSE")) && defender.canUseItem()) {
                accuracy *= 0.9;
            }
            //changes in defender evasion
            if(attacker.hasAbility("KEENEYE") && evasion > 1.0) {
                evasion = 1.0;
            }
            // fore sight/odor sleuth, miracle eye and OHKO move
            if(defender.effectMoves.get(5) > 0 || defender.effectMoves.get(41) > 0 || attackerMove.getCode() == 133) {
                accuracy = 1.0;
                evasion = 1.0;
            }

            double a = (moveAccuracy / 100.0) * (accuracy / evasion);
            // calculate precision
            if (a >= Math.random() || moveAccuracy == 0 || !twoTurnAttacks(attacker,attackerMove)) {
                defender.lastMoveReceived = attackerMove;
                lastMoveUsed = attackerMove;
                // change move type
                changeMoveType(attacker,defender,attackerMove);
                // immunity caused by abilities, weather, items, etc...
                if(inmunityToAttack(attacker, defender, attackerMove) && twoTurnAttacks(attacker,attackerMove)) {
                    ifMoveFailed(attacker,attackerMove, true);
                }
                // gravity impides moves
                else if(effectFieldMoves.get(2) > 0 && (attackerMove.hasName("BOUNCE") || attackerMove.hasName("SKYDROP")
                        || attackerMove.hasName("MAGNETRISE") || attackerMove.hasName("JUMPKICK") || attackerMove.hasName("HIGHJUMPKICK")
                        || attackerMove.hasName("FLYINGPRESS") || attackerMove.hasName("SPLASH") || attackerMove.hasName("TELEKINESIS")
                        || attackerMove.hasName("FLY"))) {
                    ifMoveFailed(attacker,attackerMove, true);
                }
                // sky drop fails
                else if(attackerMove.hasName("SKYDROP") && attacker.effectMoves.get(3) == 0 && defender.getWeight(attacker.hasAbility("MOLDBREAKER")) >= 200.0) {
                    System.out.println(defender.nickname + " is too heavy!");
                    ifMoveFailed(attacker,attackerMove, true);
                } else if(attackerMove.hasName("SKYDROP") && attacker.effectMoves.get(3) > 0 && defender.hasType("FLYING")) {
                    System.out.println(defender.nickname + " doesn't receive damage!");
                    ifMoveFailed(attacker,attackerMove, true);
                }
                // rival is protecting
                else if(defender.effectMoves.get(2) == 1 && attackerMove.getFlags().contains("b") && twoTurnAttacks(attacker,attackerMove)) {
                    System.out.println(defender.nickname + " has protected!");
                    ifMoveFailed(attacker,attackerMove, false);
                }
                // rival is using dig, fly/bounce, dive, phantom force
                else if((defender.effectMoves.get(36) > 0 && !affectDig(defender,attacker,attackerMove)) ||
                        (defender.effectMoves.get(44) > 0 && !affectFly(defender,attacker,attackerMove)) ||
                        (defender.effectMoves.get(59) > 0 && !affectFly(defender,attacker,attackerMove)) ||
                        (defender.effectMoves.get(46) > 0 && !affectDive(defender,attacker,attackerMove)) ||
                        (defender.effectMoves.get(65) > 0 && !affectPhantomForce(defender,attacker,attackerMove))) { // dig, fly/skydrop/bounce, dive, phantom force
                    System.out.println(defender.nickname + " evaded the attack!");
                    ifMoveFailed(attacker,attackerMove, true);
                } // moves that will fail
                else if((attackerMove.hasName("FAKEOUT") || attackerMove.hasName("FIRSTIMPRESSION")) && attacker.pokeTurn > 1) { // fake out
                    System.out.println("But it failed!");
                    ifMoveFailed(attacker,attackerMove, true);
                } else if(attackerMove.hasName("SPITUP") && attacker.stockpile == 0) { // spit up
                    System.out.println("But it failed!");
                    ifMoveFailed(attacker,attackerMove, true);
                } else if(attackerMove.getCode() == 82 && attacker != firstAttacker) {
                    System.out.println("But it failed!"); // me first
                    ifMoveFailed(attacker,attackerMove, true);
                } else if(attackerMove.getCode() == 83 && !attacker.canUseLastResort()) { // last resort
                    System.out.println("But it failed!");
                    ifMoveFailed(attacker,attackerMove, true);
                } else if(attackerMove.getCode() == 79 && ((defenderMove.getCategory().equals(Category.STATUS) && !defenderMove.hasName("MEFIRST")) || attacker != firstAttacker)) {
                    System.out.println("But it failed!"); // sucker punch
                    ifMoveFailed(attacker,attackerMove, true);
                }else if(defender.effectMoves.get(18) > 0 && attackerMove.getFlags().contains("e")) {
                    System.out.println(defender.nickname + " snatched the move!"); // snatch
                    useMove(defender,attacker,attackerMove,defenderMove,false,false,true);
                } else if((defender.effectMoves.get(58) > 0 || (defender.hasAbility("MAGICBOUNCE") && !attacker.hasAbility("MOLDBREAKER")))
                        && attackerMove.getFlags().contains("d")) {
                    System.out.println(defender.nickname + " returned the move!"); // magic coat, magic bounce
                    useMove(defender,attacker,attackerMove,defenderMove,false,false,true);
                } else if(attacker.hasAbility("PRANKSTER") && attackerMove.getCategory().equals(Category.STATUS) &&
                        attackerMove.targetIsEnemy() && defender.hasType("DARK")) {
                    System.out.println(defender.nickname + " evaded the attack!"); // Dark Pokémon are immune to PRANKSTER moves
                } else if((attackerMove == attacker.disabledMove && attacker.effectMoves.get(17) > 0) ||
                        (attackerMove == attacker.cursedBodyMove && attacker.effectMoves.get(52) > 0) ||
                        isImprisonMove(defender,attackerMove) || attacker.disabledMove(attackerMove)) {
                    System.out.println(attackerMove.name + " is disabled!"); // disabled move
                    removeMultiTurnAttackEffects(attacker, attackerMove);
                } else if(!getMovesWithPP(attacker,defender).contains(attackerMove)) {
                    System.out.println(attackerMove.name + " can't use this move!"); // disabled move
                    removeMultiTurnAttackEffects(attacker, attackerMove);
                } else {
                    int hits = 1;
                    ArrayList<Pokemon> beatUp = new ArrayList<Pokemon>();
                    // attack more than one time in 1 turn
                    if(attackerMove.getCode() == 31) {
                        beatUp = attacker.getTeam().getBeatUpTeam(attacker);
                        hits = beatUp.size();
                    } else if(attackerMove.getCode() == 65 || attackerMove.getCode() == 128) { // twin needle, double kick
                        hits = 2;
                    } else if(attackerMove.getCode() == 266) { // triple kick
                        hits = 3;
                    } else if(attackerMove.getCode() == 66) { // fury attack, pin missile
                        double prob = Math.random();
                        if(0.125 >= prob || attacker.hasAbility("SKILLLINK")) {
                            hits = 5;
                        } else if(0.25 >= prob) {
                            hits = 4;
                        } else if(0.625 >= prob) {
                            hits = 3;
                        } else {
                            hits = 2;
                        }
                    }

                    for(int i=0;i<hits;i++) {
                        // BEAT UP
                        if(attackerMove.getCode() == 31) {
                            attacker = beatUp.get(i);
                        }
                        int addEffect = attackerMove.getAddEffect();
                        if(attacker.hasAbility("SERENEGRACE")) {
                            addEffect *= 2;
                            if(addEffect > 100) addEffect = 100;
                        }
                        if(attackerMove.getCode() == 266 && i > 0) {
                            if(a >= Math.random() || attacker.hasAbility("SKILLLINK")) {
                                attackerMove.setPower(attackerMove.getPower()+10);
                                dmg = attack(attacker,defender,attackerMove,defenderMove,mefirst,dmg,addEffect);
                            } else {
                                moveMissed(attacker,defender,attackerMove);
                                hits = i;
                            }
                        } else {
                            dmg = attack(attacker,defender,attackerMove,defenderMove,mefirst,dmg,addEffect);
                        }
                    }
                    if((hits > 1 || attackerMove.getCode() == 266) && attackerMove.getCode() != 31) System.out.println("Number of hits: " + hits + "!");
                }

            } else {
                // move failed
                moveMissed(attacker,defender,attackerMove);
            }
            defender.effectMoves.set(31, 0);
            useAutomatic(attacker,defender);
            useAutomatic(defender,attacker);
            if(reducePP) { attacker.reducePP(attackerMove,1, defender); }
            if(defender.effectMoves.get(22) > 0 && defenderMove.hasName("BIDE")) defender.bideDamage += dmg; // bide damage increases
            // self destruct, explosion
            if(attackerMove.getCode() == 209) {
                attacker.reduceHP(-1);
            }
            System.out.println(defender.nickname + " HP: " + defender.getPsActuales() + "/" + defender.getHP());
        }
        attackerMove.recoverType();
        attacker.effectMoves.set(35, 0);
    }

    private int attack(Pokemon attacker, Pokemon defender, Movement attackerMove, Movement defenderMove, boolean mefirst,int dmg, int addEffect) {
        // rival is using substitute
        if(defender.effectMoves.get(56) > 1 && attackerMove.targetIsEnemy() && !attackerMove.getFlags().contains("q") &&
                twoTurnAttacks(attacker,attackerMove) && !attackerMove.getFlags().contains("j") && attacker.hasAbility("INFILTRATOR")) {
            if(attackerMove.getPower() == 0) {
                System.out.println("But it failed!");
            } else {
                defender.substitute -= CalcDamage(attacker, defender, attackerMove, mefirst);
                System.out.println(defender.nickname + "'s substitute received the damage!");
                if(defender.substitute <= 0) {
                    defender.effectMoves.set(56, 0);
                    defender.substitute = -1;
                    System.out.println(defender.nickname + "'s substitute was defeated!");
                }
            }
        } else {
            if (twoTurnAttacks(attacker,attackerMove) && attackerMove.getPower() != 0) {
                dmg = CalcDamage(attacker, defender, attackerMove, mefirst);
                if(dmg > 0) {
                    defender.reduceHP(dmg);
                    defender.lastMoveInThisTurn = attackerMove;
                    attacker.lastMoveUsedInTurn = attackerMove;
                    defender.previousDamage = dmg;

                    if (attackerMove.getAddEffect() == 0 || ((addEffect / 100.0) >= Math.random())) {
                        moveEffects.moveEffects(attackerMove, attacker, defender, defenderMove, dmg);
                    }
                } else {
                    removeMultiTurnAttackEffects(attacker, attackerMove);
                }
            } else {
                if (attackerMove.getAddEffect() == 0 || ((addEffect / 100.0) >= Math.random())) {
                    if (!moveEffects.moveEffects(attackerMove, attacker, defender, defenderMove, dmg)) {
                        System.out.println("But it failed!");
                        ifMoveFailed(attacker,attackerMove, true);
                    }
                }
            }
            // effects after attacks - LIFE ORB, ROUGH SKIN...
            if(dmg > 0) {
                // rage
                if(attackerMove.getPower() != 0 && !defender.isFainted() && !attacker.isFainted() && defender.effectMoves.get(8) > 0) { // rage
                    System.out.println(defender.nickname + " rage is increasing!");
                    defender.changeStat(0,1,false, false, attacker);
                }
                itemEffectsAfterAttack(attacker,defender,attackerMove,defenderMove, dmg);
                abilityEffectsAfterAttack(attacker,defender,attackerMove,defenderMove, dmg);
                moveEffectsAfterAttack(attacker,defender,attackerMove,defenderMove);
            }
        }
        return dmg;
    }

    private void moveMissed(Pokemon attacker, Pokemon defender, Movement attackerMove) {
        System.out.println(attacker.nickname + "'s move missed!");
        ifMoveFailed(attacker,attackerMove, true);
        attacker.protectTurns = 0;
        attacker.destinyBondTurns = 0;
        defender.lastMoveReceived = attackerMove;
        if(!attacker.isFainted() && attacker.hasItem("BLUNDERPOLICY") && attacker.canUseItem()) { // blunder policy
            if(attacker.changeStat(4,2,false,true, defender)) attacker.useItem(true,true,true);
        }
    }

    private void removeMultiTurnAttackEffects(Pokemon target, Movement move) {
        target.effectMoves.set(11, 0); // petal dance
        target.effectMoves.set(3, 0); // two turn attack
        target.effectMoves.set(36, 0); // dig
        target.effectMoves.set(22, 0); // bide
        target.effectMoves.set(24, 0); // fury cutter
        target.effectMoves.set(8, 0); // rage
        target.effectMoves.set(13, 0); // uproar
        target.effectMoves.set(25, 0); // rollout, ice ball
        target.effectMoves.set(44, 0); // bounce/fly
        target.effectMoves.set(46, 0); // dive
        target.effectMoves.set(59, 0); // sky drop
        target.effectMoves.set(65, 0); // phantom force
    }

    private boolean willNotAttack(Pokemon attacker, Pokemon defender, Movement attackerMove) {
        boolean notAttack = false;
        if(attacker.effectMoves.get(57) > 0) { // must repose (giga impact, hyper beam...)
            System.out.println(attacker.nickname + " must recover for its attack...");
            notAttack = true;
        }
        // sky drop
        if(defender.effectMoves.get(59) > 0 && defender.effectMoves.get(3) > 0) {
            attacker.effectMoves.set(59, 0);
            notAttack = true;
        }
        if(attackerMove != null) {
            if(attackerMove.getCode() == 37 && attacker.effectMoves.get(14) == 0) { // focus punch
                System.out.println(attacker.nickname + " lost its concentration!");
                attacker.reducePP(attackerMove,1, defender);
                notAttack = true;
            }
        }
        // flinched
        if(attacker.hasTemporalStatus(TemporalStatus.FLINCHED)) {
            System.out.println(attacker.nickname + " flinched!");
            if(attacker.hasAbility("STEADFAST")) {
                attacker.changeStat(4,1,true,false,defender);
            }
            notAttack = true;
            // paralyzed
        } else if(attacker.hasStatus(Status.PARALYZED) && 0.25 >= Math.random()) {
            System.out.println(attacker.nickname + " is paralyzed! It's unable to move!");
            notAttack = true;
        } else if(attacker.hasStatus(Status.FROZEN)) {
            System.out.println(attacker.nickname + " is frozen!");
            notAttack = true;
            // asleep
        } else if(attacker.hasStatus(Status.ASLEEP)) {
            System.out.println(attacker.nickname + " is asleep!");
            notAttack = true;
        } else {
            // infatuated
            if(attacker.hasTemporalStatus(TemporalStatus.INFATUATED)) {
                System.out.println(attacker.nickname + " is infatuated of " + defender.nickname + "!");
                if(0.5 >= Math.random()) {
                    System.out.println("Love prevents " + attacker.nickname + " from attacking!");
                    notAttack = true;
                }
            }
            // confused
            if(attacker.hasTemporalStatus(TemporalStatus.CONFUSED)) {
                System.out.println(attacker.nickname + " is confused!");
                if(0.33 >= Math.random()) {
                    System.out.println("It's so confused hurts itself!");
                    attacker.reduceHP(CalcDamageConfuse(attacker));
                    System.out.println(attacker.nickname + " HP: " + attacker.getPsActuales() + "/" + attacker.getHP());
                    notAttack = true;
                }
            }
        }
        if(notAttack && defender.effectMoves.get(59) == 0) {
            // cancel multi turn moves
            removeMultiTurnAttackEffects(attacker, attackerMove);
        }
        return notAttack;
    }

    private void itemEffectsAfterAttack(Pokemon attacker, Pokemon defender, Movement attackerMove, Movement defenderMove, int damage) {
        if(defender.hasItem("AIRBALLOON") && !defender.isFainted()) { // air balloon explodes
            System.out.println(defender.nickname + "' " + defender.item.name + " explodes!");
            defender.loseItem(false, false);
        }

        if(defender.canUseItem()) {
            if(defender.hasItem("EJECTBUTTON") && !defender.isFainted() && defender.getTeam().alivePokemon() > 1) { // eject button
                System.out.println(defender.nickname + " has a " + defender.item.name + "!");
                defender.loseItem(true, true);
                doChange(defender == user, defender.getTeam().choseRandomAliveMember(defender));
                battleResult = 5;
            }
            if(!defender.isFainted() && ((defender.hasItem("CELLBATTERY") && attackerMove.type.is("ELECTRIC"))
                    || (defender.hasItem("SNOWBALL") && attackerMove.type.is("ICE")))) { // cell battery and snow ball actives
                if(defender.changeStat(0,1,false,true, attacker)) defender.loseItem(true, true);
            }
            if(!defender.isFainted() && defender.hasItem("ABSORBBULB") && attackerMove.type.is("WATER")) { // absorb bulb actives
                if(defender.changeStat(2,1,false,true,attacker)) defender.loseItem(true, true);
            }
            if(!defender.isFainted() && defender.hasItem("LUMINOUSMOSS") && attackerMove.type.is("WATER")) { // luminous moss actives
                if(defender.changeStat(3,1,false,true,attacker)) defender.loseItem(true, true);
            }
            if(!defender.isFainted() && getEffectiveness(attacker,defender,attackerMove,false) >= 2.0) { // weakness policy
                if(defender.hasItem("WEAKNESSPOLICY")) {
                    boolean inc1 = defender.changeStat(0,2,false,true, attacker);
                    boolean inc2 = defender.changeStat(2,2,false,true, attacker);
                    if(!inc1 && !inc2) defender.loseItem(true, true);
                } else if(defender.hasItem("ENIGMABERRY") && !defender.hasAllHP()) {
                    defender.healHP(defender.getHP()/4,true,false,false, true);
                    defender.loseItem(true, true);
                }
            }
        }
        if(attacker.canUseItem()) {
            if(!attacker.isFainted() && attacker.hasItem("THROATSPRAY") && attackerMove.getFlags().contains("j")) { // throat spray actives
                if(attacker.changeStat(2,1,false,true, defender)) attacker.loseItem(true, true);
            }
            if(attacker.hasItem("LIFEORB") && !attacker.isFainted() && !attacker.hasAbility("MAGICGUARD")) { // life orb
                System.out.println(attacker.item.name + " damages " + attacker.nickname + "!");
                int d = (int) (damage*0.1);
                if(d == 0) d = 1;
                attacker.reduceHP(d);
            }
            if(!attacker.hasAllHP() && attacker.hasItem("SHELLBELL") && !attacker.isFainted()) {
                System.out.println(attacker.nickname + " recover HP from " + attacker.item.name + "!");
                int d = (int) (damage/8);
                if(d == 0) d = 1;
                attacker.healHP(d, true, true, false, true);
            }
        }

        // berries
        if(attackerMove.getCategory().equals(Category.PHYSICAL) && !defender.isFainted()) {
            if(defender.hasItem("JABOCABERRY") && defender.canUseItem()) {
                attacker.reduceHP(attacker.getHP()/8);
                defender.loseItem(true, true);
            } else if(defender.hasItem("KEEBERRY") && defender.canUseItem()) {
                if(defender.changeStat(1,1,false,true, attacker)) defender.loseItem(true, true);
            }
        }
        if(attackerMove.getCategory().equals(Category.SPECIAL) && !defender.isFainted()) {
            if(defender.hasItem("ROWAPBERRY") && defender.canUseItem()) {
                attacker.reduceHP(attacker.getHP()/8);
                defender.loseItem(true, true);
            } else if(defender.hasItem("MARANGABERRY") && defender.canUseItem()) {
                if(defender.changeStat(3,1,false,true, attacker)) defender.loseItem(true, true);
            }
        }

        // contact item effects
        if(attackerMove.getFlags().contains("a") && !(attacker.hasItem("PROTECTIVEPADS") && defender.canUseItem())) {
            if(defender.hasItem("ROCKYHELMET") && !attacker.isFainted() && !attacker.hasAbility("MAGICGUARD") && defender.canUseItem()) { // rocky helmet
                System.out.println(defender.nickname + " has a " + defender.item.name + "!");
                attacker.reduceHP(attacker.getHP()/6);
            }
            if(defender.hasItem("STICKYBARB") && !attacker.isFainted() && attacker.item == null && defender.canUseItem()) { // sticky barb
                System.out.println(attacker.nickname + " received a " + defender.item.name + " from " + defender.nickname + "!");
                attacker.giveItem(defender.item.getInternalName(), false);
                defender.loseItem(false, false);
            }
        }
    }

    private void abilityEffectsAfterAttack(Pokemon attacker, Pokemon defender, Movement attackerMove, Movement defenderMove, int damage) {
        // moxie
        if(attacker.hasAbility("MOXIE") && defender.isFainted()) {
            attacker.changeStat(0,1,false,true, defender);
        }
        // justified
        if(defender.hasAbility("JUSTIFIED") && attackerMove.type.is("DARK") && !defender.isFainted()) {
            defender.changeStat(0,1,false,true, attacker);
        }
        // rattled
        if(defender.hasAbility("RATTLED") && (attackerMove.type.is("BUG") || attackerMove.type.is("GHOST") ||
                attackerMove.type.is("DARK")) && !defender.isFainted()) {
            defender.changeStat(4,1,false,true, attacker);
        }
        // weak armor
        if(defender.hasAbility("WEAKARMOR") && attackerMove.getCategory().equals(Category.PHYSICAL) && !defender.isFainted()) {
            defender.changeStat(1,1,false,true, attacker);
            defender.changeStat(4,2,false,true, attacker);
        }
        // cursed body
        if(defender.hasAbility("CURSEDBODY") && Math.random() <= 0.3 && attacker.effectMoves.get(52) == 0 &&
                !attackerMove.hasName("STRUGGLE") && attacker.cursedBodyMove == null) {
            attacker.cursedBodyMove = attackerMove;
            attacker.increaseEffectMove(52);
            System.out.println(attackerMove.name + " of " + attacker.nickname + " is disabled!");
        }
        // contact ability effects
        if(attackerMove.getFlags().contains("a") && !(attacker.hasItem("PROTECTIVEPADS") && attacker.canUseItem())) {
            if(defender.hasAbility("STATIC") && attacker.canParalyze(false, defender) && Math.random() <= 0.3) { // static
                attacker.causeStatus(Status.PARALYZED, defender, false);
            }
            if(defender.hasAbility("FLAMEBODY") && attacker.canBurn(false, defender) && Math.random() <= 0.3) { // flame body
                attacker.causeStatus(Status.BURNED, defender, false);
            }
            if(defender.hasAbility("POISONPOINT") && attacker.canPoison(false, defender) && Math.random() <= 0.3) { // poison point
                attacker.causeStatus(Status.POISONED, defender, false);
            }
            if(attacker.hasAbility("POISONTOUCH") && defender.canPoison(false, attacker) && Math.random() <= 0.3) { // poison touch
                defender.causeStatus(Status.POISONED, attacker, false);
            }
            if(defender.hasAbility("EFFECTSPORE") && !attacker.hasAbility("OVERCOAT") && !(attacker.hasItem("SAFETYGOOGLES") && attacker.canUseItem()) && attacker.hasType("GRASS")) { // poison point
                if(Math.random() <= 0.1 && attacker.canPoison(false, defender)) {
                    attacker.causeStatus(Status.POISONED, defender, false);
                } else if(Math.random() <= 0.1 && attacker.canSleep(false, defender)) {
                    attacker.causeStatus(Status.ASLEEP, defender, false);
                } else if(Math.random() <= 0.1 && attacker.canParalyze(false, defender)) {
                    attacker.causeStatus(Status.PARALYZED, defender, false);
                }
            }
            if(defender.hasAbility("CUTECHARM") && attacker.canInfatuate(false, defender) && Math.random() <= 0.3) { // cute charm
                attacker.causeTemporalStatus(TemporalStatus.INFATUATED, defender);
            }
            if(defender.hasAbility("STENCH") && attacker.canFlinch(attacker) && Math.random() <= 0.1) { // stench
                attacker.causeTemporalStatus(TemporalStatus.FLINCHED, defender);
            }
            if(defender.hasAbility("AFTERMATH") && defender.isFainted() && !attacker.isFainted() && !attacker.hasAbility("DAMP")) { // aftermath
                attacker.reduceHP(attacker.getHP()/4);
            }
            if(defender.hasAbility("PICKPOCKET") && !defender.isFainted()) { // pickpocket
                moveEffects.stealItem(defender,attacker);
            }
        }
    }

    private void moveEffectsAfterAttack(Pokemon attacker, Pokemon defender, Movement attackerMove, Movement defenderMove) {
        if(attackerMove.hasName("RAPIDSPIN") && !attacker.isFainted()) { // rapid spin
            attacker.rapidSpin();
        }
        if(attackerMove.hasName("UPROAR") && effectFieldMoves.get(1) == 1 && !defender.isFainted() && !attacker.isFainted()) { // uproar wake up all
            if(attacker.hasStatus(Status.ASLEEP)) attacker.healPermanentStatus();
            if(defender.hasStatus(Status.ASLEEP)) defender.healPermanentStatus();
        }
        if(defender.effectMoves.get(14) > 0 && defenderMove.getCode() == 37) { // focus punch
            defender.effectMoves.set(14, 0);
        }
        if(attackerMove.getCode() == 136 && !defender.isFainted() && defender.hasStatus(Status.ASLEEP)) { // wake up slap
            defender.healPermanentStatus();
        }
        if(attackerMove.getCode() == 204 && !defender.isFainted() && defender.hasStatus(Status.PARALYZED)) { // smelling salts
            defender.healPermanentStatus();
        }
        if(defender.isFainted() && attacker.effectMoves.get(31) > 0) { // grudge
            System.out.println(attackerMove.name + " lost all its PPs!");
            attacker.reducePP(attackerMove,-1, defender);
            attacker.effectMoves.set(31, 0);
        }
        if(defender.isFainted() && defender.effectMoves.get(53) > 0 && !attacker.isFainted()) { // destiny bond
            System.out.println(defender.nickname + " took " + attacker.nickname + " with itself!");
            attacker.reduceHP(-1);
            defender.effectMoves.set(53, 0);
        }
        if(attackerMove.getCode() == 255 && !attacker.isFainted() && attacker.getTeam().alivePokemon() > 1 && defender.getTeam().alivePokemon() > 0) { // volt switch, u-turn
            int chosenIndex = -1;
            do {
                userTeam.showTeam();
                chosenIndex = Integer.parseInt(in.nextLine());
                if(chosenIndex != 0) {
                    if(userTeam.getPokemon(chosenIndex-1).isFainted()) {
                        System.out.println(userTeam.getPokemon(chosenIndex-1).nickname + " has no energy for fight!");
                        chosenIndex = -1;
                    } else if(userTeam.getPokemon(chosenIndex-1) == user) {
                        System.out.println(userTeam.getPokemon(chosenIndex-1).nickname + " is already in battle!");
                        chosenIndex = -1;
                    }
                }
            } while(chosenIndex < 0 || chosenIndex > userTeam.getPokemonTeam().size());
            doChange(attacker == user, chosenIndex);
            //battleResult = 5;
        }
    }

    public void useAutomatic(Pokemon target, Pokemon other) {
        // mental herb
        if(target.hasItem("MENTALHERB") && target.canUseItem()) {
            if(target.hasTemporalStatus(TemporalStatus.INFATUATED)) {
                target.healTempStatus(TemporalStatus.INFATUATED, true);
                target.loseItem(true, true);
            }
            if(target.effectMoves.get(37) > 0 || target.effectMoves.get(1) > 0 || target.effectMoves.get(17) > 0) { // TODO: torment, heal block, cursed body
                target.effectMoves.set(37, 0);
                target.effectMoves.set(1, 0);
                target.effectMoves.set(17, 0);
                target.disabledMove = null;
                target.cursedBodyMove = null;
                System.out.println(target.nickname + "'s status were reestablished!");
                target.loseItem(true, true);
            }
        }
        // white herb
        if(target.hasItem("WHITEHERB") && target.canUseItem()) {
            boolean negativeStats = false;
            for(int i=0;i<target.getStatChanges().size();i++) {
                if(target.getStatChanges().get(i) < 0) {
                    negativeStats = true;
                    target.getStatChanges().set(i, 0);
                    System.out.println(target.nickname + "'s stats changes were reestablished!");
                }
            }
            if(negativeStats) target.loseItem(true, true);
        }
        // abilities heal
        if((target.hasStatus(Status.POISONED) || target.hasStatus(Status.BADLYPOISONED)) && target.hasAbility("IMMUNITY") ||
                (target.hasStatus(Status.PARALYZED) && target.hasAbility("LIMBER")) || (target.hasStatus(Status.FROZEN) && target.hasAbility("MAGMAARMOR")) ||
                (target.hasStatus(Status.BURNED) && (target.hasAbility("WATERVEIL") || target.hasAbility("WATERBUBBLE"))) ||
                (target.hasStatus(Status.ASLEEP) && (target.hasAbility("INSOMNIA") || target.hasAbility("VITALSPIRIT")))) {
            target.healPermanentStatus();
        }
        if(target.hasTemporalStatus(TemporalStatus.CONFUSED) && target.hasAbility("OWNTEMPO")) {
            target.healTempStatus(TemporalStatus.CONFUSED, true);
        }
        useBerry(target,other.hasAbility("UNNERVE"));
    }

    public boolean useBerry(Pokemon target, boolean unnerve) {
        // berries
        if(!unnerve && target.canUseItem()) {
            if((target.hasItem("CHERIBERRY") && target.hasStatus(Status.PARALYZED)) || // berries that heal permanent status
                    (target.hasItem("CHESTOBERRY") && target.hasStatus(Status.ASLEEP)) ||
                    (target.hasItem("PECHABERRY") && (target.hasStatus(Status.POISONED) || target.hasStatus(Status.BADLYPOISONED))) ||
                    (target.hasItem("RAWSTBERRY") && target.hasStatus(Status.BURNED)) ||
                    (target.hasItem("ASPEARBERRY") && target.hasStatus(Status.FROZEN))) {
                target.healPermanentStatus();
                target.loseItem(true, true);
                return true;
            }
            if(target.hasItem("LUMBERRY")) {
                if(!target.hasStatus(Status.FINE) && !target.hasStatus(Status.FAINTED)) {
                    target.healPermanentStatus(); // heal all permanent status
                    target.loseItem(true, true);
                }
                if(target.hasTemporalStatus(TemporalStatus.CONFUSED)) {
                    target.healTempStatus(TemporalStatus.CONFUSED, true); // heal confusion
                    target.loseItem(true, true);
                }
                return true;
            }
            if(target.hasItem("PERSIMBERRY") && target.hasTemporalStatus(TemporalStatus.CONFUSED)) {
                target.healTempStatus(TemporalStatus.CONFUSED, true); // heal confusion
                target.loseItem(true, true);
                return true;
            }
            if(target.getPercentHP() <= 50.0) { // berries that recover HP when HP are under 50%
                if(target.hasItem("ORANBERRY")) {
                    target.healHP(10,true,false,false, true);
                    target.loseItem(true, true);
                    return true;
                }
                if(target.hasItem("SITRUSBERRY")) {
                    target.healHP(target.getHP()/4,true,false,false, true);
                    target.loseItem(true, true);
                    return true;
                }
            }
            if(target.moveWithoutPP() != null && target.hasItem("LEPPABERRY")) { // berry restore PP
                target.healPP(target.getIndexMove(target.moveWithoutPP().getInternalName()),10);
                target.loseItem(true, true);
                return true;
            }
            if(target.getPercentHP() <= 25.0 || (target.getPercentHP() <= 50.0) && target.hasAbility("GLUTTONY")) { // berries that actives when HP are under 25%
                if(target.hasItem("FIGYBERRY")) {
                    target.healHP(target.getHP()/3,true,false,false, true);
                    target.loseItem(true, true);
                    if(target.canConfuse(false,null) && (target.getNature("MODEST") || target.getNature("TIMID") || target.getNature("CALM") || target.getNature("BOLD"))) {
                        target.causeTemporalStatus(TemporalStatus.CONFUSED,null);
                    }
                    return true;
                }
                if(target.hasItem("WIKIBERRY")) {
                    target.healHP(target.getHP()/3,true,false,false, true);
                    target.loseItem(true, true);
                    if(target.canConfuse(false,null) && (target.getNature("ADAMANT") || target.getNature("IMPISH") || target.getNature("CAREFUL") || target.getNature("JOLLY"))) {
                        target.causeTemporalStatus(TemporalStatus.CONFUSED,null);
                    }
                    return true;
                }
                if(target.hasItem("MAGOBERRY")) {
                    target.healHP(target.getHP()/3,true,false,false, true);
                    target.loseItem(true, true);
                    if(target.canConfuse(false,null) && (target.getNature("BRAVE") || target.getNature("RELAXED") || target.getNature("QUIET") || target.getNature("SASSY"))) {
                        target.causeTemporalStatus(TemporalStatus.CONFUSED,null);
                    }
                    return true;
                }
                if(target.hasItem("AGUAVBERRY")) {
                    target.healHP(target.getHP()/3,true,false,false, true);
                    target.loseItem(true, true);
                    if(target.canConfuse(false,null) && (target.getNature("NAUGHTY") || target.getNature("LAX") || target.getNature("RASH") || target.getNature("NAIVE"))) {
                        target.causeTemporalStatus(TemporalStatus.CONFUSED,null);
                    }
                    return true;
                }
                if(target.hasItem("IAPAPABERRY")) {
                    target.healHP(target.getHP()/3,true,false,false, true);
                    target.loseItem(true, true);
                    if(target.canConfuse(false,null) && (target.getNature("LONELY") || target.getNature("MILD") || target.getNature("GENTLE") || target.getNature("HASTY"))) {
                        target.causeTemporalStatus(TemporalStatus.CONFUSED,null);
                    }
                    return true;
                }
                if(target.hasItem("LIECHIBERRY")) { // increase attack
                    target.changeStat(0,1,true,true,null);
                    target.loseItem(true, true);
                    return true;
                }
                if(target.hasItem("GANLONBERRY")) { // increase defense
                    target.changeStat(1,1,true,true,null);
                    target.loseItem(true, true);
                    return true;
                }
                if(target.hasItem("SALACBERRY")) { // increase speed
                    target.changeStat(4,1,true,true,null);
                    target.loseItem(true, true);
                    return true;
                }
                if(target.hasItem("PETAYABERRY")) { // increase special attack
                    target.changeStat(2,1,true,true,null);
                    target.loseItem(true, true);
                    return true;
                }
                if(target.hasItem("APICOTBERRY")) { // increase special defense
                    target.changeStat(3,1,true,true,null);
                    target.loseItem(true, true);
                    return true;
                }
                if(target.hasItem("LANSATBERRY")) { // critical index increase
                    target.criticalIndex += 2;
                    if(target.criticalIndex > 4) target.criticalIndex = 4;
                    System.out.println(target.nickname + " critical index increased!");
                    target.loseItem(true, true);
                    return true;
                }
                if(target.hasItem("STARFBERRY") && !target.statsAreMaximum()) { // increase random stat
                    Random rand = new Random();
                    int randomStat = -1;
                    do {
                        randomStat = rand.nextInt(target.getStatChanges().size());
                    } while(target.getStatChanges().get(randomStat) == 6);
                    target.changeStat(randomStat, 2, true, true, target);
                    target.loseItem(true, true);
                    return true;
                }
                if(target.hasItem("MICLEBERRY")) { // next move will increase accuracy
                    target.effectMoves.set(47, 1);
                    target.loseItem(true, true);
                    return true;
                }
                if(target.hasItem("CUSTAPBERRY")) { // next move will attack first
                    target.effectMoves.set(47, 1);
                    target.loseItem(true, true);
                    return true;
                }
            }
        }
        return false;
    }

    private void startMove(Pokemon target, Pokemon other, Movement attackerMove) {
        // thaw
        double posibility = 0.2;
        if(weather.hasWeather(Weathers.SUNLIGHT) || weather.hasWeather(Weathers.HEAVYSUNLIGHT)) {
            posibility = 0.3;
        }
        if(target.hasStatus(Status.FROZEN)) {
            if(posibility >= Math.random() || attackerMove.getFlags().contains("g")) {
                System.out.println(target.nickname + " thaws!");
                target.healPermanentStatus();
            }
        }
        // wake up
        if((0.33 >= Math.random() && target.sleepTurns == 1 && target.effectMoves.get(32) == 0) || target.hasAbility("EARLYBIRD")) {
            target.healPermanentStatus();
        } else if((0.66 >= Math.random() && target.sleepTurns == 2) || target.effectMoves.get(32) > 0) {
            target.healPermanentStatus();
            target.effectMoves.set(32, 0);
        } else if(target.sleepTurns >= 3) {
            target.healPermanentStatus();
        }

        int durationTrap = 5;
        if(other.hasItem("GRIPCLAW") && other.canUseItem()) {
            durationTrap = 7;
        }
        // fire spin
        if(0.5 >= Math.random() && target.effectMoves.get(4) == 4 && !(other.hasItem("GRIPCLAW") && other.canUseItem())) {
            target.healTempStatus(TemporalStatus.PARTIALLYTRAPPED, false);
            System.out.println(target.nickname + " was freed from Fire Spin!");
            target.effectMoves.set(4, 0);
        } else if(target.effectMoves.get(4) >= durationTrap) {
            target.healTempStatus(TemporalStatus.PARTIALLYTRAPPED, false);
            System.out.println(target.nickname + " was freed from Fire Spin!");
            target.effectMoves.set(4, 0);
        }
        // wrap
        if(0.5 >= Math.random() && target.effectMoves.get(16) == 4 && !(other.hasItem("GRIPCLAW") && other.canUseItem())) {
            target.healTempStatus(TemporalStatus.PARTIALLYTRAPPED, false);
            System.out.println(target.nickname + " was freed from Wrap!");
            target.effectMoves.set(16, 0);
        } else if(target.effectMoves.get(16) >= durationTrap) {
            target.healTempStatus(TemporalStatus.PARTIALLYTRAPPED, false);
            System.out.println(target.nickname + " was freed from Wrap!");
            target.effectMoves.set(16, 0);
        }
        // sand tomb
        if(0.5 >= Math.random() && target.effectMoves.get(21) == 4 && !(other.hasItem("GRIPCLAW") && other.canUseItem())) {
            target.healTempStatus(TemporalStatus.PARTIALLYTRAPPED, false);
            System.out.println(target.nickname + " was freed from Sand Tomb!");
            target.effectMoves.set(21, 0);
        } else if(target.effectMoves.get(21) >= durationTrap) {
            target.healTempStatus(TemporalStatus.PARTIALLYTRAPPED, false);
            System.out.println(target.nickname + " was freed from Sand Tomb!");
            target.effectMoves.set(21, 0);
        }
        // clamp
        if(0.5 >= Math.random() && target.effectMoves.get(49) == 4 && !(other.hasItem("GRIPCLAW") && other.canUseItem())) {
            target.healTempStatus(TemporalStatus.PARTIALLYTRAPPED, false);
            System.out.println(target.nickname + " was freed from Clamp!");
            target.effectMoves.set(49, 0);
        } else if(target.effectMoves.get(49) >= durationTrap) {
            target.healTempStatus(TemporalStatus.PARTIALLYTRAPPED, false);
            System.out.println(target.nickname + " was freed from Clamp!");
            target.effectMoves.set(49, 0);
        }
        // whirlpool
        if(0.5 >= Math.random() && target.effectMoves.get(50) == 4 && !(other.hasItem("GRIPCLAW") && other.canUseItem())) {
            target.healTempStatus(TemporalStatus.PARTIALLYTRAPPED, false);
            System.out.println(target.nickname + " was freed from Whirlpool!");
            target.effectMoves.set(50, 0);
        } else if(target.effectMoves.get(50) >= durationTrap) {
            target.healTempStatus(TemporalStatus.PARTIALLYTRAPPED, false);
            System.out.println(target.nickname + " was freed from Whirlpool!");
            target.effectMoves.set(50, 0);
        }
        // bind
        if(0.5 >= Math.random() && target.effectMoves.get(51) == 4 && !(other.hasItem("GRIPCLAW") && other.canUseItem())) {
            target.healTempStatus(TemporalStatus.PARTIALLYTRAPPED, false);
            System.out.println(target.nickname + " was freed from Bind!");
            target.effectMoves.set(51, 0);
        } else if(target.effectMoves.get(51) >= durationTrap) {
            target.healTempStatus(TemporalStatus.PARTIALLYTRAPPED, false);
            System.out.println(target.nickname + " was freed from Bind!");
            target.effectMoves.set(51, 0);
        }
        // infestation
        if(0.5 >= Math.random() && target.effectMoves.get(61) == 4 && !(other.hasItem("GRIPCLAW") && other.canUseItem())) {
            target.healTempStatus(TemporalStatus.PARTIALLYTRAPPED, false);
            System.out.println(target.nickname + " was freed from Infestation!");
            target.effectMoves.set(61, 0);
        } else if(target.effectMoves.get(61) >= durationTrap) {
            target.healTempStatus(TemporalStatus.PARTIALLYTRAPPED, false);
            System.out.println(target.nickname + " was freed from Infestation!");
            target.effectMoves.set(61, 0);
        }
    }

    private int checkFaint() {
        /* 0 -> nothing
           1 -> win
           2 -> lose
           3 -> run (only for wild battles)
           4 -> caught (only for wild battles)
           5 -> change for fainted Pokémon
        */
        if(userTeam.isTeamDefeated()) {
            // lose
            endBattle = true;
            battleResult = 2;
        } else if(rivalTeam.isTeamDefeated()) {
            // win
            endBattle = true;
            battleResult = 1;
        } else if(battleResult == 4) {
            // caught
            endBattle = true;
        } else if(battleResult == 5) { // force change
            battleResult = 0;
            return 5;
        } else if(firstAttacker.effectMoves.get(12) > 0 || secondAttacker.effectMoves.get(12) > 0) {
            // run
            endBattle = true;
            battleResult = 3;
        } else if(user.isFainted()) {
            while(!changePokemon(true)) {
                System.out.println("Select another Pokemon: ");
            }
            return 5;
        }

        return battleResult;
    }

    private void determinePriority(Movement userMove, Movement rivalMove) {
        boolean userFirst;
        // moves priority
        if(userMove.getPriority() > rivalMove.getPriority()) {
            userFirst = true;
        } else if(userMove.getPriority() < rivalMove.getPriority()) {
            userFirst = false;
        // lagging tail and full incense
        } else if((rival.hasItem("LAGGINGTAIL") || rival.hasItem("FULLINCENSE") ) && (!user.hasItem("LAGGINGTAIL") && !user.hasItem("FULLINCENSE"))) {
            userFirst = true;
        } else if((user.hasItem("LAGGINGTAIL") || user.hasItem("FULLINCENSE") ) && (!rival.hasItem("LAGGINGTAIL") && !rival.hasItem("FULLINCENSE"))) {
            userFirst = false;
            // quick claw and custap berry
        } else if((rival.hasItem("QUICKCLAW") || rival.effectMoves.get(48) > 0) && (!user.hasItem("QUICKCLAW") && !(user.effectMoves.get(48) > 0))) {
            userFirst = false;
        } else if((user.hasItem("QUICKCLAW") || user.effectMoves.get(48) > 0) && (!rival.hasItem("QUICKCLAW") && !(rival.effectMoves.get(48) > 0))) {
            userFirst = true;
            // STALL ability
        } else if(rival.hasAbility("STALL") && !user.hasAbility("STALL")) {
            userFirst = true;
        } else if(user.hasAbility("STALL") && !rival.hasAbility("STALL")) {
            userFirst = false;
        // pokemon speed
        } else if(user.getVelocity() > rival.getVelocity()) {
            userFirst = effectFieldMoves.get(7) <= 0; // trick room
        } else if(user.getVelocity() < rival.getVelocity()) {
            userFirst = effectFieldMoves.get(7) > 0; // trick room
        // random
        } else if(Math.random() < 0.5) {
            userFirst = true;
        } else {
            userFirst = false;
        }

        if(userFirst) {
            firstAttacker = user;
            secondAttacker = rival;
            firstMove = userMove;
            secondMove = rivalMove;
        } else {
            firstAttacker = rival;
            secondAttacker = user;
            firstMove = rivalMove;
            secondMove = userMove;
        }
    }

    private void changeMoveType(Pokemon attacker,Pokemon defender, Movement attackerMove) {
        if(attacker.hasAbility("NORMALIZE")) {
            attackerMove.changeType(attacker.utils.getType("NORMAL"));
        }
        if(attackerMove.hasName("TECHNOBLAST") && attacker.specieNameIs("GENESECT")) {
            if(attacker.hasItem("DOUSEDRIVE") && attacker.canUseItem()) attackerMove.changeType(attacker.utils.getType("WATER"));
            if(attacker.hasItem("SHOCKDRIVE") && attacker.canUseItem()) attackerMove.changeType(attacker.utils.getType("ELECTRIC"));
            if(attacker.hasItem("BURNDRIVE") && attacker.canUseItem()) attackerMove.changeType(attacker.utils.getType("FIRE"));
            if(attacker.hasItem("CHILLDRIVE") && attacker.canUseItem()) attackerMove.changeType(attacker.utils.getType("ICE"));
        }
        if(attackerMove.getCode() == 164 && attacker.canUseItem()) { // natural gift
            if(attacker.hasItem("CHERIBERRY") || attacker.hasItem("OCCABERRY")) {
                attackerMove.changeType(attacker.utils.getType("FIRE"));
            } else if(attacker.hasItem("CHESTOBERRY") || attacker.hasItem("PASSHOBERRY")) {
                attackerMove.changeType(attacker.utils.getType("WATER"));
            } else if(attacker.hasItem("PECHABERRY") || attacker.hasItem("WACANBERRY")) {
                attackerMove.changeType(attacker.utils.getType("ELECTRIC"));
            } else if(attacker.hasItem("RAWSTBERRY") || attacker.hasItem("RINDOBERRY") || attacker.hasItem("LIECHIBERRY")) {
                attackerMove.changeType(attacker.utils.getType("GRASS"));
            } else if(attacker.hasItem("ASPEARBERRY") || attacker.hasItem("POMEGBERRY") || attacker.hasItem("YACHEBERRY")
                    || attacker.hasItem("GANLONBERRY")) {
                attackerMove.changeType(attacker.utils.getType("ICE"));
            } else if(attacker.hasItem("LEPPABERRY") || attacker.hasItem("KELPSYBERRY") || attacker.hasItem("CHOPLEBERRY")
                    || attacker.hasItem("SALACBERRY")) {
                attackerMove.changeType(attacker.utils.getType("FIGHTING"));
            } else if(attacker.hasItem("ORANBERRY") || attacker.hasItem("QUALOTBERRY") || attacker.hasItem("KEBIABERRY")
                    || attacker.hasItem("PETAYABERRY")) {
                attackerMove.changeType(attacker.utils.getType("POISON"));
            } else if(attacker.hasItem("PERSIMBERRY") || attacker.hasItem("HONDEWBERRY") || attacker.hasItem("SHUCABERRY")
                    || attacker.hasItem("APICOTBERRY")) {
                attackerMove.changeType(attacker.utils.getType("GROUND"));
            } else if(attacker.hasItem("LUMBERRY") || attacker.hasItem("GREPABERRY") || attacker.hasItem("COBABERRY")
                    || attacker.hasItem("LANSATBERRY")) {
                attackerMove.changeType(attacker.utils.getType("FLYING"));
            } else if(attacker.hasItem("SITRUSBERRY") || attacker.hasItem("TAMATOBERRY") || attacker.hasItem("PAYAPABERRY")
                    || attacker.hasItem("STARFBERRY")) {
                attackerMove.changeType(attacker.utils.getType("PSYCHIC"));
            } else if(attacker.hasItem("FIGYBERRY") || attacker.hasItem("TANGABERRY") || attacker.hasItem("ENIGMABERRY")) {
                attackerMove.changeType(attacker.utils.getType("BUG"));
            } else if(attacker.hasItem("WIKIBERRY") || attacker.hasItem("CHARTIBERRY") || attacker.hasItem("MICLEBERRY")) {
                attackerMove.changeType(attacker.utils.getType("ROCK"));
            } else if(attacker.hasItem("MAGOBERRY") || attacker.hasItem("KASIBBERRY") || attacker.hasItem("CUSTAPBERRY")) {
                attackerMove.changeType(attacker.utils.getType("GHOST"));
            } else if(attacker.hasItem("AGUAVBERRY") || attacker.hasItem("HABANBERRY") || attacker.hasItem("JABOCABERRY")) {
                attackerMove.changeType(attacker.utils.getType("DRAGON"));
            } else if(attacker.hasItem("IAPAPABERRY") || attacker.hasItem("COLBURBERRY") || attacker.hasItem("ROWAPBERRY")
                    || attacker.hasItem("MARANGABERRY")) {
                attackerMove.changeType(attacker.utils.getType("DARK"));
            } else if(attacker.hasItem("BABIRIBERRY")) {
                attackerMove.changeType(attacker.utils.getType("STEEL"));
            } else if(attacker.hasItem("CHILANBERRY")) {
                attackerMove.changeType(attacker.utils.getType("NORMAL"));
            } else {
                attackerMove.changeType(attacker.utils.getType("FAIRY"));
            }
        }
        if(attackerMove.getCode() == 206) { // weather ball
            if(weather.hasWeather(Weathers.SUNLIGHT) || weather.hasWeather(Weathers.HEAVYSUNLIGHT)) attackerMove.changeType(attacker.utils.getType("FIRE"));
            if(weather.hasWeather(Weathers.RAIN) || weather.hasWeather(Weathers.HEAVYRAIN)) attackerMove.changeType(attacker.utils.getType("WATER"));
            if(weather.hasWeather(Weathers.HAIL)) attackerMove.changeType(attacker.utils.getType("ICE"));
            if(weather.hasWeather(Weathers.SANDSTORM)) attackerMove.changeType(attacker.utils.getType("ROCK"));
        }
        if(attackerMove.getCode() == 247) { // hidden power
            attackerMove.changeType(attacker.getHiddenPowerType());
        }
        if(effectFieldMoves.get(8) > 8 && attackerMove.type.is("NORMAL")) { // ion deluge
            attackerMove.changeType(attacker.utils.getType("ELECTRIC"));
        }
    }

    private int CalcDamage(Pokemon attacker, Pokemon defender, Movement move, boolean mefirst) {
        int damage = 0;
        int attack = 0;
        int defense = 0;
        double stab = 1.0;
        int variation = attacker.utils.getRandomNumberBetween(85,101);
        boolean critical = isCriticalHit(attacker,defender,move);
        // change move type
        //changeMoveType(attacker,defender,move);
        // move power
        int power = move.getPower();

        int changePower = moveChangingPower(attacker,defender,move);
        // moves with variable power
        if(changePower == 0) {
            return 0;
        } else if(changePower != -1) {
            power = changePower;
        }
        // moves power changes for external reasons
        power = movePowerByExternalReasons(attacker, defender, move, power, critical, mefirst);

        // get effectiveness
        double effectiveness = getEffectiveness(attacker, defender, move, true);
        // STAB
        if(attacker.hasType(move.type.getInternalName())) {
            stab = 1.5;
            if(attacker.hasAbility("ADAPTABILITY")) stab = 2;
        }

        // move category, physical or special?
        if(move.getCategory() == Category.PHYSICAL) {
            attack = attacker.getAttack(critical, defender.hasAbility("UNAWARE") && !attacker.hasAbility("MOLDBREAKER"),move);
            if(move.getCode() == 173) {
                attack = defender.getAttack(critical, defender.hasAbility("UNAWARE") && !attacker.hasAbility("MOLDBREAKER"),move);
                if(attacker.hasStatus(Status.BURNED) && !attacker.hasAbility("GUTS") && move.getCode() != 250) attack /= 2.0;
            }
            defense = defender.getDefense(critical, move.getCode() == 125, attacker.hasAbility("UNAWARE") && !attacker.hasAbility("MOLDBREAKER"),attacker.hasAbility("MOLDBREAKER")); // chip away
        } else if(move.getCategory() == Category.SPECIAL) {
            attack = attacker.getSpecialAttack(critical, defender.hasAbility("UNAWARE") && !attacker.hasAbility("MOLDBREAKER"));
            defense = defender.getSpecialDefense(critical, attacker.hasAbility("UNAWARE") && !attacker.hasAbility("MOLDBREAKER"));
            if(move.getCode() == 224) {
                defense = defender.getDefense(critical, false, attacker.hasAbility("UNAWARE") && !attacker.hasAbility("MOLDBREAKER"),attacker.hasAbility("MOLDBREAKER"));
            }
        }
        // calculate damage
        double dmg = (0.01*stab*effectiveness*variation*(((attack*power*(0.2*attacker.getLevel()+1))/(25*defense))+2));
        // increase damage
        if(attacker.hasItem("EXPERTBELT") && attacker.canUseItem() && effectiveness >= 2.0) {
            dmg *= 1.2;
        }
        if(defender.effectMoves.get(36) > 0 && (move.hasName("EARTHQUAKE") || move.hasName("MAGNITUDE"))) { // dig
            dmg *= 2;
        }
        if(defender.effectMoves.get(46) > 0 && (move.hasName("WHIRLPOOL") || move.hasName("SURF"))) { // dive
            dmg *= 2;
        }
        if(defender.hasAbility("FLUFFY") && move.type.is("FIRE") && !attacker.hasAbility("MOLDBREAKER")) {
            dmg *= 2;
        }
        if(defender.effectMoves.get(44) > 0 && (move.hasName("TWISTER") || move.hasName("SKYUPPERCUT") || move.hasName("GUST")
                || move.hasName("THUNDER") || move.hasName("THOUSANDARROWS"))) { // fly/bounce
            dmg *= 2;
        }
        // reduce damage
        if(defender.hasAbility("FLUFFY") && move.getFlags().contains("a") && !attacker.hasAbility("MOLDBREAKER") &&
                !(attacker.hasItem("PROTECTIVEPADS") && attacker.canUseItem())) {
            dmg *= 0.5;
        }
        if(defender.hasAbility("THICKFAT") && !attacker.hasAbility("MOLDBREAKER") && (move.type.is("FIRE") || move.type.is("ICE"))) {
            dmg *= 0.5;
        }
        if(defender.hasAbility("MULTISCALE") && defender.hasAllHP() && !attacker.hasAbility("MOLDBREAKER")) {
            dmg *= 0.5;
        }
        if((defender.hasAbility("HEATPROOF") || defender.hasAbility("WATERBUBBLE")) && move.type.is("FIRE") && !attacker.hasAbility("MOLDBREAKER")) {
            dmg *= 0.5;
        }
        if(defender.hasAbility("FURCOAT") && !attacker.hasAbility("MOLDBREAKER")) {
            if(move.getCategory().equals(Category.PHYSICAL) || move.getCode() == 224) dmg *= 0.5;
        }
        if(defender.getTeam().effectTeamMoves.get(18) > 0 && !attacker.hasAbility("INFILTRATOR") &&
                (defender.getTeam().effectTeamMoves.get(5) > 0 || defender.getTeam().effectTeamMoves.get(4) > 0)) { // aurora veil
            dmg *= 0.5;
        }

        // critical hit
        if(critical && effectiveness > 0.0) {
            dmg *= 1.5;
            if(attacker.hasAbility("SNIPER")) dmg *= 1.5;
            System.out.println("A critical hit!");
        }

        // the minimum damage is always 1
        if(effectiveness > 0.0) {
            damage = (int) dmg;
            if(dmg < 1.0 && dmg > 0.0) {
                damage = 1;
            }
        } else {
            return 0;
        }

        if(resistsWith1HP(defender, attacker, damage, move)) {
            damage = defender.getPsActuales()-1;
        }

        if(critical && defender.hasAbility("ANGERPOINT") && !defender.isFainted()) {
            defender.changeStat(0, 12, true, true, attacker);
        }

        return damage;
    }

    public boolean resistsWith1HP(Pokemon defender, Pokemon other, int damage, Movement move) {
        if(defender.getPsActuales()-damage <= 0) {
            if(defender.effectMoves.get(1) == 1) { // endure
                return true;
            }
            if(defender.hasAllHP() && defender.hasItem("FOCUSSASH")) { // focus sash
                return defender.useItem(true, true, true);
            }
            if(defender.hasAllHP() && defender.hasAbility("STURDY") && !other.hasAbility("MOLDBREAKER")) { // sturdy
                return true;
            }
            if(defender.hasItem("FOCUSBAND") && Math.random() <= 0.1) { // focus band
                return defender.useItem(false, true, true);
            }
            if(move.getCode() == 214) { // false swipe
                return true;
            }
            //TODO: false swipe, etc...
        }

        return false;
    }

    private int moveChangingPower(Pokemon attacker, Pokemon defender, Movement move) {
        int p = -1;
        if(move.getCode() == 31) { // beat up
            p = 5+(attacker.getSpecie().stats.get(1)/10);
        }
        if(move.getCode() == 45) { // flail, reversal
            if(attacker.getPercentHP() >= 68.75) {
                p = 20;
            } else if(attacker.getPercentHP() >= 35.42) {
                p = 40;
            } else if(attacker.getPercentHP() >= 20.83) {
                p = 80;
            } else if(attacker.getPercentHP() >= 10.42) {
                p = 100;
            } else if(attacker.getPercentHP() >= 4.17) {
                p = 150;
            } else {
                p = 200;
            }
        }
        if(move.getCode() == 54) { // water spout, eruption
            p = move.getPower()*(attacker.getPsActuales()/attacker.getHP());
            if(p >= 0.0 && p < 1.0) {
                p = 1;
            }
        }
        if(move.getCode() == 55 && defender.getPercentHP() <= 50.0) { // brine
            p = move.getPower()*2;
        }
        if(move.getCode() == 68 && attacker.effectMoves.get(15) > 0) {
            p = move.getPower()*2;
            attacker.effectMoves.set(15, 0);
        }
        if(move.getCode() == 70 && (defender.hasStatus(Status.POISONED) || defender.hasStatus(Status.BADLYPOISONED))) { // venoshock
            p = move.getPower()*2;
        }
        if(move.getCode() == 71 && defender.effectMoves.get(9) > 0) { // assurance
            p = move.getPower()*2;
        }
        if(move.getCode() == 84 && attacker.previousDamage > 0) { // revenge
            p = move.getPower()*2;
        }
        if(move.getCode() == 91) { // spit up
            p = 100*attacker.stockpile;
        }
        if(move.getCode() == 103) { // electro ball
            double vel = (double) attacker.getVelocity()/defender.getVelocity();
            p = 40;
            if(vel < 2.0) {
                p = 60;
            } else if(vel < 3.0) {
                p = 80;
            } else if(vel < 4.0) {
                p = 120;
            } else {
                p = 150;
            }
        }
        if(move.getCode() == 117) { // present
            double rand = Math.random();
            if(rand <= 0.4) {
                p = 40;
            } else if(rand <= 0.7) {
                p = 80;
            } else if(rand <= 0.8) {
                p = 120;
            } else {
                defender.healHP(defender.getHP()/4,true,true, false, true);
                return 0;
            }
        }
        if(move.getCode() == 120) { // rollout, ice ball
            p = move.getPower()*((int)(Math.pow(2,attacker.effectMoves.get(25))));
            if(attacker.effectMoves.get(20) > 0) {
                p *= 2;
            }
        }
        if(move.getCode() == 121) { // fury cutter
            p = move.getPower()*((int)(Math.pow(2,attacker.effectMoves.get(24))));
        }
        if(move.getCode() == 122) { // magnitude
            double rand = Math.random();
            if(rand <= 0.05) {
                System.out.println("Magnitude 4!");
                p = 10;
            } else if(rand <= 0.15) {
                System.out.println("Magnitude 5!");
                p = 30;
            } else if(rand <= 0.35) {
                System.out.println("Magnitude 6!");
                p = 50;
            } else if(rand <= 0.65) {
                System.out.println("Magnitude 7!");
                p = 70;
            } else if(rand <= 0.85) {
                System.out.println("Magnitude 8!");
                p = 90;
            } else if(rand <= 0.95) {
                System.out.println("Magnitude 9!");
                p = 110;
            } else {
                System.out.println("Magnitude 10!");
                p = 150;
            }
        }
        if(move.getCode() == 123) { // gyro ball
            p = 25*(defender.getVelocity()/attacker.getVelocity());
            if(p < 1) {
                p = 1;
            } else if(p > 150) {
                p = 150;
            }
        }
        if(move.getCode() == 136 && defender.hasStatus(Status.ASLEEP)) { // wake up slap
            p = move.getPower()*2;
        }
        if(move.getCode() == 204 && defender.hasStatus(Status.PARALYZED)) { // smelling salts
            p = move.getPower()*2;
        }
        if(move.getCode() == 138) { // stored power
            p = move.getPower();
            for(int i=0;i<7;i++) {
                if(attacker.getStatChanges().get(i) > 0) p += attacker.getStatChanges().get(i)*move.getPower();
            }
        }
        if(move.getCode() == 150 && (defender.hasStatus(Status.POISONED) || defender.hasStatus(Status.BADLYPOISONED)
                || defender.hasStatus(Status.PARALYZED) || defender.hasStatus(Status.ASLEEP) || defender.hasStatus(Status.BURNED)
                || defender.hasStatus(Status.FROZEN))) { // hex
            p = move.getPower()*2;
        }
        if(move.getCode() == 160) { // punishment
            int c = 0;
            for(int i=0;i<7;i++) {
                if(defender.getStatChanges().get(i) > 0) c++;
            }
            p = 60+(c*20);
        }
        if(move.getCode() == 164 && attacker.canUseItem()) { // natural gift
            Item berry = attacker.item;
            p = 80;
            if(berry.hasName("POMEGBERRY") || berry.hasName("KELPSYBERRY") || berry.hasName("QUALOTBERRY")
                    || berry.hasName("HONDEWBERRY") || berry.hasName("GREPABERRY") || berry.hasName("TAMATOBERRY")) {
                p = 90;
            }
            if(berry.hasName("LIECHIBERRY") || berry.hasName("GANLONBERRY") || berry.hasName("SALACBERRY")
                    || berry.hasName("PETAYABERRY") || berry.hasName("APICOTBERRY") || berry.hasName("LANSATBERRY")
                    || berry.hasName("STARFBERRY") || berry.hasName("ENIGMABERRY") || berry.hasName("MICLEBERRY")
                    || berry.hasName("CUSTAPBERRY") || berry.hasName("JABOCABERRY") || berry.hasName("ROWAPBERRY")
                    || berry.hasName("KEEBERRY") || berry.hasName("MARANGABERRY")) {
                p = 100;
            }
        }
        if(move.getCode() == 182) { // low kick
            if(defender.getWeight(attacker.hasAbility("MOLDBREAKER")) < 9.9) {
                p = 20;
            } else if(defender.getWeight(attacker.hasAbility("MOLDBREAKER")) < 24.9) {
                p = 40;
            } else if(defender.getWeight(attacker.hasAbility("MOLDBREAKER")) < 49.9) {
                p = 60;
            } else if(defender.getWeight(attacker.hasAbility("MOLDBREAKER")) < 99.9) {
                p = 80;
            } else if(defender.getWeight(attacker.hasAbility("MOLDBREAKER")) < 199.9) {
                p = 100;
            } else {
                p = 120;
            }
        }
        if(move.getCode() == 186 && attacker.effectMoves.get(38) > 0) { // stomping tantrum
            p = move.getPower()*2;
        }
        if(move.getCode() == 187 && attacker.canUseItem()) { // fling
            Item it = attacker.item;
            if(it.hasName("IRONBALL")) {
                p = 130;
            } else if(it.hasName("RAREBONE") || it.hasName("HARDSTONE") || it.hasName("ROOMSERVICE") || it.getFlags().contains("f")) {
                p = 100;
            } else if(it.hasName("DEEPSEATOOTH") || it.hasName("THICKCLUB") || it.hasName("GRIPCLAW") || it.getFlags().contains("l")) {
                p = 90;
            } else if(it.hasName("HEAVYDUTYBOOTS") || it.hasName("ASSAULTVEST") || it.hasName("ELECTIRIZER") ||
                    it.hasName("SAFETYGOOGLES") || it.hasName("RAZORCLAW") || it.hasName("QUICKCLAW") ||
                    it.hasName("MAGMARIZER") || it.hasName("DAWNSTONE") || it.hasName("SHINYSTONE") ||
                    it.hasName("ODDKEYSTONE") || it.hasName("DUSKSTONE") || it.hasName("OVALSTONE") ||
                    it.hasName("PROTECTOR") || it.hasName("WEAKNESSPOLICY") || it.hasName("BLUNDERPOLICY") ||
                    it.hasName("CRACKEDPOT") || it.hasName("CHIPPEDPOT") || it.hasName("STICKYBARB") ||
                      it.getFlags().contains("j")) {
                p = 80;
            } else if(it.hasName("POWERBAND") || it.hasName("POWERBRACER") || it.hasName("POWERBELT") ||
                    it.hasName("DRAGONFANG") || it.hasName("CHILLDRIVE") || it.hasName("WHIPPEDDREAM") ||
                    it.hasName("POISONBARB") || it.hasName("POWERANKLET") || it.hasName("SHOCKDRIVE") ||
                    it.hasName("DOUSEDRIVE") || it.hasName("POWERLENS") || it.hasName("POWERWEIGHT") ||
                    it.hasName("BURNDRIVE") || it.hasName("SACHET")) {
                p = 70;
            } else if(it.hasName("MACHOBRACE") || it.hasName("ROCKYHELMET") || it.hasName("TERRAINEXTENDER") ||
                    it.hasName("ADAMANTORB") || it.hasName("GRISEOUSORB") || it.hasName("LUSTROUSORB") ||
                    it.hasName("UTILITYUMBRELLA") || it.hasName("LEEK") || it.hasName("HEATROCK") ||
                    it.hasName("DAMPROCK")) {
                p = 60;
            } else if(it.hasName("DUBIOUSDISC") || it.hasName("SHARPBEAK") || it.getFlags().contains("m")) {
                p = 50;
            } else if(it.hasName("EVIOLITE") || it.hasName("LUCKYPUNCH") || it.hasName("ICYROCK")) {
                p = 40;
            } else if(it.nameContains("FEATHER")) {
                p = 20;
            } else if(it.nameContains("INCENSE") || it.nameContains("MINT") || it.getFlags().contains("c") ||
                    it.hasName("SOFTSAND") || it.hasName("FOCUSSASH") || it.hasName("RINGTARGET") ||
                    it.hasName("SOOTHEBELL") || it.hasName("FOCUSBAND") || it.hasName("CHOICEBAND") ||
                    it.hasName("EXPERTBELT") || it.hasName("MUSCLEBAND") || it.hasName("LAGGINGTAIL") ||
                    it.hasName("SWEET") || it.hasName("CHOICESPECS") || it.hasName("WISEGLASSES") ||
                    it.hasName("AIRBALLOON") || it.hasName("WHITEHERB") || it.hasName("MENTALHERB") ||
                    it.hasName("POWERHERB") || it.hasName("DESTINYKNOT") || it.hasName("WIDELENS") ||
                    it.hasName("SHEDSHELL") || it.hasName("CHOICESPECS") || it.hasName("WISEGLASSES") ||
                    it.nameContains("NECTAR") || it.hasName("CHOICESCARF") || it.hasName("SILKSCARF") ||
                    it.hasName("BRIGHTPOWDER") || it.hasName("SILVERPOWDER") || it.hasName("METALPOWDER") ||
                    it.hasName("QUICKPOWDER") || it.hasName("BIGROOT") || it.hasName("LEFTOVERS") ||
                    it.hasName("SMOOTHROCK") || it.hasName("MISTYSEED") || it.hasName("ELECTRICSEED") ||
                    it.hasName("GRASSYSEED") || it.hasName("PSYCHICSEED") || it.hasName("REDCARD") ||
                    it.hasName("REAPERCLOTH") || it.hasName("ZOOMLENS")) {
                p = 10;
            } else {
                p = 30;
            }
        }
        if(move.getCode() == 188 && attacker.getTeam().effectTeamMoves.get(13) > 0) { // retaliate
            p = move.getPower()*2;
        }
        if(move.getCode() == 195) { // knock off
            // TODO: kyogre with blue orb, groudon with red orb, mail and mega stones
            if(!((defender.hasItem("GRISEOUSORB") && defender.specieNameIs("GIRATINA")) ||
                    (defender.item.getFlags().contains("l") && defender.hasAbility("MULTITYPE")) ||
                    ((defender.hasItem("DOUSEDRIVE") || defender.hasItem("SHOCKDRIVE") || defender.hasItem("CHILLDRIVE") ||
                            defender.hasItem("BURNDRIVE")) && defender.specieNameIs("GENESECT")) ||
                    (defender.item.getFlags().contains("m") && defender.specieNameIs("SILVALLY")))) {
                p = move.getPower()*2;
            }
        }
        if(move.getCode() == 203) { // heavy slam
            if(attacker.getWeight(attacker.hasAbility("MOLDBREAKER")) <= defender.getWeight(attacker.hasAbility("MOLDBREAKER"))*2) {
                p = 40;
            } else if(attacker.getWeight(attacker.hasAbility("MOLDBREAKER")) <= defender.getWeight(attacker.hasAbility("MOLDBREAKER"))*3) {
                p = 60;
            } else if(attacker.getWeight(attacker.hasAbility("MOLDBREAKER")) <= defender.getWeight(attacker.hasAbility("MOLDBREAKER"))*4) {
                p = 80;
            } else if(attacker.getWeight(attacker.hasAbility("MOLDBREAKER")) <= defender.getWeight(attacker.hasAbility("MOLDBREAKER"))*5) {
                p = 100;
            } else {
                p = 120;
            }
        }
        if(move.getCode() == 205) { // wring out
            p = 1 + 120*(defender.getPsActuales()/defender.getStats().get(0));
        }
        if(move.getCode() == 206 && !weather.hasWeather(Weathers.CLEARSKIES) && !weather.hasWeather(Weathers.STRONGWINDS)) { // weather ball
            p = move.getPower()*2;
        }
        if(move.getCode() == 215) { // trump card
            if(attacker.remainPPOf(move) < 0 || attacker.remainPPOf(move) >= 5) {
                p = 40;
            } else if(attacker.remainPPOf(move) == 4) {
                p = 50;
            } else if(attacker.remainPPOf(move) == 3) {
                p = 60;
            } else if(attacker.remainPPOf(move) == 2) {
                p = 80;
            } else if(attacker.remainPPOf(move) == 1) {
                p = 200;
            }
        }
        if(move.getCode() == 250 && (attacker.hasStatus(Status.POISONED) || attacker.hasStatus(Status.BADLYPOISONED) ||
                attacker.hasStatus(Status.PARALYZED) || attacker.hasStatus(Status.BURNED))) { // facade
            p = move.getPower()*2;
        }
        if(move.getCode() == 248) { // frustration
            p = (int)((255-attacker.getHappiness())/2.5);
            if(p < 1) p = 1;
            if(p > 102) p = 102;
        }
        if(move.getCode() == 257) { // return
            p = (int)(attacker.getHappiness()/2.5);
            if(p < 1) p = 1;
            if(p > 102) p = 102;
        }

        return p;
    }

    private int movePowerByExternalReasons(Pokemon attacker, Pokemon defender, Movement move, int p, boolean critical, boolean mefirst) {
        int power = p;
        // power change for move effects
        if(effectFieldMoves.get(0) > 0 && move.type.is("ELECTRIC")) { // mud sport
            power *= 0.667;
        }
        if(effectFieldMoves.get(4) > 0 && move.type.is("FIRE")) { // water sport
            power *= 0.667;
        }
        if(attacker.effectMoves.get(23) > 0 && move.type.is("ELECTRIC")) { // charge
            power *= 2;
        }
        if(effectFieldMoves.get(3) > 0 && move.getCode() == 157) { // round
            power *= 2;
        }
        if((move.hasName("BODYSLAM") || move.hasName("STOMP") || move.hasName("STEAMROLLER") || move.hasName("HEATCRASH")
                || move.hasName("DRAGONRUSH") || move.hasName("PHANTOMFORCE") || move.hasName("FLYINGPRESS")
                || move.hasName("HEAVYSLAM") || move.hasName("DOUBLEIRONBASH")) && defender.effectMoves.get(29) > 0) { // minimize double damage
            power *= 2;
        }
        if(mefirst) { // me first
            power *= 1.5;
        }
        // abilities
        if(attacker.hasAbility("OVERGROW") && attacker.getPercentHP() < 33.33 && move.type.is("GRASS")) {
            power *= 1.5;
        }
        if(attacker.hasAbility("TORRENT") && attacker.getPercentHP() < 33.33 && move.type.is("WATER")) {
            power *= 1.5;
        }
        if(attacker.hasAbility("BLAZE") && attacker.getPercentHP() < 33.33 && move.type.is("FIRE")) {
            power *= 1.5;
        }
        if(attacker.hasAbility("SWARM") && attacker.getPercentHP() < 33.33 && move.type.is("BUG")) {
            power *= 1.5;
        }
        if(attacker.hasAbility("IRONFIST") && move.getFlags().contains("k")) {
            power *= 1.2;
        }
        if(attacker.hasAbility("RECKLESS") && (move.getCode() == 5 || move.getCode() == 8 || move.getCode() == 134)) {
            power *= 1.2;
        }
        if(attacker.hasAbility("SANDFORCE") && weather.hasWeather(Weathers.SANDSTORM) && (move.type.is("GROUND") ||
                move.type.is("ROCK") || move.type.is("STEEL"))) {
            power *= 1.3;
        }
        if(attacker.hasAbility("WATERBUBBLE") && move.type.is("WATER")) {
            power *= 2.0;
        }
        if(attacker.hasAbility("RIVALRY")) {
            if((defender.getGender() == attacker.getGender()) && attacker.getGender() != 2) {
                power *= 1.25;
            } else if((defender.getGender() != attacker.getGender()) && (attacker.getGender() != 2 && defender.getGender() != 2)) {
                power *= 0.75;
            }
        }
        if(defender.hasAbility("DRYSKIN") && move.type.is("FIRE")) {
            power *= 1.25;
        }
        if(attacker.hasAbility("TECHNICIAN") && power <= 60) {
            power *= 1.5;
        }
        if(attacker.hasAbility("ANALYTIC") && defender.lastMoveUsedInTurn != null) {
            power *= 1.3;
        }
        if(attacker.effectMoves.get(28) > 0 && move.type.is("FIRE")) { // flash fire powers fire moves
            power *= 1.5;
        }
        if(attacker.hasAbility("NORMALIZE") && move.type.is("NORMAL") && move.typeIsChanged()) {
            power *= 1.2;
        }
        // items
        if(attacker.canUseItem()) {
            if(attacker.hasItem("LIFEORB")) power *= 1.3;
            if(attacker.hasItem("METRONOME")) power *= 1.0+(attacker.effectMoves.get(27)*0.2);

            if((attacker.hasItem("METALCOAT") || attacker.hasItem("IRONPLATE")) && move.type.is("STEEL")) power *= 1.2;
            if((attacker.hasItem("MYSTICWATER") || attacker.hasItem("WAVEINCENSE") || attacker.hasItem("SEAINCENSE") || attacker.hasItem("SPLASHPLATE")) && move.type.is("WATER")) power *= 1.2;
            if((attacker.hasItem("SILVERPOWDER") || attacker.hasItem("INSECTPLATE")) && move.type.is("BUG")) power *= 1.2;
            if((attacker.hasItem("DRAGONFANG") || attacker.hasItem("DRACOPLATE")) && move.type.is("DRAGON")) power *= 1.2;
            if((attacker.hasItem("MAGNET") || attacker.hasItem("ZAPPLATE")) && move.type.is("ELECTRIC")) power *= 1.2;
            if((attacker.hasItem("SPELLTAG") || attacker.hasItem("SPOOKYPLATE")) && move.type.is("GHOST")) power *= 1.2;
            if((attacker.hasItem("CHARCOAL") || attacker.hasItem("FLAMEPLATE")) && move.type.is("FIRE")) power *= 1.2;
            if(attacker.hasItem("PIXIEPLATE") && move.type.is("FAIRY")) power *= 1.2;
            if((attacker.hasItem("NEVERMELTICE") || attacker.hasItem("ICICLEPLATE")) && move.type.is("ICE")) power *= 1.2;
            if((attacker.hasItem("BLACKBELT") || attacker.hasItem("FISTPLATE")) && move.type.is("FIGHTING")) power *= 1.2;
            if((attacker.hasItem("SILKSCARF") || attacker.hasItem("BLANKPLATE")) && move.type.is("NORMAL")) power *= 1.2;
            if((attacker.hasItem("MIRACLESEED") || attacker.hasItem("ROSEINCENSE") || attacker.hasItem("MEADOWPLATE")) && move.type.is("GRASS")) power *= 1.2;
            if((attacker.hasItem("TWISTEDSPOON") || attacker.hasItem("ODDINCENSE") || attacker.hasItem("MINDPLATE")) && move.type.is("PSYCHIC")) power *= 1.2;
            if((attacker.hasItem("HARDSTONE") || attacker.hasItem("ROCKINCENSE") || attacker.hasItem("STONEPLATE")) && move.type.is("ROCK")) power *= 1.2;
            if((attacker.hasItem("BLACKGLASSES") || attacker.hasItem("DREADPLATE")) && move.type.is("DARK")) power *= 1.2;
            if((attacker.hasItem("SOFTSAND") || attacker.hasItem("EARTHPLATE")) && move.type.is("GROUND")) power *= 1.2;
            if((attacker.hasItem("POISONBARB") || attacker.hasItem("TOXICPLATE")) && move.type.is("POISON")) power *= 1.2;
            if((attacker.hasItem("SHARPBEAK") || attacker.hasItem("SKYPLATE")) && move.type.is("FLYING")) power *= 1.2;
            // gems
            if(attacker.hasItem("STEELGEM") && move.type.is("STEEL") || (attacker.hasItem("WATERGEM") && move.type.is("WATER")) ||
                    (attacker.hasItem("DRAGONGEM") && move.type.is("DRAGON")) || (attacker.hasItem("BUGGEM") && move.type.is("BUG")) ||
                    (attacker.hasItem("ELECTRICGEM") && move.type.is("ELECTRIC")) || (attacker.hasItem("FAIRYGEM") && move.type.is("FAIRY")) ||
                    (attacker.hasItem("GHOSTGEM") && move.type.is("GHOST")) || (attacker.hasItem("ICEGEM") && move.type.is("ICE")) ||
                    (attacker.hasItem("FIREGEM") && move.type.is("FIRE")) || (attacker.hasItem("ROCKGEM") && move.type.is("ROCK")) ||
                    (attacker.hasItem("FIGHTINGGEM") && move.type.is("FIGHTING")) || (attacker.hasItem("DARKGEM") && move.type.is("DARK")) ||
                    (attacker.hasItem("NORMALGEM") && move.type.is("NORMAL")) || (attacker.hasItem("GROUNDGEM") && move.type.is("GROUND")) ||
                    (attacker.hasItem("GRASSGEM") && move.type.is("GRASS")) || (attacker.hasItem("POISONGEM") && move.type.is("POISON")) ||
                    (attacker.hasItem("PSYCHICGEM") && move.type.is("PSYCHIC")) || (attacker.hasItem("FLYINGGEM") && move.type.is("FLYING"))) {
                power *= 1.3;
                attacker.loseItem(true, true);
            }
            // reductor berries
            if(getEffectiveness(attacker,defender,move,false) >= 2.0) {
                if(defender.hasItem("BABIRIBERRY") && move.type.is("STEEL") || (defender.hasItem("PASSHOBERRY") && move.type.is("WATER")) ||
                        (defender.hasItem("HABANBERRY") && move.type.is("DRAGON")) || (defender.hasItem("TANGABERRY") && move.type.is("BUG")) ||
                        (defender.hasItem("WACANBERRY") && move.type.is("ELECTRIC")) || (defender.hasItem("ROSELIBERRY") && move.type.is("FAIRY")) ||
                        (defender.hasItem("KASIBBERRY") && move.type.is("GHOST")) || (defender.hasItem("YACHEBERRY") && move.type.is("ICE")) ||
                        (defender.hasItem("OCCABERRY") && move.type.is("FIRE")) || (defender.hasItem("CHARTIBERRY") && move.type.is("ROCK")) ||
                        (defender.hasItem("CHOPLEBERRY") && move.type.is("FIGHTING")) || (defender.hasItem("COLBURBERRY") && move.type.is("DARK")) ||
                        (defender.hasItem("SHUCABERRY") && move.type.is("GROUND")) || (defender.hasItem("RINDOBERRY") && move.type.is("GRASS")) ||
                        (defender.hasItem("KEBIABERRY") && move.type.is("POISON")) || (defender.hasItem("PAYAPABERRY") && move.type.is("PSYCHIC")) ||
                        (defender.hasItem("COBABERRY") && move.type.is("FLYING"))) {
                    power *= 0.5;
                    attacker.loseItem(true, true);
                }
            }
            if(defender.hasItem("CHILANBERRY") && move.type.is("NORMAL")) {
                power *= 0.5;
                attacker.loseItem(true, true);
            }

            if(attacker.hasItem("SOULDEW") && (move.type.is("DRAGON") || move.type.is("PSYCHIC")) && (attacker.specieNameIs("LATIAS") || attacker.specieNameIs("LATIOS"))) power *= 1.2;
            if(attacker.hasItem("ADAMANTORB") && (move.type.is("DRAGON") || move.type.is("STEEL")) && attacker.specieNameIs("DIALGA")) power *= 1.2;
            if(attacker.hasItem("LUSTROUSORB") && (move.type.is("DRAGON") || move.type.is("WATER")) && attacker.specieNameIs("PALKIA")) power *= 1.2;
            if(attacker.hasItem("GRISEOUSORB") && (move.type.is("DRAGON") || move.type.is("GHOST")) && attacker.specieNameIs("GIRATINA")) power *= 1.2;
        }

        if(move.getCode() == 213 && attacker.item == null) { // acrobatics
            power *= 2;
        }
        // weather
        if(weather.hasWeather(Weathers.SUNLIGHT) || weather.hasWeather(Weathers.HEAVYSUNLIGHT)) {
            if(move.type.is("FIRE")) {
                power *= 1.5;
            } else if(move.type.is("WATER")) {
                power *= 0.5;
            }
        }
        if(weather.hasWeather(Weathers.RAIN) || weather.hasWeather(Weathers.HEAVYRAIN)) {
            if(move.type.is("FIRE")) {
                power *= 0.5;
            } else if(move.type.is("WATER")) {
                power *= 1.5;
            }
            if(move.hasName("SOLARBEAM") || move.hasName("SOLARBLADE")) {
                power *= 0.5;
            }
        }
        if(weather.hasWeather(Weathers.HAIL)) {
            if(move.hasName("SOLARBEAM") || move.hasName("SOLARBLADE")) {
                power *= 0.5;
            }
        }
        if(weather.hasWeather(Weathers.SANDSTORM)) {
            if(move.hasName("SOLARBEAM") || move.hasName("SOLARBLADE")) {
                power *= 0.5;
            }
        }
        if(weather.hasWeather(Weathers.FOG)) {
            if(move.hasName("SOLARBEAM") || move.hasName("SOLARBLADE")) {
                power *= 0.5;
            }
        }
        if(move.getCode() == 229) { // brick break breaks barriers
            defender.getTeam().effectTeamMoves.set(4, 0);
            defender.getTeam().effectTeamMoves.set(5, 0);
            defender.getTeam().effectTeamMoves.set(18, 0);
        }
        // screens
        if(defender.getTeam().effectTeamMoves.get(4) > 0 && !critical && move.getCategory().equals(Category.SPECIAL) && !attacker.hasAbility("INFILTRATOR")) { // light screen
            power /= 2.0;
        }
        if(defender.getTeam().effectTeamMoves.get(5) > 0 && !critical && move.getCategory().equals(Category.PHYSICAL) && !attacker.hasAbility("INFILTRATOR")) { // reflect
            power /= 2.0;
        }
        // terrains
        if(terrain.hasTerrain(TerrainTypes.GRASSY)) {
            if(!attacker.isLevitating() && move.type.is("GRASS")) {
                power *= 1.3;
            }
            if(move.hasName("EARTHQUAKE") && move.hasName("MAGNITUDE") && move.hasName("BULLDOZE")) {
                power *= 0.5;
            }
        }
        if(terrain.hasTerrain(TerrainTypes.ELECTRIC) && !attacker.isLevitating() && move.type.is("ELECTRIC")) {
            power *= 1.3;
        }
        if(terrain.hasTerrain(TerrainTypes.MISTY) && !defender.isLevitating() && move.type.is("DRAGON")) {
            power *= 0.5;
        }
        if(terrain.hasTerrain(TerrainTypes.PSYCHIC) && !attacker.isLevitating() && move.type.is("PSYCHIC")) {
            power *= 1.3;
        }
        return power;
    }

    private int CalcDamageConfuse(Pokemon attacker) {
        int damage = 0;
        int attack = attacker.getAttack(false, false, attacker.utils.getMove("STRUGGLE"));
        int defense = attacker.getDefense(false, false, false, false);
        int variation = attacker.utils.getRandomNumberBetween(85,101);

        // calculate damage
        double dmg = (0.01*variation*(((attack*40*(0.2*attacker.getLevel()+1))/(25*defense))+2));

        // the minimum damage is always 1
        damage = (int) dmg;
        if(dmg < 1.0 && dmg > 0.0) {
            damage = 1;
        }
        return damage;
    }

    private boolean isCriticalHit(Pokemon attacker, Pokemon defender, Movement move) {
        if(defender.getTeam().effectTeamMoves.get(6) > 0) { // lucky chant
            return false;
        }
        if((defender.hasAbility("SHELLARMOR") || defender.hasAbility("BATTLEARMOR")) && !attacker.hasAbility("MOLDBREAKER")) { // shell armor, battle armor
            return false;
        }
        if(move.getCode() == 239 || attacker.effectMoves.get(60) > 0) return true;
        int rand = attacker.utils.getRandomNumberBetween(0,101);
        return (attacker.getCriticalIndex() == 0 && rand <= 4.167) || (attacker.getCriticalIndex() == 1 && rand <= 12.5) ||
                (attacker.getCriticalIndex() == 2 && rand <= 50) || attacker.getCriticalIndex() >= 3;
    }

    private double changesInInmunities(Pokemon attacker, Pokemon defender, Movement move) {
        if((defender.effectMoves.get(5) > 0 || attacker.hasAbility("SCRAPPY")) && defender.hasType("GHOST") && // foresight, odor sleuth, scrappy
                (move.type.is("NORMAL") || move.type.is("FIGHTING"))) {
            return 1.0;
        }
        if(defender.effectMoves.get(41) > 0 && defender.hasType("DARK") && // miracle eye
                move.type.is("PSYCHIC")) {
            return 1.0;
        }
        if(effectFieldMoves.get(2) > 0 && defender.hasType("FLYING") && move.type.is("GROUND")) { // gravity
            return 1.0;
        }
        if(defender.hasItem("RINGTARGET") && defender.canUseItem()) { // ring target
            return 1.0;
        }
        return 0.0;
    }

    private double changesInWeaknesses(Pokemon defender) {
        // strong winds delete flying type weaknesses
        if(defender.hasType("FLYING") && weather.hasWeather(Weathers.STRONGWINDS)) {
            return 1.0;
        }
        return 2.0;
    }

    private double changesInResistances(Pokemon defender, Movement move) {
        // freeze dry
        if(defender.hasType("WATER") && move.hasName("FREEZEDRY")) {
            return 2.0;
        }
        return 0.5;
    }

    public double getEffectiveness(Pokemon attacker, Pokemon defender, Movement move, boolean message) {
        double effectiveness = 1.0;
        if(defender.battleType1 != null) {
            if(defender.battleType1.weaknesses.contains(move.type.getInternalName())) {
                effectiveness *= changesInWeaknesses(defender);
            } else if(defender.battleType1.resistances.contains(move.type.getInternalName())) {
                effectiveness *= changesInResistances(defender, move);
            } else if(defender.battleType1.immunities.contains(move.type.getInternalName())) {
                effectiveness *= changesInInmunities(attacker,defender,move);
            }
        }

        if(defender.battleType2 != null) {
            if(defender.battleType2.weaknesses.contains(move.type.getInternalName())) {
                effectiveness *= changesInWeaknesses(defender);
            } else if(defender.battleType2.resistances.contains(move.type.getInternalName())) {
                effectiveness *= changesInResistances(defender, move);
            } else if(defender.battleType2.immunities.contains(move.type.getInternalName())) {
                effectiveness *= changesInInmunities(attacker,defender,move);
            }
        }

        // IRON BALL
        if(defender.hasItem("IRONBALL") && defender.canUseItem() && defender.hasType("FLYING") && move.type.is("GROUND")) {
            effectiveness = 1.0;
        }

        if(effectiveness >= 2.0) {
            if(message) System.out.println("It's very effective!");
            if((defender.hasAbility("FILTER") || defender.hasAbility("SOLIDROCK")) && !attacker.hasAbility("MOLDBREAKER")) { // filter/solid rock
                effectiveness *= 0.75;
            }
        } else if(effectiveness <= 0.5 && effectiveness > 0.0) {
            if(message) System.out.println("It's not very effective...");
            if(attacker.hasAbility("TINTEDLENS")) { // tinted lens
                effectiveness *= 2.0;
            }
        } else if(effectiveness <= 0.0) {
            if(message) System.out.println("It doesn't affect " + defender.nickname + "...");
        }

        return effectiveness;
    }

    private void endTurn(Pokemon target, Pokemon other) {

        target.lastMoveInThisTurn = null;
        target.lastMoveUsedInTurn = null;
        target.previousDamage = 0;
        target.effectMoves.set(9, 0);

        // end round effect
        effectFieldMoves.set(3, 0);

        int bindDamage = 8;
        if(other.hasItem("BINDINGBAND") && other.canUseItem()) {
            bindDamage = 6;
        }
        int screenDuration = 6;
        if(target.hasItem("LIGHTCLAY") && target.canUseItem()) {
            screenDuration = 9;
        }

        // remove flinch
        target.healTempStatus(TemporalStatus.FLINCHED,false);
        // weather
        if(weather.hasWeather(Weathers.HAIL) && target.affectHail()) {
            System.out.println(target.nickname + " is buffeted by the hail!");
            target.reduceHP(target.getHP()/16);
        }
        if(weather.hasWeather(Weathers.SANDSTORM) && target.affectSandstorm()) {
            System.out.println(target.nickname + " is buffeted by the sandstorm!!");
            target.reduceHP(target.getHP()/16);
        }
        // grassy terrain
        if(terrain.hasTerrain(TerrainTypes.GRASSY) && !target.isLevitating()) {
            target.healHP(target.getHP()/16, true, false, false, true);
        }
        // shed skin
        if(target.hasAbility("SHEDSKIN") && (target.hasStatus(Status.ASLEEP) || target.hasStatus(Status.PARALYZED)
            || target.hasStatus(Status.BURNED) || target.hasStatus(Status.POISONED) || target.hasStatus(Status.BADLYPOISONED)) && 0.3 >= Math.random()) {
            target.healPermanentStatus();
        }

        // poison
        if(target.hasStatus(Status.POISONED) && !target.isFainted() && !target.hasAbility("MAGICGUARD")) {
            System.out.println(target.nickname + " is affected by the poison!");
            if(target.hasAbility("POISONHEAL")) {
                target.healHP(target.getHP()/8,true,false,false,true);
            } else {
                target.reduceHP(target.getHP()/8);
            }
        }
        // badly poison
        if(target.hasStatus(Status.BADLYPOISONED) && !target.isFainted() && !target.hasAbility("MAGICGUARD")) {
            System.out.println(target.nickname + " is affected by the poison!");
            if(target.hasAbility("POISONHEAL")) {
                target.healHP(target.getHP()/8,true,false,false,true);
            } else {
                target.reduceHP((int) (target.getHP()*(target.badPoisonTurns/16.0)));
            }
        }
        // burn
        if(target.hasStatus(Status.BURNED) && !target.isFainted() && !target.hasAbility("MAGICGUARD")) {
            int ps = 16;
            System.out.println(target.nickname + " is affected by the burn!");
            if(target.hasAbility("HEATPROOF")) ps = 32;
            target.reduceHP(target.getHP()/ps);
        }
        // cursed
        if(target.hasTemporalStatus(TemporalStatus.CURSED) && !target.isFainted() && !target.hasAbility("MAGICGUARD")) {
            System.out.println(target.nickname + " is affected by Curse!");
            target.reduceHP(target.getHP()/4);
        }
        // leech seed
        if(target.hasTemporalStatus(TemporalStatus.SEEDED) && !target.isFainted() && !target.hasAbility("MAGICGUARD")) {
            System.out.println(target.nickname + " is seeded by Leech Seed!");
            target.reduceHP(target.getHP()/8);
            if(!target.hasAbility("LIQUIDOOZE")) {
                other.healHP(target.getHP()/8, true, false, true, true);
            } else {
                other.reduceHP(target.getHP()/8);
            }
        }
        // partially trapped moves
        if(target.hasTemporalStatus(TemporalStatus.PARTIALLYTRAPPED) && target.effectMoves.get(4) >= 1 && !target.hasAbility("MAGICGUARD")) {
            System.out.println(target.nickname + " is hurt by Fire Spin!"); // fire spin
            target.reduceHP(target.getHP()/bindDamage);
            target.effectMoves.set(4, target.effectMoves.get(4)+1);
        }
        if(target.hasTemporalStatus(TemporalStatus.PARTIALLYTRAPPED) && target.effectMoves.get(16) >= 1 && !target.hasAbility("MAGICGUARD")) {
            System.out.println(target.nickname + " is hurt by Wrap!"); // wrap
            target.reduceHP(target.getHP()/bindDamage);
            target.effectMoves.set(16, target.effectMoves.get(16)+1);
        }
        if(target.hasTemporalStatus(TemporalStatus.PARTIALLYTRAPPED) && target.effectMoves.get(21) >= 1 && !target.hasAbility("MAGICGUARD")) {
            System.out.println(target.nickname + " is hurt by Sand Tomb!"); // sand tomb
            target.reduceHP(target.getHP()/bindDamage);
            target.effectMoves.set(21, target.effectMoves.get(21)+1);
        }
        if(target.hasTemporalStatus(TemporalStatus.PARTIALLYTRAPPED) && target.effectMoves.get(49) >= 1 && !target.hasAbility("MAGICGUARD")) {
            System.out.println(target.nickname + " is hurt by Clamp!"); // clamp
            target.reduceHP(target.getHP()/bindDamage);
            target.effectMoves.set(49, target.effectMoves.get(49)+1);
        }
        if(target.hasTemporalStatus(TemporalStatus.PARTIALLYTRAPPED) && target.effectMoves.get(50) >= 1 && !target.hasAbility("MAGICGUARD")) {
            System.out.println(target.nickname + " is hurt by Whirlpool"); // whirlpool
            target.reduceHP(target.getHP()/bindDamage);
            target.effectMoves.set(50, target.effectMoves.get(50)+1);
        }
        if(target.hasTemporalStatus(TemporalStatus.PARTIALLYTRAPPED) && target.effectMoves.get(51) >= 1 && !target.hasAbility("MAGICGUARD")) {
            System.out.println(target.nickname + " is hurt by Bind!"); // bind
            target.reduceHP(target.getHP()/bindDamage);
            target.effectMoves.set(51, target.effectMoves.get(51)+1);
        }
        if(target.hasTemporalStatus(TemporalStatus.PARTIALLYTRAPPED) && target.effectMoves.get(61) >= 1 && !target.hasAbility("MAGICGUARD")) {
            System.out.println(target.nickname + " is hurt by Infestation!"); // infestation
            target.reduceHP(target.getHP()/bindDamage);
            target.effectMoves.set(61, target.effectMoves.get(61)+1);
        }
        // nightmare
        if(target.effectMoves.get(54) > 0 && !target.isFainted() && target.hasStatus(Status.ASLEEP) && !target.hasAbility("MAGICGUARD")) {
            System.out.println(target.nickname + " is affected by Nightmare!");
            target.reduceHP(target.getHP()/4);
        }
        // solar power and dry skin
        if((target.hasAbility("SOLARPOWER") || target.hasAbility("DRYSKIN")) && target.effectMoves.get(36) == 0
                && target.effectMoves.get(46) == 0 && (weather.hasWeather(Weathers.SUNLIGHT) || weather.hasWeather(Weathers.HEAVYSUNLIGHT))) {
            System.out.println(target.nickname + " is affected by " + target.getAbility().name + "!");
            target.reduceHP(target.getHP()/8);
        }

        // ingrain
        if(!target.hasAllHP() && target.effectMoves.get(0) == 1 && !target.isFainted()) {
            System.out.println(target.nickname + " obtains energy from roots!");
            target.healHP(target.getHP()/16, true, true, true, true);
        }
        // aqua ring
        if(!target.hasAllHP() && target.effectMoves.get(7) == 1 && !target.isFainted()) {
            System.out.println(target.nickname + " recover HP by Aqua Ring!");
            target.healHP(target.getHP()/16, true, true, true, true);
        }
        // rain dish and dry skin
        if((target.hasAbility("RAINDISH") || target.hasAbility("DRYSKIN")) && target.effectMoves.get(36) == 0
                && target.effectMoves.get(46) == 0 && (weather.hasWeather(Weathers.RAIN) || weather.hasWeather(Weathers.HEAVYRAIN))) {
            System.out.println(target.nickname + " recover HP by " + target.getAbility().name + "!");
            int ps = 16;
            if(target.hasAbility("DRYSKIN")) {
                ps = 8;
            }
            target.healHP(target.getHP()/ps, true, true, false, true);
        }
        // ice body
        if(target.hasAbility("ICEBODY") && target.effectMoves.get(36) == 0 && target.effectMoves.get(46) == 0
                && weather.hasWeather(Weathers.HAIL)) {
            System.out.println(target.nickname + " recover HP by " + target.getAbility().name + "!");
            target.healHP(target.getHP()/16, true, true, false, true);
        }
        // hydration
        if(target.hasAbility("HYDRATION") && !target.hasStatus(Status.FINE) && !target.hasStatus(Status.FAINTED)) {
            target.healPermanentStatus();
        }
        // moody
        if(target.hasAbility("MOODY")) {
            if(!target.statsAreMaximum()) {
                int randomStat = -1;
                do {
                    randomStat = random.nextInt(target.getStatChanges().size());
                } while(target.getStatChanges().get(randomStat) == 6);
                target.changeStat(randomStat,2,true,true,other);
            }
            target.changeStat(4,1,true,true,other);
            if(!target.statsAreMinimum()) {
                int randomStat = -1;
                do {
                    randomStat = random.nextInt(target.getStatChanges().size());
                } while(target.getStatChanges().get(randomStat) == -6);
                target.changeStat(randomStat,-1,true,true,other);
            }
        }
        // speed boost
        if(target.hasAbility("SPEEDBOOST")) {
            target.changeStat(4,1,true,true,other);
        }

        // remove endure, protect, detect, snatch...
        target.effectMoves.set(1, 0); // endure
        target.effectMoves.set(2, 0); // protect
        target.effectMoves.set(18, 0); // snatch
        target.effectMoves.set(58, 0); // magic coat

        // count and remove individual effects
        if(target.effectMoves.get(6) > 0) { // yawn
            target.increaseEffectMove(6); // increase turn
            if(target.effectMoves.get(6) > 2) {
                if(target.canSleep(true, other)) {
                    target.causeStatus(Status.ASLEEP, other, false);
                }
                target.effectMoves.set(6, 0);
            }
        }
        if(target.effectMoves.get(17) > 0) { // disable
            target.increaseEffectMove(17); // increase turn
            if(target.effectMoves.get(17) > 4) {
                System.out.println("Disable of " + target.nickname + " finished!");
                target.effectMoves.set(17, 0);
                target.disabledMove = null;
            }
        }
        if(target.effectMoves.get(52) > 0) { // cursed body
            target.increaseEffectMove(52); // increase turn
            if(target.effectMoves.get(52) > 4) {
                System.out.println("Disable of " + target.nickname + " finished!");
                target.effectMoves.set(52, 0);
                target.cursedBodyMove = null;
            }
        }
        if(target.effectMoves.get(37) > 0) { // taunt
            target.increaseEffectMove(37); // increase turn
            if(target.effectMoves.get(37) > 3) {
                System.out.println(target.nickname + " is not taunted!");
                target.effectMoves.set(37, 0);
            }
        }
        if(target.effectMoves.get(26) > 0) { // encore
            target.increaseEffectMove(26); // increase turn
            if(target.effectMoves.get(26) > 3) {
                System.out.println("Encore of " + target.nickname + " finished!");
                target.effectMoves.set(26, 0);
                target.encoreMove = null;
            }
        }
        if(target.effectMoves.get(42) > 0) { // telekinesis
            target.increaseEffectMove(42); // increase turn
            if(target.effectMoves.get(42) > 3) {
                System.out.println("Telekinesis of " + target.nickname + " finished!");
                target.effectMoves.set(42, 0);
            }
        }
        if(target.effectMoves.get(45) > 0) { // magnet rise
            target.increaseEffectMove(45); // increase turn
            if(target.effectMoves.get(45) > 5) {
                System.out.println("Magnet rise of " + target.nickname + " finished!");
                target.effectMoves.set(45, 0);
            }
        }
        if(target.effectMoves.get(23) > 0) { // charge
            target.increaseEffectMove(23); // increase turn
            if(target.effectMoves.get(23) > 2) {
                target.effectMoves.set(23, 0);
            }
        }
        if(target.effectMoves.get(40) > 0) { // mind reader, lock on
            target.increaseEffectMove(40); // increase turn
            if(target.effectMoves.get(40) > 2) {
                target.effectMoves.set(40, 0);
            }
        }
        if(target.effectMoves.get(57) > 0) { // repose turn
            target.increaseEffectMove(57); // increase turn
            if(target.effectMoves.get(57) > 2) {
                target.effectMoves.set(57, 0);
            }
        }
        if(target.effectMoves.get(60) > 0) { // laser focus
            target.increaseEffectMove(60); // increase turn
            if(target.effectMoves.get(60) > 2) {
                target.effectMoves.set(60, 0);
            }
        }
        if(target.effectMoves.get(62) > 0) { // embargo
            target.increaseEffectMove(62); // increase turn
            if(target.effectMoves.get(62) > 5) {
                System.out.println("Embargo of " + target.nickname + " finished!");
                target.effectMoves.set(62, 0);
            }
        }
        if(target.effectMoves.get(63) > 0) { // throat chop
            target.increaseEffectMove(63); // increase turn
            if(target.effectMoves.get(63) > 3) {
                target.effectMoves.set(63, 0);
            }
        }
        if(target.effectMoves.get(64) > 0) { // heal block
            target.increaseEffectMove(64); // increase turn
            if(target.effectMoves.get(64) > 5) {
                System.out.println("Heal block of " + target.nickname + " finished!");
                target.effectMoves.set(64, 0);
            }
        }

        // count and remove team effects
        if(target.getTeam().effectTeamMoves.get(0) > 0) { // mist
            target.getTeam().increaseEffectMove(0); // increase turn
            if(target.getTeam().effectTeamMoves.get(0) > 5) {
                target.getTeam().removeTeamEffects(target, 0);
            }
        }
        if(target.getTeam().effectTeamMoves.get(1) > 0) { // safeguard
            target.getTeam().increaseEffectMove(1); // increase turn
            if(target.getTeam().effectTeamMoves.get(1) > 5) {
                target.getTeam().removeTeamEffects(target, 1);
            }
        }
        if(target.getTeam().effectTeamMoves.get(2) > 0) { // tailwind
            target.getTeam().increaseEffectMove(2); // increase turn
            if(target.getTeam().effectTeamMoves.get(2) > 4) {
                target.getTeam().removeTeamEffects(target, 2);
            }
        }
        if(target.getTeam().effectTeamMoves.get(4) > 0) { // light screen
            target.getTeam().increaseEffectMove(4); // increase turn
            if(target.getTeam().effectTeamMoves.get(4) > screenDuration) {
                target.getTeam().removeTeamEffects(target, 4);
            }
        }
        if(target.getTeam().effectTeamMoves.get(5) > 0) { // reflect
            target.getTeam().increaseEffectMove(5); // increase turn
            if(target.getTeam().effectTeamMoves.get(5) > screenDuration) {
                target.getTeam().removeTeamEffects(target, 5);
            }
        }
        if(target.getTeam().effectTeamMoves.get(18) > 0) { // aurora veil
            target.getTeam().increaseEffectMove(18); // increase turn
            if(target.getTeam().effectTeamMoves.get(18) > screenDuration) {
                target.getTeam().removeTeamEffects(target, 18);
            }
        }
        if(target.getTeam().effectTeamMoves.get(6) > 0) { // lucky chant
            target.getTeam().increaseEffectMove(6); // increase turn
            if(target.getTeam().effectTeamMoves.get(6) > 6) {
                target.getTeam().removeTeamEffects(target, 6);
            }
        }
        if(target.getTeam().effectTeamMoves.get(7) > 0) { // wish
            target.getTeam().increaseEffectMove(7); // increase turn
            if(target.getTeam().effectTeamMoves.get(7) > 2) {
                System.out.println(target.nickname + " received the Wish!");
                target.healHP(target.getTeam().wishRecover,true,true, false, true);
                target.getTeam().removeTeamEffects(target, 7);
            }
        }
        if(target.getTeam().effectTeamMoves.get(12) > 0 && target.getTeam().futureAttackerPoke != null) { // future sight, doom desire
            target.getTeam().increaseEffectMove(12); // increase turn
            if(target.getTeam().effectTeamMoves.get(12) > 2) {
                System.out.println(target.nickname + " received the attack!");
                Movement mv = target.utils.getMove("FUTURESIGHT");
                if(target.getTeam().futureAttackId == 2) {
                    mv = target.utils.getMove("DOOMDESIRE");
                }
                useMove(target.getTeam().futureAttackerPoke, target, mv, target.utils.getMove("STRUGGLE"), false, false, true);
                target.getTeam().futureAttackerPoke = null;
            }
        }
        if(target.getTeam().effectTeamMoves.get(13) > 0) { // retaliate
            target.getTeam().increaseEffectMove(13); // increase turn
            if(target.getTeam().effectTeamMoves.get(13) > 2) {
                target.getTeam().effectTeamMoves.set(13, 0);
            }
        }
        if(target.getTeam().effectTeamMoves.get(9) > 0) { // quick guard
            target.getTeam().effectTeamMoves.set(9, 0);
        }
        if(target.getTeam().effectTeamMoves.get(10) > 0) { // wide guard
            target.getTeam().effectTeamMoves.set(10, 0);
        }
        if(target.getTeam().effectTeamMoves.get(17) > 0) { // guard spec
            target.getTeam().increaseEffectMove(17); // increase turn
            if(target.getTeam().effectTeamMoves.get(17) > 5) {
                target.getTeam().removeTeamEffects(target, 17);
            }
        }

        // turn counter
        if(target.hasStatus(Status.BADLYPOISONED)) {
            target.badPoisonTurns++;
        }
        if(target.hasStatus(Status.ASLEEP)) {
            target.sleepTurns++;
        }

        // items
        if(target.canUseItem()) {
            if(target.hasItem("TOXICORB") && target.canPoison(true, other)) {
                System.out.println(target.item.name + " of " + target.nickname + " is activated!");
                target.causeStatus(Status.BADLYPOISONED, other, true);
            }
            if(target.hasItem("FLAMEORB") && target.canBurn(true, other)) {
                System.out.println(target.item.name + " of " + target.nickname + " is activated!");
                target.causeStatus(Status.BURNED, other, true);
            }
            if(!target.hasAllHP() && target.hasItem("LEFTOVERS") && !target.isFainted()) {
                System.out.println(target.nickname + " recover HP from " + target.item.name + "!");
                target.healHP(target.getHP()/16, true, true, false, true);
            }
            if(!target.hasAllHP() && target.hasItem("BLACKSLUDGE") && !target.isFainted()) {
                if(target.hasType("POISON")) {
                    System.out.println(target.nickname + " recover HP from " + target.item.name + "!");
                    target.healHP(target.getHP()/16, true, true, false, true);
                } else {
                    System.out.println(target.nickname + " is hurted by " + target.item.name + "!");
                    target.reduceHP(target.getHP()/8);
                }
            }
            if(target.hasItem("STICKYBARB") && !target.isFainted()) {
                System.out.println(target.nickname + " is hurted by " + target.item.name + "!");
                target.reduceHP(target.getHP()/8);
            }
        }

        // end roost
        if(target.effectMoves.get(10) > 0) {
            target.effectMoves.set(10, 0);
            target.battleType1 = target.getSpecie().type1;
            target.battleType2 = target.getSpecie().type2;
        }

        // end stomping tantrum
        if(target.effectMoves.get(38) > 0) {
            target.increaseEffectMove(38); // increase turn
            if(target.effectMoves.get(38) > 2) {
                target.effectMoves.set(38, 0);
            }
        }
        // end micle berry
        if(target.effectMoves.get(47) > 0) {
            target.increaseEffectMove(47); // increase turn
            if(target.effectMoves.get(47) > 2) {
                target.effectMoves.set(47, 0);
            }
        }
        // end custap berry
        if(target.effectMoves.get(48) > 0) {
            target.increaseEffectMove(48); // increase turn
            if(target.effectMoves.get(48) > 2) {
                target.effectMoves.set(48, 0);
            }
        }

        // perish song
        if(target.effectMoves.get(33) > 0) { // perish song
            target.increaseEffectMove(33); // increase turn
            System.out.println("Perish song of " + target.nickname + " count to " + (5-target.effectMoves.get(33)) + "!");
            if(target.effectMoves.get(33) > 4) {
                target.reduceHP(-1);
            }
        }
        effectFieldMoves.set(8,0);
        // truant ability
        target.changeTruant();

        target.pokeTurn++;
        System.out.println("Turn: " + turn);
    }

    private void increaseEffectMove(int index) {
        effectFieldMoves.set(index,effectFieldMoves.get(index)+1);
    }

    public boolean hasCloudNine() {
        return user.hasAbility("CLOUDNINE") || rival.hasAbility("CLOUDNINE");
    }
    public boolean hasDamp() {
        return (user.hasAbility("DAMP") && !rival.hasAbility("MOLDBREAKER")) || (rival.hasAbility("DAMP") && !user.hasAbility("MOLDBREAKER"));
    }

    private void fieldEffects() {
        // count and remove battle effects
        if(effectFieldMoves.get(0) > 0) { // mud sport
            increaseEffectMove(0); // increase turn
            if(effectFieldMoves.get(0) > 5) {
                effectFieldMoves.set(0, 0);
            }
        }
        if(effectFieldMoves.get(2) > 0) { // gravity
            increaseEffectMove(2); // increase turn
            if(effectFieldMoves.get(2) > 5) {
                effectFieldMoves.set(2, 0);
            }
        }
        if(effectFieldMoves.get(4) > 0) { // water sport
            increaseEffectMove(4); // increase turn
            if(effectFieldMoves.get(4) > 5) {
                effectFieldMoves.set(4, 0);
            }
        }
        if(effectFieldMoves.get(5) > 0) { // wonder room
            increaseEffectMove(5); // increase turn
            if(effectFieldMoves.get(5) > 5) {
                System.out.println("Wonder Room ended!");
                effectFieldMoves.set(5, 0);
            }
        }
        if(effectFieldMoves.get(6) > 0) { // magic room
            increaseEffectMove(6); // increase turn
            if(effectFieldMoves.get(6) > 6) {
                System.out.println("Magic Room ended!");
                effectFieldMoves.set(6, 0);
            }
        }
        if(effectFieldMoves.get(7) > 0) { // trick room
            increaseEffectMove(7); // increase turn
            if(effectFieldMoves.get(7) > 6) {
                System.out.println("Trick Room ended!");
                effectFieldMoves.set(7, 0);
            }
        }

        // count and end weather
        if(weather.hasWeather(Weathers.RAIN) || weather.hasWeather(Weathers.SUNLIGHT) || weather.hasWeather(Weathers.SANDSTORM) || weather.hasWeather(Weathers.HAIL)) {
            weather.increaseTurn();
        }

        // count and end terrains
        if(!terrain.hasTerrain(TerrainTypes.NONE)) {
            terrain.increaseTurn();
        }
    }

    private void endBattle(boolean trainer) {
        c = 0;
        // remove field effects
        for(int i=0;i<effectFieldMoves.size();i++) {
            effectFieldMoves.set(i,0);
        }

        if(battleResult == 1) {
            // get experience
            userTeam.gainTeamExperience(rival,trainer);
            userTeam.gainBattleMoney(trainer);
        } else if(battleResult == 2) {
            System.out.println("You lose!");
            // heal team
            userTeam.healTeam();
        }
        lastMoveUsed = null;
        userTeam.battleEnded();
    }
}
