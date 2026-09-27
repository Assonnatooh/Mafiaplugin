package com.mafiaplugin;

import org.bukkit.plugin.java.JavaPlugin;

public class MafiaPlugin extends JavaPlugin {

    private MafiaManager manager;

    @Override
    public void onEnable() {
        manager = new MafiaManager(this);
        MafiaListener listener = new MafiaListener(this, manager);

        getServer().getPluginManager().registerEvents(listener, this);

        getCommand("adminmafia").setExecutor(new AdminMafiaCommand());
        getCommand("mafia").setExecutor(new MafiaCommand(manager, listener));
        getCommand("mc").setExecutor(new McCommand(manager));

        getLogger().info("MafiaPlugin abilitato!");
    }

    @Override
    public void onDisable() {
        if (manager != null) {
            manager.save();
        }
        getLogger().info("MafiaPlugin disabilitato.");
    }
}
