package br.com.laboon.core.progression;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LevelCalculatorTest {

    @Test
    void shouldStartAtLevelOne() {
        LevelCalculator calculator =
                LevelCalculator.defaultCalculator();

        assertEquals(1, calculator.levelFromExperience(0));
        assertEquals(1, calculator.levelFromExperience(999));
    }

    @Test
    void shouldReachLevelTwo() {
        LevelCalculator calculator =
                LevelCalculator.defaultCalculator();

        assertEquals(
                2,
                calculator.levelFromExperience(1275)
        );
    }

    @Test
    void shouldReturnNextLevelRequirement() {
        LevelCalculator calculator =
                LevelCalculator.defaultCalculator();

        assertEquals(
                1275L,
                calculator.requiredExperienceForNextLevel(1)
        );
    }
}
