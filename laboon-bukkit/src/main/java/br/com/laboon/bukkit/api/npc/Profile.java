package br.com.laboon.bukkit.api.npc;

import br.com.laboon.bukkit.api.skin.Skin;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;

import java.util.UUID;

public final class Profile {

    private UUID uniqueId;
    private String name;
    private WrappedSignedProperty signedProperty;

    public Profile(WrappedSignedProperty signedProperty) {
        this(null, null, signedProperty);
    }

    public Profile(String name, WrappedSignedProperty signedProperty) {
        this(null, name, signedProperty);
    }

    public Profile(UUID uniqueId) {
        this(uniqueId, null, null);
    }

    public Profile(String name) {
        this(null, name, null);
    }

    public Profile(UUID uniqueId, String name) {
        this(uniqueId, name, null);
    }

    public Profile(UUID uniqueId, String name, WrappedSignedProperty signedProperty) {
        this.uniqueId = uniqueId != null ? uniqueId : UUID.randomUUID();

        this.name = name == null || name.isBlank() ? "LaboonNPC" : name;

        this.signedProperty = signedProperty;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public Profile uniqueId(UUID uniqueId) {

        if (uniqueId != null) {
            this.uniqueId = uniqueId;
        }

        return this;
    }

    public String getName() {
        return name;
    }

    public Profile name(String name) {

        if (name != null && !name.isBlank()) {
            this.name = name;
        }

        return this;
    }

    public WrappedSignedProperty getSignedProperty() {
        return signedProperty;
    }

    public Profile texture(String value, String signature) {

        if (value == null || value.isBlank()) {
            return this;
        }

        this.signedProperty = new WrappedSignedProperty("textures", value, signature);

        return this;
    }

    public Profile texture(WrappedSignedProperty property) {

        if (property != null) {
            this.signedProperty = property;
        }

        return this;
    }

    public Profile skin(Skin skin) {

        if (skin == null) {
            return this;
        }

        return texture(skin.getValue(), skin.getSignature());
    }

    public boolean hasProperties() {
        return signedProperty != null;
    }

    public WrappedGameProfile asWrapped() {

        return new WrappedGameProfile(uniqueId, name);
    }
}