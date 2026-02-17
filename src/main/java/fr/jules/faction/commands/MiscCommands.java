package fr.jules.faction.commands;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.model.PlayerData;
import fr.jules.faction.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class MiscCommands implements CommandExecutor {
    private final FactionPlugin plugin;

    public MiscCommands(FactionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        String cmd = label.toLowerCase();
        String perm = "faction.command." + cmd;
        if (!player.hasPermission(perm)) {
            MessageUtils.sendMessage(player, "no-permission", "%perm%", perm);
            return true;
        }

        switch (cmd) {
            case "spawn":
                player.teleport(player.getWorld().getSpawnLocation());
                MessageUtils.sendMessage(player, "spawn-teleport");
                break;
            case "chateau":
                handleChateau(player);
                break;
            case "forteresse":
                handleForteresse(player);
                break;
            case "money":
                double bal = plugin.getEconomyManager().getBalance(player);
                MessageUtils.sendMessage(player, "money-status", "%amount%", String.format("%.2f", bal));
                break;
            case "power":
                handlePower(player, args);
                break;
            case "ignore":
                handleIgnore(player, args);
                break;
            case "shop":
                handleShop(player);
                break;
            case "boutique":
                handleBoutique(player);
                break;
            case "eco":
                handleEconomy(player, args);
                break;
            case "mod":
                handleMod(player);
                break;
            case "ec":
                player.openInventory(player.getEnderChest());
                player.sendMessage("§aOuverture de l'ender chest.");
                break;
            case "rank":
                handleRank(player, args);
                break;
            case "afk":
                plugin.getAfkManager().setAFK(player, !plugin.getAfkManager().isAFK(player));
                break;
        }
        return true;
    }

    private void handleRank(Player player, String[] args) {
        if (!player.hasPermission("faction.admin")) {
            player.sendMessage("§cVous n'avez pas la permission.");
            return;
        }
        if (args.length < 3) {
            player.sendMessage("§cUsage: /rank set <joueur> <rank>");
            return;
        }
        if (args[0].equalsIgnoreCase("set")) {
            PlayerData targetData = plugin.getPlayerManager().getPlayerDataByName(args[1]);
            if (targetData == null) {
                player.sendMessage("§cJoueur introuvable.");
                return;
            }
            try {
                fr.jules.faction.model.Rank rank = fr.jules.faction.model.Rank.valueOf(args[2].toUpperCase());
                targetData.setRank(rank);
                player.sendMessage("§aLe rank de " + targetData.getName() + " a été mis à " + rank.name());

                Player targetPlayer = Bukkit.getPlayer(targetData.getUuid());
                if (targetPlayer != null) {
                    targetPlayer.sendMessage("§aVotre rank a été mis à " + rank.getPrefix());
                    plugin.getTabManager().updateTab(targetPlayer);
                }
                plugin.getDataManager().savePlayerData(targetData);
            } catch (IllegalArgumentException e) {
                player.sendMessage("§cRank invalide. Ranks: JOUEUR, NOVICE, GUERRIER, ELITE, LEGENDE, HELPER, MODERATEUR, ADMINISTRATEUR");
            }
        }
    }

    private void handleShop(Player player) {
        fr.jules.faction.gui.ShopGUI.openShopMenu(player);
        MessageUtils.sendMessage(player, "shop-open");
    }

    private void handleEconomy(Player player, String[] args) {
        if (!player.hasPermission("faction.admin")) return;
        if (args.length < 3) {
            player.sendMessage("§cUsage: /eco <give|take|set> <joueur> <montant>");
            return;
        }
        String action = args[0].toLowerCase();
        org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        double amount;
        try { amount = Double.parseDouble(args[2]); } catch (Exception e) { player.sendMessage("§cMontant invalide."); return; }

        if (action.equals("give")) {
            plugin.getEconomyManager().deposit(target, amount);
            player.sendMessage("§aDonné " + amount + "$ à " + target.getName());
        } else if (action.equals("take")) {
            plugin.getEconomyManager().withdraw(Bukkit.getPlayer(target.getUniqueId()), amount); // fallback logic handles offline if needed
            player.sendMessage("§aRetiré " + amount + "$ à " + target.getName());
        } else if (action.equals("set")) {
            double current = plugin.getEconomyManager().getBalance(Bukkit.getPlayer(target.getUniqueId()));
            plugin.getEconomyManager().withdraw(Bukkit.getPlayer(target.getUniqueId()), current);
            plugin.getEconomyManager().deposit(target, amount);
            player.sendMessage("§aMis le solde de " + target.getName() + " à " + amount + "$");
        }
    }

    private void handleMod(Player player) {
        if (!player.hasPermission("faction.staff")) return;
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());

        if (!data.isStaffMode()) {
            // Activate Staff Mode
            data.setStaffMode(true);
            data.setSavedInventory(player.getInventory().getContents());
            data.setSavedArmor(player.getInventory().getArmorContents());

            player.getInventory().clear();

            player.getInventory().setItem(0, createStaffItem(org.bukkit.Material.COMPASS, "§bVanish", "§7Clic pour alterner"));
            player.getInventory().setItem(1, createStaffItem(org.bukkit.Material.PACKED_ICE, "§bFreeze", "§7Clic droit sur joueur"));
            player.getInventory().setItem(2, createStaffItem(org.bukkit.Material.CHEST, "§eInvSee", "§7Clic droit sur joueur"));
            player.getInventory().setItem(4, createStaffItem(org.bukkit.Material.BOOK, "§6Outils Modération", "§7Ouvrir le menu"));
            player.getInventory().setItem(8, createStaffItem(org.bukkit.Material.BARRIER, "§cQuitter Staff Mode", "§7Désactiver"));

            player.sendMessage("§a§l[Staff] §aMode Staff activé.");
        } else {
            // Deactivate Staff Mode
            data.setStaffMode(false);
            player.getInventory().clear();
            if (data.getSavedInventory() != null) player.getInventory().setContents(data.getSavedInventory());
            if (data.getSavedArmor() != null) player.getInventory().setArmorContents(data.getSavedArmor());

            if (player.hasMetadata("vanished")) {
                player.removeMetadata("vanished", plugin);
                Bukkit.getOnlinePlayers().forEach(p -> p.showPlayer(plugin, player));
            }

            player.sendMessage("§c§l[Staff] §cMode Staff désactivé. Inventaire restauré.");
        }
    }

    private ItemStack createStaffItem(org.bukkit.Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(java.util.Collections.singletonList(lore));
        item.setItemMeta(meta);
        return item;
    }

    private void handleBoutique(Player player) {
        Inventory boutique = Bukkit.createInventory(new fr.jules.faction.gui.FactionInventoryHolder("BOUTIQUE", null), 27, "§c§lBoutique Me's");
        boutique.setItem(11, createShopItem(org.bukkit.Material.NETHER_STAR, "§fÉtoile du Nether", "§7Prix: §e1000 Me's"));
        boutique.setItem(13, createShopItem(org.bukkit.Material.EXPERIENCE_BOTTLE, "§aBouteille d'XP x64", "§7Prix: §e500 Me's"));
        boutique.setItem(15, createShopItem(org.bukkit.Material.ENCHANTED_GOLDEN_APPLE, "§dPomme Notch", "§7Prix: §e750 Me's"));
        player.openInventory(boutique);
        MessageUtils.sendMessage(player, "boutique-open");
    }

    private ItemStack createShopItem(org.bukkit.Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(java.util.Collections.singletonList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    private void handlePower(Player player, String[] args) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        MessageUtils.sendMessage(player, "power-status",
                "%current%", String.format("%.1f", data.getPower()),
                "%max%", String.format("%.1f", data.getMaxPower()));
    }

    private void handleIgnore(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage("§cUtilisation: /ignore [pseudo]");
            return;
        }
        UUID targetUUID = Bukkit.getOfflinePlayer(args[0]).getUniqueId();
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getIgnoredPlayers().contains(targetUUID)) {
            data.getIgnoredPlayers().remove(targetUUID);
            player.sendMessage("§aVous n'ignorez plus " + args[0] + ".");
        } else {
            data.getIgnoredPlayers().add(targetUUID);
            player.sendMessage("§aVous ignorez désormais " + args[0] + ".");
        }
    }

    private void handleChateau(Player player) {
        org.bukkit.Location loc = plugin.getChateauLocation();
        if (loc == null) {
            player.sendMessage("§cLa localisation du château n'est pas définie.");
            return;
        }
        if (canTeleportToObjective(player, loc)) {
            player.teleport(loc);
            player.sendMessage("§aTéléportation au château !");
        } else {
            player.sendMessage("§cVotre faction ne possède pas le château actuellement (contrôlez le territoire du château).");
        }
    }

    private void handleForteresse(Player player) {
        org.bukkit.Location loc = plugin.getForteresseLocation();
        if (loc == null) {
            player.sendMessage("§cLa localisation de la forteresse n'est pas définie.");
            return;
        }
        if (canTeleportToObjective(player, loc)) {
            player.teleport(loc);
            player.sendMessage("§aTéléportation à la forteresse !");
        } else {
            player.sendMessage("§cVotre faction ne possède pas la forteresse actuellement (contrôlez le territoire de la forteresse).");
        }
    }

    private boolean canTeleportToObjective(Player player, org.bukkit.Location objectiveLoc) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) return false;

        fr.jules.faction.model.Claim claim = plugin.getClaimManager().getClaim(objectiveLoc.getWorld().getName(), objectiveLoc.getChunk().getX(), objectiveLoc.getChunk().getZ());
        if (claim == null) return false;

        return claim.getFactionId().equals(data.getFactionId());
    }
}
