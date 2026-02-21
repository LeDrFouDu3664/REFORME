package fr.jules.faction.gui;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.model.Rank;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class RankGUI {

    public static void openRankMenu(Player player, FactionPlugin plugin) {
        Inventory inv = Bukkit.createInventory(new FactionInventoryHolder("RANK_MENU", null), 27, "§c§lGrades du Serveur");

        // Fill border
        ItemStack pane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta pm = pane.getItemMeta();
        pm.setDisplayName(" ");
        pane.setItemMeta(pm);
        for (int i = 0; i < 27; i++) {
            if (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) inv.setItem(i, pane);
        }

        int slot = 10;
        for (Rank rank : Rank.values()) {
            if (slot > 16) break;
            addRankItem(inv, slot++, rank, plugin.getPlayerManager().getPlayerData(player.getUniqueId()).getRank() == rank);
        }

        player.openInventory(inv);
    }

    private static void addRankItem(Inventory inv, int slot, Rank rank, boolean current) {
        ItemStack item = new ItemStack(current ? Material.ENCHANTED_BOOK : Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(rank.getPrefix());
        List<String> lore = new ArrayList<>();
        lore.add("§7Multiplicateur Power: §e" + rank.getPowerMultiplier() + "x");
        lore.add("§7Homes maximum: §e" + rank.getMaxHomes());
        lore.add("");
        if (current) lore.add("§a§lVOTRE GRADE ACTUEL");
        else lore.add("§e▶ Cliquez pour voir les détails");
        meta.setLore(lore);
        item.setItemMeta(meta);
        inv.setItem(slot, item);
    }
}
