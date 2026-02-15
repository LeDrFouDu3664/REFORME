package fr.jules.faction.gui;

import fr.jules.faction.model.Faction;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Map;

public class FactionGUI {
    public static void openMainMenu(Player player, Faction faction) {
        Inventory inv = Bukkit.createInventory(new FactionInventoryHolder("MAIN", faction), 27, "§c§lGestion: " + faction.getName());
        fillBorder(inv);

        inv.setItem(10, createItem(Material.BOOK, "§eStatistiques",
            "§7• §fNiveau: §6" + faction.getLevel(),
            "§7• §fExp: §7" + String.format("%.1f", faction.getExp()) + " / " + (faction.getLevel() * 1000 * 1.5),
            "§7• §fPower: §b" + String.format("%.1f", faction.getPower()),
            "§7• §fTerritoires: §b" + faction.getClaims().size(),
            "§7• §fBanque: §a" + faction.getBalance() + "$",
            "§7• §fMembres: §b" + faction.getMembers().size()));

        inv.setItem(11, createItem(Material.PLAYER_HEAD, "§eMembres", "§7Gérer les membres et grades"));
        inv.setItem(12, createItem(Material.GRASS_BLOCK, "§eTerritoires", "§7Voir les parcelles et auto-claim"));
        inv.setItem(13, createItem(Material.GOLD_INGOT, "§eBanque", "§7Gérer l'argent de la faction"));
        inv.setItem(14, createItem(Material.MAP, "§eRelations", "§7Gérer les Alliés et Ennemis"));
        inv.setItem(15, createItem(Material.COMPARATOR, "§eParamètres", "§7Flags de faction (TNT, PVP, etc)"));
        inv.setItem(16, createItem(Material.REDSTONE_TORCH, "§ePermissions", "§7Actions autorisées par grade"));

        inv.setItem(20, createItem(Material.EXPERIENCE_BOTTLE, "§eNiveaux Faction", "§7Voir les récompenses de niveau"));
        inv.setItem(21, createItem(Material.IRON_SWORD, "§eMétiers", "§7Choisir un métier"));
        inv.setItem(22, createItem(Material.BLAZE_POWDER, "§ePouvoirs", "§7Débloquer des capacités"));
        inv.setItem(23, createItem(Material.WRITABLE_BOOK, "§eQuêtes", "§7Voir les quêtes"));
        inv.setItem(24, createItem(Material.BONE, "§eCompagnon", "§7Gérer votre familier"));

        player.openInventory(inv);
    }

    public static void openPowersMenu(Player player, fr.jules.faction.model.PlayerData data, fr.jules.faction.manager.PowerManager pm) {
        Inventory inv = Bukkit.createInventory(new FactionInventoryHolder("POWERS", data), 54, "§c§lPouvoirs de Faction");
        fillBorder(inv);
        inv.setItem(49, createItem(Material.SHEARS, "§7Retour", "§8Clic pour revenir"));

        int slot = 10;
        for (Map.Entry<String, fr.jules.faction.manager.PowerManager.PowerInfo> entry : pm.getPowers().entrySet()) {
            String pid = entry.getKey();
            fr.jules.faction.manager.PowerManager.PowerInfo info = entry.getValue();
            boolean has = data.getPowers().contains(pid);

            double cost = 5000;
            if (pid.contains("II") || pid.equals("VAMPIRE") || pid.equals("STRENGTH")) cost = 15000;

            inv.setItem(slot++, createItem(has ? Material.ENCHANTED_BOOK : Material.BOOK,
                "§e" + info.name,
                "§7" + info.description,
                has ? "§aDébloqué" : "§cCliquez pour débloquer (" + String.format("%.0f", cost) + "$)"));

            if (slot % 9 == 8) slot += 2;
            if (slot >= 44) break;
        }

        player.openInventory(inv);
    }

    public static void openQuestsMenu(Player player, fr.jules.faction.model.PlayerData data, fr.jules.faction.manager.QuestManager qm) {
        Inventory inv = Bukkit.createInventory(new FactionInventoryHolder("QUESTS", data), 27, "§c§lQuêtes");
        fillBorder(inv);
        inv.setItem(22, createItem(Material.SHEARS, "§7Retour", "§8Clic pour revenir"));

        int slot = 10;
        for (Map.Entry<String, fr.jules.faction.manager.QuestManager.QuestInfo> entry : qm.getQuests().entrySet()) {
            String qid = entry.getKey();
            fr.jules.faction.manager.QuestManager.QuestInfo info = entry.getValue();
            int progress = data.getQuestProgress().getOrDefault(qid, 0);
            boolean done = progress >= info.goal;

            inv.setItem(slot++, createItem(done ? Material.ENCHANTED_BOOK : Material.BOOK,
                "§e" + info.name,
                "§7" + info.description,
                "§7Progrès: §f" + Math.min(progress, info.goal) + " / " + info.goal,
                "§7Récompense: §e" + info.reward + "$",
                done ? "§aTerminée !" : "§cEn cours..."));
            if (slot == 17) break;
        }

        player.openInventory(inv);
    }

    public static void openJobsMenu(Player player, fr.jules.faction.model.PlayerData data) {
        Inventory inv = Bukkit.createInventory(new FactionInventoryHolder("JOBS", data), 27, "§c§lMétiers");
        fillBorder(inv);
        inv.setItem(22, createItem(Material.SHEARS, "§7Retour", "§8Clic pour revenir"));

        inv.setItem(10, createItem(Material.DIAMOND_PICKAXE, "§eMineur", "§7Gagnez de l'XP en minant.", "§7Statut: " + (data.getJob().equals("MINEUR") ? "§aActif" : "§cInactif")));
        inv.setItem(12, createItem(Material.IRON_AXE, "§eBûcheron", "§7Gagnez de l'XP en coupant du bois.", "§7Statut: " + (data.getJob().equals("BUCHERON") ? "§aActif" : "§cInactif")));
        inv.setItem(14, createItem(Material.IRON_HOE, "§eFermier", "§7Gagnez de l'XP en cultivant.", "§7Statut: " + (data.getJob().equals("FERMIER") ? "§aActif" : "§cInactif")));
        inv.setItem(16, createItem(Material.DIAMOND_SWORD, "§eGuerrier", "§7Gagnez de l'XP en combattant.", "§7Statut: " + (data.getJob().equals("GUERRIER") ? "§aActif" : "§cInactif")));

        inv.setItem(22, createItem(Material.BOOK, "§6Vos Stats", "§7Métier: §f" + data.getJob(), "§7Niveau: §f" + data.getJobLevel(), "§7Exp: §f" + String.format("%.1f", data.getJobExp()) + " / " + (data.getJobLevel() * 100 * 1.5)));

        player.openInventory(inv);
    }

    public static void openBankMenu(Player player, Faction faction) {
        Inventory inv = Bukkit.createInventory(new FactionInventoryHolder("BANK", faction), 27, "§c§lBanque: " + faction.getName());
        fillBorder(inv);
        inv.setItem(22, createItem(Material.SHEARS, "§7Retour", "§8Clic pour revenir"));

        inv.setItem(4, createItem(Material.GOLD_BLOCK, "§6Solde: §e" + String.format("%.2f", faction.getBalance()) + "$"));

        inv.setItem(11, createItem(Material.GOLD_NUGGET, "§aDéposer 100$", "§7Clic pour déposer 100$"));
        inv.setItem(12, createItem(Material.GOLD_INGOT, "§aDéposer 1000$", "§7Clic pour déposer 1000$"));

        inv.setItem(14, createItem(Material.IRON_NUGGET, "§cRetirer 100$", "§7Clic pour retirer 100$"));
        inv.setItem(15, createItem(Material.IRON_INGOT, "§cRetirer 1000$", "§7Clic pour retirer 1000$"));

        player.openInventory(inv);
    }

    public static void openMembersMenu(Player player, Faction faction) {
        Inventory inv = Bukkit.createInventory(new FactionInventoryHolder("MEMBERS", faction), 54, "§c§lMembres: " + faction.getName());
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

    public static void openParametersMenu(Player player, Faction faction) {
        Inventory inv = Bukkit.createInventory(new FactionInventoryHolder("PARAMETERS", faction), 27, "§c§lParamètres: " + faction.getName());
        fillBorder(inv);
        inv.setItem(22, createItem(Material.SHEARS, "§7Retour", "§8Clic pour revenir"));

        boolean allyHome = faction.getFactionFlags().getOrDefault("ALLY_HOME", true);
        inv.setItem(10, createItem(allyHome ? Material.LIME_DYE : Material.GRAY_DYE, "§eALLY_HOME", "§7Autoriser les alliés au home", "§7Statut: " + (allyHome ? "§aActivé" : "§cDésactivé")));

        boolean openInvites = faction.getFactionFlags().getOrDefault("OPEN_INVITES", false);
        inv.setItem(11, createItem(openInvites ? Material.LIME_DYE : Material.GRAY_DYE, "§eOPEN_INVITES", "§7Tout le monde peut rejoindre", "§7Statut: " + (openInvites ? "§aActivé" : "§cDésactivé")));

        boolean ff = faction.getFactionFlags().getOrDefault("friendlyFire", false);
        inv.setItem(12, createItem(ff ? Material.IRON_SWORD : Material.WOODEN_SWORD, "§efriendlyFire", "§7Feu ami activé", "§7Statut: " + (ff ? "§aActivé" : "§cDésactivé")));

        boolean autoPlant = faction.getFactionFlags().getOrDefault("AUTO_PLANT", false);
        inv.setItem(13, createItem(autoPlant ? Material.WHEAT : Material.WHEAT_SEEDS, "§eAUTO_PLANT", "§7Replantation automatique", "§7Statut: " + (autoPlant ? "§aActivé" : "§cDésactivé")));

        boolean mobGriefing = faction.getFactionFlags().getOrDefault("MOB_GRIEFING", false);
        inv.setItem(14, createItem(mobGriefing ? Material.CREEPER_HEAD : Material.DIRT, "§eMOB_GRIEFING", "§7Dégâts blocs par mobs", "§7Statut: " + (mobGriefing ? "§aActivé" : "§cDésactivé")));

        inv.setItem(15, createItem(Material.OAK_SIGN, "§eDescription", "§7Modifier la description", "§7Actuel: §f" + faction.getDescription()));
        inv.setItem(16, createItem(Material.PAPER, "§eMOTD", "§7Modifier le message de connexion", "§7Actuel: §f" + faction.getMotd()));

        player.openInventory(inv);
    }

    public static void openFactionLevelMenu(Player player, Faction faction) {
        Inventory inv = Bukkit.createInventory(new FactionInventoryHolder("LEVELS", faction), 27, "§c§lRécompenses de Niveau");
        fillBorder(inv);
        inv.setItem(22, createItem(Material.SHEARS, "§7Retour", "§8Clic pour revenir"));

        inv.setItem(10, createItem(Material.IRON_CHESTPLATE, "§eNiveau 2", "§7• §f+2 Max Power / membre", faction.getLevel() >= 2 ? "§aDébloqué" : "§cVerrouillé"));
        inv.setItem(11, createItem(Material.GOLDEN_CHESTPLATE, "§eNiveau 5", "§7• §f+4 Max Power / membre", faction.getLevel() >= 5 ? "§aDébloqué" : "§cVerrouillé"));
        inv.setItem(12, createItem(Material.DIAMOND_CHESTPLATE, "§eNiveau 10", "§7• §f+7 Max Power / membre", "§7• §f+5 slots membres", faction.getLevel() >= 10 ? "§aDébloqué" : "§cVerrouillé"));
        inv.setItem(13, createItem(Material.NETHERITE_CHESTPLATE, "§eNiveau 15", "§7• §f+10 Max Power / membre", "§7• §f+10 slots membres", faction.getLevel() >= 15 ? "§aDébloqué" : "§cVerrouillé"));
        inv.setItem(14, createItem(Material.BEACON, "§eNiveau 20", "§7• §f+15 Max Power / membre", "§7• §f+20 slots membres", faction.getLevel() >= 20 ? "§aDébloqué" : "§cVerrouillé"));

        player.openInventory(inv);
    }

    public static void openRankPermissionsMenu(Player player, Faction faction, String target) {
        Inventory inv = Bukkit.createInventory(new FactionInventoryHolder("RANK_PERMS", target), 45, "§c§lPerms: " + target + " (" + faction.getName() + ")");
        fillBorder(inv);
        inv.setItem(40, createItem(Material.SHEARS, "§7Retour", "§8Clic pour revenir"));

        String[] actions = {"CLAIM", "UNCLAIM", "SETHOME", "UNSETHOME", "PROMOTE", "DEMOTE", "KICK", "INVITE", "DESC", "MOTD", "RENAME", "TITLE", "BUILD", "DESTROY", "USE"};
        int slot = 10;
        for (String action : actions) {
            if (slot >= 35) break;
            if (slot % 9 == 0 || slot % 9 == 8) slot++;

            boolean allowed;
            if (target.equals("ALLY")) {
                allowed = faction.getFactionFlags().getOrDefault("ALLY_" + action, false);
            } else {
                allowed = faction.hasPermission(fr.jules.faction.model.Grade.valueOf(target), action);
            }

            inv.setItem(slot++, createItem(allowed ? Material.LIME_DYE : Material.RED_DYE, "§e" + action, "§7Autorisé: " + (allowed ? "§aOui" : "§cNon")));
        }

        player.openInventory(inv);
    }

    public static void openPermissionsMenu(Player player, Faction faction) {
        Inventory inv = Bukkit.createInventory(new FactionInventoryHolder("PERMISSIONS", faction), 27, "§c§lPermissions: " + faction.getName());
        fillBorder(inv);
        inv.setItem(22, createItem(Material.SHEARS, "§7Retour", "§8Clic pour revenir"));

        inv.setItem(10, createItem(Material.LEATHER_HELMET, "§eRECRUIT", "§7Modifier les perms des Recrues"));
        inv.setItem(11, createItem(Material.CHAINMAIL_HELMET, "§eMEMBER", "§7Modifier les perms des Membres"));
        inv.setItem(12, createItem(Material.GOLDEN_HELMET, "§eMODERATOR", "§7Modifier les perms des Modérateurs"));
        inv.setItem(13, createItem(Material.IRON_HELMET, "§eOFFICER", "§7Modifier les perms des Officiers"));
        inv.setItem(16, createItem(Material.PINK_DYE, "§dALLY", "§7Modifier les perms des Alliés"));
        player.openInventory(inv);
    }

    public static void openRelationsMenu(Player player, Faction faction) {
        Inventory inv = Bukkit.createInventory(new FactionInventoryHolder("RELATIONS", faction), 27, "§c§lRelations: " + faction.getName());
        fillBorder(inv);
        inv.setItem(22, createItem(Material.SHEARS, "§7Retour", "§8Clic pour revenir"));

        inv.setItem(10, createItem(Material.PINK_DYE, "§dAlliés", "§7Voir vos alliés actuels"));
        inv.setItem(12, createItem(Material.ORANGE_DYE, "§6Trêves", "§7Voir vos trêves actuelles"));
        inv.setItem(14, createItem(Material.RED_DYE, "§cEnnemis", "§7Voir vos ennemis actuels"));
        inv.setItem(16, createItem(Material.COMPASS, "§eToutes les Factions", "§7Gérer les relations avec les autres"));

        player.openInventory(inv);
    }

    public static void openFactionsListMenu(Player player, Faction playerFaction, java.util.Collection<Faction> allFactions) {
        Inventory inv = Bukkit.createInventory(new FactionInventoryHolder("FACTIONS_LIST", playerFaction), 54, "§c§lListe des Factions");
        fillBorder(inv);
        inv.setItem(49, createItem(Material.SHEARS, "§7Retour", "§8Clic pour revenir"));

        int slot = 10;
        for (Faction f : allFactions) {
            if (f.getId().equals(playerFaction.getId())) continue;
            if (f.getType() != fr.jules.faction.model.FactionType.NORMAL) continue;
            if (slot >= 44) break;
            if (slot % 9 == 0 || slot % 9 == 8) slot++;

            String rel = playerFaction.getRelations().getOrDefault(f.getId(), "NEUTRAL");
            inv.setItem(slot++, createItem(Material.PAPER, "§e" + f.getName(),
                "§7Relation: §f" + rel,
                "§7Clic Gauche: §dAllié",
                "§7Clic Droit: §cEnnemi",
                "§7Shift+Clic Gauche: §6Trêve",
                "§7Shift+Clic Droit: §fNeutre"));
        }
        player.openInventory(inv);
    }

    public static void openPetMenu(Player player, fr.jules.faction.model.PlayerData data) {
        Inventory inv = Bukkit.createInventory(new FactionInventoryHolder("PETS", data), 27, "§c§lAnimaux de Compagnie");
        fillBorder(inv);
        inv.setItem(22, createItem(Material.SHEARS, "§7Retour", "§8Clic pour revenir"));

        String[] pets = {"LOUP", "CHAT", "PERROQUET", "RENARD"};
        Material[] icons = {Material.BONE, Material.COD, Material.FEATHER, Material.SWEET_BERRIES};
        String[] bonuses = {"Force I", "Vitesse I", "Saut I", "Vision Nocturne"};
        int[] slots = {10, 12, 14, 16};

        for (int i = 0; i < pets.length; i++) {
            boolean owned = data.getOwnedPets().contains(pets[i]);
            inv.setItem(slots[i], createItem(owned ? icons[i] : Material.BARRIER,
                "§e" + pets[i],
                "§7Bonus: " + bonuses[i],
                owned ? "§aPossédé - Clic pour invoquer" : "§cVerrouillé - Capturez-le avec un Lasso !"));
        }

        inv.setItem(4, createItem(Material.BARRIER, "§cRenvoyer", "§7Faire disparaitre le familier"));

        player.openInventory(inv);
    }

    public static void openClaimsMenu(Player player, Faction faction) {
        Inventory inv = Bukkit.createInventory(new FactionInventoryHolder("CLAIMS", faction), 27, "§c§lTerritoires: " + faction.getName());
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
        ItemStack pane = createItem(Material.RED_STAINED_GLASS_PANE, " ");
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
