package io.github.cardsandhuskers.survivalgames.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import io.github.cardsandhuskers.teams.objects.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.handler;

/**
 * Class that handles glowing through packets so that only specific players will be glowing for others
 */
public class GlowPacketListener implements Runnable{
    SurvivalGames plugin;
    PacketAdapter glowListener = null;
    private Integer assignedTaskId;


    public GlowPacketListener(SurvivalGames plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles the logic for what players are meant to glow for the player
     * @param p player receiving the packet
     * @return Arraylist - players that glow for the param player
     */
    public ArrayList<Player> getGlows(Player p) {
        ArrayList<Player> isGlowing = handler.getPlayerTeam(p).getOnlinePlayers();
        isGlowing.remove(p);
        return isGlowing;
    }

    /**
     * Sets correct players as glowing and enables the packet listener that will keep them glowing
     */
    public void enableGlow() {
        var protocolManager = ProtocolLibrary.getProtocolManager();

        for(Team t: handler.getTeams()) {
            for(Player p:t.getOnlinePlayers()) {
                ArrayList<Player> isGlowing = getGlows(p);

                for(Player pl:isGlowing) {
                    PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
                    packet.getIntegers().write(0, pl.getEntityId()); //Set packet's entity id
                    WrappedDataWatcher watcher = new WrappedDataWatcher(); //Create data watcher, the Entity Metadata packet requires this
                    WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.get(Byte.class); //Found this through google, needed for some stupid reason
                    watcher.setEntity(pl); //Set the new data watcher's target
                    watcher.setObject(0, serializer, (byte) (0x40)); //Set status to glowing, found on protocol page
                    packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects()); //Make the packet's datawatcher the one we created

                    try {
                        protocolManager.sendServerPacket(p, packet);
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        glowListener = new PacketAdapter(plugin, PacketType.Play.Server.ENTITY_METADATA, PacketType.Play.Server.NAMED_ENTITY_SPAWN) {
            @Override
            public void onPacketSending(PacketEvent event) {
                try {
                    //event.getPlayer() is the player being sent the packet
                    //this should be right, finds all players that the packet recipient should be seeing glow
                    if (handler.getPlayerTeam(event.getPlayer()) == null) return;
                    ArrayList<Player> isGlowing = getGlows(event.getPlayer());
                    System.out.println(event.getPlayer().getDisplayName() + ": " + isGlowing + "\n\n");

                    //for each player the recipient of the packet should see glowing
                    boolean found = false;
                    for (Player player : isGlowing) {
                        //entityID is a unique identifier for each entity

                        //check if player is in the packet
                        if (player.getEntityId() == event.getPacket().getIntegers().read(0)) {
                            found = true;
                            //entityMetadata and EntitySpawn packets work differently
                            if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {
                                List<WrappedWatchableObject> watchableObjectList = event.getPacket().getWatchableCollectionModifier().read(0);
                                for (WrappedWatchableObject metadata : watchableObjectList) {
                                    if (metadata.getIndex() == 0) {
                                        byte b = (byte) metadata.getValue();
                                        b |= 0b01000000;
                                        metadata.setValue(b);
                                    }
                                }
                            } else {
                                WrappedDataWatcher watcher = event.getPacket().getDataWatcherModifier().read(0);
                                if (watcher.hasIndex(0)) {
                                    byte b = watcher.getByte(0);
                                    b |= 0b01000000;
                                    watcher.setObject(0, b);
                                }
                            }
                            break;
                        }
                    }
                    if (!found) {
                        boolean glowing = false;
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p.getEntityId() == event.getPacket().getIntegers().read(0)) {
                                if (p.hasPotionEffect(PotionEffectType.GLOWING)) glowing = true;
                            }
                        }
                        if (!glowing) {
                            if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {
                                List<WrappedWatchableObject> watchableObjectList = event.getPacket().getWatchableCollectionModifier().read(0);
                                for (WrappedWatchableObject metadata : watchableObjectList) {
                                    if (metadata.getIndex() == 0) {
                                        byte b = (byte) metadata.getValue();
                                        b &= ~(1 << 6);
                                        metadata.setValue(b);
                                    }
                                }
                            } else {
                                WrappedDataWatcher watcher = event.getPacket().getDataWatcherModifier().read(0);
                                if (watcher.hasIndex(0)) {
                                    byte b = watcher.getByte(0);
                                    b &= ~(1 << 6);
                                    watcher.setObject(0, b);
                                }
                            }
                        }
                    }
                } catch (Exception e){}
            }
        };

        protocolManager.addPacketListener(glowListener);
    }

    /**
     * Stop all players glowing by disabling the packet listener
     */
    public void disableGlow() {
        var protocolManager = ProtocolLibrary.getProtocolManager();
        if(glowListener != null) protocolManager.removePacketListener(glowListener);

        for(Team t: handler.getTeams()) {
            for(Player p:t.getOnlinePlayers()) {
                ArrayList<Player> isGlowing = getGlows(p);

                for(Player pl:isGlowing) {
                    PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
                    packet.getIntegers().write(0, pl.getEntityId()); //Set packet's entity id
                    WrappedDataWatcher watcher = new WrappedDataWatcher(); //Create data watcher, the Entity Metadata packet requires this
                    WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.get(Byte.class); //Found this through google, needed for some stupid reason
                    watcher.setEntity(pl); //Set the new data watcher's target
                    watcher.setObject(0, serializer, (byte) (0x0)); //Set status to glowing, found on protocol page
                    packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects()); //Make the packet's datawatcher the one we created

                    try {
                        protocolManager.sendServerPacket(p, packet);
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }

            }
        }


    }

    public void sendArtificialGlowPackets() {
        var protocolManager = ProtocolLibrary.getProtocolManager();

        for(Team t: handler.getTeams()) {
            for(Player p:t.getOnlinePlayers()) {
                ArrayList<Player> isGlowing = getGlows(p);

                for(Player pl:isGlowing) {
                    PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
                    packet.getIntegers().write(0, pl.getEntityId()); //Set packet's entity id
                    WrappedDataWatcher watcher = new WrappedDataWatcher(); //Create data watcher, the Entity Metadata packet requires this
                    WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.get(Byte.class); //Found this through google, needed for some stupid reason
                    watcher.setEntity(pl); //Set the new data watcher's target
                    watcher.setObject(0, serializer, (byte) (0x40)); //Set status to glowing, found on protocol page
                    packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects()); //Make the packet's datawatcher the one we created

                    try {
                        protocolManager.sendServerPacket(p, packet);
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    @Override
    public void run() {
        sendArtificialGlowPackets();
    }

    /**
     * Schedules this instance to run every tick
     */
    public void startOperation() {
        // Initialize our assigned task's id, for later use so we can cancel
        this.assignedTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 0L, 1L);
    }
    public void cancelOperation() {
        if (assignedTaskId != null) Bukkit.getScheduler().cancelTask(assignedTaskId);
    }
}
