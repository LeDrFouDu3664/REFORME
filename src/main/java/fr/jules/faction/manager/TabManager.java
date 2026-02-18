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
                "\n§c§l«§m-------§r §c§lTPC FACTION §c§l§m-------§r§c§l»\n" +
                "§7Bienvenue sur le PvP/Faction\n" +
                "§f\n" +
                "§8» §7Connectés: §e" + online + " §8| §7Latence: §e" + player.getPing() + "ms\n" +
                "§f",
                "§f\n" +
                "§8» §6✪ §7Faction: " + factionName + "\n" +
                "§8» §6⚡ §7Puissance: " + powerInfo + "\n" +
                "§8» §d⚒ §7Métier: " + jobInfo + "\n" +
                "§8» §a$ §7Argent: §a" + String.format("%.0f", plugin.getEconomyManager().getBalance(player)) + "$\n" +
                "§f\n" +
                "§c§l«§m-------------------------§r§c§l»\n" +
                "§eIP: §fplay.tpcfaction.fr\n"
        );

        // Update name in tab with faction prefix and job color
        String color = "§f";
        if (data.getJob().equals("MINEUR")) color = "§b";
        else if (data.getJob().equals("BUCHERON")) color = "§6";
        else if (data.getJob().equals("FERMIER")) color = "§a";
        else if (data.getJob().equals("GUERRIER")) color = "§c";

        String rankPrefix = data.getRank().getPrefix() + " ";
        String factionPrefix = data.getFactionId() != null ? "§8[§6" + factionName.substring(2) + "§8] " : "§7";
        String vanishSuffix = player.hasMetadata("vanished") ? " §7[VANISH]" : "";
        String afkSuffix = plugin.getAfkManager().isAFK(player) ? " §8[AFK]" : "";

        player.setPlayerListName(factionPrefix + rankPrefix + color + player.getName() + vanishSuffix + afkSuffix);
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
