package io.github.cardsandhuskers.survivalgames.commands;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSpawnPointCommand implements CommandExecutor {
    private final SurvivalGames plugin;
    public SetSpawnPointCommand(SurvivalGames plugin) {
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

                plugin.getConfig().set(game + ".spawnPoint", location);
                plugin.saveConfig();
                p.sendMessage("Spawn point set at " + location);
                return true;
            } else {
                return false;
            }
        } else {
            System.out.println("Either You are not opped or you're the console. Either way, you can't do this");
        }



        return true;
    }
}
