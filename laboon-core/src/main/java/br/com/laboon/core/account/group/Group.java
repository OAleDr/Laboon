package br.com.laboon.core.account.group;

public enum Group {

    OWNER("OWN", "OWNER", 100, '4'),

    ADMIN("ADM", "ADMIN", 90, 'c'),

    MODPLUS("MD++", "MOD++", 80, '5'),

    MOD("MOD", "MOD", 70, '5'),

    TRIAL("TRL", "TRIAL", 60, '5'),

    HELPER("HLP", "HELPER", 50, '9'),

    STAFF("STF", "STAFF", 40, 'e'),

    DEV("DEV", "DEV", 30, '3'),

    BUILDER("BLD", "BUILDER", 30, 'e'),

    INFLUENCER("INF", "INFLUENCER", 20, '9'),

    PROMOTER("PRO", "PROMOTER", 20, '9'),

    LEGENDPLUS("LEG+", "LEGEND+", 15, 'd'),

    LEGEND("LEG", "LEGEND", 14, 'd'),

    EXPLORERPLUS("EXP+", "EXPLORER+", 13, '6'),

    EXPLORER("EXP", "EXPLORER", 12, '6'),

    DEFAULT("", "", 0, '7');

    private final String abbreviation;
    private final String displayName;
    private final int power;
    private final char color;

    Group(String abbreviation, String displayName, int power, char color) {
        this.abbreviation = abbreviation;
        this.displayName = displayName;
        this.power = power;
        this.color = color;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getPower() {
        return power;
    }

    public char getColor() {
        return color;
    }

    public boolean hasPermission(Group requiredGroup) {
        if (requiredGroup == null) {
            return true;
        }

        return power >= requiredGroup.power;
    }

    public boolean isDefault() {
        return this == DEFAULT;
    }

    public static Group fromId(String id) {
        if (id == null || id.isBlank()) {
            return DEFAULT;
        }

        try {
            return valueOf(id.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return DEFAULT;
        }
    }
}