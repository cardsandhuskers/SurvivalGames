package io.github.cardsandhuskers.survivalgames.objects.stats;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * @author cardsandhuskers
 * @version 12-Jan-2024
 * Main stat holder class, holds the kills list and calculates out the other holders
 *
 */
public class PlayerStatsHolder {
    private String name;
    int currentEvent;

    //integer is eventNum, arraylist is list of players they killed in that event
    private HashMap<Integer, ArrayList<String>> sgKills, sgDeaths;
    private HashMap<Integer, ArrayList<String>> skywarsKills, skywarsDeaths;
    private ArrayList<Tuples.KDTuple> kdList;

    public PlayerStatsHolder(String name, int currentEvent) {
        this.name = name;
        sgKills = new HashMap<>();
        sgDeaths = new HashMap<>();

        skywarsKills = new HashMap<>();
        skywarsDeaths = new HashMap<>();
        this.currentEvent = currentEvent;
    }

    /**
     * Adds a death to the stats holder
     * @param event - event number the kill happened in
     * @param killer - player they were killed by
     * @param gameType - skywars or sg
     */
    public void addDeath(int event, String killer, SurvivalGames.GameType gameType) {
        if(gameType == SurvivalGames.GameType.SKYWARS) {
            if(skywarsDeaths.containsKey(event)) {
                skywarsDeaths.get(event).add(killer);
            } else {
                ArrayList<String> players = new ArrayList<>();
                players.add(killer);
                skywarsDeaths.put(event, players);
            }

        } else {
            if(sgDeaths.containsKey(event)) {
                sgDeaths.get(event).add(killer);
            } else {
                ArrayList<String> players = new ArrayList<>();
                players.add(killer);
                sgDeaths.put(event, players);
            }
        }
    }

    /**
     * Adds a kill to the stats holder
     * @param event - event number the kill happened in
     * @param killed - player they killed
     * @param gameType - skywars or sg
     */
    public void addKill(int event, String killed, SurvivalGames.GameType gameType) {
        if(gameType == SurvivalGames.GameType.SKYWARS) {
            if(skywarsKills.containsKey(event)) {
                skywarsKills.get(event).add(killed);
            } else {
                ArrayList<String> players = new ArrayList<>();
                players.add(killed);
                skywarsKills.put(event, players);
            }

        } else {
            if(sgKills.containsKey(event)) {
                sgKills.get(event).add(killed);
            } else {
                ArrayList<String> players = new ArrayList<>();
                players.add(killed);
                sgKills.put(event, players);
            }
        }
    }

    /**
     * Calculates player KD ratios against every other player
     * Stores the values in KD tuples, so you can see any player's KD against any other specific player
     */
    public void calculateKD() {
        HashMap<String, Tuples.KDTuple> kdMap = new HashMap<>();

        for(ArrayList<String> kills: sgKills.values()) {
            for(String kill: kills) {
                if(kdMap.containsKey(kill)) {
                    kdMap.get(kill).kills++;
                } else {
                    Tuples.KDTuple kd = Tuples.createKDTuple(kill);
                    kd.kills++;
                    kdMap.put(kill, kd);
                }
            }
        }
        for(ArrayList<String> deaths: sgDeaths.values()) {
            for(String death: deaths) {
                if(kdMap.containsKey(death)) {
                    kdMap.get(death).deaths++;
                } else {
                    Tuples.KDTuple kd = Tuples.createKDTuple(death);
                    kd.deaths++;
                    kdMap.put(death, kd);
                }
            }
        }

        for(ArrayList<String> kills: skywarsKills.values()) {
            for(String kill: kills) {
                if(kdMap.containsKey(kill)) {
                    kdMap.get(kill).kills++;
                } else {
                    Tuples.KDTuple kd = Tuples.createKDTuple(kill);;
                    kd.kills++;
                    kdMap.put(kill, kd);
                }
            }
        }
        for(ArrayList<String> deaths: skywarsDeaths.values()) {
            for(String death: deaths) {
                if(kdMap.containsKey(death)) {
                    kdMap.get(death).deaths++;
                } else {
                    Tuples.KDTuple kd = Tuples.createKDTuple(death);
                    kd.deaths++;
                    kdMap.put(death, kd);
                }
            }
        }

        kdList = new ArrayList<>(kdMap.values());
        Collections.sort(kdList, new Tuples.KDTupleComparator());
        Collections.reverse(kdList);
    }

    /**
     * Gets the number of kills the player has in the specified gametype
     * @param gameType - either sg or skywars
     * @return integer, number of kills
     */
    public int getKills(SurvivalGames.GameType gameType) {
        int kills = 0;
        if(gameType == SurvivalGames.GameType.SKYWARS) {
            for(int i = 1; i <= currentEvent; i++) {
                if(skywarsKills.containsKey(i)) {
                    kills += skywarsKills.get(i).size();
                }
            }
        }
        if(gameType == SurvivalGames.GameType.SURVIVAL_GAMES) {
            for(int i = 1; i <= currentEvent; i++) {
                if(sgKills.containsKey(i)) {
                    kills += sgKills.get(i).size();
                }
            }
        }
        return kills;
    }

    /**
     * Gets the number of kills the player has in each event in the specified game
     * The EventKillsHolders are small tuples that contain an integer representing the number of kills
     * @param gameType - sg or skywars
     * @return ArrayList of EventKillsHolders
     */
    public ArrayList<Tuples.EventKillsHolder> getEventKills(SurvivalGames.GameType gameType) {
        ArrayList<Tuples.EventKillsHolder> eventKills = new ArrayList<>();

        if(gameType == SurvivalGames.GameType.SKYWARS) {
            for (int i = 1; i <= currentEvent; i++) {
                if (skywarsKills.containsKey(i)) {
                    eventKills.add(Tuples.createEventKillsHolder(name, i, skywarsKills.get(i).size()));
                }
            }
        }
        if(gameType == SurvivalGames.GameType.SURVIVAL_GAMES) {
            for (int i = 1; i <= currentEvent; i++) {
                if (sgKills.containsKey(i)) {
                    eventKills.add(Tuples.createEventKillsHolder(name, i, sgKills.get(i).size()));
                }
            }
        }
        return eventKills;
    }

    public ArrayList<Tuples.KDTuple> getKdList() {
        return new ArrayList<>(kdList);
    }

    public String getName() {
        return name;
    }


}
