package me.simplicitee.project.items;

import com.projectkorra.projectkorra.attribute.AttributeModifier;

public final class BendingModifier {

	private String attribute;
	private AttributeModifier mod;
	private Number value;
	
	public BendingModifier(String attribute, AttributeModifier mod, Number value) {
		this.attribute = attribute;
		this.mod = mod;
		this.value = value;
	}
	
	public String attribute() {
		return attribute;
	}
	
	public AttributeModifier method() {
		return mod;
	}
	
	public Number value() {
		return value;
	}
}
