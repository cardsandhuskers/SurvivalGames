package io.github.cardsandhuskers.survivalgames.objects.stats;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import io.github.cardsandhuskers.survivalgames.objects.stats.PlayerStatsHolder;
import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * @author cardsandhuskers
 * @version 12-Jan-2024
 *
 * Handles the calculation of all the stats for survival games and skywars. Contains accessor methods to retrieve
 * the data, mostly used by the Placeholder class
 */
public class StatCalculator {
    private SurvivalGames plugin;
    private ArrayList<PlayerStatsHolder> playerStatsHolders;
    private ArrayList<Tuples.EventKillsHolder> sgKillsHolders, skywarsKillsHolders;
    private int currentEvent;

    public StatCalculator(SurvivalGames plugin) {
        this.plugin = plugin;
        try {currentEvent = Bukkit.getPluginManager().getPlugin("LobbyPlugin").getConfig().getInt("eventNum");}
        catch (Exception e) {currentEvent = 1;}
    }

    /**
     * Pulls the data from the csv files and builds the stats holders
     * @throws IOException
     */
    public void calculateStats() throws IOException {

        HashMap<String, PlayerStatsHolder> playerStatsMap = new HashMap<>();
        FileReader reader;

        for(int i = 1; i <= currentEvent; i++) {
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
                            PlayerStatsHolder statsHolder = new PlayerStatsHolder(killer, currentEvent);
                            statsHolder.addKill(i, died, type);
                            playerStatsMap.put(killer, statsHolder);
                        }
                    }
                    if (playerStatsMap.containsKey(died)) {
                        playerStatsMap.get(died).addDeath(i, killer, type);
                    } else {
                        PlayerStatsHolder statsHolder = new PlayerStatsHolder(died, currentEvent);
                        statsHolder.addDeath(i, killer, type);
                        playerStatsMap.put(died, statsHolder);
                    }
                }
            }

        }
        playerStatsHolders = new ArrayList<>(playerStatsMap.values());
        for(PlayerStatsHolder holder: playerStatsHolders) {
            holder.calculateKD();
        }

        sgKillsHolders = new ArrayList<>();
        skywarsKillsHolders = new ArrayList<>();
        for(PlayerStatsHolder psh: playerStatsHolders) sgKillsHolders.addAll(psh.getEventKills(SurvivalGames.GameType.SURVIVAL_GAMES));
        for(PlayerStatsHolder psh: playerStatsHolders) skywarsKillsHolders.addAll(psh.getEventKills(SurvivalGames.GameType.SKYWARS));

    }

    /**
     * Gets the event kills holder for the respective place
     * used in printing top 10
     * The place is ordered from most to fewest kills
     * @param place - 1st place, 2nd place, etc.
     * @param gameType - sg or skywars
     * @return - EventKillsHolder in that place
     */
    public Tuples.EventKillsHolder getEventKills(int place, SurvivalGames.GameType gameType) {
        ArrayList<Tuples.EventKillsHolder> returnableList;
        if(gameType == SurvivalGames.GameType.SKYWARS) returnableList = new ArrayList<>(skywarsKillsHolders);
        else returnableList = new ArrayList<>(sgKillsHolders);

        Collections.sort(returnableList, new Tuples.EventKillsComparator());
        Collections.reverse(returnableList);

        if(place > returnableList.size()) return null;
        else return returnableList.get(place-1);

    }

    /**
     * Gets the total kills holder for the respective place
     * Used in printing the top 10
     * The place is ordered from most to fewest kills
     * @param place - 1st place, 2nd place, etc.
     * @param gameType - sg or skywars
     * @return - PlayerStatsHolder in that place
     */
    public PlayerStatsHolder getTotalKills(int place, SurvivalGames.GameType gameType) {
        ArrayList<PlayerStatsHolder> returnableList = new ArrayList<>(playerStatsHolders);
        Collections.sort(returnableList, new Tuples.TotalKillsComparator(gameType));
        Collections.reverse(returnableList);

        if(place > returnableList.size()) return null;
        else return returnableList.get(place-1);

    }

    /**
     * Gets the position of the passed player on the kills leaderboard and returns that place
     * @param p
     * @param gameType
     * @return
     */
    public String getPlayerKillsPosition(OfflinePlayer p, SurvivalGames.GameType gameType) {
        String name = p.getName();

        ArrayList<PlayerStatsHolder> returnableList = new ArrayList<>(playerStatsHolders);
        Collections.sort(returnableList, new Tuples.TotalKillsComparator(gameType));
        Collections.reverse(returnableList);

        int i = 1;
        PlayerStatsHolder playerHolder = null;
        for(PlayerStatsHolder holder: returnableList) {
            if(holder.getName().equals(name)) {
                playerHolder = holder;
                break;
            }
            i++;
        }
        if(playerHolder == null || i <= 10) return "";

        Team team = TeamHandler.getInstance().getPlayerTeam(p.getPlayer());
        String color = "";
        if(team != null) color = team.getColor();

        return i + ". " + color + "You" + ChatColor.RESET + ": " + playerHolder.getKills(gameType);

    }

    /**
     * Gets the specified line number in a player's kd tuples
     * @param p - player
     * @param place - line number
     * @return tuple data
     */
    public String getKDLoc(OfflinePlayer p, int place) {
        String name = p.getName();
        PlayerStatsHolder playerHolder = null;
        for(PlayerStatsHolder holder: playerStatsHolders) {
            if(holder.getName().equals(name)) {
                playerHolder = holder;
                break;
            }
        }
        if(playerHolder == null) return "";
        Tuples.KDTuple tuple = null;

        if(place > playerHolder.getKdList().size()) return "";
        tuple = playerHolder.getKdList().get(place - 1);
        if(tuple == null) return "";
        return tuple.toString();

    }

    /**
     * Returns the list of KD tuples for a player's KD
     * @param p - player
     * @return tuples
     */
    public ArrayList<Tuples.KDTuple> getAllKDs(OfflinePlayer p) {
        String name = p.getName();
        PlayerStatsHolder playerHolder = null;
        for(PlayerStatsHolder holder: playerStatsHolders) {
            if(holder.getName().equals(name)) {
                playerHolder = holder;
                break;
            }
        }
        if(playerHolder == null) return null;
       return playerHolder.getKdList();

    }



}


