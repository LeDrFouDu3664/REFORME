package fr.jules.faction.manager;

import fr.jules.faction.model.PlayerData;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import fr.jules.faction.FactionPlugin;

public class PlayerManager {
    private final FactionPlugin plugin;
    private final Map<UUID, PlayerData> players = new ConcurrentHashMap<>();

    public PlayerManager(FactionPlugin plugin) {
        this.plugin = plugin;
    }

    public PlayerData getPlayerData(UUID uuid) {
        PlayerData data = players.get(uuid);
        if (data == null) {
            // Tentative de chargement depuis le disque
            data = plugin.getDataManager().loadPlayerData(uuid);
            if (data == null) {
                data = new PlayerData(uuid);
            }
            players.put(uuid, data);
        }
        updateMaxPower(data);
        return data;
    }

    private void updateMaxPower(PlayerData data) {
        double base = 10.0;
        if (data.getFactionId() != null) {
            fr.jules.faction.model.Faction f = plugin.getFactionManager().getFaction(data.getFactionId());
            if (f != null) {
                base += plugin.getFactionLevelManager().getMaxPowerBoost(f);
            }
        }
        base += data.getPowerBoost();
        data.setMaxPower(base);
    }

    public void removePlayerData(UUID uuid) {
        players.remove(uuid);
    }

    public Collection<PlayerData> getAllPlayerData() {
        return players.values();
    }

    public void addPlayerData(PlayerData data) {
        players.put(data.getUuid(), data);
    }

    public PlayerData getPlayerDataByName(String name) {
        return players.values().stream()
                .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
