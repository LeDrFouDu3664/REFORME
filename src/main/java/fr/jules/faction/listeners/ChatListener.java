package fr.jules.faction.listeners;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.model.Faction;
import fr.jules.faction.model.PlayerData;
import fr.jules.faction.utils.MessageUtils;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class ChatListener implements Listener {
    private final FactionPlugin plugin;

    public ChatListener(FactionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        // Anti-Spam
        if (!player.hasPermission("faction.staff")) {
            if (System.currentTimeMillis() - data.getLastMessageTime() < 2000) { // 2s cooldown
                event.setCancelled(true);
                player.sendMessage("§cMerci de ne pas spammer ! (2s)");
                return;
            }
            if (message.equalsIgnoreCase(data.getLastMessageContent())) {
                event.setCancelled(true);
                player.sendMessage("§cNe répétez pas le même message !");
                return;
            }
        }
        data.setLastMessageTime(System.currentTimeMillis());
        data.setLastMessageContent(message);

        String mode = data.getChatMode();
        if (mode.equalsIgnoreCase("PUBLIC")) return;

        event.setCancelled(true);

        if (data.getFactionId() == null) {
            MessageUtils.sendMessage(player, "not-in-faction");
            data.setChatMode("PUBLIC");
            return;
        }

        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        String title = data.getTitle() == null || data.getTitle().isEmpty() ? "" : data.getTitle() + " ";

        if (mode.equalsIgnoreCase("FACTION")) {
            broadcastToFaction(faction, "§a[F] " + title + player.getName() + ": §f" + message);
        } else if (mode.equalsIgnoreCase("TRUCE")) {
            broadcastToRelations(faction, "§6[T] " + title + player.getName() + ": §f" + message, "TRUCE", "ALLY");
        } else if (mode.equalsIgnoreCase("ALLY")) {
            broadcastToRelations(faction, "§d[A] " + title + player.getName() + ": §f" + message, "ALLY");
        }
    }

    private void broadcastToFaction(Faction faction, String message) {
        for (UUID memberId : faction.getMembers()) {
            Player p = Bukkit.getPlayer(memberId);
            if (p != null) p.sendMessage(message);
        }
    }

    private void broadcastToRelations(Faction faction, String message, String... relations) {
        broadcastToFaction(faction, message);
        for (UUID otherFacId : faction.getRelations().keySet()) {
            String rel1 = faction.getRelations().get(otherFacId);
            Faction otherFac = plugin.getFactionManager().getFaction(otherFacId);
            if (otherFac == null) continue;
            String rel2 = otherFac.getRelations().get(faction.getId());

            for (String allowed : relations) {
                if (rel1.equals(allowed) && rel1.equals(rel2)) {
                    broadcastToFaction(otherFac, message);
                    break;
                }
            }
        }
    }
}
