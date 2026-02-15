package fr.jules.faction.manager;

import fr.jules.faction.FactionPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {
    private final FactionPlugin plugin;
    private Economy vaultEconomy = null;

    public EconomyManager(FactionPlugin plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    private boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        vaultEconomy = rsp.getProvider();
        return vaultEconomy != null;
    }

    public double getBalance(org.bukkit.entity.Player player) {
        if (vaultEconomy != null) return vaultEconomy.getBalance(player);
        return plugin.getPlayerManager().getPlayerData(player.getUniqueId()).getBalance();
    }

    public void withdraw(org.bukkit.entity.Player player, double amount) {
        if (vaultEconomy != null) vaultEconomy.withdrawPlayer(player, amount);
        else plugin.getPlayerManager().getPlayerData(player.getUniqueId()).removeBalance(amount);
    }

    public void deposit(org.bukkit.entity.Player player, double amount) {
        if (vaultEconomy != null) vaultEconomy.depositPlayer(player, amount);
        else plugin.getPlayerManager().getPlayerData(player.getUniqueId()).addBalance(amount);
    }

    public void deposit(org.bukkit.OfflinePlayer player, double amount) {
        if (vaultEconomy != null) vaultEconomy.depositPlayer(player, amount);
        else plugin.getPlayerManager().getPlayerData(player.getUniqueId()).addBalance(amount);
    }

    public boolean has(org.bukkit.entity.Player player, double amount) {
        if (vaultEconomy != null) return vaultEconomy.has(player, amount);
        return plugin.getPlayerManager().getPlayerData(player.getUniqueId()).getBalance() >= amount;
    }
}
