package fr.jules.faction.listeners;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.model.Claim;
import fr.jules.faction.model.Faction;
import fr.jules.faction.model.PlayerData;
import org.bukkit.Bukkit;
import java.util.Objects;
import java.util.UUID;
import java.util.List;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

public class PlayerListener implements Listener {
    private final FactionPlugin plugin;

    public PlayerListener(FactionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage("§7[§a+§7] §f" + event.getPlayer().getName());
        plugin.getAfkManager().updateActivity(event.getPlayer());
        PlayerData data = plugin.getDataManager().loadPlayerData(event.getPlayer().getUniqueId());
        if (data == null) {
            data = plugin.getPlayerManager().getPlayerData(event.getPlayer().getUniqueId());
        } else {
            plugin.getPlayerManager().addPlayerData(data);
        }
        data.setName(event.getPlayer().getName());
        data.setLastJoin(System.currentTimeMillis());

        if (data.getFactionId() != null) {
            Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
            if (faction != null) {
                event.getPlayer().sendMessage("§6[MOTD] " + faction.getMotd());
            }
        }
        plugin.getPowerManager().refreshPowerEffects(event.getPlayer());
        plugin.getPlayerManager().applyRankPermissions(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());

        if (player.hasMetadata("vanished")) {
            event.setQuitMessage(null);
        } else {
            event.setQuitMessage("§7[§c-§7] §f" + player.getName());
        }

        // Anti-combat log
        if (System.currentTimeMillis() - data.getCombatLoggedTime() < 15000) {
            player.setHealth(0);
            Bukkit.broadcastMessage("§c" + player.getName() + " s'est déconnecté en combat !");
        }

        plugin.getDataManager().savePlayerData(data);
        plugin.getAfkManager().removePlayer(event.getPlayer().getUniqueId());
        plugin.getPlayerManager().removePlayerData(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(event.getPlayer().getUniqueId());
        if (System.currentTimeMillis() - data.getCombatLoggedTime() < 15000) {
            if (event.getCause() == PlayerTeleportEvent.TeleportCause.COMMAND || event.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("§cVous ne pouvez pas vous téléporter en combat !");
            }
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        plugin.getAfkManager().updateActivity(event.getPlayer());
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        org.bukkit.inventory.ItemStack item = event.getItemDrop().getItemStack();
        if (item.hasItemMeta()) {
            String dn = item.getItemMeta().getDisplayName();
            if (dn.contains("§bVanish") || dn.contains("§bFreeze") || dn.contains("§eInvSee") || dn.contains("§6Outils Modération") || dn.contains("§cQuitter Staff Mode")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Check for intentional movement: rotation OR sneaking OR sprinting
        boolean rotationChanged = event.getFrom().getYaw() != event.getTo().getYaw() || event.getFrom().getPitch() != event.getTo().getPitch();
        boolean activeInput = rotationChanged || player.isSneaking() || player.isSprinting();

        if (activeInput) {
            if (plugin.getAfkManager().isAFK(player)) {
                plugin.getAfkManager().setAFK(player, false);
            }
            plugin.getAfkManager().updateActivity(player);
        }

        PlayerData data = plugin.getPlayerManager().getPlayerData(event.getPlayer().getUniqueId());

        // Power: Lava Speed
        if (data.getActivePower().equals("LAVA_SPEED") && event.getTo().getBlock().getType() == org.bukkit.Material.LAVA) {
            event.getPlayer().addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SPEED, 40, 2, false, false));
        }

        if (event.getFrom().getChunk().equals(event.getTo().getChunk())) return;

        // Territory notification
        Claim oldClaim = plugin.getClaimManager().getClaim(event.getFrom().getWorld().getName(), event.getFrom().getChunk().getX(), event.getFrom().getChunk().getZ());
        Claim newClaim = plugin.getClaimManager().getClaim(event.getTo().getWorld().getName(), event.getTo().getChunk().getX(), event.getTo().getChunk().getZ());

        UUID oldId = oldClaim != null ? oldClaim.getFactionId() : null;
        UUID newId = newClaim != null ? newClaim.getFactionId() : null;

        if (!Objects.equals(oldId, newId) && data.isShowTitles()) {
            if (newId == null) {
                event.getPlayer().sendTitle("§7Nature", "§fTerritoire libre", 10, 40, 10);
            } else {
                Faction f = plugin.getFactionManager().getFaction(newId);
                if (f != null) {
                    String color = "§f";
                    if (data.getFactionId() != null) {
                        if (newId.equals(data.getFactionId())) color = "§a";
                        else {
                            Faction playerFac = plugin.getFactionManager().getFaction(data.getFactionId());
                            String rel = playerFac.getRelations().get(newId);
                            if ("ALLY".equals(rel)) color = "§d";
                            else if ("ENEMY".equals(rel)) color = "§c";
                        }
                    }
                    event.getPlayer().sendTitle(color + f.getName(), "§7" + f.getDescription(), 10, 40, 10);
                }
            }
        }

        // Auto-claim logic: 100% relié au Power
        if (data.isAutoClaim() && data.getFactionId() != null) {
            Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
            if (faction != null && faction.isOfficer(event.getPlayer().getUniqueId())) {
                // Forcer le recalcul du power
                plugin.getFactionManager().recalculatePower(faction, plugin.getPlayerManager());

                // Vérification stricte du Power vs Claims
                if (!data.isBypass()) {
                    if (faction.getClaims().size() >= (int) faction.getPower()) {
                        data.setAutoClaim(false);
                        event.getPlayer().sendMessage("§c§l[TPC Faction] §cAuto-claim désactivé ! Plus assez de Power (§e" + String.format("%.1f", faction.getPower()) + "§c).");
                        return;
                    }
                }

                // Appeler performClaim qui gère le reste (déjà sécurisé)
                fr.jules.faction.commands.FactionCommand cmd = (fr.jules.faction.commands.FactionCommand) plugin.getCommand("f").getExecutor();
                cmd.performClaim(event.getPlayer(), faction, event.getTo().getWorld().getName(), event.getTo().getChunk().getX(), event.getTo().getChunk().getZ(), false);
            }
        }

        // Auto-unclaim logic
        if (data.isAutoUnclaim() && data.getFactionId() != null) {
            Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
            if (faction != null && faction.isOfficer(event.getPlayer().getUniqueId())) {
                Claim c = plugin.getClaimManager().getClaim(event.getTo().getWorld().getName(), event.getTo().getChunk().getX(), event.getTo().getChunk().getZ());
                if (c != null && c.getFactionId().equals(faction.getId())) {
                    plugin.getClaimManager().removeClaim(c.getWorld(), c.getX(), c.getZ());
                    faction.getClaims().remove(c.toString());
                    event.getPlayer().sendMessage("§c§l[TPC Faction] §aParcelle libérée via auto-unclaim.");
                }
            }
        }
    }
}
