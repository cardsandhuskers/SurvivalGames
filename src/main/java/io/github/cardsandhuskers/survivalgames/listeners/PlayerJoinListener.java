package io.github.cardsandhuskers.survivalgames.listeners;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import io.github.cardsandhuskers.survivalgames.handlers.PlayerDeathHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.*;

public class PlayerJoinListener implements Listener {

    private final PlayerDeathHandler playerDeathHandler;
    private final SurvivalGames plugin;

    public PlayerJoinListener(PlayerDeathHandler playerDeathHandler, SurvivalGames plugin) {
        this.plugin = plugin;
        this.playerDeathHandler = playerDeathHandler;
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();


        if(handler.getPlayerTeam(p) != null && gameState == SurvivalGames.State.GAME_STARTING) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()-> {
                Team t = handler.getPlayerTeam(p);
                for(Player player:t.getOnlinePlayers()) {
                    if(!player.equals(p)) {
                        p.teleport(player);
                        break;
                    }
                }
            }, 10L);
                if(!playerDeathHandler.isPlayerAlive(p)) {
                    playerDeathHandler.addPlayer(p);
                }
        } else {
            if(plugin.getConfig().getLocation("spawnPoint") != null) {
                p.teleport(plugin.getConfig().getLocation(gameType + ".spawnPoint"));
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()-> {
                p.setGameMode(GameMode.SPECTATOR);
            }, 10L);
        }


    }

}
