package fr.jules.faction.model;

import lombok.Data;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

@Data
public class AuctionItem {
    private final UUID id;
    private final UUID sellerId;
    private final String sellerName;
    private final ItemStack item;
    private final double price;
    private final long expiry;

    public AuctionItem(UUID sellerId, String sellerName, ItemStack item, double price) {
        this.id = UUID.randomUUID();
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.item = item;
        this.price = price;
        this.expiry = System.currentTimeMillis() + (48 * 60 * 60 * 1000); // 48 hours
    }
}
