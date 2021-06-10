package me.simplicitee.project.items;

import com.projectkorra.projectkorra.attribute.AttributeModifier;

public final class BendingModifier {

	private String attribute, value;
	private AttributeModifier mod;
	private Number num;
	
	public BendingModifier(String attribute, String value) {
		this.attribute = attribute;
		this.value = value;
		this.mod = value.charAt(0) == 'x' ? AttributeModifier.MULTIPLICATION : AttributeModifier.ADDITION;
		this.num = Double.parseDouble(value.substring(1, value.length()));
	}
	
	public String attribute() {
		return attribute;
	}
	
	public AttributeModifier method() {
		return mod;
	}
	
	public Number num() {
		return num;
	}
	
	public String value() {
		return value;
	}
}
