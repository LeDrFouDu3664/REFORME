package fr.jules.faction.listeners;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.model.Claim;
import fr.jules.faction.model.Faction;
import fr.jules.faction.model.PlayerData;
import fr.jules.faction.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PvPListener implements Listener {
    private final FactionPlugin plugin;

    public PvPListener(FactionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFallDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.FALL) {
            PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
            if (data.getPowers().contains("NO_FALL")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPvP(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player target && event.getDamager() instanceof Player attacker)) return;

        PlayerData attackerData = plugin.getPlayerManager().getPlayerData(attacker.getUniqueId());
        PlayerData targetData = plugin.getPlayerManager().getPlayerData(target.getUniqueId());

        if (attackerData.getFactionId() == null || targetData.getFactionId() == null) return;

        if (attackerData.getFactionId().equals(targetData.getFactionId())) {
            MessageUtils.sendMessage(attacker, "pvp-denied-same-faction");
            event.setCancelled(true);
            return;
        }

        Faction attackerFaction = plugin.getFactionManager().getFaction(attackerData.getFactionId());
        Faction targetFaction = plugin.getFactionManager().getFaction(targetData.getFactionId());

        // SafeZone check
        Claim targetClaim = plugin.getClaimManager().getClaim(target.getWorld().getName(), target.getLocation().getChunk().getX(), target.getLocation().getChunk().getZ());
        if (targetClaim != null) {
            Faction owner = plugin.getFactionManager().getFaction(targetClaim.getFactionId());
            if (owner != null && owner.getType() == fr.jules.faction.model.FactionType.SAFEZONE) {
                MessageUtils.sendMessage(attacker, "pvp-denied-safezone");
                event.setCancelled(true);
                return;
            }
        }

        String rel1 = attackerFaction.getRelations().get(targetData.getFactionId());
        String rel2 = targetFaction.getRelations().get(attackerData.getFactionId());

        String ally = fr.jules.faction.model.Relation.ALLY.name();
        String truce = fr.jules.faction.model.Relation.TRUCE.name();

        if ((ally.equals(rel1) && ally.equals(rel2)) || (truce.equals(rel1) && truce.equals(rel2))) {
            // Check friendly fire flag
            if (!attackerFaction.getFactionFlags().getOrDefault("friendlyFire", false)) {
                MessageUtils.sendMessage(attacker, "pvp-denied-relation");
                event.setCancelled(true);
                return;
            }
        }

        // Powers logic
        if (attackerData.getPowers().contains("VAMPIRE")) {
            attacker.setHealth(Math.min(attacker.getMaxHealth(), attacker.getHealth() + (event.getFinalDamage() * 0.1)));
        }

        if (attackerData.getPowers().contains("STUN") && Math.random() < 0.1) {
            target.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SLOW, 40, 2));
            target.sendMessage("§cVous avez été étourdi !");
            attacker.sendMessage("§aVous avez étourdi votre cible !");
        }

        // Combat tagging
        attackerData.setCombatLoggedTime(System.currentTimeMillis());
        targetData.setCombatLoggedTime(System.currentTimeMillis());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        double loss = plugin.getConfig().getDouble("settings.power.per-death", 2.0);
        data.removePower(loss);
        MessageUtils.sendMessage(player, "power-loss",
                "%amount%", String.valueOf(loss),
                "%current%", String.format("%.1f", data.getPower()));

        if (data.getFactionId() != null) {
            Faction f = plugin.getFactionManager().getFaction(data.getFactionId());
            if (f != null) {
                plugin.getFactionManager().recalculatePower(f, plugin.getPlayerManager());
            }
        }
    }
}
