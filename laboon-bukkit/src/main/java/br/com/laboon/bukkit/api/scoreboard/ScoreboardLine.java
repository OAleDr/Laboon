package br.com.laboon.bukkit.api.scoreboard;

import org.bukkit.ChatColor;

public class ScoreboardLine {

    private final String id;

    private String text;
    private String prefix;
    private String suffix;

    public ScoreboardLine(String id, String text) {
        this.id = id;
        setText(text);
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setText(String text) {
        if (text == null) {
            text = "";
        }

        this.text = text;

        if (text.length() <= 16) {
            this.prefix = text;
            this.suffix = "";
            return;
        }

        String prefix = text.substring(0, Math.min(16, text.length()));

        // Evita deixar o caractere § sozinho no final
        if (prefix.endsWith(String.valueOf(ChatColor.COLOR_CHAR))) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }

        String remaining = text.substring(prefix.length());

        // Mantém a cor da primeira parte na segunda
        String colors = ChatColor.getLastColors(prefix);

        String suffix = colors + remaining;

        if (suffix.length() > 16) {
            suffix = suffix.substring(0, 16);
        }

        if (suffix.endsWith(String.valueOf(ChatColor.COLOR_CHAR))) {
            suffix = suffix.substring(0, suffix.length() - 1);
        }

        this.prefix = prefix;
        this.suffix = suffix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix == null ? "" : prefix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix == null ? "" : suffix;
    }
}