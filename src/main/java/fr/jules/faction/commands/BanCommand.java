package fr.jules.faction.commands;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.model.BanData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BanCommand implements CommandExecutor {
    private final FactionPlugin plugin;

    public BanCommand(FactionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("faction.admin")) {
            sender.sendMessage("§cVous n'avez pas la permission.");
            return true;
        }

        if (label.equalsIgnoreCase("tempban")) {
            if (args.length < 3) {
                sender.sendMessage("§cUsage: /tempban <joueur> <durée (ex: 1d, 2h)> <raison>");
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            long duration = parseDuration(args[1]);
            if (duration == -1) {
                sender.sendMessage("§cDurée invalide. Utilisez 1d, 1h, 1m.");
                return true;
            }
            StringBuilder reason = new StringBuilder();
            for (int i = 2; i < args.length; i++) reason.append(args[i]).append(" ");

            executeBan(target, sender.getName(), reason.toString().trim(), System.currentTimeMillis() + duration, false);
            sender.sendMessage("§aJoueur banni temporairement.");
            return true;
        }

        if (label.equalsIgnoreCase("banip")) {
            if (args.length < 2) {
                sender.sendMessage("§cUsage: /banip <joueur> <raison>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("§cLe joueur doit être en ligne pour bannir son IP.");
                return true;
            }
            StringBuilder reason = new StringBuilder();
            for (int i = 1; i < args.length; i++) reason.append(args[i]).append(" ");

            executeBan(target, sender.getName(), reason.toString().trim(), -1, true);
            sender.sendMessage("§aIP du joueur bannie définitivement.");
            return true;
        }

        if (label.equalsIgnoreCase("unban")) {
            if (args.length < 1) {
                sender.sendMessage("§cUsage: /unban <joueur|IP|all>");
                return true;
            }
            if (args[0].equalsIgnoreCase("all")) {
                plugin.getBanManager().unbanAll(false);
                sender.sendMessage("§aTous les bannis ont été graciés (sauf pour triche).");
            } else if (args[0].matches("^\\d{1,3}(\\.\\d{1,3}){3}$")) {
                plugin.getBanManager().unbanIP(args[0]);
                sender.sendMessage("§aIP débannie.");
            } else {
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
                plugin.getBanManager().unban(target.getUniqueId());
                sender.sendMessage("§aJoueur débanni.");
            }
            return true;
        }

        return false;
    }

    private void executeBan(OfflinePlayer target, String admin, String reason, long expiry, boolean isIP) {
        String banId = "#" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String ip = null;
        if (target.isOnline()) {
            ip = target.getPlayer().getAddress().getAddress().getHostAddress();
        }

        BanData data = new BanData(banId, target.getName(), target.getUniqueId(), isIP ? ip : null, reason, admin, expiry, System.currentTimeMillis());
        plugin.getBanManager().ban(data);

        if (target.isOnline()) {
            target.getPlayer().kickPlayer(plugin.getBanManager().getBan(target.getUniqueId()).getReason()); // Placeholder
            // Re-kick with proper message
            target.getPlayer().kickPlayer(formatBanMessage(data));
        }
    }

    public static String formatBanMessage(BanData data) {
        String remaining = data.getExpiryTime() == -1 ? "DÉFINITIF" : formatTime(data.getExpiryTime() - System.currentTimeMillis());
        return "§c§lVOUS AVEZ ÉTÉ BANNI !\n\n" +
               "§7Raison: §f" + data.getReason() + "\n" +
               "§7Administrateur: §f" + data.getAdmin() + "\n" +
               "§7ID du Ban: §e" + data.getBanId() + "\n" +
               "§7Temps restant: §b" + remaining + "\n\n" +
               "§7Si vous pensez qu'il s'agit d'une erreur, contactez le staff.";
    }

    private static String formatTime(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        if (days > 0) return days + " jours " + (hours % 24) + "h";
        if (hours > 0) return hours + " heures " + (minutes % 60) + "m";
        if (minutes > 0) return minutes + " minutes " + (seconds % 60) + "s";
        return seconds + " secondes";
    }

    private long parseDuration(String input) {
        try {
            long unit = 1000;
            if (input.endsWith("d")) unit *= 60 * 60 * 24;
            else if (input.endsWith("h")) unit *= 60 * 60;
            else if (input.endsWith("m")) unit *= 60;
            else if (input.endsWith("s")) unit *= 1;
            else return -1;

            return Long.parseLong(input.substring(0, input.length() - 1)) * unit;
        } catch (Exception e) { return -1; }
    }
}
