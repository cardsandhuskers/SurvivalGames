package io.github.cardsandhuskers.survivalgames.objects;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import io.github.cardsandhuskers.survivalgames.objects.border.Border;
import io.github.cardsandhuskers.survivalgames.objects.stats.PlayerStatsHolder;
import io.github.cardsandhuskers.survivalgames.objects.stats.Tuples;
import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.*;
import static io.github.cardsandhuskers.survivalgames.SurvivalGames.GameType.SKYWARS;
import static io.github.cardsandhuskers.survivalgames.SurvivalGames.GameType.SURVIVAL_GAMES;
import static io.github.cardsandhuskers.survivalgames.handlers.PlayerDeathHandler.numPlayers;
import static io.github.cardsandhuskers.survivalgames.handlers.PlayerDeathHandler.numTeams;
import static io.github.cardsandhuskers.survivalgames.objects.border.BorderOld.borderSize;

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


        if(gameType == SKYWARS) {
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
            return " +- " + Border.getSize();
        }
        if(s.equalsIgnoreCase("playerkills")) {
            if (!playerKills.containsKey((Player) p)) {
                playerKills.put((Player) p, 0);
            }
            return playerKills.get(p) + "";
        }
        if(s.equalsIgnoreCase("round")) {
            return gameNumber + "";
        }

        String[] values = s.split("_");
        //playerKills, totalKills, wins
        //sg / skywars
        // num
        //playerKills_sg_1
        try {
            if(values[0].equalsIgnoreCase("playerKills")) {
                int place = Integer.parseInt(values[2]);
                GameType type = SKYWARS;
                if(values[1].equalsIgnoreCase("skywars")) type = SKYWARS;
                if(values[1].equalsIgnoreCase("sg")) type = SURVIVAL_GAMES;

                Tuples.EventKillsHolder holder = plugin.statCalculator.getEventKills(place, type);
                if (holder == null) return "";

                Team team = TeamHandler.getInstance().getPlayerTeam(Bukkit.getPlayer(holder.getName()));
                String color = "";
                if(team != null) color = team.getColor();
                return color + holder.getName() + ChatColor.RESET + " Event " + holder.getEvent() + ": " + holder.getKills();

            }
        } catch (Exception e) {e.printStackTrace();};

        try {
            if(values[0].equalsIgnoreCase("totalKills")) {
                int place = Integer.parseInt(values[2]);
                GameType type = SKYWARS;

                if(values[1].equalsIgnoreCase("skywars")) type = SKYWARS;
                if(values[1].equalsIgnoreCase("sg")) type = SURVIVAL_GAMES;

                PlayerStatsHolder holder = plugin.statCalculator.getTotalKills(place, type);
                if (holder == null) return "";

                Team team = TeamHandler.getInstance().getPlayerTeam(Bukkit.getPlayer(holder.getName()));
                String color = "";
                if(team != null) color = team.getColor();
                return color + holder.getName() + ChatColor.RESET + ": " + holder.getKills(type);

            }
        } catch (Exception e) {e.printStackTrace();};

        try {
            if(values[0].equalsIgnoreCase("yourKills")) {
                GameType type = SKYWARS;
                if(values[1].equalsIgnoreCase("skywars")) type = SKYWARS;
                if(values[1].equalsIgnoreCase("sg")) type = SURVIVAL_GAMES;

                return plugin.statCalculator.getPlayerKillsPosition(p, type);
            }
        } catch (Exception e) {e.printStackTrace();}

        try {
            //survivalgames_kd_1
            if(values[0].equalsIgnoreCase("kd")) {

                int place = Integer.parseInt(values[1]);
                return plugin.statCalculator.getKDLoc(p, place);
            }

            if(values[0].equalsIgnoreCase("allkd")) {
                ArrayList<Tuples.KDTuple> kds = plugin.statCalculator.getAllKDs(p);
                if(kds != null) {
                    for (Tuples.KDTuple kd : kds) {
                        p.getPlayer().sendMessage(kd.toString());
                    }
                }
            return "";
            }
        } catch (Exception e) {e.printStackTrace();}


        return null;
    }
}
