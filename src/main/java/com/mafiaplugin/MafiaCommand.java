package com.mafiaplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MafiaCommand implements CommandExecutor {

    private final MafiaManager manager;
    private final MafiaListener listener;

    public MafiaCommand(MafiaManager manager, MafiaListener listener) {
        this.manager = manager;
        this.listener = listener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Questo comando può essere usato solo in gioco.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "Uso: /mafia aggiungi <nick> | /mafia rimuovi <nick> | /mafia info");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "aggiungi":
                return handleAggiungi(player, args);
            case "rimuovi":
                return handleRimuovi(player, args);
            case "info":
                return handleInfo(player);
            default:
                player.sendMessage(ChatColor.YELLOW + "Uso: /mafia aggiungi <nick> | /mafia rimuovi <nick> | /mafia info");
                return true;
        }
    }

    private boolean handleAggiungi(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.YELLOW + "Uso: /mafia aggiungi <nick>");
            return true;
        }

        MafiaData mafia = manager.getByPlayer(player.getUniqueId());
        if (mafia == null) {
            player.sendMessage(ChatColor.RED + "Non fai parte di nessuna mafia!");
            return true;
        }
        if (!mafia.getLeader().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Solo il capo della mafia può aggiungere membri!");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "Questo giocatore non ha mai giocato su questo server!");
            return true;
        }
        if (manager.isInAnyMafia(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Questo giocatore è già in una mafia!");
            return true;
        }

        manager.addMember(mafia.getDisplayName(), target.getUniqueId());
        player.sendMessage(ChatColor.GREEN + target.getName() + " è stato aggiunto alla mafia!");

        if (target.isOnline() && target.getPlayer() != null) {
            target.getPlayer().sendMessage(ChatColor.GOLD + "Sei stato aggiunto alla mafia '" + mafia.getDisplayName() + "'!");
        }
        return true;
    }

    private boolean handleRimuovi(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.YELLOW + "Uso: /mafia rimuovi <nick>");
            return true;
        }

        MafiaData mafia = manager.getByPlayer(player.getUniqueId());
        if (mafia == null) {
            player.sendMessage(ChatColor.RED + "Non fai parte di nessuna mafia!");
            return true;
        }
        if (!mafia.getLeader().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Solo il capo della mafia può rimuovere membri!");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

        if (target.getUniqueId().equals(mafia.getLeader())) {
            player.sendMessage(ChatColor.RED + "Il capo non può rimuovere se stesso! Chiedi a uno staff di eliminare la mafia con /adminmafia.");
            return true;
        }
        if (!mafia.getMembers().contains(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Questo giocatore non fa parte della tua mafia!");
            return true;
        }

        manager.removeMember(mafia.getDisplayName(), target.getUniqueId());
        player.sendMessage(ChatColor.GREEN + target.getName() + " è stato rimosso dalla mafia!");

        if (target.isOnline() && target.getPlayer() != null) {
            target.getPlayer().sendMessage(ChatColor.RED + "Sei stato rimosso dalla mafia '" + mafia.getDisplayName() + "'.");
        }
        return true;
    }

    private boolean handleInfo(Player player) {
        MafiaData mafia = manager.getByPlayer(player.getUniqueId());
        if (mafia == null) {
            player.sendMessage(ChatColor.RED + "Non fai parte di nessuna mafia!");
            return true;
        }

        listener.sendMafiaInfo(player, mafia);
        return true;
    }
}
