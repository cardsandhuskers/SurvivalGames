package io.github.cardsandhuskers.survivalgames.objects;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import io.github.cardsandhuskers.teams.objects.Team;
import io.github.cardsandhuskers.teams.objects.TempPointsHolder;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.gameType;
import static io.github.cardsandhuskers.survivalgames.SurvivalGames.multiplier;
import static io.github.cardsandhuskers.teams.Teams.handler;

public class GameMessages {

    public static String getSGDescription() {
        String SURVIVAL_GAMES_DESCRIPTION = ChatColor.STRIKETHROUGH + "----------------------------------------\n" + ChatColor.RESET +
                StringUtils.center(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Survival Games", 30) +
                ChatColor.BLUE + "" + ChatColor.BOLD + "\nHow To Play:" +
                ChatColor.RESET + "\nThis iconic survival games map returns!" +
                "\nWork with your teammates to take down the other teams and be the last one standing!" +
                "\nThe game will start with a 45 second grace period where PvP is disabled." +
                "\nThe worldborder will shrink over time. Don't get caught outside it, you will die." +
                ChatColor.STRIKETHROUGH + "\n----------------------------------------";
        return SURVIVAL_GAMES_DESCRIPTION;
    }

    public static String getSkywarsDescription() {
        String SKYWARS_DESCRIPTION = ChatColor.STRIKETHROUGH + "----------------------------------------\n" + ChatColor.RESET +
                StringUtils.center(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Skywars", 30) +
                ChatColor.BLUE + "" + ChatColor.BOLD + "\nHow To Play:" +
                ChatColor.RESET + "\nThere will be 2 rounds of Skywars on the famous Hypixel map: Elven" +
                "\nWork with your teammates to take down the other teams and be the last one standing!" +
                "\nEach island has chests to get geared up, while the middle has many chests with even better loot!" +
                "\nBe careful not to fall off the edge! The void will kill you." +
                ChatColor.STRIKETHROUGH + "\n----------------------------------------";
        return SKYWARS_DESCRIPTION;
    }

    public static String getPointsDescription(SurvivalGames plugin) {
        String POINTS_DESCRIPTION = ChatColor.STRIKETHROUGH + "----------------------------------------" +
                ChatColor.GOLD + "" + ChatColor.BOLD + "\nHow is the game Scored:" +
                ChatColor.RESET + "\nFor winning: " + ChatColor.GOLD + (int) (plugin.getConfig().getInt(gameType + ".winPoints") * multiplier) + ChatColor.RESET + " points divided among the team members" +
                "\nFor Each Kill: " + ChatColor.GOLD + (int) (plugin.getConfig().getInt(gameType + ".killPoints") * multiplier) + ChatColor.RESET + " points" +
                "\nFor each player you outlive: " + ChatColor.GOLD + (plugin.getConfig().getDouble(gameType + ".survivalPoints") * multiplier) + ChatColor.RESET + " points" +
                ChatColor.STRIKETHROUGH + "\n----------------------------------------";
        return POINTS_DESCRIPTION;
    }


    public static String getWinnerDescription(Team winner) {
        String message = winner.color + ChatColor.STRIKETHROUGH + "------------------------------" + ChatColor.RESET +
                        ChatColor.BOLD + "\nWinner:\n" + ChatColor.RESET +
                        winner.color + winner.getTeamName() + "\n" + ChatColor.RESET;
        message += winner.color;
        for(Player p:winner.getOnlinePlayers()) {
            message += "\n    " + p.getDisplayName();
        }
        message += "\n" + ChatColor.STRIKETHROUGH + "------------------------------";
        return message;
    }


    /**
     * Announces the top 5 earning players in the game
     */
    public static void announceTopPlayers() {
        ArrayList<TempPointsHolder> tempPointsList = new ArrayList<>();
        for(Team team: handler.getTeams()) {
            for(Player p:team.getOnlinePlayers()) {
                tempPointsList.add(team.getPlayerTempPoints(p));
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        }

        Collections.sort(tempPointsList, Comparator.comparing(TempPointsHolder::getPoints));
        Collections.reverse(tempPointsList);

        int max;
        if(tempPointsList.size() >= 5) {
            max = 4;
        } else {
            max = tempPointsList.size() - 1;
        }

        Bukkit.broadcastMessage("\n" + ChatColor.RED + "" + ChatColor.BOLD + "Top 5 Players:");
        Bukkit.broadcastMessage(ChatColor.DARK_RED + "------------------------------");
        int number = 1;
        for(int i = 0; i <= max; i++) {
            TempPointsHolder h = tempPointsList.get(i);
            Bukkit.broadcastMessage(number + ". " + handler.getPlayerTeam(h.getPlayer()).color + h.getPlayer().getName() + ChatColor.RESET + "    Points: " +  h.getPoints());
            number++;
        }
        Bukkit.broadcastMessage(ChatColor.DARK_RED + "------------------------------");
    }

    /**
     * Announces the leaderboard for players on your team based on points earned in the game
     */
    public static void announceTeamPlayers() {
        for (Team team : handler.getTeams()) {
            ArrayList<TempPointsHolder> tempPointsList = new ArrayList<>();
            for (Player p : team.getOnlinePlayers()) {
                if (team.getPlayerTempPoints(p) != null) {
                    tempPointsList.add(team.getPlayerTempPoints(p));
                }
            }
            Collections.sort(tempPointsList, Comparator.comparing(TempPointsHolder::getPoints));
            Collections.reverse(tempPointsList);

            for (Player p : team.getOnlinePlayers()) {
                p.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Your Team Standings:");
                p.sendMessage(ChatColor.DARK_BLUE + "------------------------------");
                int number = 1;
                for (TempPointsHolder h : tempPointsList) {
                    p.sendMessage(number + ". " + handler.getPlayerTeam(p).color + h.getPlayer().getName() + ChatColor.RESET + "    Points: " + h.getPoints());
                    number++;
                }
                p.sendMessage(ChatColor.DARK_BLUE + "------------------------------\n");
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        }
    }

    /**
     * Announces the leaderboard of teams based on points earned in the game
     */
    public static void announceTeamLeaderboard() {
        ArrayList<Team> teamList = handler.getTeams();
        Collections.sort(teamList, Comparator.comparing(Team::getTempPoints));
        Collections.reverse(teamList);

        Bukkit.broadcastMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "Team Leaderboard:");
        Bukkit.broadcastMessage(ChatColor.GREEN + "------------------------------");
        int counter = 1;
        for(Team team:teamList) {
            Bukkit.broadcastMessage(counter + ". " + team.color + ChatColor.BOLD +  team.getTeamName() + ChatColor.RESET + " Points: " + team.getTempPoints());
            counter++;
        }
        Bukkit.broadcastMessage(ChatColor.GREEN + "------------------------------");
        for(Player p: Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
        }
    }
}
