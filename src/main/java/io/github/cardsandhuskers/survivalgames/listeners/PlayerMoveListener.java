package io.github.cardsandhuskers.survivalgames.listeners;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {
    private final PlayerDeathListener playerDeathListener;
    public PlayerMoveListener(PlayerDeathListener playerDeathListener) {
        this.playerDeathListener = playerDeathListener;
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
                if(e.getPlayer().getGameMode() != GameMode.SURVIVAL) {
                    Location l = e.getPlayer().getLocation();
                    l.setY(80);
                    e.getPlayer().teleport(l);
                    return;
                }
                playerDeathListener.onOtherDeath(e.getPlayer());
                Player p = e.getPlayer();
                Location l = p.getLocation();
                l.setY(80);
                p.teleport(l);
            }
        }
    }
}
