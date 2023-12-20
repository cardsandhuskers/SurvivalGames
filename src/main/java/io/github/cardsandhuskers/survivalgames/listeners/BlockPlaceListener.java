package io.github.cardsandhuskers.survivalgames.listeners;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.gameState;
import static io.github.cardsandhuskers.survivalgames.SurvivalGames.gameType;

public class BlockPlaceListener implements Listener {
    SurvivalGames plugin;
    public BlockPlaceListener(SurvivalGames plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Material mat = e.getBlock().getType();
        if(gameType == SurvivalGames.GameType.SURVIVAL_GAMES) {
            if (mat == Material.FIRE || mat == Material.COBWEB || mat == Material.CAKE) {
                if (mat == Material.FIRE) {
                    Block b = e.getBlock();

                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                        b.setType(Material.AIR);
                    }, 600L);

                }
            } else {
                e.setCancelled(true);
            }
        }
        if(gameType == SurvivalGames.GameType.SKYWARS) {
            if(e.getBlock().getY() >= 95) {
                e.setCancelled(true);
            }
            if(gameState != SurvivalGames.State.GAME_IN_PROGRESS) {
                e.setCancelled(true);
            }
        }
    }


}
