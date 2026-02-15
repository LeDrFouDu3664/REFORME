package fr.jules.faction.listeners;

import fr.jules.faction.FactionPlugin;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SpawnerListener implements Listener {
    private final FactionPlugin plugin;

    public SpawnerListener(FactionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            Block block = event.getClickedBlock();
            if (block.getType() == Material.SPAWNER) {
                if (event.getPlayer().isSneaking()) {
                    event.setCancelled(true);
                    fr.jules.faction.gui.SpawnerGUI.openSpawnerMenu(event.getPlayer(), block);
                }
            }
        }
    }

    @EventHandler
    public void onSpawn(SpawnerSpawnEvent event) {
        if (event.getSpawner().getBlock().hasMetadata("inactive")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBreak(org.bukkit.event.block.BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.SPAWNER) return;

        org.bukkit.entity.Player player = event.getPlayer();
        org.bukkit.inventory.ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.DIAMOND_PICKAXE && item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Pioche à Spawner")) {
            event.setDropItems(false);
            giveSpawner(player, block);
        }
    }

    private void giveSpawner(org.bukkit.entity.Player player, Block block) {
        org.bukkit.block.CreatureSpawner cs = (org.bukkit.block.CreatureSpawner) block.getState();
        org.bukkit.entity.EntityType type = cs.getSpawnedType();

        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(Material.SPAWNER);
        org.bukkit.inventory.meta.BlockStateMeta meta = (org.bukkit.inventory.meta.BlockStateMeta) item.getItemMeta();
        org.bukkit.block.CreatureSpawner metaCs = (org.bukkit.block.CreatureSpawner) meta.getBlockState();
        metaCs.setSpawnedType(type);
        meta.setBlockState(metaCs);
        meta.setDisplayName("§eSpawner: §b" + type.name());
        item.setItemMeta(meta);

        block.getWorld().dropItemNaturally(block.getLocation(), item);
        player.sendMessage("§aSpawner récupéré !");
    }
}
