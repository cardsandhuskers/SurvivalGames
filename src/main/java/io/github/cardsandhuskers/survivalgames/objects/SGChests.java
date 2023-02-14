package io.github.cardsandhuskers.survivalgames.objects;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import org.bukkit.Bukkit;
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

public class SGChests {
    private final SurvivalGames plugin;

    public SGChests(SurvivalGames plugin) {
        this.plugin = plugin;
    }

    public void populateSGChests(ArrayList<Block> chestList, HashMap<Chests.ItemCategory, HashMap<Integer, ArrayList<ItemStack>>> itemMap) {
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
        int minItems = plugin.getConfig().getInt("SURVIVAL_GAMES.chest.min");
        int maxItems = plugin.getConfig().getInt("SURVIVAL_GAMES.chest.max");

        //get category values
        //weapons, armor, util, food
        final int weaponsChance = plugin.getConfig().getInt("SURVIVAL_GAMES.chest.WEAPONS.chance");
        final int weaponsDropoff = plugin.getConfig().getInt("SURVIVAL_GAMES.chest.WEAPONS.dropoff");

        final int armorChance = plugin.getConfig().getInt("SURVIVAL_GAMES.chest.ARMOR.chance");
        final int armorDropoff = plugin.getConfig().getInt("SURVIVAL_GAMES.chest.ARMOR.dropoff");

        final int foodChance = plugin.getConfig().getInt("SURVIVAL_GAMES.chest.FOOD.chance");
        final int foodDropoff = plugin.getConfig().getInt("SURVIVAL_GAMES.chest.FOOD.dropoff");

        final int specialChance = plugin.getConfig().getInt("SURVIVAL_GAMES.chest.SPECIAL.chance");
        final int specialDropoff = plugin.getConfig().getInt("SURVIVAL_GAMES.chest.SPECIAL.dropoff");

        //System.out.println(weaponsChance + " " + weaponsDropoff + " " + armorChance + " " + armorDropoff + " " + utilChance + " " + utilDropoff);
        Random random = new Random();
        int counter = 0;

        for(Block block:chestList) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->{
                int tempWeaponsChance = weaponsChance;
                int tempArmorChance = armorChance;
                int tempFoodChance = foodChance;
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
                        if(tempFoodChance < 0) tempFoodChance = 0;
                        if(tempSpecialChance < 0) tempSpecialChance = 1;

                        //System.out.println(tempWeaponsChance + " " + tempArmorChance + " " + tempUtilChance);

                        //get category and quality
                        int categoryValue = random.nextInt((tempWeaponsChance + tempArmorChance + tempFoodChance + tempSpecialChance));
                        //System.out.println(categoryValue);
                        int quality = random.nextInt(15);
                        Chests.ItemCategory category = Chests.ItemCategory.WEAPONS;

                        if(categoryValue < tempWeaponsChance) {
                            category = Chests.ItemCategory.WEAPONS;
                            tempWeaponsChance -= weaponsDropoff;
                        } else if(categoryValue < (tempWeaponsChance + tempArmorChance)) {
                            category = Chests.ItemCategory.ARMOR;
                            tempArmorChance -= armorDropoff;
                        } else if(categoryValue < (tempWeaponsChance + tempArmorChance + tempFoodChance)) {
                            category = Chests.ItemCategory.FOOD;
                            tempFoodChance -= foodDropoff;
                        } else if(categoryValue < (tempWeaponsChance + tempArmorChance + tempFoodChance + tempSpecialChance)) {
                            category = Chests.ItemCategory.SPECIAL;
                            tempSpecialChance -= specialDropoff;
                        }

                        File arenaFile = new File(plugin.getDataFolder(),"SURVIVAL_GAMES.yml");
                        FileConfiguration arenaFileConfig = YamlConfiguration.loadConfiguration(arenaFile);
                        boolean isMid = false;

                        //for each spawn location, if the chest is +- 10 blocks of the X or Z, then it's an island chest
                        Location pos1 = plugin.getConfig().getLocation("SURVIVAL_GAMES.pos1");
                        Location pos2 = plugin.getConfig().getLocation("SURVIVAL_GAMES.pos2");
                        int centerX = (int)(pos1.getX() + pos2.getX())/2;
                        int centerZ = (int)(pos1.getZ() + pos2.getZ())/2;
                        Location chestLoc = block.getLocation();

                        if(Math.abs(centerX - chestLoc.getX()) <= 10 && Math.abs(centerZ - chestLoc.getZ()) <= 10) isMid = true;


                        ItemStack item;
                        ArrayList<ItemStack> materialList;
                        if(isMid) {
                            //if (quality <= 2) materialList = itemMap.get(category).get(1); //quality 1, 0-2
                            if (quality <= 3) materialList = itemMap.get(category).get(2); //quality 2, 0-3 (4)
                            else if (quality <= 6) materialList = itemMap.get(category).get(3); //quality 3, 4-6 (3)
                            else if (quality <= 10) materialList = itemMap.get(category).get(4); //quality 4, 7-10 (3)
                            else materialList = itemMap.get(category).get(5); //quality 5, 11-14 (4)
                        } else {
                            if (quality <= 4) materialList = itemMap.get(category).get(1); //quality 1, 0-4 (5)
                            else if (quality <= 8) materialList = itemMap.get(category).get(2); //quality 2, 5-8 (4)
                            else if (quality <= 11) materialList = itemMap.get(category).get(3); //quality 3, 9-11 (3)
                            else if (quality <= 13) materialList = itemMap.get(category).get(4); //quality 4, 12-13 (2)
                            else materialList = itemMap.get(category).get(5); //quality 5, 14 (1)
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
                    }
                }
            },counter/2);
            counter++;
        }
    }
}
