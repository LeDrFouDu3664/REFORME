package fr.jules.faction.modules.pet;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.modules.Module;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PetModule extends Module {

    public PetModule(FactionPlugin plugin) {
        super(plugin, "Pet");
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    @EventHandler
    public void onCapture(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        String lassoName = config.getString("capture.lasso-name", "§bLasso de Capture");
        if (item.hasItemMeta() && item.getItemMeta().getDisplayName().equals(lassoName)) {
            event.setCancelled(true);
            EntityType type = event.getRightClicked().getType();

            List<String> blocked = config.getStringList("capture.blocked-entities");
            if (blocked.contains(type.name())) {
                player.sendMessage(config.getString("capture.refusal-message"));
                return;
            }

            plugin.getPetManager().handleCapture(player, event.getRightClicked());
        }
    }

    @EventHandler
    public void onPetKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        plugin.getPetManager().handlePetXPGain(killer, config.getDouble("leveling.xp-per-kill", 10.0));
    }
}
