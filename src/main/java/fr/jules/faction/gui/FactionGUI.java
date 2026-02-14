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
        fillBorder(inv);

        inv.setItem(10, createItem(Material.BOOK, "§eStatistiques",
            "§7• §fPower: §b" + String.format("%.1f", faction.getPower()),
            "§7• §fTerritoires: §b" + faction.getClaims().size(),
            "§7• §fBanque: §a" + faction.getBalance() + "$",
            "§7• §fMembres: §b" + faction.getMembers().size()));

        inv.setItem(11, createItem(Material.PLAYER_HEAD, "§eMembres", "§7Gérer les membres et grades"));
        inv.setItem(12, createItem(Material.GRASS_BLOCK, "§eTerritoires", "§7Voir les parcelles et auto-claim"));
        inv.setItem(14, createItem(Material.MAP, "§eRelations", "§7Gérer les Alliés et Ennemis"));
        inv.setItem(15, createItem(Material.COMPARATOR, "§eParamètres", "§7Flags de faction (TNT, PVP, etc)"));
        inv.setItem(16, createItem(Material.REDSTONE_TORCH, "§ePermissions", "§7Actions autorisées par grade"));

        player.openInventory(inv);
    }

    public static void openMembersMenu(Player player, Faction faction) {
        Inventory inv = Bukkit.createInventory(null, 54, "§6Membres: " + faction.getName());
        fillBorder(inv);
        inv.setItem(49, createItem(Material.SHEARS, "§7Retour", "§8Clic pour revenir"));

        int slot = 10;
        for (java.util.UUID memberId : faction.getMembers()) {
            if (slot >= 44) break;
            if (slot % 9 == 0 || slot % 9 == 8) slot++;
            String role = faction.getLeader().equals(memberId) ? "Chef" :
                         (faction.getOfficers().contains(memberId) ? "Officier" : "Membre");
            inv.setItem(slot++, createItem(Material.PLAYER_HEAD, "§e" + Bukkit.getOfflinePlayer(memberId).getName(), "§7Grade: " + role, "§7Clic gauche: Promouvoir", "§7Clic droit: Rétrograder", "§7Shift+Clic: Exclure"));
        }
        player.openInventory(inv);
    }

    public static void openPermissionsMenu(Player player, Faction faction) {
        Inventory inv = Bukkit.createInventory(null, 27, "§6Paramètres: " + faction.getName());
        fillBorder(inv);
        inv.setItem(22, createItem(Material.SHEARS, "§7Retour", "§8Clic pour revenir"));

        boolean allyHome = faction.getFactionFlags().getOrDefault("ALLY_HOME", true);
        inv.setItem(10, createItem(allyHome ? Material.LIME_DYE : Material.GRAY_DYE, "§eALLY_HOME", "§7Autoriser les alliés au home", "§7Statut: " + (allyHome ? "§aActivé" : "§cDésactivé")));

        boolean openInvites = faction.getFactionFlags().getOrDefault("OPEN_INVITES", false);
        inv.setItem(11, createItem(openInvites ? Material.LIME_DYE : Material.GRAY_DYE, "§eOPEN_INVITES", "§7Tout le monde peut rejoindre", "§7Statut: " + (openInvites ? "§aActivé" : "§cDésactivé")));

        boolean pvp = faction.getFactionFlags().getOrDefault("pvp", true);
        inv.setItem(12, createItem(pvp ? Material.IRON_SWORD : Material.WOODEN_SWORD, "§epvp", "§7PVP activé", "§7Statut: " + (pvp ? "§aActivé" : "§cDésactivé")));

        boolean explosions = faction.getFactionFlags().getOrDefault("explosions", true);
        inv.setItem(13, createItem(explosions ? Material.TNT : Material.GUNPOWDER, "§eexplosions", "§7TNT activé", "§7Statut: " + (explosions ? "§aActivé" : "§cDésactivé")));

        inv.setItem(15, createItem(Material.OAK_SIGN, "§eDescription", "§7Modifier la description", "§7Actuel: §f" + faction.getDescription()));
        inv.setItem(16, createItem(Material.PAPER, "§eMOTD", "§7Modifier le message de connexion", "§7Actuel: §f" + faction.getMotd()));

        player.openInventory(inv);
    }

    public static void openGradePermissionsMenu(Player player, Faction faction, fr.jules.faction.model.Grade targetGrade) {
        Inventory inv = Bukkit.createInventory(null, 45, "§6Grade: " + targetGrade.name() + " (" + faction.getName() + ")");
        fillBorder(inv);
        inv.setItem(40, createItem(Material.SHEARS, "§7Retour", "§8Clic pour revenir"));

        String[] actions = {"CLAIM", "UNCLAIM", "SETHOME", "UNSETHOME", "PROMOTE", "DEMOTE", "KICK", "INVITE", "DESC", "MOTD", "RENAME", "TITLE", "BUILD", "DESTROY", "USE"};
        int slot = 10;
        for (String action : actions) {
            if (slot >= 35) break;
            if (slot % 9 == 0 || slot % 9 == 8) slot++;
            boolean allowed = faction.hasPermission(targetGrade, action);
            inv.setItem(slot++, createItem(allowed ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE, "§e" + action, "§7Autorisé: " + (allowed ? "§aOui" : "§cNon")));
        }

        player.openInventory(inv);
    }

    public static void openGradeSelectorMenu(Player player, Faction faction) {
        Inventory inv = Bukkit.createInventory(null, 27, "§6Sélecteur de Grade");
        fillBorder(inv);
        inv.setItem(22, createItem(Material.SHEARS, "§7Retour", "§8Clic pour revenir"));

        inv.setItem(10, createItem(Material.LEATHER_HELMET, "§eRECRUIT", "§7Modifier les perms des Recrues"));
        inv.setItem(12, createItem(Material.CHAINMAIL_HELMET, "§eMEMBER", "§7Modifier les perms des Membres"));
        inv.setItem(14, createItem(Material.GOLDEN_HELMET, "§eMODERATOR", "§7Modifier les perms des Modérateurs"));
        inv.setItem(16, createItem(Material.IRON_HELMET, "§eOFFICER", "§7Modifier les perms des Officiers"));
        player.openInventory(inv);
    }

    public static void openRelationsMenu(Player player, Faction faction) {
        Inventory inv = Bukkit.createInventory(null, 27, "§6Relations: " + faction.getName());
        fillBorder(inv);
        inv.setItem(22, createItem(Material.SHEARS, "§7Retour", "§8Clic pour revenir"));

        inv.setItem(11, createItem(Material.PINK_DYE, "§dAlliés", "§7Voir vos alliés actuels"));
        inv.setItem(13, createItem(Material.ORANGE_DYE, "§6Trêves", "§7Voir vos trêves actuelles"));
        inv.setItem(15, createItem(Material.RED_DYE, "§cEnnemis", "§7Voir vos ennemis actuels"));

        player.openInventory(inv);
    }

    public static void openClaimsMenu(Player player, Faction faction) {
        Inventory inv = Bukkit.createInventory(null, 27, "§6Territoires: " + faction.getName());
        fillBorder(inv);
        inv.setItem(22, createItem(Material.SHEARS, "§7Retour", "§8Clic pour revenir"));

        inv.setItem(10, createItem(Material.GRASS_BLOCK, "§aClaim", "§7Revendiquer le chunk actuel"));
        inv.setItem(11, createItem(Material.DIRT, "§cUnclaim", "§7Libérer le chunk actuel"));
        inv.setItem(13, createItem(Material.MAP, "§eCarte", "§7Afficher la carte dans le chat"));
        inv.setItem(15, createItem(Material.BEACON, "§bAuto-Claim", "§7Activer/Désactiver l'auto-claim"));
        inv.setItem(16, createItem(Material.BARRIER, "§cUnclaim All", "§7Libérer tous les territoires"));

        player.openInventory(inv);
    }

    private static void fillBorder(Inventory inv) {
        ItemStack pane = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inv.getSize(); i++) {
            if (i < 9 || i >= inv.getSize() - 9 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, pane);
            }
        }
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
