package fr.jules.faction.manager;

import fr.jules.faction.FactionPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class VanishManager {
    private final FactionPlugin plugin;

    public VanishManager(FactionPlugin plugin) {
        this.plugin = plugin;
    }

    public void toggleVanish(Player player) {
        if (player.hasMetadata("vanished")) {
            player.removeMetadata("vanished", plugin);
            String msg = plugin.getConfig().getString("messages.join-message", "§7[§a+§7] §f%player% a rejoint la partie")
                    .replace("%player%", player.getName());
            Bukkit.broadcastMessage(msg);
            player.sendMessage("§aVanish: §cDésactivé");
            plugin.getTabManager().updateVisibility(player);
        } else {
            player.setMetadata("vanished", new FixedMetadataValue(plugin, true));
            String msg = plugin.getConfig().getString("messages.quit-message", "§7[§c-§7] §f%player% a quitté la partie")
                    .replace("%player%", player.getName());
            Bukkit.broadcastMessage(msg);
            player.sendMessage("§aVanish: §aActivé");
            plugin.getTabManager().updateVisibility(player);
        }
    }
}
