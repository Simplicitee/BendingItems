package me.simplicitee.project.items;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.attribute.AttributeModifier;
import com.projectkorra.projectkorra.attribute.AttributePriority;

public final class BendingModifier {

	private String attribute;
	private AttributeModifier mod;
	private Number scalar;
	
	public BendingModifier(String attribute, AttributeModifier mod, Number scalar) {
		this.attribute = attribute;
		this.mod = mod;
		this.scalar = scalar;
	}
	
	public String attribute() {
		return attribute;
	}
	
	public void apply(CoreAbility ability) {
	    try {
	        ability.addAttributeModifier(attribute, scalar, mod, AttributePriority.LOW);
        } catch (Exception e) {}
	}
	
	@Override
	public String toString() {
	    return attribute + ": " + (mod == AttributeModifier.MULTIPLICATION ? "x" : "+") + scalar.toString();
	}
	
	public static BendingModifier of(String attribute, String value) {
	    if (!value.startsWith("x") || !value.startsWith("+")) {
	        throw new IllegalArgumentException("Mods must start with 'x' or '+'");
	    }
	    
	    AttributeModifier mod = value.startsWith("x") ? AttributeModifier.MULTIPLICATION : AttributeModifier.ADDITION;
	    Number scalar = Double.parseDouble(value.substring(1));
	    
	    return new BendingModifier(attribute, mod, scalar);
	}
}
