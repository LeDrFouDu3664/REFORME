package fr.jules.faction.gui;

import fr.jules.faction.model.Faction;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class FactionGUI {
    public static void openMainMenu(Player player, Faction faction) {
        Inventory inv = Bukkit.createInventory(null, 27, "§6Gestion: " + faction.getName());

        inv.setItem(10, createItem(Material.BOOK, "§eInformations", "§7Voir les infos de la faction"));
        inv.setItem(11, createItem(Material.PLAYER_HEAD, "§eMembres", "§7Gérer les membres"));
        inv.setItem(13, createItem(Material.GRASS_BLOCK, "§eClaims", "§7Gérer les parcelles"));
        inv.setItem(15, createItem(Material.REDSTONE, "§eRelations", "§7Gérer les relations"));
        inv.setItem(16, createItem(Material.COMPARATOR, "§ePermissions", "§7Gérer les permissions"));

        player.openInventory(inv);
    }

    private static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        if (item.getItemMeta() == null) return item;
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
}
