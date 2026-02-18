package fr.jules.faction.modules;

import fr.jules.faction.FactionPlugin;
import java.util.HashMap;
import java.util.Map;

public class ModuleManager {
    private final FactionPlugin plugin;
    private final Map<String, Module> modules = new HashMap<>();

    public ModuleManager(FactionPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerModule(Module module) {
        modules.put(module.name.toLowerCase(), module);
        plugin.getServer().getPluginManager().registerEvents(module, plugin);
        module.onEnable();
        plugin.getLogger().info("Module " + module.name + " activé !");
    }

    public void enableModules() {
        // Registration will happen here or in FactionPlugin
    }

    public void disableModules() {
        modules.values().forEach(Module::onDisable);
    }

    public Module getModule(String name) {
        return modules.get(name.toLowerCase());
    }
}
