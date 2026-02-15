package fr.jules.faction.utils;

import fr.jules.faction.FactionPlugin;
import org.bukkit.ChatColor;

public class MessageUtils {
    private static FactionPlugin plugin;

    public static void init(FactionPlugin instance) {
        plugin = instance;
    }

    public static String getMessage(String key) {
        String msg = plugin.getConfig().getString("messages." + key);
        if (msg == null) return "§cMessage '" + key + "' manquant dans la config.";
        return msg.replace('&', '§');
    }

    public static String getMessage(String key, String... placeholders) {
        String msg = getMessage(key);
        for (int i = 0; i < placeholders.length; i += 2) {
            msg = msg.replace(placeholders[i], placeholders[i + 1]);
        }
        return msg;
    }

    public static void sendMessage(org.bukkit.entity.Player player, String key, String... placeholders) {
        player.sendMessage(getMessage(key, placeholders));
    }
}
