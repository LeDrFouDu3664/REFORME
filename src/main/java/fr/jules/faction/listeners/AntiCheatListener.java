package fr.jules.faction.listeners;

import fr.jules.faction.FactionPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AntiCheatListener implements Listener {
    private final FactionPlugin plugin;
    private final Map<UUID, Long> airTime = new HashMap<>();
    private final Map<UUID, Double> fallDistance = new HashMap<>();

    public AntiCheatListener(FactionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("faction.staff") || player.getAllowFlight() || player.isInsideVehicle()) return;

        // 1. Detection Fly
        if (event.getTo().getY() > event.getFrom().getY() && !player.getLocation().getBlock().isLiquid() && player.getVelocity().getY() >= 0) {
            Material ground = player.getLocation().subtract(0, 0.1, 0).getBlock().getType();
            if (ground == Material.AIR) {
                long time = airTime.getOrDefault(player.getUniqueId(), 0L);
                if (time == 0) airTime.put(player.getUniqueId(), System.currentTimeMillis());
                else if (System.currentTimeMillis() - time > 1500) {
                    alertStaff(player, "Fly / Ascent");
                    airTime.put(player.getUniqueId(), System.currentTimeMillis());
                }
            } else {
                airTime.remove(player.getUniqueId());
            }
        }

        // 2. Detection Speed
        double deltaX = event.getTo().getX() - event.getFrom().getX();
        double deltaZ = event.getTo().getZ() - event.getFrom().getZ();
        double dist = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        double limit = player.isSprinting() ? 0.75 : 0.5;
        if (dist > limit && !player.isFlying()) {
             alertStaff(player, "Speed (" + String.format("%.2f", dist) + " blocks/move)");
        }

        // 3. Fall tracker for NoFall
        if (player.getFallDistance() > 3.0) {
            fallDistance.put(player.getUniqueId(), (double) player.getFallDistance());
        }
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.hasPermission("faction.staff")) return;

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            double distance = fallDistance.getOrDefault(player.getUniqueId(), 0.0);
            if (distance > 4.0 && event.getDamage() < 1.0) {
                alertStaff(player, "NoFall (Chute: " + String.format("%.1f", distance) + "m)");
            }
            fallDistance.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            if (attacker.hasPermission("faction.staff")) return;

            double reach = attacker.getLocation().distance(event.getEntity().getLocation());
            if (reach > 5.5) { // Strict reach check
                event.setCancelled(true);
                alertStaff(attacker, "Reach (" + String.format("%.1f", reach) + " blocks)");
            }
        }
    }

    private void alertStaff(Player suspect, String cheat) {
        String msg = "§c§l[TPC AntiCheat] §e" + suspect.getName() + " §7- §6" + cheat;
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("faction.staff"))
                .forEach(p -> p.sendMessage(msg));
        plugin.getLogger().warning("[AntiCheat] " + suspect.getName() + " flagged for " + cheat);
    }
}
