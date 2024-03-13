package io.github.cardsandhuskers.survivalgames.objects.border;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SkywarsBorderDamageHandler implements Runnable {
    SurvivalGames plugin;
    private Integer assignedTaskId;
    double damage;

    public SkywarsBorderDamageHandler(SurvivalGames plugin) {
        this.plugin = plugin;
        damage = plugin.getConfig().getDouble("SKYWARS.borderDamage");
    }

    @Override
    public void run() {
        applyDamage();
    }

    /**
     * damages players that are outside the border
     */
    private void applyDamage() {
        for(Player p: Bukkit.getOnlinePlayers()) {
            if(p.getGameMode() == GameMode.SURVIVAL) {
                Location playerLoc = p.getLocation();
                if(playerLoc.getY() < SkywarsBorder.getFloor() || playerLoc.getY() > SkywarsBorder.getCeil()) {
                    p.damage(damage);
                    return;
                }
                double playerX = playerLoc.getX();
                double playerZ = playerLoc.getZ();

                if((playerX * playerX) + (playerZ * playerZ) >= SkywarsBorder.getSize() * SkywarsBorder.getSize()) {
                    p.damage(damage);
                    return;
                }
            }
        }
    }

    /**
     * Schedules this instance to run every half second
     */
    public void startOperation() {
        // Initialize our assigned task's id, for later use so we can cancel
        this.assignedTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 0L, 10L);
    }
    public void cancelOperation() {
        if (assignedTaskId != null) Bukkit.getScheduler().cancelTask(assignedTaskId);
    }


}
