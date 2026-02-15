package fr.jules.faction.manager;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.model.AuctionItem;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AuctionManager {
    private final FactionPlugin plugin;
    private final Map<UUID, AuctionItem> items = new ConcurrentHashMap<>();
    private final File file;

    public AuctionManager(FactionPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "auction.yml");
        load();
    }

    public void addItem(AuctionItem item) {
        items.put(item.getId(), item);
        save();
    }

    public void removeItem(UUID id) {
        items.remove(id);
        save();
    }

    public Collection<AuctionItem> getItems() {
        return items.values();
    }

    public AuctionItem getItem(UUID id) {
        return items.get(id);
    }

    public void load() {
        if (!file.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!config.contains("items")) return;

        for (String key : config.getConfigurationSection("items").getKeys(false)) {
            UUID id = UUID.fromString(key);
            UUID sellerId = UUID.fromString(config.getString("items." + key + ".sellerId"));
            String sellerName = config.getString("items." + key + ".sellerName");
            ItemStack itemStack = config.getItemStack("items." + key + ".item");
            double price = config.getDouble("items." + key + ".price");
            long expiry = config.getLong("items." + key + ".expiry");

            AuctionItem ai = new AuctionItem(sellerId, sellerName, itemStack, price);
            // Overwrite generated ID and expiry
            try {
                java.lang.reflect.Field idField = AuctionItem.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(ai, id);
                java.lang.reflect.Field expiryField = AuctionItem.class.getDeclaredField("expiry");
                expiryField.setAccessible(true);
                expiryField.set(ai, expiry);
            } catch (Exception e) { e.printStackTrace(); }

            if (expiry > System.currentTimeMillis()) {
                items.put(id, ai);
            }
        }
    }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        for (AuctionItem ai : items.values()) {
            String path = "items." + ai.getId().toString();
            config.set(path + ".sellerId", ai.getSellerId().toString());
            config.set(path + ".sellerName", ai.getSellerName());
            config.set(path + ".item", ai.getItem());
            config.set(path + ".price", ai.getPrice());
            config.set(path + ".expiry", ai.getExpiry());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
