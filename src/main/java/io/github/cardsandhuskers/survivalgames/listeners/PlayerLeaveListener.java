package io.github.cardsandhuskers.survivalgames.listeners;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import io.github.cardsandhuskers.survivalgames.handlers.PlayerDeathHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.gameState;
import static io.github.cardsandhuskers.survivalgames.handlers.PlayerDeathHandler.numPlayers;

public class PlayerLeaveListener implements Listener {
    private PlayerDamageListener playerDamageListener;

    public PlayerLeaveListener(PlayerDamageListener playerDamageListener) {
        this.playerDamageListener = playerDamageListener;
    }
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if(gameState != SurvivalGames.State.GAME_OVER) {
            playerDamageListener.onPlayerDeath(p);
        }
        //numPlayers--;
    }
}
