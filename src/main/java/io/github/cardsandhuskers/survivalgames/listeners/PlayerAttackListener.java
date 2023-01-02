package io.github.cardsandhuskers.survivalgames.listeners;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import io.github.cardsandhuskers.survivalgames.handlers.PlayerDeathHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.*;


public class PlayerAttackListener implements Listener {
    private PlayerPointsAPI ppAPI;
    private HashMap<Player, Player> storedAttackers;
    private HashMap<Player, Integer> attackerTimers;
    private PlayerDeathHandler deathHandler;
    private PlayerDamageListener playerDamageListener;
    private SurvivalGames plugin;

    public PlayerAttackListener(PlayerPointsAPI ppAPI, PlayerDeathHandler deathHandler, HashMap<Player, Player> storedAttackers, HashMap<Player, Integer> attackerTimers, PlayerDamageListener playerDamageListener, SurvivalGames plugin) {
        this.attackerTimers = attackerTimers;
        this.storedAttackers = storedAttackers;
        this.deathHandler = deathHandler;
        this.ppAPI = ppAPI;
        this.playerDamageListener = playerDamageListener;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent e) {
        Player attacker;
        Player attacked;

        if(e.getEntity().getType() == EntityType.PLAYER) {
            attacked = (Player) e.getEntity();
            //if attack is from arrow
            if(e.getDamager().getType() == EntityType.ARROW) {
                Arrow arrow = (Arrow) e.getDamager();
                attacker = (Player) arrow.getShooter();
                damage(attacker, attacked, e);

                //if attack is from player
            } else if(e.getDamager().getType() == EntityType.SPECTRAL_ARROW){
                SpectralArrow arrow = (SpectralArrow) e.getDamager();
                attacker = (Player) arrow.getShooter();
                damage(attacker, attacked, e);
            }else if(e.getDamager().getType() == EntityType.PLAYER) {
                attacker = (Player) e.getDamager();
                damage(attacker, attacked, e);
            } else if(e.getDamager().getType() == EntityType.SPLASH_POTION) {
                ThrownPotion potion = (ThrownPotion) e.getDamager();
                attacker = (Player) potion.getShooter();
                damage(attacker, attacked, e);
            }else if(e.getDamager().getType() == EntityType.SNOWBALL) {
                if(gameState == State.GAME_IN_PROGRESS || gameState == State.DEATHMATCH) {

                    double velocityX = attacked.getVelocity().getX() + e.getDamager().getVelocity().getX()/1.25;
                    double velocityY = attacked.getVelocity().getY();
                    double velocityZ = attacked.getVelocity().getZ() + e.getDamager().getVelocity().getZ()/1.25;

                    Vector velocity = new Vector(velocityX, velocityY, velocityZ);
                    attacked.setVelocity(velocity);

                    if(attacked.getHealth() > .5) {
                        attacked.damage(0.5);
                        if(!(attacked.getHealth() + .5 > 20)) {
                            attacked.setHealth(attacked.getHealth() + .5);
                        }

                    } else {
                        attacked.setHealth(attacked.getHealth() + .5);
                        attacked.damage(0.5);
                    }
                }

            }else {
                if(attacked.getHealth() - e.getDamage() <= 0 && attacked.getGameMode() != GameMode.SPECTATOR) {
                    e.setCancelled(true);
                    playerDamageListener.onPlayerDeath(attacked);
                }
            }
        } else {
            //e.setCancelled(true);
        }
    }

    /**
     * Handles the conditions for pvp damage and determines whether to cancel it
     * @param attacker
     * @param attacked
     * @param e
     */
    public void damage(Player attacker, Player attacked, EntityDamageByEntityEvent e) { //this part added for SKYWARS to remove grace period
        if(gameState == State.GAME_IN_PROGRESS || gameState == State.DEATHMATCH || (gameType == GameType.SKYWARS && gameState == State.GRACE_PERIOD)) {
            //handle error if someone's team is null
            if (!(handler.getPlayerTeam(attacker) == null || handler.getPlayerTeam(attacked) == null)) {
                if (handler.getPlayerTeam(attacker).equals(handler.getPlayerTeam(attacked))) {
                    e.setCancelled(true);
                } else {
                    //s is an attacked player
                    storedAttackers.put(attacked, attacker);
                    attackerTimers.put(attacked, 0);

                    if (attacked.getHealth() - e.getDamage() <= 0) {
                        //attacked.sendMessage("You Died");
                        e.setCancelled(true);

                        //give everyone else survival points in the deathHandler
                        deathHandler.onPlayerDeath(attacked);

                        //give killer points
                        ppAPI.give(attacker.getUniqueId(), (int) (plugin.getConfig().getInt(gameType + ".killPoints") * multiplier));
                        handler.getPlayerTeam(attacker).addTempPoints(attacker, (int) (plugin.getConfig().getInt(gameType + ".killPoints") * multiplier));

                        if(playerKills.get(attacker) != null) {
                            playerKills.put(attacker, playerKills.get(attacker) + 1);
                        } else {
                            playerKills.put(attacker, 1);
                        }

                        for (Player p : handler.getPlayerTeam(attacker).getOnlinePlayers()) {
                            if (p.equals(attacker)) {
                                p.sendMessage("[+" + plugin.getConfig().getInt(gameType + ".killPoints") * multiplier + " points] " + handler.getPlayerTeam(attacked).color + attacked.getName() + ChatColor.RESET + " was killed by " + handler.getPlayerTeam(attacker).color + attacker.getName());
                            } else {
                                //p.sendMessage(handler.getPlayerTeam(attacked).color + attacked.getName() + ChatColor.RESET + " was killed by " + handler.getPlayerTeam(attacker).color + attacker.getName());
                            }
                        }
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if(!p.equals(attacker) && !p.equals(attacked)) {
                                p.sendMessage(handler.getPlayerTeam(attacked).color + attacked.getName() + ChatColor.RESET + " was killed by " + handler.getPlayerTeam(attacker).color + attacker.getName() + ChatColor.RESET + "[+" + ChatColor.YELLOW + (int)(plugin.getConfig().getInt(gameType + ".survivalPoints") * multiplier) + ChatColor.RESET + "] Points");
                            }
                        }
                        attacker.playSound(attacker.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1f, 2f);
                        attacker.sendTitle("Killed " + handler.getPlayerTeam(attacked).color + attacked.getName(), "", 2, 16, 2);

                        attacked.sendMessage("You were killed by " + handler.getPlayerTeam(attacker).color + attacker.getName() + "!");
                        attacked.playSound(attacked.getLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT, 1, 2);

                    }
                }
            }
        } else {
            //wrong state
            e.setCancelled(true);
        }
    }
}
