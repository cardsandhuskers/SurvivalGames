package io.github.cardsandhuskers.survivalgames.listeners;

import io.github.cardsandhuskers.survivalgames.handlers.PlayerDeathHandler;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashMap;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.*;
import static io.github.cardsandhuskers.survivalgames.SurvivalGames.playerKills;

public class PlayerDamageListener implements Listener {
    //Designed to listen for environment damage
    PlayerPointsAPI ppAPI;
    PlayerDeathHandler playerDeathHandler;
    HashMap<Player, Player> storedAttackers;
    public PlayerDamageListener(PlayerPointsAPI ppAPI, PlayerDeathHandler playerDeathHandler, HashMap<Player, Player> storedAttackers) {
        this.storedAttackers = storedAttackers;
        this.ppAPI = ppAPI;
        this.playerDeathHandler = playerDeathHandler;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        if(e.getEntity() instanceof Player p) {
            EntityDamageEvent.DamageCause cause =  e.getCause();
            if(cause == EntityDamageEvent.DamageCause.FIRE || cause == EntityDamageEvent.DamageCause.FIRE_TICK || cause == EntityDamageEvent.DamageCause.LAVA || cause == EntityDamageEvent.DamageCause.FALL || cause == EntityDamageEvent.DamageCause.VOID || cause == EntityDamageEvent.DamageCause.SUFFOCATION) {
                if(p.getHealth() - e.getDamage() <= 0) {
                        if(storedAttackers.get(p) != null) {
                            Player attacker = storedAttackers.get(p);
                            ppAPI.give(attacker.getUniqueId(), (int)(50 * multiplier));
                            handler.getPlayerTeam(attacker).addTempPoints(attacker, (int)(50 * multiplier));

                            if(playerKills.get(attacker) != null) {
                                playerKills.put(attacker, playerKills.get(attacker) + 1);
                            } else {
                                playerKills.put(attacker, 1);
                            }
                            
                            for(Player player: handler.getPlayerTeam(attacker).getOnlinePlayers()) {
                                if(player.equals(attacker)) {
                                    player.sendMessage("[+" + 50 * multiplier + " points] " + handler.getPlayerTeam(p).color + p.getName() + ChatColor.RESET + " was killed by " + handler.getPlayerTeam(attacker).color + attacker.getName());
                                } else {
                                    //player.sendMessage(handler.getPlayerTeam(p).color + p.getName() + ChatColor.RESET + " was killed by " + handler.getPlayerTeam(attacker).color + attacker.getName());
                                }
                            }
                            for(Player player: Bukkit.getOnlinePlayers()) {
                                if(!p.equals(attacker)) {
                                    player.sendMessage(handler.getPlayerTeam(p).color + p.getName() + ChatColor.RESET + " was killed by " + handler.getPlayerTeam(attacker).color + attacker.getName());
                                }
                            }
                            attacker.playSound(attacker.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1f, 2f);
                            attacker.sendTitle("Killed " + handler.getPlayerTeam(p).color + p.getName(), "", 2, 16, 2);
                            
                        } else {
                            Bukkit.broadcastMessage(handler.getPlayerTeam(p).color + p.getName() + ChatColor.RESET + " died");
                        }
                    //p.sendMessage("Dead");
                    e.setCancelled(true);
                    playerDeathHandler.onPlayerDeath(p);
                }
            }
        }
    }
}
