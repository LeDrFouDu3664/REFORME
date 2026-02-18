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
        File moduleFolder = new File(plugin.getDataFolder(), "modules/" + name.toLowerCase());
        if (!moduleFolder.exists()) moduleFolder.mkdirs();

        configFile = new File(moduleFolder, "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("modules/" + name.toLowerCase() + "/config.yml", false);
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
