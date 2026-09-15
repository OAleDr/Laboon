package br.com.laboon.core.progression;

@FunctionalInterface
public interface ProgressionLevelUpListener {

    void onLevelUp(
            ProgressionSnapshot snapshot,
            int level
    );
}
