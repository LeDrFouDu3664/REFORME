package fr.jules.faction.commands;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.model.PlayerData;
import fr.jules.faction.gui.FactionGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class QuestCommand implements CommandExecutor {
    private final FactionPlugin plugin;

    public QuestCommand(FactionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        FactionGUI.openQuestsMenu(player, data, plugin.getQuestManager());
        return true;
    }
}
