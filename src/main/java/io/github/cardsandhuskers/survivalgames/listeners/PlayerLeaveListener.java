package io.github.cardsandhuskers.survivalgames.listeners;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.gameState;

public class PlayerLeaveListener implements Listener {
    private final PlayerDeathListener playerDeathListener;

    public PlayerLeaveListener(PlayerDeathListener playerDeathListener) {
        this.playerDeathListener = playerDeathListener;
    }
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if(TeamHandler.getInstance().getPlayerTeam(p) == null) return;

        if(gameState != SurvivalGames.State.GAME_OVER && gameState != SurvivalGames.State.GAME_STARTING) {
            playerDeathListener.onOtherDeath(p);
        }
        //numPlayers--;
    }
}
