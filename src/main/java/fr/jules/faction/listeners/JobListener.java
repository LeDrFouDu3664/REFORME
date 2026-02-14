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

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlant(org.bukkit.event.player.PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        PlayerData data = plugin.getPlayerManager().getPlayerData(event.getPlayer().getUniqueId());
        if (data.getPowers().contains("LUCK_FARMER") && Math.random() < 0.1) {
            org.bukkit.block.Block b = event.getClickedBlock();
            if (b == null) return;
            org.bukkit.block.data.BlockData bd = b.getBlockData();
            if (bd instanceof org.bukkit.block.data.Ageable ageable) {
                if (ageable.getAge() < ageable.getMaximumAge()) {
                    ageable.setAge(ageable.getMaximumAge());
                    b.setBlockData(ageable);
                    event.getPlayer().sendMessage("§aMagie ! La culture a poussé instantanément.");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        org.bukkit.block.Block block = event.getBlock();

        // Power: Telekinesis, Auto-Smelt, Luck Miner
        boolean tele = data.getPowers().contains("TELEKINESIS");
        boolean smelt = data.getPowers().contains("AUTO_SMELT");
        boolean luck = data.getPowers().contains("LUCK_MINER") && Math.random() < 0.05;

        if (tele || smelt || luck) {
            java.util.Collection<org.bukkit.inventory.ItemStack> drops = block.getDrops(player.getInventory().getItemInMainHand());
            for (org.bukkit.inventory.ItemStack item : drops) {
                if (smelt) {
                    Material cooked = getCookedMaterial(item.getType());
                    if (cooked != null) item.setType(cooked);
                }
                if (luck && (item.getType().name().contains("RAW") || item.getType().name().contains("INGOT") || item.getType().name().contains("DIAMOND"))) {
                    item.setAmount(item.getAmount() * 2);
                }
                if (tele) {
                    java.util.Map<Integer, org.bukkit.inventory.ItemStack> left = player.getInventory().addItem(item);
                    if (!left.isEmpty()) {
                        for (org.bukkit.inventory.ItemStack l : left.values()) {
                            block.getWorld().dropItemNaturally(block.getLocation(), l);
                        }
                    }
                } else {
                    block.getWorld().dropItemNaturally(block.getLocation(), item);
                }
            }
            event.setDropItems(false);
        }

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

        // Power: Luck Loot
        if (data.getPowers().contains("LUCK_LOOT")) {
            for (org.bukkit.inventory.ItemStack item : event.getDrops()) {
                item.setAmount(item.getAmount() * 2);
            }
        }

        if (data.getJob().equals("GUERRIER")) {
            if (event.getEntity() instanceof Player) {
                awardExp(killer, data, 50);
            } else {
                awardExp(killer, data, 5);
            }
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

    private void awardExp(Player player, PlayerData data, double amount) {
        if (data.getPowers().contains("DOUBLE_XP")) {
            amount *= 2.0;
        } else if (data.getPowers().contains("LUCK_MINER") && data.getJob().equals("MINEUR")) {
            amount *= 1.5;
        }
        data.setJobExp(data.getJobExp() + amount);

        // Argent par action basé sur le niveau
        double moneyMultiplier = 1 + (data.getJobLevel() * 0.2);
        if (data.getPowers().contains("DOUBLE_MONEY")) {
            moneyMultiplier *= 2.0;
        }
        double moneyReward = amount * moneyMultiplier;
        plugin.getEconomyManager().deposit(player, moneyReward);

        // XP pour la faction
        if (data.getFactionId() != null) {
            fr.jules.faction.model.Faction f = plugin.getFactionManager().getFaction(data.getFactionId());
            if (f != null) {
                addFactionExp(player, f, amount * 0.5);
            }
        }

        double nextLevelExp = data.getJobLevel() * 100 * 1.5;
        if (data.getJobExp() >= nextLevelExp) {
            data.setJobExp(data.getJobExp() - nextLevelExp);
            data.setJobLevel(data.getJobLevel() + 1);
            player.sendMessage("§b§l[Métier] §aFélicitations ! Vous passez au niveau §e" + data.getJobLevel() + " §aen tant que §e" + data.getJob() + "§a !");
            plugin.getEconomyManager().deposit(player, data.getJobLevel() * 1000); // Grosse récompense
            player.sendMessage("§7Vous avez reçu §e" + (data.getJobLevel() * 1000) + "$ §7en récompense de niveau.");
        }
    }

    private void addFactionExp(Player player, fr.jules.faction.model.Faction f, double amount) {
        f.setExp(f.getExp() + amount);
        double nextLevelExp = f.getLevel() * 1000 * 1.5;
        if (f.getExp() >= nextLevelExp) {
            f.setExp(f.getExp() - nextLevelExp);
            f.setLevel(f.getLevel() + 1);
            for (java.util.UUID mid : f.getMembers()) {
                Player member = org.bukkit.Bukkit.getPlayer(mid);
                if (member != null) {
                    plugin.getPlayerManager().getPlayerData(mid); // Rafraîchit le max power
                    member.sendMessage("§6§l[Faction] §aVotre faction est passée au niveau §e" + f.getLevel() + "§a !");
                    member.sendTitle("§6§lNiveau Faction Up !", "§aNiveau: " + f.getLevel(), 10, 40, 10);
                }
            }
        }
    }
}
