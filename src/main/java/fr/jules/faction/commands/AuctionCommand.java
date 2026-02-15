package fr.jules.faction.commands;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.model.AuctionItem;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AuctionCommand implements CommandExecutor {
    private final FactionPlugin plugin;

    public AuctionCommand(FactionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length == 0) {
            fr.jules.faction.gui.AuctionGUI.openAuctionMenu(player, plugin.getAuctionManager().getItems());
            return true;
        }

        if (args[0].equalsIgnoreCase("sell")) {
            if (args.length < 2) {
                player.sendMessage("§cUsage: /ah sell [prix]");
                return true;
            }

            double price;
            try {
                price = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cPrix invalide.");
                return true;
            }

            if (price <= 0) {
                player.sendMessage("§cLe prix doit être positif.");
                return true;
            }

            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() == Material.AIR) {
                player.sendMessage("§cVous devez tenir un objet en main.");
                return true;
            }

            AuctionItem ai = new AuctionItem(player.getUniqueId(), player.getName(), item.clone(), price);
            plugin.getAuctionManager().addItem(ai);
            player.getInventory().setItemInMainHand(null);
            player.sendMessage("§aObjet mis en vente pour " + price + "$ !");
        }

        return true;
    }
}
