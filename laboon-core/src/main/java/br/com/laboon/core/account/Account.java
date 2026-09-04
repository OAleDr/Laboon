package br.com.laboon.core.account;

import br.com.laboon.core.account.group.Group;

import java.time.Instant;
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