package io.github.cardsandhuskers.survivalgames.objects;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import io.github.cardsandhuskers.teams.objects.TempPointsHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.gameType;
import static io.github.cardsandhuskers.survivalgames.SurvivalGames.multiplier;
import static io.github.cardsandhuskers.teams.Teams.handler;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;
import static net.kyori.adventure.text.format.TextDecoration.STRIKETHROUGH;

public class GameMessages {

    private static final Map<String, NamedTextColor> COLOR_MAP = new HashMap<>();

    static {
        COLOR_MAP.put("&0", NamedTextColor.BLACK);
        COLOR_MAP.put("&1", NamedTextColor.DARK_BLUE);
        COLOR_MAP.put("&2", NamedTextColor.DARK_GREEN);
        COLOR_MAP.put("&3", NamedTextColor.DARK_AQUA);
        COLOR_MAP.put("&4", NamedTextColor.DARK_RED);
        COLOR_MAP.put("&5", NamedTextColor.DARK_PURPLE);
        COLOR_MAP.put("&6", NamedTextColor.GOLD);
        COLOR_MAP.put("&7", NamedTextColor.GRAY);
        COLOR_MAP.put("&8", NamedTextColor.DARK_GRAY);
        COLOR_MAP.put("&9", NamedTextColor.BLUE);
        COLOR_MAP.put("&a", NamedTextColor.GREEN);
        COLOR_MAP.put("&b", NamedTextColor.AQUA);
        COLOR_MAP.put("&c", NamedTextColor.RED);
        COLOR_MAP.put("&d", LIGHT_PURPLE);
        COLOR_MAP.put("&e", NamedTextColor.YELLOW);
        COLOR_MAP.put("&f", WHITE);
    }

    public static Component getSGDescription() {
        return Component.text()
                .append(Component.text("----------------------------------------\n", WHITE, STRIKETHROUGH))
                .append(Component.text(StringUtils.center("Survival Games", 40), LIGHT_PURPLE, BOLD))
                .append(Component.text("\nHow To Play:", BLUE, BOLD))
                .append(Component.text("""
                        \nWork with your teammates to hunt down the other teams and be the last one standing!
                        The game will start with a 45 second grace period where PvP is disabled.
                        The worldborder will shrink over time. Don't get caught outside it, you will die."""))
                .append(Component.text("\n----------------------------------------", WHITE, STRIKETHROUGH))
                .build();
    }

    public static Component getSkywarsDescription() {
        return Component.text()
                .append(Component.text("----------------------------------------\n", WHITE, STRIKETHROUGH))
                .append(Component.text(StringUtils.center("Skywars", 40), LIGHT_PURPLE, BOLD))
                .append(Component.text("\nHow To Play:", BLUE, BOLD))
                .append(Component.text("""
                        \nThere will be 2 rounds of Skywars on our custom map: Time Traveler
                        Work with your teammates to take down the other teams and be the last one standing!
                        Each island has chests to get geared up, while the middle has many chests with even better loot!
                        The border will begin shrinking after the chest refill.
                        The border shrinks by crumbling.
                        Be careful not to fall off the edge! The void will kill you."""))
                .append(Component.text("\n----------------------------------------", WHITE, STRIKETHROUGH))
                .build();
    }

    public static Component getPointsDescription(SurvivalGames plugin) {

        int winPoints = (int) (plugin.getConfig().getInt(gameType + ".winPoints") * multiplier);
        int killPoints = (int) (plugin.getConfig().getInt(gameType + ".killPoints") * multiplier);
        double survivalPoints = (plugin.getConfig().getDouble(gameType + ".survivalPoints") * multiplier);

        return Component.text()
                .append(Component.text("----------------------------------------\n", WHITE, STRIKETHROUGH))
                .append(Component.text("How is the game Scored:\n", GOLD, BOLD))
                .append(Component.text("For winning: ")).append(Component.text(winPoints, GOLD)).append(Component.text(" points divided among the team members"))
                .append(Component.text("\nFor Each Kill: ")).append(Component.text(killPoints, GOLD)).append(Component.text(" points"))
                .append(Component.text("\nFor each player you outlive: ")).append(Component.text(survivalPoints, GOLD)).append(Component.text(" points"))
                .append(Component.text("\n----------------------------------------", WHITE, STRIKETHROUGH))
                .build();
    }


    public static Component getWinnerDescription(Team winner) {
        NamedTextColor color = COLOR_MAP.get(winner.getConfigColor());

        TextComponent.Builder builder = Component.text()
                .append(Component.text("------------------------------\n", color, STRIKETHROUGH))
                .append(Component.text(StringUtils.center("Winner:", 40) + "\n", WHITE, BOLD))
                .append(Component.text(StringUtils.center(winner.getTeamName(), 40), color))
                .append(Component.text("\nMembers:", WHITE, BOLD));

        for(Player p:winner.getOnlinePlayers()) {
            builder.append(Component.text("\n  " + p.getName(), color));
        }
        builder.append(Component.text("\n------------------------------", color, STRIKETHROUGH));

        return builder.build();
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

        tempPointsList.sort(Comparator.comparing(TempPointsHolder::getPoints));
        Collections.reverse(tempPointsList);

        int max;
        if(tempPointsList.size() >= 5) {
            max = 4;
        } else {
            max = tempPointsList.size() - 1;
        }

        TextComponent.Builder builder = Component.text()
                .append(Component.text("\nTop 5 Players:", RED, BOLD))
                .append(Component.text("\n------------------------------", DARK_RED));

        int number = 1;
        for(int i = 0; i <= max; i++) {
            TempPointsHolder h = tempPointsList.get(i);
            Team team = TeamHandler.getInstance().getPlayerTeam(h.getPlayer());

            builder.append(Component.text("\n" + number + ". "))
                    .append(Component.text(h.getPlayer().getName(), COLOR_MAP.get(team.getConfigColor()), BOLD))
                    .append(Component.text(" Points: " + h.getPoints()));
            number++;
        }
        builder.append(Component.text("\n------------------------------", DARK_RED));
        Bukkit.broadcast(builder.build());
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
            tempPointsList.sort(Comparator.comparing(TempPointsHolder::getPoints));
            Collections.reverse(tempPointsList);
            NamedTextColor teamColor = COLOR_MAP.get(team.getConfigColor());

            TextComponent.Builder builder = Component.text()
                    .append(Component.text("\nYour Team Standings:", teamColor, BOLD))
                    .append(Component.text("\n------------------------------", teamColor));

            int number = 1;
            for (TempPointsHolder h : tempPointsList) {
                builder.append(Component.text("\n" + number + ". "))
                        .append(Component.text(h.getPlayer().getName(), teamColor, BOLD))
                        .append(Component.text(" Points: " + h.getPoints()));
                number++;
            }
            builder.append(Component.text("\n------------------------------", teamColor));


            for (Player p : team.getOnlinePlayers()) {
                p.sendMessage(builder.build());
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        }
    }

    /**
     * Announces the leaderboard of teams based on points earned in the game
     */
    public static void announceTeamLeaderboard() {

        ArrayList<Team> teamList = TeamHandler.getInstance().getTeams();
        teamList.sort(Comparator.comparing(Team::getTempPoints));
        Collections.reverse(teamList);

        TextComponent.Builder builder = Component.text()
                .append(Component.text("\nTeam Leaderboard:", BLUE, BOLD))
                .append(Component.text("\n------------------------------", DARK_BLUE));

        int counter = 1;
        for(Team team:teamList) {
            builder.append(Component.text("\n" + counter + ". "))
                    .append(Component.text(team.getTeamName(), COLOR_MAP.get(team.getConfigColor()), BOLD))
                    .append(Component.text(" Points: " + team.getTempPoints()));

            counter++;
        }
        builder.append(Component.text("\n------------------------------", DARK_BLUE));

        Bukkit.broadcast(builder.build());
        for(Player p: Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
        }
    }
}
