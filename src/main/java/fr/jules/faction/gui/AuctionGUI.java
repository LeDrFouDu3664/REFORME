package fr.jules.faction.gui;

import fr.jules.faction.model.AuctionItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AuctionGUI {
    public static void openAuctionMenu(Player player, Collection<AuctionItem> items) {
        Inventory inv = Bukkit.createInventory(new FactionInventoryHolder("AUCTION", null), 54, "§c§lHôtel de Vente");
        fillBorder(inv);

        int slot = 10;
        for (AuctionItem ai : items) {
            if (slot >= 44) break;
            if (slot % 9 == 0 || slot % 9 == 8) slot++;

            ItemStack is = ai.getItem().clone();
            ItemMeta meta = is.getItemMeta();
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.add(" ");
            lore.add("§7Vendeur: §e" + ai.getSellerName());
            lore.add("§7Prix: §a" + ai.getPrice() + "$");
            lore.add("§7Expire dans: §f" + ((ai.getExpiry() - System.currentTimeMillis()) / 3600000) + "h");
            lore.add(" ");
            lore.add("§e§lCLIC POUR ACHETER");

            // Store ID in lore secretly or use PDC. I'll use a hidden tag in lore for simplicity here
            lore.add("§0ID:" + ai.getId().toString());

            meta.setLore(lore);
            is.setItemMeta(meta);

            inv.setItem(slot++, is);
        }

        player.openInventory(inv);
    }

    private static void fillBorder(Inventory inv) {
        ItemStack pane = createItem(Material.RED_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inv.getSize(); i++) {
            if (i < 9 || i >= inv.getSize() - 9 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, pane);
            }
        }
    }

    private static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(java.util.Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }
}
