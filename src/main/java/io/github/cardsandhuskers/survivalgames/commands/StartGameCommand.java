package io.github.cardsandhuskers.survivalgames.commands;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.session.ClipboardHolder;
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

import java.io.File;
import java.io.FileInputStream;
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
        if(sender instanceof Player p) {
            if (args.length > 0) {
                try {
                    multiplier = Double.parseDouble(args[0]);
                    startGame();
                } catch (Exception e) {
                    p.sendMessage(ChatColor.RED + "ERROR: argument must be a double");
                }
            } else {
                startGame();
            }
        } else {
            if (args.length > 0) {
                try {
                    multiplier = Double.parseDouble(args[0]);
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

        for(Player p:Bukkit.getOnlinePlayers()) {
            p.setHealth(20);
            p.setSaturation(20);
            p.setFoodLevel(20);
            Inventory inv = p.getInventory();
            inv.clear();
        }

        //fill the chests
        try {
            chests = new Chests(plugin);
            chests.populateChests();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for(Team t:handler.getTeams()) {
            t.resetTempPoints();
        }

        totalPlayers = Bukkit.getOnlinePlayers().size();

        Location spawnPoint = plugin.getConfig().getLocation("spawnPoint");
        for(Player p:Bukkit.getOnlinePlayers()) {
            p.teleport(spawnPoint);
            Inventory inv = p.getInventory();
            inv.clear();
            if(handler.getPlayerTeam(p) == null) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()-> {
                    p.setGameMode(GameMode.SPECTATOR);
                }, 20L);
            }
        }
        Border worldBorder = new Border(plugin);
        worldBorder.buildWorldBorder();

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

        getServer().getPluginManager().registerEvents(new PlayerAttackListener(ppAPI, playerDeathHandler, storedAttackers, attackerTimers), plugin);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(plugin), plugin);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), plugin);
        getServer().getPluginManager().registerEvents(new ItemClickListener(), plugin);
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(ppAPI, playerDeathHandler, storedAttackers), plugin);
        getServer().getPluginManager().registerEvents(new PlayerLeaveListener(playerDeathHandler), plugin);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(playerDeathHandler, plugin), plugin);



        //Load Schematic
        BukkitWorld weWorld = new BukkitWorld(plugin.getConfig().getLocation("pos1").getWorld());
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
        Countdown timer = new Countdown((JavaPlugin)plugin,
                90,
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
                    if(t.getSecondsLeft() == 80) {
                        Bukkit.broadcastMessage(ChatColor.STRIKETHROUGH + "----------------------------------------");
                        Bukkit.broadcastMessage(StringUtils.center(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Survival Games", 30));
                        Bukkit.broadcastMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "How To Play:");
                        Bukkit.broadcastMessage("This iconic survival games map returns!" +
                                "\nWork with your teammates to take down the other teams and be the last one standing!" +
                                "\nThe game will start with a 45 second grace period where PvP is disabled." +
                                "\nThe worldborder will shrink over time. Don't get caught outside it, you will die.");
                        Bukkit.broadcastMessage(ChatColor.STRIKETHROUGH + "----------------------------------------");
                    }
                    if(t.getSecondsLeft() == 70) {
                        Bukkit.broadcastMessage(ChatColor.STRIKETHROUGH + "----------------------------------------");
                        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "How is the game Scored:");
                        Bukkit.broadcastMessage("For winning: " + ChatColor.GOLD + (int)(200 * multiplier) + ChatColor.RESET + " points per player on your team (alive or dead)" +
                                "\nFor Each Kill: " + ChatColor.GOLD + (int)(50 * multiplier) + ChatColor.RESET + " points" +
                                "\nFor each player you outlive: " + ChatColor.GOLD + (int)(10 * multiplier) + ChatColor.RESET + " points");
                        Bukkit.broadcastMessage(ChatColor.STRIKETHROUGH + "----------------------------------------");
                    }

                    if(t.getSecondsLeft() == 15) {
                        try {
                            gameStageHandler.updateGlass(Material.BARRIER);
                            gameStageHandler.teleportPlayers();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if(t.getSecondsLeft() < 5) {
                        for(Player p:Bukkit.getOnlinePlayers()) {
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1F);
                            if(t.getSecondsLeft() != 0) {
                                p.sendTitle(ChatColor.GREEN + ">" + t.getSecondsLeft() + "<", "", 2, 16, 2);
                            }
                        }
                    }
                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        timer.scheduleTimer();
    }

}
