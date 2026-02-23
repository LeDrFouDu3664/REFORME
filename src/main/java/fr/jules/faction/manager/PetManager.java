package fr.jules.faction.manager;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.model.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
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

        long deathRemaining = (info.getLastDeath() + 600000 - System.currentTimeMillis()) / 1000; // 10 min
        if (deathRemaining > 0) {
            player.sendMessage("§cVotre compagnon est blessé ! Revenez dans " + (deathRemaining / 60) + " min " + (deathRemaining % 60) + " s.");
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
        String petName = info.getCustomName() != null ? info.getCustomName() : info.getType();
        pet.setCustomName("§c§l" + petName);
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
        applyEquipment(pet, info);

        activePets.put(player.getUniqueId(), pet);
        data.setCurrentPet(petId);
        data.setPetCooldown(System.currentTimeMillis() + 20000); // 20s cooldown

        fr.jules.faction.modules.pet.PetModule petModule = (fr.jules.faction.modules.pet.PetModule) plugin.getModuleManager().getModule("Pet");
        String customName = info.getCustomName() != null ? info.getCustomName() : petId;
        String msg = petModule.getConfig().getString("messages.summon", "§aVous avez invoqué %name% !")
                .replace("%name%", customName);
        player.sendMessage(msg);

        // Metadata for death listener
        pet.setMetadata("is_pet", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
        pet.setMetadata("owner_uuid", new org.bukkit.metadata.FixedMetadataValue(plugin, player.getUniqueId().toString()));
    }

    public void handlePetXPGain(Player player, double amount) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        String petId = data.getCurrentPet();
        if (petId == null) return;

        fr.jules.faction.model.PetInfo info = data.getCapturedPets().get(petId);
        if (info == null) return;

        // Pet Level
        info.setExp(info.getExp() + amount);
        double req = 100 * Math.pow(1.5, info.getLevel() - 1);

        if (info.getExp() >= req) {
            info.setLevel(info.getLevel() + 1);
            info.setExp(0);
            player.sendMessage("§6§l[Compagnon] §e" + (info.getCustomName() != null ? info.getCustomName() : petId) + " est passé au niveau " + info.getLevel() + " !");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        }

        // Active Power Level
        String activePower = info.getActivePower();
        if (!activePower.equals("NONE")) {
            fr.jules.faction.model.PetInfo.PowerData pData = info.getPowers().computeIfAbsent(activePower, k -> new fr.jules.faction.model.PetInfo.PowerData());
            pData.setExp(pData.getExp() + amount);
            double pReq = 200 * Math.pow(1.8, pData.getLevel() - 1);
            if (pData.getExp() >= pReq) {
                pData.setLevel(pData.getLevel() + 1);
                pData.setExp(0);
                player.sendMessage("§6§l[Compagnon] §eLe pouvoir " + activePower + " de votre compagnon est passé au niveau " + pData.getLevel() + " !");
            }
        }
    }

    public void handleCapture(Player player, Entity entity) {
        if (!(entity instanceof LivingEntity) || entity instanceof Player) return;

        // Block Hostile and Nether/End Mobs
        if (entity instanceof Monster || entity instanceof Ghast || entity instanceof Slime || entity instanceof MagmaCube ||
            entity instanceof Phantom || entity instanceof Shulker || entity instanceof Hoglin || entity instanceof Piglin ||
            entity instanceof Warden || entity instanceof Blaze || entity instanceof WitherSkeleton ||
            entity instanceof Enderman || entity instanceof Breeze || entity instanceof Endermite) {
            player.sendMessage("§cVous ne pouvez pas capturer de monstres hostiles !");
            return;
        }

        org.bukkit.World.Environment env = entity.getWorld().getEnvironment();
        if (env == org.bukkit.World.Environment.NETHER || env == org.bukkit.World.Environment.THE_END) {
            player.sendMessage("§cVous ne pouvez pas capturer de créatures provenant du Nether ou de l'End !");
            return;
        }

        // Block Villagers, Golems and NPC-like
        if (entity instanceof Villager || entity instanceof IronGolem || entity instanceof Snowman ||
            entity instanceof WanderingTrader || entity instanceof AbstractVillager) {
            player.sendMessage("§cVous ne pouvez pas capturer cette créature !");
            return;
        }

        // Block Large Creatures
        if (entity instanceof Ravager || entity instanceof ElderGuardian || entity instanceof Wither ||
            entity instanceof EnderDragon || entity instanceof Giant || entity instanceof Sniffer ||
            entity instanceof Camel) {
            player.sendMessage("§cCette créature est beaucoup trop imposante pour être capturée !");
            return;
        }

        // Block Aquatic
        if (entity instanceof Squid || entity instanceof GlowSquid || entity instanceof Dolphin ||
            entity instanceof Turtle || entity instanceof Fish) {
            player.sendMessage("§cVous ne pouvez pas capturer de créatures aquatiques !");
            return;
        }

        if (entity instanceof Tameable tameable && tameable.isTamed()) {
            if (tameable.getOwner() != null && !tameable.getOwner().getUniqueId().equals(player.getUniqueId())) {
                player.sendMessage("§cCet animal appartient déjà à quelqu'un !");
                return;
            }
        }

        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        fr.jules.faction.modules.pet.PetModule petModule = (fr.jules.faction.modules.pet.PetModule) plugin.getModuleManager().getModule("Pet");

        String species = entity.getType().name();
        boolean alreadyHasSpecies = data.getCapturedPets().values().stream()
                .anyMatch(p -> p.getType().equalsIgnoreCase(species));

        if (alreadyHasSpecies) {
            player.sendMessage("§cVous possédez déjà un animal de cette espèce !");
            return;
        }

        String petId = entity.getType().name() + "_" + (data.getCapturedPets().size() + 1);

        fr.jules.faction.model.PetInfo info = new fr.jules.faction.model.PetInfo();
        info.setType(entity.getType().name());
        info.setBaby(entity instanceof Ageable ageable && !ageable.isAdult());
        info.setVariant(getVariant(entity));

        data.getCapturedPets().put(petId, info);
        entity.remove();

        String msg = petModule.getConfig().getString("messages.capture-success", "§aFélicitations ! Vous avez capturé un §e%type% §a!")
                .replace("%type%", entity.getType().name());
        player.sendMessage(msg);

        // Consume Lasso
        ItemStack lasso = player.getInventory().getItemInMainHand();
        lasso.setAmount(lasso.getAmount() - 1);
    }

    private String getVariant(Entity entity) {
        try {
            if (entity instanceof Cat cat) return cat.getCatType().getKey().getKey().toUpperCase();
            if (entity instanceof Parrot parrot) return parrot.getVariant().name();
            if (entity instanceof Fox fox) return fox.getFoxType().name();
            if (entity instanceof Rabbit rabbit) return rabbit.getRabbitType().name();
            if (entity instanceof Llama llama) return llama.getColor().name();
            if (entity instanceof Wolf wolf) return wolf.getVariant().getKey().getKey().toUpperCase();
            if (entity instanceof Horse horse) return horse.getColor().name();
            if (entity instanceof Sheep sheep) return sheep.getColor().name();
            if (entity instanceof MushroomCow mc) return mc.getVariant().name();
            if (entity instanceof Axolotl axo) return axo.getVariant().name();
            if (entity instanceof Frog frog) return frog.getVariant().getKey().getKey().toUpperCase();
            if (entity instanceof Villager vil) return vil.getVillagerType().toString();
        } catch (Exception ignored) {}
        return null;
    }

    private void applyEquipment(Entity entity, fr.jules.faction.model.PetInfo info) {
        if (entity instanceof Pig pig && info.getSaddle() != null) {
            pig.setSaddle(true);
        } else if (entity instanceof Horse horse) {
            if (info.getSaddle() != null) horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
            if (info.getChestplate() != null) {
                try {
                    horse.getInventory().setArmor(new ItemStack(Material.valueOf(info.getChestplate())));
                } catch (Exception ignored) {}
            }
        } else if (entity instanceof Wolf wolf && info.getChestplate() != null) {
            try {
                // In 1.21.1 Wolves use a specific body armor slot or method
                wolf.getEquipment().setChestplate(new ItemStack(Material.valueOf(info.getChestplate())));
            } catch (Exception ignored) {}
        } else if (entity instanceof Camel camel && info.getSaddle() != null) {
            camel.getInventory().setSaddle(new ItemStack(Material.SADDLE));
        } else if (entity instanceof Llama llama && info.getChestplate() != null) {
            // Llamas use carpets as armor
            try {
                llama.getInventory().setDecor(new ItemStack(Material.valueOf(info.getChestplate())));
            } catch (Exception ignored) {}
        }
    }

    private void applyVariant(Entity entity, String variant) {
        if (variant == null || variant.isEmpty()) return;
        try {
            if (entity instanceof Cat cat) {
                cat.setCatType(org.bukkit.Registry.CAT_VARIANT.get(org.bukkit.NamespacedKey.minecraft(variant.toLowerCase())));
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
                vil.setVillagerType(org.bukkit.Registry.VILLAGER_TYPE.get(org.bukkit.NamespacedKey.minecraft(variant.toLowerCase())));
            }
        } catch (Exception ignored) {}
    }

    public void despawnPet(Player player) {
        Entity pet = activePets.remove(player.getUniqueId());
        if (pet != null) {
            pet.remove();
            fr.jules.faction.modules.pet.PetModule petModule = (fr.jules.faction.modules.pet.PetModule) plugin.getModuleManager().getModule("Pet");
            PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
            String customName = data.getCurrentPet();
            if (data.getCurrentPet() != null && data.getCapturedPets().containsKey(data.getCurrentPet())) {
                fr.jules.faction.model.PetInfo info = data.getCapturedPets().get(data.getCurrentPet());
                if (info.getCustomName() != null) customName = info.getCustomName();
            }
            String msg = petModule.getConfig().getString("messages.store", "§eVous avez rangé %name%.")
                    .replace("%name%", customName != null ? customName : "l'animal");
            player.sendMessage(msg);
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

                // Pet Buffs based on type and active power
                String petId = plugin.getPlayerManager().getPlayerData(player.getUniqueId()).getCurrentPet();
                fr.jules.faction.model.PetInfo info = plugin.getPlayerManager().getPlayerData(player.getUniqueId()).getCapturedPets().get(petId);
                if (info == null) continue;

                // Base species buff
                String type = info.getType();
                if (type.equals("WOLF")) player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 40, 0, false, false));
                else if (type.equals("CAT")) player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0, false, false));
                else if (type.equals("PARROT")) player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 40, 0, false, false));
                else if (type.equals("FOX")) player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 300, 0, false, false));
                else if (type.equals("SHEEP")) player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 0, false, false));
                else player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 40, 0, false, false));

                // Active Power Buff
                String activePower = info.getActivePower();
                if (!activePower.equals("NONE")) {
                    fr.jules.faction.model.PetInfo.PowerData pData = info.getPowers().get(activePower);
                    int pLevel = pData != null ? pData.getLevel() : 1;
                    int amplifier = (pLevel / 5); // Level 5 = II, Level 10 = III, etc.

                    switch (activePower) {
                        case "MINER":
                            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 40, amplifier, false, false));
                            break;
                        case "TANK":
                            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 40, amplifier, false, false));
                            break;
                        case "FIGHTER":
                            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 40, amplifier, false, false));
                            break;
                        case "SCOUT":
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, amplifier + 1, false, false));
                            break;
                    }
                }
            }
        }, 20, 20);
    }
}
