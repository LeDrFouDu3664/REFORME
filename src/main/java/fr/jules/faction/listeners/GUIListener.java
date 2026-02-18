package fr.jules.faction.listeners;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.model.Faction;
import fr.jules.faction.model.PlayerData;
import fr.jules.faction.model.PetInfo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.ChatColor;

import java.util.UUID;
import java.util.Map;
import java.util.List;

public class GUIListener implements Listener {
    private final FactionPlugin plugin;

    public GUIListener(FactionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta()) {
            String dn = event.getCurrentItem().getItemMeta().getDisplayName();
            if (dn.contains("§bVanish") || dn.contains("§bFreeze") || dn.contains("§eInvSee") || dn.contains("§6Outils Modération") || dn.contains("§cQuitter Staff Mode")) {
                event.setCancelled(true);
                return;
            }
        }

        if (!(event.getInventory().getHolder() instanceof fr.jules.faction.gui.FactionInventoryHolder holder)) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;
        String name = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());

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
                handlePetMenuClick(player, name, data, faction, event);
                break;
            case "PET_DETAIL":
                handlePetDetailClick(player, name, (String) holder.getData(), data, faction);
                break;
            case "PET_EQUIP":
                handlePetEquipmentClick(player, name, (String) holder.getData(), data);
                break;
            case "PET_POWERS":
                handlePetPowersClick(player, event, name, (String) holder.getData(), data);
                break;
            case "PET_SKILLS":
                handlePetSkillsClick(player, name, (String[]) holder.getData(), data);
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

    private void handlePetMenuClick(Player player, String name, PlayerData data, Faction faction, InventoryClickEvent event) {
        if (name.equalsIgnoreCase("Page Suivante")) {
            int page = (int) ((fr.jules.faction.gui.FactionInventoryHolder) event.getInventory().getHolder()).getData();
            fr.jules.faction.gui.FactionGUI.openPetMenu(player, data, page + 1);
            return;
        }
        if (name.equalsIgnoreCase("Page Précédente")) {
            int page = (int) ((fr.jules.faction.gui.FactionInventoryHolder) event.getInventory().getHolder()).getData();
            fr.jules.faction.gui.FactionGUI.openPetMenu(player, data, page - 1);
            return;
        }
        if (name.equalsIgnoreCase("Retour") || name.equalsIgnoreCase("Quitter")) {
            if (faction != null) fr.jules.faction.gui.FactionGUI.openMainMenu(player, faction);
            else player.closeInventory();
            return;
        }
        if (name.equalsIgnoreCase("Renvoyer")) {
            plugin.getPetManager().despawnPet(player);
            return;
        }

        String petIdMatch = null;
        for (String id : data.getCapturedPets().keySet()) {
            PetInfo pi = data.getCapturedPets().get(id);
            String dn = ChatColor.stripColor(pi.getCustomName() != null ? pi.getCustomName() : id);
            if (dn.equalsIgnoreCase(name)) {
                petIdMatch = id;
                break;
            }
        }

        if (petIdMatch != null) {
            if (event.getClick().isShiftClick() && event.getClick().isRightClick()) {
                data.getCapturedPets().remove(petIdMatch);
                player.sendMessage("§cCompagnon supprimé.");
                fr.jules.faction.gui.FactionGUI.openPetMenu(player, data);
            } else {
                fr.jules.faction.gui.FactionGUI.openPetDetailMenu(player, petIdMatch, data.getCapturedPets().get(petIdMatch));
            }
        }
    }

    private void handlePetDetailClick(Player player, String name, String petId, PlayerData data, Faction faction) {
        PetInfo info = data.getCapturedPets().get(petId);
        if (info == null) return;

        if (name.equalsIgnoreCase("Retour")) {
            fr.jules.faction.gui.FactionGUI.openPetMenu(player, data);
        } else if (name.equalsIgnoreCase("Invoquer")) {
            plugin.getPetManager().spawnPet(player, petId);
            player.closeInventory();
        } else if (name.equalsIgnoreCase("Renommer")) {
            if (plugin.getEconomyManager().has(player, 5000)) {
                player.sendMessage("§eEntrez le nouveau nom dans le chat.");
                player.setMetadata("renaming_pet", new org.bukkit.metadata.FixedMetadataValue(plugin, petId));
                player.closeInventory();
            } else {
                player.sendMessage("§cPas assez d'argent.");
            }
        } else if (name.equalsIgnoreCase("Equipement")) {
            fr.jules.faction.gui.FactionGUI.openPetEquipmentMenu(player, petId, info);
        } else if (name.equalsIgnoreCase("Pouvoirs & Compétences")) {
            fr.jules.faction.gui.FactionGUI.openPetPowersMenu(player, petId, info);
        } else if (name.equalsIgnoreCase("Renvoyer")) {
            plugin.getPetManager().despawnPet(player);
        }
    }

    private void handlePetEquipmentClick(Player player, String name, String petId, PlayerData data) {
        PetInfo info = data.getCapturedPets().get(petId);
        if (info == null) return;

        if (name.equalsIgnoreCase("Retour")) {
            fr.jules.faction.gui.FactionGUI.openPetDetailMenu(player, petId, info);
        } else if (name.equalsIgnoreCase("Selle")) {
            if (info.getSaddle() != null) {
                info.setSaddle(null);
                player.getInventory().addItem(new ItemStack(Material.SADDLE));
                player.sendMessage("§aSelle retirée.");
            } else if (player.getInventory().contains(Material.SADDLE)) {
                player.getInventory().removeItem(new ItemStack(Material.SADDLE, 1));
                info.setSaddle("SADDLE");
                player.sendMessage("§aSelle équipée !");
            } else player.sendMessage("§cVous n'avez pas de selle.");
            fr.jules.faction.gui.FactionGUI.openPetEquipmentMenu(player, petId, info);
        } else if (name.equalsIgnoreCase("Armure")) {
            if (info.getChestplate() != null) {
                Material mat = Material.valueOf(info.getChestplate());
                info.setChestplate(null);
                player.getInventory().addItem(new ItemStack(mat));
                player.sendMessage("§aArmure retirée.");
            } else {
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand.getType().name().contains("HORSE_ARMOR")) {
                    info.setChestplate(hand.getType().name());
                    hand.setAmount(hand.getAmount() - 1);
                    player.sendMessage("§aArmure équipée !");
                } else player.sendMessage("§cTenez une armure pour cheval.");
            }
            fr.jules.faction.gui.FactionGUI.openPetEquipmentMenu(player, petId, info);
        }
    }

    private void handlePetPowersClick(Player player, InventoryClickEvent event, String name, String petId, PlayerData data) {
        PetInfo info = data.getCapturedPets().get(petId);
        if (info == null) return;
        if (name.equalsIgnoreCase("Retour")) { fr.jules.faction.gui.FactionGUI.openPetDetailMenu(player, petId, info); return; }

        fr.jules.faction.modules.pet.PetModule petModule = (fr.jules.faction.modules.pet.PetModule) plugin.getModuleManager().getModule("Pet");
        org.bukkit.configuration.ConfigurationSection powersSec = petModule.getConfig().getConfigurationSection("powers");
        for (String pid : powersSec.getKeys(false)) {
            if (ChatColor.stripColor(powersSec.getString(pid + ".name")).equalsIgnoreCase(name)) {
                if (event.getClick().isLeftClick()) {
                    info.setActivePower(pid);
                    player.sendMessage("§aPouvoir §e" + name + " §aactivé pour ce compagnon.");
                    fr.jules.faction.gui.FactionGUI.openPetPowersMenu(player, petId, info);
                } else if (event.getClick().isRightClick()) {
                    fr.jules.faction.gui.FactionGUI.openPetSkillsMenu(player, petId, info, pid);
                }
                break;
            }
        }
    }

    private void handlePetSkillsClick(Player player, String name, String[] meta, PlayerData data) {
        String petId = meta[0]; String powerId = meta[1];
        PetInfo info = data.getCapturedPets().get(petId);
        if (info == null) return;
        if (name.equalsIgnoreCase("Retour")) { fr.jules.faction.gui.FactionGUI.openPetPowersMenu(player, petId, info); return; }

        PetInfo.PowerData pData = info.getPowers().computeIfAbsent(powerId, k -> new PetInfo.PowerData());
        fr.jules.faction.modules.pet.PetModule petModule = (fr.jules.faction.modules.pet.PetModule) plugin.getModuleManager().getModule("Pet");
        org.bukkit.configuration.ConfigurationSection skillsSec = petModule.getConfig().getConfigurationSection("powers." + powerId + ".skills");

        for (String sid : skillsSec.getKeys(false)) {
            if (skillsSec.getString(sid).equalsIgnoreCase(name)) {
                int currentLevel = pData.getSkills().getOrDefault(sid, 1);
                if (pData.getExp() >= currentLevel * 100) {
                    pData.setExp(pData.getExp() - currentLevel * 100);
                    pData.getSkills().put(sid, currentLevel + 1);
                    player.sendMessage("§aCompétence §b" + name + " §aaméliorée au niveau §e" + (currentLevel + 1) + "§a !");
                } else player.sendMessage("§cPas assez d'XP de pouvoir (" + (currentLevel * 100) + " requis).");
                fr.jules.faction.gui.FactionGUI.openPetSkillsMenu(player, petId, info, powerId);
                break;
            }
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
        double cost = powerId.contains("II") || powerId.equals("VAMPIRE") || powerId.equals("STRENGTH") ? 15000 : 5000;
        if (plugin.getEconomyManager().has(player, cost)) {
            if (data.getActivePower().equals(powerId)) { player.sendMessage("§cDéjà actif."); return; }
            plugin.getEconomyManager().withdraw(player, cost);
            data.setActivePower(powerId);
            plugin.getPowerManager().refreshPowerEffects(player);
            player.sendMessage("§a§l[Pouvoirs] §aVous avez activé: §e" + name);
            fr.jules.faction.gui.FactionGUI.openPowersMenu(player, data, plugin.getPowerManager());
        } else player.sendMessage("§cPas assez d'argent.");
    }

    private void handleAdminShopMainClick(Player player, String name) {
        if (name.contains("Blocs")) fr.jules.faction.gui.ShopGUI.openCategoryMenu(player, "Blocs");
        else if (name.contains("Combat")) fr.jules.faction.gui.ShopGUI.openCategoryMenu(player, "Combat");
        else if (name.contains("Agriculture")) fr.jules.faction.gui.ShopGUI.openCategoryMenu(player, "Agriculture");
        else if (name.contains("Spécial")) fr.jules.faction.gui.ShopGUI.openCategoryMenu(player, "Spécial");
    }

    private void handleAdminShopCategoryClick(Player player, InventoryClickEvent event, String category) {
        if (ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()).equalsIgnoreCase("Retour")) {
            fr.jules.faction.gui.ShopGUI.openShopMenu(player);
            return;
        }
        List<String> lore = event.getCurrentItem().getItemMeta().getLore();
        if (lore == null || lore.size() < 3) return;
        int amount = Integer.parseInt(ChatColor.stripColor(lore.get(0)).replace("Quantité: ", ""));
        double buyPrice = Double.parseDouble(ChatColor.stripColor(lore.get(1)).replace("Prix Achat: ", "").replace("$", ""));
        double sellPrice = Double.parseDouble(ChatColor.stripColor(lore.get(2)).replace("Prix Vente: ", "").replace("$", ""));
        Material material = event.getCurrentItem().getType();
        if (event.getClick().isLeftClick()) {
            if (plugin.getEconomyManager().has(player, buyPrice)) {
                plugin.getEconomyManager().withdraw(player, buyPrice);
                player.getInventory().addItem(new ItemStack(material, amount));
                player.sendMessage("§aAchat de " + amount + " " + material.name() + " pour " + buyPrice + "$.");
            } else player.sendMessage("§cPas assez d'argent.");
        } else if (event.getClick().isRightClick()) {
            if (player.getInventory().containsAtLeast(new ItemStack(material), amount)) {
                player.getInventory().removeItem(new ItemStack(material, amount));
                plugin.getEconomyManager().deposit(player, sellPrice);
                player.sendMessage("§aVente de " + amount + " " + material.name() + " pour " + sellPrice + "$.");
            } else player.sendMessage("§cPas assez d'items.");
        }
    }

    private void handleAuctionClick(Player player, InventoryClickEvent event) {
        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;
        List<String> lore = event.getCurrentItem().getItemMeta().getLore();
        if (lore == null) return;
        String idStr = null;
        for (String s : lore) { if (s.startsWith("§0ID:")) { idStr = s.replace("§0ID:", ""); break; } }
        if (idStr == null) return;
        UUID id = UUID.fromString(idStr);
        fr.jules.faction.model.AuctionItem ai = plugin.getAuctionManager().getItem(id);
        if (ai == null) { player.sendMessage("§cN'est plus en vente."); fr.jules.faction.gui.AuctionGUI.openAuctionMenu(player, plugin.getAuctionManager().getItems()); return; }
        if (ai.getSellerId().equals(player.getUniqueId())) { player.sendMessage("§cImpossible d'acheter votre objet."); return; }
        if (plugin.getEconomyManager().has(player, ai.getPrice())) {
            plugin.getEconomyManager().withdraw(player, ai.getPrice());
            plugin.getEconomyManager().deposit(Bukkit.getOfflinePlayer(ai.getSellerId()), ai.getPrice());
            player.getInventory().addItem(ai.getItem());
            plugin.getAuctionManager().removeItem(id);
            player.sendMessage("§aAchat réussi !");
            Player seller = Bukkit.getPlayer(ai.getSellerId());
            if (seller != null) seller.sendMessage("§aObjet vendu !");
            fr.jules.faction.gui.AuctionGUI.openAuctionMenu(player, plugin.getAuctionManager().getItems());
        } else player.sendMessage("§cPas assez d'argent.");
    }

    private void handleBoutiqueClick(Player player, InventoryClickEvent event) {
        int slot = event.getRawSlot(); double price = 0; Material mat = null;
        if (slot == 11) { price = 1000; mat = Material.NETHER_STAR; }
        else if (slot == 13) { price = 500; mat = Material.EXPERIENCE_BOTTLE; }
        else if (slot == 15) { price = 750; mat = Material.ENCHANTED_GOLDEN_APPLE; }
        if (mat == null) return;
        if (plugin.getEconomyManager().has(player, price)) {
            plugin.getEconomyManager().withdraw(player, price);
            player.getInventory().addItem(new ItemStack(mat, slot == 13 ? 64 : 1));
            player.sendMessage("§bAchat boutique réussi !");
        } else player.sendMessage("§cPas assez de Me's.");
    }

    private void handleModMainClick(Player player, String name) {
        if (name.contains("Bâton")) {
            ItemStack rod = new ItemStack(Material.BLAZE_ROD);
            org.bukkit.inventory.meta.ItemMeta meta = rod.getItemMeta();
            meta.setDisplayName("§6Bâton de Modération");
            rod.setItemMeta(meta);
            player.getInventory().addItem(rod);
            player.sendMessage("§aOutil reçu.");
        } else if (name.contains("Freeze")) fr.jules.faction.gui.ModGUI.openPlayerList(player);
        else if (name.contains("Vanish")) plugin.getVanishManager().toggleVanish(player);
        else if (name.contains("God Mode")) {
            if (player.hasMetadata("godmode")) { player.removeMetadata("godmode", plugin); player.sendMessage("§aGod Mode Off."); }
            else { player.setMetadata("godmode", new org.bukkit.metadata.FixedMetadataValue(plugin, true)); player.sendMessage("§aGod Mode On."); }
        } else if (name.contains("Vitesse Fly")) {
            float s = player.getFlySpeed(); player.setFlySpeed(s < 0.2f ? 0.2f : (s < 0.5f ? 0.5f : (s < 1.0f ? 1.0f : 0.1f)));
            player.sendMessage("§aVitesse Fly: " + player.getFlySpeed());
        } else if (name.contains("TP Aléatoire")) {
            List<Player> ts = Bukkit.getOnlinePlayers().stream().filter(p -> !p.getUniqueId().equals(player.getUniqueId())).map(p -> (Player) p).toList();
            if (ts.isEmpty()) player.sendMessage("§cAucun joueur.");
            else { Player t = ts.get(new java.util.Random().nextInt(ts.size())); player.teleport(t.getLocation()); player.sendMessage("§aTP sur " + t.getName()); }
        } else if (name.contains("Joueurs")) fr.jules.faction.gui.ModGUI.openPlayerList(player);
        else if (name.contains("Clear Chat")) { for (int i = 0; i < 100; i++) Bukkit.broadcastMessage(""); Bukkit.broadcastMessage("§c§l[Modération] §aChat vidé."); }
    }

    private void handleModPlayersClick(Player staff, String targetName) { Player t = Bukkit.getPlayer(targetName); if (t != null) fr.jules.faction.gui.ModGUI.openPlayerActions(staff, t); }

    private void handleModActionsClick(Player staff, String action, Player target) {
        if (target.getUniqueId().equals(staff.getUniqueId())) { staff.sendMessage("§cAction impossible sur vous."); return; }
        if (target.hasPermission("faction.admin") && !staff.isOp()) { staff.sendMessage("§cImpossible d'agir sur un admin."); return; }
        if (action.contains("Inventaire")) staff.openInventory(target.getInventory());
        else if (action.contains("Clear Inv")) { target.getInventory().clear(); staff.sendMessage("§aInventaire vidé."); }
        else if (action.contains("Mute")) { dataManagerSetMuted(target); staff.sendMessage("§aJoueur mute."); }
        else if (action.contains("Téléportation")) { staff.teleport(target.getLocation()); staff.sendMessage("§aTP sur " + target.getName()); }
        else if (action.contains("Freeze")) {
            if (target.hasMetadata("frozen")) { target.removeMetadata("frozen", plugin); target.sendMessage("§aLibéré !"); staff.sendMessage("§aLibéré."); }
            else { target.setMetadata("frozen", new org.bukkit.metadata.FixedMetadataValue(plugin, true)); target.sendMessage("§cGelé !"); staff.sendMessage("§cGelé."); }
        } else if (action.contains("Kick")) { target.kickPlayer("§cKické."); staff.closeInventory(); }
        else if (action.contains("Ban")) { Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(target.getName(), "§cBanni.", null, null); target.kickPlayer("§cBanni."); staff.closeInventory(); }
    }

    private void dataManagerSetMuted(Player target) { PlayerData d = plugin.getPlayerManager().getPlayerData(target.getUniqueId()); d.setMutedUntil(System.currentTimeMillis() + 3600000); target.sendMessage("§cMute 1h."); }

    private void handleSpawnerClick(Player player, String name, org.bukkit.block.Block spawner, InventoryClickEvent event) {
        if (name.contains("Statut")) {
            if (spawner.hasMetadata("inactive")) { spawner.removeMetadata("inactive", plugin); player.sendMessage("§aActivé !"); }
            else { spawner.setMetadata("inactive", new org.bukkit.metadata.FixedMetadataValue(plugin, true)); player.sendMessage("§cDésactivé !"); }
            fr.jules.faction.gui.SpawnerGUI.openSpawnerMenu(player, spawner);
        } else if (name.contains("Récupérer")) {
            ItemStack pick = player.getInventory().getItemInMainHand();
            if (pick.getType() == Material.DIAMOND_PICKAXE && pick.hasItemMeta() && pick.getItemMeta().getDisplayName().contains("Pioche à Spawner")) giveSpawner(player, spawner);
            else if (event.getClick().isRightClick() && plugin.getEconomyManager().has(player, 25000)) { plugin.getEconomyManager().withdraw(player, 25000); giveSpawner(player, spawner); }
            else player.sendMessage("§cBesoin de la pioche ou 25k$.");
        } else if (name.contains("Acheter")) {
            if (plugin.getEconomyManager().has(player, 50000)) {
                plugin.getEconomyManager().withdraw(player, 50000);
                ItemStack item = new ItemStack(Material.DIAMOND_PICKAXE); org.bukkit.inventory.meta.ItemMeta m = item.getItemMeta(); m.setDisplayName("§dPioche à Spawner"); item.setItemMeta(m);
                player.getInventory().addItem(item); player.sendMessage("§aAchetée !");
            } else player.sendMessage("§cPas assez d'argent.");
        }
    }

    private void giveSpawner(Player player, org.bukkit.block.Block block) {
        org.bukkit.block.CreatureSpawner cs = (org.bukkit.block.CreatureSpawner) block.getState(); org.bukkit.entity.EntityType type = cs.getSpawnedType();
        ItemStack item = new ItemStack(Material.SPAWNER); org.bukkit.inventory.meta.BlockStateMeta m = (org.bukkit.inventory.meta.BlockStateMeta) item.getItemMeta();
        org.bukkit.block.CreatureSpawner mc = (org.bukkit.block.CreatureSpawner) m.getBlockState(); mc.setSpawnedType(type); m.setBlockState(mc); m.setDisplayName("§eSpawner: §b" + type.name()); item.setItemMeta(m);
        block.setType(Material.AIR); player.getInventory().addItem(item); player.sendMessage("§aRécupéré !"); player.closeInventory();
    }
}
