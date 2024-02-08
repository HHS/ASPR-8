package gov.hhs.aspr.ms.gcm.plugins.regions.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_RegionPropertyDefinitionInitialization {

	@Test
	@UnitTestMethod(target = RegionPropertyDefinitionInitialization.class, name = "builder", args = {})
	public void testBuilder() {
		RegionPropertyDefinitionInitialization.Builder builder = RegionPropertyDefinitionInitialization.builder();

		assertNotNull(builder);
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDefinitionInitialization.class, name = "getPropertyDefinition", args = {})
	public void testGetPropertyDefinition() {
		RegionPropertyDefinitionInitialization.Builder builder = RegionPropertyDefinitionInitialization.builder();
		RegionPropertyId regionPropertyId = new SimpleRegionPropertyId(1000);
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setDefaultValue(3).setType(Integer.class).build();
		builder.setPropertyDefinition(propertyDefinition).setRegionPropertyId(regionPropertyId);

		RegionPropertyDefinitionInitialization propertyDefinitionInitialization = builder.build();

		assertNotNull(propertyDefinitionInitialization);
		assertEquals(propertyDefinition, propertyDefinitionInitialization.getPropertyDefinition());
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDefinitionInitialization.class, name = "getPropertyValues", args = {})
	public void testGetPropertyValues() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1981422884617107939L);
		RegionPropertyDefinitionInitialization.Builder builder = RegionPropertyDefinitionInitialization.builder();
		RegionPropertyId regionPropertyId = new SimpleRegionPropertyId(1000);
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setDefaultValue(3).setType(Integer.class).build();
		builder.setPropertyDefinition(propertyDefinition).setRegionPropertyId(regionPropertyId);

		List<Pair<RegionId, Object>> expectedValues = new ArrayList<>();

		for (int i = 0; i < 15; i++) {
			int value = randomGenerator.nextInt(100);
			RegionId regionId = new SimpleRegionId(i * 2 + 5);
			builder.addPropertyValue(regionId, value);
			expectedValues.add(new Pair<>(regionId, value));
		}

		RegionPropertyDefinitionInitialization propertyDefinitionInitialization = builder.build();

		assertNotNull(propertyDefinitionInitialization);
		assertEquals(expectedValues, propertyDefinitionInitialization.getPropertyValues());
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDefinitionInitialization.class, name = "getRegionPropertyId", args = {})
	public void testGetRegionPropertyId() {
		RegionPropertyDefinitionInitialization.Builder builder = RegionPropertyDefinitionInitialization.builder();
		RegionPropertyId regionPropertyId = new SimpleRegionPropertyId(1000);
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setDefaultValue(3).setType(Integer.class).build();
		builder.setPropertyDefinition(propertyDefinition).setRegionPropertyId(regionPropertyId);

		RegionPropertyDefinitionInitialization propertyDefinitionInitialization = builder.build();

		assertNotNull(propertyDefinitionInitialization);
		assertEquals(regionPropertyId, propertyDefinitionInitialization.getRegionPropertyId());
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDefinitionInitialization.Builder.class, name = "build", args = {})
	public void testBuild() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1981422884617107939L);
		RegionPropertyDefinitionInitialization.Builder builder = RegionPropertyDefinitionInitialization.builder();
		RegionPropertyId regionPropertyId = new SimpleRegionPropertyId(1000);
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setDefaultValue(3).setType(Integer.class).build();
		builder.setPropertyDefinition(propertyDefinition).setRegionPropertyId(regionPropertyId);

		for (int i = 0; i < 15; i++) {
			int value = randomGenerator.nextInt(100);
			RegionId regionId = new SimpleRegionId(i * 2 + 5);
			builder.addPropertyValue(regionId, value);
		}

		RegionPropertyDefinitionInitialization propertyDefinitionInitialization = builder.build();

		assertNotNull(propertyDefinitionInitialization);

		// precondition: property definition is null
		PropertyDefinition nullPropertyDefinition = null;
		ContractException contractException = assertThrows(ContractException.class, () -> builder.setPropertyDefinition(nullPropertyDefinition).setRegionPropertyId(regionPropertyId).build());
		assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());

		// precondition: region property id is null
		RegionPropertyId nullRegionPropertyId = null;
		contractException = assertThrows(ContractException.class, () -> builder.setPropertyDefinition(propertyDefinition).setRegionPropertyId(nullRegionPropertyId).build());
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// precondition: incompatible property value
		PropertyDefinition badPropertyDefinition = PropertyDefinition.builder().setDefaultValue(false).setType(Boolean.class).build();
		contractException = assertThrows(ContractException.class,
				() -> builder.setPropertyDefinition(badPropertyDefinition).setRegionPropertyId(regionPropertyId).addPropertyValue(new SimpleRegionId(1000), 100).build());
		assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDefinitionInitialization.Builder.class, name = "setPropertyDefinition", args = { PropertyDefinition.class })
	public void testSetPropertyDefinition() {
		RegionPropertyDefinitionInitialization.Builder builder = RegionPropertyDefinitionInitialization.builder();
		RegionPropertyId regionPropertyId = new SimpleRegionPropertyId(1000);
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setDefaultValue(3).setType(Integer.class).build();
		builder.setPropertyDefinition(propertyDefinition).setRegionPropertyId(regionPropertyId);

		RegionPropertyDefinitionInitialization propertyDefinitionInitialization = builder.build();

		assertNotNull(propertyDefinitionInitialization);
		assertEquals(propertyDefinition, propertyDefinitionInitialization.getPropertyDefinition());

		// precondition: property definition is null
		ContractException contractException = assertThrows(ContractException.class, () -> builder.setPropertyDefinition(null));
		assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDefinitionInitialization.Builder.class, name = "addPropertyValue", args = { RegionId.class, Object.class })
	public void testAddPropertyValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9052754083757003238L);
		RegionPropertyDefinitionInitialization.Builder builder = RegionPropertyDefinitionInitialization.builder();
		RegionPropertyId regionPropertyId = new SimpleRegionPropertyId(1000);
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setDefaultValue(3).setType(Integer.class).build();
		builder.setPropertyDefinition(propertyDefinition).setRegionPropertyId(regionPropertyId);

		List<Pair<RegionId, Object>> expectedValues = new ArrayList<>();

		for (int i = 0; i < 15; i++) {
			int value = randomGenerator.nextInt(100);
			RegionId regionId = new SimpleRegionId(i * 2 + 5);
			builder.addPropertyValue(regionId, value);
			expectedValues.add(new Pair<>(regionId, value));
		}

		RegionPropertyDefinitionInitialization propertyDefinitionInitialization = builder.build();

		assertNotNull(propertyDefinitionInitialization);
		assertEquals(expectedValues, propertyDefinitionInitialization.getPropertyValues());

		// precondition: region id is null
		
		ContractException contractException = assertThrows(ContractException.class, () -> RegionPropertyDefinitionInitialization.builder().addPropertyValue(null, 1000));
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		// precondition: value is null
		contractException = assertThrows(ContractException.class, () -> RegionPropertyDefinitionInitialization.builder().addPropertyValue(new SimpleRegionId(1000), null));
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDefinitionInitialization.Builder.class, name = "setRegionPropertyId", args = { RegionPropertyId.class })
	public void testSetRegionPropertyId() {
		RegionPropertyDefinitionInitialization.Builder builder = RegionPropertyDefinitionInitialization.builder();
		RegionPropertyId regionPropertyId = new SimpleRegionPropertyId(1000);
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setDefaultValue(3).setType(Integer.class).build();
		builder.setPropertyDefinition(propertyDefinition).setRegionPropertyId(regionPropertyId);

		RegionPropertyDefinitionInitialization propertyDefinitionInitialization = builder.build();

		assertNotNull(propertyDefinitionInitialization);
		assertEquals(regionPropertyId, propertyDefinitionInitialization.getRegionPropertyId());

		// precondition: region property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> builder.setRegionPropertyId(null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
	}

}
