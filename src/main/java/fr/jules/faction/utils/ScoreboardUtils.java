package fr.jules.faction.utils;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.model.Faction;
import fr.jules.faction.model.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class ScoreboardUtils {
    public static void updateScoreboard(FactionPlugin plugin, Player player) {
        Scoreboard board = player.getScoreboard();
        if (board == Bukkit.getScoreboardManager().getMainScoreboard()) {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
            player.setScoreboard(board);
        }

        Objective obj = board.getObjective("faction");
        if (obj != null) obj.unregister();

        obj = board.registerNewObjective("faction", Criteria.DUMMY, "§c§lFACTION");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        Faction faction = data.getFactionId() != null ? plugin.getFactionManager().getFaction(data.getFactionId()) : null;

        int line = 10;
        obj.getScore("§7" + player.getName()).setScore(line--);
        obj.getScore("§1").setScore(line--);

        obj.getScore("§fArgent: §a" + String.format("%.0f", plugin.getEconomyManager().getBalance(player)) + "$").setScore(line--);
        obj.getScore("§fPower: §b" + String.format("%.1f", data.getPower())).setScore(line--);
        obj.getScore("§2").setScore(line--);

        if (faction != null) {
            obj.getScore("§fFaction: §e" + faction.getName()).setScore(line--);
            obj.getScore("§fNiveau Fac: §6" + faction.getLevel()).setScore(line--);
            obj.getScore("§fPower Fac: §b" + String.format("%.0f", faction.getPower())).setScore(line--);
        } else {
            obj.getScore("§7Pas de faction").setScore(line--);
        }

        obj.getScore("§3").setScore(line--);
        obj.getScore("§fMétier: §e" + data.getJob()).setScore(line--);
        obj.getScore("§fNiveau Job: §a" + data.getJobLevel()).setScore(line--);
        obj.getScore("§4").setScore(line--);
        obj.getScore("§7play.monserveur.fr").setScore(line--);

        player.setScoreboard(board);
    }
}
