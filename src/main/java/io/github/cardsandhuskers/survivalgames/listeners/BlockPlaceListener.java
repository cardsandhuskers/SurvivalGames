package io.github.cardsandhuskers.survivalgames.listeners;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener implements Listener {
    SurvivalGames plugin;
    public BlockPlaceListener(SurvivalGames plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Material mat = e.getBlock().getType();
        if(mat == Material.FIRE || mat == Material.COBWEB || mat == Material.CAKE) {
            if(mat == Material.FIRE) {
                Block b = e.getBlock();

                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()-> {
                    b.setType(Material.AIR);
                }, 600L);

            }
        } else {
            e.setCancelled(true);
        }
    }


}
