package tools;

import plugins.util.properties.BooleanPropertyManager;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.arraycontainers.BooleanContainer;

public class Junk {
	
	
	public static void main(String[] args) {
//		BooleanContainer b = new BooleanContainer(false);
//		
//		System.out.println(b);
//		
//		b.set(12, true);
//		b.set(45, true);
//		
//		System.out.println(b);
//		
//		b.set(45, false);
//		
//		System.out.println(b);
		PropertyDefinition propertyDefinition = PropertyDefinition.builder()//
				.setDefaultValue(true)//
				.setType(Boolean.class)//
				.build(); 
		BooleanPropertyManager bpm = new BooleanPropertyManager(propertyDefinition,0);
		
		System.out.println(bpm.toString());
		
		
		bpm.setPropertyValue(13, true);
		bpm.setPropertyValue(14, false);
		bpm.setPropertyValue(11, true);
		
		System.out.println(bpm.toString());
		
	}
}
