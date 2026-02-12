package fr.jules.faction.model;

public enum Grade {
    RECRUIT("Recrue"),
    MEMBER("Membre"),
    OFFICER("Officier"),
    LEADER("Chef");

    private final String name;

    Grade(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
