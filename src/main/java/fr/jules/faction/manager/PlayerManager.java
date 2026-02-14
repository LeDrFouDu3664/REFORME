package fr.jules.faction.manager;

import fr.jules.faction.model.PlayerData;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {
    private final Map<UUID, PlayerData> players = new ConcurrentHashMap<>();

    public PlayerData getPlayerData(UUID uuid) {
        PlayerData data = players.get(uuid);
        if (data == null) {
            data = new PlayerData(uuid);
            // Default values from potential config (handled in Plugin)
            players.put(uuid, data);
        }
        return data;
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
