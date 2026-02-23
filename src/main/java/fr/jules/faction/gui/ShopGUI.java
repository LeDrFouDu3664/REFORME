package fr.jules.faction.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ShopGUI {
    public static void openShopMenu(Player player) {
        Inventory inv = Bukkit.createInventory(new FactionInventoryHolder("SHOP_MAIN", null), 27, "§c§lBoutique Faction");
        fillBorder(inv);

        fr.jules.faction.FactionPlugin plugin = (fr.jules.faction.FactionPlugin) Bukkit.getPluginManager().getPlugin("TPCFaction");
        fr.jules.faction.modules.shop.ShopModule shopMod = (fr.jules.faction.modules.shop.ShopModule) plugin.getModuleManager().getModule("Shop");

        org.bukkit.configuration.ConfigurationSection categories = shopMod.getConfig().getConfigurationSection("categories");
        if (categories != null) {
            int slot = 10;
            for (String key : categories.getKeys(false)) {
                Material icon = Material.valueOf(categories.getString(key + ".icon", "BARRIER"));
                inv.setItem(slot, createShopCategory(icon, "§a" + key, "§7Ouvrir la catégorie " + key));
                slot += 2;
                if (slot > 16) break;
            }
        }

        player.openInventory(inv);
    }

    public static void openCategoryMenu(Player player, String category) {
        Inventory inv = Bukkit.createInventory(new FactionInventoryHolder("SHOP_CATEGORY", category), 54, "§c§lBoutique: " + category);
        fillBorder(inv);
        inv.setItem(49, createItem(Material.SHEARS, "§7Retour", "§8Clic pour revenir"));

        fr.jules.faction.FactionPlugin plugin = (fr.jules.faction.FactionPlugin) Bukkit.getPluginManager().getPlugin("TPCFaction");
        fr.jules.faction.modules.shop.ShopModule shopMod = (fr.jules.faction.modules.shop.ShopModule) plugin.getModuleManager().getModule("Shop");

        org.bukkit.configuration.ConfigurationSection catSection = shopMod.getConfig().getConfigurationSection("categories." + category);
        if (catSection != null) {
            List<Map<?, ?>> items = catSection.getMapList("items");
            int slot = 10;
            for (Map<?, ?> itemData : items) {
                String matStr = (String) itemData.get("material");
                if (matStr == null) continue;
                Material m = Material.valueOf(matStr);

                Object amountObj = itemData.get("amount");
                int amount = (amountObj instanceof Number n) ? n.intValue() : 1;

                Object buyObj = itemData.get("buy");
                double buy = (buyObj instanceof Number n) ? n.doubleValue() : 0.0;

                Object sellObj = itemData.get("sell");
                double sell = (sellObj instanceof Number n) ? n.doubleValue() : 0.0;

                String customName = (String) itemData.get("name");

                if (customName != null) {
                    inv.setItem(slot++, createItem(m, customName,
                        "§7Quantité: §f" + amount,
                        "§7Prix Achat: §a" + buy + "$",
                        "§7Prix Vente: §c" + sell + "$",
                        "§8Clic Gauche: Acheter"));
                } else {
                    addShopItem(inv, slot++, m, amount, buy, sell);
                }

                if (slot % 9 == 8) slot += 2;
                if (slot >= 44) break;
            }
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
