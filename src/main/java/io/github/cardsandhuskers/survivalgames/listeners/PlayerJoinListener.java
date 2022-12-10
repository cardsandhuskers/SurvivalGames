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

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.gameState;
import static io.github.cardsandhuskers.survivalgames.SurvivalGames.handler;

public class PlayerJoinListener implements Listener {

    private PlayerDeathHandler playerDeathHandler;
    private SurvivalGames plugin;

    public PlayerJoinListener(PlayerDeathHandler playerDeathHandler, SurvivalGames plugin) {
        this.plugin = plugin;
        this.playerDeathHandler = playerDeathHandler;
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();


        if(handler.getPlayerTeam(p) != null && gameState == SurvivalGames.State.GAME_STARTING) {
            System.out.println("TESTA");
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
                    System.out.println("TESTB");
                    playerDeathHandler.addPlayer(p);
                }
        } else {

            System.out.println("TESTC");
            if(plugin.getConfig().getLocation("spawnPoint") != null) {
                p.teleport(plugin.getConfig().getLocation("spawnPoint"));
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()-> {
                p.setGameMode(GameMode.SPECTATOR);
            }, 10L);
        }


    }

}
