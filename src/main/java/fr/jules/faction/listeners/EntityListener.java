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
        if (entity.hasMetadata("is_pet")) {
            event.getDrops().clear();
            event.setDroppedExp(0);

            String uuidStr = entity.getMetadata("owner_uuid").get(0).asString();
            Player owner = org.bukkit.Bukkit.getPlayer(UUID.fromString(uuidStr));

            if (owner != null) {
                PlayerData data = plugin.getPlayerManager().getPlayerData(owner.getUniqueId());
                String petId = data.getCurrentPet();
                if (petId != null) {
                    fr.jules.faction.model.PetInfo info = data.getCapturedPets().get(petId);
                    if (info != null) {
                        info.setLastDeath(System.currentTimeMillis());

                        long cooldown;
                        String message;

                        if (event.getEntity().getKiller() != null && event.getEntity().getKiller().equals(owner)) {
                            cooldown = 600000; // 10 min
                            message = "§cTu as tué ton propre compagnon ! Cooldown de 10 minutes.";
                        } else {
                            cooldown = 60000; // 1 min
                            message = "§cTon compagnon a été tué ! Cooldown de 1 minute.";
                        }

                        data.setPetCooldown(System.currentTimeMillis() + cooldown);
                        owner.sendMessage(message);
                    }
                }
            }
        }
    }
}
