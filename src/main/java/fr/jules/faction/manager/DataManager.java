package fr.jules.faction.manager;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.model.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DataManager {
    private final FactionPlugin plugin;
    private final File factionDir;
    private final File playerDir;

    public DataManager(FactionPlugin plugin) {
        this.plugin = plugin;
        this.factionDir = new File(plugin.getDataFolder(), "factions");
        this.playerDir = new File(plugin.getDataFolder(), "players");
        if (!factionDir.exists()) factionDir.mkdirs();
        if (!playerDir.exists()) playerDir.mkdirs();
    }

    public void saveFaction(Faction faction) {
        File file = new File(factionDir, faction.getId().toString() + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("id", faction.getId().toString());
        config.set("name", faction.getName());
        config.set("tag", faction.getTag());
        config.set("description", faction.getDescription());
        config.set("motd", faction.getMotd());
        config.set("leader", faction.getLeader().toString());
        config.set("members", faction.getMembers().stream().map(UUID::toString).toList());
        config.set("officers", faction.getOfficers().stream().map(UUID::toString).toList());
        config.set("invites", faction.getInvites().stream().map(UUID::toString).toList());
        config.set("requests", faction.getRequests().stream().map(UUID::toString).toList());
        config.set("home", faction.getHome());
        config.set("power", faction.getPower());
        config.set("level", faction.getLevel());
        config.set("exp", faction.getExp());
        config.set("claims", new ArrayList<>(faction.getClaims()));

        Map<String, String> relations = new HashMap<>();
        faction.getRelations().forEach((k, v) -> relations.put(k.toString(), v));
        config.set("relations", relations);

        Map<String, List<String>> perms = new HashMap<>();
        faction.getPermissions().forEach((k, v) -> perms.put(k, v.stream().map(Grade::name).toList()));
        config.set("permissions", perms);
        config.set("flags", faction.getFactionFlags());
        config.set("balance", faction.getBalance());
        config.set("tntStock", faction.getTntStock());
        config.set("type", faction.getType().name());

        if (faction.getFactionChest() != null) {
            config.set("chest", faction.getFactionChest().getContents());
        }

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFactions(FactionManager factionManager, ClaimManager claimManager) {
        File[] files = factionDir.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (!file.getName().endsWith(".yml")) continue;
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            UUID id = UUID.fromString(config.getString("id"));
            Faction faction = new Faction(id);
            faction.setName(config.getString("name"));
            faction.setTag(config.getString("tag"));
            faction.setDescription(config.getString("description"));
            faction.setMotd(config.getString("motd"));
            faction.setLeader(UUID.fromString(config.getString("leader")));

            List<String> members = config.getStringList("members");
            members.forEach(m -> faction.getMembers().add(UUID.fromString(m)));

            List<String> officers = config.getStringList("officers");
            officers.forEach(o -> faction.getOfficers().add(UUID.fromString(o)));

            List<String> invites = config.getStringList("invites");
            invites.forEach(i -> faction.getInvites().add(UUID.fromString(i)));

            List<String> requests = config.getStringList("requests");
            requests.forEach(r -> faction.getRequests().add(UUID.fromString(r)));

            faction.setHome(config.getLocation("home"));
            faction.setPower(config.getDouble("power"));
            faction.setLevel(config.getInt("level", 1));
            faction.setExp(config.getDouble("exp", 0));

            List<String> claims = config.getStringList("claims");
            faction.getClaims().addAll(claims);
            for (String claimStr : claims) {
                Claim claim = Claim.fromString(claimStr);
                claim.setFactionId(id);
                claimManager.addClaim(claim);
            }

            ConfigurationSection relSection = config.getConfigurationSection("relations");
            if (relSection != null) {
                for (String key : relSection.getKeys(false)) {
                    faction.getRelations().put(UUID.fromString(key), relSection.getString(key));
                }
            }

            ConfigurationSection permSection = config.getConfigurationSection("permissions");
            if (permSection != null) {
                for (String key : permSection.getKeys(false)) {
                    Set<Grade> grades = new HashSet<>();
                    permSection.getStringList(key).forEach(g -> grades.add(Grade.valueOf(g)));
                    faction.getPermissions().put(key, grades);
                }
            }

            ConfigurationSection flagSection = config.getConfigurationSection("flags");
            if (flagSection != null) {
                for (String key : flagSection.getKeys(false)) {
                    faction.getFactionFlags().put(key, flagSection.getBoolean(key));
                }
            }
            faction.setBalance(config.getDouble("balance", 0));
            faction.setTntStock(config.getInt("tntStock", 0));
            faction.setType(FactionType.valueOf(config.getString("type", "NORMAL")));

            if (config.contains("chest")) {
                List<ItemStack> items = (List<ItemStack>) config.getList("chest");
                Inventory chest = Bukkit.createInventory(null, 54, "§c§lCoffre de Faction: " + faction.getName());
                if (items != null) {
                    chest.setContents(items.toArray(new ItemStack[0]));
                }
                faction.setFactionChest(chest);
            }

            factionManager.addFaction(faction);
        }
    }

    public void savePlayerData(PlayerData data) {
        File file = new File(playerDir, data.getUuid().toString() + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("uuid", data.getUuid().toString());
        config.set("name", data.getName());
        config.set("factionId", data.getFactionId() != null ? data.getFactionId().toString() : null);
        config.set("role", data.getRole().name());
        config.set("power", data.getPower());
        config.set("maxPower", data.getMaxPower());
        config.set("title", data.getTitle());
        config.set("lastJoin", data.getLastJoin());
        config.set("job", data.getJob());
        config.set("jobExp", data.getJobExp());
        config.set("jobLevel", data.getJobLevel());
        config.set("questProgress", data.getQuestProgress());
        config.set("powers", new ArrayList<>(data.getPowers()));
        config.set("ownedPets", new ArrayList<>(data.getOwnedPets()));
        config.set("petCooldown", data.getPetCooldown());
        config.set("showTitles", data.isShowTitles());
        config.set("powerBoost", data.getPowerBoost());
        config.set("ignoredPlayers", data.getIgnoredPlayers().stream().map(UUID::toString).toList());

        ConfigurationSection homesSection = config.createSection("homes");
        data.getHomes().forEach(homesSection::set);

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PlayerData loadPlayerData(UUID uuid) {
        File file = new File(playerDir, uuid.toString() + ".yml");
        if (!file.exists()) return null;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        PlayerData data = new PlayerData(uuid);
        data.setName(config.getString("name"));
        String facIdStr = config.getString("factionId");
        if (facIdStr != null) data.setFactionId(UUID.fromString(facIdStr));
        data.setRole(Grade.valueOf(config.getString("role", "MEMBER")));
        data.setPower(config.getDouble("power", 10.0));
        data.setMaxPower(config.getDouble("maxPower", 10.0));
        data.setTitle(config.getString("title", ""));
        data.setLastJoin(config.getLong("lastJoin"));
        data.setJob(config.getString("job", "NONE"));
        data.setJobExp(config.getDouble("jobExp", 0));
        data.setJobLevel(config.getInt("jobLevel", 1));
        data.getPowers().addAll(config.getStringList("powers"));
        data.getOwnedPets().addAll(config.getStringList("ownedPets"));
        data.setPetCooldown(config.getLong("petCooldown", 0));
        data.setShowTitles(config.getBoolean("showTitles", true));
        ConfigurationSection questSec = config.getConfigurationSection("questProgress");
        if (questSec != null) {
            for (String key : questSec.getKeys(false)) {
                data.getQuestProgress().put(key, questSec.getInt(key));
            }
        }
        data.setPowerBoost(config.getDouble("powerBoost", 0));

        List<String> ignored = config.getStringList("ignoredPlayers");
        ignored.forEach(i -> data.getIgnoredPlayers().add(UUID.fromString(i)));

        ConfigurationSection homesSection = config.getConfigurationSection("homes");
        if (homesSection != null) {
            for (String key : homesSection.getKeys(false)) {
                data.getHomes().put(key, homesSection.getLocation(key));
            }
        }
        return data;
    }

    public void saveAll(FactionManager fm, PlayerManager pm, ClaimManager cm) {
        fm.getAllFactions().forEach(this::saveFaction);
        pm.getAllPlayerData().forEach(this::savePlayerData);
    }

    public void deleteFaction(UUID id) {
        File file = new File(factionDir, id.toString() + ".yml");
        if (file.exists()) file.delete();
    }
}
