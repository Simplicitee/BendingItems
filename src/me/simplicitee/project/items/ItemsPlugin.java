package me.simplicitee.project.items;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.event.AbilityStartEvent;

public class ItemsPlugin extends JavaPlugin implements Listener {

	@Override
	public void onEnable() {
		// register items from files
		ItemManager.init(this);
		
		// register listener
		this.getServer().getPluginManager().registerEvents(this, this);
		
		// register command
		new ItemCommand();
	}
	
	@Override
	public void onDisable() {}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onAbilityStart(AbilityStartEvent event) {
		for (BendingItem item : ItemManager.listActive(event.getAbility().getPlayer())) {
			item.applyMods((CoreAbility) event.getAbility());
		}
	}
}
