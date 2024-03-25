package me.simplicitee.project.items.item.parser;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import me.simplicitee.project.items.ItemsPlugin;
import me.simplicitee.project.items.item.ItemDataParser;

public class FlagsParser implements ItemDataParser {
    
    private static final String PATH = "Flags";

    @Override
    public String getPath() {
        return PATH;
    }

    @Override
    public void apply(String name, ItemStack item, ItemMeta meta, FileConfiguration config) {
        if (!config.contains(PATH)) return;
        
        for (String flag : config.getStringList(PATH)) {
            try {
                meta.addItemFlags(ItemFlag.valueOf(flag.toUpperCase()));
            } catch (Exception e) {
                JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning("Unable to parse item flag '" + flag + "' in item '" + name + "'");
            }
        }
    }

}
