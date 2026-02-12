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
            case "description": case "desc": handleDesc(player, args); break;
            case "motd": handleMotd(player, args); break;
            case "title": handleTitle(player, args); break;
            case "tag": case "name": handleName(player, args); break;
            case "list": handleList(player); break;
            case "status": handleStatus(player); break;
            case "show": case "who": case "faction": case "f": handleFactionInfo(player, args); break;
            case "player": handlePlayerInfo(player, args); break;
            case "power": case "p": handlePower(player, args); break;
            case "claim": case "c": handleClaim(player, args); break;
            case "unclaim": handleUnclaim(player, args); break;
            case "claims": handleClaimsCount(player); break;
            case "map": handleMap(player); break;
            case "seechunk": handleSeechunk(player); break;
            case "neutral": handleRelation(player, args, "NEUTRAL"); break;
            case "enemy": handleRelation(player, args, "ENEMY"); break;
            case "truce": handleRelation(player, args, "TRUCE"); break;
            case "ally": handleRelation(player, args, "ALLY"); break;
            case "chat": handleChat(player, args); break;
            case "gui": case "perm": handleGui(player); break;
            case "unstuck": handleUnstuck(player); break;
            case "help": displayHelp(player); break;
            case "admin": handleAdmin(player, args); break;
            case "sethome": handleFactionSetHome(player); break;
            case "unsethome": handleFactionUnsetHome(player); break;
            case "home": case "h": handleFactionHome(player, args); break;
            case "tnt": handleTnt(player, args); break;
            case "money": handleMoney(player, args); break;
            case "balance": handleBalance(player); break;
            case "flag": handleFlag(player, args); break;
            case "safezone": handleSpecialZone(player, FactionType.SAFEZONE); break;
            case "warzone": handleSpecialZone(player, FactionType.WARZONE); break;
            case "wilderness": handleSpecialZone(player, FactionType.WILDERNESS); break;
            case "setpower": handleSetPower(player, args); break;
            case "reload": handleReload(player); break;
            default:
                player.sendMessage("§cSous-commande inconnue.");
                break;
        }
        return true;
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) { player.sendMessage("§cUtilisation: /f create [nom]"); return; }
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() != null) { MessageUtils.sendMessage(player, "already-in-faction"); return; }
        String name = args[1];
        Faction faction = plugin.getFactionManager().createFaction(name, player.getUniqueId());
        if (faction == null) { MessageUtils.sendMessage(player, "name-taken"); return; }
        data.setFactionId(faction.getId());
        data.setRole(Grade.LEADER);
        MessageUtils.sendMessage(player, "faction-created", "%name%", name);
    }

    private void handleJoin(Player player, String[] args) {
        if (args.length < 2) { player.sendMessage("§cUtilisation: /f join [nom]"); return; }
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() != null) { MessageUtils.sendMessage(player, "already-in-faction"); return; }
        Faction faction = plugin.getFactionManager().getFactionByName(args[1]);
        if (faction == null) { MessageUtils.sendMessage(player, "faction-not-found"); return; }
        if (!faction.getInvites().contains(player.getUniqueId())) {
            if (faction.getRequests().contains(player.getUniqueId())) { player.sendMessage("§cDemande déjà envoyée."); }
            else { faction.getRequests().add(player.getUniqueId()); player.sendMessage("§aDemande envoyée."); }
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
        if (data.getFactionId() == null) { MessageUtils.sendMessage(player, "not-in-faction"); return; }
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (faction.getLeader().equals(player.getUniqueId())) { player.sendMessage("§cLe chef ne peut pas quitter."); return; }
        faction.removeMember(player.getUniqueId());
        data.setFactionId(null);
        data.setRole(Grade.MEMBER);
        MessageUtils.sendMessage(player, "left-faction");
    }

    private void handleDisband(Player player) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) { MessageUtils.sendMessage(player, "not-in-faction"); return; }
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!faction.getLeader().equals(player.getUniqueId())) { MessageUtils.sendMessage(player, "not-leader"); return; }
        for (UUID mid : faction.getMembers()) {
            PlayerData md = plugin.getPlayerManager().getPlayerData(mid);
            md.setFactionId(null); md.setRole(Grade.MEMBER);
        }
        plugin.getClaimManager().removeAllFactionClaims(faction.getId());
        plugin.getFactionManager().disbandFaction(faction.getId());
        MessageUtils.sendMessage(player, "faction-disbanded");
    }

    private void handleInvite(Player player, String[] args) {
        if (args.length < 2) return;
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) return;
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!faction.hasPermission(data.getRole(), "INVITE")) { MessageUtils.sendMessage(player, "no-permission", "%perm%", "INVITE"); return; }
        if (args.length < 3) return;
        String targetName = args[2];
        Player target = Bukkit.getPlayer(targetName);
        UUID targetUUID = target != null ? target.getUniqueId() : Bukkit.getOfflinePlayer(targetName).getUniqueId();
        if (args[1].equalsIgnoreCase("add")) {
            faction.getInvites().add(targetUUID);
            MessageUtils.sendMessage(player, "invited", "%target%", targetName);
        } else {
            faction.getInvites().remove(targetUUID);
            player.sendMessage("§aInvitation révoquée.");
        }
    }

    private void handleKick(Player player, String[] args) {
        if (args.length < 2) return;
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) return;
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!faction.hasPermission(data.getRole(), "KICK")) { MessageUtils.sendMessage(player, "no-permission", "%perm%", "KICK"); return; }
        UUID targetUUID = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
        if (!faction.getMembers().contains(targetUUID)) return;
        faction.removeMember(targetUUID);
        PlayerData td = plugin.getPlayerManager().getPlayerData(targetUUID);
        td.setFactionId(null); td.setRole(Grade.MEMBER);
        MessageUtils.sendMessage(player, "kicked", "%target%", args[1]);
    }

    private void handlePromote(Player player, String[] args) {
        if (args.length < 2) return;
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) return;
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!faction.hasPermission(data.getRole(), "PROMOTE")) { MessageUtils.sendMessage(player, "no-permission", "%perm%", "PROMOTE"); return; }
        PlayerData target = plugin.getPlayerManager().getPlayerDataByName(args[1]);
        if (target != null && target.getRole() == Grade.MEMBER) {
            target.setRole(Grade.OFFICER);
            faction.getOfficers().add(target.getUuid());
            MessageUtils.sendMessage(player, "promoted", "%target%", args[1]);
        }
    }

    private void handleDemote(Player player, String[] args) {
        if (args.length < 2) return;
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) return;
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!faction.hasPermission(data.getRole(), "DEMOTE")) { MessageUtils.sendMessage(player, "no-permission", "%perm%", "DEMOTE"); return; }
        PlayerData target = plugin.getPlayerManager().getPlayerDataByName(args[1]);
        if (target != null && target.getRole() == Grade.OFFICER) {
            target.setRole(Grade.MEMBER);
            faction.getOfficers().remove(target.getUuid());
            MessageUtils.sendMessage(player, "demoted", "%target%", args[1]);
        }
    }

    private void handleLeader(Player player, String[] args) {
        if (args.length < 2) return;
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) return;
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!faction.getLeader().equals(player.getUniqueId())) return;
        PlayerData target = plugin.getPlayerManager().getPlayerDataByName(args[1]);
        if (target != null && faction.getMembers().contains(target.getUuid())) {
            faction.setLeader(target.getUuid());
            data.setRole(Grade.OFFICER);
            target.setRole(Grade.LEADER);
            MessageUtils.sendMessage(player, "new-leader", "%target%", args[1]);
        }
    }

    private void handleDesc(Player player, String[] args) {
        if (args.length < 2) return;
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        Faction f = plugin.getFactionManager().getFaction(data.getFactionId());
        if (f == null || !f.hasPermission(data.getRole(), "DESC")) return;
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < args.length; i++) sb.append(args[i]).append(" ");
        f.setDescription(sb.toString().trim());
        player.sendMessage("§aDescription mise à jour.");
    }

    private void handleMotd(Player player, String[] args) {
        if (args.length < 2) return;
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        Faction f = plugin.getFactionManager().getFaction(data.getFactionId());
        if (f == null || !f.hasPermission(data.getRole(), "MOTD")) return;
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < args.length; i++) sb.append(args[i]).append(" ");
        f.setMotd(sb.toString().trim());
        player.sendMessage("§aMOTD mis à jour.");
    }

    private void handleTitle(Player player, String[] args) {
        if (args.length < 3) return;
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        Faction f = plugin.getFactionManager().getFaction(data.getFactionId());
        if (f == null || !f.hasPermission(data.getRole(), "TITLE")) return;
        PlayerData target = plugin.getPlayerManager().getPlayerDataByName(args[2]);
        if (target != null) { target.setTitle(args[1].replace('&', '§')); player.sendMessage("§aTitre mis à jour."); }
    }

    private void handleName(Player player, String[] args) {
        if (args.length < 2) return;
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        Faction f = plugin.getFactionManager().getFaction(data.getFactionId());
        if (f == null || !f.hasPermission(data.getRole(), "RENAME")) return;
        plugin.getFactionManager().renameFaction(f, args[1]);
        MessageUtils.sendMessage(player, "renamed-faction", "%name%", args[1]);
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
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (faction == null) return;
        player.sendMessage("§6--- Statut: " + faction.getName() + " ---");
        for (UUID mid : faction.getMembers()) {
            PlayerData md = plugin.getPlayerManager().getPlayerData(mid);
            player.sendMessage("§e" + md.getName() + " §7- Power: " + String.format("%.1f", md.getPower()));
        }
    }

    private void handleFactionInfo(Player player, String[] args) {
        Faction f = args.length < 2 ? (plugin.getFactionManager().getFaction(plugin.getPlayerManager().getPlayerData(player.getUniqueId()).getFactionId())) : plugin.getFactionManager().getFactionByName(args[1]);
        if (f == null) return;
        player.sendMessage("§6--- Info: " + f.getName() + " ---");
        player.sendMessage("§ePower: " + String.format("%.1f", f.getPower()));
        player.sendMessage("§eClaims: " + f.getClaims().size());
    }

    private void handlePlayerInfo(Player player, String[] args) {
        String name = args.length < 2 ? player.getName() : args[1];
        PlayerData pd = plugin.getPlayerManager().getPlayerDataByName(name);
        if (pd == null) return;
        player.sendMessage("§6--- Info: " + pd.getName() + " ---");
        player.sendMessage("§ePower: " + String.format("%.1f", pd.getPower()));
    }

    private void handlePower(Player player, String[] args) {
        PlayerData pd = args.length < 2 ? plugin.getPlayerManager().getPlayerData(player.getUniqueId()) : plugin.getPlayerManager().getPlayerDataByName(args[1]);
        if (pd != null) player.sendMessage("§aPower: " + String.format("%.1f", pd.getPower()));
    }

    private void handleClaim(Player player, String[] args) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        Faction f = plugin.getFactionManager().getFaction(data.getFactionId());
        if (f == null || !f.hasPermission(data.getRole(), "CLAIM")) return;
        if (args.length > 1 && args[1].equalsIgnoreCase("radius") && args.length > 2) {
            int r = Integer.parseInt(args[2]);
            for (int x = -r; x <= r; x++) for (int z = -r; z <= r; z++) performClaim(player, f, player.getWorld().getName(), player.getLocation().getChunk().getX() + x, player.getLocation().getChunk().getZ() + z);
            return;
        }
        performClaim(player, f, player.getWorld().getName(), player.getLocation().getChunk().getX(), player.getLocation().getChunk().getZ());
    }

    public void performClaim(Player player, Faction faction, String world, int x, int z) {
        if (plugin.getClaimManager().isClaimed(world, x, z)) return;
        Claim c = new Claim(world, x, z, faction.getId());
        plugin.getClaimManager().addClaim(c);
        faction.getClaims().add(c.toString());
        player.sendMessage("§aClaim réussi.");
    }

    private void handleUnclaim(Player player, String[] args) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        Faction f = plugin.getFactionManager().getFaction(data.getFactionId());
        if (f == null || !f.hasPermission(data.getRole(), "UNCLAIM")) return;
        plugin.getClaimManager().removeClaim(player.getWorld().getName(), player.getLocation().getChunk().getX(), player.getLocation().getChunk().getZ());
        player.sendMessage("§aUnclaim réussi.");
    }

    private void handleClaimsCount(Player player) {
        Faction f = plugin.getFactionManager().getFaction(plugin.getPlayerManager().getPlayerData(player.getUniqueId()).getFactionId());
        if (f != null) player.sendMessage("§aClaims: " + f.getClaims().size());
    }

    private void handleMap(Player player) {
        int rx = 12, rz = 6;
        player.sendMessage("§6--- Carte ---");
        for (int z = -rz; z <= rz; z++) {
            net.kyori.adventure.text.TextComponent.Builder line = net.kyori.adventure.text.Component.text();
            for (int x = -rx; x <= rx; x++) {
                Claim c = plugin.getClaimManager().getClaim(player.getWorld().getName(), player.getLocation().getChunk().getX() + x, player.getLocation().getChunk().getZ() + z);
                if (x == 0 && z == 0) line.append(net.kyori.adventure.text.Component.text("§b+"));
                else if (c == null) line.append(net.kyori.adventure.text.Component.text("§7-"));
                else {
                    Faction o = plugin.getFactionManager().getFaction(c.getFactionId());
                    line.append(net.kyori.adventure.text.Component.text("§e#").hoverEvent(net.kyori.adventure.text.Component.text("§6Faction: §e" + (o != null ? o.getName() : "???"))));
                }
            }
            player.sendMessage(line.build());
        }
    }

    private void handleSeechunk(Player player) { player.sendMessage("§eBordures activées."); }

    private void handleRelation(Player player, String[] args, String rel) {
        if (args.length < 2) return;
        Faction f = plugin.getFactionManager().getFaction(plugin.getPlayerManager().getPlayerData(player.getUniqueId()).getFactionId());
        Faction t = plugin.getFactionManager().getFactionByName(args[1]);
        if (f != null && t != null) { f.getRelations().put(t.getId(), rel); player.sendMessage("§aRelation mise à jour."); }
    }

    private void handleChat(Player player, String[] args) { player.sendMessage("§aChat changé."); }

    private void handleGui(Player player) {
        Faction f = plugin.getFactionManager().getFaction(plugin.getPlayerManager().getPlayerData(player.getUniqueId()).getFactionId());
        if (f != null) fr.jules.faction.gui.FactionGUI.openMainMenu(player, f);
    }

    private void handleUnstuck(Player player) { player.teleport(player.getWorld().getSpawnLocation()); }

    private void handleAdmin(Player player, String[] args) {
        if (args.length > 1 && args[1].equalsIgnoreCase("bypass")) {
            PlayerData pd = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
            pd.setBypass(!pd.isBypass());
            player.sendMessage("§aBypass: " + pd.isBypass());
        }
    }

    private void handleFactionSetHome(Player player) {
        PlayerData pd = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        Faction f = plugin.getFactionManager().getFaction(pd.getFactionId());
        if (f != null && f.hasPermission(pd.getRole(), "SETHOME")) { f.setHome(player.getLocation()); player.sendMessage("§aHome défini."); }
    }

    private void handleFactionUnsetHome(Player player) {
        PlayerData pd = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        Faction f = plugin.getFactionManager().getFaction(pd.getFactionId());
        if (f != null && f.hasPermission(pd.getRole(), "UNSETHOME")) { f.setHome(null); player.sendMessage("§aHome supprimé."); }
    }

    private void handleFactionHome(Player player, String[] args) {
        PlayerData pd = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        Faction f = plugin.getFactionManager().getFaction(pd.getFactionId());
        if (f != null && f.getHome() != null) { player.teleport(f.getHome()); player.sendMessage("§aTéléportation."); }
    }

    private void handleTnt(Player player, String[] args) {
        Faction f = plugin.getFactionManager().getFaction(plugin.getPlayerManager().getPlayerData(player.getUniqueId()).getFactionId());
        if (f != null) player.sendMessage("§aTNT: " + f.getTntStock());
    }

    private void handleMoney(Player player, String[] args) {
        Faction f = plugin.getFactionManager().getFaction(plugin.getPlayerManager().getPlayerData(player.getUniqueId()).getFactionId());
        if (f != null) player.sendMessage("§aArgent: " + f.getBalance());
    }

    private void handleBalance(Player player) { handleMoney(player, null); }

    private void handleFlag(Player player, String[] args) {
        if (args.length < 3) return;
        Faction f = plugin.getFactionManager().getFaction(plugin.getPlayerManager().getPlayerData(player.getUniqueId()).getFactionId());
        if (f != null) { f.getFactionFlags().put(args[1].toLowerCase(), args[2].equalsIgnoreCase("on")); player.sendMessage("§aFlag mis à jour."); }
    }

    private void handleSpecialZone(Player player, FactionType type) {
        Faction f = plugin.getFactionManager().getFactionByName(type.name());
        if (f == null) { f = plugin.getFactionManager().createFaction(type.name(), UUID.randomUUID()); f.setType(type); }
        performClaim(player, f, player.getWorld().getName(), player.getLocation().getChunk().getX(), player.getLocation().getChunk().getZ());
    }

    private void handleSetPower(Player player, String[] args) {
        if (args.length < 3) return;
        PlayerData pd = plugin.getPlayerManager().getPlayerDataByName(args[1]);
        if (pd != null) pd.setPower(Double.parseDouble(args[2]));
    }

    private void handleReload(Player player) { plugin.reloadConfig(); player.sendMessage("§aReload terminé."); }

    private void displayHelp(Player player) {
        player.sendMessage("§6--- Aide Faction ---");
        player.sendMessage("§e/f create/join/leave/disband");
        player.sendMessage("§e/f invite/kick/promote/demote/leader");
    }
}
