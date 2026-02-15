package fr.jules.faction.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.entity.EntityType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PetInfo {
    private String type;
    private String variant; // For colors, variants, etc.
    private boolean baby;
}
