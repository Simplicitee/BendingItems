package me.simplicitee.project.items;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.event.AbilityStartEvent;
import com.projectkorra.projectkorra.event.BendingReloadEvent;

import me.simplicitee.project.items.command.AttributesCommand;
import me.simplicitee.project.items.command.ItemCommand;

public class ItemsPlugin extends JavaPlugin implements Listener {

	@Override
	public void onEnable() {
		// register items from files
		ItemManager.init(this);
		
		// register listener
		this.getServer().getPluginManager().registerEvents(this, this);
		
		// register commands
		new ItemCommand();
		new AttributesCommand();
	}
	
	@Override
	public void onDisable() {
		ItemManager.disable();
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onAbilityStart(AbilityStartEvent event) {
		ItemManager.modify((CoreAbility) event.getAbility());
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSlotChange(PlayerItemHeldEvent event) {
		Player player = event.getPlayer();
		ItemStack equip = player.getInventory().getItem(event.getPreviousSlot());
		
		if (ItemManager.isEquipped(player, equip)) {
			player.getInventory().setItem(event.getPreviousSlot(), event.getPlayer().getInventory().getItem(event.getNewSlot()));
			player.getInventory().setItem(event.getNewSlot(), equip);
			player.updateInventory();
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onOffhand(PlayerSwapHandItemsEvent event) {
	    if (ItemManager.equipped(event.getPlayer())) {
	        event.setCancelled(true);
	    }
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onCraft(CraftItemEvent event) {
		BendingItem item = ItemManager.get(event.getInventory().getResult());
		
		if (item != null) {
			event.getInventory().setResult(item.newStack());
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPKReload(BendingReloadEvent event) {
		new ItemCommand();
		new AttributesCommand();
	}
}
