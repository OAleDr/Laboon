package br.com.laboon.core.account.cache;

import br.com.laboon.core.account.Account;
import br.com.laboon.core.account.AccountPreferences;
import br.com.laboon.core.account.AccountType;
import br.com.laboon.core.account.group.Group;
import br.com.laboon.core.language.LanguageLocale;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public final class AccountCacheCodec {

    private final Gson gson = new Gson();

    public String serialize(Account account) {

        JsonObject json = new JsonObject();

        json.addProperty(
                "uuid",
                account.getUniqueId().toString()
        );

        json.addProperty(
                "name",
                account.getName()
        );

        json.addProperty(
                "group",
                account.getGroup().name()
        );

        json.addProperty(
                "tag",
                account.getTag()
        );

        json.addProperty(
                "experience",
                account.getExperience()
        );

        json.addProperty(
                "type",
                account.getType().name()
        );

        json.addProperty(
                "createdAt",
                account.getCreatedAt().toString()
        );

        if (account.getLastLogin() != null) {

            json.addProperty(
                    "lastLogin",
                    account.getLastLogin().toString()
            );
        }

        AccountPreferences preferences =
                account.getPreferences();

        json.addProperty(
                "language",
                preferences.getLanguage().getCode()
        );

        json.addProperty(
                "privateMessages",
                preferences.isPrivateMessages()
        );

        json.addProperty(
                "friendRequests",
                preferences.isFriendRequests()
        );

        json.addProperty(
                "serverJoinMessages",
                preferences.isServerJoinMessages()
        );

        JsonObject groups =
                new JsonObject();

        for (Map.Entry<Group, Instant> entry :
                account.getTemporaryGroups().entrySet()) {

            groups.addProperty(
                    entry.getKey().name(),
                    entry.getValue().toString()
            );
        }

        json.add(
                "temporaryGroups",
                groups
        );

        return gson.toJson(json);
    }

    public Account deserialize(String value) {

        try {

            JsonObject json =
                    gson.fromJson(value, JsonObject.class);

            UUID uuid =
                    UUID.fromString(
                            json.get("uuid").getAsString()
                    );

            AccountType type;

            try {

                type = AccountType.valueOf(
                        json.get("type").getAsString()
                );

            } catch (Exception exception) {

                type = AccountType.ORIGINAL;
            }

            Instant createdAt =
                    Instant.parse(
                            json.get("createdAt").getAsString()
                    );

            Instant lastLogin = null;

            if (json.has("lastLogin")
                    && !json.get("lastLogin").isJsonNull()) {

                lastLogin =
                        Instant.parse(
                                json.get("lastLogin").getAsString()
                        );
            }

            AccountPreferences preferences =
                    new AccountPreferences();

            if (json.has("language")) {

                String language =
                        json.get("language").getAsString();

                try {
                    preferences.setLanguage(
                            LanguageLocale.fromCode(language)
                    );
                } catch (IllegalArgumentException ignored) {
                    preferences.setLanguage(
                            LanguageLocale.ptBR()
                    );
                }
            }

            if (json.has("privateMessages")) {

                preferences.setPrivateMessages(
                        json.get(
                                "privateMessages"
                        ).getAsBoolean()
                );
            }

            if (json.has("friendRequests")) {

                preferences.setFriendRequests(
                        json.get(
                                "friendRequests"
                        ).getAsBoolean()
                );
            }

            if (json.has("serverJoinMessages")) {

                preferences.setServerJoinMessages(
                        json.get(
                                "serverJoinMessages"
                        ).getAsBoolean()
                );
            }

            Account account =
                    new Account(
                            uuid,
                            json.get("name").getAsString(),
                            type,
                            createdAt,
                            lastLogin,
                            preferences
                    );

            if (json.has("group")) {

                try {

                    account.setGroup(
                            Group.valueOf(
                                    json.get(
                                            "group"
                                    ).getAsString()
                            )
                    );

                } catch (IllegalArgumentException ignored) {
                }
            }

            if (json.has("tag")) {

                account.setTag(
                        json.get(
                                "tag"
                        ).getAsString()
                );
            }

            if (json.has("experience")) {

                account.setExperience(
                        json.get(
                                "experience"
                        ).getAsLong()
                );
            }

            if (json.has("temporaryGroups")) {

                JsonObject groups =
                        json.getAsJsonObject(
                                "temporaryGroups"
                        );

                for (String groupName :
                        groups.keySet()) {

                    try {

                        Group group =
                                Group.valueOf(
                                        groupName
                                );

                        Instant expiresAt =
                                Instant.parse(
                                        groups.get(
                                                groupName
                                        ).getAsString()
                                );

                        if (expiresAt.isAfter(
                                Instant.now()
                        )) {

                            account.setTemporaryGroup(
                                    group,
                                    expiresAt
                            );
                        }

                    } catch (Exception ignored) {
                    }
                }
            }

            return account;

        } catch (Exception exception) {

            return null;
        }
    }
}