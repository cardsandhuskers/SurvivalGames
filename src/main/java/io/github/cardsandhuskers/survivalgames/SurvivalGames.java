package io.github.cardsandhuskers.survivalgames;

import io.github.cardsandhuskers.survivalgames.commands.*;
import io.github.cardsandhuskers.survivalgames.objects.Placeholder;
import io.github.cardsandhuskers.teams.Teams;
import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public final class SurvivalGames extends JavaPlugin {
    public static State gameState = State.GAME_STARTING;
    public static TeamHandler handler;
    private PlayerPointsAPI ppAPI;
    public static int timeVar = 0;
    public static int altTimeVar = 0;
    public static double multiplier = 1;
    public static int totalPlayers;
    public static HashMap<Player, Integer> playerKills;
    public static GameType gameType = GameType.SURVIVAL_GAMES;
    @Override
    public void onEnable() {
        // Plugin startup logic
        if (Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")) {
            this.ppAPI = PlayerPoints.getInstance().getAPI();
        } else {
            System.out.println("Could not find PlayerPointsAPI! This plugin is required.");
            Bukkit.getPluginManager().disablePlugin(this);
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
            System.out.println("Could not find PlaceholderAPI! This plugin is required.");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        handler = Teams.handler;
        playerKills = new HashMap<>();

        /*
         * Register Commands
         * -----------------
         */
        getCommand("setSGPos1").setExecutor(new SetPos1Command(this));
        getCommand("setSGPos2").setExecutor(new SetPos2Command(this));
        getCommand("saveSGArena").setExecutor(new SaveArenaCommand(this));
        getCommand("setSGSpawnPoint").setExecutor(new SetSpawnPointCommand(this));
        getCommand("setSGSpawnBox").setExecutor(new SetTeamSpawn(this));
        getCommand("startSurvivalGames").setExecutor(new StartGameCommand(this, ppAPI));
        getCommand("setLobby").setExecutor(new SetLobbyCommand(this));
        getCommand("loadSGArena").setExecutor(new ResetArenaCommand(this));

        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
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


