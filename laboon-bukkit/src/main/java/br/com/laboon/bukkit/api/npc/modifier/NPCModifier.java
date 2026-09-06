package br.com.laboon.bukkit.api.npc.modifier;

import br.com.laboon.bukkit.api.npc.NPC;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NPCModifier {

    protected final NPC npc;

    private final List<PacketContainer> packets = new CopyOnWriteArrayList<>();

    protected NPCModifier(NPC npc) {

        if (npc == null) {
            throw new IllegalArgumentException("NPC não pode ser nulo.");
        }

        this.npc = npc;
    }

    protected PacketContainer addPacket(PacketContainer packet) {
        packets.add(packet);
        return packet;
    }

    protected List<PacketContainer> getPackets() {
        return packets;
    }

    public void send() {
        send(Bukkit.getOnlinePlayers().toArray(Player[]::new));
    }

    public void send(Player... players) {

        if (players == null || players.length == 0) {
            packets.clear();
            return;
        }

        for (Player player : players) {

            if (player == null || !player.isOnline()) {
                continue;
            }

            for (PacketContainer packet : packets) {

                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);

            }
        }

        packets.clear();
    }
}