package fr.jules.faction.manager;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.model.BanData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BanManager {
    private final FactionPlugin plugin;
    private final File file;
    private FileConfiguration config;
    private final Map<UUID, BanData> bans = new ConcurrentHashMap<>();
    private final Map<String, BanData> ipBans = new ConcurrentHashMap<>();

    public BanManager(FactionPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "bans.yml");
        loadBans();
    }

    public void loadBans() {
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException ignored) {}
        }
        config = YamlConfiguration.loadConfiguration(file);
        bans.clear();
        ipBans.clear();

        if (config.getConfigurationSection("bans") != null) {
            for (String key : config.getConfigurationSection("bans").getKeys(false)) {
                String path = "bans." + key + ".";
                BanData data = new BanData(
                        key,
                        config.getString(path + "name"),
                        UUID.fromString(config.getString(path + "uuid")),
                        config.getString(path + "ip"),
                        config.getString(path + "reason"),
                        config.getString(path + "admin"),
                        config.getLong(path + "expiry"),
                        config.getLong(path + "timestamp")
                );
                if (!data.isExpired()) {
                    bans.put(data.getTargetUUID(), data);
                    if (data.getIp() != null) ipBans.put(data.getIp(), data);
                }
            }
        }
    }

    public void saveBans() {
        config = new YamlConfiguration();
        for (BanData data : bans.values()) {
            String path = "bans." + data.getBanId() + ".";
            config.set(path + "name", data.getTargetName());
            config.set(path + "uuid", data.getTargetUUID().toString());
            config.set(path + "ip", data.getIp());
            config.set(path + "reason", data.getReason());
            config.set(path + "admin", data.getAdmin());
            config.set(path + "expiry", data.getExpiryTime());
            config.set(path + "timestamp", data.getTimestamp());
        }
        try { config.save(file); } catch (IOException ignored) {}
    }

    public void ban(BanData data) {
        bans.put(data.getTargetUUID(), data);
        if (data.getIp() != null) ipBans.put(data.getIp(), data);
        saveBans();
    }

    public void unban(UUID uuid) {
        BanData data = bans.remove(uuid);
        if (data != null && data.getIp() != null) ipBans.remove(data.getIp());
        saveBans();
    }

    public void unbanAll(boolean includeCheat) {
        Iterator<Map.Entry<UUID, BanData>> it = bans.entrySet().iterator();
        while (it.hasNext()) {
            BanData data = it.next().getValue();
            if (!includeCheat && data.getReason().toLowerCase().contains("cheat")) continue;
            it.remove();
            if (data.getIp() != null) ipBans.remove(data.getIp());
        }
        saveBans();
    }

    public BanData getBan(UUID uuid) {
        BanData data = bans.get(uuid);
        if (data != null && data.isExpired()) {
            unban(uuid);
            return null;
        }
        return data;
    }

    public BanData getIPBan(String ip) {
        BanData data = ipBans.get(ip);
        if (data != null && data.isExpired()) {
            unban(data.getTargetUUID());
            return null;
        }
        return data;
    }
}
