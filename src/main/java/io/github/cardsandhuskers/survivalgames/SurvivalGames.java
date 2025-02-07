package io.github.cardsandhuskers.survivalgames;

import io.github.cardsandhuskers.survivalgames.commands.*;
import io.github.cardsandhuskers.survivalgames.listeners.InventoryClickListener;
import io.github.cardsandhuskers.survivalgames.objects.Placeholder;
import io.github.cardsandhuskers.survivalgames.objects.stats.StatCalculator;
import io.github.cardsandhuskers.teams.Teams;
import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public final class SurvivalGames extends JavaPlugin {
    public static State gameState = State.GAME_STARTING;
    public static TeamHandler handler;
    public static int timeVar = 0;
    public static int altTimeVar = 0;
    public static double multiplier = 1;
    public static int totalPlayers;
    public static int gameNumber = 1;
    public static HashMap<Player, Integer> playerKills;
    public static GameType gameType = GameType.SURVIVAL_GAMES;
    public StatCalculator statCalculator;
    @Override
    public void onEnable() {
        // Plugin startup logic
        statCalculator = new StatCalculator(this);
        try {
            statCalculator.calculateStats();
        } catch (Exception e) {
            StackTraceElement[] trace = e.getStackTrace();
            String str = "";
            for(StackTraceElement element:trace) str += element.toString() + "\n";
            this.getLogger().severe("ERROR Calculating Stats!\n" + str);
        }

        //Placeholder API validation
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            /*
             * We register the EventListener here, when PlaceholderAPI is installed.
             * Since all events are in the main class (this class), we simply use "this"
             */
            new Placeholder(this).register();

        } else {
            /*
             * We inform about the fact that PlaceholderAPI isn't installed and then
             * disable this plugin to prevent issues.
             */
            System.out.println("Could not find PlaceholderAPI!");
            //Bukkit.getPluginManager().disablePlugin(this);
        }

        handler = Teams.handler;
        playerKills = new HashMap<>();

        /*
         * Register Commands
         * -----------------
         */
        getCommand("setSGPos1").setExecutor(new SetPos1Command(this));
        getCommand("setSGPos2").setExecutor(new SetPos2Command(this));
        getCommand("setSGCenter").setExecutor(new SetCenterCommand(this));
        getCommand("saveSGArena").setExecutor(new SaveArenaCommand(this));
        getCommand("setSGSpawnPoint").setExecutor(new SetSpawnPointCommand(this));
        getCommand("setSGSpawnBox").setExecutor(new SetTeamSpawnCommand(this));
        StartGameCommand startGameCommand = new StartGameCommand(this);
        getCommand("startSurvivalGames").setExecutor(startGameCommand);
        getCommand("setLobby").setExecutor(new SetLobbyCommand(this));
        getCommand("reloadSGArena").setExecutor(new ResetArenaCommand(this));
        getCommand("pauseSGTimer").setExecutor(new PauseTimerCommand(this, startGameCommand));
        getCommand("cancelSG").setExecutor(new CancelGameCommand(this, startGameCommand));
        getCommand("reloadSG").setExecutor(new ReloadConfigCommand(this));

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        //This is only here for testing purposes
        //getServer().getPluginManager().registerEvents(new InventoryClickListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    public enum State {
        GAME_STARTING,
        GRACE_PERIOD,
        GAME_IN_PROGRESS,
        DEATHMATCH,
        GAME_OVER
    }
    public enum GameType {
        SURVIVAL_GAMES,
        SKYWARS
    }
}


