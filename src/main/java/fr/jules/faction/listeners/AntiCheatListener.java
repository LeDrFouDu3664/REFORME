package fr.jules.faction.listeners;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.model.BanData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AntiCheatListener implements Listener {
    private final FactionPlugin plugin;
    private final Map<UUID, Long> airTime = new HashMap<>();
    private final Map<UUID, Double> fallDistance = new HashMap<>();
    private final Map<UUID, Integer> violations = new HashMap<>();
    private final Map<UUID, Long> lastClick = new HashMap<>();
    private final Map<UUID, Integer> clickCount = new HashMap<>();
    private final Map<UUID, Integer> breakCount = new HashMap<>();

    public AntiCheatListener(FactionPlugin plugin) {
        this.plugin = plugin;
        startDecayTask();
    }

    private void startDecayTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            violations.entrySet().removeIf(entry -> {
                int newVal = entry.getValue() - 5;
                if (newVal <= 0) return true;
                entry.setValue(newVal);
                return false;
            });
        }, 20 * 60, 20 * 60); // Every minute, reduce violations by 5
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("faction.staff") || player.getAllowFlight() || player.isInsideVehicle()) return;

        // Grace period after damage
        if (player.getNoDamageTicks() > 10) return;

        // 1. Detection Fly / AirJump / Airstrike+
        if (event.getTo().getY() > event.getFrom().getY() && !player.getLocation().getBlock().isLiquid() && player.getVelocity().getY() >= 0) {
            Material ground = player.getLocation().subtract(0, 0.1, 0).getBlock().getType();
            if (ground == Material.AIR) {
                long time = airTime.getOrDefault(player.getUniqueId(), 0L);
                if (time == 0) airTime.put(player.getUniqueId(), System.currentTimeMillis());
                else if (System.currentTimeMillis() - time > 4000) {
                    flag(player, "Fly / AirJump / Airstrike+", 2);
                    airTime.put(player.getUniqueId(), System.currentTimeMillis());
                }
            } else {
                airTime.remove(player.getUniqueId());
            }
        }

        // 2. Detection Speed / Sprint / Step
        double deltaX = event.getTo().getX() - event.getFrom().getX();
        double deltaZ = event.getTo().getZ() - event.getFrom().getZ();
        double deltaY = event.getTo().getY() - event.getFrom().getY();
        double dist = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        double limit = player.isSprinting() ? 1.3 : 1.0;
        if (dist > limit && !player.isFlying()) {
             flag(player, "Speed / Step / Sprint", 2);
        }

        // 3. Jesus / Liquid Filler
        if (player.getLocation().getBlock().isLiquid() && deltaY == 0 && dist > 0.2) {
            flag(player, "Jesus / LiquidFiller", 4);
        }

        // 4. Spider / FastClimb (More lenient: requires low horizontal movement + steep Y)
        if (deltaY > 0.5 && player.getLocation().getBlock().getType() == Material.AIR && dist < 0.1) {
             Material wall = player.getLocation().add(player.getLocation().getDirection().multiply(0.5)).getBlock().getType();
             if (wall.isSolid() && wall != Material.LADDER && wall != Material.VINE) {
                 flag(player, "Spider / FastClimb", 2);
             }
        }

        // 5. Fall tracker for NoFall / PortalGodMode
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
                flag(player, "NoFall / PortalGodMode", 5);
            }
            fallDistance.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            if (attacker.hasPermission("faction.staff")) return;

            // Reach / Hitboxes
            double reach = attacker.getLocation().distance(event.getEntity().getLocation());
            if (reach > 5.0) {
                event.setCancelled(true);
                flag(attacker, "Reach / Hitboxes / KillAura", 6);
            }

            // KillAura (Angle check)
            org.bukkit.util.Vector dir = attacker.getLocation().getDirection();
            org.bukkit.util.Vector toEntity = event.getEntity().getLocation().toVector().subtract(attacker.getLocation().toVector());
            double angle = dir.angle(toEntity);
            if (angle > 1.2) { // Roughly > 70 degrees
                 flag(attacker, "KillAura / AimBot / MaceKill", 4);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("faction.staff")) return;

        // AutoClicker detection
        long now = System.currentTimeMillis();
        long last = lastClick.getOrDefault(player.getUniqueId(), 0L);
        if (now - last < 50) { // < 20 CPS roughly
            int clicks = clickCount.getOrDefault(player.getUniqueId(), 0) + 1;
            clickCount.put(player.getUniqueId(), clicks);
            if (clicks > 15) {
                flag(player, "AutoClicker / BowSpam / MachineGun", 2);
                clickCount.put(player.getUniqueId(), 0);
            }
        } else {
            clickCount.put(player.getUniqueId(), 0);
        }
        lastClick.put(player.getUniqueId(), now);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("faction.staff")) return;

        // Nuker / InstaMine / SpeedMine
        int breaks = breakCount.getOrDefault(player.getUniqueId(), 0) + 1;
        breakCount.put(player.getUniqueId(), breaks);
        if (breaks > 5) { // More than 5 blocks in one tick (or very fast)
             flag(player, "Nuker / InstaMine / SpeedMine / Excavator", 5);
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> breakCount.put(player.getUniqueId(), 0), 1L);
    }

    private void flag(Player suspect, String cheat, int violationPoints) {
        int total = violations.getOrDefault(suspect.getUniqueId(), 0) + violationPoints;
        violations.put(suspect.getUniqueId(), total);

        String msg = "§c§l[AntiCheat] §e" + suspect.getName() + " §7flagged §6" + cheat + " §8(x" + total + ")";
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("faction.staff"))
                .forEach(p -> p.sendMessage(msg));

        if (total >= 200) {
            autoPunish(suspect, cheat);
        }
    }

    private void autoPunish(Player suspect, String cheat) {
        String reason = "[AntiCheat] " + cheat;
        String banId = "#AC-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        BanData data = new BanData(banId, suspect.getName(), suspect.getUniqueId(), suspect.getAddress().getAddress().getHostAddress(), reason, "Console", -1, System.currentTimeMillis());

        Bukkit.getScheduler().runTask(plugin, () -> {
            plugin.getBanManager().ban(data);
            suspect.kickPlayer(fr.jules.faction.commands.BanCommand.formatBanMessage(data));
            Bukkit.broadcastMessage("§c§l[AntiCheat] §e" + suspect.getName() + " §7a été banni définitivement pour §6" + cheat);
        });
        violations.remove(suspect.getUniqueId());
    }
}
