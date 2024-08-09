package io.github.cardsandhuskers.survivalgames.objects.border;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import io.github.cardsandhuskers.survivalgames.objects.Countdown;
import org.bukkit.*;

import java.util.HashSet;
import java.util.Set;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.gameType;

/**
 * @deprecated this causes a lot of lag, use SkywarsCrumbleBorder instead
 * Uses particles to construct a border.
 *
 */
public class SkywarsBorder implements Border, Runnable{
    private final SurvivalGames plugin;
    private static double borderSize = 0;
    private static double minY = 0, maxY = 0, height = 0;
    private Set<Location> edgeLocations;
    private int ticks = 0;
    Countdown borderTimer;
    private Integer assignedTaskId;

    public SkywarsBorder(SurvivalGames plugin) {
        this.plugin = plugin;
    }

    /**
     * Sets initial border size values and constructs the initial world border
     */
    @Override
    public void buildWorldBorder() {
        Location l1 = plugin.getConfig().getLocation(gameType + ".pos1");
        Location l2 = plugin.getConfig().getLocation(gameType + ".pos2");

        int centerX = (int)(l1.getX() + l2.getX())/2;
        borderSize = centerX - plugin.getConfig().getLocation(gameType + ".pos1").getBlockX();

        minY = Math.min(l1.getY(), l2.getY());
        maxY = Math.max(l1.getY(), l2.getY());
        height = maxY - minY;
        ticks = 0;

        updateBorderEdgeLocs();
    }

    /**
     * Triggers the shrinking of the worldborder
     * then initializes a countdown timer to incrementally shrink the border
     * @param size end size of border
     * @param time time for shrink
     */
    @Override
    public void shrinkWorldBorder(int size, int time) {
        double shrinkDelta = (borderSize - size) / (double)time;
        double shrinkDeltaY = (maxY - minY - size) / (double)time / 2.0;

        borderTimer = new Countdown(plugin,

                time,
                //Timer Start
                () -> {
                },

                //Timer End
                () -> {
                },

                //Each Second
                (t) -> {

                    borderSize = borderSize - shrinkDelta;
                    if(borderSize < 0) borderSize = 0;

                    maxY -= shrinkDeltaY;
                    minY += shrinkDeltaY;

                    updateBorderEdgeLocs();
                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        borderTimer.scheduleTimer();
    }




    public static int getSize() {
        return (int)borderSize;
    }

    /**
     * sets width of borderSize variable to passed value
     * @param size - new border size
     */
    @Override
    public void setSize(int size) {
        borderSize = size;
        updateBorderEdgeLocs();
    }

    public static int getHeight() {
        return (int) height;
    }

    public static int getFloor() {
        return (int)minY;
    }

    public static int getCeil() {
        return (int)maxY;
    }

    /**
     * sets the new worldborder height and adjusts the min and max y values
     * @param height
     */
    public void setHeight(int height) {
        minY -= (height - SkywarsBorder.height) / 2;
        maxY += (height - SkywarsBorder.height) / 2;

        SkywarsBorder.height = height;
        updateBorderEdgeLocs();
    }

    /**
     * updates the set of edge locations to match the new border size
     */
    private void updateBorderEdgeLocs() {
        Location l1 = plugin.getConfig().getLocation(gameType + ".pos1");
        Location l2 = plugin.getConfig().getLocation(gameType + ".pos2");

        int centerX = (int)(l1.getX() + l2.getX())/2;
        int centerZ = (int)(l1.getZ() + l2.getZ())/2;

        edgeLocations = new HashSet<>();
        double pi = 3.14;
        for(double angle = 0; angle < 2 * 3.14; angle += (2* pi) / 360 * (60 / Math.max(borderSize, .5))) {
            //double radians = Math.toRadians(angle);
            double x = centerX + borderSize * Math.cos(angle);
            double z = centerZ + borderSize * Math.sin(angle);

            for(int y = (int)minY; y < maxY; y++) {
                edgeLocations.add(new Location(l1.getWorld(), x, y, z));
            }
        }
    }

    /**
     * Spawns in the particles around the border.
     */
    private void generateBorderParticles() {
        Location l1 = plugin.getConfig().getLocation(gameType + ".pos1");
        Location l2 = plugin.getConfig().getLocation(gameType + ".pos2");

        int centerX = (int)(l1.getX() + l2.getX())/2;
        int centerZ = (int)(l1.getZ() + l2.getZ())/2;

        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(0, 127, 255), 1.0F);

        for(Location l: edgeLocations) {
            l1.getWorld().spawnParticle(Particle.REDSTONE, l.getX(), l.getY(), l.getZ(), 1, dustOptions);
        }

        double borderSquared = borderSize * borderSize;
        for(int x = (int) (centerX - borderSize); x < centerX + borderSize; x++) {
            for(int z = (int) (centerZ - borderSize); z < centerZ + borderSize; z++) {
                if((x * x) + (z * z) <= borderSquared) {
                    l1.getWorld().spawnParticle(Particle.REDSTONE, x, minY, z, 1, dustOptions);
                }
            }
        }

    }

    @Override
    public void run() {
        generateBorderParticles();
    }

    /**
     * Schedules this instance to run once every second, controls the particles
     */
    public void startOperation() {
        // Initialize our assigned task's id, for later use so we can cancel
        this.assignedTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 0L, 20L);
    }
    public void cancelOperation() {
        if (assignedTaskId != null) Bukkit.getScheduler().cancelTask(assignedTaskId);
        if(borderTimer != null) borderTimer.cancelTimer();
    }

    @Override
    public int getCenterX() {
        return 0;
    }

    @Override
    public int getCenterZ() {
        return 0;
    }
}
