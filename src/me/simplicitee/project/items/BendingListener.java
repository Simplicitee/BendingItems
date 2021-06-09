package me.simplicitee.project.items;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.event.AbilityStartEvent;

import me.simplicitee.project.items.BendingItem.Usage;

public class BendingListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onAbilityStart(AbilityStartEvent event) {
		BendingItem held = ItemManager.get(event.getAbility().getPlayer().getInventory().getItemInMainHand());
		if (held != null && held.getUsage() == Usage.HOLDING) {
			held.applyMods((CoreAbility) event.getAbility());
		}
		
		BendingItem offHand = ItemManager.get(event.getAbility().getPlayer().getInventory().getItemInOffHand());
		if (offHand != null && offHand.getUsage() == Usage.HOLDING) {
			offHand.applyMods((CoreAbility) event.getAbility());
		}
		
		ItemManager.modify((CoreAbility) event.getAbility());
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onItemClick(InventoryClickEvent event) {
		if (event.getWhoClicked() instanceof Player) {
			return;
		}
		
		if (event.getRawSlot() >= 100 && event.getRawSlot() <= 103) {
			BendingItem curs = null, curr = null;
			
			if (event.getAction() == InventoryAction.PLACE_ALL) {
				curs = ItemManager.get(event.getCursor());
			} else if (event.getAction() == InventoryAction.PICKUP_ALL) {
				curr = ItemManager.get(event.getCurrentItem());
			} else if (event.getAction() == InventoryAction.SWAP_WITH_CURSOR) {
				curs = ItemManager.get(event.getCursor());
				curr = ItemManager.get(event.getCurrentItem());
			}
			
			if (curs != null && curs.getUsage() == Usage.WEARING) {
				ItemManager.active((Player) event.getWhoClicked(), curs);
			}
			
			if (curr != null && curr.getUsage() == Usage.WEARING) {
				ItemManager.remove((Player) event.getWhoClicked(), curr);
			}
		}
		
		BendingItem item = null;
		if (event.getAction() == InventoryAction.DROP_ALL_CURSOR) {
			item = ItemManager.get(event.getCursor());
		} else if (event.getAction() == InventoryAction.DROP_ALL_SLOT) {
			item = ItemManager.get(event.getCurrentItem());
		}
		
		if (item != null) {
			ItemManager.remove((Player) event.getWhoClicked(), item);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInteract(PlayerInteractEvent event) {
		if (event.getItem() == null) {
			return;
		}
		
		BendingItem item = ItemManager.get(event.getItem());
		
		if (item != null && item.getUsage() == Usage.WEARING && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			ItemManager.active(event.getPlayer(), item);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPickup(InventoryPickupItemEvent event) {
		if (event.getInventory().getHolder() == null || !(event.getInventory().getHolder() instanceof Player)) {
			return;
		}
		
		BendingItem item = ItemManager.get(event.getItem().getItemStack());
		
		if (item != null && item.getUsage() == Usage.POSSESS) {
			ItemManager.active((Player) event.getInventory().getHolder(), item);
		}
	}
}
