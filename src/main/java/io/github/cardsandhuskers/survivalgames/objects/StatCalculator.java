package io.github.cardsandhuskers.survivalgames.objects;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.bukkit.Bukkit;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.gameType;

public class StatCalculator {
    private SurvivalGames plugin;
    private ArrayList<PlayerStatsHolder> playerStatsHolders;
    private ArrayList<EventKillsHolder> sgKillsHolders, skywarsKillsHolders;
    private int currentEvent;

    public StatCalculator(SurvivalGames plugin) {
        this.plugin = plugin;
        try {currentEvent = Bukkit.getPluginManager().getPlugin("LobbyPlugin").getConfig().getInt("eventNum");}
        catch (Exception e) {currentEvent = 1;}
    }

    public void calculateStats() throws IOException {
        int initialEvent = 1;


        HashMap<String, PlayerStatsHolder> playerStatsMap = new HashMap<>();
        FileReader reader;

        for(int i = initialEvent; i <= currentEvent; i++) {
            for(SurvivalGames.GameType type : SurvivalGames.GameType.values()) {

                try {
                    reader = new FileReader(plugin.getDataFolder() + "/" + type + "Stats" + i + ".csv");
                } catch (IOException e) {
                    plugin.getLogger().warning("Stats file not found!");
                    continue;
                }
                String[] headers = {"round", "deadTeam", "deadName", "killerTeam", "killerName"};

                CSVFormat.Builder builder = CSVFormat.Builder.create();
                builder.setHeader(headers);
                CSVFormat format = builder.build();

                CSVParser parser;
                parser = new CSVParser(reader, format);

                List<CSVRecord> recordList = parser.getRecords();
                reader.close();


                for(CSVRecord r:recordList) {
                    if (r.getRecordNumber() == 1) continue;

                    String killer = r.get(4);
                    String died = r.get(2);

                    //win condition, this isn't a death but means the player won
                    if(killer.equals("Winner-")) {
                        continue;
                    }

                    if(!killer.equals("Environment-")) {
                        if (playerStatsMap.containsKey(killer)) {
                            playerStatsMap.get(killer).addKill(i, died, type);

                        } else {
                            PlayerStatsHolder statsHolder = new PlayerStatsHolder(killer);
                            statsHolder.addKill(i, died, type);
                            playerStatsMap.put(killer, statsHolder);
                        }
                    }
                        if (playerStatsMap.containsKey(died)) {
                            playerStatsMap.get(died).addDeath(i, killer, type);
                        } else {
                            PlayerStatsHolder statsHolder = new PlayerStatsHolder(died);
                            statsHolder.addDeath(i, killer, type);
                            playerStatsMap.put(died, statsHolder);
                        }
                }
            }

        }
        playerStatsHolders = new ArrayList<>(playerStatsMap.values());
        System.out.println(playerStatsHolders);

        sgKillsHolders = new ArrayList<>();
        skywarsKillsHolders = new ArrayList<>();

        for(PlayerStatsHolder psh: playerStatsHolders) sgKillsHolders.addAll(psh.getEventKills(SurvivalGames.GameType.SURVIVAL_GAMES));
        for(PlayerStatsHolder psh: playerStatsHolders) skywarsKillsHolders.addAll(psh.getEventKills(SurvivalGames.GameType.SKYWARS));

        System.out.println(sgKillsHolders);
        System.out.println(skywarsKillsHolders);

    }

    public EventKillsHolder getEventKills(int place, SurvivalGames.GameType gameType) {
        ArrayList<EventKillsHolder> returnableList;
        if(gameType == SurvivalGames.GameType.SKYWARS) returnableList = new ArrayList<>(skywarsKillsHolders);
        else returnableList = new ArrayList<>(sgKillsHolders);

        Collections.sort(returnableList, new EventKillsComparator(gameType));
        Collections.reverse(returnableList);

        if(place > returnableList.size()) return null;
        else return returnableList.get(place-1);

    }

    public PlayerStatsHolder getTotalKills(int place, SurvivalGames.GameType gameType) {
        ArrayList<PlayerStatsHolder> returnableList = new ArrayList<>(playerStatsHolders);
        Collections.sort(returnableList, new TotalKillsComparator(gameType));
        Collections.reverse(returnableList);

        if(place > returnableList.size()) return null;
        else return returnableList.get(place-1);

    }

    public class PlayerStatsHolder {
        String name;

        //integer is eventNum, arraylist is list of players they killed in that event
        private HashMap<Integer, ArrayList<String>> sgKills, sgDeaths;
        private HashMap<Integer, ArrayList<String>> skywarsKills, skywarsDeaths;

        public PlayerStatsHolder(String name) {
            this.name = name;
            sgKills = new HashMap<>();
            sgDeaths = new HashMap<>();

            skywarsKills = new HashMap<>();
            skywarsDeaths = new HashMap<>();
        }

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
        /*public int getEventKills(int eventNum, SurvivalGames.GameType gameType) {
            if(gameType == SurvivalGames.GameType.SKYWARS) {
                if(skywarsKills.containsKey(eventNum)) return skywarsKills.get(eventNum).size();
                else return 0;
            }
            if(gameType == SurvivalGames.GameType.SURVIVAL_GAMES) {
                if(sgKills.containsKey(eventNum)) return sgKills.get(eventNum).size();
                else return 0;
            }
            return 0;
        }*/

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

        public ArrayList<EventKillsHolder> getEventKills(SurvivalGames.GameType gameType) {
            ArrayList<EventKillsHolder> eventKills = new ArrayList<>();

            if(gameType == SurvivalGames.GameType.SKYWARS) {
                for (int i = 1; i <= currentEvent; i++) {
                    if (skywarsKills.containsKey(i)) {
                        eventKills.add(new EventKillsHolder(name, i, skywarsKills.get(i).size()));
                    }
                }
            }
            if(gameType == SurvivalGames.GameType.SURVIVAL_GAMES) {
                for (int i = 1; i <= currentEvent; i++) {
                    if (sgKills.containsKey(i)) {
                        eventKills.add(new EventKillsHolder(name, i, sgKills.get(i).size()));
                    }
                }
            }
            return eventKills;
        }
    }

    public class TotalKillsComparator implements Comparator<PlayerStatsHolder> {
        private SurvivalGames.GameType gameType;
        public TotalKillsComparator(SurvivalGames.GameType gameType) {
            this.gameType = gameType;
        }

        public int compare(PlayerStatsHolder h1, PlayerStatsHolder h2) {
            int compare = Integer.compare(h1.getKills(gameType), h2.getKills(gameType));
            if (compare == 0) compare = h1.name.compareTo(h2.name);
            return compare;
        }
    }

    public class EventKillsHolder {
        String name;
        int event, kills;
        public EventKillsHolder(String name, int event, int kills) {
            this.name = name;
            this.event = event;
            this.kills = kills;
        }
    }

    public class EventKillsComparator implements Comparator<EventKillsHolder> {
        private SurvivalGames.GameType gameType;
        public EventKillsComparator(SurvivalGames.GameType gameType) {
            this.gameType = gameType;
        }

        public int compare(EventKillsHolder h1, EventKillsHolder h2) {
            int compare = Integer.compare(h1.kills, h2.kills);
            if(compare == 0) compare = h1.name.compareTo(h2.name);
            return compare;
        }
    }
}
