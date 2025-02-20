package io.github.cardsandhuskers.survivalgames.commands;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.*;

public class CancelGameCommand implements CommandExecutor {
    SurvivalGames plugin;
    StartGameCommand startGameCommand;

    public CancelGameCommand(SurvivalGames plugin, StartGameCommand startGameCommand) {
        this.plugin = plugin;
        this.startGameCommand = startGameCommand;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player p && p.isOp()) {
            cancelGame();
        } else if(sender instanceof Player p) {
            p.sendMessage(ChatColor.RED + "You don't have permission to do this");
        }else {
            cancelGame();
        }
        return true;
    }

    public void cancelGame() {
        gameNumber = 1;
        if(gameState == SurvivalGames.State.GAME_STARTING) {
            startGameCommand.pregameTimer.cancelTimer();
        } else if(gameState == SurvivalGames.State.GAME_OVER) {

        } else {
            startGameCommand.cancelGame();
        }

        Collection<Entity> entityList = plugin.getConfig().getLocation(gameType + ".pos1").getWorld().getEntities();
        for (Entity e : entityList) {
            if (e.getType() != EntityType.PLAYER) e.remove();
        }

        try {
            Bukkit.getScoreboardManager().getMainScoreboard().getObjective("belowNameHP").unregister();
            Bukkit.getScoreboardManager().getMainScoreboard().getObjective("listHP").unregister();
        } catch(Exception e) {}
        HandlerList.unregisterAll(plugin);

        try {
            Location lobby = plugin.getConfig().getLocation("Lobby");
            for (Player p : Bukkit.getOnlinePlayers()) p.teleport(lobby);
        } catch (Exception e) {Bukkit.broadcast(Component.text("Lobby does not exist!"));}


    }
}
