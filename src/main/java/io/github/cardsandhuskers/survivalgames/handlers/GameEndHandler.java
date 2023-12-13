package io.github.cardsandhuskers.survivalgames.handlers;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import io.github.cardsandhuskers.survivalgames.commands.StartGameCommand;
import io.github.cardsandhuskers.survivalgames.listeners.GlowPacketListener;
import io.github.cardsandhuskers.survivalgames.objects.Countdown;
import io.github.cardsandhuskers.survivalgames.objects.GameMessages;
import io.github.cardsandhuskers.teams.objects.Team;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.meta.FireworkMeta;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.*;
import static io.github.cardsandhuskers.teams.Teams.handler;

public class GameEndHandler {
    private final SurvivalGames plugin;
    private final ArrayList<Team> teamList;
    private Countdown gameEndTimer;

    public GameEndHandler(SurvivalGames plugin, ArrayList<Team> teamList) {
        this.teamList = teamList;
        this.plugin = plugin;
    }

    public void gameEndTimer(GlowPacketListener glowPacketListener) {
        int totalSeconds;

        if(gameType == GameType.SKYWARS && gameNumber == 1) totalSeconds = 10;
        else totalSeconds = plugin.getConfig().getInt(gameType + ".GameEndTime");

        gameEndTimer = new Countdown(plugin,

                totalSeconds,
                //Timer Start
                () -> {

                    if(gameType == GameType.SKYWARS && gameNumber == 1) {
                        //ResetArenaCommand resetArenaCommand = new ResetArenaCommand(plugin);
                        //resetArenaCommand.resetArena(GameType.SKYWARS);
                        Bukkit.broadcastMessage(ChatColor.GRAY + "Resetting Arena for round 2...");
                    }

                    final Team winner;
                    if(!teamList.isEmpty()) {
                        winner = teamList.get(0);
                    } else {
                        winner = handler.getTeam(0);
                    }

                    int numPlayers = winner.getOnlinePlayers().size();
                    int winPoints = plugin.getConfig().getInt(gameType + ".winPoints");

                    for(Player p: winner.getOnlinePlayers()) {
                        winner.addTempPoints(p, (winPoints/numPlayers) * multiplier);
                        //ppAPI.give(p.getUniqueId(), (int) ((winPoints/numPlayers) * multiplier));
                    }

                    gameState = SurvivalGames.State.GAME_OVER;
                    Bukkit.broadcastMessage(GameMessages.getWinnerDescription(winner));

                    //put title on screen
                    for(Player p:Bukkit.getOnlinePlayers()) {
                        p.sendTitle("Winner:", winner.color + winner.getTeamName(), 5, 30, 5);
                    }
                    //fireworks
                    Location pos1 = plugin.getConfig().getLocation(gameType + ".pos1");
                    Location pos2 = plugin.getConfig().getLocation(gameType + ".pos2");
                    int y;
                    if(gameType == GameType.SURVIVAL_GAMES) y = 69;
                    else y = 105;
                    Location spawn = new Location(pos1.getWorld(), (pos1.getX() + pos2.getX())/2, y, (pos1.getZ() + pos2.getZ())/2);

                    Location l1 = new Location(spawn.getWorld(), spawn.getX() + 10, spawn.getY() + 5, spawn.getZ() + 10);
                    Location l2 = new Location(spawn.getWorld(), spawn.getX() + 10, spawn.getY() + 5, spawn.getZ() - 10);
                    Location l3 = new Location(spawn.getWorld(), spawn.getX() -10, spawn.getY() + 5, spawn.getZ() + 10);
                    Location l4 = new Location(spawn.getWorld(), spawn.getX() - 10, spawn.getY() + 5, spawn.getZ() - 10);

                    for(int i = 0; i < 10; i++) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                            Firework firework1 = (Firework) l1.getWorld().spawnEntity(l1, EntityType.FIREWORK);
                            FireworkMeta fireworkMeta = firework1.getFireworkMeta();
                            fireworkMeta.addEffect(FireworkEffect.builder().withColor(winner.translateColor()).flicker(true).build());
                            firework1.setFireworkMeta(fireworkMeta);

                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                                firework1.detonate();
                            }, 30L);


                            Firework firework2 = (Firework) l2.getWorld().spawnEntity(l2, EntityType.FIREWORK);
                            firework2.setFireworkMeta(fireworkMeta);
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                                firework2.detonate();
                            }, 30L);

                            Firework firework3 = (Firework) l3.getWorld().spawnEntity(l3, EntityType.FIREWORK);
                            firework3.setFireworkMeta(fireworkMeta);
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                                firework3.detonate();
                            }, 30L);

                            Firework firework4 = (Firework) l4.getWorld().spawnEntity(l4, EntityType.FIREWORK);
                            firework4.setFireworkMeta(fireworkMeta);
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                                firework4.detonate();
                            }, 30L);

                        }, 20L * i);
                    }
                    //save kills and winning team
                    try {
                        savePoints(winner);
                    } catch (IOException e) {
                        StackTraceElement[] trace = e.getStackTrace();
                        String str = "";
                        for(StackTraceElement element:trace) str += element.toString() + "\n";
                        plugin.getLogger().severe("ERROR Calculating Stats!\n" + str);
                    }

                },

                //Timer End
                () -> {
                    endGame();
                },

                //Each Second
                (t) -> {
                    timeVar = t.getSecondsLeft();

                    if (t.getSecondsLeft() == t.getTotalSeconds() - 2) {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.setGameMode(GameMode.SPECTATOR);
                        }
                    }

                    if (!(gameType == GameType.SKYWARS && gameNumber == 1)) {
                        //show each player their team performance
                        if (t.getSecondsLeft() == t.getTotalSeconds() - 5) GameMessages.announceTopPlayers();
                        if (t.getSecondsLeft() == t.getTotalSeconds() - 10) GameMessages.announceTeamPlayers();
                        if (t.getSecondsLeft() == t.getTotalSeconds() - 15) GameMessages.announceTeamLeaderboard();
                    }
                }
        );
        // Start scheduling, don't use the "run" method unless you want to skip a second
        gameEndTimer.scheduleTimer();
    }

    public void endGame() {
        //removes ALL ENTITIES left behind
        Collection<Entity> entityList = plugin.getConfig().getLocation(gameType + ".pos1").getWorld().getEntities();
        for (Entity e : entityList) {
            if (e.getType() != EntityType.PLAYER) {
                e.remove();
            }
        }

        try {
            Bukkit.getScoreboardManager().getMainScoreboard().getObjective("belowNameHP").unregister();
            Bukkit.getScoreboardManager().getMainScoreboard().getObjective("listHP").unregister();
        } catch(Exception e) {}
        HandlerList.unregisterAll(plugin);
        if (gameType == GameType.SKYWARS && gameNumber == 1) {
            gameNumber++;
            StartGameCommand startGameCommand = new StartGameCommand(plugin);
            startGameCommand.startGame();
        } else {
            gameNumber = 1;
            try {
                Location lobby = plugin.getConfig().getLocation("Lobby");
                for (Player p : Bukkit.getOnlinePlayers()) p.teleport(lobby);
            } catch (Exception e) {Bukkit.broadcastMessage("Lobby does not exist!");}
            //way to execute a command as console
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "startRound");
        }
    }
    public void savePoints(Team winner) throws IOException {

        FileWriter writer = new FileWriter("plugins/SurvivalGames/stats.csv", true);
        FileReader reader = new FileReader("plugins/SurvivalGames/stats.csv");

        String[] headers = {"Event", "Type", "Team", "Name", "Kills"};

        CSVFormat.Builder builder = CSVFormat.Builder.create();
        builder.setHeader(headers);
        CSVFormat format = builder.build();

        CSVParser parser = new CSVParser(reader, format);

        if(!parser.getRecords().isEmpty()) {
            format = CSVFormat.DEFAULT;
        }

        CSVPrinter printer = new CSVPrinter(writer, format);

        int eventNum = Bukkit.getPluginManager().getPlugin("LobbyPlugin").getConfig().getInt("eventNum");
        //printer.printRecord(currentGame);
        for(Player p:playerKills.keySet()) {
            if(p == null) continue;
            if(handler.getPlayerTeam(p) == null) continue;
            printer.printRecord(eventNum, gameType, handler.getPlayerTeam(p).getTeamName(), p.getDisplayName(), playerKills.get(p));
        }
        for(Player p: winner.getOnlinePlayers()) {
            if(p == null) continue;
            if(handler.getPlayerTeam(p) == null) continue;
            printer.printRecord(eventNum, gameType, winner.getTeamName(), p.getDisplayName(), "WINNER-");
        }
        writer.close();
        try {
            plugin.statCalculator.calculateStats();
        } catch (Exception e) {
            StackTraceElement[] trace = e.getStackTrace();
            String str = "";
            for(StackTraceElement element:trace) str += element.toString() + "\n";
            plugin.getLogger().severe("ERROR Calculating Stats!\n" + str);
        }

    }
}
