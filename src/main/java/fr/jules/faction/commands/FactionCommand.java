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
            case "enemy": case "e": handleRelation(player, args, "ENEMY"); break;
            case "truce": case "t": handleRelation(player, args, "TRUCE"); break;
            case "ally": case "a": handleRelation(player, args, "ALLY"); break;
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
        if (args.length < 2) { player.sendMessage("§cUtilisation: /f invite add/revoke [pseudo]"); return; }
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) { MessageUtils.sendMessage(player, "not-in-faction"); return; }
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!faction.hasPermission(data.getRole(), "INVITE")) { MessageUtils.sendMessage(player, "no-permission", "%perm%", "INVITE (Faction)"); return; }
        if (args.length < 3) { player.sendMessage("§cPrécisez un pseudo."); return; }
        String action = args[1].toLowerCase();
        String targetName = args[2];
        if (action.equals("add")) {
            Player target = Bukkit.getPlayer(targetName);
            UUID targetUUID = target != null ? target.getUniqueId() : Bukkit.getOfflinePlayer(targetName).getUniqueId();
            faction.getInvites().add(targetUUID);
            MessageUtils.sendMessage(player, "invited", "%target%", targetName);
            if (target != null) MessageUtils.sendMessage(target, "invite-received", "%name%", faction.getName());
        } else {
            UUID targetUUID = Bukkit.getOfflinePlayer(targetName).getUniqueId();
            faction.getInvites().remove(targetUUID);
            player.sendMessage("§aInvitation révoquée.");
        }
    }

    private void handleKick(Player player, String[] args) {
        if (args.length < 2) { player.sendMessage("§cUtilisation: /f kick [pseudo]"); return; }
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) { MessageUtils.sendMessage(player, "not-in-faction"); return; }
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!faction.hasPermission(data.getRole(), "KICK")) { MessageUtils.sendMessage(player, "no-permission", "%perm%", "KICK (Faction)"); return; }
        UUID targetUUID = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
        if (!faction.getMembers().contains(targetUUID)) return;
        if (targetUUID.equals(faction.getLeader())) { MessageUtils.sendMessage(player, "cannot-kick-leader"); return; }
        faction.removeMember(targetUUID);
        PlayerData td = plugin.getPlayerManager().getPlayerData(targetUUID);
        td.setFactionId(null); td.setRole(Grade.MEMBER);
        MessageUtils.sendMessage(player, "kicked", "%target%", args[1]);
    }

    private void handlePromote(Player player, String[] args) {
        if (args.length < 2) { player.sendMessage("§cUtilisation: /f promote [pseudo]"); return; }
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) { MessageUtils.sendMessage(player, "not-in-faction"); return; }
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!faction.hasPermission(data.getRole(), "PROMOTE")) { MessageUtils.sendMessage(player, "no-permission", "%perm%", "PROMOTE (Faction)"); return; }
        PlayerData target = plugin.getPlayerManager().getPlayerDataByName(args[1]);
        if (target != null && target.getRole() != Grade.LEADER && target.getRole() != Grade.OFFICER) {
            target.setRole(Grade.OFFICER);
            faction.getOfficers().add(target.getUuid());
            MessageUtils.sendMessage(player, "promoted", "%target%", args[1]);
        }
    }

    private void handleDemote(Player player, String[] args) {
        if (args.length < 2) { player.sendMessage("§cUtilisation: /f demote [pseudo]"); return; }
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) { MessageUtils.sendMessage(player, "not-in-faction"); return; }
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!faction.hasPermission(data.getRole(), "DEMOTE")) { MessageUtils.sendMessage(player, "no-permission", "%perm%", "DEMOTE (Faction)"); return; }
        PlayerData target = plugin.getPlayerManager().getPlayerDataByName(args[1]);
        if (target != null && target.getRole() == Grade.OFFICER) {
            target.setRole(Grade.MEMBER);
            faction.getOfficers().remove(target.getUuid());
            MessageUtils.sendMessage(player, "demoted", "%target%", args[1]);
        }
    }

    private void handleLeader(Player player, String[] args) {
        if (args.length < 2) { player.sendMessage("§cUtilisation: /f leader [pseudo]"); return; }
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) { MessageUtils.sendMessage(player, "not-in-faction"); return; }
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!faction.getLeader().equals(player.getUniqueId())) { MessageUtils.sendMessage(player, "not-leader"); return; }
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
        if (f == null || !f.hasPermission(data.getRole(), "DESC")) { MessageUtils.sendMessage(player, "no-permission"); return; }
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < args.length; i++) sb.append(args[i]).append(" ");
        f.setDescription(sb.toString().trim());
        player.sendMessage("§aDescription mise à jour.");
    }

    private void handleMotd(Player player, String[] args) {
        if (args.length < 2) return;
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        Faction f = plugin.getFactionManager().getFaction(data.getFactionId());
        if (f == null || !f.hasPermission(data.getRole(), "MOTD")) { MessageUtils.sendMessage(player, "no-permission"); return; }
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < args.length; i++) sb.append(args[i]).append(" ");
        f.setMotd(sb.toString().trim());
        player.sendMessage("§aMOTD mis à jour.");
    }

    private void handleTitle(Player player, String[] args) {
        if (args.length < 3) return;
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        Faction f = plugin.getFactionManager().getFaction(data.getFactionId());
        if (f == null || !f.hasPermission(data.getRole(), "TITLE")) { MessageUtils.sendMessage(player, "no-permission"); return; }
        PlayerData targetData = plugin.getPlayerManager().getPlayerDataByName(args[2]);
        if (targetData != null) { targetData.setTitle(args[1].replace('&', '§')); player.sendMessage("§aTitre mis à jour."); }
    }

    private void handleName(Player player, String[] args) {
        if (args.length < 2) return;
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        Faction f = plugin.getFactionManager().getFaction(data.getFactionId());
        if (f == null || !f.hasPermission(data.getRole(), "RENAME")) { MessageUtils.sendMessage(player, "no-permission"); return; }
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
            String status = Bukkit.getPlayer(mid) != null ? "§a[On]" : "§c[Off]";
            player.sendMessage("§e" + md.getName() + " §7- Power: " + String.format("%.1f", md.getPower()) + " " + status);
        }
    }

    private void handleFactionInfo(Player player, String[] args) {
        PlayerData pd = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        Faction f;
        if (args.length < 2) {
            if (pd.getFactionId() == null) { MessageUtils.sendMessage(player, "not-in-faction"); return; }
            f = plugin.getFactionManager().getFaction(pd.getFactionId());
        } else {
            f = plugin.getFactionManager().getFactionByName(args[1]);
        }
        if (f == null) { MessageUtils.sendMessage(player, "faction-not-found"); return; }
        player.sendMessage("§6§l--- Information Faction: " + f.getName() + " ---");
        player.sendMessage("§eDescription: §f" + f.getDescription());
        player.sendMessage("§eChef: §f" + Bukkit.getOfflinePlayer(f.getLeader()).getName());
        player.sendMessage("§eMembres: §f" + f.getMembers().size());
        player.sendMessage("§ePower: §f" + String.format("%.1f", f.getPower()));
        player.sendMessage("§eClaims: §f" + f.getClaims().size());
        player.sendMessage("§eBanque: §f" + f.getBalance() + "$");
    }

    private void handlePlayerInfo(Player player, String[] args) {
        String name = args.length < 2 ? player.getName() : args[1];
        PlayerData pd = plugin.getPlayerManager().getPlayerDataByName(name);
        if (pd == null) { player.sendMessage("§cJoueur inconnu."); return; }
        player.sendMessage("§6--- Info: " + pd.getName() + " ---");
        player.sendMessage("§eGrade: §f" + pd.getRole().name());
        player.sendMessage("§ePower: §f" + String.format("%.1f", pd.getPower()) + "/" + String.format("%.1f", pd.getMaxPower()));
    }

    private void handlePower(Player player, String[] args) {
        PlayerData pd = args.length < 2 ? plugin.getPlayerManager().getPlayerData(player.getUniqueId()) : plugin.getPlayerManager().getPlayerDataByName(args[1]);
        if (pd != null) player.sendMessage("§aPower: §e" + String.format("%.1f", pd.getPower()));
    }

    private void handleClaim(Player player, String[] args) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        Faction f = plugin.getFactionManager().getFaction(data.getFactionId());
        if (f == null || !f.hasPermission(data.getRole(), "CLAIM")) { MessageUtils.sendMessage(player, "no-permission"); return; }
        if (args.length > 1 && args[1].equalsIgnoreCase("auto")) {
            data.setAutoClaim(!data.isAutoClaim());
            player.sendMessage("§aAuto-claim: " + (data.isAutoClaim() ? "§aOn" : "§cOff"));
            return;
        }
        if (args.length > 1 && args[1].equalsIgnoreCase("radius") && args.length > 2) {
            int r = Integer.parseInt(args[2]);
            if (r > 5) r = 5;
            for (int x = -r; x <= r; x++) for (int z = -r; z <= r; z++) performClaim(player, f, player.getWorld().getName(), player.getLocation().getChunk().getX() + x, player.getLocation().getChunk().getZ() + z);
            return;
        }
        performClaim(player, f, player.getWorld().getName(), player.getLocation().getChunk().getX(), player.getLocation().getChunk().getZ());
    }

    public void performClaim(Player player, Faction faction, String world, int x, int z) {
        if (plugin.getClaimManager().isClaimed(world, x, z)) return;
        if (!faction.getClaims().isEmpty() && !player.hasPermission("faction.admin")) {
            boolean adj = faction.getClaims().contains(world + "," + (x + 1) + "," + z) || faction.getClaims().contains(world + "," + (x - 1) + "," + z) || faction.getClaims().contains(world + "," + x + "," + (z + 1)) || faction.getClaims().contains(world + "," + x + "," + (z - 1));
            if (!adj) { player.sendMessage("§cPas adjacent."); return; }
        }
        if (faction.getPower() < faction.getClaims().size() + 1 && !player.hasPermission("faction.admin")) { MessageUtils.sendMessage(player, "claim-not-enough-power"); return; }
        Claim c = new Claim(world, x, z, faction.getId());
        plugin.getClaimManager().addClaim(c);
        faction.getClaims().add(c.toString());
        player.sendMessage("§aClaim réussi.");
    }

    private void handleUnclaim(Player player, String[] args) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        Faction f = plugin.getFactionManager().getFaction(data.getFactionId());
        if (f == null || !f.hasPermission(data.getRole(), "UNCLAIM")) return;
        if (args.length > 1 && args[1].equalsIgnoreCase("all")) {
            for (String s : new ArrayList<>(f.getClaims())) {
                Claim c = Claim.fromString(s);
                plugin.getClaimManager().removeClaim(c.getWorld(), c.getX(), c.getZ());
            }
            f.getClaims().clear();
            player.sendMessage("§aUnclaim total.");
            return;
        }
        plugin.getClaimManager().removeClaim(player.getWorld().getName(), player.getLocation().getChunk().getX(), player.getLocation().getChunk().getZ());
        player.sendMessage("§aUnclaim.");
    }

    private void handleClaimsCount(Player player) {
        Faction f = plugin.getFactionManager().getFaction(plugin.getPlayerManager().getPlayerData(player.getUniqueId()).getFactionId());
        if (f != null) player.sendMessage("§aClaims: " + f.getClaims().size());
    }

    private void handleMap(Player player) {
        int rx = 16, rz = 8;
        PlayerData pd = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        player.sendMessage("§6§l--- Carte ---");
        for (int z = -rz; z <= rz; z++) {
            net.kyori.adventure.text.TextComponent.Builder line = net.kyori.adventure.text.Component.text();
            for (int x = -rx; x <= rx; x++) {
                if (x == 0 && z == 0) line.append(net.kyori.adventure.text.Component.text("§b+"));
                else {
                    Claim c = plugin.getClaimManager().getClaim(player.getWorld().getName(), player.getLocation().getChunk().getX() + x, player.getLocation().getChunk().getZ() + z);
                    if (c == null) line.append(net.kyori.adventure.text.Component.text("§7-"));
                    else {
                        Faction o = plugin.getFactionManager().getFaction(c.getFactionId());
                        String color = "§f";
                        if (pd.getFactionId() != null) {
                            if (o.getId().equals(pd.getFactionId())) color = "§a";
                            else {
                                String r = plugin.getFactionManager().getFaction(pd.getFactionId()).getRelations().get(o.getId());
                                if ("ALLY".equals(r)) color = "§d"; else if ("ENEMY".equals(r)) color = "§c";
                            }
                        }
                        line.append(net.kyori.adventure.text.Component.text(color + "#").hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(net.kyori.adventure.text.Component.text(color + o.getName()))));
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
        if (args.length < 2) {
            player.sendMessage("§cUtilisation: /f " + rel.toLowerCase() + " [faction]");
            return;
        }
        PlayerData pd = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (pd.getFactionId() == null) {
            MessageUtils.sendMessage(player, "not-in-faction");
            return;
        }
        Faction f = plugin.getFactionManager().getFaction(pd.getFactionId());
        if (!f.isOfficer(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "not-officer");
            return;
        }

        Faction target = plugin.getFactionManager().getFactionByName(args[1]);
        if (target == null) {
            MessageUtils.sendMessage(player, "faction-not-found");
            return;
        }

        if (f.getId().equals(target.getId())) {
            player.sendMessage("§cVous ne pouvez pas changer de relation avec votre propre faction.");
            return;
        }

        f.getRelations().put(target.getId(), rel);
        String otherRel = target.getRelations().get(f.getId());

        if (rel.equals("ENEMY")) {
            MessageUtils.sendMessage(player, "relation-updated", "%target%", target.getName(), "%relation%", "§cEnnemi");
            broadcastToFaction(target, "§cLa faction " + f.getName() + " vous a déclaré la guerre !");
        } else if (rel.equals("ALLY")) {
            if ("ALLY".equals(otherRel)) {
                MessageUtils.sendMessage(player, "relation-updated", "%target%", target.getName(), "%relation%", "§dAllié (Mutuel)");
                broadcastToFaction(target, "§dVous êtes désormais alliés avec " + f.getName() + " !");
            } else {
                MessageUtils.sendMessage(player, "relation-updated", "%target%", target.getName(), "%relation%", "§dDemande d'alliance envoyée");
                broadcastToFaction(target, "§dLa faction " + f.getName() + " souhaite devenir votre alliée. Utilisez /f ally " + f.getName() + " pour accepter.");
            }
        } else if (rel.equals("TRUCE")) {
            if ("TRUCE".equals(otherRel)) {
                MessageUtils.sendMessage(player, "relation-updated", "%target%", target.getName(), "%relation%", "§6Trêve (Mutuelle)");
                broadcastToFaction(target, "§6Vous êtes désormais en trêve avec " + f.getName() + " !");
            } else {
                MessageUtils.sendMessage(player, "relation-updated", "%target%", target.getName(), "%relation%", "§6Demande de trêve envoyée");
                broadcastToFaction(target, "§6La faction " + f.getName() + " souhaite une trêve. Utilisez /f truce " + f.getName() + " pour accepter.");
            }
        } else {
            MessageUtils.sendMessage(player, "relation-updated", "%target%", target.getName(), "%relation%", "§fNeutre");
        }
    }

    private void broadcastToFaction(Faction faction, String message) {
        for (UUID memberId : faction.getMembers()) {
            Player p = Bukkit.getPlayer(memberId);
            if (p != null) p.sendMessage(message);
        }
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
        if (!player.hasPermission("faction.admin")) return;
        if (args.length > 1 && args[1].equalsIgnoreCase("bypass")) {
            PlayerData pd = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
            pd.setBypass(!pd.isBypass());
            player.sendMessage("§aBypass: " + pd.isBypass());
        }
    }

    private void handleFactionSetHome(Player player) {
        PlayerData pd = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (pd.getFactionId() == null) return;
        Faction f = plugin.getFactionManager().getFaction(pd.getFactionId());
        if (f.hasPermission(pd.getRole(), "SETHOME")) { f.setHome(player.getLocation()); player.sendMessage("§aHome défini."); }
    }

    private void handleFactionUnsetHome(Player player) {
        PlayerData pd = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (pd.getFactionId() == null) return;
        Faction f = plugin.getFactionManager().getFaction(pd.getFactionId());
        if (f.hasPermission(pd.getRole(), "UNSETHOME")) { f.setHome(null); player.sendMessage("§aHome supprimé."); }
    }

    private void handleFactionHome(Player player, String[] args) {
        PlayerData pd = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        Faction f = args.length < 2 ? (pd.getFactionId() != null ? plugin.getFactionManager().getFaction(pd.getFactionId()) : null) : plugin.getFactionManager().getFactionByName(args[1]);
        if (f == null || f.getHome() == null) return;
        boolean ok = pd.getFactionId() != null && (pd.getFactionId().equals(f.getId()) || ("ALLY".equals(f.getRelations().get(pd.getFactionId())) && f.getFactionFlags().getOrDefault("ALLY_HOME", true)));
        if (!ok) { MessageUtils.sendMessage(player, "no-permission"); return; }
        player.teleport(f.getHome());
    }

    private void handleTnt(Player player, String[] args) {
        PlayerData pd = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (pd.getFactionId() == null) return;
        Faction f = plugin.getFactionManager().getFaction(pd.getFactionId());
        player.sendMessage("§aTNT: " + f.getTntStock());
    }

    private void handleMoney(Player player, String[] args) {
        PlayerData pd = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (pd.getFactionId() == null) return;
        Faction f = plugin.getFactionManager().getFaction(pd.getFactionId());
        player.sendMessage("§aBanque: " + f.getBalance());
    }

    private void handleBalance(Player player) { handleMoney(player, null); }

    private void handleFlag(Player player, String[] args) {
        if (args.length < 3) return;
        PlayerData pd = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (pd.getFactionId() == null) return;
        Faction f = plugin.getFactionManager().getFaction(pd.getFactionId());
        if (f.isOfficer(player.getUniqueId())) { f.getFactionFlags().put(args[1].toLowerCase(), args[2].equalsIgnoreCase("on")); player.sendMessage("§aFlag mis à jour."); }
    }

    private void handleSpecialZone(Player player, FactionType type) {
        if (!player.hasPermission("faction.admin")) return;
        Faction f = plugin.getFactionManager().getFactionByName(type.name());
        if (f == null) { f = plugin.getFactionManager().createFaction(type.name(), UUID.randomUUID()); f.setType(type); }
        performClaim(player, f, player.getWorld().getName(), player.getLocation().getChunk().getX(), player.getLocation().getChunk().getZ());
    }

    private void handleSetPower(Player player, String[] args) {
        if (!player.hasPermission("faction.admin") || args.length < 3) return;
        PlayerData target = plugin.getPlayerManager().getPlayerDataByName(args[1]);
        if (target != null) target.setPower(Double.parseDouble(args[2]));
    }

    private void handleReload(Player player) { if (player.hasPermission("faction.admin")) { plugin.reloadConfig(); player.sendMessage("§aReload."); } }

    private void displayHelp(Player player) {
        player.sendMessage("§6--- Commandes Faction ---");
        player.sendMessage("§e/f create <nom> §7- Créer une faction");
        player.sendMessage("§e/f show [faction] §7- Infos faction");
        player.sendMessage("§e/f join <faction> §7- Rejoindre");
        player.sendMessage("§e/f leave §7- Quitter");
        player.sendMessage("§e/f claim/unclaim §7- Territoires");
        player.sendMessage("§e/f map §7- Carte");
        player.sendMessage("§e/f gui §7- Menu de gestion");
        player.sendMessage("§e/f home §7- Téléportation");
    }
}
