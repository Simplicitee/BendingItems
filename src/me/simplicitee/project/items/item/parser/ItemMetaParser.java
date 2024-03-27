package me.simplicitee.project.items.item.parser;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Axolotl;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.AxolotlBucketMeta;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.plugin.java.JavaPlugin;

import me.simplicitee.project.items.ItemsPlugin;
import me.simplicitee.project.items.item.ItemDataParser;
import net.md_5.bungee.api.ChatColor;

public class ItemMetaParser implements ItemDataParser {
    
    private static final String PATH = "ItemMeta";
    
    private static final String ARMOR_TRIM = PATH + ".ArmorTrim";
    private static final String ARMOR_TRIM_MATERIAL = ARMOR_TRIM + ".Material";
    private static final String ARMOR_TRIM_PATTERN = ARMOR_TRIM + ".Pattern";
    
    private static final String AXOLOTL_BUCKET = PATH + ".AxolotlBucket";
    private static final String AXOLOTL_BUCKET_VARIANT = AXOLOTL_BUCKET + ".Variant";
    
    private static final String BANNER = PATH + ".Banner";
    private static final String BANNER_PATTERNS = BANNER + ".Patterns";
    
    private static final String BLOCK_DATA = PATH + ".BlockData";
    private static final String BLOCK_DATA_VALUE = BLOCK_DATA + ".Value";
    
    /* AVOIDING, CAN'T DESERIALIZE NICELY
    private static final String BLOCK_STATE = PATH + ".BlockState";
    private static final String BLOCK_STATE_VALUE = BLOCK_STATE + ".Value";
    */
    
    private static final String BOOK = PATH + ".Book";
    private static final String BOOK_TITLE = BOOK + ".Title";
    private static final String BOOK_AUTHOR = BOOK + ".Author";
    private static final String BOOK_GENERATION = BOOK + ".Generation";
    private static final String BOOK_PAGES = BOOK + ".Pages";
    
    private static final String COMPASS = PATH + ".Compass";
    private static final String COMPASS_TARGET_WORLD = COMPASS + ".TargetWorld";
    private static final String COMPASS_TARGET_X = COMPASS + ".TargetX";
    private static final String COMPASS_TARGET_Y = COMPASS + ".TargetY";
    private static final String COMPASS_TARGET_Z = COMPASS + ".TargetZ";
    
    private static final String LEATHER_ARMOR = PATH + ".LeatherArmor";
    private static final String LEATHER_ARMOR_COLOR = LEATHER_ARMOR + ".Color";

    @Override
    public String getPath() {
        return PATH;
    }

    @Override
    public void apply(String name, ItemStack item, ItemMeta meta, FileConfiguration config) {
        if (!config.contains(PATH)) return;
        
        if (config.contains(ARMOR_TRIM) && meta instanceof ArmorMeta armorMeta) {
            parseArmorTrim(name, armorMeta, config);
        }
        
        if (config.contains(AXOLOTL_BUCKET) && meta instanceof AxolotlBucketMeta abMeta) {
            parseAxolotlBucket(name, abMeta, config);
        }
        
        if (config.contains(BANNER) && meta instanceof BannerMeta bannerMeta) {
            parseBanner(name, bannerMeta, config);
        }
        
        if (config.contains(BLOCK_DATA) && meta instanceof BlockDataMeta blockDataMeta) {
            parseBlockData(name, blockDataMeta, config);
        }
        
        if (config.contains(BOOK) && meta instanceof BookMeta bookMeta) {
            parseBook(name, bookMeta, config);
        }
        
        if (config.contains(COMPASS) && meta instanceof CompassMeta compassMeta) {
        	parseCompass(name, compassMeta, config);
        }
        
        if (config.contains(LEATHER_ARMOR) && meta instanceof LeatherArmorMeta armorMeta) {
        	parseLeatherArmor(name, armorMeta, config);
        }
    }

    private void parseArmorTrim(String name, ArmorMeta meta, FileConfiguration config) {
        if (!config.contains(ARMOR_TRIM_MATERIAL)) {
            JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning(ARMOR_TRIM_MATERIAL + " not found, ignoring ArmorTrim meta.");
            return;
        } else if (!config.contains(ARMOR_TRIM_PATTERN)) {
            JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning(ARMOR_TRIM_PATTERN + " not found, ignoring ArmorTrim meta.");
            return;
        }
        
        String value = config.getString(ARMOR_TRIM_MATERIAL);
        TrimMaterial trimMat = Registry.TRIM_MATERIAL.match(value);
        if (trimMat == null) {
            JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning("Unable to parse trim material '" + value + "' for item '" + name + "'");
            return;
        }
        
        value = config.getString(ARMOR_TRIM_PATTERN);
        TrimPattern trimPat = Registry.TRIM_PATTERN.match(value);
        if (trimPat == null) {
            JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning("Unable to parse trim pattern '" + value + "' for item '" + name + "'");
            return;
        }
        
        meta.setTrim(new ArmorTrim(trimMat, trimPat));
    }

    private void parseAxolotlBucket(String name, AxolotlBucketMeta meta, FileConfiguration config) {
        if (!config.contains(AXOLOTL_BUCKET_VARIANT)) {
            JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning(AXOLOTL_BUCKET_VARIANT + " not found, ignoring AxolotlBucket meta.");
            return;
        }
        
        String value = config.getString(AXOLOTL_BUCKET_VARIANT).toUpperCase();
        Axolotl.Variant variant;
        try {
            variant = Axolotl.Variant.valueOf(config.getString(AXOLOTL_BUCKET_VARIANT).toUpperCase());
        } catch (Exception e) {
            JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning("Unable to parse axolotl variant from '" + value + "' for item '" + name + "'");
            return;
        }
        
        meta.setVariant(variant);
    }
    
    private void parseBanner(String name, BannerMeta meta, FileConfiguration config) {
        if (!config.contains(BANNER_PATTERNS)) {
            JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning(BANNER_PATTERNS + " not found, ignoring Banner meta.");
            return;
        }
        
        List<Pattern> patterns = new ArrayList<>();
        for (String pattern : config.getStringList(BANNER_PATTERNS)) {
            String[] split = pattern.split(";");
            
            if (split.length != 2) {
                JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning("Unable to parse '" + pattern + "' as a banner pattern for item '" + name + "', expected <PATTERN TYPE>;<DYE COLOR>, ignoring line.");
                continue;
            }
            
            PatternType type;
            try {
                type = PatternType.valueOf(split[0].toUpperCase());
            } catch (Exception e) {
                JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning("Unable to parse pattern type from '" + split[0] + "' for item '" + name + "', ignoring line.");
                continue;
            }
            
            DyeColor color;
            try {
                color = DyeColor.valueOf(split[1].toUpperCase());
            } catch (Exception e) {
                JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning("Unable to parse banner dye color from '" + split[1] + "' for item '" + name + "', ignoring line.");
                continue;
            }
            
            patterns.add(new Pattern(color, type));
        }
        
        meta.setPatterns(patterns);
    }
    
    private void parseBlockData(String name, BlockDataMeta meta, FileConfiguration config) {
        if (!config.contains(BLOCK_DATA_VALUE)) {
            JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning(BLOCK_DATA_VALUE + " not found, ignoring BlockData meta.");
            return;
        }
        
        String value = config.getString(BLOCK_DATA_VALUE);
        BlockData data;
        try {
            data = Bukkit.createBlockData(value);
        } catch (Exception e) {
            JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning("Unable to parse block data from '" + value + "' for item '" + name + "', ignoring.");
            return;
        }
        
        meta.setBlockData(data);
    }

    private void parseBook(String name, BookMeta meta, FileConfiguration config) {
        if (!config.contains(BOOK_TITLE)) {
            JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning(BOOK_TITLE + " not found, ignoring Book meta.");
            return;
        } else if (!config.contains(BOOK_AUTHOR)) {
            JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning(BOOK_AUTHOR + " not found, ignoring Book meta.");
            return;
        } else if (!config.contains(BOOK_PAGES)) {
            JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning(BOOK_PAGES + " not found, ignoring Book meta.");
            return;
        }
        
        meta.setTitle(ChatColor.translateAlternateColorCodes('&', config.getString(BOOK_TITLE)));
        meta.setAuthor(config.getString(BOOK_AUTHOR));
        
        BookMeta.Generation gen;
        try {
            gen = BookMeta.Generation.valueOf(config.getString(BOOK_GENERATION).toUpperCase());
        } catch (Exception e) {
            JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning("Unable to parse book generation, using COPY_OF_COPY.");
            gen = BookMeta.Generation.COPY_OF_COPY;
        }
        
        meta.setGeneration(gen);
        meta.setPages(config.getStringList(BOOK_PAGES).stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).toList());
    }

    private void parseCompass(String name, CompassMeta meta, FileConfiguration config) {
    	if (!config.contains(COMPASS_TARGET_WORLD)) {
    		JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning(COMPASS_TARGET_WORLD + " not found, ignoring Compass meta.");
            return;
    	} else if (!config.contains(COMPASS_TARGET_X)) {
    		JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning(COMPASS_TARGET_X + " not found, ignoring Compass meta.");
            return;
    	} else if (!config.contains(COMPASS_TARGET_Y)) {
    		JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning(COMPASS_TARGET_Y + " not found, ignoring Compass meta.");
            return;
    	} else if (!config.contains(COMPASS_TARGET_Z)) {
    		JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning(COMPASS_TARGET_Z + " not found, ignoring Compass meta.");
            return;
    	}
    	
    	String value = config.getString(COMPASS_TARGET_WORLD);
    	World world = Bukkit.getWorld(value);
    	if (world == null) {
    		JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning("Unable to parse world from '" + value + "' for item '" + name + "', ignoring Compass meta.");
            return;
    	}
    	
    	value = config.getString(COMPASS_TARGET_X);
    	double x;
    	try {
    		x = Double.parseDouble(value);
    	} catch (Exception e) {
    		JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning("Unable to parse x coordinate from '" + value + "' for item '" + name + "', ignoring Compass meta.");
            return;
    	}
    	
    	value = config.getString(COMPASS_TARGET_X);
    	double y;
    	try {
    		y = Double.parseDouble(value);
    	} catch (Exception e) {
    		JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning("Unable to parse y coordinate from '" + value + "' for item '" + name + "', ignoring Compass meta.");
            return;
    	}
    	
    	value = config.getString(COMPASS_TARGET_X);
    	double z;
    	try {
    		z = Double.parseDouble(value);
    	} catch (Exception e) {
    		JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning("Unable to parse z coordinate from '" + value + "' for item '" + name + "', ignoring Compass meta.");
            return;
    	}
    	
    	meta.setLodestoneTracked(false); //assume there's no lodestone at the target location
    	meta.setLodestone(new Location(world, x, y, z));
    }
    
    private void parseLeatherArmor(String name, LeatherArmorMeta meta, FileConfiguration config) {
    	if (!config.contains(LEATHER_ARMOR_COLOR)) {
    		JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning(LEATHER_ARMOR_COLOR + " not found, ignoring LeatherArmor meta.");
            return;
    	}
    	
    	String value = config.getString(LEATHER_ARMOR_COLOR);
    	if (value.startsWith("#")) {
    		value = value.substring(1);
    	}
    	
    	int rgb;
    	try {
    		rgb = Integer.parseInt(value, 16);
    	} catch (Exception e) {
    		JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning("Unable to parse color from '" + value + "' for item '" + name + "', ignoring LeatherArmor meta.");
            return;
    	}
    	
    	meta.setColor(Color.fromRGB(rgb));
    }
}