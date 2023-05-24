package io.github.cardsandhuskers.survivalgames.objects;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class StatCalculator {
    SurvivalGames plugin;
    ArrayList<PlayerKillsHolder> playerKillsHolders;
    ArrayList<TeamKillsHolder> teamKillsHolders;
    ArrayList<TotalKillsHolder> totalKillsHolders;

    public StatCalculator(SurvivalGames plugin) {
        this.plugin = plugin;
    }

    public void calculateStats() throws Exception {
        playerKillsHolders = new ArrayList<>();
        teamKillsHolders = new ArrayList<>();

        FileReader reader = null;
        try {
            reader = new FileReader(plugin.getDataFolder() + "/stats.csv");
        } catch (IOException e) {
            plugin.getLogger().warning("Stats file not found!");
            return;
        }

        String[] headers = {"Event", "Type", "Team", "Name", "Kills"};
        CSVFormat.Builder builder = CSVFormat.Builder.create();
        builder.setHeader(headers);
        CSVFormat format = builder.build();

        CSVParser parser;
        try {
            parser = new CSVParser(reader, format);
        } catch (IOException e) {
            throw new Exception(e);
        }
        List<CSVRecord> recordList = parser.getRecords();

        try {
            reader.close();
        } catch (IOException e) {
            throw new Exception(e);
        }

        //maps records to each event number
        HashMap<Integer, ArrayList<CSVRecord>> recordsMap = new HashMap<>();
        int totalEvents = 0;
        for (CSVRecord r : recordList) {
            //skip header
            if (r.getRecordNumber() == 1) continue;

            //have totalEvents be value of "last event"
            totalEvents = Math.max(totalEvents, Integer.parseInt(r.get(0)));

            //if event number isn't in map, put new arraylist in spot at map
            if(!recordsMap.containsKey(Integer.valueOf(r.get(0)))) recordsMap.put(Integer.valueOf(r.get(0)), new ArrayList<>());
            //append to arraylist
            recordsMap.get(Integer.valueOf(r.get(0))).add(r);
        }


        ArrayList<CSVRecord> winningRecords = new ArrayList<>();

        for(int i = 1; i <= totalEvents; i++) {
            if(!recordsMap.containsKey(i)) continue;
            for(CSVRecord r:recordsMap.get(i)) {
                SurvivalGames.GameType type = SurvivalGames.GameType.valueOf(r.get(1));
                String team = r.get(2);
                String player = r.get(3);

                if(r.get(4).equals("WINNER-")) {
                    winningRecords.add(r);
                    continue;
                }
                int kills = Integer.parseInt(r.get(4));


                boolean exists = false;
                for(PlayerKillsHolder h:playerKillsHolders) {
                    if(h.name.equalsIgnoreCase(player) && h.eventNum == i) {
                        if(type == SurvivalGames.GameType.SKYWARS) h.skywarsKills += kills;
                        if(type == SurvivalGames.GameType.SURVIVAL_GAMES) h.sgKills += kills;

                        exists = true;
                        break;
                    }
                }
                if(!exists) {
                    PlayerKillsHolder holder = new PlayerKillsHolder();
                    holder.eventNum = i;
                    holder.name = player;
                    holder.team = team;
                    if(type == SurvivalGames.GameType.SKYWARS) holder.skywarsKills = kills;
                    if(type == SurvivalGames.GameType.SURVIVAL_GAMES) holder.sgKills = kills;

                    playerKillsHolders.add(holder);
                }

                exists = false;
                for(TeamKillsHolder h:teamKillsHolders) {
                    if(h.team.equalsIgnoreCase(team) && h.eventNum == i) {
                        if(type == SurvivalGames.GameType.SKYWARS) h.skywarsKills += kills;
                        if(type == SurvivalGames.GameType.SURVIVAL_GAMES) h.sgKills += kills;

                        exists = true;
                        break;
                    }
                }
                if(!exists) {
                    TeamKillsHolder holder = new TeamKillsHolder();
                    holder.eventNum = i;
                    holder.team = team;
                    if(type == SurvivalGames.GameType.SKYWARS) holder.skywarsKills = kills;
                    if(type == SurvivalGames.GameType.SURVIVAL_GAMES) holder.sgKills = kills;

                    teamKillsHolders.add(holder);
                }

            }

        }

        HashMap<String, TotalKillsHolder> killsHolderHashMap = new HashMap<>();
        for(PlayerKillsHolder h:playerKillsHolders) {
            //use this to sum total kills for each player
            if(killsHolderHashMap.containsKey(h.name)) {
                TotalKillsHolder holder = killsHolderHashMap.get(h.name);
                holder.sgKills += h.sgKills;
                holder.skywarsKills += h.skywarsKills;
            } else {
                TotalKillsHolder holder = new TotalKillsHolder();
                holder.name = h.name;
                holder.sgKills = h.sgKills;
                holder.skywarsKills = h.skywarsKills;

                killsHolderHashMap.put(h.name, holder);
            }

        }
        for(CSVRecord r: winningRecords) {
            String name = r.get(3);
            SurvivalGames.GameType type = SurvivalGames.GameType.valueOf(r.get(1));
            if(killsHolderHashMap.containsKey(name)) {
                if(type == SurvivalGames.GameType.SKYWARS) killsHolderHashMap.get(name).skywarsWins++;
                else killsHolderHashMap.get(name).sgWins++;
            }
        }

        totalKillsHolders = new ArrayList<>(killsHolderHashMap.values());

    }

    public ArrayList<PlayerKillsHolder> getPlayerKillsHolders(SurvivalGames.GameType type) {
        ArrayList<PlayerKillsHolder> pkh = new ArrayList<>(playerKillsHolders);

        Comparator playerKillsCompare = new PlayerKillsComparator(type);
        pkh.sort(playerKillsCompare);
        Collections.reverse(pkh);
        return pkh;

    }

    public ArrayList<TotalKillsHolder> getTotalKillsHolders(SurvivalGames.GameType game, TotalKillsComparator.SortType type) {
        ArrayList<TotalKillsHolder> tkh = new ArrayList<>(totalKillsHolders);
        
        Comparator totalKillsCompare = new TotalKillsComparator(game, type);
        tkh.sort(totalKillsCompare);
        Collections.reverse(tkh);
        return tkh;
    }


    class PlayerKillsHolder {
        public int eventNum;
        public int skywarsKills;
        public int sgKills;
        public String name;
        public String team;

        @Override
        public String toString() {
            return name + "  Team: " + team + ": \n" + "Event: " + eventNum + " SG: "  +sgKills + " Skywars: " + skywarsKills;
        }
    }
    class PlayerKillsComparator implements Comparator<PlayerKillsHolder> {
        public SurvivalGames.GameType game;
        public PlayerKillsComparator(SurvivalGames.GameType game) {
            this.game = game;
        }


        public int compare(PlayerKillsHolder h1, PlayerKillsHolder h2) {
            int compare;
            if (game == SurvivalGames.GameType.SKYWARS) compare = Integer.compare(h1.skywarsKills, h2.skywarsKills);
            else compare = Integer.compare(h1.sgKills, h2.sgKills);
            if (compare == 0) compare = h1.name.compareTo(h2.name);
            if (compare == 0) compare = Integer.compare(h1.eventNum, h2.eventNum);
            return compare;
        }
    }


    class TeamKillsHolder {
        public int eventNum;
        public int skywarsKills;
        public int sgKills;
        public String team;

    }

    class TotalKillsHolder {
        public int sgKills;
        public int skywarsKills;
        public String name;
        public int skywarsWins = 0;
        public int sgWins = 0;

        @Override
        public String toString() {
            return name + ": \n" + "SGK: "  +sgKills + " SkywarsK: " + skywarsKills + "SGW: "  +sgWins + " SkywarsW: " + skywarsWins;
        }
    }
    class TotalKillsComparator implements Comparator<TotalKillsHolder> {
        public SurvivalGames.GameType game;
        public SortType type;
        enum SortType {
            KILLS,
            WINS
        }
        public TotalKillsComparator(SurvivalGames.GameType game, SortType type) {
            this.game = game;
            this.type = type;
        }


        public int compare(TotalKillsHolder h1, TotalKillsHolder h2) {
            int compare;
            if(type == SortType.KILLS) {
                if (game == SurvivalGames.GameType.SKYWARS) compare = Integer.compare(h1.skywarsKills, h2.skywarsKills);
                else compare = Integer.compare(h1.sgKills, h2.sgKills);
            }
            else {
                if (game == SurvivalGames.GameType.SKYWARS) compare = Integer.compare(h1.skywarsWins, h2.skywarsWins);
                else compare = Integer.compare(h1.sgWins, h2.sgWins);
            }
            if (compare == 0) compare = h1.name.compareTo(h2.name);
            return compare;
        }

    }



}
