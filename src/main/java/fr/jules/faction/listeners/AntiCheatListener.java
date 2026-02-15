package fr.jules.faction.listeners;

import fr.jules.faction.FactionPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AntiCheatListener implements Listener {
    private final FactionPlugin plugin;
    private final Map<UUID, Long> airTime = new HashMap<>();

    public AntiCheatListener(FactionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("faction.staff") || player.getAllowFlight()) return;

        // Detection Fly
        if (event.getTo().getY() > event.getFrom().getY() && player.getLocation().subtract(0, 1, 0).getBlock().getType() == Material.AIR) {
            long time = airTime.getOrDefault(player.getUniqueId(), 0L);
            if (time == 0) airTime.put(player.getUniqueId(), System.currentTimeMillis());
            else if (System.currentTimeMillis() - time > 2000) {
                alertStaff(player, "Fly / Speed Vertical");
                airTime.put(player.getUniqueId(), System.currentTimeMillis());
            }
        } else {
            airTime.remove(player.getUniqueId());
        }

        // Detection Speed (Horizontal)
        double deltaX = event.getTo().getX() - event.getFrom().getX();
        double deltaZ = event.getTo().getZ() - event.getFrom().getZ();
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        if (distance > 0.6 && !player.isSprinting() && !player.isFlying()) {
             alertStaff(player, "Speed (Horizontal)");
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            if (attacker.hasPermission("faction.staff")) return;

            double distance = attacker.getLocation().distance(event.getEntity().getLocation());
            if (distance > 5.0) {
                event.setCancelled(true);
                alertStaff(attacker, "Reach (" + String.format("%.1f", distance) + " blocks)");
            }
        }
    }

    private void alertStaff(Player suspect, String cheat) {
        String msg = "§c§l[TPC AntiCheat] §e" + suspect.getName() + " §7suspecté de: §6" + cheat;
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("faction.staff"))
                .forEach(p -> p.sendMessage(msg));
        plugin.getLogger().warning("[AntiCheat] " + suspect.getName() + " flagged for " + cheat);
    }
}
