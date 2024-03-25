package me.simplicitee.project.items.item.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import me.simplicitee.project.items.ItemsPlugin;
import me.simplicitee.project.items.item.ItemDataParser;

public class RecipeParser implements ItemDataParser {

    private static final String PATH = "Recipe";
    private static final char[] CHARS = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i'};
    
    @Override
    public String getPath() {
        return PATH;
    }

    @Override
    public void apply(String name, ItemStack item, ItemMeta meta, FileConfiguration config) {
        if (!config.contains(PATH)) return;
        
        Logger logger = JavaPlugin.getPlugin(ItemsPlugin.class).getLogger();
        
        if (!config.contains(PATH + ".Ingredients")) {
            logger.warning("Recipe for '" + name + "' requires a list of ingredients under the config path 'Recipe.Ingredients'");
            return;
        } else if (!config.contains(PATH + ".Shaped")) {
            logger.warning("Recipe for '" + name + "' requires a boolean (true / false) under the config path 'Recipe.Shaped'");
            return;
        }
        
        boolean isShaped = config.getBoolean(PATH + ".Shaped");
        List<String> ingredients = config.getStringList(PATH + ".Ingredients");
        
        if (ingredients == null) {
            logger.warning("Ingredients list for '" + name + "' recipe not found!");
            return;
        } else if (ingredients.isEmpty()) {
            logger.warning("Ingredients list for '" + name + "' recipe cannot be empty!");
            return;
        } else if (ingredients.size() > 9) {
            logger.warning("Ingredients list for '" + name + "' recipe cannot be longer than 9 items!");
            return;
        }
        
        List<Material> mats = new ArrayList<>();
        for (String mat : ingredients) {
            try {
                mats.add(Material.valueOf(mat.toUpperCase()));
            } catch (Exception e) {
                logger.warning("Unable to parse material from '" + mat + "' in '" + name + "' recipe!");
                return;
            }
        }
        
        Recipe recipe = null;
        
        if (isShaped) {
            if (!config.contains(PATH + ".Shape")) {
                logger.warning("Recipe for '" + name + "' requires a shape under the config path 'Recipe.Shape'");
                return;
            }
            
            ShapedRecipe shaped = new ShapedRecipe(new NamespacedKey(JavaPlugin.getPlugin(ItemsPlugin.class), name), item);
            
            shaped.shape(config.getStringList(PATH + ".Shape").toArray(new String[0]));
            
            for (int i = 0; i < ingredients.size(); ++i) {
                shaped.setIngredient(CHARS[i], mats.get(i));
            }
        } else {
            ShapelessRecipe shapeless = new ShapelessRecipe(new NamespacedKey(JavaPlugin.getPlugin(ItemsPlugin.class), name), item);
            
            for (Material ingredient : mats) {
                shapeless.addIngredient(ingredient);
            }
        }
        
        Bukkit.addRecipe(recipe);
    }

}
