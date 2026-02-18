package fr.jules.faction.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PetInfo {
    private String type;
    private String variant;
    private boolean baby;
    private String customName;
    private int level = 1;
    private double exp = 0;
    private String activePower = "NONE";
    private Map<String, PowerData> powers = new HashMap<>();
    private String helmet; // For armor
    private String chestplate;
    private String saddle;
    private long lastDeath = 0;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PowerData {
        private int level = 1;
        private double exp = 0;
        private Map<String, Integer> skills = new HashMap<>(); // skillId -> level
    }
}
