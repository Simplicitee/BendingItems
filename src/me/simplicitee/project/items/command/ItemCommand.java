package me.simplicitee.project.items.command;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.command.PKCommand;

import me.simplicitee.project.items.BendingItem;
import me.simplicitee.project.items.ItemManager;
import me.simplicitee.project.items.BendingItem.Usage;
import me.simplicitee.project.items.gui.ItemGui;
import net.md_5.bungee.api.ChatColor;

public class ItemCommand extends PKCommand {

	private ItemGui gui;
	
	public ItemCommand() {
		super("item", "/bending item <give <item> [player] / list / gui / stats <item> / equip>", "Do many things with bending items!", new String[] {"item"});
		gui = new ItemGui(ItemManager.listItems());
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if (!this.correctLength(sender, args.size(), 1, 3)) {
			return;
		}
		
		if (args.get(0).equalsIgnoreCase("give")) {
			if (!hasPermission(sender, "give")) {
				return;
			} else if (args.size() < 2) {
				sender.sendMessage(ChatColor.RED + "Not enough arguments!");
				return;
			}
			
			BendingItem item = ItemManager.get(args.get(1));
			
			if (item == null) {
				sender.sendMessage(ChatColor.RED + "Unknown item specified!");
				return;
			}
			
			Player target = args.size() == 3 ? Bukkit.getPlayer(args.get(2)) : sender instanceof Player ? (Player) sender : null;
			
			if (target == null) {
				sender.sendMessage(ChatColor.RED + "You must be a player to give yourself an item!");
				return;
			}
			
			item.give(target);
			target.sendMessage("You were given '" + item.getDisplayName() + ChatColor.RESET + "'");
			if (target != sender) {
				sender.sendMessage("Gave " + target.getName() + " '" + item.getDisplayName() + ChatColor.RESET + "'");
			}
		} else if (args.get(0).equalsIgnoreCase("list")) {
			if (args.size() > 1) {
				sender.sendMessage(ChatColor.RED + "Too many arguments!");
				return;
			}
			
			sender.sendMessage(ItemManager.listItems().stream().map((b) -> "- " + ChatColor.BOLD + b.getInternalName() + ChatColor.RESET + " [" + b.getDisplayName() + ChatColor.RESET + "]").collect(Collectors.toList()).toArray(new String[0]));
		} else if (args.get(0).equalsIgnoreCase("gui")) {
			if (!hasPermission(sender, "gui")) {
				return;
			} else if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Player only command!");
				return;
			} else if (args.size() > 1) {
				sender.sendMessage(ChatColor.RED + "Too many arguments!");
				return;
			}
			
			sender.sendMessage(ChatColor.GREEN + "Opening bending item gui!");
			gui.open((Player) sender);
		} else if (args.get(0).equalsIgnoreCase("active")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Player only command!");
				return;
			} else if (args.size() > 1) {
				sender.sendMessage(ChatColor.RED + "Too many arguments!");
				return;
			}
			
			sender.sendMessage(ItemManager.listActive((Player) sender).stream().map((b) -> "- " + ChatColor.BOLD + b.getInternalName() + ChatColor.RESET + " [" + b.getDisplayName() + ChatColor.RESET + "]").collect(Collectors.toList()).toArray(new String[0]));
		} else if (args.get(0).equalsIgnoreCase("stats")) {
			if (args.size() != 2) {
				sender.sendMessage(ChatColor.RED + "Incorrect argument amount!");
				return;
			}
			
			BendingItem item = ItemManager.get(args.get(1));
			if (item == null) {
				sender.sendMessage(ChatColor.RED + "Invalid bending item!");
				return;
			}
			
			List<String> stats = new ArrayList<>();
			stats.add(ChatColor.BOLD + item.getInternalName() + ChatColor.RESET + " [" + item.getDisplayName() + ChatColor.RESET + "]");
			stats.add("Usage: " + item.getUsage().toString());
			stats.addAll(item.listMods());
			sender.sendMessage(stats.toArray(new String[0]));
		} else if (args.get(0).equalsIgnoreCase("equip")) {
			if (!hasPermission(sender, "equip")) {
				return;
			} else if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Player only command!");
				return;
			} else if (args.size() > 1) {
				sender.sendMessage(ChatColor.RED + "Too many arguments!");
				return;
			}
			
			Player player = (Player) sender;
			BendingItem item = ItemManager.get(player.getInventory().getItemInMainHand());
			
			if (item == null) {
				sender.sendMessage(ChatColor.RED + "Invalid bending item!");
				return;
			} else if (item.getUsage() != Usage.HOLDING) {
				sender.sendMessage(ChatColor.RED + "Can only equip items used by HOLDING!");
				return;
			}
			
			if (!ItemManager.equipped(player)) {
				sender.sendMessage(ChatColor.GREEN + "Equipped");
				ItemManager.equip(player, item);
			} else {
				sender.sendMessage(ChatColor.GREEN + "Unequipped");
				ItemManager.unequip(player);
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Unknown argument given!");
			return;
		}
	}

}
