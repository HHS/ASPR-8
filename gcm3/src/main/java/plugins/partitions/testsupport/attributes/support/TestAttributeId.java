package plugins.partitions.testsupport.attributes.support;

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

}
