package io.github.cardsandhuskers.survivalgames.handlers;

import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;

public class AttackerTimersHandler implements Runnable{
    private SurvivalGames plugin;
    private HashMap<Player, Player> storedAttackers;
    private HashMap<Player, Integer> attackerTimers;
    // Our scheduled task's assigned id, needed for canceling
    private Integer assignedTaskId;

    public AttackerTimersHandler(SurvivalGames plugin, HashMap<Player, Player> storedAttackers, HashMap<Player, Integer> attackerTimers) {
        this.plugin = plugin;
        this.storedAttackers = storedAttackers;
        this.attackerTimers = attackerTimers;
    }

    @Override
    public void run() {
        //System.out.println(attackerTimers);
        //System.out.println(storedAttackers);
        ArrayList<Player> removablePlayers = new ArrayList<>();
        for(Player p: attackerTimers.keySet()) {
            int time = attackerTimers.get(p) + 1;
            attackerTimers.put(p, time);
            if(time > 20) {
                removablePlayers.add(p);
            }
        }
        for(Player p:removablePlayers) {
            attackerTimers.remove(p);
            storedAttackers.remove(p);
        }
    }

    /**
     * Stop the repeating task
     */
    public void cancelOperation() {
        if (assignedTaskId != null) Bukkit.getScheduler().cancelTask(assignedTaskId);
    }


    /**
     * Schedules this instance to "run" every tick
     */
    public void startOperation() {
        // Initialize our assigned task's id, for later use so we can cancel
        this.assignedTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 0L, 20L);
    }
}
