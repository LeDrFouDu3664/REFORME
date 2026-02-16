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
        String powerInfo = "§b10.0";
        if (data.getFactionId() != null) {
            Faction f = plugin.getFactionManager().getFaction(data.getFactionId());
            if (f != null) {
                factionName = "§6" + f.getName();
                powerInfo = "§b" + String.format("%.1f", f.getPower());
            }
        }

        String jobInfo = "§e" + data.getJob() + " §7(Lvl " + data.getJobLevel() + ")";
        int online = Bukkit.getOnlinePlayers().size();

        player.setPlayerListHeaderFooter(
                "§8§m---------------------------------------\n" +
                "§c§lTPC FACTION §7- §fPvP/Factions\n" +
                "§7Joueurs en ligne: §a" + online + " §8| §7Ping: §e" + player.getPing() + "ms\n" +
                "§8§m---------------------------------------",
                "§8§m---------------------------------------\n" +
                "§6✪ §7Faction: " + factionName + " §8| §6⚡ §7Power: " + powerInfo + "\n" +
                "§d⚒ §7Métier: " + jobInfo + " §8| §a$ §7Argent: §a" + String.format("%.0f", plugin.getEconomyManager().getBalance(player)) + "$\n" +
                "§8§m---------------------------------------\n" +
                "§eplay.tpcfaction.fr"
        );

        // Update name in tab with faction prefix and job color
        String color = "§f";
        if (data.getJob().equals("MINEUR")) color = "§b";
        else if (data.getJob().equals("BUCHERON")) color = "§6";
        else if (data.getJob().equals("FERMIER")) color = "§a";
        else if (data.getJob().equals("GUERRIER")) color = "§c";

        String prefix = data.getFactionId() != null ? "§8[§6" + factionName.substring(2) + "§8] " : "§7";
        String suffix = player.hasMetadata("vanished") ? " §7[VANISH]" : "";
        player.setPlayerListName(prefix + color + player.getName() + suffix);
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
