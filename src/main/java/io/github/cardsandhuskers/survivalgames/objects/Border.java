package io.github.cardsandhuskers.survivalgames.objects;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.plugin.java.JavaPlugin;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.altTimeVar;

public class Border {
    public static int borderSize = 0;
    private WorldBorder worldBorder;
    private SurvivalGames plugin;

    public Border(SurvivalGames plugin) {
        this.plugin = plugin;
    }

    /**
     * Initializes a worldborder the size of the arena
     */
    public void buildWorldBorder() {
        Location l = plugin.getConfig().getLocation("pos1");

        worldBorder = Bukkit.getWorld(l.getWorld().getUID()).getWorldBorder();
        worldBorder.setCenter(0, -2);

        worldBorder.setSize(Math.abs(l.getX() * 2));
        worldBorder.setDamageBuffer(0);
    }

    /**
     * Resizes the worldborder
     * @param size
     * @param time
     */
    public void shrinkWorldBorder(int size, int time) {
        worldBorder.setSize(size, time);
        updateBorderSize(altTimeVar);
    }

    /**
     * updates a worldborder size variable every second
     * @param time
     */
    private void updateBorderSize(int time) {
        Countdown timer = new Countdown((JavaPlugin)plugin,

                time,
                //Timer Start
                () -> {
                },

                //Timer End
                () -> {
                },

                //Each Second
                (t) -> {
                    borderSize = (int) (worldBorder.getSize()/2);
                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        timer.scheduleTimer();

    }

}