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
            Bukkit.broadcastMessage("§e" + player.getName() + " a rejoint la partie");
            player.sendMessage("§aVanish: §cDésactivé");
            plugin.getTabManager().updateVisibility(player);
        } else {
            player.setMetadata("vanished", new FixedMetadataValue(plugin, true));
            Bukkit.broadcastMessage("§e" + player.getName() + " a quitté la partie");
            player.sendMessage("§aVanish: §aActivé");
            plugin.getTabManager().updateVisibility(player);
        }
    }
}
