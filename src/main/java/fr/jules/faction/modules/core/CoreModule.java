package fr.jules.faction.modules.core;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.modules.Module;
import org.bukkit.configuration.ConfigurationSection;

public class CoreModule extends Module {

    public CoreModule(FactionPlugin plugin) {
        super(plugin, "Core");
    }

    @Override
    public void onEnable() {
        loadQuests();
    }

    @Override
    public void onDisable() {}

    private void loadQuests() {
        ConfigurationSection questSection = config.getConfigurationSection("quests.list");
        if (questSection == null) return;

        plugin.getQuestManager().getQuests().clear();
        for (String key : questSection.getKeys(false)) {
            String name = questSection.getString(key + ".name");
            String desc = questSection.getString(key + ".desc");
            int goal = questSection.getInt(key + ".goal");
            double reward = questSection.getDouble(key + ".reward");
            plugin.getQuestManager().getQuests().put(key, new fr.jules.faction.manager.QuestManager.QuestInfo(name, desc, goal, reward));
        }
    }
}
