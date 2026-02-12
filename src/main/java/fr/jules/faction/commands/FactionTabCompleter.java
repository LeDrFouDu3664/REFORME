package fr.jules.faction.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FactionTabCompleter implements TabCompleter {
    private final List<String> subCommands = Arrays.asList(
            "create", "join", "leave", "disband", "invite", "kick", "promote", "demote", "leader",
            "claim", "unclaim", "map", "status", "faction", "player", "home", "sethome", "unsethome",
            "chat", "gui", "help", "seechunk", "unstuck", "admin"
    );

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return subCommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .filter(s -> sender.hasPermission("faction.command." + s))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("invite")) return Arrays.asList("add", "revoke");
            if (sub.equals("claim") || sub.equals("unclaim")) return Arrays.asList("one", "all", "auto");
            if (sub.equals("admin")) return Arrays.asList("disband", "bypass", "setpower");
            if (sub.equals("chat")) return Arrays.asList("f", "t", "a", "p");
        }

        return new ArrayList<>();
    }
}
