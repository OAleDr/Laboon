package br.com.laboon.bukkit.api.npc;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

public final class Profile {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private UUID uniqueId;
    private String name;
    private WrappedSignedProperty signedProperty;

    public Profile(String name) {
        this(name, null);
    }

    public Profile(String name, WrappedSignedProperty signedProperty) {
        this.uniqueId = UUID.randomUUID();
        this.name = name == null || name.isBlank() ? "LaboonNPC" : name;
        this.signedProperty = signedProperty;
        loadSkin();
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getName() {
        return name;
    }

    public WrappedSignedProperty getSignedProperty() {
        return signedProperty;
    }

    private Profile loadSkin() {

        try {
            // Nome -> UUID
            HttpRequest uuidRequest = HttpRequest.newBuilder().uri(URI.create("https://api.mojang.com/users/profiles/minecraft/" + name)).GET().build();

            HttpResponse<String> uuidResponse = HTTP_CLIENT.send(uuidRequest, HttpResponse.BodyHandlers.ofString());

            if (uuidResponse.statusCode() != 200) {
                return this;
            }

            JSONObject uuidJson = new JSONObject(uuidResponse.body());

            String uuid = uuidJson.getString("id");

            this.uniqueId = UUID.fromString(uuid.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));

            // UUID -> propriedades da skin
            HttpRequest profileRequest = HttpRequest.newBuilder().uri(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false")).GET().build();

            HttpResponse<String> profileResponse = HTTP_CLIENT.send(profileRequest, HttpResponse.BodyHandlers.ofString());

            if (profileResponse.statusCode() != 200) {
                return this;
            }

            JSONObject profileJson = new JSONObject(profileResponse.body());

            JSONArray properties = profileJson.getJSONArray("properties");

            for (int i = 0; i < properties.length(); i++) {

                JSONObject property = properties.getJSONObject(i);

                if (!property.getString("name").equals("textures")) {
                    continue;
                }

                String value = property.getString("value");
                String signature = property.optString("signature", null);

                this.signedProperty = new WrappedSignedProperty("textures", value, signature);

                break;
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return this;
    }

    public WrappedGameProfile asWrapped() {

        WrappedGameProfile profile = new WrappedGameProfile(uniqueId, name);

        if (signedProperty != null) {
            profile.getProperties().put("textures", signedProperty);
        }

        return profile;
    }
}