package br.com.laboon.bukkit.api.scoreboard;


import br.com.laboon.bukkit.api.LaboonScoreboard;

@FunctionalInterface
public interface ScoreboardUpdater {

    void update(LaboonScoreboard scoreboard);
}