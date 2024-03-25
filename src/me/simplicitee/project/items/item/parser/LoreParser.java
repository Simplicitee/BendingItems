package me.simplicitee.project.items.item.parser;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.simplicitee.project.items.item.ItemDataParser;
import net.md_5.bungee.api.ChatColor;

public class LoreParser implements ItemDataParser {
    
    private static final String PATH = "Lore";

    @Override
    public String getPath() {
        return PATH;
    }

    @Override
    public void apply(String name, ItemStack item, ItemMeta meta, FileConfiguration config) {
        if (!config.contains(PATH)) return;
        
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        for (String line : config.getStringList(PATH)) {
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        
        meta.setLore(lore);
    }

}
