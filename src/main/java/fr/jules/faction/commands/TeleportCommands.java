package fr.jules.faction.commands;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TeleportCommands implements CommandExecutor {
    private final FactionPlugin plugin;

    public TeleportCommands(FactionPlugin plugin) {
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
            case "tpa":
                handleTpa(player, args);
                break;
            case "tpahere":
                handleTpaHere(player, args);
                break;
            case "tpyes":
                handleTpYes(player);
                break;
            case "tpno":
                handleTpNo(player);
                break;
        }
        return true;
    }

    private void handleTpa(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage("§cUtilisation: /tpa [pseudo]");
            return;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage("§cJoueur non trouvé.");
            return;
        }
        plugin.getTeleportManager().sendTpa(player.getUniqueId(), target.getUniqueId());
        MessageUtils.sendMessage(player, "tpa-sent", "%target%", target.getName());
        MessageUtils.sendMessage(target, "tpa-received", "%player%", player.getName());
    }

    private void handleTpaHere(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage("§cUtilisation: /tpahere [pseudo]");
            return;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage("§cJoueur non trouvé.");
            return;
        }
        plugin.getTeleportManager().sendTpaHere(player.getUniqueId(), target.getUniqueId());
        MessageUtils.sendMessage(player, "tpahere-sent", "%target%", target.getName());
        MessageUtils.sendMessage(target, "tpahere-received", "%player%", player.getName());
    }

    private void handleTpYes(Player player) {
        UUID senderTpa = plugin.getTeleportManager().getTpaRequest(player.getUniqueId());
        if (senderTpa != null) {
            Player sender = Bukkit.getPlayer(senderTpa);
            if (sender != null) {
                sender.teleport(player.getLocation());
                MessageUtils.sendMessage(sender, "teleport-success");
                player.sendMessage("§aVous avez accepté la demande.");
            }
            plugin.getTeleportManager().removeRequests(player.getUniqueId());
            return;
        }

        UUID senderTpaHere = plugin.getTeleportManager().getTpaHereRequest(player.getUniqueId());
        if (senderTpaHere != null) {
            Player sender = Bukkit.getPlayer(senderTpaHere);
            if (sender != null) {
                player.teleport(sender.getLocation());
                MessageUtils.sendMessage(player, "teleport-success");
                sender.sendMessage("§a" + player.getName() + " a accepté votre demande.");
            }
            plugin.getTeleportManager().removeRequests(player.getUniqueId());
            return;
        }

        MessageUtils.sendMessage(player, "no-pending-request");
    }

    private void handleTpNo(Player player) {
        plugin.getTeleportManager().removeRequests(player.getUniqueId());
        MessageUtils.sendMessage(player, "request-denied");
    }
}
