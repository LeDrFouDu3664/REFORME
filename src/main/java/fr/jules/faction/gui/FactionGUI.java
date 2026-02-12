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

    public static void openMembersMenu(Player player, Faction faction) {
        Inventory inv = Bukkit.createInventory(null, 54, "§6Membres: " + faction.getName());
        int slot = 0;
        for (java.util.UUID memberId : faction.getMembers()) {
            if (slot >= 54) break;
            String role = faction.getLeader().equals(memberId) ? "Chef" :
                         (faction.getOfficers().contains(memberId) ? "Officier" : "Membre");
            inv.setItem(slot++, createItem(Material.PLAYER_HEAD, "§e" + Bukkit.getOfflinePlayer(memberId).getName(), "§7Grade: " + role, "§7Clic gauche: Promouvoir", "§7Clic droit: Rétrograder", "§7Shift+Clic: Exclure"));
        }
        player.openInventory(inv);
    }

    public static void openPermissionsMenu(Player player, Faction faction) {
        Inventory inv = Bukkit.createInventory(null, 27, "§6Permissions: " + faction.getName());

        boolean allyHome = faction.getFlags().getOrDefault("ALLY_HOME", true);
        inv.setItem(10, createItem(allyHome ? Material.LIME_DYE : Material.GRAY_DYE, "§eALLY_HOME", "§7Autoriser les alliés au home", "§7Statut: " + (allyHome ? "§aActivé" : "§cDésactivé")));

        boolean openInvites = faction.getFlags().getOrDefault("OPEN_INVITES", false);
        inv.setItem(11, createItem(openInvites ? Material.LIME_DYE : Material.GRAY_DYE, "§eOPEN_INVITES", "§7Tout le monde peut rejoindre", "§7Statut: " + (openInvites ? "§aActivé" : "§cDésactivé")));

        inv.setItem(22, createItem(Material.NAME_TAG, "§ePermissions par Grade", "§7Gérer les actions autorisées"));

        player.openInventory(inv);
    }

    public static void openGradePermissionsMenu(Player player, Faction faction, fr.jules.faction.model.Grade targetGrade) {
        Inventory inv = Bukkit.createInventory(null, 36, "§6Grade: " + targetGrade.name() + " (" + faction.getName() + ")");

        String[] actions = {"CLAIM", "UNCLAIM", "SETHOME", "UNSETHOME", "PROMOTE", "DEMOTE", "KICK", "INVITE", "DESC", "MOTD", "RENAME", "TITLE"};
        int slot = 0;
        for (String action : actions) {
            boolean allowed = faction.hasPermission(targetGrade, action);
            inv.setItem(slot++, createItem(allowed ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE, "§e" + action, "§7Autorisé: " + (allowed ? "§aOui" : "§cNon")));
        }

        inv.setItem(31, createItem(Material.ARROW, "§7Retour"));

        player.openInventory(inv);
    }

    public static void openGradeSelectorMenu(Player player, Faction faction) {
        Inventory inv = Bukkit.createInventory(null, 27, "§6Sélecteur de Grade");
        inv.setItem(11, createItem(Material.LEATHER_HELMET, "§eMEMBER"));
        inv.setItem(15, createItem(Material.IRON_HELMET, "§eOFFICER"));
        inv.setItem(22, createItem(Material.ARROW, "§7Retour"));
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
