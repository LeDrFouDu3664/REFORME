package fr.jules.faction.listeners;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.model.Faction;
import fr.jules.faction.model.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.UUID;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

public class GUIListener implements Listener {
    private final FactionPlugin plugin;

    public GUIListener(FactionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (title.startsWith("§6Gestion: ") || title.startsWith("§6Membres: ") || title.startsWith("§6Permissions: ") ||
            title.startsWith("§6Perms: ") ||
            title.startsWith("§6Relations: ") || title.startsWith("§6Territoires: ") ||
            title.startsWith("§6Banque: ") || title.startsWith("§6Liste des Factions") ||
            title.startsWith("§6Paramètres: ") || title.startsWith("§6Récompenses de Niveau") ||
            title.startsWith("§6Boutique: ") || title.equals("§6Boutique Administrative") ||
            title.equals("§6Métiers") || title.equals("§6Quêtes") || title.equals("§6Pouvoirs de Faction") ||
            title.equals("§6Boutique Faction") || title.equals("§6Boutique Me's")) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;
            String name = org.bukkit.ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

            PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
            Faction faction = data.getFactionId() != null ? plugin.getFactionManager().getFaction(data.getFactionId()) : null;

            if (title.startsWith("§6Gestion: ") && faction != null) {
                handleMainMenuClick(player, name, faction);
            } else if (title.startsWith("§6Membres: ") && faction != null) {
                handleMembersMenuClick(player, name, faction, event);
            } else if (title.startsWith("§6Paramètres: ") && faction != null) {
                handleParametersMenuClick(player, name, faction);
            } else if (title.startsWith("§6Permissions: ") && faction != null) {
                handlePermissionsSelectorClick(player, name, faction);
            } else if (title.startsWith("§6Perms: ") && faction != null) {
                handleRankPermissionsClick(player, name, faction, title);
            } else if (title.startsWith("§6Relations: ") && faction != null) {
                handleRelationsMenuClick(player, name, faction);
            } else if (title.startsWith("§6Liste des Factions") && faction != null) {
                handleFactionsListMenuClick(player, name, faction, event);
            } else if (title.startsWith("§6Territoires: ") && faction != null) {
                handleTerritoriesMenuClick(player, name, faction);
            } else if (title.startsWith("§6Banque: ") && faction != null) {
                handleBankMenuClick(player, name, faction, event.getRawSlot());
            } else if (title.startsWith("§6Récompenses de Niveau") && faction != null) {
                handleFactionLevelMenuClick(player, name, faction);
            } else if (title.equals("§6Métiers")) {
                handleJobsMenuClick(player, name, data, faction);
            } else if (title.equals("§6Quêtes")) {
                handleQuestsMenuClick(player, name, faction);
            } else if (title.equals("§6Pouvoirs de Faction")) {
                handlePowersMenuClick(player, name, data, faction);
            } else if (title.equals("§6Boutique Administrative")) {
                handleAdminShopMainClick(player, name);
            } else if (title.startsWith("§6Boutique: ")) {
                handleAdminShopCategoryClick(player, event, title.replace("§6Boutique: ", ""));
            } else if (title.equals("§6Boutique Faction")) {
                handleShopClick(player, event, faction);
            } else if (title.equals("§6Boutique Me's")) {
                handleBoutiqueClick(player, event);
            }
        }
    }

    private void handleMainMenuClick(Player player, String name, Faction faction) {
        if (name.equalsIgnoreCase("Membres")) {
            fr.jules.faction.gui.FactionGUI.openMembersMenu(player, faction);
        } else if (name.equalsIgnoreCase("Statistiques")) {
            player.performCommand("f faction");
            player.closeInventory();
        } else if (name.equalsIgnoreCase("Territoires")) {
            fr.jules.faction.gui.FactionGUI.openClaimsMenu(player, faction);
        } else if (name.equalsIgnoreCase("Banque")) {
            fr.jules.faction.gui.FactionGUI.openBankMenu(player, faction);
        } else if (name.equalsIgnoreCase("Relations")) {
            fr.jules.faction.gui.FactionGUI.openRelationsMenu(player, faction);
        } else if (name.equalsIgnoreCase("Paramètres")) {
            fr.jules.faction.gui.FactionGUI.openParametersMenu(player, faction);
        } else if (name.equalsIgnoreCase("Permissions")) {
            fr.jules.faction.gui.FactionGUI.openPermissionsMenu(player, faction);
        } else if (name.equalsIgnoreCase("Métiers")) {
            fr.jules.faction.gui.FactionGUI.openJobsMenu(player, plugin.getPlayerManager().getPlayerData(player.getUniqueId()));
        } else if (name.equalsIgnoreCase("Quêtes")) {
            fr.jules.faction.gui.FactionGUI.openQuestsMenu(player, plugin.getPlayerManager().getPlayerData(player.getUniqueId()), plugin.getQuestManager());
        } else if (name.equalsIgnoreCase("Niveaux Faction")) {
            fr.jules.faction.gui.FactionGUI.openFactionLevelMenu(player, faction);
        } else if (name.equalsIgnoreCase("Pouvoirs")) {
            fr.jules.faction.gui.FactionGUI.openPowersMenu(player, plugin.getPlayerManager().getPlayerData(player.getUniqueId()), plugin.getPowerManager());
        }
    }

    private void handleMembersMenuClick(Player player, String targetName, Faction faction, InventoryClickEvent event) {
        if (targetName.equalsIgnoreCase("Retour")) {
            fr.jules.faction.gui.FactionGUI.openMainMenu(player, faction);
            return;
        }
        if (!faction.getLeader().equals(player.getUniqueId())) {
            player.sendMessage("§cSeul le chef peut gérer les membres via le menu.");
            return;
        }
        if (event.getClick().isShiftClick()) {
            player.performCommand("f kick " + targetName);
        } else if (event.getClick().isLeftClick()) {
            player.performCommand("f promote " + targetName);
        } else if (event.getClick().isRightClick()) {
            player.performCommand("f demote " + targetName);
        }
        fr.jules.faction.gui.FactionGUI.openMembersMenu(player, faction);
    }

    private void handleParametersMenuClick(Player player, String permName, Faction faction) {
        if (permName.equalsIgnoreCase("Retour")) {
            fr.jules.faction.gui.FactionGUI.openMainMenu(player, faction);
            return;
        }
        if (!faction.isOfficer(player.getUniqueId())) {
            player.sendMessage("§cSeuls les officiers peuvent gérer les paramètres.");
            return;
        }
        if (permName.equalsIgnoreCase("Description")) {
            player.sendMessage("§eUtilisez /f desc [texte] pour changer la description.");
            player.closeInventory();
            return;
        }
        if (permName.equalsIgnoreCase("MOTD")) {
            player.sendMessage("§eUtilisez /f motd [texte] pour changer le message du jour.");
            player.closeInventory();
            return;
        }

        boolean current = faction.getFactionFlags().getOrDefault(permName, false);
        if (permName.equals("ALLY_HOME")) current = faction.getFactionFlags().getOrDefault("ALLY_HOME", true);

        faction.getFactionFlags().put(permName, !current);
        player.sendMessage("§aOption " + permName + " passée à: " + (!current));
        fr.jules.faction.gui.FactionGUI.openParametersMenu(player, faction);
    }

    private void handlePermissionsSelectorClick(Player player, String name, Faction faction) {
        if (name.equalsIgnoreCase("Retour")) {
            fr.jules.faction.gui.FactionGUI.openMainMenu(player, faction);
            return;
        }
        if (name.equalsIgnoreCase("ALLY")) {
            fr.jules.faction.gui.FactionGUI.openRankPermissionsMenu(player, faction, "ALLY");
        } else {
            try {
                fr.jules.faction.model.Grade grade = fr.jules.faction.model.Grade.valueOf(name.toUpperCase());
                fr.jules.faction.gui.FactionGUI.openRankPermissionsMenu(player, faction, grade.name());
            } catch (IllegalArgumentException ignored) {}
        }
    }

    private void handleRankPermissionsClick(Player player, String action, Faction faction, String title) {
        if (action.equalsIgnoreCase("Retour")) {
            fr.jules.faction.gui.FactionGUI.openPermissionsMenu(player, faction);
            return;
        }
        // Title: §6Perms: RANK (Fac)
        String target = title.split(" ")[1];

        if (target.equals("ALLY")) {
            String flagKey = "ALLY_" + action;
            boolean current = faction.getFactionFlags().getOrDefault(flagKey, false);
            faction.getFactionFlags().put(flagKey, !current);
            player.sendMessage("§aPermission " + action + " pour les ALLIES modifiée.");
        } else {
            fr.jules.faction.model.Grade grade = fr.jules.faction.model.Grade.valueOf(target);
            java.util.Set<fr.jules.faction.model.Grade> allowed = faction.getPermissions().computeIfAbsent(action, k -> new java.util.HashSet<>());
            if (allowed.contains(grade)) {
                allowed.remove(grade);
            } else {
                allowed.add(grade);
            }
            player.sendMessage("§aPermission " + action + " pour " + target + " modifiée.");
        }
        fr.jules.faction.gui.FactionGUI.openRankPermissionsMenu(player, faction, target);
    }

    private void handleRelationsMenuClick(Player player, String name, Faction faction) {
        if (name.equalsIgnoreCase("Retour")) {
            fr.jules.faction.gui.FactionGUI.openMainMenu(player, faction);
            return;
        }
        if (name.equalsIgnoreCase("Alliés")) {
            player.sendMessage("§dVos alliés: " + String.join(", ", faction.getRelations().entrySet().stream().filter(e -> e.getValue().equals("ALLY")).map(e -> {
                Faction f = plugin.getFactionManager().getFaction(e.getKey());
                return f != null ? f.getName() : "Inconnu";
            }).toList()));
        } else if (name.equalsIgnoreCase("Trêves")) {
            player.sendMessage("§6Vos trêves: " + String.join(", ", faction.getRelations().entrySet().stream().filter(e -> e.getValue().equals("TRUCE")).map(e -> {
                Faction f = plugin.getFactionManager().getFaction(e.getKey());
                return f != null ? f.getName() : "Inconnu";
            }).toList()));
        } else if (name.equalsIgnoreCase("Ennemis")) {
            player.sendMessage("§cVos ennemis: " + String.join(", ", faction.getRelations().entrySet().stream().filter(e -> e.getValue().equals("ENEMY")).map(e -> {
                Faction f = plugin.getFactionManager().getFaction(e.getKey());
                return f != null ? f.getName() : "Inconnu";
            }).toList()));
        } else if (name.equalsIgnoreCase("Toutes les Factions")) {
            fr.jules.faction.gui.FactionGUI.openFactionsListMenu(player, faction, plugin.getFactionManager().getAllFactions());
        }
    }

    private void handleFactionsListMenuClick(Player player, String targetFactionName, Faction faction, InventoryClickEvent event) {
        if (targetFactionName.equalsIgnoreCase("Retour")) {
            fr.jules.faction.gui.FactionGUI.openRelationsMenu(player, faction);
            return;
        }

        if (!faction.isOfficer(player.getUniqueId())) {
            player.sendMessage("§cSeuls les officiers peuvent gérer les relations.");
            return;
        }

        String rel = null;
        if (event.getClick().isLeftClick()) {
            if (event.getClick().isShiftClick()) rel = "truce";
            else rel = "ally";
        } else if (event.getClick().isRightClick()) {
            if (event.getClick().isShiftClick()) rel = "neutral";
            else rel = "enemy";
        }

        if (rel != null) {
            player.performCommand("f " + rel + " " + targetFactionName);
            fr.jules.faction.gui.FactionGUI.openFactionsListMenu(player, faction, plugin.getFactionManager().getAllFactions());
        }
    }

    private void handleTerritoriesMenuClick(Player player, String name, Faction faction) {
        if (name.equalsIgnoreCase("Retour")) {
            fr.jules.faction.gui.FactionGUI.openMainMenu(player, faction);
            return;
        }
        if (name.equalsIgnoreCase("Claim")) {
            player.performCommand("f claim");
        } else if (name.equalsIgnoreCase("Unclaim")) {
            player.performCommand("f unclaim");
        } else if (name.equalsIgnoreCase("Carte")) {
            player.performCommand("f map");
        } else if (name.equalsIgnoreCase("Auto-Claim")) {
            player.performCommand("f claim auto");
        } else if (name.equalsIgnoreCase("Unclaim All")) {
            player.performCommand("f unclaim all");
        }
        player.closeInventory();
    }

    private void handleBankMenuClick(Player player, String name, Faction faction, int slot) {
        if (name.equalsIgnoreCase("Retour")) {
            fr.jules.faction.gui.FactionGUI.openMainMenu(player, faction);
            return;
        }

        if (slot == 11) player.performCommand("f money deposit 100");
        else if (slot == 12) player.performCommand("f money deposit 1000");
        else if (slot == 14) player.performCommand("f money withdraw 100");
        else if (slot == 15) player.performCommand("f money withdraw 1000");

        fr.jules.faction.gui.FactionGUI.openBankMenu(player, faction);
    }

    private void handleFactionLevelMenuClick(Player player, String name, Faction faction) {
        if (name.equalsIgnoreCase("Retour")) {
            fr.jules.faction.gui.FactionGUI.openMainMenu(player, faction);
        }
    }

    private void handleJobsMenuClick(Player player, String name, PlayerData data, Faction faction) {
        if (name.equalsIgnoreCase("Retour")) {
            if (faction != null) fr.jules.faction.gui.FactionGUI.openMainMenu(player, faction);
            else player.closeInventory();
            return;
        }
        if (name.contains("Mineur")) data.setJob("MINEUR");
        else if (name.contains("Bûcheron")) data.setJob("BUCHERON");
        else if (name.contains("Fermier")) data.setJob("FERMIER");
        else if (name.contains("Guerrier")) data.setJob("GUERRIER");
        else return;

        player.sendMessage("§b§l[Métier] §aVous avez choisi le métier: §e" + data.getJob());
        fr.jules.faction.gui.FactionGUI.openJobsMenu(player, data);
    }

    private void handleQuestsMenuClick(Player player, String name, Faction faction) {
        if (name.equalsIgnoreCase("Retour")) {
            if (faction != null) fr.jules.faction.gui.FactionGUI.openMainMenu(player, faction);
            else player.closeInventory();
        }
    }

    private void handlePowersMenuClick(Player player, String name, PlayerData data, Faction faction) {
        if (name.equalsIgnoreCase("Retour")) {
            if (faction != null) fr.jules.faction.gui.FactionGUI.openMainMenu(player, faction);
            else player.closeInventory();
            return;
        }

        String powerId = null;
        for (Map.Entry<String, fr.jules.faction.manager.PowerManager.PowerInfo> entry : plugin.getPowerManager().getPowers().entrySet()) {
            if (entry.getValue().name.equalsIgnoreCase(name)) {
                powerId = entry.getKey();
                break;
            }
        }

        if (powerId == null) return;
        if (data.getPowers().contains(powerId)) {
            player.sendMessage("§cVous possédez déjà ce pouvoir.");
            return;
        }

        double cost = 5000;
        if (powerId.contains("II") || powerId.equals("VAMPIRE") || powerId.equals("STRENGTH")) cost = 15000;

        if (plugin.getEconomyManager().has(player, cost)) {
            plugin.getEconomyManager().withdraw(player, cost);
            data.getPowers().add(powerId);
            player.sendMessage("§aPouvoir §e" + name + " §adébloqué !");
            fr.jules.faction.gui.FactionGUI.openPowersMenu(player, data, plugin.getPowerManager());
        } else {
            player.sendMessage("§cPas assez d'argent (" + String.format("%.0f", cost) + "$).");
        }
    }

    private void handleShopClick(Player player, InventoryClickEvent event, Faction faction) {
        int slot = event.getRawSlot();
        double price = 0;
        org.bukkit.Material mat = null;
        int amount = 1;

        if (slot == 11) { price = 500; mat = org.bukkit.Material.DIAMOND_SWORD; }
        else if (slot == 13) { price = 250; mat = org.bukkit.Material.GOLDEN_APPLE; }
        else if (slot == 15) { price = 100; mat = org.bukkit.Material.OBSIDIAN; amount = 16; }

        if (mat == null) return;

        if (plugin.getEconomyManager().has(player, price)) {
            plugin.getEconomyManager().withdraw(player, price);
            player.getInventory().addItem(new org.bukkit.inventory.ItemStack(mat, amount));
            player.sendMessage("§aAchat réussi !");
        } else {
            player.sendMessage("§cVous n'avez pas assez d'argent.");
        }
    }

    private void handleAdminShopMainClick(Player player, String name) {
        if (name.contains("Blocs")) fr.jules.faction.gui.ShopGUI.openCategoryMenu(player, "Blocs");
        else if (name.contains("Combat")) fr.jules.faction.gui.ShopGUI.openCategoryMenu(player, "Combat");
        else if (name.contains("Agriculture")) fr.jules.faction.gui.ShopGUI.openCategoryMenu(player, "Agriculture");
    }

    private void handleAdminShopCategoryClick(Player player, InventoryClickEvent event, String category) {
        if (org.bukkit.ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()).equalsIgnoreCase("Retour")) {
            fr.jules.faction.gui.ShopGUI.openShopMenu(player);
            return;
        }

        List<String> lore = event.getCurrentItem().getItemMeta().getLore();
        if (lore == null || lore.size() < 3) return;

        int amount = Integer.parseInt(org.bukkit.ChatColor.stripColor(lore.get(0)).replace("Quantité: ", ""));
        double buyPrice = Double.parseDouble(org.bukkit.ChatColor.stripColor(lore.get(1)).replace("Prix Achat: ", "").replace("$", ""));
        double sellPrice = Double.parseDouble(org.bukkit.ChatColor.stripColor(lore.get(2)).replace("Prix Vente: ", "").replace("$", ""));
        org.bukkit.Material material = event.getCurrentItem().getType();

        if (event.getClick().isLeftClick()) {
            if (plugin.getEconomyManager().has(player, buyPrice)) {
                plugin.getEconomyManager().withdraw(player, buyPrice);
                player.getInventory().addItem(new org.bukkit.inventory.ItemStack(material, amount));
                player.sendMessage("§aAchat de " + amount + " " + material.name() + " pour " + buyPrice + "$.");
            } else {
                player.sendMessage("§cPas assez d'argent.");
            }
        } else if (event.getClick().isRightClick()) {
            if (player.getInventory().containsAtLeast(new org.bukkit.inventory.ItemStack(material), amount)) {
                player.getInventory().removeItem(new org.bukkit.inventory.ItemStack(material, amount));
                plugin.getEconomyManager().deposit(player, sellPrice);
                player.sendMessage("§aVente de " + amount + " " + material.name() + " pour " + sellPrice + "$.");
            } else {
                player.sendMessage("§cVous n'avez pas assez d'items.");
            }
        }
    }

    private void handleBoutiqueClick(Player player, InventoryClickEvent event) {
        int slot = event.getRawSlot();
        // Pour la boutique Me's, on pourrait utiliser une autre monnaie.
        // Ici on utilise aussi l'économie pour l'exemple.
        double price = 0;
        org.bukkit.Material mat = null;

        if (slot == 11) { price = 1000; mat = org.bukkit.Material.NETHER_STAR; }
        else if (slot == 13) { price = 500; mat = org.bukkit.Material.EXPERIENCE_BOTTLE; }
        else if (slot == 15) { price = 750; mat = org.bukkit.Material.ENCHANTED_GOLDEN_APPLE; }

        if (mat == null) return;

        if (plugin.getEconomyManager().has(player, price)) {
            plugin.getEconomyManager().withdraw(player, price);
            player.getInventory().addItem(new org.bukkit.inventory.ItemStack(mat, slot == 13 ? 64 : 1));
            player.sendMessage("§bAchat boutique réussi !");
        } else {
            player.sendMessage("§cVous n'avez pas assez de Me's.");
        }
    }
}
