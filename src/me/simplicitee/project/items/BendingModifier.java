package me.simplicitee.project.items;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.attribute.AttributeModifier;
import com.projectkorra.projectkorra.attribute.AttributePriority;

public final class BendingModifier {

	private String attribute;
	private List<Operation> ops;
	
	public BendingModifier(String attribute, List<Operation> ops) {
		this.attribute = attribute;
		this.ops = ops;
	}
	
	public String attribute() {
		return attribute;
	}
	
	public void apply(CoreAbility ability) {
	    try {
            for (Operation op : ops) {
                ability.addAttributeModifier(attribute, op.scalar, op.type, AttributePriority.LOW);
            }
        } catch (Exception e) {}
	}
	
	@Override
	public String toString() {
	    return attribute + ": " + String.join(",", ops.stream().map(Operation::toString).collect(Collectors.toList()));
	}
	
	public static BendingModifier of(String attribute, String value) {
    	List<Operation> ops = new ArrayList<>();
    	String[] split = value.split(",");
    	
    	for (String op : split) {
    	    AttributeModifier type = null;
    	    
    	    if (op.startsWith("+")) {
    	        type = AttributeModifier.ADDITION;
    	    } else if (op.startsWith("-")) {
    	        type = AttributeModifier.SUBTRACTION;
    	    } else if (op.startsWith("x") || op.startsWith("*")) {
    	        type = AttributeModifier.MULTIPLICATION;
    	    } else if (op.startsWith("/")) {
    	        type = AttributeModifier.DIVISION;
    	    } else {
    	        throw new IllegalArgumentException("mod type");
    	    }
    	    
    	    Number scalar = null;
    	    String sub = op.substring(1);
    	    
    	    if (sub.contains(".")) {
    	        scalar = Double.parseDouble(sub);
    	    } else {
    	        scalar = Long.parseLong(sub);
    	    }
    	    
    	    ops.add(new Operation(type, scalar));
    	}
    	
    	if (ops.isEmpty()) {
    	    throw new IllegalArgumentException("empty");
    	}
	    
	    return new BendingModifier(attribute, ops);
	}
	
	private static class Operation {
	    
	    private AttributeModifier type;
	    private Number scalar;
	    
	    private Operation(AttributeModifier type, Number scalar) {
	        this.type = type;
	        this.scalar = scalar;
	    }
	    
	    @Override
	    public String toString() {
	        switch (type) {
	        case ADDITION: return "+" + scalar.toString();
	        case SUBTRACTION: return "-" + scalar.toString();
	        case MULTIPLICATION: return "x" + scalar.toString();
	        case DIVISION: return "/" + scalar.toString();
	        }
	        
	        return "NULL";
	    }
	}
}
