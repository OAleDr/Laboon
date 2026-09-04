package br.com.laboon.core.account;

import br.com.laboon.core.account.group.Group;

import java.time.Instant;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public final class Account {

    private final UUID uniqueId;

    private String name;

    private Group group;

    private String tag;

    private long experience;

    private AccountType type;

    private final Instant createdAt;

    private Instant lastLogin;

    private AccountPreferences preferences;

    private final Map<Group, Instant> temporaryGroups;

    public Account(UUID uniqueId, String name) {
        this(uniqueId, name, AccountType.ORIGINAL, Instant.now(), null, new AccountPreferences());
    }

    public Account(UUID uniqueId, String name, AccountType type, Instant createdAt, Instant lastLogin, AccountPreferences preferences) {
        this.uniqueId = uniqueId;
        this.name = name;

        this.group = Group.DEFAULT;
        this.tag = Group.DEFAULT.getAbbreviation();

        this.experience = 0;

        this.type = type;

        this.createdAt = createdAt;
        this.lastLogin = lastLogin;

        this.preferences = preferences;

        this.temporaryGroups = new EnumMap<>(Group.class);
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /*
     * =========================
     * GROUP
     * =========================
     */

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group == null ? Group.DEFAULT : group;
    }

    /*
     * =========================
     * TEMPORARY GROUPS
     * =========================
     */

    public Map<Group, Instant> getTemporaryGroups() {
        return Collections.unmodifiableMap(temporaryGroups);
    }

    public void setTemporaryGroup(Group group, Instant expiresAt) {
        if (group == null || expiresAt == null) {
            return;
        }

        temporaryGroups.put(group, expiresAt);
    }

    public void removeTemporaryGroup(Group group) {
        if (group == null) {
            return;
        }

        temporaryGroups.remove(group);
    }

    public boolean hasTemporaryGroup(Group group) {
        if (group == null) {
            return false;
        }

        Instant expiresAt = temporaryGroups.get(group);

        if (expiresAt == null) {
            return false;
        }

        return expiresAt.isAfter(Instant.now());
    }

    /*
     * =========================
     * TAG
     * =========================
     */

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag == null ? "" : tag;
    }

    /*
     * =========================
     * EXPERIENCE
     * =========================
     */

    public long getExperience() {
        return experience;
    }

    public void setExperience(long experience) {
        this.experience = experience;
    }

    /*
     * =========================
     * ACCOUNT TYPE
     * =========================
     */

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    /*
     * =========================
     * DATES
     * =========================
     */

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Instant lastLogin) {
        this.lastLogin = lastLogin;
    }

    /*
     * =========================
     * PREFERENCES
     * =========================
     */

    public AccountPreferences getPreferences() {
        return preferences;
    }

    public void setPreferences(AccountPreferences preferences) {
        this.preferences = preferences;
    }
}