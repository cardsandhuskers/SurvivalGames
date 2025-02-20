package io.github.cardsandhuskers.survivalgames.commands;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.State;
import static io.github.cardsandhuskers.survivalgames.SurvivalGames.gameState;

public class PauseTimerCommand implements CommandExecutor {
    private boolean isPaused = false;
    private SurvivalGames plugin;
    private StartGameCommand startGameCommand;

    public PauseTimerCommand(SurvivalGames plugin, StartGameCommand startGameCommand) {
        this.plugin = plugin;
        this.startGameCommand = startGameCommand;
    }


    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player p && p.isOp()) {
           pauseTimer();
        } else if(sender instanceof Player p) {
            p.sendMessage(Component.text("You don't have permission to do this", NamedTextColor.RED));
        }else {
            pauseTimer();
        }
        return true;
    }

    public void pauseTimer() {
        if(gameState == State.GAME_STARTING) {
            if (isPaused) {
                startGameCommand.pregameTimer.scheduleTimer();
                isPaused = false;
            } else {
                startGameCommand.pregameTimer.cancelTimer();
                isPaused = true;
            }
        }
    }

}
