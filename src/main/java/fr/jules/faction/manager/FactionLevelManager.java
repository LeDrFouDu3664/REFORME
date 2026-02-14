package fr.jules.faction.manager;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.model.Faction;
import fr.jules.faction.model.PlayerData;

public class FactionLevelManager {
    private final FactionPlugin plugin;

    public FactionLevelManager(FactionPlugin plugin) {
        this.plugin = plugin;
    }

    public double getMaxPowerBoost(Faction faction) {
        int level = faction.getLevel();
        if (level >= 20) return 15.0;
        if (level >= 15) return 10.0;
        if (level >= 10) return 7.0;
        if (level >= 5) return 4.0;
        if (level >= 2) return 2.0;
        return 0.0;
    }

    public int getMaxMembersBoost(Faction faction) {
        int level = faction.getLevel();
        if (level >= 20) return 20;
        if (level >= 15) return 10;
        if (level >= 10) return 5;
        return 0;
    }
}
