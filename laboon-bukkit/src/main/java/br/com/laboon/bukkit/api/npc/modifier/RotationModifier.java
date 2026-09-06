package br.com.laboon.bukkit.api.npc.modifier;

import br.com.laboon.bukkit.api.npc.NPC;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Location;

public final class RotationModifier extends NPCModifier {

    public RotationModifier(NPC npc) {
        super(npc);
    }

    public RotationModifier queueRotate(float yaw, float pitch) {

        byte yawByte = (byte) (yaw * 256F / 360F);

        byte pitchByte = (byte) (pitch * 256F / 360F);

        PacketContainer body = new PacketContainer(PacketType.Play.Server.ENTITY_LOOK);

        body.getModifier().writeDefaults();

        body.getIntegers().write(0, npc.getEntityId());

        body.getBytes().write(0, yawByte).write(1, pitchByte);

        body.getBooleans().write(0, true);

        addPacket(body);

        PacketContainer head = new PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION);

        head.getModifier().writeDefaults();

        head.getIntegers().write(0, npc.getEntityId());

        head.getBytes().write(0, yawByte);

        addPacket(head);

        return this;
    }

    public RotationModifier queueLookAt(Location target) {

        if (target == null) {
            return this;
        }

        Location origin = npc.getLocation();

        double x = target.getX() - origin.getX();

        double y = target.getY() - origin.getY();

        double z = target.getZ() - origin.getZ();

        double distance = Math.sqrt(x * x + y * y + z * z);

        if (distance <= 0.0001D) {
            return this;
        }

        float yaw = (float) (-Math.atan2(x, z) * 180D / Math.PI);

        float pitch = (float) (-Math.asin(y / distance) * 180D / Math.PI);

        return queueRotate(yaw, pitch);
    }
}