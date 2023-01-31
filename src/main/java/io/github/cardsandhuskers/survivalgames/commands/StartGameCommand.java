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
import org.black_ixx.playerpoints.PlayerPointsAPI;
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
    private PlayerPointsAPI ppAPI;
    private PlayerDeathHandler playerDeathHandler;
    private ArrayList<PlayerTracker> trackerList;
    public StartGameCommand(SurvivalGames plugin, PlayerPointsAPI ppAPI) {
        this.plugin = plugin;
        this.ppAPI = ppAPI;
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
        if(gameType == GameType.SURVIVAL_GAMES) {
            worldBorder.buildWorldBorder(0, -2);
        }
        if(gameType == GameType.SKYWARS) {
            //TODO CHANGE COORDINATES BASED ON CENTER
            //Skywars may not need a worldborder, not sure yet.
            worldBorder.buildWorldBorder(0, 0);
        }

        HashMap<Player, Player> storedAttackers = new HashMap<>();
        HashMap<Player, Integer> attackerTimers = new HashMap<>();

        AttackerTimersHandler attackerTimersHandler = new AttackerTimersHandler(plugin, storedAttackers, attackerTimers);
        attackerTimersHandler.startOperation();

        spawnPoint.getWorld().setTime(0);
        ArrayList<Team> teamList = new ArrayList<>();
        trackerList = new ArrayList<>();

        gameStageHandler = new GameStageHandler(plugin, chests, ppAPI, worldBorder, teamList, attackerTimersHandler, trackerList);

        playerDeathHandler = new PlayerDeathHandler(ppAPI, plugin, gameStageHandler, teamList);
        //attacked, attacker (an attacked player can only have 1 attacker, vise versa is not true)

        PlayerDamageListener playerDamageListener = new PlayerDamageListener(ppAPI, playerDeathHandler, storedAttackers);
        getServer().getPluginManager().registerEvents(new PlayerAttackListener(ppAPI, playerDeathHandler, storedAttackers, attackerTimers, playerDamageListener, plugin), plugin);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(plugin), plugin);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), plugin);
        getServer().getPluginManager().registerEvents(new ItemClickListener(), plugin);

        getServer().getPluginManager().registerEvents(playerDamageListener, plugin);
        getServer().getPluginManager().registerEvents(new PlayerLeaveListener(playerDamageListener), plugin);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(playerDeathHandler, plugin), plugin);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(playerDamageListener), plugin);
        getServer().getPluginManager().registerEvents(new PearlThrowListener(), plugin);


        if(gameType == GameType.SKYWARS) {
            ResetArenaCommand resetArenaCommand = new ResetArenaCommand(plugin);
            resetArenaCommand.resetArena(GameType.SKYWARS);
        }
        //Load Schematic
        //BukkitWorld weWorld = new BukkitWorld(plugin.getConfig().getLocation("pos1").getWorld());
/*
        for(int i = 1; i <= 27; i++) {
            int finalI = i;
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()-> {
                Clipboard clipboard;
                File file = new File("plugins/SurvivalGames/arena" + finalI + ".schem");

                ClipboardFormat format = ClipboardFormats.findByFile(file);
                try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
                    clipboard = reader.read();

                    try (EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld)) {
                        Operation operation = new ClipboardHolder(clipboard)
                                .createPaste(editSession)
                                .to(clipboard.getOrigin())
                                // configure here
                                .build();
                        Operations.complete(operation);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 40L * i);
        }

 */

        pregameTimer();
    }


    private void pregameTimer() {
        int time;
        if(gameNumber == 1) {
            time = plugin.getConfig().getInt(gameType + ".PregameTime");
        } else {
            time = 30;
        }
        Countdown timer = new Countdown((JavaPlugin)plugin,
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
                            if (t.getSecondsLeft() == t.getTotalSeconds() - 1) {
                                Bukkit.broadcastMessage(ChatColor.STRIKETHROUGH + "----------------------------------------");
                                Bukkit.broadcastMessage(StringUtils.center(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Survival Games", 30));
                                Bukkit.broadcastMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "How To Play:");
                                Bukkit.broadcastMessage("This iconic survival games map returns!" +
                                        "\nWork with your teammates to take down the other teams and be the last one standing!" +
                                        "\nThe game will start with a 45 second grace period where PvP is disabled." +
                                        "\nThe worldborder will shrink over time. Don't get caught outside it, you will die.");
                                Bukkit.broadcastMessage(ChatColor.STRIKETHROUGH + "----------------------------------------");
                            }
                        }
                        if (gameType == GameType.SKYWARS) {
                            if (t.getSecondsLeft() == t.getTotalSeconds() - 1) {
                                Bukkit.broadcastMessage(ChatColor.STRIKETHROUGH + "----------------------------------------");
                                Bukkit.broadcastMessage(StringUtils.center(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Skywars", 30));
                                Bukkit.broadcastMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "How To Play:");
                                Bukkit.broadcastMessage("Welcome to Skywars!" +
                                        "\nWork with your teammates to take down the other teams and be the last one standing!" +
                                        "\nEach island has chests so you can get geared up before heading to the center to fight." +
                                        "\nBe careful not to fall off the edge! The void will kill you.");
                                Bukkit.broadcastMessage(ChatColor.STRIKETHROUGH + "----------------------------------------");
                            }
                        }

                        if (t.getSecondsLeft() == t.getTotalSeconds() - 11) {
                            Bukkit.broadcastMessage(ChatColor.STRIKETHROUGH + "----------------------------------------");
                            Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "How is the game Scored:");
                            Bukkit.broadcastMessage("For winning: " + ChatColor.GOLD + (int) (plugin.getConfig().getInt(gameType + ".winPoints") * multiplier) + ChatColor.RESET + " points divided among the team members" +
                                    "\nFor Each Kill: " + ChatColor.GOLD + (int) (plugin.getConfig().getInt(gameType + ".killPoints") * multiplier) + ChatColor.RESET + " points" +
                                    "\nFor each player you outlive: " + ChatColor.GOLD + (int) (plugin.getConfig().getInt(gameType + ".survivalPoints") * multiplier) + ChatColor.RESET + " points");
                            Bukkit.broadcastMessage(ChatColor.STRIKETHROUGH + "----------------------------------------");
                        }
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
                    if(t.getSecondsLeft() == 3) {
                        //fill the chests
                        chests.populateChests();
                    }
                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        timer.scheduleTimer();
    }

}
