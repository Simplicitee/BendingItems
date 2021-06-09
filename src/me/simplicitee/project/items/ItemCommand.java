package me.simplicitee.project.items;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.command.PKCommand;

import net.md_5.bungee.api.ChatColor;

public class ItemCommand extends PKCommand {

	public ItemCommand() {
		super("item", "/bending item <give <item> [player] / list / gui>", "Do many things with bending items!", new String[] {"item"});
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if (!this.correctLength(sender, args.size(), 1, 3)) {
			return;
		}
		
		if (args.get(0).equalsIgnoreCase("give")) {
			if (args.size() < 2) {
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
				sender.sendMessage(ChatColor.RED + "");
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
			
			sender.sendMessage(ItemManager.listItems().stream().map((b) -> "- " + b.getDisplayName()).collect(Collectors.toList()).toArray(new String[0]));
		} else if (args.get(0).equalsIgnoreCase("gui")) {
			sender.sendMessage("WIP");
		} else {
			sender.sendMessage(ChatColor.RED + "Unknown argument given!");
			return;
		}
	}

}
