package fr.jules.faction.model;

import lombok.Getter;

@Getter
public enum Rank {
    JOUEUR("§7Joueur", 1, 1.0),
    NOVICE("§aNovice", 2, 1.1),
    GUERRIER("§6Guerrier", 3, 1.2),
    ELITE("§bElite", 5, 1.5),
    LEGENDE("§dLégende", 10, 2.0),
    HELPER("§2Helper", 15, 2.0),
    MODERATEUR("§9Modérateur", 20, 2.0),
    ADMINISTRATEUR("§cAdministrateur", 50, 5.0),
    FONDATEUR("§4Fondateur", 100, 10.0);

    private final String prefix;
    private final int maxHomes;
    private final double powerMultiplier;

    Rank(String prefix, int maxHomes, double powerMultiplier) {
        this.prefix = prefix;
        this.maxHomes = maxHomes;
        this.powerMultiplier = powerMultiplier;
    }

    public boolean isStaff() {
        return this == HELPER || this == MODERATEUR || this == ADMINISTRATEUR;
    }
}
