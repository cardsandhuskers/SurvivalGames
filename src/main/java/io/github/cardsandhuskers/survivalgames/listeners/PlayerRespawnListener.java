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

public class PlayerRespawnListener implements Listener {
    HashMap<Player, Location> playerLocationMap;
    Plugin plugin;
    public PlayerRespawnListener(Plugin plugin, HashMap playerLocationMap) {
        this.playerLocationMap = playerLocationMap;
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {

        Player p = e.getPlayer();
        System.out.println(playerLocationMap.get(p));
        e.setRespawnLocation(playerLocationMap.get(p));
        //p.setGameMode(GameMode.SPECTATOR);
        p.setGameMode(GameMode.SURVIVAL);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->p.setGameMode(GameMode.SPECTATOR), 1);

    }
}
