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
        player.setSleepingIgnored(status);
        player.setCollidable(!status);
        player.setInvulnerable(status);

        if (status) {
            Bukkit.broadcastMessage("§7[AFK] §e" + player.getName() + " §7est désormais §6AFK.");
        } else {
            Bukkit.broadcastMessage("§7[AFK] §e" + player.getName() + " §7n'est plus AFK.");
        }
        plugin.getTabManager().updateTab(player);
    }

    public boolean isAFK(Player player) {
        return afkPlayers.getOrDefault(player.getUniqueId(), false);
    }

    private void startTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            for (Player player : Bukkit.getOnlinePlayers()) {
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
        }, 400L, 400L); // Check every 20 seconds
    }

    public void removePlayer(UUID uuid) {
        lastActivity.remove(uuid);
        afkPlayers.remove(uuid);
    }
}
