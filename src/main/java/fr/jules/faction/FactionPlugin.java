package fr.jules.faction;

import fr.jules.faction.commands.*;
import fr.jules.faction.listeners.*;
import fr.jules.faction.manager.*;
import fr.jules.faction.model.*;
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
    @Getter private QuestManager questManager;
    @Getter private PowerManager powerManager;
    @Getter private FactionLevelManager factionLevelManager;
    @Getter private PetManager petManager;
    @Getter private AuctionManager auctionManager;
    @Getter private BanManager banManager;
    @Getter private DiscordManager discordManager;
    @Getter private AFKManager afkManager;
    @Getter private TabManager tabManager;
    @Getter private VanishManager vanishManager;
    @Getter private fr.jules.faction.modules.ModuleManager moduleManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadLocations();
        fr.jules.faction.utils.MessageUtils.init(this);
        fr.jules.faction.gui.FactionGUI.init(this);

        this.factionManager = new FactionManager();
        this.playerManager = new PlayerManager(this);
        this.claimManager = new ClaimManager();
        this.teleportManager = new TeleportManager();
        this.dataManager = new DataManager(this);
        this.economyManager = new EconomyManager(this);
        this.questManager = new QuestManager(this);
        this.powerManager = new PowerManager(this);
        this.factionLevelManager = new FactionLevelManager(this);
        this.petManager = new PetManager(this);
        this.auctionManager = new AuctionManager(this);
        this.banManager = new BanManager(this);
        this.discordManager = new DiscordManager(this);
        this.afkManager = new AFKManager(this);
        this.tabManager = new TabManager(this);
        this.vanishManager = new VanishManager(this);

        this.moduleManager = new fr.jules.faction.modules.ModuleManager(this);
        moduleManager.registerModule(new fr.jules.faction.modules.core.CoreModule(this));
        moduleManager.registerModule(new fr.jules.faction.modules.chat.ChatModule(this));
        moduleManager.registerModule(new fr.jules.faction.modules.pet.PetModule(this));

        dataManager.loadFactions(factionManager, claimManager);
        factionManager.getAllFactions().forEach(f -> factionManager.recalculatePower(f, playerManager));

        registerCommands();
        registerListeners();
        startTasks();

        getLogger().info("TPC faction a été activé ! (Système de Claim/Power à 100%)");
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
        getCommand("eco").setExecutor(misc);
        getCommand("mod").setExecutor(misc);
        getCommand("ec").setExecutor(misc);
        getCommand("afk").setExecutor(misc);
        getCommand("rank").setExecutor(misc);
        getCommand("jobs").setExecutor(new JobCommand(this));
        getCommand("quests").setExecutor(new QuestCommand(this));
        getCommand("ah").setExecutor(new AuctionCommand(this));

        BanCommand banCommand = new BanCommand(this);
        getCommand("tempban").setExecutor(banCommand);
        getCommand("banip").setExecutor(banCommand);
        getCommand("unban").setExecutor(banCommand);
        getCommand("mute").setExecutor(banCommand);
        getCommand("unmute").setExecutor(banCommand);
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ProtectionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PvPListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GUIListener(this), this);
        Bukkit.getPluginManager().registerEvents(new EntityListener(this), this);
        Bukkit.getPluginManager().registerEvents(new JobListener(this), this);
        Bukkit.getPluginManager().registerEvents(new QuestListener(this), this);
        Bukkit.getPluginManager().registerEvents(new AntiCheatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new XRayListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ModerationListener(this), this);
        Bukkit.getPluginManager().registerEvents(new SpawnerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PowerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BanListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CommandListener(this), this);
    }

    private void startTasks() {
        // Task for Power Effects
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            Bukkit.getOnlinePlayers().forEach(p -> {
                powerManager.applyEffects(p);
                updateCompass(p);
            });
        }, 20, 20);

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

        // Auto-save every 5 minutes (Synchronous to avoid ConcurrentModificationException)
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            getLogger().info("Auto-sauvegarde des données...");
            dataManager.saveAll(factionManager, playerManager, claimManager);
        }, 6000L, 6000L);

        // Clear Lag Task (Every 60 minutes)
        startClearLagTask();
    }

    private void startClearLagTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            Bukkit.broadcastMessage("§c§l[ClearLag] §eNettoyage des items au sol dans 30 secondes...");

            Bukkit.getScheduler().runTaskLater(this, () -> {
                int count = 0;
                for (org.bukkit.World world : Bukkit.getWorlds()) {
                    for (org.bukkit.entity.Entity entity : world.getEntities()) {
                        if (entity instanceof org.bukkit.entity.Item) {
                            entity.remove();
                            count++;
                        }
                    }
                }
                Bukkit.broadcastMessage("§c§l[ClearLag] §aNettoyage terminé ! §e" + count + " §aitems supprimés.");
            }, 20 * 30L); // 30 seconds later

        }, 20 * 60 * 60L, 20 * 60 * 60L); // Every 60 minutes
    }

    @Override
    public void onDisable() {
        if (moduleManager != null) moduleManager.disableModules();
        if (auctionManager != null) auctionManager.save();
        if (factionManager != null) {
            factionManager.getAllFactions().forEach(dataManager::saveFaction);
        }
        if (playerManager != null) {
            playerManager.getAllPlayerData().forEach(dataManager::savePlayerData);
        }
        saveLocations();
        getLogger().info("TPC faction a été désactivé !");
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

    private void updateCompass(org.bukkit.entity.Player player) {
        PlayerData data = playerManager.getPlayerData(player.getUniqueId());
        if (data.getFactionId() != null) {
            Faction f = factionManager.getFaction(data.getFactionId());
            if (f != null && f.getHome() != null) {
                player.setCompassTarget(f.getHome());
            }
        }
    }
}
