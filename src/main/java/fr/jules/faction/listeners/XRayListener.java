package fr.jules.faction.listeners;

import fr.jules.faction.FactionPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class XRayListener implements Listener {
    private final FactionPlugin plugin;
    private final Map<UUID, Integer> diamondCount = new HashMap<>();
    private final Map<UUID, Long> lastMine = new HashMap<>();

    public XRayListener(FactionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("faction.staff")) return;

        Material type = event.getBlock().getType();
        if (type == Material.DIAMOND_ORE || type == Material.DEEPSLATE_DIAMOND_ORE || type == Material.GOLD_ORE || type == Material.DEEPSLATE_GOLD_ORE) {

            // Heuristic 1: Check if the ore was "hidden" (not exposed to air/water/etc)
            boolean exposed = false;
            for (org.bukkit.block.BlockFace face : org.bukkit.block.BlockFace.values()) {
                if (face.isCartesian() && event.getBlock().getRelative(face).isPassable()) {
                    exposed = true;
                    break;
                }
            }

            if (!exposed) {
                 // Player mined a completely hidden ore. This is very suspicious.
                 alertStaff(player, "Minerai caché détecté (X-Ray probable)");
            }

            // Heuristic 2: Mining statistics
            if (type == Material.DIAMOND_ORE || type == Material.DEEPSLATE_DIAMOND_ORE) {
                long now = System.currentTimeMillis();
                long last = lastMine.getOrDefault(player.getUniqueId(), 0L);

                if (now - last > 60000) { // Reset every minute
                    diamondCount.put(player.getUniqueId(), 1);
                } else {
                    int count = diamondCount.getOrDefault(player.getUniqueId(), 0) + 1;
                    diamondCount.put(player.getUniqueId(), count);

                    if (count >= 6) { // Stricter alert: 6+ diamonds mined in < 1 minute
                        alertStaff(player, "Stats suspectes: " + count + " diamants/min");
                    }
                }
                lastMine.put(player.getUniqueId(), now);
            }
        }
    }

    private void alertStaff(Player suspect, String msg_suffix) {
        String msg = "§c§l[TPC X-Ray] §e" + suspect.getName() + " §7- §6" + msg_suffix;
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("faction.staff"))
                .forEach(p -> p.sendMessage(msg));
    }

}
