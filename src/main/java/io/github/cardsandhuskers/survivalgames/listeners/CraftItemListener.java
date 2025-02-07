package io.github.cardsandhuskers.survivalgames.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class CraftItemListener implements Listener {

    @EventHandler
    public void onCraft(PrepareItemCraftEvent e) {
        System.out.println("EVENT");

        if(e.getRecipe() != null && e.getRecipe().getResult().getType() == Material.SHIELD) {

            ItemStack stack = new ItemStack(Material.SHIELD);
            ItemMeta shieldMeta = stack.getItemMeta();
            Damageable shieldDamageable = (Damageable) shieldMeta;
            shieldDamageable.setDamage(327);
            stack.setItemMeta(shieldMeta);

            e.getInventory().setResult(stack);
        }
        if(e.getRecipe() != null && e.getRecipe().getResult().getType() == Material.FLINT_AND_STEEL) {

            ItemStack stack = new ItemStack(Material.FLINT_AND_STEEL);
            ItemMeta flintMeta = stack.getItemMeta();
            Damageable flintDamageable = (Damageable) flintMeta;
            flintDamageable.setDamage(55);
            stack.setItemMeta(flintDamageable);

            e.getInventory().setResult(stack);
        }

        if(e.getRecipe() != null && e.getRecipe().getResult().getType() == Material.MUSHROOM_STEW) {
            ItemStack stack = new ItemStack(Material.MUSHROOM_STEW);
            ItemMeta stewMeta = stack.getItemMeta();
            stewMeta.setDisplayName("Speed Soup");
            stewMeta.setLore(Collections.singletonList("Gives 15 seconds of speed, plus health!"));

            stack.setItemMeta(stewMeta);

            e.getInventory().setResult(stack);
        }
    }

}
