package com.mafiaplugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MafiaManager {

    private final JavaPlugin plugin;
    private final File file;
    private FileConfiguration config;

    // chiave = nome mafia in minuscolo (per confronti case-insensitive)
    private final Map<String, MafiaData> mafias = new HashMap<>();
    // giocatore -> chiave mafia (minuscolo). Un giocatore sta in una sola mafia alla volta.
    private final Map<UUID, String> playerMafia = new HashMap<>();

    public MafiaManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "mafias.yml");
        load();
    }

    private void load() {
        plugin.getDataFolder().mkdirs();
        if (!file.exists()) {
            config = new YamlConfiguration();
            return;
        }

        config = YamlConfiguration.loadConfiguration(file);
        if (!config.isConfigurationSection("mafias")) return;

        for (String key : config.getConfigurationSection("mafias").getKeys(false)) {
            String path = "mafias." + key;
            String displayName = config.getString(path + ".nome", key);
            String leaderStr = config.getString(path + ".capo");
            if (leaderStr == null) continue;

            UUID leader = UUID.fromString(leaderStr);
            MafiaData data = new MafiaData(displayName, leader);
            data.getMembers().clear(); // ricostruiamo la lista membri da quella salvata

            List<String> membersStr = config.getStringList(path + ".membri");
            for (String m : membersStr) {
                UUID uid = UUID.fromString(m);
                data.getMembers().add(uid);
                playerMafia.put(uid, key);
            }

            if (!data.getMembers().contains(leader)) {
                data.getMembers().add(leader);
                playerMafia.put(leader, key);
            }

            mafias.put(key, data);
        }
    }

    public void save() {
        config = new YamlConfiguration();
        for (Map.Entry<String, MafiaData> entry : mafias.entrySet()) {
            String key = entry.getKey();
            MafiaData data = entry.getValue();
            String path = "mafias." + key;

            config.set(path + ".nome", data.getDisplayName());
            config.set(path + ".capo", data.getLeader().toString());

            List<String> membersStr = new ArrayList<>();
            for (UUID uid : data.getMembers()) {
                membersStr.add(uid.toString());
            }
            config.set(path + ".membri", membersStr);
        }

        try {
            plugin.getDataFolder().mkdirs();
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Impossibile salvare mafias.yml: " + e.getMessage());
        }
    }

    public boolean exists(String nome) {
        return mafias.containsKey(nome.toLowerCase());
    }

    public MafiaData get(String nome) {
        if (nome == null) return null;
        return mafias.get(nome.toLowerCase());
    }

    public MafiaData getByPlayer(UUID uuid) {
        String key = playerMafia.get(uuid);
        if (key == null) return null;
        return mafias.get(key);
    }

    public boolean isInAnyMafia(UUID uuid) {
        return playerMafia.containsKey(uuid);
    }

    public MafiaData createMafia(String nome, UUID leader) {
        String key = nome.toLowerCase();
        MafiaData data = new MafiaData(nome, leader);
        mafias.put(key, data);
        playerMafia.put(leader, key);
        save();
        return data;
    }

    public boolean disbandMafia(String nome) {
        String key = nome.toLowerCase();
        MafiaData data = mafias.remove(key);
        if (data == null) return false;

        for (UUID uid : data.getMembers()) {
            playerMafia.remove(uid);
        }
        save();
        return true;
    }

    public boolean addMember(String nome, UUID target) {
        MafiaData data = get(nome);
        if (data == null) return false;

        data.getMembers().add(target);
        playerMafia.put(target, nome.toLowerCase());
        save();
        return true;
    }

    public boolean removeMember(String nome, UUID target) {
        MafiaData data = get(nome);
        if (data == null) return false;

        data.getMembers().remove(target);
        playerMafia.remove(target);
        save();
        return true;
    }
}
