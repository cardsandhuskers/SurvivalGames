package io.github.cardsandhuskers.survivalgames.handlers;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import io.github.cardsandhuskers.teams.objects.Team;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.*;

public class PlayerDeathHandler {
    private final ArrayList<Player> playerList;
    private final ArrayList<Team> teamList;
    public static int numTeams;
    public static int numPlayers;
    SurvivalGames plugin;
    GameStageHandler gameStageHandler;
    public PlayerDeathHandler(SurvivalGames plugin, GameStageHandler gameStageHandler, ArrayList<Team> teamList) {
        this.plugin = plugin;
        this.gameStageHandler = gameStageHandler;
        this.teamList = teamList;

        playerList = new ArrayList<>();

        //initialize list
        for(Team t:handler.getTeams()) {
            for(Player p:t.getOnlinePlayers()) {
                playerList.add(p);
            }
            teamList.add(t);
        }
        numPlayers = playerList.size();
        numTeams = teamList.size();
    }

    public void onPlayerDeath(Player p) {
        Team t = handler.getPlayerTeam(p);
        playerList.remove(p);
        boolean found = false;
        for(Player player:playerList) {
            Team team = handler.getPlayerTeam(player);
            if(team != null && team.equals(t)) {
                found = true;
            }
        }
        if(!found) {
            teamList.remove(t);
        }
        numPlayers = playerList.size();
        numTeams = teamList.size();
        if(teamList.size() == 2) {
            if(gameState != State.GAME_STARTING && gameType == GameType.SURVIVAL_GAMES) {
                gameStageHandler.startDeathmatch();
            }
        }
        if(teamList.size() <= 1) {
            gameStageHandler.endGame();
            GameEndHandler gameEndHandler = new GameEndHandler(plugin, teamList);
            gameEndHandler.gameEndTimer(gameStageHandler.glowPacketListener);

        }
        Inventory playerInv = p.getInventory();
        ItemStack[] playerItems = playerInv.getContents();
        for(int i = 0; i < playerItems.length; i++) {
            ItemStack item = playerItems[i];
            if(item != null && item.getType() != Material.AIR) {
                World world = p.getWorld();
                world.dropItemNaturally(p.getLocation(), item);
            }

        }

        playerInv.clear();
        //p.setGameMode(GameMode.SPECTATOR);
        //give survival points to everyone alive
        for(Player player:playerList) {
            handler.getPlayerTeam(player).addTempPoints(player, plugin.getConfig().getDouble(gameType + ".survivalPoints") * multiplier);
            //ppAPI.give(player.getUniqueId(), (int)(plugin.getConfig().getInt(gameType + ".survivalPoints") * multiplier));
        }

        p.setGameMode(GameMode.SPECTATOR);
    }

    /**
     * return whether the player is still alive
     * @param p
     * @return boolean alive state
     */
    public boolean isPlayerAlive(Player p) {
        return playerList.contains(p);
        /*
        for(Player player:playerList) {
            if(p.equals(player)) {
                return true;
            }
        }
        return false;
         */
    }
    public void addPlayer(Player p) {
        playerList.add(p);
        Team t = handler.getPlayerTeam(p);
        if(!teamList.contains(t)) {
            teamList.add(t);
        }
        numPlayers = playerList.size();
        numTeams = teamList.size();
    }
}
