package fr.jules.faction.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class ShopGUI {
    public static void openShopMenu(Player player) {
        Inventory inv = Bukkit.createInventory(new FactionInventoryHolder("SHOP_MAIN", null), 27, "§c§lBoutique Faction");
        fillBorder(inv);

        inv.setItem(10, createShopCategory(Material.GRASS_BLOCK, "§aBlocs", "§7Acheter des matériaux de construction"));
        inv.setItem(12, createShopCategory(Material.DIAMOND_SWORD, "§cCombat", "§7Armes et armures"));
        inv.setItem(14, createShopCategory(Material.WHEAT, "§eAgriculture", "§7Nourriture et ressources"));
        inv.setItem(16, createShopCategory(Material.LEAD, "§bSpécial", "§7Objets uniques et outils"));

        player.openInventory(inv);
    }

    public static void openCategoryMenu(Player player, String category) {
        Inventory inv = Bukkit.createInventory(new FactionInventoryHolder("SHOP_CATEGORY", category), 54, "§c§lBoutique: " + category);
        fillBorder(inv);
        inv.setItem(49, createItem(Material.SHEARS, "§7Retour", "§8Clic pour revenir"));

        if (category.equals("Blocs")) {
            addShopItem(inv, 10, Material.STONE, 64, 100, 20);
            addShopItem(inv, 11, Material.COBBLESTONE, 64, 50, 10);
            addShopItem(inv, 12, Material.OAK_LOG, 64, 200, 40);
            addShopItem(inv, 13, Material.DIRT, 64, 20, 5);
            addShopItem(inv, 14, Material.GLASS, 64, 150, 30);
            addShopItem(inv, 15, Material.OBSIDIAN, 16, 1000, 200);
            addShopItem(inv, 16, Material.END_STONE, 64, 500, 100);
        } else if (category.equals("Combat")) {
            addShopItem(inv, 10, Material.IRON_SWORD, 1, 500, 100);
            addShopItem(inv, 11, Material.DIAMOND_SWORD, 1, 2500, 500);
            addShopItem(inv, 12, Material.BOW, 1, 400, 80);
            addShopItem(inv, 13, Material.ARROW, 64, 200, 40);
            addShopItem(inv, 14, Material.IRON_CHESTPLATE, 1, 1000, 200);
            addShopItem(inv, 15, Material.DIAMOND_CHESTPLATE, 1, 5000, 1000);
            addShopItem(inv, 16, Material.GOLDEN_APPLE, 1, 1000, 200);
        } else if (category.equals("Agriculture")) {
            addShopItem(inv, 10, Material.BREAD, 16, 100, 20);
            addShopItem(inv, 11, Material.COOKED_BEEF, 16, 200, 40);
            addShopItem(inv, 12, Material.WHEAT_SEEDS, 32, 50, 10);
            addShopItem(inv, 13, Material.POTATO, 32, 100, 20);
            addShopItem(inv, 14, Material.CARROT, 32, 100, 20);
            addShopItem(inv, 15, Material.MELON_SLICE, 64, 150, 30);
            addShopItem(inv, 16, Material.SUGAR_CANE, 32, 300, 60);
        } else if (category.equals("Spécial")) {
            inv.setItem(10, createItem(Material.LEAD, "§bLasso de Capture",
                "§7Quantité: §f1",
                "§7Prix Achat: §a10000$",
                "§7Prix Vente: §c0$",
                "§7Permet de capturer un animal", "§7sauvage comme compagnon.",
                "§8Clic Gauche: Acheter"));
        }

        player.openInventory(inv);
    }

    private static void addShopItem(Inventory inv, int slot, Material material, int amount, double buyPrice, double sellPrice) {
        inv.setItem(slot, createItem(material, "§e" + material.name(),
            "§7Quantité: §f" + amount,
            "§7Prix Achat: §a" + buyPrice + "$",
            "§7Prix Vente: §c" + sellPrice + "$",
            "§8Clic Gauche: Acheter", "§8Clic Droit: Vendre"));
    }

    private static ItemStack createShopCategory(Material material, String name, String description) {
        return createItem(material, name, description, "§8Clic pour ouvrir");
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

    private static void fillBorder(Inventory inv) {
        ItemStack pane = createItem(Material.RED_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inv.getSize(); i++) {
            if (i < 9 || i >= inv.getSize() - 9 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, pane);
            }
        }
    }
}
