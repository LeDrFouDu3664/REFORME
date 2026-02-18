package fr.jules.faction.modules.chat;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.modules.Module;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.Random;

public class ChatModule extends Module {
    private final Random random = new Random();

    public ChatModule(FactionPlugin plugin) {
        super(plugin, "Chat");
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            String welcome = config.getString("join.welcome-first")
                    .replace("%player%", player.getName())
                    .replace("%count%", String.valueOf(Bukkit.getOfflinePlayers().length));
            Bukkit.broadcastMessage(welcome);
        }

        String joinMsg = config.getString("join.join-message")
                .replace("%player%", player.getName());
        event.setJoinMessage(joinMsg);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        String quitMsg = config.getString("join.quit-message")
                .replace("%player%", event.getPlayer().getName());
        event.setQuitMessage(quitMsg);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();

        String category = "other";
        if (killer != null) {
            category = "combat";
        } else if (player.getLastDamageCause() != null) {
            switch (player.getLastDamageCause().getCause()) {
                case FALL: case LAVA: case FIRE_TICK: category = "stupid"; break;
                case LIGHTNING: case BLOCK_EXPLOSION: case ENTITY_EXPLOSION: category = "accidental"; break;
            }
        }

        List<String> messages = config.getStringList("death." + category);
        if (messages.isEmpty()) messages = config.getStringList("death.other");

        String msg = messages.get(random.nextInt(messages.size()))
                .replace("%player%", player.getName())
                .replace("%killer%", killer != null ? killer.getName() : "Inconnu");

        event.setDeathMessage(msg);
    }

    @EventHandler
    public void onPetDeath(EntityDeathEvent event) {
        if (event.getEntity().hasMetadata("is_pet")) {
            String uuidStr = event.getEntity().getMetadata("owner_uuid").get(0).asString();
            Player owner = Bukkit.getPlayer(java.util.UUID.fromString(uuidStr));
            String ownerName = owner != null ? owner.getName() : "Inconnu";
            String petName = event.getEntity().getCustomName();
            if (petName == null) petName = event.getEntity().getType().name();

            String msg = config.getString("pet-death")
                    .replace("%player%", ownerName)
                    .replace("%name%", petName);
            Bukkit.broadcastMessage(msg);
        }
    }
}
