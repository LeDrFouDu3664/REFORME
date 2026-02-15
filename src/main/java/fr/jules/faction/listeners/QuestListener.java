package fr.jules.faction.listeners;

import fr.jules.faction.FactionPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class QuestListener implements Listener {
    private final FactionPlugin plugin;

    public QuestListener(FactionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material mat = event.getBlock().getType();

        if (mat.name().contains("ORE")) {
            plugin.getQuestManager().progressQuest(player, "MINER_JOB", 1);
        }

        if (mat.name().contains("LOG") || mat.name().contains("WOOD")) {
            plugin.getQuestManager().progressQuest(player, "WOOD_JOB", 1);
        }

        if (mat == Material.WHEAT || mat == Material.CARROTS || mat == Material.POTATOES || mat == Material.NETHER_WART || mat == Material.SUGAR_CANE) {
            plugin.getQuestManager().progressQuest(player, "FARM_JOB", 1);
        }
    }

    @EventHandler
    public void onKill(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            plugin.getQuestManager().progressQuest(killer, "WARRIOR_JOB", 1);
        }
    }

    @EventHandler
    public void onMobKill(org.bukkit.event.entity.EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null && !(event.getEntity() instanceof Player)) {
            plugin.getQuestManager().progressQuest(killer, "KILL_MOBS", 1);
        }
    }
}
