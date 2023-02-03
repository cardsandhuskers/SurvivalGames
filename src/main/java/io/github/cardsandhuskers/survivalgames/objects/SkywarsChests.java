package io.github.cardsandhuskers.survivalgames.objects;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class SkywarsChests {
    SurvivalGames plugin;

    SkywarsChests(SurvivalGames plugin) {
        this.plugin = plugin;
    }

    public void populateSkywarsChests(ArrayList<Block> chestList, HashMap<Chests.ItemCategory, HashMap<Integer, ArrayList<ItemStack>>> itemMap) {
        //empty chests
        for(Block block:chestList) {
            //Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()-> {
            if(block.getType() == Material.CHEST) {
                Chest chest = (Chest) block.getState();
                Inventory chestInv = chest.getInventory();
                chestInv.clear();
            } else {
                System.out.println("BLOCK IS NO LONGER A CHEST");
            }
            //}, 1L * counter);
        }

        //initialize variables from config
        int minItems = plugin.getConfig().getInt("SKYWARS.chest.min");
        int maxItems = plugin.getConfig().getInt("SKYWARS.chest.max");

        //get category values
        //weapons, armor, util, food
        final int weaponsChance = plugin.getConfig().getInt("SKYWARS.chest.WEAPONS.chance");
        final int weaponsDropoff = plugin.getConfig().getInt("SKYWARS.chest.WEAPONS.dropoff");

        final int armorChance = plugin.getConfig().getInt("SKYWARS.chest.ARMOR.chance");
        final int armorDropoff = plugin.getConfig().getInt("SKYWARS.chest.ARMOR.dropoff");

        final int blocksChance = plugin.getConfig().getInt("SKYWARS.chest.BLOCKS.chance");
        final int blocksDropoff = plugin.getConfig().getInt("SKYWARS.chest.BLOCKS.dropoff");

        final int foodChance = plugin.getConfig().getInt("SKYWARS.chest.FOOD.chance");
        final int foodDropoff = plugin.getConfig().getInt("SKYWARS.chest.FOOD.dropoff");

        final int toolsChance = plugin.getConfig().getInt("SKYWARS.chest.TOOLS.chance");
        final int toolsDropoff = plugin.getConfig().getInt("SKYWARS.chest.TOOLS.dropoff");

        final int specialChance = plugin.getConfig().getInt("SKYWARS.chest.SPECIAL.chance");
        final int specialDropoff = plugin.getConfig().getInt("SKYWARS.chest.SPECIAL.dropoff");

        Random random = new Random();

        for(Block block:chestList) {
            int tempWeaponsChance = weaponsChance;
            int tempArmorChance = armorChance;
            int tempBlocksChance = blocksChance;
            int tempFoodChance = foodChance;
            int tempToolsChance = toolsChance;
            int tempSpecialChance = specialChance;

            if(block.getType() == Material.CHEST) {
                //get the number of items to put in each chest
                int numItems = random.nextInt(maxItems - minItems) + minItems;

                //get the chest inventory
                Chest chest = (Chest) block.getState();
                Inventory chestInv = chest.getInventory();

                ArrayList<Integer> usedLocations = new ArrayList<>();

                for (int i = 1; i <= numItems; i++) {

                    if(tempWeaponsChance < 0) tempWeaponsChance = 0;
                    if(tempArmorChance < 0) tempArmorChance = 0;
                    if(tempBlocksChance < 0) tempBlocksChance = 0;
                    if(tempFoodChance < 0) tempFoodChance = 0;
                    if(tempToolsChance < 0) tempToolsChance = 0;
                    if(tempSpecialChance < 0) tempSpecialChance = 1;

                    //get category
                    int categoryValue = random.nextInt((tempWeaponsChance + tempArmorChance + tempBlocksChance + tempFoodChance + tempToolsChance + tempSpecialChance));
                    Chests.ItemCategory category = Chests.ItemCategory.WEAPONS;

                    if(categoryValue < tempWeaponsChance) {
                        category = Chests.ItemCategory.WEAPONS;
                        tempWeaponsChance -= weaponsDropoff;
                    } else if(categoryValue < (tempWeaponsChance + tempArmorChance)) {
                        category = Chests.ItemCategory.ARMOR;
                        tempArmorChance -= armorDropoff;
                    } else if(categoryValue < (tempWeaponsChance + tempArmorChance + tempBlocksChance)) {
                        category = Chests.ItemCategory.BLOCKS;
                        tempBlocksChance -= blocksDropoff;
                    } else if(categoryValue < (tempWeaponsChance + tempArmorChance + tempBlocksChance + tempFoodChance)) {
                        category = Chests.ItemCategory.FOOD;
                        tempFoodChance -= foodDropoff;
                    } else if(categoryValue < (tempWeaponsChance + tempArmorChance + tempBlocksChance + tempFoodChance + tempToolsChance)) {
                        category = Chests.ItemCategory.TOOLS;
                        tempToolsChance -= toolsDropoff;
                    } else if(categoryValue < (tempWeaponsChance + tempArmorChance + tempBlocksChance + tempFoodChance + tempToolsChance + tempSpecialChance)) {
                        category = Chests.ItemCategory.SPECIAL;
                        tempSpecialChance -= specialDropoff;
                    }

                    File arenaFile = new File(plugin.getDataFolder(),"SKYWARS.yml");
                    FileConfiguration arenaFileConfig = YamlConfiguration.loadConfiguration(arenaFile);
                    boolean isIsland = false;

                    //for each spawn location, if the chest is +- 10 blocks of the X or Z, then it's an island chest
                    int counter2 = 1;
                    while(arenaFileConfig.get("teamSpawn." + counter2) != null) {
                        Location spawn = arenaFileConfig.getLocation("teamSpawn." + counter2);
                        Location chestLoc = block.getLocation();
                        if(Math.abs(spawn.getX() - chestLoc.getX()) <= 10 && Math.abs(spawn.getZ() - chestLoc.getZ()) <= 10) {
                            isIsland = true;
                            break;
                        }
                        counter2++;
                    }

                    int quality = random.nextInt(12);
                    ItemStack item;
                    ArrayList<ItemStack> materialList;

                    if(isIsland) {
                        if (quality <= 4) materialList = itemMap.get(category).get(1); //quality 1, 0-4 (5)
                        else if (quality <= 8) materialList = itemMap.get(category).get(2); //quality 2, 5-8 (4)
                        else materialList = itemMap.get(category).get(3); //quality 3, 9-11 (3)
                    } else {
                        if (quality <= 4) materialList = itemMap.get(category).get(3); //quality 3, 0-4 (5)
                        else if (quality <= 8) materialList = itemMap.get(category).get(4); //quality 4, 5-8 (4)
                        else materialList = itemMap.get(category).get(5); //quality 5, 9-11 (3)
                    }

                    int chosenItem = random.nextInt(materialList.size());
                    item = materialList.get(chosenItem);

                    int slot = random.nextInt(27);
                    while (usedLocations != null && usedLocations.contains(slot)) {
                        slot = random.nextInt(27);
                    }
                    usedLocations.add(slot);
                    //System.out.println("used locations " + usedLocations);
                    chestInv.setItem(slot, item);

                    if(item.getType()==Material.BOW || item.getType() == Material.CROSSBOW) {
                        int quantity;
                        if(isIsland) quantity = 16;
                        else quantity = 48;

                        slot = random.nextInt(27);
                        while (usedLocations != null && usedLocations.contains(slot)) {
                            slot = random.nextInt(27);
                        }
                        usedLocations.add(slot);
                        chestInv.setItem(slot, new ItemStack(Material.ARROW, quantity));
                        i++;
                    }
                }
            }
        }
    }
}
