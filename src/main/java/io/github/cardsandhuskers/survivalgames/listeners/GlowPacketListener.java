package io.github.cardsandhuskers.survivalgames.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import io.github.cardsandhuskers.survivalgames.SurvivalGames;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

import static io.github.cardsandhuskers.survivalgames.SurvivalGames.handler;

public class GlowPacketListener {
    SurvivalGames plugin;


    public GlowPacketListener(SurvivalGames plugin) {
        this.plugin = plugin;
    }

    public void addGlow() {
        var protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.ENTITY_METADATA, PacketType.Play.Server.NAMED_ENTITY_SPAWN) {
            @Override
            public void onPacketSending(PacketEvent event) {
                //event.getPlayer() is the player being sent the packet
                //this should be right, finds all players that the packet recipient should be seeing glow
                ArrayList<Player> isGlowing = handler.getPlayerTeam(event.getPlayer()).getOnlinePlayers();
                isGlowing.remove(event.getPlayer());

                //for each player the recipient of the packet should see glowing
                boolean found = false;
                for (Player player:isGlowing) {
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
                        }
                        else {
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
                if(!found) {
                    boolean glowing = false;
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.getEntityId() == event.getPacket().getIntegers().read(0)) {
                            if(p.hasPotionEffect(PotionEffectType.GLOWING)) glowing = true;
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
            }
        });
    }
}
