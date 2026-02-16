package fr.jules.faction.listeners;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.commands.BanCommand;
import fr.jules.faction.model.BanData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class BanListener implements Listener {
    private final FactionPlugin plugin;

    public BanListener(FactionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(AsyncPlayerPreLoginEvent event) {
        // Check UUID ban
        BanData data = plugin.getBanManager().getBan(event.getUniqueId());
        if (data == null) {
            // Check IP ban
            data = plugin.getBanManager().getIPBan(event.getAddress().getHostAddress());
        }

        if (data != null) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, BanCommand.formatBanMessage(data));
        }
    }
}
