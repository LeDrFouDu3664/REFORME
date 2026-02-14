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
        // Quêtes quotidiennes équilibrées
        quests.put("MINER_JOB", new QuestInfo("Mineur Pro", "Miner 50 minerais précieux.", 50, 1500));
        quests.put("WOOD_JOB", new QuestInfo("Maître Bûcheron", "Couper 100 bûches.", 100, 1200));
        quests.put("FARM_JOB", new QuestInfo("Grand Fermier", "Récolter 200 cultures.", 200, 1000));
        quests.put("WARRIOR_JOB", new QuestInfo("Légende de Guerre", "Tuer 5 joueurs.", 5, 2500));
        quests.put("CLAIM_MASTER", new QuestInfo("Expansion", "Revendiquer 10 territoires.", 10, 2000));
        quests.put("KILL_MOBS", new QuestInfo("Chasseur", "Tuer 50 monstres.", 50, 800));
    }

    public void checkRefresh(Player player) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        long now = System.currentTimeMillis();
        // 24 heures = 86 400 000 ms
        if (now - data.getLastQuestRefresh() > 86400000) {
            data.getQuestProgress().clear();
            data.setLastQuestRefresh(now);
            player.sendMessage("§6§l[Quête] §aVos quêtes quotidiennes ont été renouvelées !");
        }
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
