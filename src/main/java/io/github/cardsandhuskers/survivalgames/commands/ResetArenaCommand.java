package io.github.cardsandhuskers.survivalgames.commands;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.session.ClipboardHolder;
import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ResetArenaCommand implements CommandExecutor {
    private final SurvivalGames plugin;
    public ResetArenaCommand(SurvivalGames plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player p && p.isOp()) {
            if(args.length > 0) {
                SurvivalGames.GameType game;
                try {
                    game = SurvivalGames.GameType.valueOf(args[0].toUpperCase());
                } catch (Exception e) {
                    p.sendMessage(ChatColor.RED + "ERROR: game type must be SURVIVAL_GAMES or SKYWARS");
                    return false;
                }
                resetArena(game);
                return true;
            }
        } else if(sender instanceof Player p) {
            p.sendMessage("No permission to do that");
            return true;
        } else {
            if(args.length > 0) {
                SurvivalGames.GameType game;
                try {
                    game = SurvivalGames.GameType.valueOf(args[0].toUpperCase());
                } catch (Exception e) {
                    System.out.println(ChatColor.RED + "ERROR: game type must be SURVIVAL_GAMES or SKYWARS");
                    return false;
                }
                resetArena(game);
                return true;
            }
        }
        return false;
    }

    public void resetArena(SurvivalGames.GameType game) {

        BukkitWorld weWorld = new BukkitWorld(plugin.getConfig().getLocation(game + ".pos1").getWorld());

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Clipboard clipboard;

            File file = new File("plugins/SurvivalGames/"+ game + ".schem");
            if (!file.exists()) {
                plugin.getLogger().warning("Arena Schematic does not exist! Cannot build arena until it is saved.");
                return;
            }


            ClipboardFormat format = ClipboardFormats.findByFile(file);
            try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
                clipboard = reader.read();

                try (EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld)) {
                    Operation operation = new ClipboardHolder(clipboard)
                            .createPaste(editSession)
                            .to(clipboard.getOrigin())
                            // configure here
                            .build();
                    Operations.complete(operation);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Bukkit.broadcastMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "ARENA RESET COMPLETE");
        });
    }
}
