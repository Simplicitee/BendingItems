package me.simplicitee.project.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.attribute.AttributePriority;

public class BendingItem implements Listener {

	public enum Usage {
		WEARING, HOLDING, POSSESS;
	}
	
	private ItemStack item;
	private Usage usage;
	private Element element;
	private Map<CoreAbility, List<BendingModifier>> mods;
	
	public BendingItem(ItemStack item, Usage usage, Element element, Map<CoreAbility, List<BendingModifier>> mods) {
		this.item = item;
		this.usage = usage;
		this.element = element;
		this.mods = mods;
	}
	
	public ItemStack getStack() {
		return item.clone();
	}
	
	public boolean isSimilar(ItemStack item) {
		return item.isSimilar(this.item);
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
	
	public void applyMods(CoreAbility abil) {
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
				abil.addAttributeModifier(mod.attribute(), mod.value(), mod.method(), AttributePriority.LOW);
			} catch (Exception e) {
				continue;
			}
		}
	}
	
	public void give(Player player) {
		player.getInventory().addItem(item);
		player.updateInventory();
	}
}
