package fr.jules.faction.gui;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.model.PetInfo;
import fr.jules.faction.model.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class PetPowersGUI {

    public static void openPowersMenu(Player player, FactionPlugin plugin, String petId) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        PetInfo info = data.getCapturedPets().get(petId);
        if (info == null) return;

        Inventory inv = Bukkit.createInventory(new FactionInventoryHolder("PET_POWERS", petId), 27, "§c§lPouvoirs: " + (info.getCustomName() != null ? info.getCustomName() : petId));

        // Power Items
        addPowerItem(inv, 10, "MINER", Material.DIAMOND_PICKAXE, "§eMineur", "§7Accorde l'effet Hâte.", 5000, info);
        addPowerItem(inv, 12, "TANK", Material.DIAMOND_CHESTPLATE, "§eTank", "§7Accorde l'effet Résistance.", 8000, info);
        addPowerItem(inv, 14, "FIGHTER", Material.DIAMOND_SWORD, "§eCombattant", "§7Accorde l'effet Force.", 10000, info);
        addPowerItem(inv, 16, "SCOUT", Material.FEATHER, "§eÉclaireur", "§7Accorde l'effet Vitesse II.", 6000, info);

        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§7Retour");
        back.setItemMeta(backMeta);
        inv.setItem(26, back);

        player.openInventory(inv);
    }

    private static void addPowerItem(Inventory inv, int slot, String id, Material material, String name, String desc, double price, PetInfo info) {
        boolean unlocked = info.getUnlockedPowers().contains(id);
        boolean active = info.getActivePower().equals(id);

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        List<String> lore = new ArrayList<>();
        lore.add(desc);
        lore.add("");
        if (active) {
            lore.add("§a§lACTIF");
        } else if (unlocked) {
            lore.add("§eDébloqué");
            lore.add("§7Clic pour activer");
        } else {
            lore.add("§cPrix: §e" + price + "$");
            lore.add("§7Clic pour acheter");
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        inv.setItem(slot, item);
    }
}
