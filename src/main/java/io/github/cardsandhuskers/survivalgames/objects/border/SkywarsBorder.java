package io.github.cardsandhuskers.survivalgames.objects.border;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Set;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.gameType;

public class SkywarsBorder implements Border{
    private final SurvivalGames plugin;
    public SkywarsBorder(SurvivalGames plugin) {
        this.plugin = plugin;
    }
    @Override
    public void buildWorldBorder() {
        //( x - h )^2 + ( y - k )^2 = r^2
        Location l1 = plugin.getConfig().getLocation(gameType + ".pos1");
        Location l2 = plugin.getConfig().getLocation(gameType + ".pos2");

        int centerX = (int)(l1.getX() + l2.getX())/2;
        int centerZ = (int)(l1.getZ() + l2.getZ())/2;

        int radius = (int) Math.abs(centerX - l1.getX());

        //find blocks along circumference line
        Set<Block> edgeBlocks = new HashSet<>();
        for(double x = centerX - radius; x <= centerX + radius; x+=.1) {
            double zSquared = (centerX - x) * (centerX - x);
            double z = Math.sqrt(zSquared);

        }

    }

    @Override
    public void shrinkWorldBorder(int size, int time) {

    }

    @Override
    public void setWorldBorder(int size) {

    }

    @Override
    public void updateSizeVariable() {

    }
}
