package me.simplicitee.project.items.item.parser;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.simplicitee.project.items.item.ItemDataParser;

public class ModelParser implements ItemDataParser {
    
    private static final String PATH = "Model";

    @Override
    public String getPath() {
        return PATH;
    }

    @Override
    public void apply(String name, ItemStack item, ItemMeta meta, FileConfiguration config) {
        if (config.contains(PATH)) {
            meta.setCustomModelData(config.getInt(PATH));
        }
    }

}
