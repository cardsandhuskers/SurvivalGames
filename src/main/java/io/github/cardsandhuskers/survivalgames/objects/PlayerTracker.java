package io.github.cardsandhuskers.survivalgames.objects;

import io.github.cardsandhuskers.survivalgames.handlers.PlayerDeathHandler;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.CompassMeta;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.handler;

public class PlayerTracker {
    Player owner;
    PlayerDeathHandler playerDeathHandler;
    public PlayerTracker(PlayerDeathHandler playerDeathHandler, Player p) {
        owner = p;
        this.playerDeathHandler = playerDeathHandler;
    }

    public void giveCompass() {
        Inventory inv = owner.getInventory();
        ItemStack compass = new ItemStack(Material.COMPASS);

        CompassMeta compassMeta = (CompassMeta) compass.getItemMeta();
        compassMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Player Tracker");

        compass.setItemMeta(compassMeta);

        inv.setItem(8,compass);
    }

    public void updateLocation() {
        Inventory inv = owner.getInventory();
        for(ItemStack i :inv.getContents()) {
            if(i != null && i.getType() == Material.COMPASS) {
                Player p = calcNearestPlayer();
                if(p != null) {
                    owner.setCompassTarget(p.getLocation());
                    sendText();
                }
            }
        }

    }

    private void sendText() {
        PlayerInventory inv = owner.getInventory();
        if(inv.getItemInMainHand().getType() == Material.COMPASS) {
            Player player = calcNearestPlayer();
            owner.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "" + ChatColor.BOLD + player.getDisplayName() + "  " + ChatColor.RESET + ChatColor.YELLOW + String.format("%,.1f", calcDistance(player)) + " blocks away."));
        }
    }

    private Player calcNearestPlayer() {
        Location l = owner.getLocation();
        Player nearest = null;
        for(Player player: Bukkit.getOnlinePlayers()) {
            if(handler.getPlayerTeam(player) != null && !handler.getPlayerTeam(player).equals(handler.getPlayerTeam(owner)) && playerDeathHandler.isPlayerAlive(player)) {
                //System.out.println("Player: " + player + " Owner: " + owner);
                Location l2 = player.getLocation();
                double dist = l.distance(l2);
                if(nearest != null && dist < l.distance(nearest.getLocation())) {
                    //System.out.println("NULL");
                    nearest = player;
                } else if(nearest == null) {
                    //System.out.println("PLAYER");
                    nearest = player;
                }
            }
        }
        return nearest;
    }
    private double calcDistance(Player p1) {
        return owner.getLocation().distance(p1.getLocation());
    }
}
