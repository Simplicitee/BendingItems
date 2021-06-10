package me.simplicitee.project.items;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.CoreAbility;

import me.simplicitee.project.items.BendingItem.Usage;
import net.md_5.bungee.api.ChatColor;

public final class ItemManager {

	private ItemManager() {}
	
	private static final NamespacedKey namespace = new NamespacedKey(JavaPlugin.getPlugin(ItemsPlugin.class), "itemid");
	private static final Map<String, BendingItem> NAME_CACHE = new HashMap<>();
	private static final Map<Integer, BendingItem> ID_CACHE = new HashMap<>();
	
	private static final String DISPLAY_PATH = "Display";
	private static final String LORE_PATH = "Lore";
	private static final String MATERIAL_PATH = "Material";
	private static final String DURABILITY_PATH = "Durability";
	private static final String USAGE_PATH = "Usage";
	private static final String ELEMENT_PATH = "Element";
	
	public static List<BendingItem> listActive(Player player) {
		List<BendingItem> actives = new ArrayList<>();
		
		for (ItemStack is : player.getInventory().getArmorContents()) {
			BendingItem item = get(is);
			if (item != null && (item.getUsage() == Usage.WEARING || item.getUsage() == Usage.POSSESS)) {
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
		BendingItem bItem = ID_CACHE.get(getID(item));
		
		if (bItem != null && !bItem.isSimilar(item)) {
			removeID(item);
			return null;
		}
		
		return bItem;
	}
	
	public static List<BendingItem> listItems() {
		return new ArrayList<>(ID_CACHE.values());
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
		lore.add(ChatColor.DARK_GRAY + "Usage: " + usage.toString());
		
		if (config.contains(DISPLAY_PATH)) {
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString(DISPLAY_PATH)));
		} else {
			meta.setDisplayName(name);
		}
		
		if (config.contains(LORE_PATH)) {
			lore.addAll(config.getStringList(LORE_PATH).stream().map((s) -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList()));
		}
		
		/*
		if (config.contains(DURABILITY_PATH) && meta instanceof Damageable) {
			((Damageable) meta).setDamage(config.getInt(DURABILITY_PATH));
		}
		*/
		int id = name.hashCode();
		meta.setLore(lore);
		meta.getPersistentDataContainer().set(namespace, PersistentDataType.INTEGER, id);
		item.setItemMeta(meta);
		
		Map<CoreAbility, List<BendingModifier>> mods = null;
		if (config.contains("Mods")) {
			mods = new HashMap<>();
			
			if (config.contains("Mods.Base")) {
				mods.put(null, loadMods(config.getConfigurationSection("Mods.Base")));
			}
			
			for (String key : config.getConfigurationSection("Mods").getKeys(false)) {
				CoreAbility ability = CoreAbility.getAbility(key);
				
				if (ability == null) {
					continue;
				}
				
				mods.put(ability, loadMods(config.getConfigurationSection("Mods." + key)));
			}
		}
		
		BendingItem bItem = new BendingItem(name, item, usage, element, mods);
		NAME_CACHE.put(name, bItem);
		ID_CACHE.put(id, bItem);
		return bItem;
	}
	
	private static List<BendingModifier> loadMods(ConfigurationSection section) {
		List<BendingModifier> mods = new ArrayList<>();
		
		for (String key : section.getKeys(false)) {
			mods.add(new BendingModifier(key, section.getString(key)));
		}
		
		return mods;
	}
	
	private static int getID(ItemStack item) {
		int id = -1;
		
		if (item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(namespace, PersistentDataType.INTEGER)) {
			id = item.getItemMeta().getPersistentDataContainer().get(namespace, PersistentDataType.INTEGER);
		}
		
		return id;
	}
	
	private static void removeID(ItemStack item) {
		if (item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(namespace, PersistentDataType.INTEGER)) {
			item.getItemMeta().getPersistentDataContainer().remove(namespace);
		}
	}
	
	private static void configureDefaults(File folder) {
		File example1 = new File(folder, "Raavaxe.yml");
		File example2 = new File(folder, "Vaatuhelm.yml");
		File example3 = new File(folder, "Meteorite.yml");
		
		try {
			example1.createNewFile();
			example2.createNewFile();
			example3.createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		FileConfiguration config1 = YamlConfiguration.loadConfiguration(example1);
		config1.options().copyDefaults(true);
		config1.addDefault(DISPLAY_PATH, "&bDivine Raavaxe");
		config1.addDefault(MATERIAL_PATH, Material.DIAMOND_AXE.toString());
		config1.addDefault(ELEMENT_PATH, Element.AVATAR.getName());
		config1.addDefault(USAGE_PATH, Usage.HOLDING.toString());
		config1.addDefault(DURABILITY_PATH, 2000);
		config1.addDefault(LORE_PATH, Arrays.asList("&dAn axe graced by Raava and", "&dimbued with spiritual energy that", "&dincreases bending damage!"));
		config1.addDefault("Mods.Base.Damage", "x2");
		
		FileConfiguration config2 = YamlConfiguration.loadConfiguration(example2);
		config2.options().copyDefaults(true);
		config2.addDefault(DISPLAY_PATH, "&cHelm of Vaatu");
		config2.addDefault(MATERIAL_PATH, Material.NETHERITE_HELMET.toString());
		config2.addDefault(ELEMENT_PATH, Element.AVATAR.getName());
		config2.addDefault(USAGE_PATH, Usage.WEARING.toString());
		config2.addDefault(DURABILITY_PATH, 2000);
		config2.addDefault(LORE_PATH, Arrays.asList("&8A helmet of darkness", "&8imbued with Vaatu's energy that", "&8increases bending range!"));
		config2.addDefault("Mods.Base.Range", "x2");
		
		FileConfiguration config3 = YamlConfiguration.loadConfiguration(example3);
		config3.options().copyDefaults(true);
		config3.addDefault(DISPLAY_PATH, "&aMeteorite Fragment");
		config3.addDefault(MATERIAL_PATH, Material.IRON_NUGGET.toString());
		config3.addDefault(ELEMENT_PATH, Element.EARTH.getName());
		config3.addDefault(USAGE_PATH, Usage.POSSESS.toString());
		config3.addDefault(LORE_PATH, Arrays.asList("&2A mysterious object, it", "&2radiates energy that empowers", "&2earthbending abilities."));
		config3.addDefault("Mods.Base.Damage", "x1.5");
		config3.addDefault("Mods.Base.Range", "+10");
		config3.addDefault("Mods.Base.Speed", "x1.2");
		config3.addDefault("Mods.Base.Duration", "x1.2");
		config3.addDefault("Mods.Base.Knockback", "x1.3");
		config3.addDefault("Mods.Base.Knockup", "x1.5");
		config3.addDefault("Mods.Base.Cooldown", "x0.7");
		config3.addDefault("Mods.Base.Height", "+1");
		config3.addDefault("Mods.EarthArmor.GoldHearts", "+2");
		config3.addDefault("Mods.EarthSmash.FlightDuration", "+3000");
		
		try {
			config1.save(example1);
			config2.save(example2);
			config3.save(example3);
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
	}
}
