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
            if (player.hasMetadata("godmode") || plugin.getAfkManager().isAFK(player)) {
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

        String staffId = item.getItemMeta().getPersistentDataContainer().get(
            new org.bukkit.NamespacedKey(plugin, "staff_item"),
            org.bukkit.persistence.PersistentDataType.STRING
        );
        if (staffId == null) return;

        if (event.getRightClicked() instanceof Player target) {
            event.setCancelled(true);
            switch (staffId) {
                case "STAFF_BATON":
                    fr.jules.faction.gui.ModGUI.openPlayerActions(staff, target);
                    break;
                case "STAFF_FREEZE":
                    toggleFreeze(target, staff);
                    break;
                case "STAFF_INVSEE":
                    staff.openInventory(target.getInventory());
                    break;
            }
        }
    }

    @EventHandler
    public void onStaffItemUse(org.bukkit.event.player.PlayerInteractEvent event) {
        Player staff = event.getPlayer();
        if (!staff.hasPermission("faction.staff")) return;

        org.bukkit.inventory.ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        String staffId = item.getItemMeta().getPersistentDataContainer().get(
            new org.bukkit.NamespacedKey(plugin, "staff_item"),
            org.bukkit.persistence.PersistentDataType.STRING
        );
        if (staffId == null) return;

        event.setCancelled(true);

        if (event.getAction().name().contains("RIGHT")) {
            switch (staffId) {
                case "STAFF_VANISH":
                    plugin.getVanishManager().toggleVanish(staff);
                    break;
                case "STAFF_TOOLS":
                    fr.jules.faction.gui.ModGUI.openModMenu(staff);
                    break;
                case "STAFF_EXIT":
                    staff.performCommand("mod");
                    break;
            }
        }
    }

    private void toggleVanish(Player staff) {
        plugin.getVanishManager().toggleVanish(staff);
    }

    private void toggleFreeze(Player target, Player staff) {
        fr.jules.faction.modules.staff.StaffModule staffMod = (fr.jules.faction.modules.staff.StaffModule) plugin.getModuleManager().getModule("Staff");
        if (target.hasMetadata("frozen")) {
            target.removeMetadata("frozen", plugin);
            target.sendMessage(staffMod.getConfig().getString("messages.freeze-off"));
            staff.sendMessage(staffMod.getConfig().getString("messages.freeze-staff-off"));
        } else {
            target.setMetadata("frozen", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
            target.sendMessage(staffMod.getConfig().getString("messages.freeze-on"));
            staff.sendMessage(staffMod.getConfig().getString("messages.freeze-staff-on"));
        }
    }
}
