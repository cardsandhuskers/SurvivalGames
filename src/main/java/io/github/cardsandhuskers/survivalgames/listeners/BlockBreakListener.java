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
            if (mat != null && mat != Material.COBWEB && mat != Material.FIRE && mat != Material.CAKE && !isLeaves(mat)) {
                e.setCancelled(true);
            }
        }
        if(gameType == SurvivalGames.GameType.SKYWARS) {
            if(e.getBlock().getY() >= 140) {
                e.setCancelled(true);
            }
            if(gameState != SurvivalGames.State.GAME_IN_PROGRESS) {
                e.setCancelled(true);
            }
        }
    }
    private boolean isLeaves(Material mat) {
        switch(mat) {
            case ACACIA_LEAVES:
            case AZALEA_LEAVES:
            case BIRCH_LEAVES:
            case DARK_OAK_LEAVES:
            case FLOWERING_AZALEA_LEAVES:
            case JUNGLE_LEAVES:
            case MANGROVE_LEAVES:
            case OAK_LEAVES:
            case SPRUCE_LEAVES:
                return true;
            default:
                return false;
        }
    }
}
