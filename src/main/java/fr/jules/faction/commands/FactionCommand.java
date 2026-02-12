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
        if (!player.hasPermission("faction.command." + sub)) {
            MessageUtils.sendMessage(player, "no-permission");
            return true;
        }

        switch (sub) {
            case "create":
                handleCreate(player, args);
                break;
            case "join":
                handleJoin(player, args);
                break;
            case "leave":
                handleLeave(player);
                break;
            case "disband":
                handleDisband(player);
                break;
            case "invite":
            case "i":
                handleInvite(player, args);
                break;
            case "kick":
                handleKick(player, args);
                break;
            case "promote":
                handlePromote(player, args);
                break;
            case "demote":
                handleDemote(player, args);
                break;
            case "officer":
                handleOfficer(player, args);
                break;
            case "leader":
                handleLeader(player, args);
                break;
            case "desc":
                handleDesc(player, args);
                break;
            case "motd":
                handleMotd(player, args);
                break;
            case "title":
                handleTitle(player, args);
                break;
            case "name":
                handleName(player, args);
                break;
            case "list":
                handleList(player);
                break;
            case "status":
                handleStatus(player);
                break;
            case "faction":
            case "f":
                handleFactionInfo(player, args);
                break;
            case "player":
            case "p":
                handlePlayerInfo(player, args);
                break;
            case "claim":
                handleClaim(player, args);
                break;
            case "unclaim":
                handleUnclaim(player, args);
                break;
            case "map":
                handleMap(player);
                break;
            case "seechunk":
                handleSeechunk(player);
                break;
            case "neutral":
                handleRelation(player, args, "NEUTRAL");
                break;
            case "enemy":
                handleRelation(player, args, "ENEMY");
                break;
            case "truce":
                handleRelation(player, args, "TRUCE");
                break;
            case "ally":
                handleRelation(player, args, "ALLY");
                break;
            case "chat":
            case "c":
                handleChat(player, args);
                break;
            case "perm":
                handlePerm(player);
                break;
            case "unstuck":
                handleUnstuck(player);
                break;
            case "gui":
                handlePerm(player);
                break;
            case "help":
                displayHelp(player);
                break;
            case "admin":
                handleAdmin(player, args);
                break;
            case "sethome":
                handleFactionSetHome(player);
                break;
            case "unsethome":
                handleFactionUnsetHome(player);
                break;
            case "home":
                handleFactionHome(player, args);
                break;
            default:
                player.sendMessage("§cSous-commande inconnue ou non encore implémentée.");
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
            player.sendMessage("§cLe chef ne peut pas quitter sa faction. Utilisez /f disband pour la supprimer ou nommez un nouveau chef.");
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
        if (args.length < 3) {
            player.sendMessage("§cUtilisation: /f invite add/revoke [pseudo]");
            return;
        }
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) {
            MessageUtils.sendMessage(player, "not-in-faction");
            return;
        }
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!faction.isOfficer(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "not-officer");
            return;
        }

        String action = args[1].toLowerCase();
        String targetName = args[2];

        if (action.equals("add")) {
            Player target = Bukkit.getPlayer(targetName);
            UUID targetUUID = target != null ? target.getUniqueId() : Bukkit.getOfflinePlayer(targetName).getUniqueId();
            faction.getInvites().add(targetUUID);
            MessageUtils.sendMessage(player, "invited", "%target%", targetName);
            if (target != null) {
                MessageUtils.sendMessage(target, "invite-received", "%name%", faction.getName());
            }
        } else if (action.equals("revoke")) {
            if (targetName.equalsIgnoreCase("all")) {
                faction.getInvites().clear();
                player.sendMessage("§aToutes les invitations ont été révoquées.");
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
        if (!faction.isOfficer(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "not-officer");
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
        if (faction.getOfficers().contains(targetUUID) && !faction.getLeader().equals(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "cannot-kick-officer");
            return;
        }

        faction.removeMember(targetUUID);
        PlayerData targetData = plugin.getPlayerManager().getPlayerData(targetUUID);
        targetData.setFactionId(null);
        targetData.setRole(Grade.MEMBER);
        MessageUtils.sendMessage(player, "kicked", "%target%", targetName);
        Player target = Bukkit.getPlayer(targetUUID);
        if (target != null) {
            MessageUtils.sendMessage(target, "kicked-received");
        }
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
        if (targetUUID.equals(player.getUniqueId())) {
            player.sendMessage("§cVous ne pouvez pas vous promouvoir vous-même.");
            return;
        }

        if (!faction.getOfficers().contains(targetUUID)) {
            faction.getOfficers().add(targetUUID);
            PlayerData targetData = plugin.getPlayerManager().getPlayerData(targetUUID);
            targetData.setRole(Grade.OFFICER);
            MessageUtils.sendMessage(player, "promoted", "%target%", targetName);
        } else {
            player.sendMessage("§cCe joueur est déjà officier. Pour le nommer chef, utilisez /f leader.");
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

        if (faction.getOfficers().contains(targetUUID)) {
            faction.getOfficers().remove(targetUUID);
            PlayerData targetData = plugin.getPlayerManager().getPlayerData(targetUUID);
            targetData.setRole(Grade.MEMBER);
            MessageUtils.sendMessage(player, "demoted", "%target%", targetName);
        } else {
            player.sendMessage("§cCe joueur est déjà au grade le plus bas.");
        }
    }

    private void handleOfficer(Player player, String[] args) {
        handlePromote(player, args);
    }

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
        faction.getOfficers().add(player.getUniqueId()); // Ancien chef devient officier

        data.setRole(Grade.OFFICER);
        PlayerData targetData = plugin.getPlayerManager().getPlayerData(targetUUID);
        targetData.setRole(Grade.LEADER);

        MessageUtils.sendMessage(player, "new-leader", "%target%", targetName);
    }

    private void handleDesc(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUtilisation: /f desc [description]");
            return;
        }
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) {
            MessageUtils.sendMessage(player, "not-in-faction");
            return;
        }
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!faction.isOfficer(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "not-officer");
            return;
        }

        StringBuilder desc = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            desc.append(args[i]).append(" ");
        }
        faction.setDescription(desc.toString().trim());
        player.sendMessage("§aDescription de la faction mise à jour.");
    }

    private void handleMotd(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUtilisation: /f motd [message]");
            return;
        }
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) {
            MessageUtils.sendMessage(player, "not-in-faction");
            return;
        }
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!faction.isOfficer(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "not-officer");
            return;
        }

        StringBuilder motd = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            motd.append(args[i]).append(" ");
        }
        faction.setMotd(motd.toString().trim());
        player.sendMessage("§aMessage du jour de la faction mis à jour.");
    }

    private void handleTitle(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cUtilisation: /f title [titre] [nom du joueur]");
            return;
        }
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) {
            MessageUtils.sendMessage(player, "not-in-faction");
            return;
        }
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!faction.isOfficer(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "not-officer");
            return;
        }

        String title = args[1].replace('&', '§');
        String targetName = args[2];
        UUID targetUUID = Bukkit.getOfflinePlayer(targetName).getUniqueId();
        if (!faction.getMembers().contains(targetUUID)) {
            MessageUtils.sendMessage(player, "target-not-in-faction");
            return;
        }

        PlayerData targetData = plugin.getPlayerManager().getPlayerData(targetUUID);
        targetData.setTitle(title);
        player.sendMessage("§aTitre de " + targetName + " mis à jour: " + title);
    }

    private void handleName(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUtilisation: /f name [nom]");
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
        player.sendMessage("§6--- Statut de la Faction " + faction.getName() + " ---");
        for (UUID memberId : faction.getMembers()) {
            PlayerData memberData = plugin.getPlayerManager().getPlayerData(memberId);
            String status = Bukkit.getPlayer(memberId) != null ? "§a[Connecté]" : "§c[Déconnecté]";
            player.sendMessage("§e" + memberData.getName() + " §7- Power: " + String.format("%.1f", memberData.getPower()) + " " + status);
        }
    }

    private void handleFactionInfo(Player player, String[] args) {
        Faction faction;
        if (args.length < 2) {
            PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
            if (data.getFactionId() == null) {
                player.sendMessage("§cUtilisation: /f faction [nom]");
                return;
            }
            faction = plugin.getFactionManager().getFaction(data.getFactionId());
        } else {
            faction = plugin.getFactionManager().getFactionByName(args[1]);
        }

        if (faction == null) {
            MessageUtils.sendMessage(player, "faction-not-found");
            return;
        }

        player.sendMessage("§6--- Information Faction: " + faction.getName() + " ---");
        player.sendMessage("§eDescription: §f" + faction.getDescription());
        player.sendMessage("§eChef: §f" + Bukkit.getOfflinePlayer(faction.getLeader()).getName());
        player.sendMessage("§eMembres: §f" + faction.getMembers().size());
        player.sendMessage("§ePower: §f" + String.format("%.1f", faction.getPower()));
        player.sendMessage("§eClaims: §f" + faction.getClaims().size());
    }

    private void handlePlayerInfo(Player player, String[] args) {
        String targetName = args.length < 2 ? player.getName() : args[1];
        PlayerData targetData = plugin.getPlayerManager().getPlayerDataByName(targetName);
        if (targetData == null) {
            player.sendMessage("§cCe joueur n'a pas de données enregistrées.");
            return;
        }

        player.sendMessage("§6--- Information Joueur: " + targetData.getName() + " ---");
        Faction faction = targetData.getFactionId() != null ? plugin.getFactionManager().getFaction(targetData.getFactionId()) : null;
        player.sendMessage("§eFaction: §f" + (faction != null ? faction.getName() : "Aucune"));
        player.sendMessage("§eGrade: §f" + targetData.getRole());
        player.sendMessage("§ePower: §f" + String.format("%.1f", targetData.getPower()) + "/" + String.format("%.1f", targetData.getMaxPower()));
        if (targetData.getTitle() != null && !targetData.getTitle().isEmpty()) {
            player.sendMessage("§eTitre: §f" + targetData.getTitle());
        }
    }

    private void handleClaim(Player player, String[] args) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) {
            MessageUtils.sendMessage(player, "not-in-faction");
            return;
        }
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!faction.isOfficer(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "not-officer");
            return;
        }

        if (args.length > 1 && args[1].equalsIgnoreCase("auto")) {
            data.setAutoClaim(!data.isAutoClaim());
            player.sendMessage("§aAuto-claim: " + (data.isAutoClaim() ? "§aActivé" : "§cDésactivé"));
            return;
        }

        String world = player.getWorld().getName();
        int x = player.getLocation().getChunk().getX();
        int z = player.getLocation().getChunk().getZ();

        performClaim(player, faction, world, x, z);
    }

    public void performClaim(Player player, Faction faction, String world, int x, int z) {
        if (plugin.getClaimManager().isClaimed(world, x, z)) {
            MessageUtils.sendMessage(player, "claim-already-owned");
            return;
        }

        if (faction.getPower() < faction.getClaims().size() + 1) {
            MessageUtils.sendMessage(player, "claim-not-enough-power");
            return;
        }

        Claim claim = new Claim(world, x, z, faction.getId());
        plugin.getClaimManager().addClaim(claim);
        faction.getClaims().add(claim.toString());
        MessageUtils.sendMessage(player, "claim-success");
    }

    private void handleUnclaim(Player player, String[] args) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) {
            MessageUtils.sendMessage(player, "not-in-faction");
            return;
        }
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!faction.isOfficer(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "not-officer");
            return;
        }

        String action = args.length < 2 ? "one" : args[1].toLowerCase();
        if (action.equals("all")) {
            for (String claimStr : new ArrayList<>(faction.getClaims())) {
                Claim c = Claim.fromString(claimStr);
                plugin.getClaimManager().removeClaim(c.getWorld(), c.getX(), c.getZ());
            }
            faction.getClaims().clear();
            MessageUtils.sendMessage(player, "unclaim-all");
            return;
        }

        String world = player.getWorld().getName();
        int x = player.getLocation().getChunk().getX();
        int z = player.getLocation().getChunk().getZ();

        Claim claim = plugin.getClaimManager().getClaim(world, x, z);
        if (claim == null || !claim.getFactionId().equals(faction.getId())) {
            MessageUtils.sendMessage(player, "not-your-claim");
            return;
        }

        plugin.getClaimManager().removeClaim(world, x, z);
        faction.getClaims().remove(claim.toString());
        MessageUtils.sendMessage(player, "unclaim-success");
    }

    private void handleMap(Player player) {
        int radius = 8;
        player.sendMessage("§6--- Carte des Factions ---");
        int playerX = player.getLocation().getChunk().getX();
        int playerZ = player.getLocation().getChunk().getZ();
        String world = player.getWorld().getName();

        for (int z = playerZ - radius; z <= playerZ + radius; z++) {
            StringBuilder line = new StringBuilder();
            for (int x = playerX - radius; x <= playerX + radius; x++) {
                if (x == playerX && z == playerZ) {
                    line.append("§b+");
                } else {
                    Claim c = plugin.getClaimManager().getClaim(world, x, z);
                    if (c == null) {
                        line.append("§7-");
                    } else {
                        line.append("§e#");
                    }
                }
            }
            player.sendMessage(line.toString());
        }
    }

    private void handleAdmin(Player player, String[] args) {
        if (!player.hasPermission("faction.admin")) {
            MessageUtils.sendMessage(player, "no-permission");
            return;
        }
        if (args.length < 2) {
            player.sendMessage("§cUtilisation: /f admin <disband/bypass/setpower>");
            return;
        }
        String action = args[1].toLowerCase();
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());

        switch (action) {
            case "bypass":
                data.setBypass(!data.isBypass());
                player.sendMessage("§aMode bypass: " + (data.isBypass() ? "Activé" : "Désactivé"));
                break;
            case "disband":
                if (args.length < 3) {
                    player.sendMessage("§cUtilisation: /f admin disband [faction]");
                    return;
                }
                Faction faction = plugin.getFactionManager().getFactionByName(args[2]);
                if (faction == null) {
                    MessageUtils.sendMessage(player, "faction-not-found");
                    return;
                }
                for (UUID memberId : faction.getMembers()) {
                    PlayerData memberData = plugin.getPlayerManager().getPlayerData(memberId);
                    memberData.setFactionId(null);
                    memberData.setRole(Grade.MEMBER);
                }
                plugin.getClaimManager().removeAllFactionClaims(faction.getId());
                plugin.getFactionManager().disbandFaction(faction.getId());
                player.sendMessage("§aFaction " + faction.getName() + " dissoute par un administrateur.");
                break;
            case "setpower":
                if (args.length < 4) {
                    player.sendMessage("§cUtilisation: /f admin setpower [joueur] [montant]");
                    return;
                }
                PlayerData targetData = plugin.getPlayerManager().getPlayerDataByName(args[2]);
                if (targetData == null) {
                    player.sendMessage("§cJoueur non trouvé.");
                    return;
                }
                targetData.setPower(Double.parseDouble(args[3]));
                player.sendMessage("§aPower de " + args[2] + " mis à: " + args[3]);
                break;
            default:
                player.sendMessage("§cAction admin inconnue.");
                break;
        }
    }

    private void displayHelp(Player player) {
        player.sendMessage("§6--- Commandes Faction ---");
        player.sendMessage("§e/f create [nom] §7- Créer une faction");
        player.sendMessage("§e/f join [nom] §7- Rejoindre une faction");
        player.sendMessage("§e/f leave §7- Quitter votre faction");
        player.sendMessage("§e/f disband §7- Dissoudre votre faction");
        player.sendMessage("§e/f invite add/revoke [pseudo] §7- Gérer les invitations");
        player.sendMessage("§e/f kick [pseudo] §7- Exclure un membre");
        player.sendMessage("§e/f promote/demote [pseudo] §7- Gérer les grades");
        player.sendMessage("§e/f leader [pseudo] §7- Nommer un nouveau chef");
        player.sendMessage("§e/f claim [one/all] §7- Revendiquer un terrain");
        player.sendMessage("§e/f unclaim [one/all] §7- Libérer un terrain");
        player.sendMessage("§e/f map §7- Voir la carte");
        player.sendMessage("§e/f status §7- Voir le statut de la faction");
        player.sendMessage("§e/f faction [nom] §7- Infos sur une faction");
        player.sendMessage("§e/f player [pseudo] §7- Infos sur un joueur");
        player.sendMessage("§e/f home/sethome/unsethome §7- Gérer le home");
        player.sendMessage("§e/f chat [f/t/a/p] §7- Changer de chat");
        player.sendMessage("§e/f gui §7- Ouvrir le menu de gestion");
        player.sendMessage("§e/f seechunk §7- Voir les limites du chunk");
        player.sendMessage("§e/f unstuck §7- Se débloquer");
    }

    private void handleSeechunk(Player player) {
        org.bukkit.Chunk chunk = player.getLocation().getChunk();
        int minX = chunk.getX() * 16;
        int minZ = chunk.getZ() * 16;
        int maxX = minX + 16;
        int maxZ = minZ + 16;

        for (int y = player.getLocation().getBlockY() - 2; y < player.getLocation().getBlockY() + 5; y++) {
            for (int x = minX; x <= maxX; x += 16) {
                for (int z = minZ; z <= maxZ; z++) {
                    player.spawnParticle(org.bukkit.Particle.REDSTONE, x, y, z, 1, new org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 1));
                }
            }
            for (int z = minZ; z <= maxZ; z += 16) {
                for (int x = minX; x <= maxX; x++) {
                    player.spawnParticle(org.bukkit.Particle.REDSTONE, x, y, z, 1, new org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 1));
                }
            }
        }
        player.sendMessage("§eLimites du chunk affichées avec des particules.");
    }

    private void handleRelation(Player player, String[] args, String relationName) {
        if (args.length < 2) {
            player.sendMessage("§cUtilisation: /f " + relationName.toLowerCase() + " [faction]");
            return;
        }
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) {
            MessageUtils.sendMessage(player, "not-in-faction");
            return;
        }
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!faction.isOfficer(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "not-officer");
            return;
        }

        Faction target = plugin.getFactionManager().getFactionByName(args[1]);
        if (target == null) {
            MessageUtils.sendMessage(player, "faction-not-found");
            return;
        }
        if (target.getId().equals(faction.getId())) {
            player.sendMessage("§cVous ne pouvez pas changer de relation avec votre propre faction.");
            return;
        }

        faction.getRelations().put(target.getId(), relationName);
        MessageUtils.sendMessage(player, "relation-updated", "%target%", target.getName(), "%relation%", relationName);
    }

    private void handleChat(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUtilisation: /f chat [f/t/a/public]");
            return;
        }
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        String mode = args[1].toLowerCase();
        switch (mode) {
            case "f":
            case "faction":
                data.setChatMode("FACTION");
                player.sendMessage("§aChat: FACTION");
                break;
            case "t":
            case "truce":
                data.setChatMode("TRUCE");
                player.sendMessage("§aChat: TRUCE");
                break;
            case "a":
            case "ally":
                data.setChatMode("ALLY");
                player.sendMessage("§aChat: ALLY");
                break;
            case "p":
            case "public":
                data.setChatMode("PUBLIC");
                player.sendMessage("§aChat: PUBLIC");
                break;
            default:
                player.sendMessage("§cMode de chat inconnu.");
                break;
        }
    }

    private void handlePerm(Player player) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) {
            MessageUtils.sendMessage(player, "not-in-faction");
            return;
        }
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!faction.isOfficer(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "not-officer");
            return;
        }
        fr.jules.faction.gui.FactionGUI.openMainMenu(player, faction);
    }

    private void handleUnstuck(Player player) {
        player.sendMessage("§eTéléportation vers la zone sauvage la plus proche...");
        player.teleport(player.getWorld().getSpawnLocation());
    }

    private void handleFactionSetHome(Player player) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) {
            MessageUtils.sendMessage(player, "not-in-faction");
            return;
        }
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!faction.isOfficer(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "not-officer");
            return;
        }
        faction.setHome(player.getLocation());
        MessageUtils.sendMessage(player, "faction-home-set");
    }

    private void handleFactionUnsetHome(Player player) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (data.getFactionId() == null) {
            MessageUtils.sendMessage(player, "not-in-faction");
            return;
        }
        Faction faction = plugin.getFactionManager().getFaction(data.getFactionId());
        if (!faction.isOfficer(player.getUniqueId())) {
            MessageUtils.sendMessage(player, "not-officer");
            return;
        }
        faction.setHome(null);
        MessageUtils.sendMessage(player, "faction-home-unset");
    }

    private void handleFactionHome(Player player, String[] args) {
        PlayerData data = plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        Faction faction;
        if (args.length < 2) {
            if (data.getFactionId() == null) {
                player.sendMessage("§cUtilisation: /f home [faction]");
                return;
            }
            faction = plugin.getFactionManager().getFaction(data.getFactionId());
        } else {
            faction = plugin.getFactionManager().getFactionByName(args[1]);
        }

        if (faction == null) {
            player.sendMessage("§cCette faction n'existe pas.");
            return;
        }

        if (faction.getHome() == null) {
            player.sendMessage("§cCette faction n'a pas de home.");
            return;
        }

        boolean allowed = false;
        if (data.getFactionId() != null && data.getFactionId().equals(faction.getId())) allowed = true;
        else if (data.getFactionId() != null) {
            Faction playerFaction = plugin.getFactionManager().getFaction(data.getFactionId());
            if ("ALLY".equals(playerFaction.getRelations().get(faction.getId()))) {
                allowed = faction.getPermissions().getOrDefault("ALLY_HOME", true);
            }
        }

        if (!allowed) {
            MessageUtils.sendMessage(player, "no-permission");
            return;
        }

        player.teleport(faction.getHome());
        MessageUtils.sendMessage(player, "faction-home-teleport", "%name%", faction.getName());
    }
}
