package fr.jules.faction.manager;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.utils.DiscordWebhook;
import org.bukkit.Bukkit;

public class DiscordManager {
    private final FactionPlugin plugin;
    private DiscordWebhook webhook;

    public DiscordManager(FactionPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        String url = plugin.getConfig().getString("settings.discord.webhook-url");
        this.webhook = new DiscordWebhook(url);
    }

    public void log(String message, String type) {
        if (plugin.getConfig().getBoolean("settings.discord.log-" + type, true)) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                webhook.execute(message);
            });
        }
    }
}
