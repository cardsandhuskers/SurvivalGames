package io.github.cardsandhuskers.survivalgames.listeners;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import io.github.cardsandhuskers.survivalgames.objects.border.Border;
import io.github.cardsandhuskers.survivalgames.objects.border.SkywarsCrumbleBorder;
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
    Border border;
    public BlockPlaceListener(SurvivalGames plugin, Border border) {
        this.plugin = plugin;
        this.border = border;
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
            SkywarsCrumbleBorder crumbleBorder = (SkywarsCrumbleBorder) border;

            if(gameState != SurvivalGames.State.GAME_IN_PROGRESS) {
                e.setCancelled(true);
            } else if(e.getBlock().getY() >= 95) {
                e.setCancelled(true);
            } else {
                int absX = Math.abs(border.getCenterX() - e.getBlock().getX());
                int absZ = Math.abs(border.getCenterZ() - e.getBlock().getZ());

                int dist = (int) Math.sqrt(absX * absX + absZ * absZ);

                if (dist >= SkywarsCrumbleBorder.getBorderSize()) {
                    e.setCancelled(true);
                }

            }
        }
    }


}
