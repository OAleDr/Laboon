package br.com.laboon.bukkit.api;


import br.com.laboon.bukkit.api.scoreboard.ScoreboardLine;
import br.com.laboon.bukkit.api.scoreboard.ScoreboardUpdater;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class LaboonScoreboard {

    private static final int MAX_LINES = 15;

    private final Player player;

    private final Scoreboard scoreboard;
    private final Objective objective;

    private final Map<Integer, ScoreboardLine> lines;
    private final Map<Integer, String> entries;

    private ScoreboardUpdater updater;

    private boolean visible;

    public LaboonScoreboard(Player player, String title) {
        this.player = player;

        this.lines = new LinkedHashMap<>();
        this.entries = new HashMap<>();

        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        this.objective = scoreboard.registerNewObjective("laboon", "dummy", title);

        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        this.visible = false;
    }

    /*
     * Player
     */

    public Player getPlayer() {
        return player;
    }

    /*
     * Title
     */

    public void setTitle(String title) {
        if (title == null) {
            title = "";
        }

        if (title.length() > 128) {
            title = title.substring(0, 128);
        }

        objective.setDisplayName(title);
    }

    public String getTitle() {
        return objective.getDisplayName();
    }

    /*
     * Lines
     */

    public void setLine(int position, String text) {

        if (position < 0 || position >= MAX_LINES) {
            throw new IllegalArgumentException("Scoreboard position must be between 0 and 14.");
        }

        ScoreboardLine line = lines.get(position);

        if (line == null) {
            line = new ScoreboardLine(createEntry(position), text);

            lines.put(position, line);
        } else {
            line.setText(text);
        }
    }

    public void setLine(int position, ScoreboardLine line) {

        if (position < 0 || position >= MAX_LINES) {
            throw new IllegalArgumentException("Scoreboard position must be between 0 and 14.");
        }

        if (line == null) {
            removeLine(position);
            return;
        }

        lines.put(position, line);
    }

    public void addLine(String text) {

        for (int i = 0; i < MAX_LINES; i++) {

            if (!lines.containsKey(i)) {
                setLine(i, text);
                return;
            }
        }

        throw new IllegalStateException("Scoreboard already contains 15 lines.");
    }

    public void removeLine(int position) {

        ScoreboardLine line = lines.remove(position);

        if (line == null) {
            return;
        }

        String entry = entries.remove(position);

        if (entry != null) {
            scoreboard.resetScores(entry);
        }

        Team team = scoreboard.getTeam(line.getId());

        if (team != null) {
            team.unregister();
        }
    }

    public void clearLines() {

        for (int i = 0; i < MAX_LINES; i++) {
            removeLine(i);
        }
    }

    public ScoreboardLine getLine(int position) {
        return lines.get(position);
    }

    public Collection<ScoreboardLine> getLines() {
        return Collections.unmodifiableCollection(lines.values());
    }

    private void updateLine(int position, ScoreboardLine line) {

        String entry = createEntry(position);

        Team team = scoreboard.getTeam(line.getId());

        if (team == null) {
            team = scoreboard.registerNewTeam(line.getId());
        }

        String prefix = line.getPrefix();
        String suffix = line.getSuffix();

        if (prefix.length() > 16) {
            prefix = prefix.substring(0, 16);
        }

        if (suffix.length() > 16) {
            suffix = suffix.substring(0, 16);
        }

        team.setPrefix(prefix);
        team.setSuffix(suffix);

        if (!team.hasEntry(entry)) {
            team.addEntry(entry);
        }

        Integer oldPosition = findEntryPosition(entry);

        if (oldPosition != null && oldPosition != position) {
            scoreboard.resetScores(entry);
        }

        if(position > MAX_LINES)
            return;

        objective.getScore(entry).setScore(position);

        entries.put(position, entry);
    }

    private Integer findEntryPosition(String entry) {

        for (Map.Entry<Integer, String> e : entries.entrySet()) {

            if (e.getValue().equals(entry)) {
                return e.getKey();
            }
        }

        return null;
    }

    public void update() {
        if (updater != null) {
            updater.update(this);
        }
        // Remove tudo que não está mais sendo utilizado
        Set<Integer> positions = new HashSet<>(lines.keySet());
        for (Integer position : new HashSet<>(entries.keySet())) {
            if (!positions.contains(position)) {
                String entry = entries.remove(position);
                if (entry != null) {
                    scoreboard.resetScores(entry);
                }
            }
        }
        for (Map.Entry<Integer, ScoreboardLine> entry : lines.entrySet()) {
            int position = entry.getKey();
            ScoreboardLine line = entry.getValue();
            updateLine(position, line);
        }
    }

    /*
     * Automatic updater
     */

    public void setUpdater(ScoreboardUpdater updater) {
        this.updater = updater;
    }

    public ScoreboardUpdater getUpdater() {
        return updater;
    }

    /*
     * Visibility
     */

    public void show() {

        player.setScoreboard(scoreboard);

        visible = true;
    }

    public void hide() {

        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());

        visible = false;
    }

    public boolean isVisible() {
        return visible;
    }

    /*
     * Bukkit scoreboard
     */

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public Objective getObjective() {
        return objective;
    }

    /*
     * Internal entry
     */

    private String createEntry(int position) {

        /*
         * Utilizamos cores diferentes como entries invisíveis.
         *
         * §0
         * §1
         * §2
         * ...
         * §e
         */

        return ChatColor.COLOR_CHAR + Integer.toHexString(position);
    }

    /*
     * Destroy
     */

    public void destroy() {

        hide();

        clearLines();

        try {
            objective.unregister();
        } catch (Exception ignored) {
        }
    }
}