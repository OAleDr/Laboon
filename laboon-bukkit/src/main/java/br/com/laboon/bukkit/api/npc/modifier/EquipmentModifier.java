package br.com.laboon.bukkit.api.npc.modifier;

import br.com.laboon.bukkit.api.npc.NPC;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.inventory.ItemStack;

public final class EquipmentModifier extends NPCModifier {

    public EquipmentModifier(NPC npc) {
        super(npc);
    }

    public EquipmentModifier queue(EnumWrappers.ItemSlot slot, ItemStack item) {

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);

        packet.getModifier().writeDefaults();

        packet.getIntegers().write(0, npc.getEntityId());

        packet.getItemSlots().write(0, slot);

        packet.getItemModifier().write(0, item == null ? null : item.clone());

        addPacket(packet);

        return this;
    }
}