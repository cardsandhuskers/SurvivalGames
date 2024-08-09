package io.github.cardsandhuskers.survivalgames.handlers;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import io.github.cardsandhuskers.survivalgames.listeners.GlowPacketListener;
import io.github.cardsandhuskers.survivalgames.objects.border.Border;
import io.github.cardsandhuskers.survivalgames.objects.Chests;
import io.github.cardsandhuskers.survivalgames.objects.Countdown;
import io.github.cardsandhuskers.survivalgames.objects.PlayerTracker;
import io.github.cardsandhuskers.teams.objects.Team;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.*;

public class GameStageHandler {
    private final SurvivalGames plugin;
    private final Chests chests;
    private final Border worldBorder;
    private Countdown gameTimer, restockTimer, deathmatchTimer, preDeathmatch, deathmatchPrepTimer, gracePeriodTimer;
    private final AttackerTimersHandler attackerTimersHandler;
    private boolean deathMatchStarted = false;
    ArrayList<Team> teamList;
    ArrayList<PlayerTracker> trackerList;
    public GlowPacketListener glowPacketListener;

    public GameStageHandler(SurvivalGames plugin, Chests chests, Border worldBorder, ArrayList<Team> teamList, AttackerTimersHandler attackerTimersHandler, ArrayList<PlayerTracker> trackerList) {
        this.trackerList = trackerList;
        this.plugin = plugin;
        this.chests = chests;
        this.worldBorder = worldBorder;
        this.teamList = teamList;
        this.attackerTimersHandler = attackerTimersHandler;
    }

    /**
     * Starts the actual game (releases players from boxes)
     */
    public void startGame() {
        for(Team t: handler.getTeams()) {
            for (Player p : t.getOnlinePlayers()) {
                p.setGameMode(GameMode.SURVIVAL);
                p.setHealth(20);
                p.setSaturation(20);
                p.setFoodLevel(20);
                Inventory inv = p.getInventory();
                inv.clear();
                for (PotionEffect potionEffect : p.getActivePotionEffects()) {
                    p.removePotionEffect(potionEffect.getType());
                }
                p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 60, 30));
                if (gameType == GameType.SKYWARS) {
                    inv.setItem(0, new ItemStack(Material.SHEARS));
                    inv.setItem(1, new ItemStack(handler.getPlayerTeam(p).getWoolColor(), 64));
                }
            }
        }

        World world = plugin.getConfig().getLocation(gameType + ".spawnPoint").getWorld();
        world.setGameRule(GameRule.KEEP_INVENTORY, true);
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);

        for(Player p:Bukkit.getOnlinePlayers()) {
            if(handler.getPlayerTeam(p) == null) {
                p.setGameMode(GameMode.SPECTATOR);
            }
        }
        try {
            updateGlass(Material.AIR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        gameTimer();
        restockTimer();
        if(gameType == GameType.SURVIVAL_GAMES) {
            gracePeriodTimer();
        }


        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = manager.getMainScoreboard();

        if(scoreboard.getObjective("belowNameHP") != null) scoreboard.getObjective("belowNameHP").unregister();
        if(scoreboard.getObjective("listHP") != null) scoreboard.getObjective("listHP").unregister();

        Objective belowNameHP = scoreboard.registerNewObjective("belowNameHP", Criteria.HEALTH, ChatColor.DARK_RED + "❤");
        Objective listHP = scoreboard.registerNewObjective("listHP", Criteria.HEALTH, ChatColor.YELLOW + "");
        belowNameHP.setDisplaySlot(DisplaySlot.BELOW_NAME);
        listHP.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        for(Player p:Bukkit.getOnlinePlayers()) {
            p.setScoreboard(scoreboard);
            belowNameHP.getScore(p.getDisplayName()).setScore(20);
            listHP.getScore(p.getDisplayName()).setScore(20);
        }

        if(plugin.getConfig().getBoolean("enableGlow")) {
            //initialize glowing
            glowPacketListener = new GlowPacketListener(plugin);
            glowPacketListener.startOperation();
        }
    }

    /**
     * Main game timer, in sg deathmatch is triggered when this ends
     */
    private void gameTimer() {
        //should be 720 seconds
        int totalSeconds = plugin.getConfig().getInt(gameType + ".GameTime");
        gameTimer = new Countdown(plugin,

                totalSeconds,
                //Timer Start
                () -> {
                    altTimeVar = totalSeconds;
                    gameState = State.GAME_IN_PROGRESS;
                    if(gameType == GameType.SURVIVAL_GAMES) {
                        worldBorder.shrinkWorldBorder(90, totalSeconds);
                        gameState = State.GRACE_PERIOD;
                    }

                },

                //Timer End
                () -> {
                //start deathmatch
                    altTimeVar = 0;
                    if(gameType == GameType.SURVIVAL_GAMES) {
                        startDeathmatch();
                    }
                },

                //Each Second
                (t) -> {
                    //+20 equal to startDeathmatch() timer quantity
                    if(gameType == GameType.SURVIVAL_GAMES) {
                        SurvivalGames.altTimeVar = t.getSecondsLeft() + 20;
                    }
                    if(gameType == GameType.SKYWARS) {
                        altTimeVar = t.getSecondsLeft();
                    }
                    for(PlayerTracker tracker:trackerList) {
                        tracker.updateLocation();
                    }
                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        gameTimer.scheduleTimer();
    }

    /**
     * Time before PVP is enabled, part of the main gameTimer
     */
    private void gracePeriodTimer() {
        gracePeriodTimer = new Countdown(plugin,
                //should be 45
                plugin.getConfig().getInt(gameType + ".GracePeriod"),
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
        gracePeriodTimer.scheduleTimer();
    }

    /**
     * Timer until restock, part of the main gameTimer
     */
    private void restockTimer() {
        int restockTime = plugin.getConfig().getInt(gameType + ".RestockTime");
        restockTimer = new Countdown(plugin,
                //should be 420 (7mins)
                restockTime,
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

                    //triggers shrink in skywars
                    if(gameType == GameType.SKYWARS) {
                        Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "BORDER WILL BEGIN SHRINKING");

                        worldBorder.shrinkWorldBorder(0, gameTimer.getSecondsLeft());
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
        if(deathMatchStarted) return;
        deathMatchStarted = true;
        int totalSeconds = plugin.getConfig().getInt(gameType + ".PreDeathmatchTime");
        preDeathmatch = new Countdown(plugin,

                totalSeconds,
                //Timer Start
                () -> {
                    Bukkit.broadcastMessage(ChatColor.RED + "Deathmatch Starts in " + ChatColor.YELLOW + totalSeconds + ChatColor.RED + " seconds!");
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
                    if(t.getSecondsLeft() == 10 || t.getSecondsLeft() < 5) {
                        Bukkit.broadcastMessage(ChatColor.RED + "Deathmatch Starts in " + ChatColor.YELLOW + t.getSecondsLeft() + ChatColor.RED + " seconds!");
                        for(Player p:Bukkit.getOnlinePlayers()) {
                            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1F);
                        }
                    }
                    for(PlayerTracker tracker:trackerList) {
                        tracker.updateLocation();
                    }
                }
        );
        if(gameState != State.DEATHMATCH) {
            gameTimer.cancelTimer();
            restockTimer.cancelTimer();
            preDeathmatch.scheduleTimer();
            timeVar = 0;
        }

    }

    /**
     * Deathmatch prep timer, runs after the conclusion of the game timer
     */
    private void deathmatchPrepTimer() {
//should be 15 seconds
        int totalSeconds = plugin.getConfig().getInt(gameType + ".DeathmatchPrepTime");
        deathmatchPrepTimer = new Countdown(plugin,

                totalSeconds,
                //Timer Start
                () -> {
                    altTimeVar = totalSeconds;
                    gameState = State.DEATHMATCH;
                    if(gameType == GameType.SURVIVAL_GAMES) {
                        worldBorder.shrinkWorldBorder(50, 1);
                    }
                    try {
                        updateGlass(Material.GLASS);
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
        deathmatchPrepTimer.scheduleTimer();
    }

    /**
     * Deathmatch timer, runs after the conclusion of the deathmatch prep timer
     */
    private void deathmatchTimer() {
        //should be 120 seconds
        int totalSeconds = plugin.getConfig().getInt(gameType + ".DeathmatchTime");
        deathmatchTimer = new Countdown(plugin,
                totalSeconds,
                //Timer Start
                () -> {
                    timeVar = totalSeconds;
                    if(gameType == GameType.SURVIVAL_GAMES) {
                        worldBorder.shrinkWorldBorder(2, totalSeconds);
                    }
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
     * Called when the last player is killed/game is cancelled
     */
    public void endGame() {
        if(gracePeriodTimer != null) gracePeriodTimer.cancelTimer();
        if(gameTimer != null) gameTimer.cancelTimer();
        if(restockTimer != null) restockTimer.cancelTimer();
        if(deathmatchTimer != null) deathmatchTimer.cancelTimer();
        if(preDeathmatch != null) preDeathmatch.cancelTimer();
        if(deathmatchPrepTimer != null) deathmatchPrepTimer.cancelTimer();

        if(gameType == GameType.SURVIVAL_GAMES) worldBorder.shrinkWorldBorder(50, 1);
        if(gameType == GameType.SKYWARS) {
            worldBorder.cancelOperation();
        }

        attackerTimersHandler.cancelOperation();

        if(plugin.getConfig().getBoolean("enableGlow")) {
            glowPacketListener.disableGlow();
            glowPacketListener.cancelOperation();
        }

    }

    /**
     * Teleports all players to their spawn points
     * @throws IOException
     */
    public void teleportPlayers() throws IOException {
        File arenaFile = new File(Bukkit.getServer().getPluginManager().getPlugin("SurvivalGames").getDataFolder(),gameType + ".yml");
        if(!arenaFile.exists()) {
            //if the file does not exist, crash program, since game cannot run without it
            throw new IOException("FILE CANNOT BE FOUND");
        }
        FileConfiguration arenaFileConfig = YamlConfiguration.loadConfiguration(arenaFile);

        Queue<Integer> spots = new LinkedList<>();
        spots.add(1);
        spots.add(7);
        spots.add(4);
        spots.add(10);
        spots.add(11);
        spots.add(5);
        spots.add(8);
        spots.add(2);
        spots.add(9);
        spots.add(3);
        spots.add(12);
        spots.add(6);

        ArrayList<Team> randomTeamList = (ArrayList<Team>) teamList.clone();
        Collections.shuffle(randomTeamList);


        int testCtr = 1;
        while(arenaFileConfig.get("teamSpawn." + testCtr) != null) {
            testCtr++;
        }
        if(testCtr < 12) {
            for(Integer i:spots) {
                if(i > testCtr) spots.remove(i);
            }
        }

        int counter = spots.remove();
        int index = 0;

        while(arenaFileConfig.get("teamSpawn." + counter) != null && index < teamList.size()) {
            Location spawn = arenaFileConfig.getLocation("teamSpawn." + counter);
            for(Player p: randomTeamList.get(index).getOnlinePlayers()) {
                p.teleport(spawn);
            }
            index++;
            counter = spots.remove();
        }
    }

    /**
     * updates the glass boxes around the spawn points
     * This needs to be better written (more compact)
     * @param mat
     * @throws IOException
     */
    public void updateGlass(Material mat) throws IOException {
        File arenaFile = new File(Bukkit.getServer().getPluginManager().getPlugin("SurvivalGames").getDataFolder(),gameType + ".yml");
        if(!arenaFile.exists()) {
            //if the file does not exist, crash program, since game cannot run without it
            throw new IOException("FILE CANNOT BE FOUND");
        }
        FileConfiguration arenaFileConfig = YamlConfiguration.loadConfiguration(arenaFile);
        int counter = 1;
        while(arenaFileConfig.get("teamSpawn." + counter) != null) {
            Location spawn = arenaFileConfig.getLocation("teamSpawn." + counter);
            int[] listX = {0,0,0,1,2,3,4,4,4,1,2,3};
            int[] listZ = {1,2,3,0,0,0,1,2,3,4,4,4};


            for(int i = 0; i < listX.length; i++) {
                for (int y = 0; y <= 2; y++) {
                    Location loc = new Location(spawn.getWorld(), spawn.getX() + listX[i] - 2, spawn.getY() + y, spawn.getZ() + listZ[i] - 2);
                    loc.getBlock().setType(mat);
                }
            }

            if(gameType == GameType.SKYWARS) {
                for(int x = -1; x <= 1; x++) {
                    for(int z = -1; z <= 1; z++) {
                        Location loc = new Location(spawn.getWorld(), spawn.getX() + x, spawn.getY() - 1, spawn.getZ() + z);
                        loc.getBlock().setType(mat);
                    }
                }
            }

            counter++;
        }
    }
}
