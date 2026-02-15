package fr.jules.faction.listeners;

import fr.jules.faction.FactionPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.*;

public class XRayListener implements Listener {
    private final FactionPlugin plugin;
    private final Map<UUID, Integer> diamondCount = new HashMap<>();
    private final Map<UUID, Long> lastMine = new HashMap<>();

    // Ores to hide
    private final Set<Material> hiddenMaterials = EnumSet.of(
            Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
            Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
            Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
            Material.ANCIENT_DEBRIS, Material.NETHER_QUARTZ_ORE
    );

    public XRayListener(FactionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // 1. Reveal nearby ores to the player
        revealNearby(player, block.getLocation());

        // 2. Behavioral detection
        if (player.hasPermission("faction.staff")) return;

        Material type = block.getType();
        if (hiddenMaterials.contains(type)) {
            // Heuristic: Check if the ore was "hidden" (not exposed)
            if (!isExposed(block)) {
                alertStaff(player, "Minerai caché détecté (X-Ray probable)");
            }

            // Mining statistics
            if (type == Material.DIAMOND_ORE || type == Material.DEEPSLATE_DIAMOND_ORE) {
                long now = System.currentTimeMillis();
                long last = lastMine.getOrDefault(player.getUniqueId(), 0L);

                if (now - last > 60000) {
                    diamondCount.put(player.getUniqueId(), 1);
                } else {
                    int count = diamondCount.getOrDefault(player.getUniqueId(), 0) + 1;
                    diamondCount.put(player.getUniqueId(), count);
                    if (count >= 6) alertStaff(player, "Vitesse minage: " + count + " diamants/min");
                }
                lastMine.put(player.getUniqueId(), now);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player = event.getPlayer();
        if (player.hasPermission("faction.staff")) return;

        // Periodically obfuscate hidden ores around the player
        obfuscateAround(player, event.getTo());
    }

    private void obfuscateAround(Player player, Location loc) {
        int radius = 4;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block b = loc.clone().add(x, y, z).getBlock();
                    if (hiddenMaterials.contains(b.getType()) && !isExposed(b)) {
                        // Send fake block change: Stone instead of Ore
                        Material fake = b.getWorld().getName().contains("nether") ? Material.NETHERRACK :
                                       (b.getY() < 0 ? Material.DEEPSLATE : Material.STONE);
                        player.sendBlockChange(b.getLocation(), fake.createBlockData());
                    }
                }
            }
        }
    }

    private void revealNearby(Player player, Location loc) {
        int radius = 2;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block b = loc.clone().add(x, y, z).getBlock();
                    if (hiddenMaterials.contains(b.getType())) {
                        player.sendBlockChange(b.getLocation(), b.getBlockData());
                    }
                }
            }
        }
    }

    private boolean isExposed(Block block) {
        for (BlockFace face : BlockFace.values()) {
            if (face.isCartesian()) {
                Material adjacent = block.getRelative(face).getType();
                if (adjacent == Material.AIR || adjacent == Material.CAVE_AIR ||
                    adjacent == Material.WATER || adjacent == Material.LAVA) {
                    return true;
                }
            }
        }
        return false;
    }

    private void alertStaff(Player suspect, String msg) {
        String alert = "§c§l[TPC X-Ray] §e" + suspect.getName() + " §7- §6" + msg;
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("faction.staff"))
                .forEach(p -> p.sendMessage(alert));
    }
}
