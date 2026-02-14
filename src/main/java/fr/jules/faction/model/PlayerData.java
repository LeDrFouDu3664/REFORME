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
    private double balance = 0.0;
    private String job = "NONE";
    private double jobExp = 0;
    private int jobLevel = 1;
    private Map<String, Integer> questProgress = new HashMap<>(); // QuestID -> Progress
    private long lastQuestRefresh = 0;
    private Set<String> powers = new HashSet<>();
    private String currentPet = "NONE";
    private boolean bypass = false;
    private boolean autoClaim = false;
    private boolean showTitles = true;
    private double powerBoost = 0;
    private long lastDeathTime = 0;
    private long combatLoggedTime = 0;

    public void addPower(double amount) {
        this.power = Math.min(this.maxPower, this.power + amount);
    }

    public void removePower(double amount) {
        this.power = Math.max(-10.0, this.power - amount);
    }

    public void addBalance(double amount) {
        this.balance += amount;
    }

    public void removeBalance(double amount) {
        this.balance -= amount;
    }
}
