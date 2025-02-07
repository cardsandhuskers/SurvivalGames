package io.github.cardsandhuskers.survivalgames.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import static org.bukkit.Material.AIR;
import static org.bukkit.Material.SNOWBALL;


/**
 * This class allows for stacking snowballs up to size 64
 * Currently does not work!
 */
public class InventoryClickListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        ItemStack target = e.getCurrentItem();
        ItemStack held = e.getCursor();
        
        if(target != null) System.out.println(target.getType());
        if(held!=null)System.out.println(held.getType());

        if(held != null && held.getType() == Material.SNOWBALL) {


            ItemStack currentItem = e.getCurrentItem();
            ItemStack cursorItem = e.getCursor();

            // Ensure both the cursor and current item are not null and are of the same type
            if (currentItem != null && cursorItem != null && currentItem.getType() == cursorItem.getType()) {
                // Optional: Add conditions for items you want to allow stacking (e.g., only certain items)

                // Make sure the item is a snowball
                if (currentItem.getType() != SNOWBALL) {
                    return;
                }

                // Check if the items have the same item meta (like durability, enchantments, etc.)
                if (currentItem.isSimilar(cursorItem)) {
                    // Combine the stack sizes
                    int combinedAmount = currentItem.getAmount() + cursorItem.getAmount();
                    int maxStackSize = 64;//currentItem.getMaxStackSize();

                    if (combinedAmount <= maxStackSize) {
                        currentItem.setAmount(combinedAmount);
                        e.setCursor(null); // Clear the cursor
                    } else {
                        currentItem.setAmount(maxStackSize);
                        cursorItem.setAmount(combinedAmount - maxStackSize);
                        e.setCursor(cursorItem);
                    }

                    e.setCancelled(true); // Cancel the event to prevent default behavior
                }
            }
        }

    }
}
