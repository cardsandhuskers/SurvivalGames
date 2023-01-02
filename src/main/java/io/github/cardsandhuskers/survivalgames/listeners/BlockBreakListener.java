package io.github.cardsandhuskers.survivalgames.listeners;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.gameState;
import static io.github.cardsandhuskers.survivalgames.SurvivalGames.gameType;

public class BlockBreakListener implements Listener {



    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Material mat = e.getBlock().getType();
        //only SG has rules against breaking blocks
        if(SurvivalGames.gameType == SurvivalGames.GameType.SURVIVAL_GAMES) {
            if (mat != null && mat != Material.COBWEB && mat != Material.FIRE && mat != Material.CAKE) {
                e.setCancelled(true);
            }
        }
        if(gameType == SurvivalGames.GameType.SKYWARS) {
            if(e.getBlock().getY() >= 120) {
                e.setCancelled(true);
            }
            if(gameState != SurvivalGames.State.GAME_IN_PROGRESS) {
                e.setCancelled(true);
            }
        }
    }
}
