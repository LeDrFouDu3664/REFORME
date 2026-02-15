package fr.jules.faction.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ModGUI {
    public static void openModMenu(Player staff) {
        Inventory inv = Bukkit.createInventory(new FactionInventoryHolder("MOD_MAIN", null), 27, "§c§lOutils de Modération");

        inv.setItem(10, createItem(Material.BLAZE_ROD, "§6Bâton de Modération", "§7Outil rapide (Clic droit sur joueur)"));
        inv.setItem(12, createItem(Material.PACKED_ICE, "§bFreeze", "§7Geler un joueur"));
        inv.setItem(14, createItem(Material.ENDER_EYE, "§aVanish", "§7Devenir invisible"));
        inv.setItem(16, createItem(Material.PLAYER_HEAD, "§eJoueurs en ligne", "§7Gérer les joueurs"));

        staff.openInventory(inv);
    }

    public static void openPlayerList(Player staff) {
        Inventory inv = Bukkit.createInventory(new FactionInventoryHolder("MOD_PLAYERS", null), 54, "§c§lGestion Joueurs");
        int slot = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (slot >= 54) break;
            inv.setItem(slot++, createItem(Material.PLAYER_HEAD, "§e" + p.getName(), "§7Clic pour gérer ce joueur"));
        }
        staff.openInventory(inv);
    }

    public static void openPlayerActions(Player staff, Player target) {
        Inventory inv = Bukkit.createInventory(new FactionInventoryHolder("MOD_ACTIONS", target), 27, "§c§lAction: " + target.getName());

        inv.setItem(10, createItem(Material.CHEST, "§eInventaire", "§7Voir l'inventaire"));
        inv.setItem(12, createItem(Material.PACKED_ICE, "§bFreeze / Unfreeze", "§7Geler le joueur"));
        inv.setItem(14, createItem(Material.IRON_DOOR, "§cKick", "§7Expulser le joueur"));
        inv.setItem(16, createItem(Material.BARRIER, "§4Ban", "§7Bannir le joueur"));

        staff.openInventory(inv);
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
