package io.github.cardsandhuskers.survivalgames.objects.border;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import io.github.cardsandhuskers.survivalgames.objects.Countdown;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldBorder;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.*;

/**
 * @deprecated updated to interface with 2 border types
 */
public class BorderOld {
    public static int borderSize = 0;
    private WorldBorder worldBorder;
    private final SurvivalGames plugin;

    public BorderOld(SurvivalGames plugin) {
        this.plugin = plugin;
    }

    /**
     * Initializes a worldborder the size of the arena
     * @param x X coordinate of center
     * @param z Z coordinate of center
     */
    public void buildWorldBorder(int x, int z) {
        Location l1 = plugin.getConfig().getLocation(gameType + ".pos1");
        Location l2 = plugin.getConfig().getLocation(gameType + ".pos2");

        worldBorder = Bukkit.getWorld(l1.getWorld().getUID()).getWorldBorder();
        worldBorder.setCenter(x, z);

        worldBorder.setSize(Math.abs(l1.getX()) + Math.abs(l2.getX()));
        worldBorder.setDamageBuffer(0);
        worldBorder.setDamageAmount(1);
    }

    /**
     * Resizes the worldborder
     * @param size end size of border
     * @param time time for shrink
     */
    public void shrinkWorldBorder(int size, int time) {
        worldBorder.setSize(size, time);
        updateBorderSize(altTimeVar);
    }

    public void setWorldBorder(int size) {
        worldBorder.setSize(size, 0);
    }

    public void chunkShrinkBorder(int time) {
        int numShrinks = 8;
        int delta = time / numShrinks;
        int shrinkValue = (int)(worldBorder.getSize() / numShrinks);

        Countdown timer = new Countdown(plugin,

                time,
                //Timer Start
                () -> {
                },

                //Timer End
                () -> {
                },

                //Each Second
                (t) -> {
                    if(timeVar % delta == 0) {
                        setWorldBorder((int) (worldBorder.getSize() - shrinkValue));
                    }
                    borderSize = (int) (worldBorder.getSize()/2);
                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        timer.scheduleTimer();
    }

    /**
     * updates a worldborder size variable every second
     * @param time to keep running the update for
     */
    private void updateBorderSize(int time) {
        Countdown timer = new Countdown(plugin,

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