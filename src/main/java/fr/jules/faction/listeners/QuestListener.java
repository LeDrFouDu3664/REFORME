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

        if (mat.name().contains("ORE") || mat == Material.STONE || mat == Material.DEEPSLATE || mat == Material.COBBLESTONE) {
            plugin.getQuestManager().progressQuest(player, "MINER_100", 1);
        }

        if (mat.name().contains("LOG") || mat.name().contains("WOOD")) {
            plugin.getQuestManager().progressQuest(player, "WOOD_50", 1);
        }
    }

    @EventHandler
    public void onKill(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            plugin.getQuestManager().progressQuest(killer, "KILL_10", 1);
        }
    }
}
