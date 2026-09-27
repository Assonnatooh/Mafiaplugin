package com.mafiaplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminMafiaCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Questo comando può essere usato solo in gioco.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("mafia.admin")) {
            player.sendMessage("§cNon hai il permesso per usare questo comando.");
            return true;
        }

        AdminMafiaGUI.open(player);
        return true;
    }
}
