package fr.jules.faction.gui;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Getter
public class FactionInventoryHolder implements InventoryHolder {
    private final String type;
    private final Object data;

    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }
}
