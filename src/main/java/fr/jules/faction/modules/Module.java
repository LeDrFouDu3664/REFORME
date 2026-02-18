package fr.jules.faction.modules;

import fr.jules.faction.FactionPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;

public abstract class Module implements Listener {
    protected final FactionPlugin plugin;
    protected final String name;
    protected FileConfiguration config;
    protected File configFile;

    public Module(FactionPlugin plugin, String name) {
        this.plugin = plugin;
        this.name = name;
        loadConfig();
    }

    public abstract void onEnable();
    public abstract void onDisable();

    protected void loadConfig() {
        String path = "modules/" + name.toLowerCase() + "/config.yml";
        configFile = new File(plugin.getDataFolder(), path);

        if (!configFile.exists()) {
            plugin.getLogger().info("Génération de la configuration pour le module: " + name);
            try {
                plugin.saveResource(path, false);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Ressource introuvable dans le JAR: " + path);
                // Create empty if missing in JAR to avoid null config
                if (!configFile.getParentFile().exists()) configFile.getParentFile().mkdirs();
                try { configFile.createNewFile(); } catch (IOException ignored) {}
            }
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
