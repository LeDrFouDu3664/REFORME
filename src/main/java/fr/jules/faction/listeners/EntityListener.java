package fr.jules.faction.listeners;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.model.Claim;
import fr.jules.faction.model.Faction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Animals;

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
        if (owner != null && !owner.getFactionFlags().getOrDefault("explosions", true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent event) {
        String world = event.getLocation().getWorld().getName();
        int x = event.getLocation().getChunk().getX();
        int z = event.getLocation().getChunk().getZ();

        Claim claim = plugin.getClaimManager().getClaim(world, x, z);
        if (claim == null) return;

        Faction owner = plugin.getFactionManager().getFaction(claim.getFactionId());
        if (owner == null) return;

        if (event.getEntity() instanceof Monster && !owner.getFactionFlags().getOrDefault("monsters", true)) {
            event.setCancelled(true);
        } else if (event.getEntity() instanceof Animals && !owner.getFactionFlags().getOrDefault("animals", true)) {
            event.setCancelled(true);
        }
    }
}
