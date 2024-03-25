package me.simplicitee.project.items.item.parser;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import me.simplicitee.project.items.ItemsPlugin;
import me.simplicitee.project.items.item.ItemDataParser;

public class AttributesParser implements ItemDataParser {
    
    private static final String PATH = "Attributes";

    @Override
    public String getPath() {
        return PATH;
    }

    @Override
    public void apply(String name, ItemStack item, ItemMeta meta, FileConfiguration config) {
        if (!config.contains(PATH)) return;
        
        List<String> values = config.getStringList(PATH);
        for (String value : values) {
            String[] split = value.split(";");
            if (split.length != 4) {
                JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning("Unable to parse '" + value + "' in attributes section, expected '<ATTRIBUTE>;<OPERATION>;<AMOUNT>;<EQUIPMENT SLOT>'");
                continue;
            }
            
            Attribute attr;
            Operation op;
            EquipmentSlot slot;
            double amount;
            
            try {
                attr = Attribute.valueOf(split[0].toUpperCase());
            } catch (Exception e) {
                JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning("Unable to parse attribute from '" + split[0] + "' on item '" + name + "'" + ", skipping that line."
                        + " Acceptable values are: " + Arrays.stream(Attribute.values()).map(Object::toString).reduce((a, b) -> a == null ? b : a + ", " + b));
                continue;
            }
            
            try {
                op = Operation.valueOf(split[1].toUpperCase());
            } catch (Exception e) {
                JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning("Unable to parse operation from '" + split[1] + "' on item '" + name + "'" + ", skipping that line."
                        + " Acceptable values are: " + Arrays.stream(Operation.values()).map(Object::toString).reduce((a, b) -> a == null ? b : a + ", " + b));
                continue;
            }
            
            try {
                slot = EquipmentSlot.valueOf(split[3].toUpperCase());
            } catch (Exception e) {
                JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning("Unable to parse equipment slot from '" + split[3] + "' on item '" + name + "'" + ", skipping that line."
                        + " Acceptable values are: " + Arrays.stream(EquipmentSlot.values()).map(Object::toString).reduce((a, b) -> a == null ? b : a + ", " + b));
                continue;
            }
            
            try {
                amount = Double.parseDouble(split[2]);
            } catch (Exception e) {
                JavaPlugin.getPlugin(ItemsPlugin.class).getLogger().warning("Unable to parse amount from '" + split[2] + "' on item '" + name + "'" + ", skipping that line.");
                continue;
            }
            
            meta.addAttributeModifier(attr, new AttributeModifier(UUID.randomUUID(), name.toUpperCase() + "_" + attr.toString(), amount, op, slot));
        }
    }

}
