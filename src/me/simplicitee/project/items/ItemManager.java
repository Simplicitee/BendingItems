package me.simplicitee.project.items;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.CoreAbility;

import me.simplicitee.project.items.BendingItem.Usage;
import me.simplicitee.project.items.item.ItemData;
import me.simplicitee.project.items.item.RecipeParser;
import net.md_5.bungee.api.ChatColor;

public final class ItemManager {

	private ItemManager() {}
	
	public static final NamespacedKey ID_KEY = new NamespacedKey(JavaPlugin.getPlugin(ItemsPlugin.class), "itemid");
	public static final NamespacedKey USES_KEY = new NamespacedKey(JavaPlugin.getPlugin(ItemsPlugin.class), "itemuses");
	
	private static final Map<String, BendingItem> NAME_CACHE = new HashMap<>();
	private static final Map<Integer, BendingItem> ID_CACHE = new HashMap<>();
	private static final Map<Player, BendingItem> EQUIPPED = new HashMap<>();
	
    private static final String MATERIAL_PATH = "Material";
    public static final String USAGE_PATH = "Usage";
    public static final String ELEMENT_PATH = "Element";
    public static final String MODS_PATH = "Mods";
	
	public static void modify(CoreAbility ability) {
		Player player = ability.getPlayer();
		
		ItemStack mainItem = player.getInventory().getItemInMainHand(), offItem = player.getInventory().getItemInOffHand();
		BendingItem main = get(mainItem), off = get(offItem);
		
		if (main != null && main.getUsage() == Usage.HOLDING) {
			if (main.applyMods(ability)) {
			    use(player, player.getInventory().getHeldItemSlot(), main);
			}
		}
		
		if (off != null && off.getUsage() == Usage.HOLDING) {
			if (off.applyMods(ability)) {
			    use(player, 40, off);
			}
		}
		
		ItemStack[] wearing = player.getInventory().getArmorContents();
		for (int slot = 0; slot < wearing.length; ++slot) {
			BendingItem item = get(wearing[slot]);
			if (item != null && item.getUsage() == Usage.WEARING) {
				if (item.applyMods(ability)) {
				    use(player, 36 + slot, item);
				}
			}
		}
		
		ItemStack[] storage = player.getInventory().getContents();
		for (int slot = 0; slot < storage.length; ++slot) {
			BendingItem item = get(storage[slot]);
			if (item != null && item.getUsage() == Usage.POSSESS) {
				if (item.applyMods(ability)) {
				    use(player, slot, item);
				}
			}
		}
	}
    
    public static void use(Player player, int slot, BendingItem bItem) {
        ItemStack item = player.getInventory().getItem(slot);
        if (!item.hasItemMeta() || !item.getItemMeta().getPersistentDataContainer().has(USES_KEY, PersistentDataType.INTEGER)) {
            return;
        }
        
        ItemMeta meta = item.getItemMeta();
        int uses = meta.getPersistentDataContainer().get(USES_KEY, PersistentDataType.INTEGER);
        if (uses == 1) {
            player.getInventory().setItem(slot, null);
            removeID(item);
            EQUIPPED.computeIfPresent(player, (k, v) -> {
                if (v == bItem) return null;
                return v;
            });
        } else {
            meta.getPersistentDataContainer().set(USES_KEY, PersistentDataType.INTEGER, uses - 1);
            item.setItemMeta(meta);
        }
        
        player.updateInventory();
    }
	
	public static List<BendingItem> listActive(Player player) {
		List<BendingItem> actives = new ArrayList<>();
		
		for (ItemStack is : player.getInventory().getArmorContents()) {
			BendingItem item = get(is);
			if (item != null && item.getUsage() == Usage.WEARING) {
				actives.add(item);
			}
		}
		
		for (ItemStack is : player.getInventory().getStorageContents()) {
			BendingItem item = get(is);
			if (item != null && item.getUsage() == Usage.POSSESS) {
				actives.add(item);
			}
		}
		
		BendingItem main = get(player.getInventory().getItemInMainHand()), off = get(player.getInventory().getItemInOffHand());
		
		if (main != null && main.getUsage() == Usage.HOLDING) {
			actives.add(main);
		}
		
		if (off != null && off.getUsage() == Usage.HOLDING) {
			actives.add(off);
		}
		
		return actives;
	}
	
	public static BendingItem get(String name) {
		return NAME_CACHE.get(name.toLowerCase());
	}
	
	public static BendingItem get(int id) {
		return ID_CACHE.get(id);
	}
	
	public static BendingItem get(ItemStack item) {
		int id = getID(item);
		BendingItem bItem = ID_CACHE.get(id);
		
		if (bItem == null && id > -1) {
			removeID(item);
			return null;
		}
		
		return bItem;
	}
	
	public static List<BendingItem> listItems() {
		return new ArrayList<>(ID_CACHE.values());
	}
	
    public static void equip(Player player, BendingItem item) {
        EQUIPPED.put(player, item);
    }
    
    public static void unequip(Player player) {
        EQUIPPED.remove(player);
    }
    
    public static boolean equipped(Player player) {
        return EQUIPPED.containsKey(player);
    }
    
    public static boolean isEquipped(Player player, ItemStack item) {
    	BendingItem equipped = EQUIPPED.get(player);
    	if (equipped == null) {
    		return false;
    	}
    	
    	return equipped == get(item);
    }
	
	public static BendingItem register(File file) throws IllegalArgumentException {
		if (!file.getName().endsWith(".yml")) {
			throw new IllegalArgumentException("Failed registration of item from '" + file.getName() + "', must be yml!");
		}
		
		String name = file.getName().substring(0, file.getName().length() - 4).toLowerCase();
		if (NAME_CACHE.containsKey(name)) {
			throw new IllegalArgumentException("Failed registration of item '" + name + "', name already in use!");
		}
		
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		
		if (!config.contains(MATERIAL_PATH)) {
			throw new IllegalArgumentException("Failed registration of item '" + name + "', no material specified!");
		} else if (!config.contains(USAGE_PATH)) {
			throw new IllegalArgumentException("Failed registration of item '" + name + "', no usage specified!");
		} else if (!config.contains(ELEMENT_PATH)) {
			throw new IllegalArgumentException("Failed registration of item '" + name + "', no element specified!");
		}
		
		Material mat;
		
		try {
			mat = Material.valueOf(config.getString(MATERIAL_PATH).toUpperCase());
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed registration of item '" + name + "', invalid material!");
		}
		
		Usage usage;
		
		try {
			usage = Usage.valueOf(config.getString(USAGE_PATH).toUpperCase());
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed registration of item '" + name + "', invalid usage!");
		}
		
		Element element = Element.getElement(config.getString(ELEMENT_PATH));
		if (element == null) {
			throw new IllegalArgumentException("Failed registration of item '" + name + "', invalid element!");
		}
		
		ItemStack item = new ItemStack(mat);
		ItemMeta meta = item.getItemMeta();
		List<String> lore = new ArrayList<>();
		lore.add(ChatColor.DARK_GRAY + (ChatColor.ITALIC + "Usage: ") + usage.toString());
		meta.setLore(lore);
		
		ItemData.parseAll(name, item, meta, config);
		
		int id = name.hashCode();
		meta.getPersistentDataContainer().set(ID_KEY, PersistentDataType.INTEGER, id);
		item.setItemMeta(meta);
		
		Map<String, List<BendingModifier>> mods = null;
		if (config.contains(MODS_PATH)) {
			mods = new HashMap<>();
			
			for (String key : config.getConfigurationSection(MODS_PATH).getKeys(false)) {
			    String ability = key.equalsIgnoreCase("Base") ? null : key.toLowerCase(); 
			    
				mods.put(ability, loadMods(config.getConfigurationSection(MODS_PATH + "." + key)));
			}
		}
		
		BendingItem bItem = new BendingItem(name, item, usage, element, mods, config);
		NAME_CACHE.put(name, bItem);
		ID_CACHE.put(id, bItem);
		return bItem;
	}
	
	private static List<BendingModifier> loadMods(ConfigurationSection section) {
		List<BendingModifier> mods = new ArrayList<>();
		
		for (String key : section.getKeys(false)) {
		    BendingModifier mod = null;
		    
		    try {
		        mod = BendingModifier.of(key, section.getString(key));
		    } catch (Exception e) {
		        Bukkit.getLogger().warning(section.getName() + " mod " + key + " has " + e.getMessage() + " operations.");
		    }
		    
		    if (mod == null) {
		        continue;
		    }
		    
			mods.add(mod);
		}
		
		return mods;
	}
	
	private static int getID(ItemStack item) {
		int id = -1;
		
		if (item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(ID_KEY, PersistentDataType.INTEGER)) {
			id = item.getItemMeta().getPersistentDataContainer().get(ID_KEY, PersistentDataType.INTEGER);
		}
		
		return id;
	}
	
	private static void removeID(ItemStack item) {
		if (item == null || !item.hasItemMeta()) {
			return;
		}
		
		ItemMeta meta = item.getItemMeta();
		
		if (meta.getPersistentDataContainer().has(ID_KEY, PersistentDataType.INTEGER)) {
			meta.getPersistentDataContainer().remove(ID_KEY);
		}
		
		item.setItemMeta(meta);
	}
	
	private static void configureDefaults(File folder) {
		File example1 = new File(folder, "Raavaxe.yml");
		File example2 = new File(folder, "Vaatuhelm.yml");
		File example3 = new File(folder, "Meteorite.yml");
		File example4 = new File(folder, "Timesword.yml");
		
		try {
			example1.createNewFile();
			example2.createNewFile();
			example3.createNewFile();
			example4.createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		FileConfiguration config1 = YamlConfiguration.loadConfiguration(example1);
		config1.options().copyDefaults(true);
		config1.addDefault(ItemData.DISPLAY_NAME.path(), "&bDivine Raavaxe");
		config1.addDefault(MATERIAL_PATH, Material.DIAMOND_AXE.toString());
		config1.addDefault(ELEMENT_PATH, Element.AVATAR.getName());
		config1.addDefault(USAGE_PATH, Usage.HOLDING.toString());
		config1.addDefault(ItemData.DURABILITY.path(), 2000);
		config1.addDefault(ItemData.LORE.path(), Arrays.asList("&dAn axe graced by Raava and", "&dimbued with spiritual energy that", "&dincreases bending damage!"));
		config1.addDefault(ItemData.FLAGS.path(), Arrays.asList(ItemFlag.HIDE_ATTRIBUTES.toString()));
		config1.addDefault(MODS_PATH + ".Base.Damage", "x2");
		
		FileConfiguration config2 = YamlConfiguration.loadConfiguration(example2);
		config2.options().copyDefaults(true);
		config2.addDefault(ItemData.DISPLAY_NAME.path(), "&cHelm of Vaatu");
		config2.addDefault(MATERIAL_PATH, Material.NETHERITE_HELMET.toString());
		config2.addDefault(ELEMENT_PATH, Element.AVATAR.getName());
		config2.addDefault(USAGE_PATH, Usage.WEARING.toString());
		config2.addDefault(ItemData.DURABILITY.path(), 2000);
		config2.addDefault(ItemData.LORE.path(), Arrays.asList("&8A helmet of darkness", "&8imbued with Vaatu's energy that", "&8increases bending range!"));
		config2.addDefault(MODS_PATH + ".Base.Range", "x2");
		
		FileConfiguration config3 = YamlConfiguration.loadConfiguration(example3);
		config3.options().copyDefaults(true);
		config3.addDefault(ItemData.DISPLAY_NAME.path(), "&aMeteorite Fragment");
		config3.addDefault(MATERIAL_PATH, Material.IRON_NUGGET.toString());
		config3.addDefault(ELEMENT_PATH, Element.EARTH.getName());
		config3.addDefault(USAGE_PATH, Usage.POSSESS.toString());
		config3.addDefault(ItemData.LORE.path(), Arrays.asList("&2A mysterious object, it", "&2radiates energy that empowers", "&2earthbending abilities."));
		config3.addDefault(MODS_PATH + ".Base.Damage", "x1.5");
		config3.addDefault(MODS_PATH + ".Base.Range", "+10");
		config3.addDefault(MODS_PATH + ".Base.Speed", "x1.2");
		config3.addDefault(MODS_PATH + ".Base.Duration", "x6,/5");
		config3.addDefault(MODS_PATH + ".Base.Knockback", "x1.3");
		config3.addDefault(MODS_PATH + ".Base.Knockup", "x1.5");
		config3.addDefault(MODS_PATH + ".Base.Cooldown", "x7,/10");
		config3.addDefault(MODS_PATH + ".Base.Height", "+1");
		config3.addDefault(MODS_PATH + ".EarthArmor.GoldHearts", "+2");
		config3.addDefault(MODS_PATH + ".EarthSmash.FlightDuration", "+3000");
		
		FileConfiguration config4 = YamlConfiguration.loadConfiguration(example4);
		config4.options().copyDefaults(true);
		config4.addDefault(ItemData.DISPLAY_NAME.path(), "&6Sword of Time");
		config4.addDefault(MATERIAL_PATH, Material.WOODEN_SWORD.toString());
		config4.addDefault(ELEMENT_PATH, Element.AVATAR.getName());
		config4.addDefault(USAGE_PATH, Usage.HOLDING.toString());
		config4.addDefault(ItemData.UNBREAKABLE.path(), true);
		config4.addDefault(ItemData.LORE.path(), Arrays.asList("&7A sword crafted out of a", "&7branch from the Tree of Time,", "&7it absorbed enough spiritual", "&7energy to be indestructible"));
		config4.addDefault(ItemData.FLAGS.path(), Arrays.asList(ItemFlag.HIDE_ATTRIBUTES.toString(), ItemFlag.HIDE_ENCHANTS.toString()));
		config4.addDefault(ItemData.ENCHANTS.path(), Arrays.asList(Enchantment.SWEEPING_EDGE.getKey().getKey() + ":4"));
		config4.addDefault(MODS_PATH + ".Damage", "x2");
		config4.addDefault(MODS_PATH + ".Speed", "x1.4");
		config4.addDefault(MODS_PATH + ".Duration", "x3,/2");
		config4.addDefault(MODS_PATH + ".Cooldown", "/2");
		config4.addDefault(MODS_PATH + ".ChargeTime", "/2");
		
		try {
			config1.save(example1);
			config2.save(example2);
			config3.save(example3);
			config4.save(example4);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static void init(ItemsPlugin plugin) {
		File folder = new File(plugin.getDataFolder(), "/items/");
		
		if (!folder.exists()) {
			folder.mkdirs();
			
			configureDefaults(folder);
		}
		
		for (File file : folder.listFiles()) {
			register(file);
		}
		
		for (BendingItem item : ID_CACHE.values()) {
			RecipeParser.apply(item);
		}
	}
	
	static void disable() {
		NAME_CACHE.clear();
		ID_CACHE.clear();
		EQUIPPED.clear();
	}
}
