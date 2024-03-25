package me.simplicitee.project.items.item.parser;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import me.simplicitee.project.items.ItemsPlugin;
import me.simplicitee.project.items.item.ItemDataParser;

public class EnchantsParser implements ItemDataParser {
    
    private static final String PATH = "Enchants";
    
    @Override
    public String getPath() {
        return PATH;
    }

    @Override
    public void apply(String name, ItemStack item, ItemMeta meta, FileConfiguration config) {
        if (!config.contains(PATH)) return;
        
        for (String enchant : config.getStringList(PATH)) {
            String[] split = enchant.split(":");
            
            if (split.length != 2) {
                continue;
            }
            
            try {
                meta.addEnchant(Registry.ENCHANTMENT.get(NamespacedKey.minecraft(split[0].toLowerCase())), Integer.parseInt(split[1]), true);
            } catch (NumberFormatException e) {
                JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning("Unable to parse integer from '" + enchant + "' in enchants on item '" + name + "', ignoring.");
            } catch (Exception e) {
                JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning("Unable to parse enchant from '" + enchant + "' in enchants on item '" + name + "', ignoring.");
            }
        }
    }

}
