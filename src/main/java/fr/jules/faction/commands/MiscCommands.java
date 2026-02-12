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

        switch (label.toLowerCase()) {
            case "spawn":
                player.teleport(player.getWorld().getSpawnLocation());
                MessageUtils.sendMessage(player, "spawn-teleport");
                break;
            case "chateau":
                player.sendMessage("§cVous ne possédez pas le château.");
                break;
            case "forteresse":
                player.sendMessage("§cVous ne possédez pas la forteresse.");
                break;
            case "money":
                MessageUtils.sendMessage(player, "money-status", "%amount%", "1000");
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
        }
        return true;
    }

    private void handleShop(Player player) {
        Inventory shop = Bukkit.createInventory(null, 27, "§6Boutique Faction");
        shop.setItem(11, new ItemStack(org.bukkit.Material.DIAMOND_SWORD));
        shop.setItem(13, new ItemStack(org.bukkit.Material.GOLDEN_APPLE));
        shop.setItem(15, new ItemStack(org.bukkit.Material.OBSIDIAN, 16));
        player.openInventory(shop);
        MessageUtils.sendMessage(player, "shop-open");
    }

    private void handleBoutique(Player player) {
        Inventory boutique = Bukkit.createInventory(null, 27, "§6Boutique Me's");
        boutique.setItem(11, new ItemStack(org.bukkit.Material.NETHER_STAR));
        boutique.setItem(13, new ItemStack(org.bukkit.Material.EXPERIENCE_BOTTLE, 64));
        boutique.setItem(15, new ItemStack(org.bukkit.Material.ENCHANTED_GOLDEN_APPLE));
        player.openInventory(boutique);
        MessageUtils.sendMessage(player, "boutique-open");
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
}
