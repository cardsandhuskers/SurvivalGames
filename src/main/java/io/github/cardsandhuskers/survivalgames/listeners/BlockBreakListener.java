package io.github.cardsandhuskers.survivalgames.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {



    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Material mat = e.getBlock().getType();
        if(mat != null && mat != Material.COBWEB && mat != Material.FIRE && mat != Material.CAKE) {
            e.setCancelled(true);
        }
    }
}
