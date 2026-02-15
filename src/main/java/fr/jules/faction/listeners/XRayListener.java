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
        if (type == Material.DIAMOND_ORE || type == Material.DEEPSLATE_DIAMOND_ORE) {
            long now = System.currentTimeMillis();
            long last = lastMine.getOrDefault(player.getUniqueId(), 0L);

            if (now - last > 60000) { // Reset every minute
                diamondCount.put(player.getUniqueId(), 1);
            } else {
                int count = diamondCount.getOrDefault(player.getUniqueId(), 0) + 1;
                diamondCount.put(player.getUniqueId(), count);

                if (count >= 8) { // Alert if 8+ diamonds mined in < 1 minute
                    alertStaff(player, count);
                }
            }
            lastMine.put(player.getUniqueId(), now);
        }
    }

    private void alertStaff(Player suspect, int count) {
        String msg = "§c§l[TPC X-Ray] §e" + suspect.getName() + " §7a miné §b" + count + " §7diamants très rapidement !";
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("faction.staff"))
                .forEach(p -> p.sendMessage(msg));
    }
}
