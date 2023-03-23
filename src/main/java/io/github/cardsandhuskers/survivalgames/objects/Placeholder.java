package io.github.cardsandhuskers.survivalgames.objects;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.*;
import static io.github.cardsandhuskers.survivalgames.handlers.PlayerDeathHandler.numPlayers;
import static io.github.cardsandhuskers.survivalgames.handlers.PlayerDeathHandler.numTeams;
import static io.github.cardsandhuskers.survivalgames.objects.Border.borderSize;

public class Placeholder extends PlaceholderExpansion {
    private final SurvivalGames plugin;

    public Placeholder(SurvivalGames plugin) {
        this.plugin = plugin;
    }


    @Override
    public String getIdentifier() {
        return "Survivalgames";
    }
    @Override
    public String getAuthor() {
        return "cardsandhuskers";
    }
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    @Override
    public boolean persist() {
        return true;
    }


    @Override
    public String onRequest(OfflinePlayer p, String s) {
        if(gameType == GameType.SURVIVAL_GAMES) {
            if (s.equalsIgnoreCase("timer")) {
                int time;
                if ((gameState == State.GAME_IN_PROGRESS && timeVar == 0) || gameState == State.DEATHMATCH && timeVar == 0) {
                    time = altTimeVar;
                } else {
                    time = timeVar;
                }
                int mins = time / 60;
                String seconds = String.format("%02d", time - (mins * 60));
                return mins + ":" + seconds;
            }

            if (s.equalsIgnoreCase("timerstage")) {
                switch (gameState) {
                    case GAME_STARTING:
                        return "Game Starting";
                    case GRACE_PERIOD:
                        return "PVP enabled";
                    case GAME_IN_PROGRESS:
                        if (timeVar > 0) {
                            return "Chests refill";
                        } else {
                            return "Deathmatch";
                        }
                    case DEATHMATCH:
                        if (altTimeVar > 0) {
                            return "Deathmatch";
                        } else {
                            return "Game end";
                        }
                    case GAME_OVER:
                        return "Return to Lobby";
                    default:
                        return "Game";
                }
            }
        }


        if(gameType == GameType.SKYWARS) {
            if (s.equalsIgnoreCase("timer")) {
                int time;
                if ((gameState == State.GAME_IN_PROGRESS && timeVar == 0) || gameState == State.DEATHMATCH && timeVar == 0) {
                    time = altTimeVar;
                } else {
                    time = timeVar;
                }
                int mins = time / 60;
                String seconds = String.format("%02d", time - (mins * 60));
                return mins + ":" + seconds;
            }

            if (s.equalsIgnoreCase("timerstage")) {
                switch (gameState) {
                    case GAME_STARTING:
                        return "Game Starting";
                    case GRACE_PERIOD:
                        //return "PVP enabled";
                    case GAME_IN_PROGRESS:
                        if (timeVar > 0) {
                            return "Chests refill";
                        } else {
                            return "Game Over";
                        }
                    case DEATHMATCH:
                        if (altTimeVar > 0) {
                            return "Deathmatch";
                        } else {
                            return "Game end";
                        }
                    case GAME_OVER:
                        if(gameNumber == 1) {
                            return "Round Over";
                        }
                        return "Game Over";
                    default:
                        return "Game";
                }
            }
        }



        if(s.equalsIgnoreCase("teamsLeft")) {
            return numTeams + "/" + handler.getNumTeams();
        }
        if(s.equalsIgnoreCase("playersLeft")) {
            return numPlayers + "/" + totalPlayers;
        }
        if(s.equalsIgnoreCase("border")) {
            return " +- " + borderSize;
        }
        if(s.equalsIgnoreCase("playerkills")) {
            if(playerKills.get(p) != null) {
                return playerKills.get(p) + "";
            } else {
                playerKills.put((Player) p, 0);
            }
        }
        if(s.equalsIgnoreCase("round")) {
            return gameNumber + "";
        }

        String[] values = s.split("_");
        //playerKills, teamKills, playerSumKills
        //sg / skywars
        // num
        //playerKills_sg_1
        try {
            if(values[0].equalsIgnoreCase("playerKills")) {
                int num = Integer.parseInt(values[2])-1;
                if(values[1].equalsIgnoreCase("skywars")) {
                    ArrayList<StatCalculator.PlayerKillsHolder> killsHolders = plugin.statCalculator.getPlayerKillsHolders(GameType.SKYWARS);
                    if(num >= killsHolders.size()) return "";
                    StatCalculator.PlayerKillsHolder holder = killsHolders.get(num);

                    String color = "";
                    if(handler.getPlayerTeam(Bukkit.getPlayer(holder.name)) != null) color = handler.getPlayerTeam(Bukkit.getPlayer(holder.name)).color;
                    return color + holder.name + ChatColor.RESET + " Event " + holder.eventNum + ": " + holder.skywarsKills;
                }
                if(values[1].equalsIgnoreCase("sg")) {
                    ArrayList<StatCalculator.PlayerKillsHolder> killsHolders = plugin.statCalculator.getPlayerKillsHolders(GameType.SURVIVAL_GAMES);
                    if(num >= killsHolders.size()) return "";
                    StatCalculator.PlayerKillsHolder holder = killsHolders.get(num);

                    String color = "";
                    if(handler.getPlayerTeam(Bukkit.getPlayer(holder.name)) != null) color = handler.getPlayerTeam(Bukkit.getPlayer(holder.name)).color;
                    return color + holder.name + ChatColor.RESET + " Event " + holder.eventNum + ": " + holder.sgKills;

                }

            }
        } catch (Exception e) {};


        try {
            if(values[0].equalsIgnoreCase("totalKills")) {
                int num = Integer.parseInt(values[2])-1;
                if(values[1].equalsIgnoreCase("skywars")) {
                    ArrayList<StatCalculator.TotalKillsHolder> killsHolders = plugin.statCalculator.getTotalKillsHolders(GameType.SKYWARS, StatCalculator.TotalKillsComparator.SortType.KILLS);
                    if(num >= killsHolders.size()) return "";
                    StatCalculator.TotalKillsHolder holder = killsHolders.get(num);

                    String color = "";
                    if(handler.getPlayerTeam(Bukkit.getPlayer(holder.name)) != null) color = handler.getPlayerTeam(Bukkit.getPlayer(holder.name)).color;
                    return color + holder.name + ChatColor.RESET + ": " + holder.skywarsKills;
                }
                if(values[1].equalsIgnoreCase("sg")) {
                    ArrayList<StatCalculator.TotalKillsHolder> killsHolders = plugin.statCalculator.getTotalKillsHolders(GameType.SURVIVAL_GAMES, StatCalculator.TotalKillsComparator.SortType.KILLS);
                    if(num >= killsHolders.size()) return "";
                    StatCalculator.TotalKillsHolder holder = killsHolders.get(num);

                    String color = "";
                    if(handler.getPlayerTeam(Bukkit.getPlayer(holder.name)) != null) color = handler.getPlayerTeam(Bukkit.getPlayer(holder.name)).color;
                    return color + holder.name + ChatColor.RESET + ": " + holder.sgKills;

                }

            }
        } catch (Exception e) {};

        try {
            if(values[0].equalsIgnoreCase("wins")) {
                int num = Integer.parseInt(values[2])-1;
                if(values[1].equalsIgnoreCase("skywars")) {
                    ArrayList<StatCalculator.TotalKillsHolder> killsHolders = plugin.statCalculator.getTotalKillsHolders(GameType.SKYWARS, StatCalculator.TotalKillsComparator.SortType.WINS);
                    if(num >= killsHolders.size()) return "";
                    StatCalculator.TotalKillsHolder holder = killsHolders.get(num);

                    String color = "";
                    if(handler.getPlayerTeam(Bukkit.getPlayer(holder.name)) != null) color = handler.getPlayerTeam(Bukkit.getPlayer(holder.name)).color;
                    return color + holder.name + ChatColor.RESET + ": " + holder.skywarsWins;
                }
                if(values[1].equalsIgnoreCase("sg")) {
                    ArrayList<StatCalculator.TotalKillsHolder> killsHolders = plugin.statCalculator.getTotalKillsHolders(GameType.SURVIVAL_GAMES, StatCalculator.TotalKillsComparator.SortType.WINS);
                    if(num >= killsHolders.size()) return "";
                    StatCalculator.TotalKillsHolder holder = killsHolders.get(num);

                    String color = "";
                    if(handler.getPlayerTeam(Bukkit.getPlayer(holder.name)) != null) color = handler.getPlayerTeam(Bukkit.getPlayer(holder.name)).color;
                    return color + holder.name + ChatColor.RESET + ": " + holder.sgWins;

                }

            }
        } catch (Exception e) {};


        return null;
    }
}
