package plugins.partitions.testsupport.attributes.support;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * A test support enumeration of attribute id value with associated attribute
 * definitions.
 */
public enum TestAttributeId implements AttributeId {

	INT_0(AttributeDefinition.builder().setType(Integer.class).setDefaultValue(0).build()),
	INT_1(AttributeDefinition.builder().setType(Integer.class).setDefaultValue(1).build()),
	DOUBLE_0(AttributeDefinition.builder().setType(Double.class).setDefaultValue(0.0).build()),
	DOUBLE_1(AttributeDefinition.builder().setType(Double.class).setDefaultValue(1.0).build()),
	BOOLEAN_0(AttributeDefinition.builder().setType(Boolean.class).setDefaultValue(false).build()),
	BOOLEAN_1(AttributeDefinition.builder().setType(Boolean.class).setDefaultValue(true).build());

	private final AttributeDefinition attributeDefinition;
	
	public AttributeDefinition getAttributeDefinition() {
		return attributeDefinition;
	}

	private TestAttributeId(AttributeDefinition attributeDefinition) {
		this.attributeDefinition = attributeDefinition;
	}
	
	public static AttributeId getUnknownAttributeId() {
		return new AttributeId() {};
	}
	
	public Object getRandomPropertyValue(final RandomGenerator randomGenerator) {
		switch (this) {
		case INT_0,INT_1:
			return randomGenerator.nextInt();
		case DOUBLE_0, DOUBLE_1:
			return randomGenerator.nextDouble();
		case BOOLEAN_0, BOOLEAN_1:
			return randomGenerator.nextBoolean();		
		default:			
			throw new RuntimeException("unhandled case: " + this);

		}
	}

}
