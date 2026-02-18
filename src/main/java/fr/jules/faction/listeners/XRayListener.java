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

    public XRayListener(FactionPlugin plugin) {
        this.plugin = plugin;
        // Reset every 5 minutes
        Bukkit.getScheduler().runTaskTimer(plugin, diamondCount::clear, 6000, 6000);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Material type = event.getBlock().getType();
        if (type == Material.DIAMOND_ORE || type == Material.DEEPSLATE_DIAMOND_ORE) {
            Player player = event.getPlayer();
            int count = diamondCount.getOrDefault(player.getUniqueId(), 0) + 1;
            diamondCount.put(player.getUniqueId(), count);

            if (count > 20) { // More than 20 diamonds in 5 minutes
                String msg = "§c§l[Anti-Xray] §e" + player.getName() + " §7mine beaucoup de diamants ! (§c" + count + "§7)";
                Bukkit.getOnlinePlayers().stream()
                        .filter(p -> p.hasPermission("faction.staff"))
                        .forEach(p -> p.sendMessage(msg));
            }
        }
    }
}
