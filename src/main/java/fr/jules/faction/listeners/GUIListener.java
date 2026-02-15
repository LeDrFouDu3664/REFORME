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
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof fr.jules.faction.gui.FactionInventoryHolder holder)) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;
        String name = org.bukkit.ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        Faction faction = data.getFactionId() != null ? plugin.getFactionManager().getFaction(data.getFactionId()) : null;

        String type = holder.getType();
        switch (type) {
            case "MAIN":
                if (faction != null) handleMainMenuClick(player, name, faction);
                break;
            case "MEMBERS":
                if (faction != null) handleMembersMenuClick(player, name, faction, event);
                break;
            case "PARAMETERS":
                if (faction != null) handleParametersMenuClick(player, name, faction);
                break;
            case "PERMISSIONS":
                if (faction != null) handlePermissionsSelectorClick(player, name, faction);
                break;
            case "RANK_PERMS":
                if (faction != null) handleRankPermissionsClick(player, name, faction, (String) holder.getData());
                break;
            case "RELATIONS":
                if (faction != null) handleRelationsMenuClick(player, name, faction);
                break;
            case "FACTIONS_LIST":
                if (faction != null) handleFactionsListMenuClick(player, name, faction, event);
                break;
            case "CLAIMS":
                if (faction != null) handleTerritoriesMenuClick(player, name, faction);
                break;
            case "BANK":
                if (faction != null) handleBankMenuClick(player, name, faction, event.getRawSlot());
                break;
            case "LEVELS":
                if (faction != null) handleFactionLevelMenuClick(player, name, faction);
                break;
            case "JOBS":
                handleJobsMenuClick(player, name, data, faction);
                break;
            case "QUESTS":
                handleQuestsMenuClick(player, name, faction);
                break;
            case "POWERS":
                handlePowersMenuClick(player, name, data, faction);
                break;
            case "PETS":
                handlePetMenuClick(player, name, data, faction);
                break;
            case "SHOP_MAIN":
                handleAdminShopMainClick(player, name);
                break;
            case "SHOP_CATEGORY":
                handleAdminShopCategoryClick(player, event, (String) holder.getData());
                break;
            case "BOUTIQUE":
                handleBoutiqueClick(player, event);
                break;
            case "AUCTION":
                handleAuctionClick(player, event);
                break;
            case "MOD_MAIN":
                handleModMainClick(player, name);
                break;
            case "MOD_PLAYERS":
                handleModPlayersClick(player, name);
                break;
            case "MOD_ACTIONS":
                handleModActionsClick(player, name, (Player) holder.getData());
                break;
            case "SPAWNER":
                handleSpawnerClick(player, name, (org.bukkit.block.Block) holder.getData(), event);
                break;
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
        } else if (name.equalsIgnoreCase("Compagnon")) {
            fr.jules.faction.gui.FactionGUI.openPetMenu(player, plugin.getPlayerManager().getPlayerData(player.getUniqueId()));
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

        // Correct default for mob griefing might be false (safe by default)
        if (permName.equals("MOB_GRIEFING")) current = faction.getFactionFlags().getOrDefault("MOB_GRIEFING", false);

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

    private void handleRankPermissionsClick(Player player, String action, Faction faction, String target) {
        if (action.equalsIgnoreCase("Retour")) {
            fr.jules.faction.gui.FactionGUI.openPermissionsMenu(player, faction);
            return;
        }

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

    private void handlePetMenuClick(Player player, String name, PlayerData data, Faction faction) {
        String petId = name; // Color already stripped in onInventoryClick

        if (name.equalsIgnoreCase("Retour")) {
            if (faction != null) fr.jules.faction.gui.FactionGUI.openMainMenu(player, faction);
            else player.closeInventory();
            return;
        }
        if (name.equalsIgnoreCase("Renvoyer")) {
            plugin.getPetManager().despawnPet(player);
            player.sendMessage("§aAnimal renvoyé.");
            return;
        }

        if (data.getCapturedPets().containsKey(petId)) {
            plugin.getPetManager().spawnPet(player, petId);
            player.closeInventory();
        } else if (!name.equalsIgnoreCase("Pas de compagnon")) {
            player.sendMessage("§cVous ne possédez pas cet animal. Capturez-le d'abord !");
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

        double cost = 5000;
        if (powerId.contains("II") || powerId.equals("VAMPIRE") || powerId.equals("STRENGTH")) cost = 15000;

        if (plugin.getEconomyManager().has(player, cost)) {
            if (data.getActivePower().equals(powerId)) {
                player.sendMessage("§cCe pouvoir est déjà actif.");
                return;
            }
            plugin.getEconomyManager().withdraw(player, cost);
            data.setActivePower(powerId);
            plugin.getPowerManager().refreshPowerEffects(player);
            player.sendMessage("§a§l[Pouvoirs] §aVous avez activé le pouvoir: §e" + name);
            fr.jules.faction.gui.FactionGUI.openPowersMenu(player, data, plugin.getPowerManager());
        } else {
            player.sendMessage("§cPas assez d'argent pour activer ce pouvoir (" + String.format("%.0f", cost) + "$).");
        }
    }

    private void handleAdminShopMainClick(Player player, String name) {
        if (name.contains("Blocs")) fr.jules.faction.gui.ShopGUI.openCategoryMenu(player, "Blocs");
        else if (name.contains("Combat")) fr.jules.faction.gui.ShopGUI.openCategoryMenu(player, "Combat");
        else if (name.contains("Agriculture")) fr.jules.faction.gui.ShopGUI.openCategoryMenu(player, "Agriculture");
        else if (name.contains("Spécial")) fr.jules.faction.gui.ShopGUI.openCategoryMenu(player, "Spécial");
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

    private void handleAuctionClick(Player player, InventoryClickEvent event) {
        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;
        List<String> lore = event.getCurrentItem().getItemMeta().getLore();
        if (lore == null) return;

        String idStr = null;
        for (String s : lore) {
            if (s.startsWith("§0ID:")) {
                idStr = s.replace("§0ID:", "");
                break;
            }
        }

        if (idStr == null) return;
        UUID id = UUID.fromString(idStr);
        fr.jules.faction.model.AuctionItem ai = plugin.getAuctionManager().getItem(id);

        if (ai == null) {
            player.sendMessage("§cCet objet n'est plus en vente.");
            fr.jules.faction.gui.AuctionGUI.openAuctionMenu(player, plugin.getAuctionManager().getItems());
            return;
        }

        if (ai.getSellerId().equals(player.getUniqueId())) {
            player.sendMessage("§cVous ne pouvez pas acheter votre propre objet.");
            return;
        }

        if (plugin.getEconomyManager().has(player, ai.getPrice())) {
            plugin.getEconomyManager().withdraw(player, ai.getPrice());
            plugin.getEconomyManager().deposit(Bukkit.getOfflinePlayer(ai.getSellerId()), ai.getPrice());

            player.getInventory().addItem(ai.getItem());
            plugin.getAuctionManager().removeItem(id);

            player.sendMessage("§aVous avez acheté l'objet pour " + ai.getPrice() + "$ !");
            Player seller = Bukkit.getPlayer(ai.getSellerId());
            if (seller != null) seller.sendMessage("§aVotre objet a été vendu pour " + ai.getPrice() + "$ !");

            fr.jules.faction.gui.AuctionGUI.openAuctionMenu(player, plugin.getAuctionManager().getItems());
        } else {
            player.sendMessage("§cPas assez d'argent.");
        }
    }

    private void handleModMainClick(Player player, String name) {
        if (name.contains("Bâton")) {
            org.bukkit.inventory.ItemStack rod = new org.bukkit.inventory.ItemStack(org.bukkit.Material.BLAZE_ROD);
            org.bukkit.inventory.meta.ItemMeta meta = rod.getItemMeta();
            meta.setDisplayName("§6Bâton de Modération");
            rod.setItemMeta(meta);
            player.getInventory().addItem(rod);
            player.sendMessage("§aOutil de modération reçu.");
        } else if (name.contains("Freeze")) {
            fr.jules.faction.gui.ModGUI.openPlayerList(player);
        } else if (name.contains("Vanish")) {
            if (player.hasMetadata("vanished")) {
                player.removeMetadata("vanished", plugin);
                Bukkit.getOnlinePlayers().forEach(p -> p.showPlayer(plugin, player));
                player.sendMessage("§aVous n'êtes plus invisible.");
            } else {
                player.setMetadata("vanished", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
                Bukkit.getOnlinePlayers().forEach(p -> p.hidePlayer(plugin, player));
                player.sendMessage("§aVous êtes désormais invisible.");
            }
        } else if (name.contains("Joueurs")) {
            fr.jules.faction.gui.ModGUI.openPlayerList(player);
        }
    }

    private void handleModPlayersClick(Player staff, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target != null) {
            fr.jules.faction.gui.ModGUI.openPlayerActions(staff, target);
        }
    }

    private void handleModActionsClick(Player staff, String action, Player target) {
        if (target.getUniqueId().equals(staff.getUniqueId())) {
            staff.sendMessage("§cAction impossible sur vous-même !");
            return;
        }

        // Hierarchy check
        if (target.hasPermission("faction.admin") && !staff.isOp()) {
            staff.sendMessage("§cVous ne pouvez pas agir sur un administrateur !");
            return;
        }

        if (action.contains("Inventaire")) {
            staff.openInventory(target.getInventory());
        } else if (action.contains("Freeze")) {
            if (target.hasMetadata("frozen")) {
                target.removeMetadata("frozen", plugin);
                target.sendMessage("§aVous avez été libéré !");
                staff.sendMessage("§aJoueur libéré.");
            } else {
                target.setMetadata("frozen", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
                target.sendMessage("§cVous avez été gelé par un modérateur !");
                staff.sendMessage("§cJoueur gelé.");
            }
        } else if (action.contains("Kick")) {
            target.kickPlayer("§cExpulsé par un modérateur.");
            staff.closeInventory();
        } else if (action.contains("Ban")) {
            Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(target.getName(), "§cBanni par un modérateur.", null, null);
            target.kickPlayer("§cBanni du serveur.");
            staff.closeInventory();
        }
    }

    private void handleSpawnerClick(Player player, String name, org.bukkit.block.Block spawner, InventoryClickEvent event) {
        if (name.contains("Statut")) {
            if (spawner.hasMetadata("inactive")) {
                spawner.removeMetadata("inactive", plugin);
                player.sendMessage("§aSpawner activé !");
            } else {
                spawner.setMetadata("inactive", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
                player.sendMessage("§cSpawner désactivé !");
            }
            fr.jules.faction.gui.SpawnerGUI.openSpawnerMenu(player, spawner);
        } else if (name.contains("Récupérer")) {
            boolean hasPickaxe = false;
            org.bukkit.inventory.ItemStack pick = player.getInventory().getItemInMainHand();
            if (pick.getType() == org.bukkit.Material.DIAMOND_PICKAXE && pick.hasItemMeta() && pick.getItemMeta().getDisplayName().contains("Pioche à Spawner")) {
                hasPickaxe = true;
            }

            if (hasPickaxe) {
                giveSpawner(player, spawner);
            } else if (event.getClick().isRightClick()) {
                if (plugin.getEconomyManager().has(player, 25000)) {
                    plugin.getEconomyManager().withdraw(player, 25000);
                    giveSpawner(player, spawner);
                } else {
                    player.sendMessage("§cPas assez d'argent (25000$).");
                }
            } else {
                player.sendMessage("§cVous n'avez pas la pioche spéciale !");
            }
        } else if (name.contains("Acheter")) {
            if (plugin.getEconomyManager().has(player, 50000)) {
                plugin.getEconomyManager().withdraw(player, 50000);
                org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND_PICKAXE);
                org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
                meta.setDisplayName("§dPioche à Spawner");
                item.setItemMeta(meta);
                player.getInventory().addItem(item);
                player.sendMessage("§aPioche achetée !");
            } else {
                player.sendMessage("§cPas assez d'argent (50000$).");
            }
        }
    }

    private void giveSpawner(Player player, org.bukkit.block.Block block) {
        org.bukkit.block.CreatureSpawner cs = (org.bukkit.block.CreatureSpawner) block.getState();
        org.bukkit.entity.EntityType type = cs.getSpawnedType();

        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(org.bukkit.Material.SPAWNER);
        org.bukkit.inventory.meta.BlockStateMeta meta = (org.bukkit.inventory.meta.BlockStateMeta) item.getItemMeta();
        org.bukkit.block.CreatureSpawner metaCs = (org.bukkit.block.CreatureSpawner) meta.getBlockState();
        metaCs.setSpawnedType(type);
        meta.setBlockState(metaCs);
        meta.setDisplayName("§eSpawner: §b" + type.name());
        item.setItemMeta(meta);

        block.setType(org.bukkit.Material.AIR);
        player.getInventory().addItem(item);
        player.sendMessage("§aSpawner récupéré !");
        player.closeInventory();
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
