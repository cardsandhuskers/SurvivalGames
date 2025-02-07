package io.github.cardsandhuskers.survivalgames.listeners;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import io.github.cardsandhuskers.survivalgames.handlers.PlayerDeathHandler;
import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.*;


public class PlayerAttackListener implements Listener {
    private final HashMap<Player, Player> storedAttackers;
    private final HashMap<Player, Integer> attackerTimers;
    private final PlayerDeathHandler deathHandler;
    private final SurvivalGames plugin;

    public PlayerAttackListener(PlayerDeathHandler deathHandler, HashMap<Player, Player> storedAttackers, HashMap<Player, Integer> attackerTimers, SurvivalGames plugin) {
        this.attackerTimers = attackerTimers;
        this.storedAttackers = storedAttackers;
        this.deathHandler = deathHandler;
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

                Team attackedTeam = TeamHandler.getInstance().getPlayerTeam(attacked);
                if(attackedTeam != null) {
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, ()-> {attacker.sendMessage(attackedTeam.getColor() + attacked.getDisplayName() + ChatColor.RESET + " is on " + ChatColor.RED + (int)attacked.getHealth() + "❤");}, 1L);
                }

                //if attack is from player
            } else if(e.getDamager().getType() == EntityType.SPECTRAL_ARROW){
                SpectralArrow arrow = (SpectralArrow) e.getDamager();
                attacker = (Player) arrow.getShooter();
                damage(attacker, attacked, e);

                Team attackedTeam = TeamHandler.getInstance().getPlayerTeam(attacked);
                if(attackedTeam != null) {
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, ()-> {attacker.sendMessage(attackedTeam.getColor() + attacked.getDisplayName() + ChatColor.RESET + " is on " + ChatColor.RED + (int)attacked.getHealth() + "❤");}, 1L);
                }

            }else if(e.getDamager().getType() == EntityType.PLAYER) {
                attacker = (Player) e.getDamager();
                damage(attacker, attacked, e);
            } else if(e.getDamager().getType() == EntityType.POTION) {
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

                Snowball snowball = (Snowball) e.getDamager();
                attacker = (Player) snowball.getShooter();
                damage(attacker, attacked, e);

            }else {
                if(attacked.getHealth() - e.getDamage() <= 0 && attacked.getGameMode() != GameMode.SPECTATOR) {
                    //e.setCancelled(true);
                    //playerDamageListener.onPlayerDeath(attacked);
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

                }
            }
        } else {
            //wrong state
            e.setCancelled(true);
        }
    }
}
