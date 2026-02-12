package fr.jules.faction.manager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TeleportManager {
    private final Map<UUID, UUID> tpaRequests = new ConcurrentHashMap<>(); // target -> sender
    private final Map<UUID, UUID> tpaHereRequests = new ConcurrentHashMap<>(); // target -> sender

    public void sendTpa(UUID sender, UUID target) {
        tpaRequests.put(target, sender);
    }

    public void sendTpaHere(UUID sender, UUID target) {
        tpaHereRequests.put(target, sender);
    }

    public UUID getTpaRequest(UUID target) {
        return tpaRequests.get(target);
    }

    public UUID getTpaHereRequest(UUID target) {
        return tpaHereRequests.get(target);
    }

    public void removeRequests(UUID target) {
        tpaRequests.remove(target);
        tpaHereRequests.remove(target);
    }
}
