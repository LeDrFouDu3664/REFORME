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
    private PlayerManager playerManager;
    private ClaimManager claimManager;
    private TeleportManager teleportManager;
    private DataManager dataManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        fr.jules.faction.utils.MessageUtils.init(this);

        this.factionManager = new FactionManager();
        this.playerManager = new PlayerManager();
        this.claimManager = new ClaimManager();
        this.teleportManager = new TeleportManager();
        this.dataManager = new DataManager(this);

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
    }

    private void startTasks() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            Bukkit.getOnlinePlayers().forEach(p -> {
                playerManager.getPlayerData(p.getUniqueId()).addPower(0.2);
            });
            factionManager.getAllFactions().forEach(f -> {
                double totalPower = 0;
                for (java.util.UUID mid : f.getMembers()) {
                    totalPower += playerManager.getPlayerData(mid).getPower();
                }
                f.setPower(totalPower);
            });
        }, 20 * 60 * 5, 20 * 60 * 5); // Toutes les 5 minutes
    }

    @Override
    public void onDisable() {
        if (factionManager != null) {
            factionManager.getAllFactions().forEach(dataManager::saveFaction);
        }
        if (playerManager != null) {
            playerManager.getAllPlayerData().forEach(dataManager::savePlayerData);
        }
        getLogger().info("FactionPlugin a été désactivé !");
    }
}
