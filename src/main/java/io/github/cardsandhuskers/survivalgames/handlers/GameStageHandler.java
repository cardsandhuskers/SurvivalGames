package io.github.cardsandhuskers.survivalgames.handlers;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import io.github.cardsandhuskers.survivalgames.objects.Border;
import io.github.cardsandhuskers.survivalgames.objects.Chests;
import io.github.cardsandhuskers.survivalgames.objects.Countdown;
import io.github.cardsandhuskers.survivalgames.objects.PlayerTracker;
import io.github.cardsandhuskers.teams.objects.Team;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.*;
import ru.xezard.glow.data.glow.Glow;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.*;

public class GameStageHandler {
    private final SurvivalGames plugin;
    private final Chests chests;
    private final Border worldBorder;
    private Countdown gameTimer, restockTimer, deathmatchTimer, preDeathmatch;
    private final AttackerTimersHandler attackerTimersHandler;
    private boolean deathMatchStarted = false;
    ArrayList<Team> teamList;
    ArrayList<PlayerTracker> trackerList;
    public GameStageHandler(SurvivalGames plugin, Chests chests, Border worldBorder, ArrayList<Team> teamList, AttackerTimersHandler attackerTimersHandler, ArrayList<PlayerTracker> trackerList) {
        this.trackerList = trackerList;
        this.plugin = plugin;
        this.chests = chests;
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
                Inventory inv = p.getInventory();
                inv.clear();
                for(PotionEffect potionEffect: p.getActivePotionEffects()) {
                    p.removePotionEffect(potionEffect.getType());
                }
                p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 60,30));
                if(gameType == GameType.SKYWARS) {
                    inv.setItem(0, new ItemStack(Material.SHEARS));
                    inv.setItem(1, new ItemStack(handler.getPlayerTeam(p).getWoolColor(), 64));
                }
            }
        }

        World world = plugin.getConfig().getLocation(gameType + ".spawnPoint").getWorld();
        world.setGameRule(GameRule.KEEP_INVENTORY, true);
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);


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

        if(scoreboard.getObjective("health") != null) {
            scoreboard.getObjective("health").unregister();
        }

        Objective objective = scoreboard.registerNewObjective("health", Criterias.HEALTH, ChatColor.DARK_RED + "❤");
        objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        for(Player p:Bukkit.getOnlinePlayers()) {
            p.setScoreboard(scoreboard);
            objective.getScore(p.getDisplayName()).setScore(20);
        }

        setGlow();
    }

    public void endGame() {
        if(gameTimer != null) {
            gameTimer.cancelTimer();
        }
        if(restockTimer != null) {
            restockTimer.cancelTimer();
        }
        if(deathmatchTimer != null) {
            deathmatchTimer.cancelTimer();
        }
        if(preDeathmatch != null) {
            preDeathmatch.cancelTimer();
        }
        if(gameType == GameType.SURVIVAL_GAMES) {
            worldBorder.shrinkWorldBorder(50, 1);
        }
        if(gameType == GameType.SKYWARS) {
            worldBorder.buildWorldBorder(0,0);
        }
        attackerTimersHandler.cancelOperation();
    }

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
                    if(gameType == GameType.SKYWARS) {
                        Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "OVERTIME! BORDER WILL SHRINK RAPIDLY");
                        for(Player p:Bukkit.getOnlinePlayers()) {
                            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT, 1F, .5F);
                        }
                        worldBorder.shrinkWorldBorder(2,60);
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
        Countdown timer = new Countdown(plugin,
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
        timer.scheduleTimer();
    }

    /**
     * Timer until restock, part of the main gameTimer
     */
    private void restockTimer() {
        restockTimer = new Countdown(plugin,
                //should be 420 (7mins)
                plugin.getConfig().getInt(gameType + ".RestockTime"),
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
        if(deathMatchStarted) return;
        deathMatchStarted = true;
        int totalSeconds = plugin.getConfig().getInt(gameType + ".PreDeathmatchTime");
        preDeathmatch = new Countdown(plugin,

                totalSeconds,
                //Timer Start
                () -> {
                    Bukkit.broadcastMessage(ChatColor.RED + "Deathmatch Starts in " + ChatColor.YELLOW + "20" + ChatColor.RED + "seconds!");
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
        Countdown timer = new Countdown(plugin,

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
        timer.scheduleTimer();
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

    public void setGlow() {

        for(Player p:Bukkit.getOnlinePlayers()) {
            if(handler.getPlayerTeam(p) == null) continue;

            Glow glow = Glow.builder()
                    .color(handler.getPlayerTeam(p).getChatColor())
                    .name(p.getDisplayName())
                    .build();

            glow.addHolders(p);
            for(Player player:handler.getPlayerTeam(p).getOnlinePlayers()) {
                if(!p.equals(player)) glow.display(player);
            }
        }
    }


    /**
     * updates the glass boxes around the spawn points
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
            //spawn is on northwest corner (-x -y corner)
            //x-1 {z+1}, z-1{x+1}
            if (gameType == GameType.SURVIVAL_GAMES) {
                //loop 3 times to handle the y
                for (int y = 0; y <= 2; y++) {
                    Location loc = new Location(spawn.getWorld(), spawn.getX(), spawn.getY() + y, spawn.getZ());
                    loc.setX(loc.getX() - 2);
                    Block b = loc.getBlock();
                    b.setType(mat);

                    loc.setZ(loc.getZ() - 1);
                    b = loc.getBlock();
                    b.setType(mat);

                    loc.setZ(loc.getZ() + 2);
                    b = loc.getBlock();
                    b.setType(mat);

                    loc = new Location(spawn.getWorld(), spawn.getX(), spawn.getY() + y, spawn.getZ());
                    loc.setX(loc.getX() + 2);
                    b = loc.getBlock();
                    b.setType(mat);

                    loc.setZ(loc.getZ() - 1);
                    b = loc.getBlock();
                    b.setType(mat);

                    loc.setZ(loc.getZ() + 2);
                    b = loc.getBlock();
                    b.setType(mat);


                    loc = new Location(spawn.getWorld(), spawn.getX(), spawn.getY() + y, spawn.getZ());
                    loc.setZ(loc.getZ() + 2);
                    b = loc.getBlock();
                    b.setType(mat);

                    loc.setX(loc.getX() - 1);
                    b = loc.getBlock();
                    b.setType(mat);

                    loc.setX(loc.getX() + 2);
                    b = loc.getBlock();
                    b.setType(mat);

                    loc = new Location(spawn.getWorld(), spawn.getX(), spawn.getY() + y, spawn.getZ());
                    loc.setZ(loc.getZ() - 2);
                    b = loc.getBlock();
                    b.setType(mat);

                    loc.setX(loc.getX() - 1);
                    b = loc.getBlock();
                    b.setType(mat);

                    loc.setX(loc.getX() + 2);
                    b = loc.getBlock();
                    b.setType(mat);

                }
            }
            if(gameType == GameType.SKYWARS) {
                Location baseBlock = new Location(spawn.getWorld(), spawn.getX(), spawn.getY() - 1, spawn.getZ());
                Block b = baseBlock.getBlock();
                b.setType(mat);


                for(int y = 0; y <= 2; y++) {
                    Location loc = new Location(spawn.getWorld(), spawn.getX(),spawn.getY(),spawn.getZ());
                    loc.setY(loc.getY() + y);
                    loc.setX(loc.getX() - 1);
                    b = loc.getBlock();
                    b.setType(mat);
                    loc.setX(spawn.getX());

                    loc.setX(loc.getX() + 1);
                    b = loc.getBlock();
                    b.setType(mat);
                    loc.setX(spawn.getX());


                    loc.setZ(loc.getZ() - 1);
                    b = loc.getBlock();
                    b.setType(mat);
                    loc.setZ(spawn.getZ());

                    loc.setZ(loc.getZ() + 1);
                    b = loc.getBlock();
                    b.setType(mat);
                    loc.setZ(spawn.getZ());

                }
            }
            counter++;
        }
    }
}
