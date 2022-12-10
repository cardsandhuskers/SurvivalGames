package io.github.cardsandhuskers.survivalgames.handlers;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import io.github.cardsandhuskers.survivalgames.objects.Countdown;
import io.github.cardsandhuskers.teams.objects.Team;
import io.github.cardsandhuskers.teams.objects.TempPointsHolder;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.meta.FireworkMeta;
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
                    final Team winner;
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
                    Location pos1 = plugin.getConfig().getLocation("pos1");
                    Location pos2 = plugin.getConfig().getLocation("pos2");
                    Location spawn = new Location(pos1.getWorld(), (pos1.getX() + pos2.getX())/2, 30, (pos1.getZ() + pos2.getZ())/2);

                    Location l1 = new Location(spawn.getWorld(), spawn.getX() + 10, spawn.getY() + 5, spawn.getZ() + 10);
                    Location l2 = new Location(spawn.getWorld(), spawn.getX() + 10, spawn.getY() + 5, spawn.getZ() - 10);
                    Location l3 = new Location(spawn.getWorld(), spawn.getX() -10, spawn.getY() + 5, spawn.getZ() + 10);
                    Location l4 = new Location(spawn.getWorld(), spawn.getX() - 10, spawn.getY() + 5, spawn.getZ() - 10);

                    for(int i = 0; i < 10; i++) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                            Firework firework1 = (Firework) l1.getWorld().spawnEntity(l1, EntityType.FIREWORK);
                            FireworkMeta fireworkMeta = firework1.getFireworkMeta();
                            fireworkMeta.addEffect(FireworkEffect.builder().withColor(winner.translateColor()).flicker(true).build());
                            firework1.setFireworkMeta(fireworkMeta);

                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                                firework1.detonate();
                            }, 30L);


                            Firework firework2 = (Firework) l2.getWorld().spawnEntity(l2, EntityType.FIREWORK);
                            firework2.setFireworkMeta(fireworkMeta);
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                                firework2.detonate();
                            }, 30L);

                            Firework firework3 = (Firework) l3.getWorld().spawnEntity(l3, EntityType.FIREWORK);
                            firework3.setFireworkMeta(fireworkMeta);
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                                firework3.detonate();
                            }, 30L);

                            Firework firework4 = (Firework) l4.getWorld().spawnEntity(l4, EntityType.FIREWORK);
                            firework4.setFireworkMeta(fireworkMeta);
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                                firework4.detonate();
                            }, 30L);

                        }, 20L * i);
                    }
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
                                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                            }
                        }
                    }
                    if(t.getSecondsLeft() == 20) {
                        ArrayList<TempPointsHolder> tempPointsList = new ArrayList<>();
                        for(Team team: handler.getTeams()) {
                            for(Player p:team.getOnlinePlayers()) {
                                tempPointsList.add(team.getPlayerTempPoints(p));
                                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
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

                    if(t.getSecondsLeft() == 12) {
                        ArrayList<Team> teamList = handler.getPointsSortedList();

                        Bukkit.broadcastMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "Team Performance:");
                        Bukkit.broadcastMessage(ChatColor.GREEN + "------------------------------");
                        int counter = 1;
                        for(Team team:teamList) {
                            Bukkit.broadcastMessage(counter + ". " + team.color + ChatColor.BOLD +  team.getTeamName() + ChatColor.RESET + " Points: " + team.getTempPoints());
                            counter++;
                        }
                        Bukkit.broadcastMessage(ChatColor.GREEN + "------------------------------");
                        for(Player p: Bukkit.getOnlinePlayers()) {
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                        }
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
        for(Player p:Bukkit.getOnlinePlayers()) {
            if(p.isOp()) {
                p.performCommand("startRound");
                break;
            }
        }
    }
}
