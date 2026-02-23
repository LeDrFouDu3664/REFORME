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
        int slot = event.getRawSlot();

        String actionId = null;
        if (event.getCurrentItem().hasItemMeta()) {
            actionId = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(
                new org.bukkit.NamespacedKey(plugin, "gui_action"),
                org.bukkit.persistence.PersistentDataType.STRING
            );
        }

        switch (type) {
            case "MAIN":
                if (faction != null) handleMainMenuClick(player, slot, faction, actionId);
                break;
            case "MEMBERS":
                handleMembersMenuClick(player, name, faction, event);
                break;
            case "PARAMETERS":
                handleParametersMenuClick(player, slot, faction);
                break;
            case "PERMISSIONS":
                handlePermissionsSelectorClick(player, slot, faction);
                break;
            case "RANK_PERMS":
                handleRankPermissionsClick(player, slot, faction, (String) holder.getData());
                break;
            case "RELATIONS":
                if (faction != null) handleRelationsMenuClick(player, slot, faction);
                break;
            case "FACTIONS_LIST":
                handleFactionsListMenuClick(player, name, faction, event);
                break;
            case "CLAIMS":
                if (faction != null) handleTerritoriesMenuClick(player, slot, faction);
                break;
            case "BANK":
                if (faction != null) handleBankMenuClick(player, slot, faction);
                break;
            case "LEVELS":
                if (faction != null) handleFactionLevelMenuClick(player, slot, faction);
                break;
            case "JOBS":
                handleJobsMenuClick(player, slot, data, faction);
                break;
            case "QUESTS":
                handleQuestsMenuClick(player, slot, faction);
                break;
            case "POWERS":
                handlePowersMenuClick(player, slot, data, faction);
                break;
            case "PETS":
                handlePetMenuClick(player, name, data, faction, event);
                break;
            case "PET_DETAIL":
                handlePetDetailClick(player, slot, (String) holder.getData(), data, faction);
                break;
            case "PET_EQUIP":
                handlePetEquipmentClick(player, slot, (String) holder.getData(), data);
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
            case "RANK_MENU":
                handleRankMenuClick(player, name);
                break;
        }
    }

    private void handleMainMenuClick(Player player, int slot, Faction faction, String actionId) {
        if (actionId == null) {
            if (slot == 10) { player.performCommand("f faction"); player.closeInventory(); }
            return;
        }

        switch (actionId) {
            case "MEMBERS": fr.jules.faction.gui.FactionGUI.openMembersMenu(player, faction); break;
            case "CLAIMS": fr.jules.faction.gui.FactionGUI.openClaimsMenu(player, faction); break;
            case "BANK": fr.jules.faction.gui.FactionGUI.openBankMenu(player, faction); break;
            case "RELATIONS": fr.jules.faction.gui.FactionGUI.openRelationsMenu(player, faction); break;
            case "PARAMETERS": fr.jules.faction.gui.FactionGUI.openParametersMenu(player, faction); break;
            case "PERMISSIONS": fr.jules.faction.gui.FactionGUI.openPermissionsMenu(player, faction); break;
            case "LEVELS": fr.jules.faction.gui.FactionGUI.openFactionLevelMenu(player, faction); break;
            case "JOBS": fr.jules.faction.gui.FactionGUI.openJobsMenu(player, plugin.getPlayerManager().getPlayerData(player.getUniqueId())); break;
            case "POWERS": fr.jules.faction.gui.FactionGUI.openPowersMenu(player, plugin.getPlayerManager().getPlayerData(player.getUniqueId()), plugin.getPowerManager()); break;
            case "QUESTS": fr.jules.faction.gui.FactionGUI.openQuestsMenu(player, plugin.getPlayerManager().getPlayerData(player.getUniqueId()), plugin.getQuestManager()); break;
            case "PETS": fr.jules.faction.gui.FactionGUI.openPetMenu(player, plugin.getPlayerManager().getPlayerData(player.getUniqueId())); break;
        }
    }

    private void handleMembersMenuClick(Player player, String targetName, Faction faction, InventoryClickEvent event) {
        if (targetName.contains("Retour")) { fr.jules.faction.gui.FactionGUI.openMainMenu(player, faction); return; }
        if (faction == null || !faction.getLeader().equals(player.getUniqueId())) { player.sendMessage("§cSeul le chef fait ça."); return; }
        if (event.getClick().isShiftClick()) player.performCommand("f kick " + targetName);
        else if (event.getClick().isLeftClick()) player.performCommand("f promote " + targetName);
        else if (event.getClick().isRightClick()) player.performCommand("f demote " + targetName);
        fr.jules.faction.gui.FactionGUI.openMembersMenu(player, faction);
    }

    private void handleParametersMenuClick(Player player, int slot, Faction faction) {
        if (slot == 22) { fr.jules.faction.gui.FactionGUI.openMainMenu(player, faction); return; }
        if (faction == null || !faction.isOfficer(player.getUniqueId())) return;

        String key = null;
        if (slot == 10) key = "ALLY_HOME";
        else if (slot == 11) key = "OPEN_INVITES";
        else if (slot == 12) key = "friendlyFire";
        else if (slot == 13) key = "AUTO_PLANT";
        else if (slot == 14) key = "MOB_GRIEFING";
        else if (slot == 15) { player.sendMessage("§e/f desc [texte]"); player.closeInventory(); return; }
        else if (slot == 16) { player.sendMessage("§e/f motd [texte]"); player.closeInventory(); return; }

        if (key != null) {
            boolean cur = faction.getFactionFlags().getOrDefault(key, false);
            faction.getFactionFlags().put(key, !cur);
            player.sendMessage("§aOption " + key + " -> " + (!cur));
            fr.jules.faction.gui.FactionGUI.openParametersMenu(player, faction);
        }
    }

    private void handlePermissionsSelectorClick(Player player, int slot, Faction faction) {
        if (slot == 22) { fr.jules.faction.gui.FactionGUI.openMainMenu(player, faction); return; }
        String grade = null;
        if (slot == 10) grade = "RECRUIT";
        else if (slot == 11) grade = "MEMBER";
        else if (slot == 12) grade = "MODERATOR";
        else if (slot == 13) grade = "OFFICER";
        else if (slot == 16) grade = "ALLY";

        if (grade != null && faction != null) fr.jules.faction.gui.FactionGUI.openRankPermissionsMenu(player, faction, grade);
    }

    private void handleRankPermissionsClick(Player player, int slot, Faction faction, String target) {
        if (slot == 40) { fr.jules.faction.gui.FactionGUI.openPermissionsMenu(player, faction); return; }
        if (faction == null) return;
        String[] actions = {"CLAIM", "UNCLAIM", "SETHOME", "UNSETHOME", "PROMOTE", "DEMOTE", "KICK", "INVITE", "DESC", "MOTD", "RENAME", "TITLE", "BUILD", "DESTROY", "USE"};

        // Mapping slot to action is a bit complex due to fillBorder logic.
        // Let's use name-based lookup for permissions as they are strictly internal keys.
        ItemStack item = player.getOpenInventory().getItem(slot);
        if (item == null || !item.hasItemMeta()) return;
        String action = ChatColor.stripColor(item.getItemMeta().getDisplayName());

        if (target.equals("ALLY")) {
            String k = "ALLY_" + action;
            boolean c = faction.getFactionFlags().getOrDefault(k, false);
            faction.getFactionFlags().put(k, !c);
        } else {
            fr.jules.faction.model.Grade g = fr.jules.faction.model.Grade.valueOf(target);
            java.util.Set<fr.jules.faction.model.Grade> allowed = faction.getPermissions().computeIfAbsent(action, k -> new java.util.HashSet<>());
            if (allowed.contains(g)) allowed.remove(g); else allowed.add(g);
        }
        fr.jules.faction.gui.FactionGUI.openRankPermissionsMenu(player, faction, target);
    }

    private void handleRelationsMenuClick(Player player, int slot, Faction faction) {
        if (slot == 22) { fr.jules.faction.gui.FactionGUI.openMainMenu(player, faction); return; }
        if (faction == null) return;
        if (slot == 10) player.sendMessage("§dVos alliés: " + faction.getRelations().entrySet().stream().filter(e -> e.getValue().equals("ALLY")).count());
        else if (slot == 12) player.sendMessage("§6Vos trêves: " + faction.getRelations().entrySet().stream().filter(e -> e.getValue().equals("TRUCE")).count());
        else if (slot == 14) player.sendMessage("§cVos ennemis: " + faction.getRelations().entrySet().stream().filter(e -> e.getValue().equals("ENEMY")).count());
        else if (slot == 16) fr.jules.faction.gui.FactionGUI.openFactionsListMenu(player, faction, plugin.getFactionManager().getAllFactions());
    }

    private void handleFactionsListMenuClick(Player player, String targetName, Faction faction, InventoryClickEvent event) {
        if (targetName.contains("Retour")) { fr.jules.faction.gui.FactionGUI.openRelationsMenu(player, faction); return; }
        if (faction == null) return;
        String rel = null;
        if (event.getClick().isLeftClick()) rel = event.getClick().isShiftClick() ? "truce" : "ally";
        else if (event.getClick().isRightClick()) rel = event.getClick().isShiftClick() ? "neutral" : "enemy";
        if (rel != null) { player.performCommand("f " + rel + " " + targetName); fr.jules.faction.gui.FactionGUI.openFactionsListMenu(player, faction, plugin.getFactionManager().getAllFactions()); }
    }

    private void handleTerritoriesMenuClick(Player player, int slot, Faction faction) {
        if (slot == 22) { fr.jules.faction.gui.FactionGUI.openMainMenu(player, faction); return; }
        if (slot == 10) player.performCommand("f claim");
        else if (slot == 11) player.performCommand("f unclaim");
        else if (slot == 13) player.performCommand("f map");
        else if (slot == 15) player.performCommand("f claim auto");
        else if (slot == 16) player.performCommand("f unclaim all");
        player.closeInventory();
    }

    private void handleBankMenuClick(Player player, int slot, Faction faction) {
        if (slot == 22) { fr.jules.faction.gui.FactionGUI.openMainMenu(player, faction); return; }
        if (slot == 11) player.performCommand("f money deposit 100");
        else if (slot == 12) player.performCommand("f money deposit 1000");
        else if (slot == 14) player.performCommand("f money withdraw 100");
        else if (slot == 15) player.performCommand("f money withdraw 1000");
        fr.jules.faction.gui.FactionGUI.openBankMenu(player, faction);
    }

    private void handleFactionLevelMenuClick(Player player, int slot, Faction faction) {
        if (slot == 22) fr.jules.faction.gui.FactionGUI.openMainMenu(player, faction);
    }

    private void handleJobsMenuClick(Player player, int slot, PlayerData data, Faction faction) {
        if (slot == 22) { if (faction != null) fr.jules.faction.gui.FactionGUI.openMainMenu(player, faction); else player.closeInventory(); return; }
        if (slot == 10) data.setJob("MINEUR");
        else if (slot == 12) data.setJob("BUCHERON");
        else if (slot == 14) data.setJob("FERMIER");
        else if (slot == 16) data.setJob("GUERRIER");
        else return;
        player.sendMessage("§aMétier choisi: " + data.getJob());
        fr.jules.faction.gui.FactionGUI.openJobsMenu(player, data);
    }

    private void handleQuestsMenuClick(Player player, int slot, Faction faction) {
        if (slot == 22) { if (faction != null) fr.jules.faction.gui.FactionGUI.openMainMenu(player, faction); else player.closeInventory(); }
    }

    private void handlePowersMenuClick(Player player, int slot, PlayerData data, Faction faction) {
        if (slot == 49) { if (faction != null) fr.jules.faction.gui.FactionGUI.openMainMenu(player, faction); else player.closeInventory(); return; }
        // Powers are dynamic, use name lookup
        ItemStack item = player.getOpenInventory().getItem(slot);
        if (item == null || !item.hasItemMeta()) return;
        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());

        String pid = null;
        for (Map.Entry<String, fr.jules.faction.manager.PowerManager.PowerInfo> e : plugin.getPowerManager().getPowers().entrySet()) {
            if (e.getValue().name.equalsIgnoreCase(name)) { pid = e.getKey(); break; }
        }
        if (pid == null) return;
        double cost = pid.contains("II") || pid.equals("VAMPIRE") || pid.equals("STRENGTH") ? 15000 : 5000;
        if (plugin.getEconomyManager().has(player, cost)) {
            if (data.getActivePower().equals(pid)) return;
            plugin.getEconomyManager().withdraw(player, cost);
            data.setActivePower(pid);
            plugin.getPowerManager().refreshPowerEffects(player);
            fr.jules.faction.gui.FactionGUI.openPowersMenu(player, data, plugin.getPowerManager());
        }
    }

    private void handlePetMenuClick(Player player, String name, PlayerData data, Faction faction, InventoryClickEvent event) {
        if (name.contains("Suivante")) {
            int p = (int) ((fr.jules.faction.gui.FactionInventoryHolder) event.getInventory().getHolder()).getData();
            fr.jules.faction.gui.FactionGUI.openPetMenu(player, data, p + 1);
            return;
        }
        if (name.contains("Précédente")) {
            int p = (int) ((fr.jules.faction.gui.FactionInventoryHolder) event.getInventory().getHolder()).getData();
            fr.jules.faction.gui.FactionGUI.openPetMenu(player, data, p - 1);
            return;
        }
        if (name.contains("Retour") || name.contains("Quitter")) {
            if (faction != null) fr.jules.faction.gui.FactionGUI.openMainMenu(player, faction);
            else player.closeInventory();
            return;
        }
        if (name.contains("Renvoyer")) {
            plugin.getPetManager().despawnPet(player);
            fr.jules.faction.gui.FactionGUI.openPetMenu(player, data);
            return;
        }

        String match = null;
        for (String id : data.getCapturedPets().keySet()) {
            PetInfo pi = data.getCapturedPets().get(id);
            if (ChatColor.stripColor(pi.getCustomName() != null ? pi.getCustomName() : id).equalsIgnoreCase(name)) { match = id; break; }
        }
        if (match != null) {
            if (event.getClick().isShiftClick() && event.getClick().isRightClick()) { data.getCapturedPets().remove(match); player.sendMessage("§cSupprimé."); fr.jules.faction.gui.FactionGUI.openPetMenu(player, data); }
            else fr.jules.faction.gui.FactionGUI.openPetDetailMenu(player, match, data.getCapturedPets().get(match));
        }
    }

    private void handlePetDetailClick(Player player, int slot, String petId, PlayerData data, Faction faction) {
        PetInfo info = data.getCapturedPets().get(petId);
        if (info == null) return;
        switch (slot) {
            case 22: fr.jules.faction.gui.FactionGUI.openPetMenu(player, data); break;
            case 13: plugin.getPetManager().spawnPet(player, petId); player.closeInventory(); break;
            case 12: if (plugin.getEconomyManager().has(player, 5000)) { player.sendMessage("§eEntrez le nom:"); player.setMetadata("renaming_pet", new org.bukkit.metadata.FixedMetadataValue(plugin, petId)); player.closeInventory(); } break;
            case 14: fr.jules.faction.gui.FactionGUI.openPetEquipmentMenu(player, petId, info); break;
            case 15: fr.jules.faction.gui.FactionGUI.openPetPowersMenu(player, petId, info); break;
            case 16: plugin.getPetManager().despawnPet(player); break;
        }
    }

    private void handlePetEquipmentClick(Player player, int slot, String petId, PlayerData data) {
        PetInfo info = data.getCapturedPets().get(petId);
        if (info == null) return;
        if (slot == 22) { fr.jules.faction.gui.FactionGUI.openPetDetailMenu(player, petId, info); return; }
        if (slot == 10) { // SADDLE
            if (info.getSaddle() != null) {
                info.setSaddle(null);
                ItemStack item = new ItemStack(Material.SADDLE);
                if (player.getInventory().firstEmpty() == -1) player.getWorld().dropItem(player.getLocation(), item);
                else player.getInventory().addItem(item);
                player.sendMessage("§aSelle retirée.");
            } else {
                int firstSaddle = player.getInventory().first(Material.SADDLE);
                if (firstSaddle != -1) {
                    ItemStack saddle = player.getInventory().getItem(firstSaddle);
                    saddle.setAmount(saddle.getAmount() - 1);
                    info.setSaddle("SADDLE");
                    player.sendMessage("§aSelle équipée !");
                } else {
                    player.sendMessage("§cVous n'avez pas de selle dans votre inventaire !");
                }
            }
            fr.jules.faction.gui.FactionGUI.openPetEquipmentMenu(player, petId, info);
        } else if (slot == 12) { // ARMOR
            if (info.getChestplate() != null) {
                Material m = Material.valueOf(info.getChestplate());
                info.setChestplate(null);
                ItemStack item = new ItemStack(m);
                if (player.getInventory().firstEmpty() == -1) player.getWorld().dropItem(player.getLocation(), item);
                else player.getInventory().addItem(item);
                player.sendMessage("§aArmure retirée.");
            } else {
                ItemStack itemInHand = player.getInventory().getItemInMainHand();
                if (itemInHand != null && (itemInHand.getType().name().contains("HORSE_ARMOR") || itemInHand.getType() == Material.WOLF_ARMOR)) {
                    info.setChestplate(itemInHand.getType().name());
                    itemInHand.setAmount(itemInHand.getAmount() - 1);
                    player.sendMessage("§aArmure équipée !");
                } else {
                    player.sendMessage("§cTenez l'armure (Cheval ou Loup) dans votre main principale !");
                }
            }
            fr.jules.faction.gui.FactionGUI.openPetEquipmentMenu(player, petId, info);
        }
    }

    private void handlePetPowersClick(Player player, InventoryClickEvent event, String name, String petId, PlayerData data) {
        PetInfo info = data.getCapturedPets().get(petId);
        if (info == null) return;
        if (event.getRawSlot() == 26) { fr.jules.faction.gui.FactionGUI.openPetDetailMenu(player, petId, info); return; }

        String powerId = null;
        double cost = 0;
        if (name.contains("Mineur")) { powerId = "MINER"; cost = 5000; }
        else if (name.contains("Tank")) { powerId = "TANK"; cost = 8000; }
        else if (name.contains("Combattant")) { powerId = "FIGHTER"; cost = 10000; }
        else if (name.contains("Éclaireur")) { powerId = "SCOUT"; cost = 6000; }

        if (powerId != null) {
            if (info.getUnlockedPowers().contains(powerId)) {
                info.setActivePower(powerId);
                player.sendMessage("§aPouvoir §e" + name + " §aactivé !");
            } else {
                if (plugin.getEconomyManager().has(player, cost)) {
                    plugin.getEconomyManager().withdraw(player, cost);
                    info.getUnlockedPowers().add(powerId);
                    info.setActivePower(powerId);
                    player.sendMessage("§aVous avez débloqué le pouvoir §e" + name + " §apour §e" + cost + "$ §a!");
                } else {
                    player.sendMessage("§cVous n'avez pas assez d'argent !");
                }
            }
            fr.jules.faction.gui.PetPowersGUI.openPowersMenu(player, plugin, petId);
        }
    }

    private void handlePetSkillsClick(Player player, String name, String[] meta, PlayerData data) {
        String petId = meta[0]; String powerId = meta[1];
        PetInfo info = data.getCapturedPets().get(petId);
        if (info == null) return;
        // Back button check
        ItemStack item = player.getOpenInventory().getItem(22);
        if (item != null && item.hasItemMeta() && ChatColor.stripColor(item.getItemMeta().getDisplayName()).equalsIgnoreCase(name)) { fr.jules.faction.gui.FactionGUI.openPetPowersMenu(player, petId, info); return; }

        PetInfo.PowerData pd = info.getPowers().computeIfAbsent(powerId, k -> new PetInfo.PowerData());
        fr.jules.faction.modules.pet.PetModule petModule = (fr.jules.faction.modules.pet.PetModule) plugin.getModuleManager().getModule("Pet");
        org.bukkit.configuration.ConfigurationSection ss = petModule.getConfig().getConfigurationSection("powers." + powerId + ".skills");
        for (String sid : ss.getKeys(false)) {
            if (ss.getString(sid).equalsIgnoreCase(name)) {
                int lvl = pd.getSkills().getOrDefault(sid, 1);
                if (pd.getExp() >= lvl * 100) { pd.setExp(pd.getExp() - lvl * 100); pd.getSkills().put(sid, lvl + 1); player.sendMessage("§aAmélioré !"); }
                fr.jules.faction.gui.FactionGUI.openPetSkillsMenu(player, petId, info, powerId);
                break;
            }
        }
    }

    private void handleAdminShopMainClick(Player player, String name) {
        if (name.contains("Blocs")) fr.jules.faction.gui.ShopGUI.openCategoryMenu(player, "Blocs");
        else if (name.contains("Combat")) fr.jules.faction.gui.ShopGUI.openCategoryMenu(player, "Combat");
        else if (name.contains("Agriculture")) fr.jules.faction.gui.ShopGUI.openCategoryMenu(player, "Agriculture");
        else if (name.contains("Spécial")) fr.jules.faction.gui.ShopGUI.openCategoryMenu(player, "Spécial");
    }

    private void handleAdminShopCategoryClick(Player player, InventoryClickEvent event, String category) {
        if (ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()).contains("Retour")) { fr.jules.faction.gui.ShopGUI.openShopMenu(player); return; }
        List<String> lore = event.getCurrentItem().getItemMeta().getLore();
        if (lore == null || lore.size() < 3) return;
        int q = Integer.parseInt(ChatColor.stripColor(lore.get(0)).replace("Quantité: ", ""));
        double buy = Double.parseDouble(ChatColor.stripColor(lore.get(1)).replace("Prix Achat: ", "").replace("$", ""));
        double sell = Double.parseDouble(ChatColor.stripColor(lore.get(2)).replace("Prix Vente: ", "").replace("$", ""));
        Material m = event.getCurrentItem().getType();
        if (event.getClick().isLeftClick()) { if (plugin.getEconomyManager().has(player, buy)) { plugin.getEconomyManager().withdraw(player, buy); player.getInventory().addItem(new ItemStack(m, q)); } }
        else if (event.getClick().isRightClick()) { if (player.getInventory().containsAtLeast(new ItemStack(m), q)) { player.getInventory().removeItem(new ItemStack(m, q)); plugin.getEconomyManager().deposit(player, sell); } }
    }

    private void handleAuctionClick(Player player, InventoryClickEvent event) {
        List<String> lore = event.getCurrentItem().getItemMeta().getLore();
        if (lore == null) return;
        String idStr = null;
        for (String s : lore) if (s.startsWith("§0ID:")) { idStr = s.replace("§0ID:", ""); break; }
        if (idStr == null) return;
        UUID id = UUID.fromString(idStr);
        fr.jules.faction.model.AuctionItem ai = plugin.getAuctionManager().getItem(id);
        if (ai != null && !ai.getSellerId().equals(player.getUniqueId()) && plugin.getEconomyManager().has(player, ai.getPrice())) {
            plugin.getEconomyManager().withdraw(player, ai.getPrice());
            plugin.getEconomyManager().deposit(Bukkit.getOfflinePlayer(ai.getSellerId()), ai.getPrice());
            player.getInventory().addItem(ai.getItem());
            plugin.getAuctionManager().removeItem(id);
            fr.jules.faction.gui.AuctionGUI.openAuctionMenu(player, plugin.getAuctionManager().getItems());
        }
    }

    private void handleBoutiqueClick(Player player, InventoryClickEvent event) {
        int slot = event.getRawSlot(); double p = 0; Material m = null;
        if (slot == 11) { p = 1000; m = Material.NETHER_STAR; }
        else if (slot == 13) { p = 500; m = Material.EXPERIENCE_BOTTLE; }
        else if (slot == 15) { p = 750; m = Material.ENCHANTED_GOLDEN_APPLE; }
        if (m != null && plugin.getEconomyManager().has(player, p)) { plugin.getEconomyManager().withdraw(player, p); player.getInventory().addItem(new ItemStack(m, slot == 13 ? 64 : 1)); }
    }

    private void handleModMainClick(Player staff, String name) {
        if (name.contains("Bâton")) { ItemStack rod = new ItemStack(Material.BLAZE_ROD); org.bukkit.inventory.meta.ItemMeta m = rod.getItemMeta(); m.setDisplayName("§6Bâton de Modération"); rod.setItemMeta(m); staff.getInventory().addItem(rod); }
        else if (name.contains("Freeze")) fr.jules.faction.gui.ModGUI.openPlayerList(staff);
        else if (name.contains("Vanish")) plugin.getVanishManager().toggleVanish(staff);
        else if (name.contains("God Mode")) { if (staff.hasMetadata("godmode")) staff.removeMetadata("godmode", plugin); else staff.setMetadata("godmode", new org.bukkit.metadata.FixedMetadataValue(plugin, true)); }
        else if (name.contains("Fly")) { float s = staff.getFlySpeed(); staff.setFlySpeed(s < 0.2f ? 0.2f : (s < 0.5f ? 0.5f : (s < 1.0f ? 1.0f : 0.1f))); }
        else if (name.contains("Joueurs")) fr.jules.faction.gui.ModGUI.openPlayerList(staff);
    }

    private void handleModPlayersClick(Player staff, String target) { Player t = Bukkit.getPlayer(target); if (t != null) fr.jules.faction.gui.ModGUI.openPlayerActions(staff, t); }

    private void handleModActionsClick(Player staff, String action, Player target) {
        if (target.getUniqueId().equals(staff.getUniqueId())) return;
        if (action.contains("Inventaire")) staff.openInventory(target.getInventory());
        else if (action.contains("Mute")) { dataManagerSetMuted(target); }
        else if (action.contains("Freeze")) { if (target.hasMetadata("frozen")) target.removeMetadata("frozen", plugin); else target.setMetadata("frozen", new org.bukkit.metadata.FixedMetadataValue(plugin, true)); }
        else if (action.contains("Kick")) { target.kickPlayer("§cKické."); staff.closeInventory(); }
        else if (action.contains("Ban")) { Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(target.getName(), "§cBanni.", null, null); target.kickPlayer("§cBanni."); staff.closeInventory(); }
    }

    private void dataManagerSetMuted(Player target) { PlayerData d = plugin.getPlayerManager().getPlayerData(target.getUniqueId()); d.setMutedUntil(System.currentTimeMillis() + 3600000); }

    private void handleSpawnerClick(Player p, String name, org.bukkit.block.Block s, InventoryClickEvent e) {
        if (name.contains("Statut")) { if (s.hasMetadata("inactive")) s.removeMetadata("inactive", plugin); else s.setMetadata("inactive", new org.bukkit.metadata.FixedMetadataValue(plugin, true)); fr.jules.faction.gui.SpawnerGUI.openSpawnerMenu(p, s); }
        else if (name.contains("Récupérer")) { ItemStack pick = p.getInventory().getItemInMainHand(); if (pick.getType() == Material.DIAMOND_PICKAXE && pick.hasItemMeta() && pick.getItemMeta().getDisplayName().contains("Pioche")) giveSpawner(p, s); }
    }

    private void handleRankMenuClick(Player player, String name) {
        String rankStr = null;
        if (name.contains("Joueur")) rankStr = "JOUEUR";
        else if (name.contains("Novice")) rankStr = "NOVICE";
        else if (name.contains("Guerrier")) rankStr = "GUERRIER";
        else if (name.contains("Élite")) rankStr = "ELITE";
        else if (name.contains("Légende")) rankStr = "LEGENDE";
        else if (name.contains("Helper")) rankStr = "HELPER";
        else if (name.contains("Modérateur")) rankStr = "MODERATEUR";
        else if (name.contains("Administrateur")) rankStr = "ADMINISTRATEUR";

        if (rankStr != null) {
            player.performCommand("rank set " + player.getName() + " " + rankStr);
            player.closeInventory();
        }
    }

    private void giveSpawner(Player p, org.bukkit.block.Block b) {
        org.bukkit.block.CreatureSpawner cs = (org.bukkit.block.CreatureSpawner) b.getState(); org.bukkit.entity.EntityType t = cs.getSpawnedType();
        ItemStack i = new ItemStack(Material.SPAWNER); org.bukkit.inventory.meta.BlockStateMeta m = (org.bukkit.inventory.meta.BlockStateMeta) i.getItemMeta();
        org.bukkit.block.CreatureSpawner mc = (org.bukkit.block.CreatureSpawner) m.getBlockState(); mc.setSpawnedType(t); m.setBlockState(mc); m.setDisplayName("§eSpawner: §b" + t.name()); i.setItemMeta(m);
        b.setType(Material.AIR); p.getInventory().addItem(i); p.closeInventory();
    }
}
