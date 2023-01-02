package io.github.cardsandhuskers.survivalgames.listeners;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import io.github.cardsandhuskers.survivalgames.handlers.PlayerDeathHandler;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
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
    private SurvivalGames plugin = (SurvivalGames) Bukkit.getPluginManager().getPlugin("SurvivalGames");
    public PlayerDamageListener(PlayerPointsAPI ppAPI, PlayerDeathHandler playerDeathHandler, HashMap<Player, Player> storedAttackers) {
        this.storedAttackers = storedAttackers;
        this.ppAPI = ppAPI;
        this.playerDeathHandler = playerDeathHandler;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        if(e.getEntity() instanceof Player p) {
            EntityDamageEvent.DamageCause cause =  e.getCause();
            if(cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                if(p.getHealth() - e.getDamage() <= 0 && p.getGameMode() != GameMode.SPECTATOR) {
                    e.setCancelled(true);
                    onPlayerDeath(p);
                }
            }
        }
    }
    public void onPlayerDeath(Player p) {
        if(storedAttackers.get(p) != null) {
            Player attacker = storedAttackers.get(p);
            ppAPI.give(attacker.getUniqueId(), (int)(plugin.getConfig().getInt(gameType + ".killPoints") * multiplier));
            handler.getPlayerTeam(attacker).addTempPoints(attacker, (int)(plugin.getConfig().getInt(gameType + ".killPoints") * multiplier));

            if(playerKills.get(attacker) != null) {
                playerKills.put(attacker, playerKills.get(attacker) + 1);
            } else {
                playerKills.put(attacker, 1);
            }

            for(Player player: handler.getPlayerTeam(attacker).getOnlinePlayers()) {
                if(player.equals(attacker)) {
                    player.sendMessage("[+" + plugin.getConfig().getInt(gameType + ".killPoints") * multiplier + " points] " + handler.getPlayerTeam(p).color + p.getName() + ChatColor.RESET + " was killed by " + handler.getPlayerTeam(attacker).color + attacker.getName());
                } else {
                    //player.sendMessage(handler.getPlayerTeam(p).color + p.getName() + ChatColor.RESET + " was killed by " + handler.getPlayerTeam(attacker).color + attacker.getName());
                }
            }
            for(Player player: Bukkit.getOnlinePlayers()) {
                if(!p.equals(attacker)) {
                    player.sendMessage(handler.getPlayerTeam(p).color + p.getName() + ChatColor.RESET + " was killed by " + handler.getPlayerTeam(attacker).color + attacker.getName() + ChatColor.RESET + "[" + ChatColor.YELLOW + (int)(plugin.getConfig().getInt(gameType + ".survivalPoints") * multiplier) + ChatColor.RESET + "] Points");
                }
            }
            attacker.playSound(attacker.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1f, 2f);
            attacker.sendTitle("Killed " + handler.getPlayerTeam(p).color + p.getName(), "", 2, 16, 2);

        } else {
            for(Player player:Bukkit.getOnlinePlayers()) {
                if(!player.equals(p)) {
                    player.sendMessage(handler.getPlayerTeam(p).color + p.getName() + ChatColor.RESET + " died [+" + ChatColor.YELLOW + (int)(plugin.getConfig().getInt(gameType + ".survivalPoints") * multiplier) + ChatColor.RESET + "] Points");
                } else {
                    p.sendMessage("You died.");
                    p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT,1,2);
                }
            }
        }
        //p.sendMessage("Dead");

        playerDeathHandler.onPlayerDeath(p);
    }
}
