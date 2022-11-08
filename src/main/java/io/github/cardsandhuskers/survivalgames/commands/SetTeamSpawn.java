package io.github.cardsandhuskers.survivalgames.commands;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class SetTeamSpawn implements CommandExecutor {
    SurvivalGames plugin;

    public SetTeamSpawn(SurvivalGames plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player p) {
            if(args.length > 0) {
                try {
                    int team = Integer.parseInt(args[0]);
                    saveLocation(p.getLocation(), team);
                    p.sendMessage("Team " + team + " saved at: " + p.getLocation());
                } catch(Exception e) {
                    p.sendMessage(ChatColor.RED + "ERROR: Argument must be an integer");
                }
            } else {
                p.sendMessage(ChatColor.RED + "ERROR: Team Must be Specified");
            }
        } else {
            System.out.println(ChatColor.RED + "ERROR: Cannot be run From Console");
        }
        return true;
    }

    private void saveLocation(Location l, int team) {
        File arenaFile = new File(Bukkit.getServer().getPluginManager().getPlugin("SurvivalGames").getDataFolder(), "arena.yml");
        if (!arenaFile.exists()) {
            try {
                System.out.println("MAKING FILE");
                arenaFile.createNewFile();
            } catch (IOException e) {
                System.out.println("ERROR CREATING FILE");
            }
        }
        FileConfiguration arenaFileConfig = YamlConfiguration.loadConfiguration(arenaFile);
        arenaFileConfig.set("teamSpawn." + team, l);
        try {
            arenaFileConfig.save(arenaFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
