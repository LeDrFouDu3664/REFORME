package fr.jules.faction;

import fr.jules.faction.commands.*;
import fr.jules.faction.listeners.*;
import fr.jules.faction.manager.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import lombok.Getter;

@Getter
public class FactionPlugin extends JavaPlugin {
    private FactionManager factionManager;
    @Getter private org.bukkit.Location chateauLocation;
    @Getter private org.bukkit.Location forteresseLocation;
    private PlayerManager playerManager;
    private ClaimManager claimManager;
    private TeleportManager teleportManager;
    private DataManager dataManager;
    @Getter private EconomyManager economyManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadLocations();
        fr.jules.faction.utils.MessageUtils.init(this);

        this.factionManager = new FactionManager();
        this.playerManager = new PlayerManager();
        this.claimManager = new ClaimManager();
        this.teleportManager = new TeleportManager();
        this.dataManager = new DataManager(this);
        this.economyManager = new EconomyManager(this);

        dataManager.loadFactions(factionManager, claimManager);

        registerCommands();
        registerListeners();
        startTasks();

        getLogger().info("FactionPlugin a été activé !");
    }

    private void registerCommands() {
        FactionCommand factionCommand = new FactionCommand(this);
        getCommand("f").setExecutor(factionCommand);
        getCommand("f").setTabCompleter(new FactionTabCompleter());

        TeleportCommands teleportCommands = new TeleportCommands(this);
        getCommand("tpa").setExecutor(teleportCommands);
        getCommand("tpahere").setExecutor(teleportCommands);
        getCommand("tpyes").setExecutor(teleportCommands);
        getCommand("tpno").setExecutor(teleportCommands);
        getCommand("sethome").setExecutor(new HomeCommands(this));
        getCommand("home").setExecutor(new HomeCommands(this));

        MiscCommands misc = new MiscCommands(this);
        getCommand("spawn").setExecutor(misc);
        getCommand("chateau").setExecutor(misc);
        getCommand("forteresse").setExecutor(misc);
        getCommand("money").setExecutor(misc);
        getCommand("power").setExecutor(misc);
        getCommand("ignore").setExecutor(misc);
        getCommand("shop").setExecutor(misc);
        getCommand("boutique").setExecutor(misc);
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ProtectionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PvPListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GUIListener(this), this);
        Bukkit.getPluginManager().registerEvents(new EntityListener(this), this);
    }

    private void startTasks() {
        double powerGain = getConfig().getDouble("settings.power.per-interval", 0.2);
        int interval = getConfig().getInt("settings.power.interval-minutes", 5);

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            Bukkit.getOnlinePlayers().forEach(p -> {
                playerManager.getPlayerData(p.getUniqueId()).addPower(powerGain);
            });
            factionManager.getAllFactions().forEach(f -> {
                factionManager.recalculatePower(f, playerManager);
            });
        }, 20 * 60 * interval, 20 * 60 * interval);
    }

    @Override
    public void onDisable() {
        if (factionManager != null) {
            factionManager.getAllFactions().forEach(dataManager::saveFaction);
        }
        if (playerManager != null) {
            playerManager.getAllPlayerData().forEach(dataManager::savePlayerData);
        }
        saveLocations();
        getLogger().info("FactionPlugin a été désactivé !");
    }

    public void setChateauLocation(org.bukkit.Location loc) {
        this.chateauLocation = loc;
        saveLocations();
    }

    public void setForteresseLocation(org.bukkit.Location loc) {
        this.forteresseLocation = loc;
        saveLocations();
    }

    private void loadLocations() {
        if (getConfig().contains("locations.chateau")) chateauLocation = getConfig().getLocation("locations.chateau");
        if (getConfig().contains("locations.forteresse")) forteresseLocation = getConfig().getLocation("locations.forteresse");
    }

    private void saveLocations() {
        getConfig().set("locations.chateau", chateauLocation);
        getConfig().set("locations.forteresse", forteresseLocation);
        saveConfig();
    }
}
