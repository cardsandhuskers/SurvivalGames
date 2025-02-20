package io.github.cardsandhuskers.survivalgames.objects.border;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import io.github.cardsandhuskers.survivalgames.objects.Countdown;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.gameType;

public class SkywarsCrumbleBorder implements Border, Runnable{
    private final SurvivalGames plugin;
    private static double borderSize = 0;
    private int period = 0;
    private Integer assignedTaskId;

    private int maxY, minY;
    private double centerX, centerZ;

    Set<Block> edgeBlocks;

    public SkywarsCrumbleBorder(SurvivalGames plugin) {
        this.plugin = plugin;
    }

    /**
     * Initial 'build' of the world border, but there is nothing that actually needs to be built here
     */
    @Override
    public void buildWorldBorder() {
        Location l1 = plugin.getConfig().getLocation(gameType + ".pos1");
        Location l2 = plugin.getConfig().getLocation(gameType + ".pos2");

        int widthX = (int)Math.abs(l1.getX() - l2.getX());
        int widthZ = (int)Math.abs(l1.getX() - l2.getX());
        borderSize = (double) Math.max(widthX, widthZ) / 2 + 1;
    }

    /**
     * Initial shrink of worldborder
     * @param size end size of border
     * @param time time for shrink
     */
    @Override
    public void shrinkWorldBorder(int size, int time) {
        //current size - 'new size'
        Location l1 = plugin.getConfig().getLocation(gameType + ".pos1");
        Location l2 = plugin.getConfig().getLocation(gameType + ".pos2");

        int widthX = (int)Math.abs(l1.getX() - l2.getX());
        int widthZ = (int)Math.abs(l1.getX() - l2.getX());

        minY = (int)Math.min(l1.getY(), l2.getY());
        maxY = (int)Math.max(l1.getY(), l2.getY());

        Location center = plugin.getConfig().getLocation("SKYWARS.center");
        if (center == null) {
            centerX =(l1.getX() + l2.getX())/2;
            centerZ = (l1.getZ() + l2.getZ())/2;
        } else {
            centerX = center.getX();
            centerZ = center.getZ();
        }


        //ticks per block
        //period is doubled because it shrinks based on radius, not diameter
        period = 20 * time / (Math.max(widthX, widthZ) - size) * 2;
        //trying a +1 cuz outer layer is ignored for some reason
        borderSize = (double)Math.max(widthX, widthZ) / 2 + 1;

        updateBorderEdgeBlocks(false);
        startOperation();

    }

    @Override
    public void setSize(int size) {
        borderSize = size;
        updateBorderEdgeBlocks(false);
    }

    @Override
    public void startOperation() {
        this.assignedTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 0L, period);
    }

    @Override
    public void cancelOperation() {
        if (assignedTaskId != null) Bukkit.getScheduler().cancelTask(assignedTaskId);
    }

    @Override
    public int getCenterX() {
        return (int)centerX;
    }

    @Override
    public int getCenterZ() {
        return (int)centerZ;
    }

    @Override
    public void run() {
        borderSize--;
        updateBorderEdgeBlocks(true);
        //crumble();

        if (borderSize <= 1) cancelOperation();

    }

    public void updateBorderEdgeBlocks(boolean crumble) {
        Location l1 = plugin.getConfig().getLocation(gameType + ".pos1");

        edgeBlocks = new HashSet<>();
        double pi = 3.14;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()-> {

            // Code to kill the task if it's still running
            Thread thread = Thread.currentThread();
            long startTime = System.currentTimeMillis();
            long timeout = 3000; // 3 seconds

            final BukkitTask[] timerTask = new BukkitTask[1];
            timerTask[0] = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (System.currentTimeMillis() - startTime > timeout) {
                    if (thread.isAlive()) {
                        //System.out.println("Task took too long, interrupting!");
                        thread.interrupt();  // Interrupt the task if it's still running
                        timerTask[0].cancel();  // Cancel the timer task after interrupting
                    }
                }
            }, 0L, 20L);  // Check every 20 ticks (1 second)


            for(double angle = 0; angle < 2 * 3.14; angle += (2* pi) / (double)360 / (borderSize / (double) 20)) {

                //double radians = Math.toRadians(angle);
                double x = centerX + borderSize * Math.cos(angle);
                double z = centerZ + borderSize * Math.sin(angle);

                edgeBlocks.add(new Location(l1.getWorld(), x, 0, z).getBlock());
            }

            //find any outstanding blocks from prior crumble
            for(double angle = 0; angle < 2 * 3.14; angle += (2* pi) / (double)360 / (borderSize / (double) 20)) {
                //double radians = Math.toRadians(angle);
                double x = centerX + (borderSize+1) * Math.cos(angle);
                double z = centerZ + (borderSize+1) * Math.sin(angle);

                Bukkit.getScheduler().runTask(plugin, ()-> {
                    for(int y = minY; y <= maxY; y++) {
                        Location testLoc = new Location(l1.getWorld(), x, y, z);
                        testLoc.getBlock().setType(Material.AIR);
                    }
                });
            }

            if (crumble) Bukkit.getScheduler().runTask(plugin, this::crumble);
        });
    }

    public void crumble() {
        World world = plugin.getConfig().getLocation(gameType + ".pos1").getWorld();
        if(world == null) {
            plugin.getLogger().warning("Cannot crumble skywars border, world not found!");
            return;
        }

        for (Block b : edgeBlocks) {

            int foundBlocks = 0;
            for(int y = minY; y <= maxY; y++) {

                Block testBlock = new Location(world, b.getX(), y, b.getZ()).getBlock();
                if(testBlock.getType() != Material.AIR &&
                        testBlock.getType() != Material.CAVE_AIR) {

                    foundBlocks++;

                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, ()-> {
                        Location loc = testBlock.getLocation();
                        loc.add(.5, 0, .5);

                        //if 3 nearest blocks above the block are air, spawn an entity to fall
                        boolean foundSolid = false;
                        for(int i = 1; i <= 3; i++) {
                            Location above = loc.clone().add(0,i,0);
                            if(above.getBlock().getType() != Material.AIR && above.getBlock().getType() != Material.CAVE_AIR) {
                                foundSolid = true;
                            }
                        }
                        if (!foundSolid) {
                            FallingBlock block = world.spawnFallingBlock(loc, testBlock.getType().createBlockData());
                            block.setCancelDrop(true);

                            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, block::remove, 15L);
                        }
                        testBlock.setType(Material.AIR);



                    }, foundBlocks);

                }
            }
        }
    }

    public static double getBorderSize() {
        return borderSize;
    }

}
