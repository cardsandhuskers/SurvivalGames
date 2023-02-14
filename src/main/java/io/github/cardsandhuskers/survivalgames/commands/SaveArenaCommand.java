package io.github.cardsandhuskers.survivalgames.commands;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class SaveArenaCommand implements CommandExecutor {
    private final SurvivalGames plugin;
    private SurvivalGames.GameType game;

    public SaveArenaCommand(SurvivalGames plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player p && p.isOp()) {
            try {
                game = SurvivalGames.GameType.valueOf(args[0].toUpperCase());
            } catch (Exception e) {
                p.sendMessage(ChatColor.RED + "ERROR: game type must be SURVIVAL_GAMES or SKYWARS");
                return false;
            }
            //get game type they are saving arena for


            //figure out which corner has the higher value for X, Y, and Z axis
            String higherx;
            String lowerx;
            String highery;
            String lowery;
            String higherz;
            String lowerz;
            if (getCoordinate("pos1", 'x') > getCoordinate("pos2", 'x')) {
                higherx = "pos1";
                lowerx = "pos2";
            } else {
                higherx = "pos2";
                lowerx = "pos1";
            }
            if (getCoordinate("pos1", 'y') > getCoordinate("pos2", 'y')) {
                highery = "pos1";
                lowery = "pos2";
            } else {
                highery = "pos2";
                lowery = "pos1";
            }
            if (getCoordinate("pos1", 'z') > getCoordinate("pos2", 'z')) {
                higherz = "pos1";
                lowerz = "pos2";
            } else {
                higherz = "pos2";
                lowerz = "pos1";
            }

            //add all chest locations to a list
            ArrayList<Location> chestList = new ArrayList<>();
            //3 loops to iterate on 3 axes
            for (int x = getCoordinate(lowerx, 'x'); x <= getCoordinate(higherx, 'x'); x++) {
                for (int z = getCoordinate(lowerz, 'z'); z <= getCoordinate(higherz, 'z'); z++) {
                    for (int y = getCoordinate(lowery, 'y'); y <= getCoordinate(highery, 'y'); y++) {
                        //make location
                        Location loc = new Location(plugin.getConfig().getLocation(game + ".pos1").getWorld(), x, y, z);
                        if(loc.getBlock().getType() == Material.CHEST) {
                            chestList.add(loc);
                        }
                    }
                }
            }
            //SAVE the list to the arena.yml file
            File arenaFile = new File(Bukkit.getServer().getPluginManager().getPlugin("SurvivalGames").getDataFolder(), game + ".yml");
            if (!arenaFile.exists()) {
                try {
                    System.out.println("MAKING FILE");
                    arenaFile.createNewFile();
                } catch (IOException e) {
                    System.out.println("ERROR CREATING FILE");
                }
            }
            FileConfiguration arenaFileConfig = YamlConfiguration.loadConfiguration(arenaFile);


            //System.out.println(blockMap);
            //arenaFileConfig.set("chests", chestList);
            int index = 0;

            for (Location l : chestList) {

                String s = "chests." + index + ".";

                arenaFileConfig.set("world", l.getWorld().getName());

                //arenaFileConfig.set(s + "Block", l.getBlock().toString());
                arenaFileConfig.set(s + "x", l.getX());
                arenaFileConfig.set(s + "y", l.getY());
                arenaFileConfig.set(s + "z", l.getZ());
                index++;
            }

            try {
                arenaFileConfig.save(arenaFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            World world = plugin.getConfig().getLocation(game + ".pos1").getWorld();


            int x1 = getCoordinate(lowerx, 'x');
            int x2 = getCoordinate(higherx, 'x');

            int y1 = getCoordinate(lowery, 'y');
            int y2 = getCoordinate(highery, 'y');

            int z1 = getCoordinate(lowerz, 'z');
            int z2 = getCoordinate(higherz, 'z');


            //int counter = 1;
/*
            for(int i = 1; i <= 3; i++) {
                int x = x1 + (x2-x1) /3 * i;
                int pastx = x1 + (x2 - x1) / 3 * (i - 1);
                for(int j = 1; j <= 3; j++) {
                    int y = y1 + (y2-y1)/3 * j;
                    int pasty = y1 + (y2 - y1) / 3 * (j-1);
                    for (int k = 1; k <= 3; k++) {
                        int z = z1 + (z2 - z1) / 3 * k;
                        int pastz = z1 + (z2 - z1) / 3 * (k-1);
*/              Bukkit.getScheduler().runTaskAsynchronously(plugin, ()-> {
                    Location loc1 = new Location(plugin.getConfig().getLocation(game + ".pos1").getWorld(), x1, y1, z1);
                    Location loc2 = new Location(plugin.getConfig().getLocation(game + ".pos1").getWorld(), x2, y2, z2);

                    BlockVector3 vector1 = BukkitAdapter.asBlockVector(loc1);
                    BlockVector3 vector2 = BukkitAdapter.asBlockVector(loc2);

                    BukkitWorld weWorld = new BukkitWorld(world);

                    CuboidRegion region = new CuboidRegion(weWorld, vector1, vector2);
                    BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
                    EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld);

                    ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());

                    //COPIED to clip board
                    File file = new File("plugins/SurvivalGames/" + game + ".schem");
                    try (ClipboardWriter writer = BuiltInClipboardFormat.FAST.getWriter(new FileOutputStream(file))) {
                        Operations.complete(forwardExtentCopy);
                        writer.write(clipboard);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //counter++;
                    editSession.close();
                });
                    //}
                //}
            //}
            Bukkit.broadcastMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "ARENA SAVED!");
        return true;
        }
        return false;
    }

    public int getCoordinate(String pos, char axis) {
        Location l = plugin.getConfig().getLocation(game + "." + pos);
        switch(axis) {
            case 'x': return l.getBlockX();
            case 'y': return l.getBlockY();
            case 'z': return l.getBlockZ();
            default: return 0;
        }
    }

}
