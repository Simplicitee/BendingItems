package me.simplicitee.project.items.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import me.simplicitee.project.items.BendingItem;
import me.simplicitee.project.items.ItemsPlugin;
import net.md_5.bungee.api.ChatColor;

public class ItemGui implements Listener {
	
	public static interface ClickAction {
		public void accept(Player player, ItemGui gui);
	}

	private static final DisplayItem PREV = DisplayItem.create(Material.RED_STAINED_GLASS_PANE, "&cPrevious page", Arrays.asList("Go to the previous page"), (p, g) -> {
		int prev = g.ordered.indexOf(g.pages.get(p.getOpenInventory().getTopInventory())) - 1;
		if (prev < 0) {
			return;
		}
		p.closeInventory();
		p.openInventory(g.ordered.get(prev).inv);
	});
	
	private static final DisplayItem NEXT = DisplayItem.create(Material.GREEN_STAINED_GLASS_PANE, "&aNext page", Arrays.asList("Go to the next page"), (p, g) -> {
		int next = g.ordered.indexOf(g.pages.get(p.getOpenInventory().getTopInventory())) + 1;
		if (next >= g.ordered.size()) {
			return;
		}
		p.closeInventory();
		p.openInventory(g.ordered.get(next).inv);
	});
	
	private static final DisplayItem NULL = DisplayItem.create(Material.BLACK_STAINED_GLASS_PANE, "&7Separator", Arrays.asList("Does nothing!"), (p, g) -> {});
	
	public static class Page {
		private DisplayItem[] items;
		private Inventory inv;
		
		public Page(int page) {
			items = new DisplayItem[54];
			inv = Bukkit.createInventory(null, 54, ChatColor.BLUE + "Bending Items [Page " + page + "]");
		}
		
		private void setSlot(int slot, DisplayItem item) {
			items[slot] = item;
			inv.setItem(slot, item.item);
		}
	}
	
	private List<Page> ordered;
	private Map<Inventory, Page> pages;
	
	public ItemGui(List<BendingItem> items) {
		ordered = new ArrayList<>();
		pages = new HashMap<>();
		
		int j = 0;
		while (j < items.size()) {
			Page page = new Page(ordered.size() + 1);
			
			for (int i = 0; i < 45 && j < items.size(); ++i, ++j) {
				page.setSlot(i, items.get(j).toDisplay());
			}
			
			for (int i = 45; i < 54; ++i) {
				if (i < 49) {
					page.setSlot(i, PREV);
				} else if (i == 49) {
					page.setSlot(i, NULL);
				} else {
					page.setSlot(i, NEXT);
				}
			}
			
			ordered.add(page);
			pages.put(page.inv, page);
		}
		
		Bukkit.getServer().getPluginManager().registerEvents(this, JavaPlugin.getPlugin(ItemsPlugin.class));
	}
	
	public void open(Player player) {
		player.openInventory(ordered.get(0).inv);
	}
	
	@EventHandler
	public void onInvClick(InventoryClickEvent event) {
		if (!pages.containsKey(event.getInventory()) || event.getCurrentItem() == null || !(event.getWhoClicked() instanceof Player)) {
			return;
		}
		
		event.setCancelled(true);
		pages.get(event.getInventory()).items[event.getRawSlot()].click.accept((Player) event.getWhoClicked(), this);
	}
}
