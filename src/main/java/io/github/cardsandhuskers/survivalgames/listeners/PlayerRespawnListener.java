package io.github.cardsandhuskers.survivalgames.listeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.gameType;

public class PlayerRespawnListener implements Listener {

    //update to UUID???
    HashMap<Player, Location> playerLocationMap;
    Plugin plugin;
    public PlayerRespawnListener(Plugin plugin, HashMap playerLocationMap) {
        this.playerLocationMap = playerLocationMap;
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {

        Player p = e.getPlayer();
        if(playerLocationMap.containsKey(p)) {
            e.setRespawnLocation(playerLocationMap.get(p));
        } else {
            e.setRespawnLocation(plugin.getConfig().getLocation(gameType + ".spawnPoint"));
        }

        p.setGameMode(GameMode.SURVIVAL);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->p.setGameMode(GameMode.SPECTATOR), 1);

    }
}
