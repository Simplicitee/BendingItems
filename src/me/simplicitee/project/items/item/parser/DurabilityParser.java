package me.simplicitee.project.items.item.parser;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import me.simplicitee.project.items.item.ItemDataParser;

public class DurabilityParser implements ItemDataParser {
    
    private static final String PATH = "Durability";
    
    @Override
    public String getPath() {
        return PATH;
    }

    @Override
    public void apply(String name, ItemStack item, ItemMeta meta, FileConfiguration config) {
        if (config.contains(PATH) && meta instanceof Damageable) {
            ((Damageable) meta).setDamage(-(config.getInt(PATH) - item.getType().getMaxDurability()));
        }
    }

}
