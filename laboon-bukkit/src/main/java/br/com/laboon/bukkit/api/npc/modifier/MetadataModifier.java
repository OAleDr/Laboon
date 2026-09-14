package br.com.laboon.bukkit.api.npc.modifier;

import br.com.laboon.bukkit.api.npc.NPC;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

import org.bukkit.entity.Player;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public final class MetadataModifier extends NPCModifier {

    private static final int SKIN_LAYERS_INDEX = 15;

    private static final byte ALL_SKIN_LAYERS = 0x7F;

    private final List<WrappedDataValue> metadata =
            new ArrayList<>();

    public MetadataModifier(NPC npc) {
        super(npc);
    }

    public MetadataModifier queue(
            int index,
            Object value
    ) {

        if (value == null) {
            throw new IllegalArgumentException(
                    "Valor de metadata não pode ser nulo."
            );
        }

        Type byteType = Byte.class;

        WrappedDataWatcher.Serializer serializer =
                WrappedDataWatcher.Registry.get(
                        byteType
                );

        if (serializer == null) {
            throw new IllegalArgumentException(
                    "Nenhum serializer ProtocolLib encontrado para "
                            + value.getClass().getName()
            );
        }

        metadata.add(
                new WrappedDataValue(
                        index,
                        serializer,
                        value
                )
        );

        return this;
    }

    public MetadataModifier queue(
            WrappedDataWatcher.WrappedDataWatcherObject object,
            Object value
    ) {

        if (object == null) {
            throw new IllegalArgumentException(
                    "DataWatcherObject não pode ser nulo."
            );
        }

        metadata.add(
                new WrappedDataValue(
                        object.getIndex(),
                        object.getSerializer(),
                        value
                )
        );

        return this;
    }

    public MetadataModifier queue(
            WrappedDataValue value
    ) {

        if (value != null) {
            metadata.add(value);
        }

        return this;
    }

    public MetadataModifier queueSkinLayers(
            boolean enabled
    ) {

        Type byteType = Byte.class;

        WrappedDataWatcher.Serializer serializer =
                WrappedDataWatcher.Registry.get(
                        byteType
                );

        metadata.add(
                new WrappedDataValue(
                        SKIN_LAYERS_INDEX,
                        serializer,
                        (byte) (
                                enabled
                                        ? ALL_SKIN_LAYERS
                                        : 0
                        )
                )
        );

        return this;
    }

    @Override
    public void send(Player... players) {

        if (metadata.isEmpty()) {
            return;
        }

        PacketContainer packet =
                new PacketContainer(
                        PacketType.Play.Server.ENTITY_METADATA
                );

        packet.getModifier()
                .writeDefaults();

        packet.getIntegers()
                .write(
                        0,
                        npc.getEntityId()
                );

        packet.getDataValueCollectionModifier()
                .write(
                        0,
                        metadata
                );

        addPacket(packet);

        super.send(players);

        metadata.clear();
    }
}