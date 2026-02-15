package fr.jules.faction.manager;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.model.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PetManager {
    private final FactionPlugin plugin;
    private final Map<UUID, Entity> activePets = new HashMap<>();

    public PetManager(FactionPlugin plugin) {
        this.plugin = plugin;
        startFollowTask();
    }

    public void spawnPet(Player player, String type) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());

        long remaining = (data.getPetCooldown() - System.currentTimeMillis()) / 1000;
        if (remaining > 0) {
            player.sendMessage("§cVous devez attendre encore " + remaining + "s avant de changer d'animal.");
            return;
        }

        if (!data.getOwnedPets().contains(type.toUpperCase())) {
            player.sendMessage("§cVous ne possédez pas cet animal.");
            return;
        }

        despawnPet(player);

        Entity pet;
        switch (type.toUpperCase()) {
            case "LOUP":
                Wolf wolf = (Wolf) player.getWorld().spawnEntity(player.getLocation(), EntityType.WOLF);
                wolf.setTamed(true);
                wolf.setOwner(player);
                wolf.setBaby();
                wolf.setAgeLock(true);
                wolf.setCustomName("§c§lCompagnon de " + player.getName());
                wolf.setCustomNameVisible(true);
                pet = wolf;
                break;
            case "CHAT":
                Cat cat = (Cat) player.getWorld().spawnEntity(player.getLocation(), EntityType.CAT);
                cat.setTamed(true);
                cat.setOwner(player);
                cat.setBaby();
                cat.setAgeLock(true);
                cat.setCustomName("§c§lFélin de " + player.getName());
                cat.setCustomNameVisible(true);
                pet = cat;
                break;
            case "PERROQUET":
                Parrot parrot = (Parrot) player.getWorld().spawnEntity(player.getLocation(), EntityType.PARROT);
                parrot.setTamed(true);
                parrot.setOwner(player);
                // Parrots don't have baby state in standard Bukkit API easily or at all
                parrot.setCustomName("§c§lPlume de " + player.getName());
                parrot.setCustomNameVisible(true);
                pet = parrot;
                break;
            case "RENARD":
                Fox fox = (Fox) player.getWorld().spawnEntity(player.getLocation(), EntityType.FOX);
                fox.setBaby();
                fox.setAgeLock(true);
                fox.setCustomName("§c§lRusé de " + player.getName());
                fox.setCustomNameVisible(true);
                pet = fox;
                break;
            default:
                return;
        }

        activePets.put(player.getUniqueId(), pet);
        data.setCurrentPet(type.toUpperCase());
        data.setPetCooldown(System.currentTimeMillis() + 300000); // 5 min cooldown
        player.sendMessage("§aVotre animal de compagnie §e" + type + " §aa été invoqué ! (Bébé)");
    }

    public void despawnPet(Player player) {
        Entity pet = activePets.remove(player.getUniqueId());
        if (pet != null) {
            pet.remove();
        }
    }

    private void startFollowTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Map.Entry<UUID, Entity> entry : activePets.entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());
                Entity pet = entry.getValue();

                if (player == null || !player.isOnline() || pet.isDead()) {
                    pet.remove();
                    continue;
                }

                if (pet.getWorld() != player.getWorld() || pet.getLocation().distance(player.getLocation()) > 10) {
                    pet.teleport(player.getLocation());
                }

                // Pet Buffs
                String type = plugin.getPlayerManager().getPlayerData(player.getUniqueId()).getCurrentPet();
                if (type.equals("LOUP")) player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 40, 0, false, false));
                if (type.equals("CHAT")) player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0, false, false));
                if (type.equals("PERROQUET")) player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 40, 0, false, false));
                if (type.equals("RENARD")) player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 300, 0, false, false));
            }
        }, 20, 20);
    }
}
