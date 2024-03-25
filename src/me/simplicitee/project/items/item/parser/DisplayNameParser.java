package me.simplicitee.project.items.item.parser;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.simplicitee.project.items.item.ItemDataParser;
import net.md_5.bungee.api.ChatColor;

public class DisplayNameParser implements ItemDataParser {
    
    private static final String PATH = "Display";

    @Override
    public String getPath() {
        return PATH;
    }

    @Override
    public void apply(String name, ItemStack item, ItemMeta meta, FileConfiguration config) {
        if (config.contains(PATH)) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString(PATH)));
        } else {
            meta.setDisplayName(name);
        }
    }

}
