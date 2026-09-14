package br.com.laboon.bukkit.api.npc.modifier;

import br.com.laboon.bukkit.api.npc.NPC;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.List;

public final class VisibilityModifier
        extends NPCModifier {

    public VisibilityModifier(NPC npc) {
        super(npc);
    }

    public VisibilityModifier queuePlayerListAdd() {

        PacketContainer packet =
                new PacketContainer(
                        PacketType.Play.Server.PLAYER_INFO
                );

        packet.getModifier()
                .writeDefaults();

        packet.getPlayerInfoActions()
                .write(
                        0,
                        EnumSet.of(
                                EnumWrappers.PlayerInfoAction.ADD_PLAYER,
                                EnumWrappers.PlayerInfoAction.UPDATE_LISTED,
                                EnumWrappers.PlayerInfoAction.UPDATE_LATENCY,
                                EnumWrappers.PlayerInfoAction.UPDATE_GAME_MODE,
                                EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME
                        )
                );

        WrappedGameProfile profile =
                npc.getProfile();

        PlayerInfoData data =
                new PlayerInfoData(
                        npc.getUniqueId(),
                        0,
                        true,
                        EnumWrappers.NativeGameMode.SURVIVAL,
                        profile,
                        WrappedChatComponent.fromText(
                                profile.getName()
                        )
                );

        packet.getPlayerInfoDataLists()
                .write(
                        1,
                        List.of(data)
                );

        addPacket(packet);

        return this;
    }

    public VisibilityModifier queuePlayerListRemove() {

        PacketContainer packet =
                new PacketContainer(
                        PacketType.Play.Server.PLAYER_INFO_REMOVE
                );

        packet.getModifier()
                .writeDefaults();

        packet.getUUIDLists()
                .write(
                        0,
                        List.of(
                                npc.getUniqueId()
                        )
                );

        addPacket(packet);

        return this;
    }

    public VisibilityModifier queueSpawn() {

        PacketContainer packet =
                new PacketContainer(
                        PacketType.Play.Server.SPAWN_ENTITY
                );

        packet.getModifier()
                .writeDefaults();

        packet.getIntegers()
                .write(
                        0,
                        npc.getEntityId()
                )
                .write(
                        1,
                        0
                );

        packet.getUUIDs()
                .write(
                        0,
                        npc.getUniqueId()
                );

        packet.getEntityTypeModifier()
                .write(
                        0,
                        EntityType.PLAYER
                );

        var location =
                npc.getLocation();

        if (location == null) {
            throw new IllegalStateException(
                    "NPC não possui localização."
            );
        }

        packet.getDoubles()
                .write(
                        0,
                        location.getX()
                )
                .write(
                        1,
                        location.getY()
                )
                .write(
                        2,
                        location.getZ()
                );

        byte pitch =
                (byte) (
                        location.getPitch()
                                * 256F / 360F
                );

        byte yaw =
                (byte) (
                        location.getYaw()
                                * 256F / 360F
                );

        packet.getBytes()
                .write(0, pitch)
                .write(1, yaw)
                .write(2, yaw);

        addPacket(packet);

        return this;
    }

    public VisibilityModifier queueDestroy() {

        PacketContainer packet =
                new PacketContainer(
                        PacketType.Play.Server.ENTITY_DESTROY
                );

        packet.getModifier()
                .writeDefaults();

        packet.getIntLists()
                .write(
                        0,
                        List.of(
                                npc.getEntityId()
                        )
                );

        addPacket(packet);

        return this;
    }

    public void spawn(Player player) {

        queuePlayerListAdd()
                .send(player);

        queueSpawn()
                .send(player);
    }
}