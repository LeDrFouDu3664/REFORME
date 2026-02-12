package fr.jules.faction.commands;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.model.PlayerData;
import fr.jules.faction.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HomeCommands implements CommandExecutor {
    private final FactionPlugin plugin;

    public HomeCommands(FactionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (label.equalsIgnoreCase("sethome")) {
            handleSetHome(player, args);
        } else if (label.equalsIgnoreCase("home")) {
            handleHome(player, args);
        }
        return true;
    }

    private void handleSetHome(Player player, String[] args) {
        String name = args.length > 0 ? args[0] : "home";
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        data.getHomes().put(name, player.getLocation());
        MessageUtils.sendMessage(player, "home-set", "%name%", name);
    }

    private void handleHome(Player player, String[] args) {
        String name = args.length > 0 ? args[0] : "home";
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (!data.getHomes().containsKey(name)) {
            player.sendMessage("§cRésidence inconnue.");
            return;
        }
        player.teleport(data.getHomes().get(name));
        MessageUtils.sendMessage(player, "home-teleport", "%name%", name);
    }
}
