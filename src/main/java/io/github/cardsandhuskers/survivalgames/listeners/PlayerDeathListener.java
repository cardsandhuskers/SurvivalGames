package io.github.cardsandhuskers.survivalgames.listeners;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import io.github.cardsandhuskers.survivalgames.handlers.PlayerDeathHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.*;

public class PlayerDeathListener implements Listener {
    HashMap<Player, Location> playerLocationMap;
    HashMap<Player, Player> storedAttackers;
    SurvivalGames plugin;
    PlayerDeathHandler playerDeathHandler;
    public PlayerDeathListener(SurvivalGames plugin, HashMap playerLocationMap, HashMap<Player, Player> storedAttackers, PlayerDeathHandler playerDeathHandler) {
        this.playerLocationMap = playerLocationMap;
        this.plugin = plugin;
        this.storedAttackers = storedAttackers;
        this.playerDeathHandler = playerDeathHandler;
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player attacked = e.getEntity();
        e.setDeathMessage("");
        playerLocationMap.put(attacked, attacked.getLocation());

        EntityDamageEvent cause = e.getEntity().getLastDamageCause();

        if(cause.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            if(e.getEntity().getKiller().getType() == EntityType.PLAYER) {
                Player attacker = e.getEntity().getKiller();
                onKillByPlayer(attacker, attacked);
            }
        } else {
            onOtherDeath(attacked);
        }
    }


    public void onKillByPlayer(Player attacker, Player attacked) {
        if(!playerDeathHandler.isPlayerAlive(attacked)) return;

        //give killer points
        //ppAPI.give(attacker.getUniqueId(), (int) (plugin.getConfig().getInt(gameType + ".killPoints") * multiplier));
        handler.getPlayerTeam(attacker).addTempPoints(attacker, plugin.getConfig().getInt(gameType + ".killPoints") * multiplier);

        if(playerKills.get(attacker) != null) {
            playerKills.put(attacker, playerKills.get(attacker) + 1);
        } else {
            playerKills.put(attacker, 1);
        }


        for (Player p : Bukkit.getOnlinePlayers()) {
            if(!p.equals(attacker) && !p.equals(attacked)) {
                if(playerDeathHandler.isPlayerAlive(p)) {
                    p.sendMessage(handler.getPlayerTeam(attacked).color + attacked.getName() + ChatColor.RESET + " was killed by " + handler.getPlayerTeam(attacker).color + attacker.getName() + ChatColor.RESET + "[+" + ChatColor.YELLOW + ChatColor.BOLD + (plugin.getConfig().getDouble(gameType + ".survivalPoints") * multiplier) + ChatColor.RESET + "] Points");
                } else {
                    p.sendMessage(handler.getPlayerTeam(attacked).color + attacked.getName() + ChatColor.RESET + " was killed by " + handler.getPlayerTeam(attacker).color + attacker.getName() + ChatColor.RESET + ".");
                }

            }
        }

        attacker.sendMessage("[+" + ChatColor.YELLOW + ChatColor.BOLD + plugin.getConfig().getInt(gameType + ".killPoints") * multiplier + ChatColor.RESET + "] points " + handler.getPlayerTeam(attacked).color + attacked.getName() + ChatColor.RESET + " was killed by " + handler.getPlayerTeam(attacker).color + attacker.getName());
        attacker.playSound(attacker.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1f, 2f);
        attacker.sendTitle("Killed " + handler.getPlayerTeam(attacked).color + attacked.getName(), "", 2, 16, 2);

        attacked.sendMessage("You were killed by " + handler.getPlayerTeam(attacker).color + attacker.getName() + "!");
        attacked.playSound(attacked.getLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT, 1, 2);

        playerDeathHandler.onPlayerDeath(attacked);
    }


    public void onOtherDeath(Player attacked) {
        if(!playerDeathHandler.isPlayerAlive(attacked)) return;

        if(storedAttackers.get(attacked) != null) {
            Player attacker = storedAttackers.get(attacked);
            handler.getPlayerTeam(attacker).addTempPoints(attacker, plugin.getConfig().getInt(gameType + ".killPoints") * multiplier);

            if(playerKills.get(attacker) != null) {
                playerKills.put(attacker, playerKills.get(attacker) + 1);
            } else {
                playerKills.put(attacker, 1);
            }

            attacker.sendTitle("Killed " + handler.getPlayerTeam(attacked).color + attacked.getName(), "", 2, 16, 2);
            attacker.sendMessage("[+" + ChatColor.YELLOW + ChatColor.BOLD + plugin.getConfig().getInt(gameType + ".killPoints") * multiplier + ChatColor.RESET + "] points" + handler.getPlayerTeam(attacked).color + attacked.getName() + ChatColor.RESET + " was killed by " + handler.getPlayerTeam(attacker).color + attacker.getName());
            attacker.playSound(attacker.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1f, 2f);

            attacked.sendMessage("You were killed by " + handler.getPlayerTeam(attacker).color + attacker.getName() + "!");
            attacked.playSound(attacked.getLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT, 1, 2);

            for(Player player: Bukkit.getOnlinePlayers()) {
                if(!player.equals(attacker) && !player.equals(attacked)) {
                    if(playerDeathHandler.isPlayerAlive(player)) {
                        player.sendMessage(handler.getPlayerTeam(attacked).color + attacked.getName() + ChatColor.RESET + " was killed by " + handler.getPlayerTeam(attacker).color + attacker.getName() + ChatColor.RESET + " [+" + ChatColor.YELLOW + ChatColor.BOLD + (int)(plugin.getConfig().getDouble(gameType + ".survivalPoints") * multiplier) + ChatColor.RESET + "] Points");
                    } else {
                        player.sendMessage(handler.getPlayerTeam(attacked).color + attacked.getName() + ChatColor.RESET + " was killed by " + handler.getPlayerTeam(attacker).color + attacker.getName() + ChatColor.RESET + ".");
                    }
                }
            }

        } else {
            for(Player player:Bukkit.getOnlinePlayers()) {
                if(!player.equals(attacked)) {
                    //System.out.println(player.getName() + "   " + playerDeathHandler.isPlayerAlive(player));
                    if(playerDeathHandler.isPlayerAlive(player)) {
                        player.sendMessage(handler.getPlayerTeam(attacked).color + attacked.getName() + ChatColor.RESET + " died [+" + ChatColor.YELLOW + ChatColor.BOLD + (int)(plugin.getConfig().getDouble(gameType + ".survivalPoints") * multiplier) + ChatColor.RESET + "] Points");
                    } else {
                        player.sendMessage(handler.getPlayerTeam(attacked).color + attacked.getName() + ChatColor.RESET + " died.");
                    }
                }
            }
            attacked.sendMessage(ChatColor.GRAY + "You died.");
            attacked.playSound(attacked.getLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT,1,2);
        }

        playerDeathHandler.onPlayerDeath(attacked);
    }
}
