package me.simplicitee.project.items.command;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.command.PKCommand;

import net.md_5.bungee.api.ChatColor;

public class AttributesCommand extends PKCommand {

	public AttributesCommand() {
		super("attributes", "/bending attributes <ability>", "Lists out an ability's attributes, if any are present", new String[] {"attributes", "attr", "attribute", "attrs"});
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if (!correctLength(sender, args.size(), 1, 1)) {
			return;
		}
		
		CoreAbility ability = CoreAbility.getAbility(args.get(0));
		if (ability == null) {
			sender.sendMessage(ChatColor.RED + "Unknown ability!");
			return;
		}
		
		List<String> attrs = new ArrayList<>();
		for (Field field : ability.getClass().getDeclaredFields()) {
			if (field.isAnnotationPresent(Attribute.class)) {
				attrs.add(ChatColor.GOLD + field.getAnnotation(Attribute.class).value());
			}
		}
		
		if (attrs.isEmpty()) {
			sender.sendMessage(ability.getName() + ChatColor.RESET + " has no attributes!");
		} else {
			sender.sendMessage(String.join(ChatColor.RESET + ", ", attrs));
		}
	}

}
