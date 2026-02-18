package fr.jules.faction.listeners;

import fr.jules.faction.FactionPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;
import java.util.List;

public class CommandListener implements Listener {
    private final FactionPlugin plugin;
    private final List<String> blockedCommands = Arrays.asList("/pl", "/plugins", "/help", "/?", "/minecraft:help", "/bukkit:help", "/ver", "/version");

    public CommandListener(FactionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage().toLowerCase();
        String cmd = msg.split(" ")[0];

        // Block plugins/help
        if (blockedCommands.contains(cmd) && !event.getPlayer().isOp()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cCette commande est désactivée sur ce serveur.");
            return;
        }

        // Redirect ban/pardon
        if (cmd.equals("/ban")) {
            event.setMessage(msg.replaceFirst("/ban", "/tempban"));
        } else if (cmd.equals("/pardon") || cmd.equals("/unban")) {
            if (!cmd.startsWith("/f")) {
                event.setMessage(msg.replaceFirst(cmd, "/unban"));
            }
        }
    }
}
