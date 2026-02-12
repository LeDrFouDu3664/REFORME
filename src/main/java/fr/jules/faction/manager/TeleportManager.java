package fr.jules.faction.manager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TeleportManager {
    private final Map<UUID, TpaRequest> tpaRequests = new ConcurrentHashMap<>(); // target -> request
    private final Map<UUID, TpaRequest> tpaHereRequests = new ConcurrentHashMap<>(); // target -> request
    private static final long EXPIRATION = 60 * 1000; // 60 seconds

    public void sendTpa(UUID sender, UUID target) {
        tpaRequests.put(target, new TpaRequest(sender, System.currentTimeMillis()));
    }

    public void sendTpaHere(UUID sender, UUID target) {
        tpaHereRequests.put(target, new TpaRequest(sender, System.currentTimeMillis()));
    }

    public UUID getTpaRequest(UUID target) {
        TpaRequest req = tpaRequests.get(target);
        if (req != null && System.currentTimeMillis() - req.time < EXPIRATION) return req.sender;
        tpaRequests.remove(target);
        return null;
    }

    public UUID getTpaHereRequest(UUID target) {
        TpaRequest req = tpaHereRequests.get(target);
        if (req != null && System.currentTimeMillis() - req.time < EXPIRATION) return req.sender;
        tpaHereRequests.remove(target);
        return null;
    }

    private record TpaRequest(UUID sender, long time) {}

    public void removeRequests(UUID target) {
        tpaRequests.remove(target);
        tpaHereRequests.remove(target);
    }
}
