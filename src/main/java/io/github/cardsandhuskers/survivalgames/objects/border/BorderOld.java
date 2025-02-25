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
public class BorderOld implements Border{
    public static int borderSize = 0;
    private WorldBorder worldBorder;
    private final SurvivalGames plugin;
    private int centerX, centerZ;

    public BorderOld(SurvivalGames plugin) {
        this.plugin = plugin;
    }

    /**
     * Initializes a worldborder the size of the arena
     */
    public void buildWorldBorder() {
        Location l1 = plugin.getConfig().getLocation(gameType + ".pos1");
        Location l2 = plugin.getConfig().getLocation(gameType + ".pos2");

        Location center = plugin.getConfig().getLocation(gameType + ".center");
        if (center == null) {
            centerX = (int)(l1.getX() + l2.getX())/2;
            centerZ = (int)(l1.getZ() + l2.getZ())/2;
        } else {
            centerX = center.getBlockX();
            centerZ = center.getBlockZ();
        }

        worldBorder = Bukkit.getWorld(l1.getWorld().getUID()).getWorldBorder();
        worldBorder.setCenter(centerX, centerZ);


        //find furthest distance from center
        int xMax = Math.max(l1.getBlockX() - centerX, l2.getBlockX() - centerX);
        int zMax = Math.max(l1.getBlockZ() - centerZ, l2.getBlockZ() - centerZ);

        worldBorder.setSize(Math.max(xMax, zMax) * 2);
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

    public void setSize(int size) {
        worldBorder.setSize(size, 0);
    }


    @Override
    public void startOperation() {

    }

    @Override
    public void cancelOperation() {

    }

    @Override
    public int getCenterX() {
        return centerX;
    }

    @Override
    public int getCenterZ() {
        return centerZ;
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
                        setSize((int) (worldBorder.getSize() - shrinkValue));
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

    public static double getBorderSize() {
        return borderSize;
    }

}