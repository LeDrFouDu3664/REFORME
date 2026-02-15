package fr.jules.faction.listeners;

import fr.jules.faction.FactionPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class ModerationListener implements Listener {
    private final FactionPlugin plugin;

    public ModerationListener(FactionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getPlayer().hasMetadata("frozen")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cVous êtes gelé !");
        }
    }

    @EventHandler
    public void onDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (player.hasMetadata("godmode")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        Player staff = event.getPlayer();
        if (!staff.hasPermission("faction.staff")) return;

        org.bukkit.inventory.ItemStack item = staff.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return;

        String name = item.getItemMeta().getDisplayName();

        if (event.getRightClicked() instanceof Player target) {
            event.setCancelled(true);
            if (name.contains("Bâton") || name.contains("Modération")) {
                fr.jules.faction.gui.ModGUI.openPlayerActions(staff, target);
            } else if (name.contains("Freeze")) {
                staff.performCommand("mod freeze " + target.getName()); // I should implement this sub or just call the logic
                toggleFreeze(target, staff);
            } else if (name.contains("InvSee")) {
                staff.openInventory(target.getInventory());
            }
        }
    }

    @EventHandler
    public void onStaffItemUse(org.bukkit.event.player.PlayerInteractEvent event) {
        Player staff = event.getPlayer();
        if (!staff.hasPermission("faction.staff")) return;

        org.bukkit.inventory.ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        String name = item.getItemMeta().getDisplayName();

        if (name.contains("Vanish")) {
            event.setCancelled(true);
            toggleVanish(staff);
        } else if (name.contains("Outils Modération")) {
            event.setCancelled(true);
            fr.jules.faction.gui.ModGUI.openModMenu(staff);
        } else if (name.contains("Quitter Staff Mode")) {
            event.setCancelled(true);
            staff.performCommand("mod");
        }
    }

    private void toggleVanish(Player staff) {
        if (staff.hasMetadata("vanished")) {
            staff.removeMetadata("vanished", plugin);
            org.bukkit.Bukkit.getOnlinePlayers().forEach(p -> p.showPlayer(plugin, staff));
            staff.sendMessage("§aVanish: §cDésactivé");
        } else {
            staff.setMetadata("vanished", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
            org.bukkit.Bukkit.getOnlinePlayers().forEach(p -> p.hidePlayer(plugin, staff));
            staff.sendMessage("§aVanish: §aActivé");
        }
    }

    private void toggleFreeze(Player target, Player staff) {
        if (target.hasMetadata("frozen")) {
            target.removeMetadata("frozen", plugin);
            target.sendMessage("§aVous avez été libéré !");
            staff.sendMessage("§aJoueur libéré.");
        } else {
            target.setMetadata("frozen", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
            target.sendMessage("§cVous avez été gelé par un modérateur !");
            staff.sendMessage("§cJoueur gelé.");
        }
    }
}
