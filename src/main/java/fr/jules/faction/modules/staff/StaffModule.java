package fr.jules.faction.modules.staff;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.modules.Module;

public class StaffModule extends Module {

    public StaffModule(FactionPlugin plugin) {
        super(plugin, "Staff");
    }

    @Override
    public void onEnable() {
        // Staff settings could be loaded here
    }

    @Override
    public void onDisable() {}
}
