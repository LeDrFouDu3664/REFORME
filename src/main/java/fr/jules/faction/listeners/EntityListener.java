package fr.jules.faction.listeners;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.model.Claim;
import fr.jules.faction.model.Faction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import fr.jules.faction.model.PlayerData;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.UUID;

public class EntityListener implements Listener {
    private final FactionPlugin plugin;

    public EntityListener(FactionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        String world = event.getLocation().getWorld().getName();
        int x = event.getLocation().getChunk().getX();
        int z = event.getLocation().getChunk().getZ();

        Claim claim = plugin.getClaimManager().getClaim(world, x, z);
        if (claim == null) return;

        Faction owner = plugin.getFactionManager().getFaction(claim.getFactionId());
        // MOB_GRIEFING flag (true = allowed, false = protected)
        if (owner != null && !owner.getFactionFlags().getOrDefault("MOB_GRIEFING", false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent event) {
        // We keep normal spawn for now, but obey MOB_GRIEFING for certain things
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Tameable tameable && tameable.getOwner() instanceof Player owner) {
            if (entity.getCustomName() != null && entity.getCustomName().contains("§c§l")) {
                PlayerData data = plugin.getPlayerManager().getPlayerData(owner.getUniqueId());
                data.setPetCooldown(System.currentTimeMillis() + 600000); // 10 min if dead
                owner.sendMessage("§cTon compagnon est mort ! Cooldown de 10 minutes.");
            }
        }
    }
}
