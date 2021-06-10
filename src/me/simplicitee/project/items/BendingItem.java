package me.simplicitee.project.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.attribute.AttributePriority;

import me.simplicitee.project.items.gui.DisplayItem;
import net.md_5.bungee.api.ChatColor;

public class BendingItem implements Listener {

	public enum Usage {
		WEARING, HOLDING, POSSESS;
	}
	
	private String name;
	private ItemStack item;
	private Usage usage;
	private Element element;
	private Map<CoreAbility, List<BendingModifier>> mods;
	
	public BendingItem(String name, ItemStack item, Usage usage, Element element, Map<CoreAbility, List<BendingModifier>> mods) {
		this.name = name;
		this.item = item;
		this.usage = usage;
		this.element = element;
		this.mods = mods;
	}
	
	public String getInternalName() {
		return name;
	}
	
	public boolean isSimilar(ItemStack item) {
		if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
			return false;
		}
		
		return this.item.getType() == item.getType() && this.item.getItemMeta().getDisplayName().equals(item.getItemMeta().getDisplayName());
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
	
	public ItemStack newStack() {
		ItemStack stack = item.clone();
		ItemMeta meta = stack.getItemMeta();
		List<String> lore = meta.getLore();
		lore.add(1, randomString());
		meta.setLore(lore);
		stack.setItemMeta(meta);
		return stack;
	}
	
	public List<String> listMods() {
		List<String> mods = new ArrayList<>();
		mods.add("Stats:");
		for (CoreAbility ability : this.mods.keySet()) {
			for (BendingModifier mod : this.mods.get(ability)) {
				mods.add("- " + (ability != null ? ability.getName() : "Base") + mod.attribute() + ChatColor.RESET + ": " + mod.value());
			}
		}
		
		return mods;
	}
	
	public void applyMods(CoreAbility abil, ItemStack stack) {
		if (mods == null) {
			return;
		} else if (element != Element.AVATAR && element != abil.getElement()) {
			return;
		}
		
		List<BendingModifier> specific = new ArrayList<>();
		
		specific.addAll(mods.getOrDefault(abil, Collections.emptyList()));
		specific.addAll(mods.getOrDefault(null, Collections.emptyList()));
		
		if (specific.isEmpty()) {
			return;
		}
		
		for (BendingModifier mod : specific) {
			try {
				abil.addAttributeModifier(mod.attribute(), mod.num(), mod.method(), AttributePriority.LOW);
			} catch (Exception e) {
				continue;
			}
		}
		
		ItemManager.use(abil.getPlayer(), stack);
	}
	
	public void give(Player player) {
		player.getInventory().addItem(newStack());
		player.updateInventory();
	}
	
	public DisplayItem toDisplay() {
		return new DisplayItem(item, (p, g) -> give(p));
	}
	
	private static String randomString() {
		String random = String.valueOf(ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE));
		return "§" + String.join("§", random.split(""));
	}
}
