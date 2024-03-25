package me.simplicitee.project.items.item;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.simplicitee.project.items.item.parser.AttributesParser;
import me.simplicitee.project.items.item.parser.DisplayNameParser;
import me.simplicitee.project.items.item.parser.DurabilityParser;
import me.simplicitee.project.items.item.parser.EnchantsParser;
import me.simplicitee.project.items.item.parser.FlagsParser;
import me.simplicitee.project.items.item.parser.LoreParser;
import me.simplicitee.project.items.item.parser.ModelParser;
import me.simplicitee.project.items.item.parser.RecipeParser;
import me.simplicitee.project.items.item.parser.UnbreakableParser;
import me.simplicitee.project.items.item.parser.UsesParser;

public enum ItemData {

    ATTRIBUTES      (new AttributesParser()),
    DISPLAY_NAME    (new DisplayNameParser()),
    DURABILITY      (new DurabilityParser()),
    ENCHANTS        (new EnchantsParser()),
    FLAGS           (new FlagsParser()),
    LORE            (new LoreParser()),
    MODEL           (new ModelParser()),
    RECIPE          (new RecipeParser()),
    UNBREAKABLE     (new UnbreakableParser()),
    USES            (new UsesParser()),
    ;
    
    private final String path;
    private final ItemDataParser parser;
    
    ItemData(ItemDataParser parser) {
        this.path = parser.getPath();
        this.parser = parser;
    }
    
    public String path() {
        return path;
    }
    
    public boolean exists(FileConfiguration config) {
        return config.contains(path);
    }
    
    public static void parseAll(String fileName, ItemStack item, ItemMeta meta, FileConfiguration config) {
        for (ItemData data : values()) {
            data.parser.apply(fileName, item, meta, config);
        }
    }
}
