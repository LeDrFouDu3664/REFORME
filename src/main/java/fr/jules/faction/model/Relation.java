package fr.jules.faction.model;

public enum Relation {
    NEUTRAL("Neutre"),
    ENEMY("Ennemi"),
    TRUCE("Trêve"),
    ALLY("Allié");

    private final String name;

    Relation(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
