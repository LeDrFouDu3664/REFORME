package fr.jules.faction.model;

import lombok.Data;
import org.bukkit.Location;

import java.util.*;

@Data
public class PlayerData {
    private final UUID uuid;
    private String name;
    private UUID factionId;
    private Grade role = Grade.MEMBER;
    private double power = 10.0;
    private double maxPower = 10.0;
    private Map<String, Location> homes = new HashMap<>();
    private Set<UUID> ignoredPlayers = new HashSet<>();
    private long lastJoin;
    private String title = "";
    private String chatMode = "PUBLIC";
    private boolean bypass = false;
    private boolean autoClaim = false;
    private double powerBoost = 0;
    private long lastDeathTime = 0;
    private long combatLoggedTime = 0;

    public void addPower(double amount) {
        this.power = Math.min(maxPower, this.power + amount);
    }

    public void removePower(double amount) {
        this.power = Math.max(-10.0, this.power - amount);
    }
}
