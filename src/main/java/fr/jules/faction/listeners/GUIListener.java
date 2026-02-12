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
            } else if (title.startsWith("§6Permissions: ")) {
                handlePermissionsMenuClick(player, name, faction);
            } else if (title.startsWith("§6Sélecteur de Grade")) {
                handleGradeSelectorClick(player, name, faction);
            } else if (title.startsWith("§6Grade: ")) {
                handleGradePermissionsClick(player, name, faction, title);
            }
        }
    }

    private void handleMainMenuClick(Player player, String name, Faction faction) {
        if (name.equalsIgnoreCase("Membres")) {
            fr.jules.faction.gui.FactionGUI.openMembersMenu(player, faction);
        } else if (name.equalsIgnoreCase("Informations")) {
            player.performCommand("f faction");
            player.closeInventory();
        } else if (name.equalsIgnoreCase("Claims")) {
            player.performCommand("f map");
            player.closeInventory();
        } else if (name.equalsIgnoreCase("Permissions")) {
            fr.jules.faction.gui.FactionGUI.openPermissionsMenu(player, faction);
        }
    }

    private void handleMembersMenuClick(Player player, String targetName, Faction faction, InventoryClickEvent event) {
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
        if (!faction.isOfficer(player.getUniqueId())) {
            player.sendMessage("§cSeuls les officiers peuvent gérer les permissions.");
            return;
        }
        if (permName.equalsIgnoreCase("Permissions par Grade")) {
            fr.jules.faction.gui.FactionGUI.openGradeSelectorMenu(player, faction);
            return;
        }

        boolean current = faction.getFactionFlags().getOrDefault(permName, false);
        if (permName.equals("ALLY_HOME")) current = faction.getFactionFlags().getOrDefault("ALLY_HOME", true);

        faction.getFactionFlags().put(permName, !current);
        player.sendMessage("§aOption " + permName + " passée à: " + (!current));
        fr.jules.faction.gui.FactionGUI.openPermissionsMenu(player, faction);
    }

    private void handleGradeSelectorClick(Player player, String name, Faction faction) {
        if (name.equals("Retour")) {
            fr.jules.faction.gui.FactionGUI.openPermissionsMenu(player, faction);
            return;
        }
        fr.jules.faction.model.Grade grade = fr.jules.faction.model.Grade.valueOf(name);
        fr.jules.faction.gui.FactionGUI.openGradePermissionsMenu(player, faction, grade);
    }

    private void handleGradePermissionsClick(Player player, String action, Faction faction, String title) {
        if (action.equals("Retour")) {
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
}
