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
            title.startsWith("§6Grade: ") || title.startsWith("§6Sélecteur de Grade") ||
            title.startsWith("§6Relations: ") || title.startsWith("§6Territoires: ") ||
            title.startsWith("§6Banque: ") || title.startsWith("§6Liste des Factions") ||
            title.equals("§6Boutique Faction") || title.equals("§6Boutique Me's")) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;
            String name = org.bukkit.ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

            PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
            if (data.getFactionId() == null) return;
            Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());

            if (title.startsWith("§6Gestion: ")) {
                handleMainMenuClick(player, name, faction);
            } else if (title.startsWith("§6Membres: ")) {
                handleMembersMenuClick(player, name, faction, event);
            } else if (title.startsWith("§6Permissions: ") || title.startsWith("§6Paramètres: ")) {
                handlePermissionsMenuClick(player, name, faction);
            } else if (title.startsWith("§6Sélecteur de Grade")) {
                handleGradeSelectorClick(player, name, faction);
            } else if (title.startsWith("§6Grade: ")) {
                handleGradePermissionsClick(player, name, faction, title);
            } else if (title.startsWith("§6Relations: ")) {
                handleRelationsMenuClick(player, name, faction);
            } else if (title.startsWith("§6Liste des Factions")) {
                handleFactionsListMenuClick(player, name, faction, event);
            } else if (title.startsWith("§6Territoires: ")) {
                handleTerritoriesMenuClick(player, name, faction);
            } else if (title.startsWith("§6Banque: ")) {
                handleBankMenuClick(player, name, faction, event.getRawSlot());
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
            fr.jules.faction.gui.FactionGUI.openPermissionsMenu(player, faction);
        } else if (name.equalsIgnoreCase("Permissions")) {
            fr.jules.faction.gui.FactionGUI.openGradeSelectorMenu(player, faction);
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

    private void handlePermissionsMenuClick(Player player, String permName, Faction faction) {
        if (permName.equalsIgnoreCase("Retour")) {
            fr.jules.faction.gui.FactionGUI.openMainMenu(player, faction);
            return;
        }
        if (!faction.isOfficer(player.getUniqueId())) {
            player.sendMessage("§cSeuls les officiers peuvent gérer les permissions.");
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
        fr.jules.faction.gui.FactionGUI.openPermissionsMenu(player, faction);
    }

    private void handleGradeSelectorClick(Player player, String name, Faction faction) {
        if (name.equalsIgnoreCase("Retour")) {
            fr.jules.faction.gui.FactionGUI.openPermissionsMenu(player, faction);
            return;
        }
        try {
            fr.jules.faction.model.Grade grade = fr.jules.faction.model.Grade.valueOf(name.toUpperCase());
            fr.jules.faction.gui.FactionGUI.openGradePermissionsMenu(player, faction, grade);
        } catch (IllegalArgumentException ignored) {}
    }

    private void handleGradePermissionsClick(Player player, String action, Faction faction, String title) {
        if (action.equalsIgnoreCase("Retour")) {
            fr.jules.faction.gui.FactionGUI.openGradeSelectorMenu(player, faction);
            return;
        }
        String gradeName = title.split(" ")[1];
        fr.jules.faction.model.Grade grade = fr.jules.faction.model.Grade.valueOf(gradeName);

        java.util.Set<fr.jules.faction.model.Grade> allowed = faction.getPermissions().computeIfAbsent(action, k -> new java.util.HashSet<>());
        if (allowed.contains(grade)) {
            allowed.remove(grade);
        } else {
            allowed.add(grade);
        }
        player.sendMessage("§aPermission " + action + " pour " + gradeName + " modifiée.");
        fr.jules.faction.gui.FactionGUI.openGradePermissionsMenu(player, faction, grade);
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
