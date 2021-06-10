package me.simplicitee.project.items.gui;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.simplicitee.project.items.gui.ItemGui.ClickAction;
import net.md_5.bungee.api.ChatColor;

public class DisplayItem {

	ItemStack item;
	ClickAction click;
	
	public DisplayItem(ItemStack item, ClickAction click) {
		this.item = item;
		this.click = click;
	}
	
	public static DisplayItem create(Material type, String name, List<String> lore, ClickAction click) {
		if (type == null || click == null) {
			return null;
		}
		
		ItemStack item = new ItemStack(type);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		meta.setLore(lore.stream().map((s) -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList()));
		item.setItemMeta(meta);
		return new DisplayItem(item, click);
	}
}
