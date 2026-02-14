package fr.jules.faction.manager;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.model.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.*;

public class PowerManager {
    private final FactionPlugin plugin;
    private final Map<String, PowerInfo> powers = new LinkedHashMap<>();

    public PowerManager(FactionPlugin plugin) {
        this.plugin = plugin;
        setupPowers();
    }

    private void setupPowers() {
        powers.put("SPEED", new PowerInfo("Vitesse", "Vitesse de déplacement augmentée.", PotionEffectType.SPEED));
        powers.put("STRENGTH", new PowerInfo("Force", "Dégâts au corps à corps augmentés.", PotionEffectType.INCREASE_DAMAGE));
        powers.put("RESISTANCE", new PowerInfo("Résistance", "Dégâts subis réduits.", PotionEffectType.DAMAGE_RESISTANCE));
        powers.put("JUMP", new PowerInfo("Saut", "Hauteur de saut augmentée.", PotionEffectType.JUMP));
        powers.put("HASTE", new PowerInfo("Hâte", "Vitesse de minage augmentée.", PotionEffectType.FAST_DIGGING));
        powers.put("NIGHT_VISION", new PowerInfo("Vision Nocturne", "Voir dans le noir.", PotionEffectType.NIGHT_VISION));
        powers.put("WATER_BREATHING", new PowerInfo("Apnée", "Respirer sous l'eau.", PotionEffectType.WATER_BREATHING));
        powers.put("FIRE_RESISTANCE", new PowerInfo("Résistance Feu", "Immunité au feu et à la lave.", PotionEffectType.FIRE_RESISTANCE));
        powers.put("REGENERATION", new PowerInfo("Régénération", "Régénération de vie passive.", PotionEffectType.REGENERATION));
        powers.put("LUCK", new PowerInfo("Chance", "Plus de chance pour le butin.", PotionEffectType.LUCK));
        powers.put("SLOW_FALLING", new PowerInfo("Chute Lente", "Tomber sans se blesser.", PotionEffectType.SLOW_FALLING));
        powers.put("HEALTH_BOOST", new PowerInfo("Vitalité", "Cœurs supplémentaires.", PotionEffectType.HEALTH_BOOST));
        powers.put("ABSORPTION", new PowerInfo("Bouclier", "Cœurs d'absorption réguliers.", PotionEffectType.ABSORPTION));
        powers.put("SATURATION", new PowerInfo("Nutrition", "Ne plus avoir faim.", PotionEffectType.SATURATION));
        powers.put("DOLPHINS_GRACE", new PowerInfo("Grâce du Dauphin", "Nager plus vite.", PotionEffectType.DOLPHINS_GRACE));
        powers.put("CONDUIT_POWER", new PowerInfo("Puissance Conduit", "Vision et minage sous-marin.", PotionEffectType.CONDUIT_POWER));
        powers.put("HERO_OF_VILLAGE", new PowerInfo("Héros", "Réductions chez les villageois.", PotionEffectType.HERO_OF_THE_VILLAGE));
        powers.put("INVISIBILITY", new PowerInfo("Furtivité", "Devenir invisible.", PotionEffectType.INVISIBILITY));
        powers.put("GLOWING", new PowerInfo("Radar", "Vous brillez (détection).", PotionEffectType.GLOWING));
        powers.put("LUCK_MINER", new PowerInfo("Mineur Chanceux", "Bonus de métier minage.", null));
    }

    public void applyEffects(Player player) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) return;

        for (String powerId : data.getPowers()) {
            PowerInfo info = powers.get(powerId);
            if (info != null && info.effectType != null) {
                player.addPotionEffect(new PotionEffect(info.effectType, 40, 0, false, false, true));
            }
        }
    }

    public Map<String, PowerInfo> getPowers() {
        return powers;
    }

    public static class PowerInfo {
        public final String name;
        public final String description;
        public final PotionEffectType effectType;

        public PowerInfo(String name, String description, PotionEffectType effectType) {
            this.name = name;
            this.description = description;
            this.effectType = effectType;
        }
    }
}
