package me.simplicitee.project.items.item.parser;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import me.simplicitee.project.items.ItemManager;
import me.simplicitee.project.items.item.ItemDataParser;

public class UsesParser implements ItemDataParser {
    
    private static final String PATH = "Uses";

    @Override
    public String getPath() {
        return PATH;
    }

    @Override
    public void apply(String name, ItemStack item, ItemMeta meta, FileConfiguration config) {
        if (config.contains(PATH)) {
            meta.getPersistentDataContainer().set(ItemManager.USES_KEY, PersistentDataType.INTEGER, config.getInt(PATH));
        }
    }

}
