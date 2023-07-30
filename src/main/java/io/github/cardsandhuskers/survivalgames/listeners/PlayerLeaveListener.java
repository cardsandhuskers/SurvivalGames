package io.github.cardsandhuskers.survivalgames.listeners;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.gameState;
import static io.github.cardsandhuskers.survivalgames.SurvivalGames.handler;

public class PlayerLeaveListener implements Listener {
    private final PlayerDamageListener playerDamageListener;

    public PlayerLeaveListener(PlayerDamageListener playerDamageListener) {
        this.playerDamageListener = playerDamageListener;
    }
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if(handler.getPlayerTeam(p) == null) return;

        if(gameState != SurvivalGames.State.GAME_OVER) {
            //use playerDeathListener onOtherDeath()
            playerDamageListener.onPlayerDeath(p);
        }
        //numPlayers--;
    }
}
