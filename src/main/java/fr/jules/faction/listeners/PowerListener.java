package fr.jules.faction.listeners;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.model.PlayerData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public class PowerListener implements Listener {
    private final FactionPlugin plugin;

    public PowerListener(FactionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        String power = data.getActivePower();

        if (power.equals("NONE")) return;

        Block block = event.getBlock();

        // AUTO_SMELT
        if (power.equals("AUTO_SMELT")) {
            java.util.Collection<org.bukkit.inventory.ItemStack> drops = block.getDrops(player.getInventory().getItemInMainHand());
            boolean smelted = false;
            for (org.bukkit.inventory.ItemStack item : drops) {
                Material cooked = getCookedMaterial(item.getType());
                if (cooked != null) {
                    item.setType(cooked);
                    smelted = true;
                }
                block.getWorld().dropItemNaturally(block.getLocation(), item);
            }
            if (smelted) event.setDropItems(false);
        }

        // LUCK_MINER
        if (power.equals("LUCK_MINER") && block.getType().name().contains("ORE")) {
            if (Math.random() < 0.15) { // 15% chance
                Collection<ItemStack> drops = block.getDrops(player.getInventory().getItemInMainHand());
                for (ItemStack drop : drops) {
                    block.getWorld().dropItemNaturally(block.getLocation(), drop);
                }
                player.sendMessage("§6§l[Pouvoirs] §eFilon double trouvé !");
            }
        }

        // TELEKINESIS
        if (power.equals("TELEKINESIS")) {
            event.setDropItems(false);
            Collection<ItemStack> drops = block.getDrops(player.getInventory().getItemInMainHand());
            for (ItemStack drop : drops) {
                if (player.getInventory().firstEmpty() != -1) {
                    player.getInventory().addItem(drop);
                } else {
                    block.getWorld().dropItemNaturally(block.getLocation(), drop);
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        PlayerData data = plugin.getPlayerManager().getPlayerData(event.getPlayer().getUniqueId());
        if (data.getActivePower().equals("LUCK_FARMER") && Math.random() < 0.1) {
            org.bukkit.block.Block b = event.getClickedBlock();
            if (b == null) return;
            org.bukkit.block.data.BlockData bd = b.getBlockData();
            if (bd instanceof org.bukkit.block.data.Ageable ageable) {
                if (ageable.getAge() < ageable.getMaximumAge()) {
                    ageable.setAge(ageable.getMaximumAge());
                    b.setBlockData(ageable);
                    event.getPlayer().sendMessage("§6§l[Pouvoirs] §eMagie ! La culture a poussé instantanément.");
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        PlayerData data = plugin.getPlayerManager().getPlayerData(killer.getUniqueId());
        if (data.getActivePower().equals("LUCK_LOOT")) {
            for (ItemStack drop : event.getDrops()) {
                killer.getWorld().dropItemNaturally(event.getEntity().getLocation(), drop);
            }
            killer.sendMessage("§6§l[Pouvoirs] §eButin doublé !");
        }
    }

    private Material getCookedMaterial(Material raw) {
        if (raw.name().startsWith("RAW_")) {
            try {
                return Material.valueOf(raw.name().replace("RAW_", "") + "_INGOT");
            } catch (Exception ignored) {}
        }
        if (raw == Material.COBBLESTONE) return Material.STONE;
        if (raw == Material.IRON_ORE) return Material.IRON_INGOT;
        if (raw == Material.GOLD_ORE) return Material.GOLD_INGOT;
        if (raw == Material.COPPER_ORE) return Material.COPPER_INGOT;
        return null;
    }
}
