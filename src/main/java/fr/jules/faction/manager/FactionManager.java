package fr.jules.faction.manager;

import fr.jules.faction.model.Faction;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FactionManager {
    private final Map<UUID, Faction> factions = new ConcurrentHashMap<>();
    private final Map<String, UUID> factionNames = new ConcurrentHashMap<>();

    public Faction createFaction(String name, UUID leader) {
        if (factionNames.containsKey(name.toLowerCase())) return null;
        Faction faction = new Faction(UUID.randomUUID());
        faction.setName(name);
        faction.setLeader(leader);
        faction.addMember(leader);
        factions.put(faction.getId(), faction);
        factionNames.put(name.toLowerCase(), faction.getId());
        return faction;
    }

    public void disbandFaction(UUID id) {
        Faction faction = factions.remove(id);
        if (faction != null) {
            factionNames.remove(faction.getName().toLowerCase());
        }
    }

    public Faction getFaction(UUID id) {
        return factions.get(id);
    }

    public Faction getFactionByName(String name) {
        UUID id = factionNames.get(name.toLowerCase());
        return id != null ? factions.get(id) : null;
    }

    public Collection<Faction> getAllFactions() {
        return factions.values();
    }

    public void renameFaction(Faction faction, String newName) {
        factionNames.remove(faction.getName().toLowerCase());
        faction.setName(newName);
        factionNames.put(newName.toLowerCase(), faction.getId());
    }

    public void addFaction(Faction faction) {
        factions.put(faction.getId(), faction);
        factionNames.put(faction.getName().toLowerCase(), faction.getId());
    }
}
