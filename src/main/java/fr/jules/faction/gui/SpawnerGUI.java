package fr.jules.faction.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class SpawnerGUI {
    public static void openSpawnerMenu(Player player, Block spawner) {
        Inventory inv = Bukkit.createInventory(new FactionInventoryHolder("SPAWNER", spawner), 27, "§c§lGestion Spawner");

        boolean active = !spawner.hasMetadata("inactive");

        inv.setItem(11, createItem(active ? Material.LIME_DYE : Material.RED_DYE, "§eStatut: " + (active ? "§aActivé" : "§cDésactivé"), "§7Clic pour changer"));
        inv.setItem(13, createItem(Material.DIAMOND_PICKAXE, "§dRécupérer le Spawner", "§7Nécessite une Pioche à Spawner", "§7ou 25000$ (Clic Droit)"));
        inv.setItem(15, createItem(Material.GOLD_INGOT, "§eAcheter Pioche Spawner", "§7Prix: 50000$"));

        player.openInventory(inv);
    }

    private static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }
}
