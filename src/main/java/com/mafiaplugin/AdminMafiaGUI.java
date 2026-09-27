package com.mafiaplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class AdminMafiaGUI {

    public static final String TITLE = ChatColor.DARK_RED + "Admin Mafia";

    public static final int SLOT_CREA = 11;
    public static final int SLOT_ELIMINA = 13;
    public static final int SLOT_INFO = 15;

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, TITLE);

        gui.setItem(SLOT_CREA, createItem(Material.LIME_DYE, "§aCrea Mafia", "§7Clicca per creare una nuova mafia"));
        gui.setItem(SLOT_ELIMINA, createItem(Material.BARRIER, "§cElimina Mafia", "§7Clicca per eliminare una mafia esistente"));
        gui.setItem(SLOT_INFO, createItem(Material.BOOK, "§bInfo Mafia", "§7Clicca per vedere i membri di una mafia"));

        player.openInventory(gui);
    }

    private static ItemStack createItem(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);

        List<String> loreList = new ArrayList<>();
        loreList.add(lore);
        meta.setLore(loreList);

        item.setItemMeta(meta);
        return item;
    }
}
