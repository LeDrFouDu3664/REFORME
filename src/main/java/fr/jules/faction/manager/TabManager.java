package fr.jules.faction.manager;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.model.Faction;
import fr.jules.faction.model.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TabManager {
    private final FactionPlugin plugin;

    public TabManager(FactionPlugin plugin) {
        this.plugin = plugin;
        startUpdateTask();
    }

    public void updateTab(Player player) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        String factionName = "§7Sans Faction";
        if (data.getFactionId() != null) {
            Faction f = plugin.getFactionManager().getFaction(data.getFactionId());
            if (f != null) factionName = "§6" + f.getName();
        }

        player.setPlayerListHeaderFooter(
                "§c§lTPC FACTION §7- §fVersion 1.0\n§7Bienvenue §e" + player.getName(),
                "\n§7Faction: " + factionName + " §8| §7Argent: §a" + String.format("%.0f", plugin.getEconomyManager().getBalance(player)) + "$\n" +
                "§eplay.tpcfaction.fr"
        );

        // Update name in tab with faction prefix
        String prefix = data.getFactionId() != null ? "§8[§6" + factionName.substring(2) + "§8] " : "§7";
        player.setPlayerListName(prefix + (player.hasMetadata("vanished") ? "§7[V] " : "§f") + player.getName());
    }

    private void startUpdateTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                updateTab(p);
                updateVisibility(p);
            }
        }, 20, 20);
    }

    public void updateVisibility(Player player) {
        boolean vanished = player.hasMetadata("vanished");
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player)) continue;

            if (vanished) {
                if (!other.hasPermission("faction.staff")) {
                    other.hidePlayer(plugin, player);
                } else {
                    other.showPlayer(plugin, player);
                }
            } else {
                other.showPlayer(plugin, player);
            }
        }
    }
}
