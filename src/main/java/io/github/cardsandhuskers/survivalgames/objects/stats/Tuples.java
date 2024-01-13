package io.github.cardsandhuskers.survivalgames.objects.stats;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;

import java.util.Comparator;

/**
 * @author cardsandhuskers
 * @version 12-Jan-2024
 * Contains various small tuples that hold specific data
 */
public class Tuples {
    public static Tuples tuples = new Tuples();

    /**
     * Holds kills for one player in one specific event
     */
    public class EventKillsHolder {
        private String name;
        private int event, kills;

        public EventKillsHolder(String name, int event, int kills) {
            this.name = name;
            this.event = event;
            this.kills = kills;
        }

        public String getName() {
            return name;
        }

        public int getEvent() {
            return event;
        }

        public int getKills() {
            return kills;
        }
    }

    /**
     * comparator for the event kills
     * compares by kill count then name
     */
    public static class EventKillsComparator implements Comparator<EventKillsHolder>{
        @Override
        public int compare(EventKillsHolder h1, EventKillsHolder h2) {
            int compare = Integer.compare(h1.kills, h2.kills);
            if(compare == 0) compare = h1.name.compareTo(h2.name);

            return compare;
        }
    }

    /**
     * Holds a single player's KD
     */
    public class KDTuple {
        String player;
        int kills;
        int deaths;
        public KDTuple(String player) {
            this.player = player;
            kills = 0;
            deaths = 0;
        }

        /**
         * Get KD Ratio against the player
         * @return - ratio
         */
        public double getRatio() {
            if(deaths > 0) return (double) kills / (double) deaths;
            else return kills;
        }
        @Override
        public String toString() {
            return player + ": " + kills + "K / " + deaths + "D (" + getRatio() + ")";
        }
    }

    /**
     * comparator for kd, compares by k/d, then kills, then deaths, then name
     */
    public static class KDTupleComparator implements Comparator<KDTuple> {
        @Override
        public int compare(KDTuple o1, KDTuple o2) {
            int compare = Double.compare(o1.getRatio(), o2.getRatio());
            if(compare == 0) compare = Integer.compare(o1.kills, o2.kills);
            if(compare == 0) compare = Integer.compare(o2.deaths, o1.deaths); //deaths is a o2 to o1 to invert death in ties, so that 0 deaths comes before 1
            if(compare == 0) compare = o1.player.compareTo(o2.player);

            return compare;
        }
    }

    /**
     * comparator for the total kills holders for the players
     */
    public static class TotalKillsComparator implements Comparator<PlayerStatsHolder> {
        private SurvivalGames.GameType gameType;
        public TotalKillsComparator(SurvivalGames.GameType gameType) {
            this.gameType = gameType;
        }

        public int compare(PlayerStatsHolder h1, PlayerStatsHolder h2) {
            int compare = Integer.compare(h1.getKills(gameType), h2.getKills(gameType));
            if (compare == 0) compare = h1.getName().compareTo(h2.getName());
            return compare;
        }
    }

    private KDTuple addKDTuple(String player) {
        return new KDTuple(player);
    }

    /**
     * getter to create a KD Tuple
     * @param player - player name
     * @return
     */
    public static KDTuple createKDTuple(String player) {
        return tuples.addKDTuple(player);
    }

    private EventKillsHolder addEventKillsHolder(String name, int event, int kills) {
        return new EventKillsHolder(name, event, kills);
    }

    /**
     * Getter to create a new EventKillsHolder
     * @param name - player name
     * @param event - event num
     * @param kills - number of kills player earned
     * @return kills holder
     */
    public static EventKillsHolder createEventKillsHolder(String name, int event, int kills) {
        return tuples.addEventKillsHolder(name, event, kills);
    }
}