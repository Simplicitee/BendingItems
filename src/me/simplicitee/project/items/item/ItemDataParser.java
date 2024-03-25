package me.simplicitee.project.items.item;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public interface ItemDataParser {
    
    String getPath();

    void apply(String name, ItemStack item, ItemMeta meta, FileConfiguration config);
}
