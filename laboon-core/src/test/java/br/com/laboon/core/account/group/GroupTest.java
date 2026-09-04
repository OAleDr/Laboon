package br.com.laboon.core.account.group;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GroupTest {

    @Test
    void shouldHaveDefaultGroup() {
        assertEquals("", Group.DEFAULT.getDisplayName());

        assertEquals(0, Group.DEFAULT.getPower());
    }

    @Test
    void shouldHaveCorrectDisplayNames() {
        assertEquals("OWNER", Group.OWNER.getDisplayName());

        assertEquals("MOD++", Group.MODPLUS.getDisplayName());

        assertEquals("EXPLORER+", Group.EXPLORERPLUS.getDisplayName());

        assertEquals("LEGEND+", Group.LEGENDPLUS.getDisplayName());
    }

    @Test
    void shouldCompareGroupPower() {
        assertTrue(Group.OWNER.hasPermission(Group.ADMIN));

        assertTrue(Group.ADMIN.hasPermission(Group.ADMIN));

        assertFalse(Group.ADMIN.hasPermission(Group.OWNER));
    }

    @Test
    void shouldAllowNullRequiredGroup() {
        assertTrue(Group.DEFAULT.hasPermission(null));
    }

    @Test
    void shouldIdentifyDefaultGroup() {
        assertTrue(Group.DEFAULT.isDefault());

        assertFalse(Group.OWNER.isDefault());
    }
}