package fr.jules.faction.listeners;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.model.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class JobListener implements Listener {
    private final FactionPlugin plugin;

    public JobListener(FactionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        String job = data.getJob();
        Material mat = event.getBlock().getType();

        if (job.equals("MINEUR")) {
            if (mat.name().contains("ORE") || mat == Material.STONE || mat == Material.COBBLESTONE || mat == Material.DEEPSLATE) {
                awardExp(player, data, 2);
            }
        } else if (job.equals("BUCHERON")) {
            if (mat.name().contains("LOG") || mat.name().contains("WOOD")) {
                awardExp(player, data, 2);
            }
        } else if (job.equals("FERMIER")) {
            if (mat == Material.WHEAT || mat == Material.CARROTS || mat == Material.POTATOES || mat == Material.NETHER_WART || mat == Material.SUGAR_CANE) {
                awardExp(player, data, 3);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        PlayerData data = plugin.getPlayerManager().getPlayerData(killer.getUniqueId());
        if (data.getJob().equals("GUERRIER")) {
            if (event.getEntity() instanceof Player) {
                awardExp(killer, data, 50);
            } else {
                awardExp(killer, data, 5);
            }
        }
    }

    private void awardExp(Player player, PlayerData data, double amount) {
        data.setJobExp(data.getJobExp() + amount);
        double nextLevelExp = data.getJobLevel() * 100 * 1.5;
        if (data.getJobExp() >= nextLevelExp) {
            data.setJobExp(data.getJobExp() - nextLevelExp);
            data.setJobLevel(data.getJobLevel() + 1);
            player.sendMessage("§b§l[Métier] §aFélicitations ! Vous passez au niveau §e" + data.getJobLevel() + " §aen tant que §e" + data.getJob() + "§a !");
            plugin.getEconomyManager().deposit(player, data.getJobLevel() * 100);
            player.sendMessage("§7Vous avez reçu §e" + (data.getJobLevel() * 100) + "$ §7en récompense.");
        }
    }
}
