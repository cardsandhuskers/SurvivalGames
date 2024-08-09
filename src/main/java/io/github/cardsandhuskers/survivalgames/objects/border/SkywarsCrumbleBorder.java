package io.github.cardsandhuskers.survivalgames.objects.border;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import io.github.cardsandhuskers.survivalgames.objects.Countdown;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;

import java.util.HashSet;
import java.util.Set;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.gameType;

public class SkywarsCrumbleBorder implements Border, Runnable{
    private final SurvivalGames plugin;
    private static double borderSize = 0;
    private int period = 0;
    private Integer assignedTaskId;

    private int maxY, minY;
    private int centerX, centerZ;

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
            centerX = (int)(l1.getX() + l2.getX())/2;
            centerZ = (int)(l1.getZ() + l2.getZ())/2;
        } else {
            centerX = center.getBlockX();
            centerZ = center.getBlockZ();
        }


        //ticks per block
        //period is doubled because it shrinks based on radius, not diameter
        period = 20 * time / (Math.max(widthX, widthZ) - size) * 2;
        //trying a +1 cuz outer layer is ignored for some reason
        borderSize = (double)Math.max(widthX, widthZ) / 2 + 1;

        updateBorderEdgeBlocks();
        startOperation();

    }

    @Override
    public void setSize(int size) {
        borderSize = size;
        updateBorderEdgeBlocks();
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
        return centerX;
    }

    @Override
    public int getCenterZ() {
        return centerZ;
    }

    @Override
    public void run() {
        borderSize--;
        updateBorderEdgeBlocks();
        crumble();

        System.out.println(borderSize);

        if (borderSize == 0) cancelOperation();

    }

    public void updateBorderEdgeBlocks() {
        Location l1 = plugin.getConfig().getLocation(gameType + ".pos1");

        edgeBlocks = new HashSet<>();
        double pi = 3.14;

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

            for(int y = minY; y <= maxY; y++) {
                Location testLoc = new Location(l1.getWorld(), x, y, z);
                testLoc.getBlock().setType(Material.AIR);
            }
        }
    }

    public void crumble() {
        World world = plugin.getConfig().getLocation(gameType + ".pos1").getWorld();
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

                        FallingBlock block = world.spawnFallingBlock(loc, testBlock.getType().createBlockData());
                        block.setCancelDrop(true);

                        testBlock.setType(Material.AIR);

                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, block::remove, 15L);

                    }, foundBlocks);

                }
            }
        }
    }

    public double getBorderSize() {
        return borderSize;
    }
}
