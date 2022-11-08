package io.github.cardsandhuskers.survivalgames.commands;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
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
            Location location = p.getLocation();

            plugin.getConfig().set("spawnPoint", location);
            plugin.saveConfig();
            p.sendMessage("Spawn point set at " + location.toString());

        } else {
            System.out.println("Either You are not opped or you're the console. Either way, you can't do this");
        }



        return false;
    }
}
