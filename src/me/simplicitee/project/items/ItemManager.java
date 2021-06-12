package me.simplicitee.project.items;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.CoreAbility;

import me.simplicitee.project.items.BendingItem.Usage;
import net.md_5.bungee.api.ChatColor;

public final class ItemManager {

	private ItemManager() {}
	
	private static final char[] CHARS = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i'};
	private static final NamespacedKey ID_KEY = new NamespacedKey(JavaPlugin.getPlugin(ItemsPlugin.class), "itemid");
	private static final NamespacedKey USES_KEY = new NamespacedKey(JavaPlugin.getPlugin(ItemsPlugin.class), "itemuses");
	private static final Map<String, BendingItem> NAME_CACHE = new HashMap<>();
	private static final Map<Integer, BendingItem> ID_CACHE = new HashMap<>();
	private static final Map<Player, BendingItem> EQUIPPED = new HashMap<>();
	
	private static final String DISPLAY_PATH = "Display";
	private static final String LORE_PATH = "Lore";
	private static final String MATERIAL_PATH = "Material";
	private static final String DURABILITY_PATH = "Durability";
	private static final String USAGE_PATH = "Usage";
	private static final String ELEMENT_PATH = "Element";
	private static final String UNBREAKABLE_PATH = "Unbreakable";
	private static final String ENCHANTS_PATH = "Enchants";
	private static final String FLAGS_PATH = "Flags";
	private static final String USES_PATH = "Uses";
	private static final String RECIPE_PATH = "Recipe";
	
	public static void equip(Player player, BendingItem item) {
		EQUIPPED.put(player, item);
	}
	
	public static void unequip(Player player) {
		EQUIPPED.remove(player);
	}
	
	public static boolean equipped(Player player) {
		return EQUIPPED.containsKey(player);
	}
	
	public static boolean matches(Player player, ItemStack item) {
		return EQUIPPED.containsKey(player) && EQUIPPED.get(player).isSimilar(item);
	}
	
	public static void modify(CoreAbility ability) {
		Player player = ability.getPlayer();
		
		BendingItem main = get(player.getInventory().getItemInMainHand()), off = get(player.getInventory().getItemInOffHand());
		
		if (main != null && main.getUsage() == Usage.HOLDING) {
			main.applyMods(ability, player.getInventory().getItemInMainHand());
		}
		
		if (off != null && off.getUsage() == Usage.HOLDING) {
			off.applyMods(ability, player.getInventory().getItemInOffHand());
		}
		
		for (ItemStack is : player.getInventory().getArmorContents()) {
			BendingItem item = get(is);
			if (item != null && (item.getUsage() == Usage.WEARING || item.getUsage() == Usage.POSSESS)) {
				item.applyMods(ability, is);
			}
		}
		
		for (ItemStack is : player.getInventory().getStorageContents()) {
			BendingItem item = get(is);
			if (item != null && item.getUsage() == Usage.POSSESS) {
				item.applyMods(ability, is);
			}
		}
	}
	
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
		int id = getID(item);
		BendingItem bItem = ID_CACHE.get(id);
		
		if ((bItem == null && id > -1) || (bItem != null && !bItem.isSimilar(item))) {
			removeID(item);
			return null;
		}
		
		return bItem;
	}
	
	public static void use(Player player, ItemStack item) {
		if (!item.hasItemMeta() || !item.getItemMeta().getPersistentDataContainer().has(USES_KEY, PersistentDataType.INTEGER)) {
			return;
		}
		
		ItemMeta meta = item.getItemMeta();
		int uses = meta.getPersistentDataContainer().get(USES_KEY, PersistentDataType.INTEGER);
		if (uses == 1) {
			player.getInventory().remove(item);
		} else {
			meta.getPersistentDataContainer().set(USES_KEY, PersistentDataType.INTEGER, uses - 1);
			item.setItemMeta(meta);
		}
		
		player.updateInventory();
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
		lore.add(ChatColor.DARK_GRAY + (ChatColor.ITALIC + "Usage: ") + usage.toString());
		
		if (config.contains(DISPLAY_PATH)) {
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString(DISPLAY_PATH)));
		} else {
			meta.setDisplayName(name);
		}
		
		if (config.contains(LORE_PATH)) {
			lore.addAll(config.getStringList(LORE_PATH).stream().map((s) -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList()));
		}
		
		if (config.contains(UNBREAKABLE_PATH)) {
			meta.setUnbreakable(config.getBoolean(UNBREAKABLE_PATH));
		}
		
		if (config.contains(DURABILITY_PATH) && meta instanceof Damageable) {
			((Damageable) meta).setDamage(-(config.getInt(DURABILITY_PATH) - mat.getMaxDurability()));
		}
		
		if (config.contains(USES_PATH)) {
			meta.getPersistentDataContainer().set(USES_KEY, PersistentDataType.INTEGER, config.getInt(USES_PATH));
		}
		
		if (config.contains(ENCHANTS_PATH)) {
			for (String enchant : config.getStringList(ENCHANTS_PATH)) {
				String[] split = enchant.split(":");
				
				if (split.length != 2) {
					continue;
				}
				
				try {
					meta.addEnchant(Enchantment.getByKey(NamespacedKey.minecraft(split[0])), Integer.parseInt(split[1]), true);
				} catch (NumberFormatException e) {
					JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning("Unable to parse integer from '" + enchant + "' in enchants on item '" + name + "', ignoring.");
				} catch (Exception e) {
					JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning("Unable to parse enchant from '" + enchant + "' in enchants on item '" + name + "', ignoring.");
				}
			}
		}
		
		if (config.contains(FLAGS_PATH)) {
			for (String flag : config.getStringList(FLAGS_PATH)) {
				try {
					meta.addItemFlags(ItemFlag.valueOf(flag.toUpperCase()));
				} catch (Exception e) {}
			}
		}
		
		int id = name.hashCode();
		meta.setLore(lore);
		meta.getPersistentDataContainer().set(ID_KEY, PersistentDataType.INTEGER, id);
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
		
		if (config.contains(RECIPE_PATH)) {
			Recipe recipe = null;
			try {
				recipe = loadRecipe(name, item, config);
			} catch (Exception e) {
				e.printStackTrace();
				recipe = null;
			}
			Bukkit.addRecipe(recipe);
		}
		
		BendingItem bItem = new BendingItem(name, item, usage, element, mods);
		NAME_CACHE.put(name, bItem);
		ID_CACHE.put(id, bItem);
		return bItem;
	}
	
	private static Recipe loadRecipe(String name, ItemStack item, FileConfiguration config) {
		Logger logger = JavaPlugin.getPlugin(ItemsPlugin.class).getLogger();
		if (!config.contains(RECIPE_PATH + ".Ingredients")) {
			logger.warning("Recipe for '" + name + "' requires a list of ingredients under the config path 'Recipe.Ingredients'");
			return null;
		} else if (!config.contains(RECIPE_PATH + ".Shaped")) {
			logger.warning("Recipe for '" + name + "' requires a boolean (true / false) under the config path 'Recipe.Shaped'");
			return null;
		}
		
		boolean shaped = config.getBoolean(RECIPE_PATH + ".Shaped");
		List<String> ingredients = config.getStringList(RECIPE_PATH + ".Ingredients");
		
		if (ingredients == null) {
			logger.warning("Ingredients list for '" + name + "' recipe not found!");
			return null;
		} else if (ingredients.isEmpty()) {
			logger.warning("Ingredients list for '" + name + "' recipe cannot be empty!");
			return null;
		} else if (ingredients.size() > 9) {
			logger.warning("Ingredients list for '" + name + "' recipe cannot be longer than 9 items!");
			return null;
		}
		
		List<Material> mats = new ArrayList<>();
		for (String mat : ingredients) {
			try {
				mats.add(Material.valueOf(mat.toUpperCase()));
			} catch (Exception e) {
				logger.warning("Unable to parse material from '" + mat + "' in '" + name + "' recipe!");
				return null;
			}
		}
		
		if (shaped) {
			if (!config.contains(RECIPE_PATH + ".Shape")) {
				logger.warning("Recipe for '" + name + "' requires a shape under the config path 'Recipe.Shape'");
				return null;
			}
			
			ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(JavaPlugin.getPlugin(ItemsPlugin.class), name), item);
			
			recipe.shape(config.getStringList(RECIPE_PATH + ".Shape").toArray(new String[0]));
			
			for (int i = 0; i < ingredients.size(); ++i) {
				recipe.setIngredient(CHARS[i], mats.get(i));
			}
			
			return recipe;
		} else {
			ShapelessRecipe recipe = new ShapelessRecipe(new NamespacedKey(JavaPlugin.getPlugin(ItemsPlugin.class), name), item);
			
			for (Material ingredient : mats) {
				recipe.addIngredient(ingredient);
			}
			
			return recipe;
		}
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
		config1.addDefault(FLAGS_PATH, Arrays.asList(ItemFlag.HIDE_ATTRIBUTES.toString()));
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
		
		FileConfiguration config4 = YamlConfiguration.loadConfiguration(example4);
		config4.options().copyDefaults(true);
		config4.addDefault(DISPLAY_PATH, "&6Sword of Time");
		config4.addDefault(MATERIAL_PATH, Material.WOODEN_SWORD.toString());
		config4.addDefault(ELEMENT_PATH, Element.AVATAR.getName());
		config4.addDefault(USAGE_PATH, Usage.HOLDING.toString());
		config4.addDefault(UNBREAKABLE_PATH, true);
		config4.addDefault(LORE_PATH, Arrays.asList("&7A sword crafted out of a", "&7branch from the Tree of Time,", "&7it absorbed enough spiritual", "&7energy to be indestructible"));
		config4.addDefault(FLAGS_PATH, Arrays.asList(ItemFlag.HIDE_ATTRIBUTES.toString(), ItemFlag.HIDE_ENCHANTS.toString()));
		config4.addDefault("Mods.Base.Damage", "x2");
		config4.addDefault("Mods.Base.Speed", "x1.4");
		config4.addDefault("Mods.Base.Duration", "x1.5");
		config4.addDefault("Mods.Base.Cooldown", "x0.5");
		config4.addDefault("Mods.Base.ChargeTime", "x0.5");
		config4.addDefault(ENCHANTS_PATH, Arrays.asList(Enchantment.SWEEPING_EDGE.getKey().getKey() + ":4"));
		
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
	}
	
	static void disable() {
		NAME_CACHE.clear();
		ID_CACHE.clear();
		EQUIPPED.clear();
	}
}
