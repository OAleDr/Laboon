package br.com.laboon.bukkit.api.npc.modifier;

import br.com.laboon.bukkit.api.npc.NPC;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class MetadataModifier extends NPCModifier {

    private final List<WrappedWatchableObject> metadata = new ArrayList<>();

    public MetadataModifier(NPC npc) {
        super(npc);
    }

    public MetadataModifier queue(int index, Object value) {

        metadata.add(new WrappedWatchableObject(index, value));

        return this;
    }

    public MetadataModifier queue(WrappedDataWatcher.WrappedDataWatcherObject object, Object value) {

        metadata.add(new WrappedWatchableObject(object, value));

        return this;
    }

    public MetadataModifier queueSkinLayers(boolean enabled) {

        /*
         * Player skin layers.
         *
         * Index 17 is used by the modern player metadata
         * layout. ProtocolLib translates the watcher type.
         */
        java.lang.reflect.Type type = Byte.class;

        WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.get(type);

        metadata.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(17, serializer), (byte) (enabled ? 0x7F : 0)));

        return this;
    }

    @Override
    public void send(Player... players) {

        if (metadata.isEmpty()) {
            return;
        }

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);

        packet.getModifier().writeDefaults();

        packet.getIntegers().write(0, npc.getEntityId());

        packet.getWatchableCollectionModifier().write(0, metadata);

        addPacket(packet);

        super.send(players);

        metadata.clear();
    }
}