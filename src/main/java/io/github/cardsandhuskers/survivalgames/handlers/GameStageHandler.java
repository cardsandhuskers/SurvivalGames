package io.github.cardsandhuskers.survivalgames.handlers;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import io.github.cardsandhuskers.survivalgames.objects.Border;
import io.github.cardsandhuskers.survivalgames.objects.Chests;
import io.github.cardsandhuskers.survivalgames.objects.Countdown;
import io.github.cardsandhuskers.teams.objects.Team;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.*;

public class GameStageHandler {
    private SurvivalGames plugin;
    private Chests chests;
    private PlayerPointsAPI ppAPI;
    private Border worldBorder;
    private Countdown gameTimer;
    private Countdown restockTimer;
    private Countdown deathmatchTimer;
    private Countdown preDeathmatch;
    private AttackerTimersHandler attackerTimersHandler;
    ArrayList<Team> teamList;
    public GameStageHandler(SurvivalGames plugin, Chests chests, PlayerPointsAPI ppAPI, Border worldBorder, ArrayList<Team> teamList, AttackerTimersHandler attackerTimersHandler) {
        this.plugin = plugin;
        this.chests = chests;
        this.ppAPI = ppAPI;
        this.worldBorder = worldBorder;
        this.teamList = teamList;
        this.attackerTimersHandler = attackerTimersHandler;
    }
    public void startGame() {
        for(Team t: handler.getTeams()) {
            for(Player p:t.getOnlinePlayers()) {
                p.setGameMode(GameMode.SURVIVAL);
                p.setHealth(20);
                p.setSaturation(20);
                p.setFoodLevel(20);
            }
        }
        try {
            updateGlass(Material.AIR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        gameTimer();
        restockTimer();
        gracePeriodTimer();
    }

    public void endGame() {
        gameTimer.cancelTimer();
        restockTimer.cancelTimer();
        if(deathmatchTimer != null) {
            deathmatchTimer.cancelTimer();
        }
        if(preDeathmatch != null) {
            preDeathmatch.cancelTimer();
        }
        worldBorder.shrinkWorldBorder(50, 1);
        attackerTimersHandler.cancelOperation();
    }

    private void gameTimer() {
        //should be 720 seconds
        int totalSeconds = 90;
        gameTimer = new Countdown((JavaPlugin)plugin,

                totalSeconds,
                //Timer Start
                () -> {
                    altTimeVar = totalSeconds;
                    gameState = State.GRACE_PERIOD;
                    worldBorder.shrinkWorldBorder(90, totalSeconds);
                },

                //Timer End
                () -> {
                //start deathmatch
                    altTimeVar = 0;
                    startDeathmatch();
                },

                //Each Second
                (t) -> {
                    //+20 equal to startDeathmatch() timer quantity
                    SurvivalGames.altTimeVar = t.getSecondsLeft() + 20;
                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        gameTimer.scheduleTimer();
    }

    /**
     * Time before PVP is enabled, part of the main gameTimer
     */
    private void gracePeriodTimer() {
        Countdown timer = new Countdown((JavaPlugin)plugin,
                //should be 60
                15,
                //Timer Start
                () -> {
                },

                //Timer End
                () -> {
                    gameState = State.GAME_IN_PROGRESS;
                    Bukkit.broadcastMessage(ChatColor.RED + "PVP has been Enabled!");
                    for(Player p:Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1F);
                    }
                    timeVar = 0;

                },

                //Each Second
                (t) -> {
                    timeVar = t.getSecondsLeft();
                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        timer.scheduleTimer();
    }

    /**
     * Timer until restock, part of the main gameTimer
     */
    private void restockTimer() {
        restockTimer = new Countdown((JavaPlugin)plugin,
                //should be 420 (7mins)
                45,
                //Timer Start
                () -> {
                },

                //Timer End
                () -> {
                    timeVar = 0;
                    chests.populateChests();
                    Bukkit.broadcastMessage(ChatColor.BLUE + "Chests Have Been Restocked!");
                    for(Player p:Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1F);
                    }
                },

                //Each Second
                (t) -> {
                        timeVar = t.getSecondsLeft();
                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        restockTimer.scheduleTimer();
    }

    /**
     * Begins the process of deathmatch
     */
    public void startDeathmatch() {
        System.out.println(gameTimer.getSecondsLeft());
        gameTimer.cancelTimer();
        restockTimer.cancelTimer();

        int totalSeconds = 20;
        preDeathmatch = new Countdown((JavaPlugin) plugin,

                totalSeconds,
                //Timer Start
                () -> {

                },

                //Timer End
                () -> {
                    //start deathmatch
                    deathmatchPrepTimer();
                    altTimeVar = 0;
                },

                //Each Second
                (t) -> {
                    altTimeVar = t.getSecondsLeft();
                }
        );
        if(gameState != State.DEATHMATCH && gameTimer.getSecondsLeft() >= 2) {
            preDeathmatch.scheduleTimer();
            timeVar = 0;
        }

    }

    /**
     * Deathmatch timer, runs after the conclusion of the game timer
     */
    private void deathmatchPrepTimer() {
//should be 15 seconds
        int totalSeconds = 15;
        Countdown timer = new Countdown((JavaPlugin) plugin,

                totalSeconds,
                //Timer Start
                () -> {
                    altTimeVar = totalSeconds;
                    gameState = State.DEATHMATCH;
                    worldBorder.shrinkWorldBorder(50, 1);
                    try {
                        updateGlass(Material.BARRIER);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        teleportPlayers();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                },

                //Timer End
                () -> {
                    //start deathmatch
                    deathmatchTimer();
                    altTimeVar = 0;
                    try {
                        updateGlass(Material.AIR);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },

                //Each Second
                (t) -> {
                    altTimeVar = t.getSecondsLeft();
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
        timer.scheduleTimer();
    }

    /**
     * Deathmatch timer, runs after the conclusion of the game timer
     */
    private void deathmatchTimer() {
        //should be 120 seconds
        int totalSeconds = 30;
        deathmatchTimer = new Countdown((JavaPlugin)plugin,

                totalSeconds,
                //Timer Start
                () -> {
                    timeVar = totalSeconds;
                    worldBorder.shrinkWorldBorder(2, totalSeconds);
                },

                //Timer End
                () -> {
                    //start deathmatch
                    timeVar = 0;
                },

                //Each Second
                (t) -> {
                    //SurvivalGames.altTimeVar = t.getSecondsLeft();
                    timeVar = t.getSecondsLeft();
                }
        );
        // Start scheduling, don't use the "run" method unless you want to skip a second
        deathmatchTimer.scheduleTimer();
    }

    /**
     * Teleports all players to their spawn points
     * @throws IOException
     */
    public void teleportPlayers() throws IOException {
        File arenaFile = new File(Bukkit.getServer().getPluginManager().getPlugin("SurvivalGames").getDataFolder(),"arena.yml");
        if(!arenaFile.exists()) {
            //if the file does not exist, crash program, since game cannot run without it
            throw new IOException("FILE CANNOT BE FOUND");
        }
        FileConfiguration arenaFileConfig = YamlConfiguration.loadConfiguration(arenaFile);
        int counter = 1;
        while(arenaFileConfig.get("teamSpawn." + counter) != null && counter <= teamList.size()) {
            Location spawn = arenaFileConfig.getLocation("teamSpawn." + counter);
            for(Player p: teamList.get(counter - 1).getOnlinePlayers()) {
                p.teleport(spawn);
            }
            counter++;
        }
    }

    /**
     * updates the glass boxes around the spawn points
     * @param mat
     * @throws IOException
     */
    public void updateGlass(Material mat) throws IOException {
        File arenaFile = new File(Bukkit.getServer().getPluginManager().getPlugin("SurvivalGames").getDataFolder(),"arena.yml");
        if(!arenaFile.exists()) {
            //if the file does not exist, crash program, since game cannot run without it
            throw new IOException("FILE CANNOT BE FOUND");
        }
        FileConfiguration arenaFileConfig = YamlConfiguration.loadConfiguration(arenaFile);
        int counter = 1;
        while(arenaFileConfig.get("teamSpawn." + counter) != null) {
            Location spawn = arenaFileConfig.getLocation("teamSpawn." + counter);
            //spawn is on northwest corner (-x -y corner)
            //x-1 {z+1}, z-1{x+1}

            //loop 3 times to handle the y
            for(int y=0; y<= 2; y++) {
                Location loc = new Location(spawn.getWorld(), spawn.getX(), spawn.getY() + y, spawn.getZ());
                loc.setX(loc.getX() - 1);
                Block b = loc.getBlock();
                b.setType(mat);

                loc.setZ(loc.getZ() + 1);
                b = loc.getBlock();
                b.setType(mat);

                loc = new Location(spawn.getWorld(), spawn.getX(), spawn.getY() + y, spawn.getZ());
                loc.setZ(loc.getZ() - 1);
                b = loc.getBlock();
                b.setType(mat);

                loc.setX(loc.getX() + 1);
                b = loc.getBlock();
                b.setType(mat);

                loc = new Location(spawn.getWorld(), spawn.getX(), spawn.getY() + y, spawn.getZ());
                loc.setX(loc.getX() + 2);
                b = loc.getBlock();
                b.setType(mat);

                loc.setZ(loc.getZ() + 1);
                b = loc.getBlock();
                b.setType(mat);

                loc = new Location(spawn.getWorld(), spawn.getX(), spawn.getY() + y, spawn.getZ());
                loc.setZ(loc.getZ() + 2);
                b = loc.getBlock();
                b.setType(mat);

                loc.setX(loc.getX() + 1);
                b = loc.getBlock();
                b.setType(mat);
            }
            counter++;
        }
    }
}
