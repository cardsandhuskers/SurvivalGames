package io.github.cardsandhuskers.survivalgames.listeners;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import io.github.cardsandhuskers.survivalgames.handlers.PlayerDeathHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.gameState;
import static io.github.cardsandhuskers.survivalgames.handlers.PlayerDeathHandler.numPlayers;

public class PlayerLeaveListener implements Listener {
    private PlayerDeathHandler playerDeathHandler;

    public PlayerLeaveListener(PlayerDeathHandler playerDeathHandler) {
        this.playerDeathHandler = playerDeathHandler;
    }
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if(gameState != SurvivalGames.State.GAME_OVER) {
            playerDeathHandler.onPlayerDeath(p);
        }
        //numPlayers--;
    }
}
