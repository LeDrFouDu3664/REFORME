package fr.jules.faction.model;

import lombok.Data;
import org.bukkit.Location;

import java.util.*;

@Data
public class Faction {
    private final UUID id;
    private String name;
    private String tag;
    private String description = "Une nouvelle faction.";
    private String motd = "Bienvenue dans la faction !";
    private UUID leader;
    private Set<UUID> members = new HashSet<>();
    private Set<UUID> officers = new HashSet<>();
    private Set<UUID> invites = new HashSet<>();
    private Set<UUID> requests = new HashSet<>();
    private Map<UUID, String> relations = new HashMap<>(); // UUID of other faction -> Relation name
    private Map<String, Set<Grade>> permissions = new HashMap<>();
    private Map<String, Boolean> factionFlags = new HashMap<>(); // Renamed from flags to avoid confusion
    private Location home;
    private double power = 0;
    private double balance = 0;
    private int tntStock = 0;
    private FactionType type = FactionType.NORMAL;
    private Set<String> claims = new HashSet<>(); // Format: "world,x,z"

    public void addMember(UUID uuid) {
        members.add(uuid);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
        officers.remove(uuid);
    }

    public void addBalance(double amount) {
        this.balance += amount;
    }

    public void removeBalance(double amount) {
        this.balance -= amount;
    }

    public void addTnt(int amount) {
        this.tntStock += amount;
    }

    public void removeTnt(int amount) {
        this.tntStock = Math.max(0, this.tntStock - amount);
    }

    public boolean isOfficer(UUID uuid) {
        return officers.contains(uuid) || uuid.equals(leader);
    }

    public boolean hasPermission(Grade grade, String action) {
        if (grade == Grade.LEADER) return true;
        Set<Grade> allowed = permissions.get(action);
        if (allowed == null) {
            // Default permissions
            return grade == Grade.OFFICER;
        }
        return allowed.contains(grade);
    }
}
