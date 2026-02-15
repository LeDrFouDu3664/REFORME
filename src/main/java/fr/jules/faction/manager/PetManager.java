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

    public void spawnPet(Player player, String petId) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());

        long remaining = (data.getPetCooldown() - System.currentTimeMillis()) / 1000;
        if (remaining > 0) {
            player.sendMessage("§cVous devez attendre encore " + remaining + "s avant de changer d'animal.");
            return;
        }

        fr.jules.faction.model.PetInfo info = data.getCapturedPets().get(petId);
        if (info == null) {
            player.sendMessage("§cVous ne possédez pas cet animal.");
            return;
        }

        despawnPet(player);

        EntityType et;
        try {
            et = EntityType.valueOf(info.getType());
        } catch (Exception e) {
            return;
        }

        Entity pet = player.getWorld().spawnEntity(player.getLocation(), et);
        pet.setCustomName("§c§lCompagnon de " + player.getName());
        pet.setCustomNameVisible(true);

        if (pet instanceof Tameable tameable) {
            tameable.setTamed(true);
            tameable.setOwner(player);
        }

        if (pet instanceof Ageable ageable) {
            if (info.isBaby()) ageable.setBaby();
            else ageable.setAdult();
            ageable.setAgeLock(true);
        }

        applyVariant(pet, info.getVariant());

        activePets.put(player.getUniqueId(), pet);
        data.setCurrentPet(petId);
        data.setPetCooldown(System.currentTimeMillis() + 300000); // 5 min cooldown
        player.sendMessage("§aVotre animal de compagnie §e" + petId + " §aa été invoqué !");
    }

    private void applyVariant(Entity entity, String variant) {
        if (variant == null || variant.isEmpty()) return;
        try {
            if (entity instanceof Cat cat) {
                cat.setCatType(org.bukkit.entity.Cat.Type.valueOf(variant));
            } else if (entity instanceof Parrot parrot) {
                parrot.setVariant(org.bukkit.entity.Parrot.Variant.valueOf(variant));
            } else if (entity instanceof Fox fox) {
                fox.setFoxType(org.bukkit.entity.Fox.Type.valueOf(variant));
            } else if (entity instanceof Rabbit rabbit) {
                rabbit.setRabbitType(org.bukkit.entity.Rabbit.Type.valueOf(variant));
            } else if (entity instanceof Llama llama) {
                llama.setColor(org.bukkit.entity.Llama.Color.valueOf(variant));
            } else if (entity instanceof Wolf wolf) {
                // In 1.21.1 Wolf has variants
                wolf.setVariant(org.bukkit.Registry.WOLF_VARIANT.get(org.bukkit.NamespacedKey.minecraft(variant.toLowerCase())));
            } else if (entity instanceof Horse horse) {
                horse.setColor(org.bukkit.entity.Horse.Color.valueOf(variant));
            } else if (entity instanceof Sheep sheep) {
                sheep.setColor(org.bukkit.DyeColor.valueOf(variant));
            } else if (entity instanceof MushroomCow mc) {
                mc.setVariant(org.bukkit.entity.MushroomCow.Variant.valueOf(variant));
            } else if (entity instanceof Axolotl axo) {
                axo.setVariant(org.bukkit.entity.Axolotl.Variant.valueOf(variant));
            } else if (entity instanceof Frog frog) {
                frog.setVariant(org.bukkit.Registry.FROG_VARIANT.get(org.bukkit.NamespacedKey.minecraft(variant.toLowerCase())));
            } else if (entity instanceof Villager vil) {
                vil.setVillagerType(org.bukkit.entity.Villager.Type.valueOf(variant));
            }
        } catch (Exception ignored) {}
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

                // Pet Buffs based on type
                String petId = plugin.getPlayerManager().getPlayerData(player.getUniqueId()).getCurrentPet();
                fr.jules.faction.model.PetInfo info = plugin.getPlayerManager().getPlayerData(player.getUniqueId()).getCapturedPets().get(petId);
                if (info == null) continue;

                String type = info.getType();
                if (type.equals("WOLF")) player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 40, 0, false, false));
                else if (type.equals("CAT")) player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0, false, false));
                else if (type.equals("PARROT")) player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 40, 0, false, false));
                else if (type.equals("FOX")) player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 300, 0, false, false));
                else if (type.equals("SHEEP")) player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 0, false, false));
                else player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 40, 0, false, false));
            }
        }, 20, 20);
    }
}
