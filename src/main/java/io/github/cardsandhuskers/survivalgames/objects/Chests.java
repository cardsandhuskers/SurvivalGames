package io.github.cardsandhuskers.survivalgames.objects;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class Chests {
    private ArrayList<Block> chestList;
    private HashMap<Integer, ArrayList<ItemStack>> itemMap;
    private SurvivalGames plugin;
    private int minItems;
    private int maxItems;
    public Chests(SurvivalGames plugin) throws IOException {
        this.plugin = plugin;
        getChests();
        buildItemLists();
    }


    /**
     * Gets the chest locations from the arena.yml file
     * @throws IOException
     */
    private void getChests() throws IOException {
        chestList = new ArrayList<>();
        //get the arena.yml file
        File arenaFile = new File(Bukkit.getServer().getPluginManager().getPlugin("SurvivalGames").getDataFolder(),"arena.yml");
        if(!arenaFile.exists()) {
            //if the file does not exist, crash program, since game cannot run without it
            throw new IOException("FILE CANNOT BE FOUND");
        }
        //ArrayList<Block> chestList = new ArrayList<>();
        FileConfiguration arenaFileConfig = YamlConfiguration.loadConfiguration(arenaFile);

        //make sure section exists
        if (Objects.requireNonNull(arenaFileConfig.getConfigurationSection("chests")).getKeys(false) != null
                || !arenaFileConfig.getConfigurationSection("chests").getKeys(false).isEmpty()) {

            for (String s : arenaFileConfig.getConfigurationSection("chests").getKeys(false)) {
                ConfigurationSection t = arenaFileConfig.getConfigurationSection("chests." + s);

                //build location and add it to list
                Location loc = new Location(Bukkit.getWorld(arenaFileConfig.get("world").toString()),
                        t.getDouble("x"), t.getDouble("y"), t.getDouble("z"));

                chestList.add(loc.getBlock());
            }
            //System.out.println(chestList);
        }


    }

    /**
     * Fills the hashmap with the loot table
     */
    private void buildItemLists() {
        itemMap = new HashMap<>();
        //for all 5 levels
        for(int i = 1; i <= 5; i++) {
            ArrayList<ItemStack> stackArray = new ArrayList<>();
            for(String item:plugin.getConfig().getStringList("chest.level" + i)) {
                String[] splitItem = item.split(",");
                Material itemMat;
                int value;
                try {
                    itemMat = Material.valueOf(splitItem[0].toUpperCase());
                } catch (Exception e) {
                    System.out.println(splitItem[0].toUpperCase() + " Does not exist.");
                    continue;
                }

                if(splitItem.length == 1) {
                    value = 1;
                } else {
                    try {
                        value = Integer.parseInt(splitItem[1].strip());
                    } catch (Exception e) {
                        value = 1;
                    }

                }
                //set flint and steel durability to 10
                if(itemMat == Material.FLINT_AND_STEEL) {
                    ItemStack stack = new ItemStack(itemMat, value);
                    ItemMeta flintMeta = stack.getItemMeta();
                    Damageable flintDamageable = (Damageable) flintMeta;
                    flintDamageable.setDamage(55);
                    stack.setItemMeta(flintMeta);

                    stackArray.add(stack);
                } else if(itemMat == Material.SHIELD) {
                    ItemStack stack = new ItemStack(itemMat, value);
                    ItemMeta shieldMeta = stack.getItemMeta();
                    Damageable shieldDamageable = (Damageable) shieldMeta;
                    shieldDamageable.setDamage(306);
                    stack.setItemMeta(shieldMeta);

                    stackArray.add(stack);
                    //potions
                } else if(itemMat == Material.POTION || itemMat == Material.SPLASH_POTION) {
                    //make sure all elements of the string exist (POTION, number, type, time, amplifier)
                    if(splitItem.length == 5) {
                        PotionEffectType potionEffectType;
                        int time;
                        int amplifier;
                        //try getting all elements, if there are any errors, break out
                        try {
                            potionEffectType = PotionEffectType.getByKey(NamespacedKey.minecraft(splitItem[2].strip().toLowerCase()));
                            //time is inputted in seconds, but code uses ticks, so *20
                            time = Integer.parseInt(splitItem[3].strip()) * 20;
                            amplifier = Integer.parseInt(splitItem[4].strip());
                            if(potionEffectType == null) {
                                System.out.println(splitItem[2] + " Does not exist.");
                                continue;
                            }
                        } catch (Exception e) {
                            System.out.println("Error creating item " + splitItem[0]);
                            continue;
                        }

                        ItemStack potion = new ItemStack(itemMat, value);
                        PotionMeta potionMeta = (PotionMeta) potion.getItemMeta();
                        potionMeta.setColor(potionEffectType.getColor());

                        //string work to make the title more A E S T H E T I C
                        String name = "Potion of ";
                        String title = String.valueOf(potionEffectType.getKey());
                        title = title.substring(10);
                        name += title.substring(0,1).toUpperCase() + title.substring(1);
                        name = name.replaceAll("_", " ");

                        potionMeta.setDisplayName(name);
                        potionMeta.addCustomEffect(new PotionEffect(potionEffectType, time, amplifier), true);

                        potion.setItemMeta(potionMeta);
                        stackArray.add(potion);
                    }
                } else if(itemMat == Material.ENCHANTED_BOOK) {
                    if(splitItem.length == 4) {
                        Enchantment enchantment;
                        int level;
                        try {
                            enchantment = Enchantment.getByKey(NamespacedKey.minecraft(splitItem[2].strip()));
                            level = Integer.parseInt(splitItem[3].strip());

                            if(enchantment == null) {
                                continue;
                            }
                        } catch (Exception e) {
                            continue;
                        }
                        ItemStack enchantedBook = new ItemStack(itemMat);
                        EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) enchantedBook.getItemMeta();
                        bookMeta.addStoredEnchant(enchantment, level, true);
                        enchantedBook.setItemMeta(bookMeta);

                        stackArray.add(enchantedBook);
                    }
                    //do this for standard items, so not pots, books, and flint and steel
                } else {
                    ItemStack stack = new ItemStack(itemMat, value);
                    stackArray.add(stack);
                }

                //System.out.println(itemMat + "  " + value + "  " + i);


                //System.out.println(stackArray);
            }
            itemMap.put(i, stackArray);
        }
        //System.out.println(itemMap);
        minItems = plugin.getConfig().getInt("chest.min");
        maxItems = plugin.getConfig().getInt("chest.max");
    }

    /**
     * Populates all of the chests in the arena with the items from the table
     */
    public void populateChests() {
        //empty chests
        int counter = 1;
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
            counter++;
        }

        Random random = new Random();
        counter = 1;
        for(Block block:chestList) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()-> {
                if(block.getType() == Material.CHEST) {
                    int numItems = random.nextInt(maxItems - minItems) + minItems;
                    //System.out.println("numItems: " + numItems);
                    Chest chest = (Chest) block.getState();
                    Inventory chestInv = chest.getInventory();
                    ArrayList<Integer> usedLocations = new ArrayList<>();
                    for (int i = 1; i <= numItems; i++) {
                        int quality = random.nextInt(15);
                        //System.out.println("quality int " + quality);
                        ItemStack item;
                        if (quality <= 4) {
                            //quality 1, 0-4
                            ArrayList<ItemStack> materialList = itemMap.get(1);
                            int chosenItem = random.nextInt(materialList.size());
                            item = materialList.get(chosenItem);
                        } else if (quality <= 8) {
                            //quality 2, 5-8
                            ArrayList<ItemStack> materialList = itemMap.get(2);
                            int chosenItem = random.nextInt(materialList.size());
                            item = materialList.get(chosenItem);
                        } else if (quality <= 11) {
                            //quality 3, 9-11
                            ArrayList<ItemStack> materialList = itemMap.get(3);
                            int chosenItem = random.nextInt(materialList.size());
                            item = materialList.get(chosenItem);
                        } else if (quality <= 13) {
                            //quality 4, 12-13
                            ArrayList<ItemStack> materialList = itemMap.get(4);
                            int chosenItem = random.nextInt(materialList.size());
                            item = materialList.get(chosenItem);
                        } else {
                            //quality 5, 14
                            ArrayList<ItemStack> materialList = itemMap.get(5);
                            int chosenItem = random.nextInt(materialList.size());
                            item = materialList.get(chosenItem);
                        }

                        int slot = random.nextInt(27);
                        while (usedLocations != null && usedLocations.contains(slot)) {
                            slot = random.nextInt(27);
                        }
                        usedLocations.add(slot);
                        //System.out.println("used locations " + usedLocations);

                        chestInv.setItem(slot, item);
                    }
            } else {
                System.out.println("Block is not a chest");
            }
            }, 1L * (counter/50) + 1);
            counter++;
        }
    }
}