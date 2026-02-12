package fr.jules.faction.commands;

import fr.jules.faction.FactionPlugin;
import fr.jules.faction.model.*;
import fr.jules.faction.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FactionCommand implements CommandExecutor {
    protected final FactionPlugin plugin;

    public FactionCommand(FactionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessageUtils.getMessage("only-players"));
            return true;
        }

        if (args.length == 0) {
            displayHelp(player);
            return true;
        }

        String sub = args[0].toLowerCase();
        String perm = "faction.command." + sub;
        if (!player.hasPermission(perm)) {
            MessageUtils.sendMessage(player, "no-permission", "%perm%", perm);
            return true;
        }

        switch (sub) {
            case "create": handleCreate(player, args); break;
            case "join": handleJoin(player, args); break;
            case "leave": handleLeave(player); break;
            case "disband": handleDisband(player); break;
            case "invite": case "i": handleInvite(player, args); break;
            case "kick": handleKick(player, args); break;
            case "promote": handlePromote(player, args); break;
            case "demote": handleDemote(player, args); break;
            case "leader": handleLeader(player, args); break;
            case "desc": handleDesc(player, args); break;
            case "motd": handleMotd(player, args); break;
            case "title": handleTitle(player, args); break;
            case "name": handleName(player, args); break;
            case "list": handleList(player); break;
            case "status": handleStatus(player); break;
            case "faction": case "f": handleFactionInfo(player, args); break;
            case "player": case "p": handlePlayerInfo(player, args); break;
            case "claim": handleClaim(player, args); break;
            case "unclaim": handleUnclaim(player, args); break;
            case "map": handleMap(player); break;
            case "seechunk": handleSeechunk(player); break;
            case "neutral": handleRelation(player, args, "NEUTRAL"); break;
            case "enemy": handleRelation(player, args, "ENEMY"); break;
            case "truce": handleRelation(player, args, "TRUCE"); break;
            case "ally": handleRelation(player, args, "ALLY"); break;
            case "chat": case "c": handleChat(player, args); break;
            case "gui": case "perm": handleGui(player); break;
            case "unstuck": handleUnstuck(player); break;
            case "help": displayHelp(player); break;
            case "admin": handleAdmin(player, args); break;
            case "sethome": handleFactionSetHome(player); break;
            case "unsethome": handleFactionUnsetHome(player); break;
            case "home": handleFactionHome(player, args); break;
            default:
                player.sendMessage("§cSous-commande inconnue.");
                break;
        }
        return true;
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUtilisation: /f create [nom]");
            return;
        }
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() != null) {
            MessageUtils.sendMessage(player, "already-in-faction");
            return;
        }
        String name = args[1];
        Faction faction = plugin.getFactionManager().createFaction(name, player.getUniqueId());
        if (faction == null) {
            MessageUtils.sendMessage(player, "name-taken");
            return;
        }
        data.setFactionId(faction.getId());
        data.setRole(Grade.LEADER);
        MessageUtils.sendMessage(player, "faction-created", "%name%", name);
    }

    private void handleJoin(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUtilisation: /f join [nom]");
            return;
        }
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() != null) {
            MessageUtils.sendMessage(player, "already-in-faction");
            return;
        }
        Faction faction = plugin.getFactionManager().getFactionByName(args[1]);
        if (faction == null) {
            MessageUtils.sendMessage(player, "faction-not-found");
            return;
        }
        if (!faction.getInvites().contains(player.getUniqueId())) {
            if (faction.getRequests().contains(player.getUniqueId())) {
                player.sendMessage("§cVous avez déjà envoyé une demande à cette faction.");
            } else {
                faction.getRequests().add(player.getUniqueId());
                player.sendMessage("§aDemande de rejoindre envoyée à " + faction.getName() + ".");
            }
            return;
        }
        faction.getInvites().remove(player.getUniqueId());
        faction.addMember(player.getUniqueId());
        data.setFactionId(faction.getId());
        data.setRole(Grade.MEMBER);
        MessageUtils.sendMessage(player, "joined-faction", "%name%", faction.getName());
    }

    private void handleLeave(Player player) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) {
            MessageUtils.sendMessage(player, "not-in-faction");
            return;
        }
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (faction.getLeader().equals(player.getUniqueId())) {
            player.sendMessage("§cLe chef ne peut pas quitter sa faction.");
            return;
        }
        faction.removeMember(player.getUniqueId());
        data.setFactionId(null);
        data.setRole(Grade.MEMBER);
        MessageUtils.sendMessage(player, "left-faction");
    }

    private void handleDisband(Player player) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) {
            MessageUtils.sendMessage(player, "not-in-faction");
            return;
        }
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!faction.getLeader().equals(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "not-leader");
            return;
        }
        for (UUID memberId : faction.getMembers()) {
            PlayerData memberData = plugin.getPlayerManager().getPlayerData(memberId);
            memberData.setFactionId(null);
            memberData.setRole(Grade.MEMBER);
        }
        plugin.getClaimManager().removeAllFactionClaims(faction.getId());
        plugin.getFactionManager().disbandFaction(faction.getId());
        MessageUtils.sendMessage(player, "faction-disbanded");
    }

    private void handleInvite(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUtilisation: /f invite add/revoke [pseudo]");
            return;
        }
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) {
            MessageUtils.sendMessage(player, "not-in-faction");
            return;
        }
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!faction.hasPermission(data.getRole(), "INVITE")) {
            MessageUtils.sendMessage(player, "no-permission", "%perm%", "INVITE (Faction)");
            return;
        }

        if (args.length < 3) {
            player.sendMessage("§cPrécisez un pseudo.");
            return;
        }
        String action = args[1].toLowerCase();
        String targetName = args[2];

        if (action.equals("add")) {
            Player target = Bukkit.getPlayer(targetName);
            UUID targetUUID = target != null ? target.getUniqueId() : Bukkit.getOfflinePlayer(targetName).getUniqueId();
            faction.getInvites().add(targetUUID);
            MessageUtils.sendMessage(player, "invited", "%target%", targetName);
            if (target != null) MessageUtils.sendMessage(target, "invite-received", "%name%", faction.getName());
        } else if (action.equals("revoke")) {
            if (targetName.equalsIgnoreCase("all")) {
                faction.getInvites().clear();
                player.sendMessage("§aToutes les invitations révoquées.");
            } else {
                UUID targetUUID = Bukkit.getOfflinePlayer(targetName).getUniqueId();
                faction.getInvites().remove(targetUUID);
                player.sendMessage("§aInvitation révoquée pour " + targetName + ".");
            }
        }
    }

    private void handleKick(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUtilisation: /f kick [pseudo]");
            return;
        }
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) {
            MessageUtils.sendMessage(player, "not-in-faction");
            return;
        }
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!faction.hasPermission(data.getRole(), "KICK")) {
            MessageUtils.sendMessage(player, "no-permission", "%perm%", "KICK (Faction)");
            return;
        }

        String targetName = args[1];
        UUID targetUUID = Bukkit.getOfflinePlayer(targetName).getUniqueId();
        if (!faction.getMembers().contains(targetUUID)) {
            MessageUtils.sendMessage(player, "target-not-in-faction");
            return;
        }
        if (targetUUID.equals(faction.getLeader())) {
            MessageUtils.sendMessage(player, "cannot-kick-leader");
            return;
        }

        faction.removeMember(targetUUID);
        PlayerData targetData = plugin.getPlayerManager().getPlayerData(targetUUID);
        targetData.setFactionId(null);
        targetData.setRole(Grade.MEMBER);
        MessageUtils.sendMessage(player, "kicked", "%target%", targetName);
        Player target = Bukkit.getPlayer(targetUUID);
        if (target != null) MessageUtils.sendMessage(target, "kicked-received");
    }

    private void handlePromote(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUtilisation: /f promote [pseudo]");
            return;
        }
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) {
            MessageUtils.sendMessage(player, "not-in-faction");
            return;
        }
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!faction.hasPermission(data.getRole(), "PROMOTE")) {
            MessageUtils.sendMessage(player, "no-permission", "%perm%", "PROMOTE (Faction)");
            return;
        }

        String targetName = args[1];
        UUID targetUUID = Bukkit.getOfflinePlayer(targetName).getUniqueId();
        if (!faction.getMembers().contains(targetUUID)) {
            MessageUtils.sendMessage(player, "target-not-in-faction");
            return;
        }

        PlayerData targetData = plugin.getPlayerManager().getPlayerData(targetUUID);
        if (targetData.getRole() == Grade.MEMBER) {
            targetData.setRole(Grade.OFFICER);
            faction.getOfficers().add(targetUUID);
            MessageUtils.sendMessage(player, "promoted", "%target%", targetName);
        } else {
            player.sendMessage("§cCe joueur ne peut pas être promu davantage ici.");
        }
    }

    private void handleDemote(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUtilisation: /f demote [pseudo]");
            return;
        }
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) {
            MessageUtils.sendMessage(player, "not-in-faction");
            return;
        }
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!faction.hasPermission(data.getRole(), "DEMOTE")) {
            MessageUtils.sendMessage(player, "no-permission", "%perm%", "DEMOTE (Faction)");
            return;
        }

        String targetName = args[1];
        UUID targetUUID = Bukkit.getOfflinePlayer(targetName).getUniqueId();
        if (!faction.getMembers().contains(targetUUID)) {
            MessageUtils.sendMessage(player, "target-not-in-faction");
            return;
        }

        PlayerData targetData = plugin.getPlayerManager().getPlayerData(targetUUID);
        if (targetData.getRole() == Grade.OFFICER) {
            targetData.setRole(Grade.MEMBER);
            faction.getOfficers().remove(targetUUID);
            MessageUtils.sendMessage(player, "demoted", "%target%", targetName);
        } else {
            player.sendMessage("§cCe joueur est déjà au grade le plus bas.");
        }
    }

    private void handleOfficer(Player player, String[] args) { handlePromote(player, args); }

    private void handleLeader(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUtilisation: /f leader [pseudo]");
            return;
        }
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) {
            MessageUtils.sendMessage(player, "not-in-faction");
            return;
        }
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!faction.getLeader().equals(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "not-leader");
            return;
        }

        String targetName = args[1];
        UUID targetUUID = Bukkit.getOfflinePlayer(targetName).getUniqueId();
        if (!faction.getMembers().contains(targetUUID)) {
            MessageUtils.sendMessage(player, "target-not-in-faction");
            return;
        }

        faction.setLeader(targetUUID);
        faction.getOfficers().add(player.getUniqueId());
        data.setRole(Grade.OFFICER);
        PlayerData targetData = plugin.getPlayerManager().getPlayerData(targetUUID);
        targetData.setRole(Grade.LEADER);
        MessageUtils.sendMessage(player, "new-leader", "%target%", targetName);
    }

    private void handleDesc(Player player, String[] args) {
        if (args.length < 2) return;
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) return;
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!faction.hasPermission(data.getRole(), "DESC")) {
            MessageUtils.sendMessage(player, "no-permission", "%perm%", "DESC (Faction)");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < args.length; i++) sb.append(args[i]).append(" ");
        faction.setDescription(sb.toString().trim());
        player.sendMessage("§aDescription mise à jour.");
    }

    private void handleMotd(Player player, String[] args) {
        if (args.length < 2) return;
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) return;
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!faction.hasPermission(data.getRole(), "MOTD")) {
            MessageUtils.sendMessage(player, "no-permission", "%perm%", "MOTD (Faction)");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < args.length; i++) sb.append(args[i]).append(" ");
        faction.setMotd(sb.toString().trim());
        player.sendMessage("§aMOTD mis à jour.");
    }

    private void handleTitle(Player player, String[] args) {
        if (args.length < 3) return;
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) return;
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!faction.hasPermission(data.getRole(), "TITLE")) {
            MessageUtils.sendMessage(player, "no-permission", "%perm%", "TITLE (Faction)");
            return;
        }
        String title = args[1].replace('&', '§');
        PlayerData targetData = plugin.getPlayerManager().getPlayerDataByName(args[2]);
        if (targetData != null) {
            targetData.setTitle(title);
            player.sendMessage("§aTitre mis à jour.");
        }
    }

    private void handleName(Player player, String[] args) {
        if (args.length < 2) return;
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) return;
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!faction.hasPermission(data.getRole(), "RENAME")) {
            MessageUtils.sendMessage(player, "no-permission", "%perm%", "RENAME (Faction)");
            return;
        }
        String newName = args[1];
        if (plugin.getFactionManager().getFactionByName(newName) != null) {
            MessageUtils.sendMessage(player, "name-taken");
            return;
        }
        plugin.getFactionManager().renameFaction(faction, newName);
        MessageUtils.sendMessage(player, "renamed-faction", "%name%", newName);
    }

    private void handleList(Player player) {
        List<Faction> factions = new ArrayList<>(plugin.getFactionManager().getAllFactions());
        factions.sort((f1, f2) -> Double.compare(f2.getPower(), f1.getPower()));
        player.sendMessage("§6--- Liste des Factions ---");
        for (int i = 0; i < factions.size(); i++) {
            Faction f = factions.get(i);
            player.sendMessage("§e" + (i + 1) + ". " + f.getName() + " §7(Power: " + String.format("%.1f", f.getPower()) + ")");
        }
    }

    private void handleStatus(Player player) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) {
            MessageUtils.sendMessage(player, "not-in-faction");
            return;
        }
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        player.sendMessage("§6--- Statut: " + faction.getName() + " ---");
        for (UUID mid : faction.getMembers()) {
            PlayerData md = plugin.getPlayerManager().getPlayerData(mid);
            String status = Bukkit.getPlayer(mid) != null ? "§a[On]" : "§c[Off]";
            player.sendMessage("§e" + md.getName() + " §7- Power: " + String.format("%.1f", md.getPower()) + " " + status);
        }
    }

    private void handleFactionInfo(Player player, String[] args) {
        Faction f = args.length < 2 ? (plugin.getFactionManager().getFaction(plugin.getPlayerManager().getPlayerData(player.getUniqueId()).getFactionId())) : plugin.getFactionManager().getFactionByName(args[1]);
        if (f == null) { MessageUtils.sendMessage(player, "faction-not-found"); return; }
        player.sendMessage("§6--- Info: " + f.getName() + " ---");
        player.sendMessage("§eChef: §f" + Bukkit.getOfflinePlayer(f.getLeader()).getName());
        player.sendMessage("§ePower: §f" + String.format("%.1f", f.getPower()));
        player.sendMessage("§eClaims: §f" + f.getClaims().size());
    }

    private void handlePlayerInfo(Player player, String[] args) {
        String name = args.length < 2 ? player.getName() : args[1];
        PlayerData pd = plugin.getPlayerManager().getPlayerDataByName(name);
        if (pd == null) { player.sendMessage("§cJoueur inconnu."); return; }
        player.sendMessage("§6--- Info: " + pd.getName() + " ---");
        player.sendMessage("§eGrade: §f" + pd.getRole().name());
        player.sendMessage("§ePower: §f" + String.format("%.1f", pd.getPower()));
    }

    private void handleClaim(Player player, String[] args) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) { MessageUtils.sendMessage(player, "not-in-faction"); return; }
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!faction.hasPermission(data.getRole(), "CLAIM")) { MessageUtils.sendMessage(player, "no-permission", "%perm%", "CLAIM (Faction)"); return; }
        if (args.length > 1 && args[1].equalsIgnoreCase("auto")) {
            data.setAutoClaim(!data.isAutoClaim());
            player.sendMessage("§aAuto-claim: " + (data.isAutoClaim() ? "§aOn" : "§cOff"));
            return;
        }
        performClaim(player, faction, player.getWorld().getName(), player.getLocation().getChunk().getX(), player.getLocation().getChunk().getZ());
    }

    public void performClaim(Player player, Faction faction, String world, int x, int z) {
        if (plugin.getClaimManager().isClaimed(world, x, z)) { MessageUtils.sendMessage(player, "claim-already-owned"); return; }
        if (faction.getPower() < faction.getClaims().size() + 1) { MessageUtils.sendMessage(player, "claim-not-enough-power"); return; }
        Claim c = new Claim(world, x, z, faction.getId());
        plugin.getClaimManager().addClaim(c);
        faction.getClaims().add(c.toString());
        MessageUtils.sendMessage(player, "claim-success");
    }

    private void handleUnclaim(Player player, String[] args) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) { MessageUtils.sendMessage(player, "not-in-faction"); return; }
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!faction.hasPermission(data.getRole(), "UNCLAIM")) { MessageUtils.sendMessage(player, "no-permission", "%perm%", "UNCLAIM (Faction)"); return; }
        if (args.length > 1 && args[1].equalsIgnoreCase("all")) {
            for (String s : new ArrayList<>(faction.getClaims())) {
                Claim c = Claim.fromString(s);
                plugin.getClaimManager().removeClaim(c.getWorld(), c.getX(), c.getZ());
            }
            faction.getClaims().clear();
            MessageUtils.sendMessage(player, "unclaim-all");
            return;
        }
        String w = player.getWorld().getName(); int x = player.getLocation().getChunk().getX(), z = player.getLocation().getChunk().getZ();
        Claim c = plugin.getClaimManager().getClaim(w, x, z);
        if (c == null || !c.getFactionId().equals(faction.getId())) { MessageUtils.sendMessage(player, "not-your-claim"); return; }
        plugin.getClaimManager().removeClaim(w, x, z);
        faction.getClaims().remove(c.toString());
        MessageUtils.sendMessage(player, "unclaim-success");
    }

    private void handleMap(Player player) {
        int rx = 12, rz = 6;
        player.sendMessage("§6--- Carte ---");
        int px = player.getLocation().getChunk().getX(), pz = player.getLocation().getChunk().getZ();
        String w = player.getWorld().getName();
        for (int z = pz - rz; z <= pz + rz; z++) {
            net.kyori.adventure.text.TextComponent.Builder line = net.kyori.adventure.text.Component.text();
            for (int x = px - rx; x <= px + rx; x++) {
                if (x == px && z == pz) line.append(net.kyori.adventure.text.Component.text("§b+"));
                else {
                    Claim c = plugin.getClaimManager().getClaim(w, x, z);
                    if (c == null) line.append(net.kyori.adventure.text.Component.text("§7-"));
                    else {
                        Faction o = plugin.getFactionManager().getFaction(c.getFactionId());
                        line.append(net.kyori.adventure.text.Component.text("§e#").hoverEvent(net.kyori.adventure.text.Component.text("§6Faction: §e" + (o != null ? o.getName() : "???"))));
                    }
                }
            }
            player.sendMessage(line.build());
        }
    }

    private void handleSeechunk(Player player) {
        org.bukkit.Chunk chunk = player.getLocation().getChunk();
        int mx = chunk.getX() * 16, mz = chunk.getZ() * 16;
        for (int y = player.getLocation().getBlockY() - 2; y < player.getLocation().getBlockY() + 5; y++) {
            for (int x = mx; x <= mx + 16; x += 16) for (int z = mz; z <= mz + 16; z++) player.spawnParticle(org.bukkit.Particle.REDSTONE, x, y, z, 1, new org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 1));
            for (int z = mz; z <= mz + 16; z += 16) for (int x = mx; x <= mx + 16; x++) player.spawnParticle(org.bukkit.Particle.REDSTONE, x, y, z, 1, new org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 1));
        }
        player.sendMessage("§eLimites affichées.");
    }

    private void handleRelation(Player player, String[] args, String rel) {
        if (args.length < 2) return;
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) return;
        Faction f = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!f.isOfficer(player.getUniqueId())) return;
        Faction t = plugin.getFactionManager().getFactionByName(args[1]);
        if (t == null) return;
        f.getRelations().put(t.getId(), rel);
        MessageUtils.sendMessage(player, "relation-updated", "%target%", t.getName(), "%relation%", rel);
    }

    private void handleChat(Player player, String[] args) {
        if (args.length < 2) return;
        PlayerData pd = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        String m = args[1].toLowerCase();
        if (m.startsWith("f")) pd.setChatMode("FACTION");
        else if (m.startsWith("t")) pd.setChatMode("TRUCE");
        else if (m.startsWith("a")) pd.setChatMode("ALLY");
        else pd.setChatMode("PUBLIC");
        MessageUtils.sendMessage(player, "chat-mode-switched", "%mode%", pd.getChatMode());
    }

    private void handleGui(Player player) {
        PlayerData pd = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (pd.getFactionId() == null) return;
        Faction f = plugin.getFactionManager().getFaction(pd.getFactionId());
        fr.jules.faction.gui.FactionGUI.openMainMenu(player, f);
    }

    private void handleUnstuck(Player player) { player.teleport(player.getWorld().getSpawnLocation()); }

    private void handleAdmin(Player player, String[] args) {
        if (!player.hasPermission("faction.admin")) { MessageUtils.sendMessage(player, "no-permission", "%perm%", "faction.admin"); return; }
        if (args.length < 2) return;
        String a = args[1].toLowerCase();
        if (a.equals("bypass")) { PlayerData pd = plugin.getPlayerManager().getPlayerData(player.getUniqueId()); pd.setBypass(!pd.isBypass()); player.sendMessage("§aBypass: " + pd.isBypass()); }
    }

    private void handleFactionSetHome(Player player) {
        PlayerData pd = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (pd.getFactionId() == null) return;
        Faction f = plugin.getFactionManager().getFaction(pd.getFactionId());
        if (!f.hasPermission(pd.getRole(), "SETHOME")) { MessageUtils.sendMessage(player, "no-permission", "%perm%", "SETHOME (Faction)"); return; }
        f.setHome(player.getLocation()); MessageUtils.sendMessage(player, "faction-home-set");
    }

    private void handleFactionUnsetHome(Player player) {
        PlayerData pd = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (pd.getFactionId() == null) return;
        Faction f = plugin.getFactionManager().getFaction(pd.getFactionId());
        if (!f.hasPermission(pd.getRole(), "UNSETHOME")) { MessageUtils.sendMessage(player, "no-permission", "%perm%", "UNSETHOME (Faction)"); return; }
        f.setHome(null); MessageUtils.sendMessage(player, "faction-home-unset");
    }

    private void handleFactionHome(Player player, String[] args) {
        PlayerData pd = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        Faction f = args.length < 2 ? (pd.getFactionId() != null ? plugin.getFactionManager().getFaction(pd.getFactionId()) : null) : plugin.getFactionManager().getFactionByName(args[1]);
        if (f == null || f.getHome() == null) return;
        boolean ok = pd.getFactionId() != null && (pd.getFactionId().equals(f.getId()) || ("ALLY".equals(f.getRelations().get(pd.getFactionId())) && f.getFlags().getOrDefault("ALLY_HOME", true)));
        if (!ok) { MessageUtils.sendMessage(player, "no-permission"); return; }
        player.teleport(f.getHome()); MessageUtils.sendMessage(player, "faction-home-teleport", "%name%", f.getName());
    }

    private void displayHelp(Player player) {
        player.sendMessage("§6--- Aide Faction ---");
        player.sendMessage("§e/f create/join/leave/disband");
        player.sendMessage("§e/f invite/kick/promote/demote/leader");
        player.sendMessage("§e/f claim/unclaim/map/seechunk");
        player.sendMessage("§e/f home/sethome/unsethome");
        player.sendMessage("§e/f chat/gui/status/faction/player");
    }
}
