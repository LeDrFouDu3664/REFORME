package fr.jules.faction.manager;

import fr.jules.faction.FactionPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AFKManager {
    private final FactionPlugin plugin;
    private final Map<UUID, Long> lastActivity = new HashMap<>();
    private final Map<UUID, Boolean> afkPlayers = new HashMap<>();

    public AFKManager(FactionPlugin plugin) {
        this.plugin = plugin;
        startTask();
    }

    public void updateActivity(Player player) {
        lastActivity.put(player.getUniqueId(), System.currentTimeMillis());
        if (afkPlayers.getOrDefault(player.getUniqueId(), false)) {
            setAFK(player, false);
        }
    }

    public void setAFK(Player player, boolean status) {
        if (afkPlayers.getOrDefault(player.getUniqueId(), false) == status) return;

        afkPlayers.put(player.getUniqueId(), status);
        if (status) {
            Bukkit.broadcastMessage("§7[AFK] §e" + player.getName() + " §7est désormais AFK.");
        } else {
            Bukkit.broadcastMessage("§7[AFK] §e" + player.getName() + " §7n'est plus AFK.");
        }
    }

    public boolean isAFK(Player player) {
        return afkPlayers.getOrDefault(player.getUniqueId(), false);
    }

    private void startTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("faction.staff.noafk")) continue;

                long last = lastActivity.getOrDefault(player.getUniqueId(), now);
                long diff = now - last;

                // 5 minutes -> Set AFK
                if (diff >= 300000 && !isAFK(player)) {
                    setAFK(player, true);
                }

                // 60 minutes -> Kick
                if (diff >= 3600000) {
                    player.kickPlayer("§cVous avez été expulsé pour inactivité (1 heure).");
                }
            }
        }, 20 * 60, 20 * 60); // Check every minute
    }

    public void removePlayer(UUID uuid) {
        lastActivity.remove(uuid);
        afkPlayers.remove(uuid);
    }
}
