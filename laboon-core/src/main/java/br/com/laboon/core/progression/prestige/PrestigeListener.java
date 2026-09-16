package br.com.laboon.core.progression.prestige;

@FunctionalInterface
public interface PrestigeListener {
    void onPrestige(PrestigeLevelUpEvent event);
}
