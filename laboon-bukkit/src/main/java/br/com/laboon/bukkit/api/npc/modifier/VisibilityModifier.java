package br.com.laboon.bukkit.api.npc.modifier;

import br.com.laboon.bukkit.api.npc.NPC;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public final class VisibilityModifier extends NPCModifier {

    public VisibilityModifier(NPC npc) {
        super(npc);
    }

    public VisibilityModifier queuePlayerListAdd() {

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);

        packet.getModifier().writeDefaults();

        packet.getPlayerInfoActions().write(0, EnumSet.of(EnumWrappers.PlayerInfoAction.ADD_PLAYER, EnumWrappers.PlayerInfoAction.UPDATE_LISTED));

        WrappedGameProfile profile = npc.getProfile();

        PlayerInfoData data = new PlayerInfoData(profile.getUUID(), 0, true, EnumWrappers.NativeGameMode.SURVIVAL, profile, WrappedChatComponent.fromText(profile.getName()));

        packet.getPlayerInfoDataLists().write(1, Collections.singletonList(data));

        addPacket(packet);

        return this;
    }

    public VisibilityModifier queuePlayerListRemove() {

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.PLAYER_INFO_REMOVE);

        packet.getModifier().writeDefaults();

        packet.getUUIDLists().write(0, List.of(npc.getProfile().getUUID()));

        addPacket(packet);

        return this;
    }

    public VisibilityModifier queueSpawn() {

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);

        packet.getModifier().writeDefaults();

        packet.getIntegers().write(0, npc.getEntityId());

        packet.getUUIDs().write(0, npc.getProfile().getUUID());

        packet.getEntityTypeModifier().write(0, org.bukkit.entity.EntityType.PLAYER);

        packet.getDoubles().write(0, npc.getLocation().getX()).write(1, npc.getLocation().getY()).write(2, npc.getLocation().getZ());

        byte pitch = (byte) (npc.getLocation().getPitch() * 256F / 360F);

        byte yaw = (byte) (npc.getLocation().getYaw() * 256F / 360F);

        packet.getBytes().write(0, pitch).write(1, yaw).write(2, yaw);

        addPacket(packet);

        return this;
    }

    public VisibilityModifier queueDestroy() {

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);

        packet.getModifier().writeDefaults();

        packet.getIntegerArrays().write(0, new int[]{npc.getEntityId()});

        addPacket(packet);

        return this;
    }

    public void spawn(Player player) {

        queuePlayerListAdd().send(player);

        queueSpawn().send(player);
    }
}