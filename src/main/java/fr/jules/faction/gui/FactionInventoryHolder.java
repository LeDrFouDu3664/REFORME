package fr.jules.faction.gui;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

@Getter
public class FactionInventoryHolder implements InventoryHolder {
    private final String type;
    private final Object data;
    @Setter
    private Inventory inventory;

    public FactionInventoryHolder(String type, Object data) {
        this.type = type;
        this.data = data;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
