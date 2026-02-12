package fr.jules.faction.listeners;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.model.Claim;
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
        if (!canInteract(event.getPlayer(), event.getBlock().getLocation())) {
            event.setCancelled(true);
            MessageUtils.sendMessage(event.getPlayer(), "claim-protection");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!canInteract(event.getPlayer(), event.getBlock().getLocation())) {
            event.setCancelled(true);
            MessageUtils.sendMessage(event.getPlayer(), "claim-protection");
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (!canInteract(event.getPlayer(), event.getClickedBlock().getLocation())) {
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

    private boolean canInteract(Player player, Location loc) {
        String world = loc.getWorld().getName();
        int x = loc.getChunk().getX();
        int z = loc.getChunk().getZ();

        Claim claim = plugin.getClaimManager().getClaim(world, x, z);
        if (claim == null) return true;

        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        return data.getFactionId() != null && data.getFactionId().equals(claim.getFactionId());
    }
}
