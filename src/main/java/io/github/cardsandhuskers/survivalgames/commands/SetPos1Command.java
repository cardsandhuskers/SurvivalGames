package io.github.cardsandhuskers.survivalgames.commands;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetPos1Command implements CommandExecutor {
    private final SurvivalGames plugin;
    public SetPos1Command(SurvivalGames plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player p && p.isOp()) {
            if(args.length > 0) {
                SurvivalGames.GameType game;
                try {
                    game = SurvivalGames.GameType.valueOf(args[0].toUpperCase());
                } catch (Exception e) {
                    p.sendMessage(ChatColor.RED + "ERROR: game type must be SURVIVAL_GAMES or SKYWARS");
                    return false;
                }

                Location location = p.getLocation();

                plugin.getConfig().set(game + ".pos1", location);
                plugin.saveConfig();
                p.sendMessage(game + " Location 1 Set at: " + location);
            } else {
                return false;
            }
        } else if(sender instanceof Player p) {
            p.sendMessage(ChatColor.RED + "ERROR: You do not have sufficient permission to do this");
        } else {
            System.out.println(ChatColor.RED + "ERROR: Cannot run from console");
        }

        return true;
    }
}
