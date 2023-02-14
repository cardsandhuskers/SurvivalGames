package io.github.cardsandhuskers.survivalgames.listeners;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {
    private final PlayerDamageListener playerDamageListener;
    public PlayerMoveListener(PlayerDamageListener playerDamageListener) {
        this.playerDamageListener = playerDamageListener;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if(e.getPlayer().getGameMode() == GameMode.SPECTATOR && e.getPlayer().getLocation().getY() <= 0) {
            Location l = e.getPlayer().getLocation();
            l.setY(50);
            e.getPlayer().teleport(l);
        }

        if(SurvivalGames.gameType == SurvivalGames.GameType.SKYWARS) {
            if(e.getPlayer().getLocation().getY() <= 0) {
                if(e.getPlayer().getGameMode() != GameMode.SURVIVAL) return;
                playerDamageListener.onPlayerDeath(e.getPlayer());
                Player p = e.getPlayer();
                Location l = p.getLocation();
                l.setY(80);
                p.teleport(l);
            }
        }
    }
}
