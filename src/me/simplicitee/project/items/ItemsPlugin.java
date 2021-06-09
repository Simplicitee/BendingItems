package me.simplicitee.project.items;

import org.bukkit.plugin.java.JavaPlugin;

public class ItemsPlugin extends JavaPlugin {

	@Override
	public void onEnable() {
		// register items from files
		ItemManager.init(this);
		
		// register listener
		this.getServer().getPluginManager().registerEvents(new BendingListener(), this);
		
		// register command
		new ItemCommand();
	}
	
	@Override
	public void onDisable() {}
}
