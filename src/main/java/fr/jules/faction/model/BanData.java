package fr.jules.faction.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BanData {
    private String banId;
    private String targetName;
    private UUID targetUUID;
    private String ip;
    private String reason;
    private String admin;
    private long expiryTime; // -1 for permanent
    private long timestamp;

    public boolean isExpired() {
        return expiryTime != -1 && System.currentTimeMillis() > expiryTime;
    }
}
