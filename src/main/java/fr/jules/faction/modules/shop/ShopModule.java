package fr.jules.faction.modules.shop;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.modules.Module;

public class ShopModule extends Module {

    public ShopModule(FactionPlugin plugin) {
        super(plugin, "Shop");
    }

    @Override
    public void onEnable() {
        // Shop settings could be loaded here
    }

    @Override
    public void onDisable() {}
}
