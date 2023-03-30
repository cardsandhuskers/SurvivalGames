package io.github.cardsandhuskers.survivalgames.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ItemClickListener implements Listener {

    @EventHandler
    public void onItemClick(PlayerInteractEvent e) {
        if(e.getItem() != null && e.getItem().getType() == Material.MUSHROOM_STEW) {
            Player p = e.getPlayer();

            ItemStack air = new ItemStack(Material.AIR);
            p.getEquipment().setItemInMainHand(air);

            p.setSaturation(p.getSaturation() + 7);
            p.setFoodLevel(p.getFoodLevel() + 6);
            PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 300, 1);
            PotionEffect regen = new PotionEffect(PotionEffectType.REGENERATION, 100, 2);
            p.addPotionEffect(speed);
            p.addPotionEffect(regen);

        }
    }

}
