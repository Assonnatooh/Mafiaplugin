package com.mafiaplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MafiaListener implements Listener {

    private enum Step {
        CREATE_NAME,
        CREATE_LEADER,
        DISBAND_NAME,
        INFO_NAME
    }

    private final JavaPlugin plugin;
    private final MafiaManager manager;

    private final Map<UUID, Step> pendingStep = new HashMap<>();
    // Usato per ricordare il nome della mafia tra lo step CREATE_NAME e CREATE_LEADER
    private final Map<UUID, String> pendingData = new HashMap<>();

    public MafiaListener(JavaPlugin plugin, MafiaManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    // ---------------------------------------------------------------
    // GUI /adminmafia
    // ---------------------------------------------------------------

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getView().getTitle() == null) return;
        if (!event.getView().getTitle().equals(AdminMafiaGUI.TITLE)) return;

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        UUID id = player.getUniqueId();

        int slot = event.getRawSlot();

        if (slot == AdminMafiaGUI.SLOT_CREA) {
            player.closeInventory();
            pendingStep.put(id, Step.CREATE_NAME);
            player.sendMessage(ChatColor.GREEN + "Scrivi in chat il NOME della nuova mafia (o 'annulla' per annullare):");
        } else if (slot == AdminMafiaGUI.SLOT_ELIMINA) {
            player.closeInventory();
            pendingStep.put(id, Step.DISBAND_NAME);
            player.sendMessage(ChatColor.RED + "Scrivi in chat il NOME della mafia da eliminare (o 'annulla' per annullare):");
        } else if (slot == AdminMafiaGUI.SLOT_INFO) {
            player.closeInventory();
            pendingStep.put(id, Step.INFO_NAME);
            player.sendMessage(ChatColor.AQUA + "Scrivi in chat il NOME della mafia di cui vuoi vedere le info (o 'annulla' per annullare):");
        }
    }

    // ---------------------------------------------------------------
    // Cattura risposte in chat
    // ---------------------------------------------------------------

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID id = player.getUniqueId();

        Step step = pendingStep.get(id);
        if (step == null) return;

        event.setCancelled(true);
        String message = event.getMessage().trim();

        // Il resto va eseguito sul thread principale (manipola inventari/file)
        Bukkit.getScheduler().runTask(plugin, () -> processStep(player, step, message));
    }

    private void processStep(Player player, Step step, String message) {
        UUID id = player.getUniqueId();

        if (message.equalsIgnoreCase("annulla")) {
            pendingStep.remove(id);
            pendingData.remove(id);
            player.sendMessage(ChatColor.GRAY + "Operazione annullata.");
            return;
        }

        switch (step) {
            case CREATE_NAME:
                handleCreateName(player, message);
                break;
            case CREATE_LEADER:
                handleCreateLeader(player, message);
                break;
            case DISBAND_NAME:
                handleDisbandName(player, message);
                break;
            case INFO_NAME:
                handleInfoName(player, message);
                break;
        }
    }

    private void handleCreateName(Player player, String nome) {
        if (manager.exists(nome)) {
            player.sendMessage(ChatColor.RED + "Esiste già una mafia con questo nome!");
            pendingStep.remove(player.getUniqueId());
            return;
        }

        pendingData.put(player.getUniqueId(), nome);
        pendingStep.put(player.getUniqueId(), Step.CREATE_LEADER);
        player.sendMessage(ChatColor.GREEN + "Ora scrivi il NICKNAME del giocatore che sarà il capo di '" + nome + "':");
    }

    private void handleCreateLeader(Player player, String nick) {
        UUID id = player.getUniqueId();
        String nomeMafia = pendingData.remove(id);
        pendingStep.remove(id);

        if (nomeMafia == null) {
            player.sendMessage(ChatColor.RED + "Qualcosa è andato storto, riprova da /adminmafia.");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(nick);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "Questo giocatore non ha mai giocato su questo server!");
            return;
        }
        if (manager.isInAnyMafia(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Questo giocatore è già in un'altra mafia!");
            return;
        }
        if (manager.exists(nomeMafia)) {
            player.sendMessage(ChatColor.RED + "Esiste già una mafia con questo nome!");
            return;
        }

        manager.createMafia(nomeMafia, target.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "Mafia '" + nomeMafia + "' creata! Capo: " + target.getName());

        if (target.isOnline() && target.getPlayer() != null) {
            target.getPlayer().sendMessage(ChatColor.GOLD + "Sei stato nominato capo della mafia '" + nomeMafia + "'!");
        }
    }

    private void handleDisbandName(Player player, String nome) {
        pendingStep.remove(player.getUniqueId());

        MafiaData data = manager.get(nome);
        if (data == null) {
            player.sendMessage(ChatColor.RED + "Nessuna mafia trovata con questo nome!");
            return;
        }

        for (UUID uid : data.getMembers()) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null) {
                p.sendMessage(ChatColor.RED + "La tua mafia '" + data.getDisplayName() + "' è stata sciolta da uno staff.");
            }
        }

        manager.disbandMafia(nome);
        player.sendMessage(ChatColor.GREEN + "Mafia '" + nome + "' eliminata.");
    }

    private void handleInfoName(Player player, String nome) {
        pendingStep.remove(player.getUniqueId());

        MafiaData data = manager.get(nome);
        if (data == null) {
            player.sendMessage(ChatColor.RED + "Nessuna mafia trovata con questo nome!");
            return;
        }

        sendMafiaInfo(player, data);
    }

    /** Usato sia dal flusso admin che dal comando /mafia info dei giocatori. */
    public void sendMafiaInfo(Player viewer, MafiaData data) {
        OfflinePlayer leader = Bukkit.getOfflinePlayer(data.getLeader());

        viewer.sendMessage(ChatColor.GOLD + "=== Mafia " + data.getDisplayName() + " ===");
        viewer.sendMessage(ChatColor.GRAY + "Capo: " + ChatColor.WHITE + leader.getName());
        viewer.sendMessage(ChatColor.GRAY + "Membri (" + data.getMembers().size() + "):");

        for (UUID uid : data.getMembers()) {
            OfflinePlayer member = Bukkit.getOfflinePlayer(uid);
            boolean online = member.isOnline();
            viewer.sendMessage((online ? ChatColor.GREEN : ChatColor.DARK_GRAY) + " - " + member.getName());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        pendingStep.remove(id);
        pendingData.remove(id);
    }
}
