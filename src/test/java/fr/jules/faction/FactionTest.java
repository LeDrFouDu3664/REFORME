package fr.jules.faction;

import fr.jules.faction.manager.FactionManager;
import fr.jules.faction.model.Faction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

public class FactionTest {
    @Test
    public void testFactionCreation() {
        FactionManager manager = new FactionManager();
        UUID leader = UUID.randomUUID();
        Faction faction = manager.createFaction("TestFaction", leader);

        assertNotNull(faction);
        assertEquals("TestFaction", faction.getName());
        assertEquals(leader, faction.getLeader());
        assertTrue(faction.getMembers().contains(leader));
    }

    @Test
    public void testFactionRename() {
        FactionManager manager = new FactionManager();
        UUID leader = UUID.randomUUID();
        Faction faction = manager.createFaction("OldName", leader);
        manager.renameFaction(faction, "NewName");

        assertEquals("NewName", faction.getName());
        assertNull(manager.getFactionByName("OldName"));
        assertEquals(faction, manager.getFactionByName("NewName"));
    }
}
