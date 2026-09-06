package br.com.laboon.bukkit.api.npc.modifier;

import br.com.laboon.bukkit.api.npc.NPC;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

public final class AnimationModifier extends NPCModifier {

    public AnimationModifier(NPC npc) {
        super(npc);
    }

    public AnimationModifier queue(EntityAnimation animation) {
        if (animation == null) {
            return this;
        }

        return queue(animation.getId());
    }

    public AnimationModifier queue(int animationId) {

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ANIMATION);

        packet.getModifier().writeDefaults();

        packet.getIntegers().write(0, npc.getEntityId());

        packet.getIntegers().write(1, animationId);

        addPacket(packet);

        return this;
    }

    public enum EntityAnimation {

        SWING_MAIN_ARM(0), TAKE_DAMAGE(1), LEAVE_BED(2), SWING_OFF_HAND(3), CRITICAL_EFFECT(4), MAGIC_CRITICAL_EFFECT(5);

        private final int id;

        EntityAnimation(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}