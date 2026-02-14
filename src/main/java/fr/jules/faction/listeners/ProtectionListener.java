package fr.jules.faction.listeners;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.model.Claim;
import fr.jules.faction.model.Faction;
import fr.jules.faction.model.PlayerData;
import fr.jules.faction.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class ProtectionListener implements Listener {
    private final FactionPlugin plugin;

    public ProtectionListener(FactionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!canPerformAction(event.getPlayer(), event.getBlock().getLocation(), "DESTROY")) {
            event.setCancelled(true);
            MessageUtils.sendMessage(event.getPlayer(), "claim-protection");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!canPerformAction(event.getPlayer(), event.getBlock().getLocation(), "BUILD")) {
            event.setCancelled(true);
            MessageUtils.sendMessage(event.getPlayer(), "claim-protection");
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (!canPerformAction(event.getPlayer(), event.getClickedBlock().getLocation(), "USE")) {
            if (event.getClickedBlock().getType().name().contains("CHEST") ||
                event.getClickedBlock().getType().name().contains("DOOR") ||
                event.getClickedBlock().getType().name().contains("BUTTON") ||
                event.getClickedBlock().getType().name().contains("LEVER") ||
                event.getClickedBlock().getType().name().contains("GATE")) {
                event.setCancelled(true);
                MessageUtils.sendMessage(event.getPlayer(), "claim-protection");
            }
        }
    }

    private boolean canPerformAction(Player player, Location loc, String action) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.isBypass()) return true;

        String world = loc.getWorld().getName();
        int x = loc.getChunk().getX();
        int z = loc.getChunk().getZ();

        Claim claim = plugin.getClaimManager().getClaim(world, x, z);
        if (claim == null) return true;

        Faction owner = plugin.getFactionManager().getFaction(claim.getFactionId());
        if (owner == null) return true;

        if (owner.getType() == fr.jules.faction.model.FactionType.SAFEZONE || owner.getType() == fr.jules.faction.model.FactionType.WARZONE) {
            return player.hasPermission("faction.admin.build");
        }

        if (owner.getPower() < owner.getClaims().size()) {
            return true; // Territory is raidable
        }

        if (data.getFactionId() != null && data.getFactionId().equals(claim.getFactionId())) {
            return owner.hasPermission(data.getRole(), action);
        }

        return false;
    }
}
