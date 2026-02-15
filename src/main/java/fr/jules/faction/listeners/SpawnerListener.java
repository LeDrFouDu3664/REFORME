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
}
