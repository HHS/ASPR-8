package plugins.resources.testsupport;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import plugins.resources.support.ResourcePropertyId;
import plugins.util.properties.PropertyDefinition;

/**
 * Enumeration that identifies resources for all tests
 */
public enum TestResourcePropertyId implements ResourcePropertyId {

	ResourceProperty_1_1_BOOLEAN_MUTABLE(TestResourceId.RESOURCE_1, PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).build()),
	ResourceProperty_1_2_INTEGER_MUTABLE(TestResourceId.RESOURCE_1, PropertyDefinition.builder().setType(Integer.class).setDefaultValue(0).build()),
	ResourceProperty_1_3_DOUBLE_MUTABLE(TestResourceId.RESOURCE_1, PropertyDefinition.builder().setType(Double.class).setDefaultValue(0.0).build()),
	ResourceProperty_2_1_BOOLEAN_MUTABLE(TestResourceId.RESOURCE_2, PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).build()),
	ResourceProperty_2_2_INTEGER_MUTABLE(TestResourceId.RESOURCE_2, PropertyDefinition.builder().setType(Integer.class).setDefaultValue(5).build()),
	ResourceProperty_3_1_BOOLEAN_MUTABLE(TestResourceId.RESOURCE_3, PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).build()),
	ResourceProperty_3_2_STRING_MUTABLE(TestResourceId.RESOURCE_3, PropertyDefinition.builder().setType(String.class).setDefaultValue("").build()),
	ResourceProperty_4_1_BOOLEAN_MUTABLE(TestResourceId.RESOURCE_4, PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).build()),
	ResourceProperty_5_1_INTEGER_IMMUTABLE(TestResourceId.RESOURCE_5, PropertyDefinition.builder().setType(Integer.class).setDefaultValue(7).setPropertyValueMutability(false).build()),
	ResourceProperty_5_1_DOUBLE_IMMUTABLE(TestResourceId.RESOURCE_5, PropertyDefinition.builder().setType(Double.class).setDefaultValue(2.7).setPropertyValueMutability(false).build());

	private final TestResourceId testResourceId;
	private final PropertyDefinition propertyDefinition;

	public PropertyDefinition getPropertyDefinition() {
		return propertyDefinition;
	}

	private TestResourcePropertyId(TestResourceId testResourceId, PropertyDefinition propertyDefinition) {
		this.testResourceId = testResourceId;
		this.propertyDefinition = propertyDefinition;
	}

	public TestResourceId getTestResourceId() {
		return testResourceId;
	}

	/**
	 * Returns a new {@link ResourcePropertyId} instance.
	 */
	public static ResourcePropertyId getUnknownResourcePropertyId() {
		return new ResourcePropertyId() {
		};
	}

	public static Set<TestResourcePropertyId> getTestResourcePropertyIds(TestResourceId testResourceId) {
		Set<TestResourcePropertyId> result = new LinkedHashSet<>();
		for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
			if (testResourcePropertyId.testResourceId == testResourceId) {
				result.add(testResourcePropertyId);
			}
		}
		return result;
	}
	
	public static TestResourcePropertyId getRandomResourcePropertyId(TestResourceId testResourceId,final RandomGenerator randomGenerator) {
		List<TestResourcePropertyId> list = new ArrayList<>(getTestResourcePropertyIds(testResourceId));
		return list.get(randomGenerator.nextInt(list.size()));
	}

	public Object getRandomPropertyValue(final RandomGenerator randomGenerator) {
		switch (this) {
		case ResourceProperty_1_1_BOOLEAN_MUTABLE:
			return randomGenerator.nextBoolean();
		case ResourceProperty_1_2_INTEGER_MUTABLE:
			return randomGenerator.nextInt();
		case ResourceProperty_1_3_DOUBLE_MUTABLE:
			return randomGenerator.nextDouble();
		case ResourceProperty_2_1_BOOLEAN_MUTABLE:
			return randomGenerator.nextBoolean();
		case ResourceProperty_2_2_INTEGER_MUTABLE:
			return randomGenerator.nextInt();
		case ResourceProperty_3_1_BOOLEAN_MUTABLE:
			return randomGenerator.nextBoolean();
		case ResourceProperty_3_2_STRING_MUTABLE:
			return Integer.toString(randomGenerator.nextInt());
		case ResourceProperty_4_1_BOOLEAN_MUTABLE:
			return randomGenerator.nextBoolean();
		case ResourceProperty_5_1_DOUBLE_IMMUTABLE:
			return randomGenerator.nextDouble();
		case ResourceProperty_5_1_INTEGER_IMMUTABLE:
			return randomGenerator.nextInt();
		default:			
			throw new RuntimeException("unhandled case: " + this);

		}
	}

}