package fr.jules.faction.manager;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.model.PlayerData;
import org.bukkit.entity.Player;
import java.util.*;

public class QuestManager {
    private final FactionPlugin plugin;
    private final Map<String, QuestInfo> quests = new LinkedHashMap<>();

    public QuestManager(FactionPlugin plugin) {
        this.plugin = plugin;
        setupQuests();
    }

    private void setupQuests() {
        quests.put("MINER_100", new QuestInfo("Mineur Débutant", "Miner 100 blocs de pierre ou minerais.", 100, 500));
        quests.put("KILL_10", new QuestInfo("Guerrier", "Tuer 10 joueurs.", 10, 2000));
        quests.put("CLAIM_5", new QuestInfo("Colon", "Revendiquer 5 territoires.", 5, 1000));
        quests.put("WOOD_50", new QuestInfo("Bûcheron Artisanal", "Couper 50 bûches.", 50, 300));
    }

    public void progressQuest(Player player, String questId, int amount) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        QuestInfo info = quests.get(questId);
        if (info == null) return;

        int current = data.getQuestProgress().getOrDefault(questId, 0);
        if (current >= info.goal) return;

        int next = current + amount;
        data.getQuestProgress().put(questId, next);

        if (next >= info.goal) {
            player.sendMessage("§6§l[Quête] §aFélicitations ! Vous avez terminé la quête : §e" + info.name);
            plugin.getEconomyManager().deposit(player, info.reward);
            player.sendMessage("§7Récompense : §e" + info.reward + "$");
        }
    }

    public Map<String, QuestInfo> getQuests() {
        return quests;
    }

    public static class QuestInfo {
        public final String name;
        public final String description;
        public final int goal;
        public final double reward;

        public QuestInfo(String name, String description, int goal, double reward) {
            this.name = name;
            this.description = description;
            this.goal = goal;
            this.reward = reward;
        }
    }
}
