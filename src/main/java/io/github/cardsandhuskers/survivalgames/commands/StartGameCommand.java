package io.github.cardsandhuskers.survivalgames.commands;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import io.github.cardsandhuskers.survivalgames.handlers.AttackerTimersHandler;
import io.github.cardsandhuskers.survivalgames.handlers.GameStageHandler;
import io.github.cardsandhuskers.survivalgames.handlers.PlayerDeathHandler;
import io.github.cardsandhuskers.survivalgames.listeners.*;
import io.github.cardsandhuskers.survivalgames.objects.Border;
import io.github.cardsandhuskers.survivalgames.objects.Chests;
import io.github.cardsandhuskers.survivalgames.objects.Countdown;
import io.github.cardsandhuskers.survivalgames.objects.PlayerTracker;
import io.github.cardsandhuskers.teams.objects.Team;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.*;
import static org.bukkit.Bukkit.getServer;

public class StartGameCommand implements CommandExecutor {
    private SurvivalGames plugin;
    private Chests chests;
    private GameStageHandler gameStageHandler;
    private PlayerDeathHandler playerDeathHandler;
    private ArrayList<PlayerTracker> trackerList;
    public Countdown pregameTimer;

    /**
     * All of the strings for the pregame information
     */
    private String SURVIVAL_GAMES_DESCRIPTION = ChatColor.STRIKETHROUGH + "----------------------------------------\n" +
                                StringUtils.center(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Survival Games", 30) +
                                ChatColor.BLUE + "" + ChatColor.BOLD + "\nHow To Play:" +
                                "\nThis iconic survival games map returns!" +
                                "\nWork with your teammates to take down the other teams and be the last one standing!" +
                                "\nThe game will start with a 45 second grace period where PvP is disabled." +
                                "\nThe worldborder will shrink over time. Don't get caught outside it, you will die." +
                                ChatColor.STRIKETHROUGH + "\n----------------------------------------",
                    SKYWARS_DESCRIPTION = ChatColor.STRIKETHROUGH + "----------------------------------------\n" +
                                StringUtils.center(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Skywars", 30) +
                                ChatColor.BLUE + "" + ChatColor.BOLD + "\nHow To Play:" +
                                "\nThere will be 2 rounds of Skywars on the famous Hypixel map: Elven" +
                                "\nWork with your teammates to take down the other teams and be the last one standing!" +
                                "\nEach island has chests to get geared up, while the middle has many chests with even better loot!" +
                                "\nBe careful not to fall off the edge! The void will kill you." +
                                ChatColor.STRIKETHROUGH + "\n----------------------------------------",
                    POINTS_DESCRIPTION = ChatColor.STRIKETHROUGH + "----------------------------------------" +
                                ChatColor.GOLD + "" + ChatColor.BOLD + "\nHow is the game Scored:" +
                                "\nFor winning: " + ChatColor.GOLD + (int) (plugin.getConfig().getInt(gameType + ".winPoints") * multiplier) + ChatColor.RESET + " points divided among the team members" +
                                "\nFor Each Kill: " + ChatColor.GOLD + (int) (plugin.getConfig().getInt(gameType + ".killPoints") * multiplier) + ChatColor.RESET + " points" +
                                "\nFor each player you outlive: " + ChatColor.GOLD + (int) (plugin.getConfig().getInt(gameType + ".survivalPoints") * multiplier) + ChatColor.RESET + " points" +
                                ChatColor.STRIKETHROUGH + "\n----------------------------------------";


    public StartGameCommand(SurvivalGames plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player p && p.isOp()) {
            if (args.length > 0) {
                try {
                    multiplier = Double.parseDouble(args[0]);
                    try {
                        //TRUE = SKYWARS "altgame"
                        gameType = GameType.valueOf(args[1].toUpperCase());
                    } catch (Exception e) {
                        System.out.println(ChatColor.RED + "ERROR: game type must be SURVIVAL_GAMES or SKYWARS");
                        return false;
                    }
                    startGame();
                } catch (Exception e) {
                    p.sendMessage(ChatColor.RED + "ERROR: argument must be a double");
                }
            } else {
                startGame();
            }
        } else if(sender instanceof Player p) {
            p.sendMessage(ChatColor.RED + "You don't have permission to do this");
        }else {
            if (args.length > 0) {
                try {
                    multiplier = Double.parseDouble(args[0]);
                    try {
                        //TRUE = SKYWARS "altgame"
                        gameType = GameType.valueOf(args[1].toUpperCase());
                    } catch (Exception e) {
                        System.out.println(ChatColor.RED + "ERROR: game type must be SURVIVAL_GAMES or SKYWARS");
                        return false;
                    }
                    startGame();
                } catch (Exception e) {
                    System.out.println(ChatColor.RED + "ERROR: argument must be a double");
                }
            } else {
                startGame();
            }
        }

        return true;
    }

    public void startGame() {
        if(handler.getNumTeams() < 2) {
            Bukkit.broadcastMessage(ChatColor.RED + "ERROR: There must be at least 2 teams!");
            return;
        }

        playerKills = new HashMap<>();
        try {
            chests = new Chests(plugin);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for(Player p:Bukkit.getOnlinePlayers()) {
            p.setHealth(20);
            p.setSaturation(20);
            p.setFoodLevel(20);
            Inventory inv = p.getInventory();
            inv.clear();
        }

        if(gameNumber == 1) {
            for (Team t : handler.getTeams()) {
                t.resetTempPoints();
            }
        }

        totalPlayers = 0;
        for(Player p:Bukkit.getOnlinePlayers()) {
            if(handler.getPlayerTeam(p) != null) {
                totalPlayers++;
            }
        }

        Location spawnPoint = plugin.getConfig().getLocation(gameType + ".spawnPoint");
        for(Player p:Bukkit.getOnlinePlayers()) {
            p.teleport(spawnPoint);
            Inventory inv = p.getInventory();
            inv.clear();
            p.setGameMode(GameMode.SURVIVAL);
            if(handler.getPlayerTeam(p) == null) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()-> {
                    p.setGameMode(GameMode.SPECTATOR);
                }, 10L);
            } else {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()-> {
                    p.setGameMode(GameMode.SURVIVAL);
                }, 10L);
            }
        }
        Border worldBorder = new Border(plugin);

        Location pos1 = plugin.getConfig().getLocation(gameType + ".pos1");
        Location pos2 = plugin.getConfig().getLocation(gameType + ".pos2");
        int centerX = (int)(pos1.getX() + pos2.getX())/2;
        int centerZ = (int)(pos1.getZ() + pos2.getZ())/2;

        worldBorder.buildWorldBorder(centerX, centerZ);


        HashMap<Player, Player> storedAttackers = new HashMap<>();
        HashMap<Player, Integer> attackerTimers = new HashMap<>();

        AttackerTimersHandler attackerTimersHandler = new AttackerTimersHandler(plugin, storedAttackers, attackerTimers);
        attackerTimersHandler.startOperation();

        spawnPoint.getWorld().setTime(0);
        ArrayList<Team> teamList = new ArrayList<>();
        trackerList = new ArrayList<>();

        gameStageHandler = new GameStageHandler(plugin, chests, worldBorder, teamList, attackerTimersHandler, trackerList);

        playerDeathHandler = new PlayerDeathHandler(plugin, gameStageHandler, teamList);
        //attacked, attacker (an attacked player can only have 1 attacker, vise versa is not true)

        PlayerDamageListener playerDamageListener = new PlayerDamageListener(playerDeathHandler, storedAttackers);
        getServer().getPluginManager().registerEvents(new PlayerAttackListener(playerDeathHandler, storedAttackers, attackerTimers, plugin), plugin);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(plugin), plugin);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), plugin);
        getServer().getPluginManager().registerEvents(new ItemClickListener(), plugin);

        getServer().getPluginManager().registerEvents(playerDamageListener, plugin);
        getServer().getPluginManager().registerEvents(new PlayerLeaveListener(playerDamageListener), plugin);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(playerDeathHandler, plugin), plugin);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(playerDamageListener), plugin);
        getServer().getPluginManager().registerEvents(new PearlThrowListener(), plugin);

        HashMap<Player, Location> playerDeathLocationMap = new HashMap<>();
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(plugin, playerDeathLocationMap, storedAttackers, playerDeathHandler), plugin);
        getServer().getPluginManager().registerEvents(new PlayerRespawnListener(plugin, playerDeathLocationMap), plugin);

        //if(gameType == GameType.SKYWARS) {
            ResetArenaCommand resetArenaCommand = new ResetArenaCommand(plugin);
            resetArenaCommand.resetArena(gameType);
        //}
        pregameTimer();
    }


    private void pregameTimer() {
        int time;
        if(gameNumber == 1) {
            time = plugin.getConfig().getInt(gameType + ".PregameTime");
        } else {
            time = 30;
        }
        pregameTimer = new Countdown(plugin,
                time,
                //Timer Start
                () -> {
                    gameState = SurvivalGames.State.GAME_STARTING;
                },

                //Timer End
                () -> {
                    for(Player p:Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2F);
                        p.sendTitle(ChatColor.GREEN + ">" + "GO" + "<", "", 2, 16, 2);
                    }
                    gameStageHandler.startGame();
                    for(Team t:handler.getTeams()) {
                        for(Player p:t.getOnlinePlayers()) {
                            PlayerTracker tracker = new PlayerTracker(playerDeathHandler, p);
                            tracker.giveCompass();
                            trackerList.add(tracker);
                        }
                    }
                },

                //Each Second
                (t) -> {
                    timeVar = t.getSecondsLeft();
                    if(gameNumber == 1) {
                        if (gameType == GameType.SURVIVAL_GAMES) {
                            if (t.getSecondsLeft() == t.getTotalSeconds() - 1) Bukkit.broadcastMessage(SURVIVAL_GAMES_DESCRIPTION);
                        }
                        if (gameType == GameType.SKYWARS) {
                            if (t.getSecondsLeft() == t.getTotalSeconds() - 1) Bukkit.broadcastMessage(SKYWARS_DESCRIPTION);
                        }

                        if (t.getSecondsLeft() == t.getTotalSeconds() - 11) Bukkit.broadcastMessage(POINTS_DESCRIPTION);
                    }

                    if (t.getSecondsLeft() == 15) {
                        try {
                            gameStageHandler.updateGlass(Material.GLASS);
                            gameStageHandler.teleportPlayers();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if (t.getSecondsLeft() < 5) {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1F);
                            if (t.getSecondsLeft() != 0) {
                                p.sendTitle(ChatColor.GREEN + ">" + t.getSecondsLeft() + "<", "", 2, 16, 2);
                            }
                        }
                    }
                    if(t.getSecondsLeft() == 14) {
                        //fill the chests
                        chests.populateChests();
                    }
                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        pregameTimer.scheduleTimer();
    }

    public void cancelGame() {
        gameStageHandler.endGame();
    }

}
