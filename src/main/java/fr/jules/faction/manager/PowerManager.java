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
        powers.put("SPEED_II", new PowerInfo("Vitesse Supérieure", "Course effrénée (Vitesse II).", PotionEffectType.SPEED, 1));
        powers.put("STRENGTH", new PowerInfo("Force Brute", "Vos coups sont dévastateurs (Force I).", PotionEffectType.INCREASE_DAMAGE, 0));
        powers.put("RESISTANCE", new PowerInfo("Blindage", "Protection accrue contre les coups (Résistance I).", PotionEffectType.DAMAGE_RESISTANCE, 0));
        powers.put("NO_FALL", new PowerInfo("Plume", "Immunité totale aux dégâts de chute.", null, 0));
        powers.put("FIRE_RES", new PowerInfo("Peau de Lave", "Immunité totale au feu.", PotionEffectType.FIRE_RESISTANCE, 0));
        powers.put("DOUBLE_XP", new PowerInfo("Érudit", "Double XP gagnée via les métiers.", null, 0));
        powers.put("DOUBLE_MONEY", new PowerInfo("Capitaliste", "Double argent gagné via les métiers.", null, 0));
        powers.put("AUTO_SMELT", new PowerInfo("Hâte Infernale", "Les minerais sont cuits à la mine.", null, 0));
        powers.put("TELEKINESIS", new PowerInfo("Magnétisme", "Les items vont direct dans l'inventaire.", null, 0));
        powers.put("SNEAK_INVIS", new PowerInfo("Ombre", "Invisible en mode discret (Sneak).", null, 0));
        powers.put("VAMPIRE", new PowerInfo("Vampirisme", "Soigne 10% des dégâts infligés.", null, 0));
        powers.put("STUN", new PowerInfo("Coup Assommant", "10% de chance d'étourdir l'ennemi.", null, 0));
        powers.put("SATURATION", new PowerInfo("Insatiable", "Barre de faim toujours pleine.", PotionEffectType.SATURATION, 0));
        powers.put("NIGHT_VISION", new PowerInfo("Nyctalope", "Vision nocturne permanente.", PotionEffectType.NIGHT_VISION, 0));
        powers.put("HEALTH_II", new PowerInfo("Vitalité II", "4 cœurs supplémentaires.", PotionEffectType.HEALTH_BOOST, 1));
        powers.put("HASTE_II", new PowerInfo("Forreur", "Minage ultra rapide (Hâte II).", PotionEffectType.FAST_DIGGING, 1));
        powers.put("LUCK_MINER", new PowerInfo("Filons d'Or", "5% de chance de doubler les minerais.", null, 0));
        powers.put("LUCK_FARMER", new PowerInfo("Main Verte", "Les cultures poussent instantanément.", null, 0));
        powers.put("LAVA_SPEED", new PowerInfo("Dauphin de Feu", "Nage rapide dans la lave.", null, 0));
        powers.put("LUCK_LOOT", new PowerInfo("Pilleur", "Double les loots des monstres.", null, 0));
    }

    public void applyEffects(Player player) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) return;

        for (String powerId : data.getPowers()) {
            PowerInfo info = powers.get(powerId);
            if (info != null && info.effectType != null) {
                player.addPotionEffect(new PotionEffect(info.effectType, 60, info.amplifier, false, false, true));
            }
            if (powerId.equals("SNEAK_INVIS") && player.isSneaking()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 40, 0, false, false, false));
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
        public final int amplifier;

        public PowerInfo(String name, String description, PotionEffectType effectType, int amplifier) {
            this.name = name;
            this.description = description;
            this.effectType = effectType;
            this.amplifier = amplifier;
        }
    }
}
