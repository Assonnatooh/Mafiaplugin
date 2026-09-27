package com.mafiaplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class McCommand implements CommandExecutor {

    private final MafiaManager manager;

    public McCommand(MafiaManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Questo comando può essere usato solo in gioco.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(ChatColor.YELLOW + "Uso: /mc <nome mafia> <messaggio>");
            return true;
        }

        String nomeMafia = args[0];
        MafiaData mafia = manager.get(nomeMafia);
        if (mafia == null) {
            player.sendMessage(ChatColor.RED + "Nessuna mafia trovata con questo nome!");
            return true;
        }

        MafiaData playerMafia = manager.getByPlayer(player.getUniqueId());
        if (playerMafia == null || !playerMafia.getDisplayName().equalsIgnoreCase(nomeMafia)) {
            player.sendMessage(ChatColor.RED + "Non fai parte della mafia '" + nomeMafia + "'!");
            return true;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) sb.append(" ");
            sb.append(args[i]);
        }
        String messaggio = sb.toString();

        String formatted = ChatColor.DARK_PURPLE + "[MAFIA " + mafia.getDisplayName() + "] "
                + ChatColor.WHITE + player.getName() + ChatColor.GRAY + ": " + ChatColor.WHITE + messaggio;

        for (UUID uid : mafia.getMembers()) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null) {
                p.sendMessage(formatted);
            }
        }
        return true;
    }
}
