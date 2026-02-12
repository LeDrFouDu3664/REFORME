package fr.jules.faction.model;

import lombok.Data;
import org.bukkit.Location;

import java.util.*;

@Data
public class Faction {
    private final UUID id;
    private String name;
    private String description = "Une nouvelle faction.";
    private String motd = "Bienvenue dans la faction !";
    private UUID leader;
    private Set<UUID> members = new HashSet<>();
    private Set<UUID> officers = new HashSet<>();
    private Set<UUID> invites = new HashSet<>();
    private Set<UUID> requests = new HashSet<>();
    private Map<UUID, String> relations = new HashMap<>(); // UUID of other faction -> Relation name
    private Map<String, Boolean> permissions = new HashMap<>();
    private Location home;
    private double power = 0;
    private Set<String> claims = new HashSet<>(); // Format: "world,x,z"

    public void addMember(UUID uuid) {
        members.add(uuid);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
        officers.remove(uuid);
    }

    public boolean isOfficer(UUID uuid) {
        return officers.contains(uuid) || uuid.equals(leader);
    }
}
