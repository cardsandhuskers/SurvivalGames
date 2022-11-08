package io.github.cardsandhuskers.survivalgames.handlers;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import io.github.cardsandhuskers.survivalgames.objects.Countdown;
import io.github.cardsandhuskers.teams.objects.Team;
import io.github.cardsandhuskers.teams.objects.TempPointsHolder;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.*;

public class GameEndHandler {
    private SurvivalGames plugin;
    private ArrayList<Team> teamList;
    private PlayerPointsAPI ppAPI;

    public GameEndHandler(SurvivalGames plugin, ArrayList<Team> teamList, PlayerPointsAPI ppAPI) {
        this.ppAPI = ppAPI;
        this.teamList = teamList;
        this.plugin = plugin;
    }

    public void gameEndTimer() {
        int totalSeconds = 30;
        Countdown timer = new Countdown((JavaPlugin)plugin,

                totalSeconds,
                //Timer Start
                () -> {
                    Team winner;
                    if(!teamList.isEmpty()) {
                        winner = teamList.get(0);
                    } else {
                        winner = handler.getTeam(0);
                    }
                    int numPlayers = winner.getOnlinePlayers().size();
                    for(Player p: winner.getOnlinePlayers()) {
                        winner.addTempPoints(p, (int) ((800/numPlayers) * multiplier));
                        ppAPI.give(p.getUniqueId(), (int) ((800/numPlayers) * multiplier));
                    }
                    gameState = SurvivalGames.State.GAME_OVER;
                    Bukkit.broadcastMessage(ChatColor.DARK_BLUE + "------------------------------");
                    Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Winner:");
                    Bukkit.broadcastMessage(winner.color + winner.getTeamName());
                    Bukkit.broadcastMessage(ChatColor.DARK_BLUE + "------------------------------");

                    //put title on screen
                    for(Player p:Bukkit.getOnlinePlayers()) {
                        p.sendTitle("Winner:", winner.color + winner.getTeamName(), 5, 30, 5);
                    }
                    //fireworks


                },

                //Timer End
                () -> {
                    endGame();
                },

                //Each Second
                (t) -> {
                    timeVar = t.getSecondsLeft();
                    //show each player their team performance
                    if(t.getSecondsLeft() == 25) {
                        for (Team team : handler.getTeams()) {
                            ArrayList<TempPointsHolder> tempPointsList = new ArrayList<>();
                            for (Player p : team.getOnlinePlayers()) {
                                if (team.getPlayerTempPoints(p) != null) {
                                    tempPointsList.add(team.getPlayerTempPoints(p));
                                }
                            }
                            Collections.sort(tempPointsList, Comparator.comparing(TempPointsHolder::getPoints));
                            Collections.reverse(tempPointsList);

                            for (Player p : team.getOnlinePlayers()) {
                                p.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Your Team Standings:");
                                p.sendMessage(ChatColor.DARK_BLUE + "------------------------------");
                                int number = 1;
                                for (TempPointsHolder h : tempPointsList) {
                                    p.sendMessage(number + ". " + handler.getPlayerTeam(p).color + h.getPlayer().getName() + ChatColor.RESET + "    Points: " + h.getPoints());
                                    number++;
                                }
                                p.sendMessage(ChatColor.DARK_BLUE + "------------------------------\n");
                            }
                        }
                    }
                    if(t.getSecondsLeft() == 20) {
                        ArrayList<TempPointsHolder> tempPointsList = new ArrayList<>();
                        for(Team team: handler.getTeams()) {
                            for(Player p:team.getOnlinePlayers()) {
                                tempPointsList.add(team.getPlayerTempPoints(p));
                            }
                        }
                        Collections.sort(tempPointsList, Comparator.comparing(TempPointsHolder::getPoints));
                        Collections.reverse(tempPointsList);

                        int max;
                        if(tempPointsList.size() >= 5) {
                            max = 4;
                        } else {
                            max = tempPointsList.size() - 1;
                        }

                        Bukkit.broadcastMessage("\n" + ChatColor.RED + "" + ChatColor.BOLD + "Top 5 Players:");
                        Bukkit.broadcastMessage(ChatColor.DARK_RED + "------------------------------");
                        int number = 1;
                        for(int i = 0; i <= max; i++) {
                            TempPointsHolder h = tempPointsList.get(i);
                            Bukkit.broadcastMessage(number + ". " + handler.getPlayerTeam(h.getPlayer()).color + h.getPlayer().getName() + ChatColor.RESET + "    Points: " +  h.getPoints());
                            number++;
                        }
                        Bukkit.broadcastMessage(ChatColor.DARK_RED + "------------------------------");
                    }
                }
        );
        // Start scheduling, don't use the "run" method unless you want to skip a second
        timer.scheduleTimer();
    }



    public void endGame() {
        HandlerList.unregisterAll(plugin);
        Location lobby = plugin.getConfig().getLocation("Lobby");
        for(Player p: Bukkit.getOnlinePlayers()) {
            p.teleport(lobby);
        }
    }
}
