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
    public void onInteract(PlayerInteractEntityEvent event) {
        Player staff = event.getPlayer();
        if (!staff.hasPermission("faction.staff")) return;

        org.bukkit.inventory.ItemStack item = staff.getInventory().getItemInMainHand();
        if (item.getType() == Material.BLAZE_ROD && item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Bâton de Modération")) {
            if (event.getRightClicked() instanceof Player target) {
                event.setCancelled(true);
                fr.jules.faction.gui.ModGUI.openPlayerActions(staff, target);
            }
        }
    }
}
