package io.github.cardsandhuskers.survivalgames.objects;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.gameType;

public class Chests {
    private ArrayList<Block> chestList;
    private HashMap<ItemCategory, HashMap<Integer, ArrayList<ItemStack>>> itemMap;
    private final SurvivalGames plugin;
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
        File arenaFile = new File(Bukkit.getServer().getPluginManager().getPlugin("SurvivalGames").getDataFolder(),gameType + ".yml");
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
     * Fills the hashmap with all the items in the loot table based on category and level
     */
    private void buildItemLists() {
        itemMap = new HashMap<>();
        //for each category
        for(int a = 0; a < ItemCategory.values().length; a++) {
            ItemCategory category = ItemCategory.values()[a];
            //for all 5 levels
            HashMap<Integer, ArrayList<ItemStack>> innerMap = new HashMap<>();
            for (int i = 1; i <= 5; i++) {
                ArrayList<ItemStack> stackArray = new ArrayList<>();
                for (String item : plugin.getConfig().getStringList(gameType + ".chest." + category + ".level" + i)) {
                    String[] splitItem = item.split(",");
                    Material itemMat;
                    int value;
                    try {
                        itemMat = Material.valueOf(splitItem[0].toUpperCase());
                    } catch (Exception e) {
                        System.out.println(splitItem[0].toUpperCase() + " Does not exist.");
                        continue;
                    }

                    if (splitItem.length == 1) {
                        value = 1;
                    } else {
                        try {
                            value = Integer.parseInt(splitItem[1].strip());
                        } catch (Exception e) {
                            value = 1;
                        }

                    }
                    ItemStack stack;
                    switch (itemMat) {
                        case FLINT_AND_STEEL:
                            stack = new ItemStack(itemMat, value);
                            ItemMeta flintMeta = stack.getItemMeta();
                            Damageable flintDamageable = (Damageable) flintMeta;
                            flintDamageable.setDamage(55);
                            stack.setItemMeta(flintMeta);

                            stackArray.add(stack);
                            break;
                        case SHIELD:
                            stack = new ItemStack(itemMat, value);
                            ItemMeta shieldMeta = stack.getItemMeta();
                            Damageable shieldDamageable = (Damageable) shieldMeta;
                            shieldDamageable.setDamage(327);
                            stack.setItemMeta(shieldMeta);

                            stackArray.add(stack);
                            break;
                        case POTION:
                        case SPLASH_POTION:
                            if (splitItem.length == 5) {
                                PotionEffectType potionEffectType;
                                int time;
                                int amplifier;
                                //try getting all elements, if there are any errors, break out
                                try {
                                    potionEffectType = PotionEffectType.getByKey(NamespacedKey.minecraft(splitItem[2].strip().toLowerCase()));
                                    //time is inputted in seconds, but code uses ticks, so *20
                                    time = Integer.parseInt(splitItem[3].strip()) * 20;
                                    amplifier = Integer.parseInt(splitItem[4].strip());
                                    if (potionEffectType == null) {
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
                                name += title.substring(0, 1).toUpperCase() + title.substring(1);
                                name = name.replaceAll("_", " ");

                                potionMeta.setDisplayName(name);
                                potionMeta.addCustomEffect(new PotionEffect(potionEffectType, time, amplifier), true);

                                potion.setItemMeta(potionMeta);
                                stackArray.add(potion);
                            }
                            break;
                        case ENCHANTED_BOOK:
                            if (splitItem.length == 4) {
                                Enchantment enchantment;
                                int level;
                                try {
                                    enchantment = Enchantment.getByKey(NamespacedKey.minecraft(splitItem[2].strip()));
                                    level = Integer.parseInt(splitItem[3].strip());

                                    if (enchantment == null) {
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
                            break;
                        case MUSHROOM_STEW:
                            stack = new ItemStack(itemMat, value);
                            ItemMeta stewMeta = stack.getItemMeta();
                            stewMeta.setDisplayName("Speed Soup");
                            stewMeta.setLore(Collections.singletonList("Gives 15 seconds of speed, plus health!"));


                            stack.setItemMeta(stewMeta);

                            stackArray.add(stack);

                            break;
                        default:
                            stack = new ItemStack(itemMat, value);
                            ItemMeta itemMeta = stack.getItemMeta();
                            for (int j = 2; j < splitItem.length - 1; j += 2) {
                                Enchantment enchantment;
                                int level;

                                try {
                                    enchantment = Enchantment.getByKey(NamespacedKey.minecraft(splitItem[j].strip().toLowerCase()));
                                    level = Integer.parseInt(splitItem[j + 1].strip());

                                    if (enchantment == null) {
                                        continue;
                                    }
                                } catch (Exception e) {
                                    System.out.println("Enchantment: " + splitItem[j].strip() + " or Level: " + splitItem[j + 1].strip() + " does not exist!");
                                    continue;
                                }
                                itemMeta.addEnchant(enchantment, level, true);

                            }
                            stack.setItemMeta(itemMeta);
                            stackArray.add(stack);
                            break;
                    }
                }
                innerMap.put(i, stackArray);
            }
            itemMap.put(category, innerMap);
        }
    }

    /**
     * Item Categories for both gamemodes.
     * Skywars uses all, SG does not use blocks or tools
     */
    public enum ItemCategory {
        WEAPONS,
        ARMOR,
        UTIL,
        BLOCKS,
        FOOD,
        TOOLS,
        SPECIAL
    }

    /**
     * Calls the correct populateChests class based on the game type
     */
    public void populateChests(){
        if(gameType == SurvivalGames.GameType.SURVIVAL_GAMES) {
            SGChests sgChests = new SGChests(plugin);
            sgChests.populateSGChests(chestList, itemMap);
        }
        if(gameType == SurvivalGames.GameType.SKYWARS) {
            SkywarsChests skywarsChests = new SkywarsChests(plugin);
            skywarsChests.populateSkywarsChests(chestList, itemMap);
        }
    }
}
