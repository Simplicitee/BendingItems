package me.simplicitee.project.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.CoreAbility;

import me.simplicitee.project.items.gui.DisplayItem;

public class BendingItem {

	public enum Usage {
		WEARING, HOLDING, POSSESS;
	}
	
	private String name;
	private ItemStack item;
	private Usage usage;
	private Element element;
	private Map<String, List<BendingModifier>> mods;
	private final FileConfiguration config;
	
	public BendingItem(String name, ItemStack item, Usage usage, Element element, Map<String, List<BendingModifier>> mods, FileConfiguration config) {
		this.name = name;
		this.item = item;
		this.usage = usage;
		this.element = element;
		this.mods = mods;
		this.config = config;
	}
	
	public String getInternalName() {
		return name;
	}
	
	public Usage getUsage() {
		return usage;
	}
	
	public Element getElement() {
		return element;
	}
	
	public String getDisplayName() {
		return item.getItemMeta().getDisplayName();
	}
	
	public ItemStack internal() {
		return item;
	}
	
	public FileConfiguration config() {
		return config;
	}
	
	public ItemStack newStack() {
		return item.clone();
	}
	
	public List<String> listMods() {
		List<String> mods = new ArrayList<>();
		mods.add("Stats:");
		
		for (Entry<String, List<BendingModifier>> entry : this.mods.entrySet()) {
		    String ability = entry.getKey();
		    
			for (BendingModifier mod : entry.getValue()) {
				mods.add("- " + (ability != null ? ability : "base") + "." + mod.toString());
			}
		}
		
		return mods;
	}
	
	public boolean applyMods(CoreAbility abil) {
		if (mods == null) {
			return false;
		} else if (element != Element.AVATAR && element != abil.getElement()) {
			return false;
		}
		
		List<BendingModifier> specific = new ArrayList<>();
		
		specific.addAll(mods.getOrDefault(abil.getName().toLowerCase(), Collections.emptyList()));
		specific.addAll(mods.getOrDefault(null, Collections.emptyList()));
		
		if (specific.isEmpty()) {
			return false;
		}
		
		for (BendingModifier mod : specific) {
			mod.apply(abil);
		}
		
		return true;
	}
	
	public void give(Player player) {
		player.getInventory().addItem(newStack());
		player.updateInventory();
	}
	
	public DisplayItem toDisplay() {
		return new DisplayItem(item, (p, g) -> give(p));
	}
}
