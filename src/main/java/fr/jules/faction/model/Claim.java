package fr.jules.faction.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class Claim {
    private final String world;
    private final int x;
    private final int z;
    private UUID factionId;

    @Override
    public String toString() {
        return world + "," + x + "," + z;
    }

    public static Claim fromString(String str) {
        String[] split = str.split(",");
        return new Claim(split[0], Integer.parseInt(split[1]), Integer.parseInt(split[2]), null);
    }
}
