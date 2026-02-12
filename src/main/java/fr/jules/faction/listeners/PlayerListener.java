package fr.jules.faction.listeners;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.model.Faction;
import fr.jules.faction.model.PlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    private final FactionPlugin plugin;

    public PlayerListener(FactionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PlayerData data = plugin.getDataManager().loadPlayerData(event.getPlayer().getUniqueId());
        if (data == null) {
            data = plugin.getPlayerManager().getPlayerData(event.getPlayer().getUniqueId());
        } else {
            plugin.getPlayerManager().addPlayerData(data);
        }
        data.setName(event.getPlayer().getName());
        data.setLastJoin(System.currentTimeMillis());

        if (data.getFactionId() != null) {
            Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
            if (faction != null) {
                event.getPlayer().sendMessage("§6[MOTD] " + faction.getMotd());
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(event.getPlayer().getUniqueId());
        plugin.getDataManager().savePlayerData(data);
        plugin.getPlayerManager().removePlayerData(event.getPlayer().getUniqueId());
    }
}
