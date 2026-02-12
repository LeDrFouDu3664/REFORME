package fr.jules.faction.listeners;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.model.Faction;
import fr.jules.faction.model.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.UUID;

public class GUIListener implements Listener {
    private final FactionPlugin plugin;

    public GUIListener(FactionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().startsWith("§6Gestion: ")) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;

            if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;
            String name = event.getCurrentItem().getItemMeta().getDisplayName();

            PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
            if (data.getFactionId() == null) return;
            Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());

            if (name.contains("Membres")) {
                player.sendMessage("§6--- Membres de " + faction.getName() + " ---");
                for (UUID memberId : faction.getMembers()) {
                    String role = faction.getLeader().equals(memberId) ? "Chef" :
                                 (faction.getOfficers().contains(memberId) ? "Officier" : "Membre");
                    player.sendMessage("§e- " + Bukkit.getOfflinePlayer(memberId).getName() + " §7(" + role + ")");
                }
                player.closeInventory();
            } else if (name.contains("Informations")) {
                player.performCommand("f faction");
                player.closeInventory();
            } else if (name.contains("Claims")) {
                player.performCommand("f map");
                player.closeInventory();
            } else if (name.contains("Permissions")) {
                boolean current = faction.getPermissions().getOrDefault("ALLY_HOME", true);
                faction.getPermissions().put("ALLY_HOME", !current);
                player.sendMessage("§aPermission ALLY_HOME passée à: " + (!current));
                player.closeInventory();
            } else {
                player.sendMessage("§aVous avez cliqué sur: " + name);
            }
        }
    }
}
