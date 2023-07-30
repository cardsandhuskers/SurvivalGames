package io.github.cardsandhuskers.survivalgames.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PearlThrowListener implements Listener {

    @EventHandler
    public void onPearlLand(PlayerTeleportEvent e) {
        if(e.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) return;
        e.setCancelled(true);
        e.getPlayer().setNoDamageTicks(1);
        e.getPlayer().teleport(e.getTo());
    }
}
