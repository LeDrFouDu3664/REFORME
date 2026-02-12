package fr.jules.faction.manager;

import fr.jules.faction.model.Claim;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ClaimManager {
    private final Map<String, Claim> claims = new ConcurrentHashMap<>(); // Key: "world,x,z"

    public void addClaim(Claim claim) {
        claims.put(claim.toString(), claim);
    }

    public void removeClaim(String world, int x, int z) {
        claims.remove(world + "," + x + "," + z);
    }

    public Claim getClaim(String world, int x, int z) {
        return claims.get(world + "," + x + "," + z);
    }

    public boolean isClaimed(String world, int x, int z) {
        return claims.containsKey(world + "," + x + "," + z);
    }

    public List<Claim> getFactionClaims(UUID factionId) {
        return claims.values().stream()
                .filter(c -> c.getFactionId().equals(factionId))
                .collect(Collectors.toList());
    }

    public void removeAllFactionClaims(UUID factionId) {
        claims.values().removeIf(c -> c.getFactionId().equals(factionId));
    }

    public Collection<Claim> getAllClaims() {
        return claims.values();
    }
}
