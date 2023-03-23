package io.github.cardsandhuskers.survivalgames.listeners;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import io.github.cardsandhuskers.survivalgames.handlers.PlayerDeathHandler;
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

public class PlayerDamageListener implements Listener {
    //Designed to listen for environment damage
    PlayerDeathHandler playerDeathHandler;
    HashMap<Player, Player> storedAttackers;
    private final SurvivalGames plugin = (SurvivalGames) Bukkit.getPluginManager().getPlugin("SurvivalGames");
    public PlayerDamageListener(PlayerDeathHandler playerDeathHandler, HashMap<Player, Player> storedAttackers) {
        this.storedAttackers = storedAttackers;
        this.playerDeathHandler = playerDeathHandler;
    }

    /**
     * this Listener does nothing
     * @param e
     */
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        /*
        if(e.getEntity() instanceof Player p) {
            EntityDamageEvent.DamageCause cause =  e.getCause();
            if(cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                //if(p.getHealth() - p.getLastDamage() <= 0 && p.getGameMode() != GameMode.SPECTATOR) {
                //    e.setCancelled(true);
                //    onPlayerDeath(p);
                //}
            }
        }
         */
    }

    /**
     * This player death is for a void death or disconnect death, as they're not "real" deaths
     * This should be transplanted somewhere else (probably PlayerDeathHandler), this class doesn't need to exist
     * @param p
     */
    public void onPlayerDeath(Player p) {
        if(storedAttackers.get(p) != null) {
            Player attacker = storedAttackers.get(p);
            //ppAPI.give(attacker.getUniqueId(), (int)(plugin.getConfig().getInt(gameType + ".killPoints") * multiplier));
            handler.getPlayerTeam(attacker).addTempPoints(attacker, plugin.getConfig().getInt(gameType + ".killPoints") * multiplier);

            if(playerKills.get(attacker) != null) {
                playerKills.put(attacker, playerKills.get(attacker) + 1);
            } else {
                playerKills.put(attacker, 1);
            }

            attacker.sendTitle("Killed " + handler.getPlayerTeam(p).color + p.getName(), "", 2, 16, 2);
            attacker.sendMessage("[+" + ChatColor.YELLOW + ChatColor.BOLD + plugin.getConfig().getInt(gameType + ".killPoints") * multiplier + ChatColor.RESET + "] points" + handler.getPlayerTeam(p).color + p.getName() + ChatColor.RESET + " was killed by " + handler.getPlayerTeam(attacker).color + attacker.getName());
            attacker.playSound(attacker.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1f, 2f);

            p.sendMessage("You were killed by " + handler.getPlayerTeam(attacker).color + attacker.getName() + "!");
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT, 1, 2);

            for(Player player: Bukkit.getOnlinePlayers()) {
                if(!player.equals(attacker) && !player.equals(p)) {
                    //System.out.println(player.getName() + "   " + playerDeathHandler.isPlayerAlive(player));
                    if(playerDeathHandler.isPlayerAlive(player)) {
                        player.sendMessage(handler.getPlayerTeam(p).color + p.getName() + ChatColor.RESET + " was killed by " + handler.getPlayerTeam(attacker).color + attacker.getName() + ChatColor.RESET + " [+" + ChatColor.YELLOW + ChatColor.BOLD + (int)(plugin.getConfig().getInt(gameType + ".survivalPoints") * multiplier) + ChatColor.RESET + "] Points");
                    } else {
                        player.sendMessage(handler.getPlayerTeam(p).color + p.getName() + ChatColor.RESET + " was killed by " + handler.getPlayerTeam(attacker).color + attacker.getName() + ChatColor.RESET + ".");
                    }
                }
            }

        } else {
            for(Player player:Bukkit.getOnlinePlayers()) {
                if(!player.equals(p)) {
                    //System.out.println(player.getName() + "   " + playerDeathHandler.isPlayerAlive(player));
                    if(playerDeathHandler.isPlayerAlive(player)) {
                        player.sendMessage(handler.getPlayerTeam(p).color + p.getName() + ChatColor.RESET + " died [+" + ChatColor.YELLOW + ChatColor.BOLD + (int)(plugin.getConfig().getInt(gameType + ".survivalPoints") * multiplier) + ChatColor.RESET + "] Points");
                    } else {
                        player.sendMessage(handler.getPlayerTeam(p).color + p.getName() + ChatColor.RESET + " died.");
                    }
                }
            }
            p.sendMessage(ChatColor.GRAY + "You died.");
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT,1,2);
        }
        //p.sendMessage("Dead");

        playerDeathHandler.onPlayerDeath(p);
        p.setGameMode(GameMode.SPECTATOR);
    }
}
