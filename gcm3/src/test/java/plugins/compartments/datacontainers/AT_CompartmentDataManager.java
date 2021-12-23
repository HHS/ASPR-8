package plugins.compartments.datacontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.Context;
import nucleus.testsupport.MockContext;
import plugins.compartments.support.CompartmentId;
import plugins.compartments.support.CompartmentPropertyId;
import plugins.compartments.testsupport.TestCompartmentId;
import plugins.properties.support.PropertyDefinition;
import util.MultiKey;
import util.MutableDouble;
import util.MutableInteger;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = CompartmentDataManager.class)
public class AT_CompartmentDataManager {
	
	@Test
	@UnitTestConstructor(args = { Context.class })
	public void testConstructor() {
		// this test is covered by the remaining tests
	}

	@Test
	@UnitTestMethod(name = "addCompartmentId", args = { CompartmentId.class })
	public void testAddCompartmentId() {

		CompartmentDataManager compartmentDataManager = new CompartmentDataManager(MockContext.builder().build());

		// precondition check: if the compartment id is null
		assertThrows(RuntimeException.class, () -> compartmentDataManager.addCompartmentId(null));

		// precondition check: if the compartment was previously added
		compartmentDataManager.addCompartmentId(TestCompartmentId.COMPARTMENT_1);
		assertThrows(RuntimeException.class, () -> compartmentDataManager.addCompartmentId(TestCompartmentId.COMPARTMENT_1));

		// show that the compartment ids that are added can be retrieved
		CompartmentDataManager compartmentDataManager2 = new CompartmentDataManager(MockContext.builder().build());
		Set<CompartmentId> expectedCompartmentIds = new LinkedHashSet<>();
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			compartmentDataManager2.addCompartmentId(testCompartmentId);
			expectedCompartmentIds.add(testCompartmentId);
		}
		assertEquals(expectedCompartmentIds, compartmentDataManager2.getCompartmentIds());

	}

	@Test
	@UnitTestMethod(name = "addCompartmentPropertyDefinition", args = { CompartmentId.class, CompartmentPropertyId.class, PropertyDefinition.class })
	public void testAddCompartmentPropertyDefinition() {
		MockContext mockContext = MockContext.builder()
				.build();
		CompartmentDataManager compartmentDataManager = new CompartmentDataManager(mockContext);

		// create some objects to support the precondition checks
		CompartmentId cId = TestCompartmentId.COMPARTMENT_1;
		CompartmentPropertyId cpID = TestCompartmentId.COMPARTMENT_1.getCompartmentPropertyId(0);
		PropertyDefinition pDef = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(5).build();
		PropertyDefinition badPropertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();

		// precondition check: if the compartment id is null
		assertThrows(RuntimeException.class, () -> compartmentDataManager.addCompartmentPropertyDefinition(null, cpID, pDef));

		// precondition check: if the compartment was not previously added
		assertThrows(RuntimeException.class, () -> compartmentDataManager.addCompartmentPropertyDefinition(cId, cpID, pDef));

		// precondition check: if the compartment property was previously added
		compartmentDataManager.addCompartmentId(cId);
		compartmentDataManager.addCompartmentPropertyDefinition(cId, cpID, pDef);
		assertThrows(RuntimeException.class, () -> compartmentDataManager.addCompartmentPropertyDefinition(cId, cpID, pDef));

		/*
		 * precondition check: if the property definition does not contain a
		 * default
		 */
		assertThrows(RuntimeException.class, () -> compartmentDataManager.addCompartmentPropertyDefinition(cId, cpID, badPropertyDefinition));

		// show that the compartment properties are added

		// collect the property definitions that we expect to find in the data
		// manager
		Set<MultiKey> expectedPropertyDefinitions = new LinkedHashSet<>();

		mockContext = MockContext.builder().build();
		CompartmentDataManager cdm = new CompartmentDataManager(mockContext);
		int defaultValue = 0;
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			cdm.addCompartmentId(testCompartmentId);
			CompartmentPropertyId[] compartmentPropertyIds = testCompartmentId.getCompartmentPropertyIds();
			for (CompartmentPropertyId testCompartmentPropertyId : compartmentPropertyIds) {
				PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(defaultValue++).build();
				cdm.addCompartmentPropertyDefinition(testCompartmentId, testCompartmentPropertyId, propertyDefinition);
				expectedPropertyDefinitions.add(new MultiKey(testCompartmentId, testCompartmentPropertyId, propertyDefinition));
			}
		}

		// collect the property definitions that are acually present
		Set<MultiKey> actualPropertyDefinitions = new LinkedHashSet<>();
		for (CompartmentId compartmentId : cdm.getCompartmentIds()) {
			for (CompartmentPropertyId compartmentPropertyId : cdm.getCompartmentPropertyIds(compartmentId)) {
				PropertyDefinition propertyDefinition = cdm.getCompartmentPropertyDefinition(compartmentId, compartmentPropertyId);
				actualPropertyDefinitions.add(new MultiKey(compartmentId, compartmentPropertyId, propertyDefinition));
			}
		}

		assertEquals(expectedPropertyDefinitions, actualPropertyDefinitions);

	}

	@Test
	@UnitTestMethod(name = "compartmentIdExists", args = { CompartmentId.class })
	public void testCompartmentIdExists() {
		CompartmentDataManager compartmentDataManager = new CompartmentDataManager(MockContext.builder().build());
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			compartmentDataManager.addCompartmentId(testCompartmentId);
		}
		// show that null compartment ids do not exist
		assertFalse(compartmentDataManager.compartmentIdExists(null));

		// show that the compartment ids added do exist
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			assertTrue(compartmentDataManager.compartmentIdExists(testCompartmentId));
		}

		// show that an unknown compartment id does not exist
		assertFalse(compartmentDataManager.compartmentIdExists(TestCompartmentId.getUnknownCompartmentId()));
	}

	@Test
	@UnitTestMethod(name = "compartmentPropertyIdExists", args = { CompartmentId.class, CompartmentPropertyId.class })
	public void testCompartmentPropertyIdExists() {

		// add a set of compartment properties
		MockContext mockContext = MockContext.builder().build();
		CompartmentDataManager cdm = new CompartmentDataManager(mockContext);
		int defaultValue = 0;
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			cdm.addCompartmentId(testCompartmentId);
			CompartmentPropertyId[] compartmentPropertyIds = testCompartmentId.getCompartmentPropertyIds();
			for (CompartmentPropertyId testCompartmentPropertyId : compartmentPropertyIds) {
				PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(defaultValue++).build();
				cdm.addCompartmentPropertyDefinition(testCompartmentId, testCompartmentPropertyId, propertyDefinition);
			}
		}

		// show that the property id we added exist
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			CompartmentPropertyId[] compartmentPropertyIds = testCompartmentId.getCompartmentPropertyIds();
			for (CompartmentPropertyId testCompartmentPropertyId : compartmentPropertyIds) {
				assertTrue(cdm.compartmentPropertyIdExists(testCompartmentId, testCompartmentPropertyId));
			}
		}

		// show that null references return false
		assertFalse(cdm.compartmentPropertyIdExists(null, null));
		assertFalse(cdm.compartmentPropertyIdExists(TestCompartmentId.COMPARTMENT_1, null));
		assertFalse(cdm.compartmentPropertyIdExists(null, TestCompartmentId.COMPARTMENT_1.getCompartmentPropertyId(0)));

		// show that unknown compartments or unknown compartment property ids
		// return false
		CompartmentId knownCompartmentId = TestCompartmentId.COMPARTMENT_1;
		CompartmentId unknownCompartmentId = TestCompartmentId.getUnknownCompartmentId();
		CompartmentPropertyId knownCompartmentPropertyId = TestCompartmentId.COMPARTMENT_1.getCompartmentPropertyId(0);
		CompartmentPropertyId unknownCompartmentPropertyId = TestCompartmentId.getUnknownCompartmentPropertyId();
		assertFalse(cdm.compartmentPropertyIdExists(unknownCompartmentId, knownCompartmentPropertyId));
		assertFalse(cdm.compartmentPropertyIdExists(unknownCompartmentId, unknownCompartmentPropertyId));
		assertFalse(cdm.compartmentPropertyIdExists(knownCompartmentId, unknownCompartmentPropertyId));

	}

	@Test
	@UnitTestMethod(name = "getCompartmentIds", args = {})
	public void testGetCompartmentIds() {
		CompartmentDataManager compartmentDataManager = new CompartmentDataManager(MockContext.builder().build());

		// show that the compartment ids that are added can be retrieved
		Set<CompartmentId> expectedCompartmentIds = new LinkedHashSet<>();
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			compartmentDataManager.addCompartmentId(testCompartmentId);
			expectedCompartmentIds.add(testCompartmentId);
		}
		assertEquals(expectedCompartmentIds, compartmentDataManager.getCompartmentIds());
	}

	@Test
	@UnitTestMethod(name = "getCompartmentPropertyDefinition", args = { CompartmentId.class, CompartmentPropertyId.class })
	public void testGetCompartmentPropertyDefinition() {

		// collect the property definitions that we expect to find in the data
		// manager
		Set<MultiKey> expectedPropertyDefinitions = new LinkedHashSet<>();
		MockContext mockContext = MockContext.builder().build();
		CompartmentDataManager cdm = new CompartmentDataManager(mockContext);
		int defaultValue = 0;
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			cdm.addCompartmentId(testCompartmentId);
			CompartmentPropertyId[] compartmentPropertyIds = testCompartmentId.getCompartmentPropertyIds();
			for (CompartmentPropertyId testCompartmentPropertyId : compartmentPropertyIds) {
				PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(defaultValue++).build();
				cdm.addCompartmentPropertyDefinition(testCompartmentId, testCompartmentPropertyId, propertyDefinition);
				expectedPropertyDefinitions.add(new MultiKey(testCompartmentId, testCompartmentPropertyId, propertyDefinition));
			}
		}

		// collect the property definitions that are actually present
		Set<MultiKey> actualPropertyDefinitions = new LinkedHashSet<>();
		for (CompartmentId compartmentId : cdm.getCompartmentIds()) {
			for (CompartmentPropertyId compartmentPropertyId : cdm.getCompartmentPropertyIds(compartmentId)) {
				PropertyDefinition propertyDefinition = cdm.getCompartmentPropertyDefinition(compartmentId, compartmentPropertyId);
				actualPropertyDefinitions.add(new MultiKey(compartmentId, compartmentPropertyId, propertyDefinition));
			}
		}

		assertEquals(expectedPropertyDefinitions, actualPropertyDefinitions);

		// precondition: the compartment id must exist
		CompartmentPropertyId compartmentPropertyId = TestCompartmentId.COMPARTMENT_1.getCompartmentPropertyId(0);

		assertThrows(RuntimeException.class, () -> cdm.getCompartmentPropertyDefinition(null, compartmentPropertyId));
		assertThrows(RuntimeException.class, () -> cdm.getCompartmentPropertyDefinition(TestCompartmentId.getUnknownCompartmentId(), compartmentPropertyId));

		// precondition: the compartment property id must exist
		assertNull(cdm.getCompartmentPropertyDefinition(TestCompartmentId.COMPARTMENT_1, null));
		assertNull(cdm.getCompartmentPropertyDefinition(TestCompartmentId.COMPARTMENT_1, TestCompartmentId.getUnknownCompartmentPropertyId()));
	}

	@Test
	@UnitTestMethod(name = "getCompartmentPropertyIds", args = { CompartmentId.class })
	public void testGetCompartmentPropertyIds() {
		// collect the property definitions that we expect to find in the data
		// manager
		Set<MultiKey> expectedPropertyDefinitions = new LinkedHashSet<>();
		MockContext mockContext = MockContext.builder()
				.build();
		CompartmentDataManager cdm = new CompartmentDataManager(mockContext);
		int defaultValue = 0;
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			cdm.addCompartmentId(testCompartmentId);
			CompartmentPropertyId[] compartmentPropertyIds = testCompartmentId.getCompartmentPropertyIds();
			for (CompartmentPropertyId testCompartmentPropertyId : compartmentPropertyIds) {
				PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(defaultValue++).build();
				cdm.addCompartmentPropertyDefinition(testCompartmentId, testCompartmentPropertyId, propertyDefinition);
				expectedPropertyDefinitions.add(new MultiKey(testCompartmentId, testCompartmentPropertyId));
			}
		}

		// collect the property definitions that are actually present
		Set<MultiKey> actualPropertyDefinitions = new LinkedHashSet<>();
		for (CompartmentId compartmentId : cdm.getCompartmentIds()) {
			for (CompartmentPropertyId compartmentPropertyId : cdm.getCompartmentPropertyIds(compartmentId)) {
				actualPropertyDefinitions.add(new MultiKey(compartmentId, compartmentPropertyId));
			}
		}

		assertEquals(expectedPropertyDefinitions, actualPropertyDefinitions);

		// precondition: the compartment id must exist
		assertEquals(0, cdm.getCompartmentPropertyIds(null).size());
		assertEquals(0, cdm.getCompartmentPropertyIds(TestCompartmentId.getUnknownCompartmentId()).size());

	}

	@Test
	@UnitTestMethod(name = "getCompartmentPropertyTime", args = { CompartmentId.class, CompartmentPropertyId.class })
	public void testGetCompartmentPropertyTime() {

		MutableDouble time = new MutableDouble(0);
		MockContext mockContext = MockContext.builder().setTimeSupplier(()->time.getValue()).build();
		CompartmentDataManager cdm = new CompartmentDataManager(mockContext);
		int defaultValue = 0;
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			cdm.addCompartmentId(testCompartmentId);
			CompartmentPropertyId[] compartmentPropertyIds = testCompartmentId.getCompartmentPropertyIds();
			for (CompartmentPropertyId testCompartmentPropertyId : compartmentPropertyIds) {
				PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(defaultValue++).build();
				cdm.addCompartmentPropertyDefinition(testCompartmentId, testCompartmentPropertyId, propertyDefinition);
			}
		}

		// show that the property times are currently zero
		for (CompartmentId compartmentId : cdm.getCompartmentIds()) {
			for (CompartmentPropertyId compartmentPropertyId : cdm.getCompartmentPropertyIds(compartmentId)) {
				double compartmentPropertyTime = cdm.getCompartmentPropertyTime(compartmentId, compartmentPropertyId);
				assertEquals(0, compartmentPropertyTime, 0);
			}
		}

		// show that changes to the property values properly reflect the time in
		// the mock context
		time.setValue(0);
		int newPropertyValue = 100;

		for (CompartmentId compartmentId : cdm.getCompartmentIds()) {
			for (CompartmentPropertyId compartmentPropertyId : cdm.getCompartmentPropertyIds(compartmentId)) {
				// get the current time for the property
				double previousCompartmentPropertyTime = cdm.getCompartmentPropertyTime(compartmentId, compartmentPropertyId);

				// move time forward and show that does not alter the
				// property time
				time.increment(0.01);
				
				double currentCompartmentPropertyTime = cdm.getCompartmentPropertyTime(compartmentId, compartmentPropertyId);
				assertEquals(previousCompartmentPropertyTime, currentCompartmentPropertyTime, 0);

				// change the property value and show that property time is
				// correct
				cdm.setCompartmentPropertyValue(compartmentId, compartmentPropertyId, newPropertyValue++);
				currentCompartmentPropertyTime = cdm.getCompartmentPropertyTime(compartmentId, compartmentPropertyId);
				assertEquals(mockContext.getTime(), currentCompartmentPropertyTime, 0);
			}
		}

		// precondition checks -- show all combinations of null or unknown
		// inputs generate a runtime exception
		CompartmentId unknownCompartmentId = TestCompartmentId.getUnknownCompartmentId();
		CompartmentId knownCompartmentId = TestCompartmentId.COMPARTMENT_1;
		CompartmentPropertyId unknownCompartmentPropertyId = TestCompartmentId.getUnknownCompartmentPropertyId();
		CompartmentPropertyId knownCompartmentPropertyId = TestCompartmentId.COMPARTMENT_1.getCompartmentPropertyId(0);

		assertThrows(RuntimeException.class, () -> cdm.getCompartmentPropertyTime(null, null));
		assertThrows(RuntimeException.class, () -> cdm.getCompartmentPropertyTime(null, unknownCompartmentPropertyId));
		assertThrows(RuntimeException.class, () -> cdm.getCompartmentPropertyTime(null, knownCompartmentPropertyId));
		assertThrows(RuntimeException.class, () -> cdm.getCompartmentPropertyTime(unknownCompartmentId, null));
		assertThrows(RuntimeException.class, () -> cdm.getCompartmentPropertyTime(unknownCompartmentId, unknownCompartmentPropertyId));
		assertThrows(RuntimeException.class, () -> cdm.getCompartmentPropertyTime(unknownCompartmentId, knownCompartmentPropertyId));
		assertThrows(RuntimeException.class, () -> cdm.getCompartmentPropertyTime(knownCompartmentId, null));
		assertThrows(RuntimeException.class, () -> cdm.getCompartmentPropertyTime(knownCompartmentId, unknownCompartmentPropertyId));

	}

	@Test
	@UnitTestMethod(name = "getCompartmentPropertyValue", args = { CompartmentId.class, CompartmentPropertyId.class })
	public void testGetCompartmentPropertyValue() {
		MutableDouble time = new MutableDouble(0);
		MockContext mockContext = MockContext.builder().setTimeSupplier(()->time.getValue()).build();
		CompartmentDataManager cdm = new CompartmentDataManager(mockContext);
		int runningValue = 0;
		Map<MultiKey, MutableInteger> expectedValues = new LinkedHashMap<>();
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			cdm.addCompartmentId(testCompartmentId);
			CompartmentPropertyId[] compartmentPropertyIds = testCompartmentId.getCompartmentPropertyIds();
			for (CompartmentPropertyId testCompartmentPropertyId : compartmentPropertyIds) {
				runningValue++;
				PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(runningValue).build();
				cdm.addCompartmentPropertyDefinition(testCompartmentId, testCompartmentPropertyId, propertyDefinition);
				expectedValues.put(new MultiKey(testCompartmentId, testCompartmentPropertyId), new MutableInteger(runningValue));
			}
		}

		// show that the property values are currently what we expect
		for (CompartmentId compartmentId : cdm.getCompartmentIds()) {
			for (CompartmentPropertyId compartmentPropertyId : cdm.getCompartmentPropertyIds(compartmentId)) {
				Integer propertyValue = cdm.getCompartmentPropertyValue(compartmentId, compartmentPropertyId);
				MutableInteger mutableInteger = expectedValues.get(new MultiKey(compartmentId, compartmentPropertyId));
				assertEquals(mutableInteger.getValue(), propertyValue.intValue());
			}
		}

		// show that changes to the property values occur
		time.setValue(0);

		for (CompartmentId compartmentId : cdm.getCompartmentIds()) {
			for (CompartmentPropertyId compartmentPropertyId : cdm.getCompartmentPropertyIds(compartmentId)) {

				// move time forward and show that does not alter the
				// property values
				time.increment(0.01);
				

				// change the property value and show that property time is
				// correct
				cdm.setCompartmentPropertyValue(compartmentId, compartmentPropertyId, runningValue);
				Integer currentValue = cdm.getCompartmentPropertyValue(compartmentId, compartmentPropertyId);
				assertEquals(runningValue, currentValue.intValue());
				runningValue++;
			}
		}

		// precondition checks -- show all combinations of null or unknown
		// inputs generate a runtime exception
		CompartmentId unknownCompartmentId = TestCompartmentId.getUnknownCompartmentId();
		CompartmentId knownCompartmentId = TestCompartmentId.COMPARTMENT_1;
		CompartmentPropertyId unknownCompartmentPropertyId = TestCompartmentId.getUnknownCompartmentPropertyId();
		CompartmentPropertyId knownCompartmentPropertyId = TestCompartmentId.COMPARTMENT_1.getCompartmentPropertyId(0);

		assertThrows(RuntimeException.class, () -> cdm.getCompartmentPropertyValue(null, null));
		assertThrows(RuntimeException.class, () -> cdm.getCompartmentPropertyValue(null, unknownCompartmentPropertyId));
		assertThrows(RuntimeException.class, () -> cdm.getCompartmentPropertyValue(null, knownCompartmentPropertyId));
		assertThrows(RuntimeException.class, () -> cdm.getCompartmentPropertyValue(unknownCompartmentId, null));
		assertThrows(RuntimeException.class, () -> cdm.getCompartmentPropertyValue(unknownCompartmentId, unknownCompartmentPropertyId));
		assertThrows(RuntimeException.class, () -> cdm.getCompartmentPropertyValue(unknownCompartmentId, knownCompartmentPropertyId));
		assertThrows(RuntimeException.class, () -> cdm.getCompartmentPropertyValue(knownCompartmentId, null));
		assertThrows(RuntimeException.class, () -> cdm.getCompartmentPropertyValue(knownCompartmentId, unknownCompartmentPropertyId));

	}

	@Test
	@UnitTestMethod(name = "setCompartmentPropertyValue", args = { CompartmentId.class, CompartmentPropertyId.class, Object.class })
	public void testSetCompartmentPropertyValue() {
		MutableDouble time = new MutableDouble(0);
		MockContext mockContext = MockContext.builder().setTimeSupplier(()->time.getValue()).build();
		CompartmentDataManager cdm = new CompartmentDataManager(mockContext);
		int runningValue = 0;
		Map<MultiKey, MutableInteger> expectedValues = new LinkedHashMap<>();
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			cdm.addCompartmentId(testCompartmentId);
			CompartmentPropertyId[] compartmentPropertyIds = testCompartmentId.getCompartmentPropertyIds();
			for (CompartmentPropertyId testCompartmentPropertyId : compartmentPropertyIds) {
				runningValue++;
				PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(runningValue).build();
				cdm.addCompartmentPropertyDefinition(testCompartmentId, testCompartmentPropertyId, propertyDefinition);
				expectedValues.put(new MultiKey(testCompartmentId, testCompartmentPropertyId), new MutableInteger(runningValue));
			}
		}

		// show that the property values are currently what we expect
		for (CompartmentId compartmentId : cdm.getCompartmentIds()) {
			for (CompartmentPropertyId compartmentPropertyId : cdm.getCompartmentPropertyIds(compartmentId)) {
				Integer propertyValue = cdm.getCompartmentPropertyValue(compartmentId, compartmentPropertyId);
				MutableInteger mutableInteger = expectedValues.get(new MultiKey(compartmentId, compartmentPropertyId));
				assertEquals(mutableInteger.getValue(), propertyValue.intValue());
			}
		}

		// show that changes to the property values occur
		time.setValue(0);

		for (CompartmentId compartmentId : cdm.getCompartmentIds()) {
			for (CompartmentPropertyId compartmentPropertyId : cdm.getCompartmentPropertyIds(compartmentId)) {

				// move time forward and show that does not alter the
				// property values
				time.increment(0.01);
				

				// change the property value and show that property time is
				// correct
				cdm.setCompartmentPropertyValue(compartmentId, compartmentPropertyId, runningValue);
				Integer currentValue = cdm.getCompartmentPropertyValue(compartmentId, compartmentPropertyId);
				assertEquals(runningValue, currentValue.intValue());
				runningValue++;
			}
		}

		// precondition checks -- show all combinations of null or unknown
		// inputs generate a runtime exception
		CompartmentId unknownCompartmentId = TestCompartmentId.getUnknownCompartmentId();
		CompartmentId knownCompartmentId = TestCompartmentId.COMPARTMENT_1;
		CompartmentPropertyId unknownCompartmentPropertyId = TestCompartmentId.getUnknownCompartmentPropertyId();
		CompartmentPropertyId knownCompartmentPropertyId = TestCompartmentId.COMPARTMENT_1.getCompartmentPropertyId(0);

		assertThrows(RuntimeException.class, () -> cdm.setCompartmentPropertyValue(null, null,1000));
		assertThrows(RuntimeException.class, () -> cdm.setCompartmentPropertyValue(null, unknownCompartmentPropertyId,1000));
		assertThrows(RuntimeException.class, () -> cdm.setCompartmentPropertyValue(null, knownCompartmentPropertyId,1000));
		assertThrows(RuntimeException.class, () -> cdm.setCompartmentPropertyValue(unknownCompartmentId, null,1000));
		assertThrows(RuntimeException.class, () -> cdm.setCompartmentPropertyValue(unknownCompartmentId, unknownCompartmentPropertyId,1000));
		assertThrows(RuntimeException.class, () -> cdm.setCompartmentPropertyValue(unknownCompartmentId, knownCompartmentPropertyId,1000));
		assertThrows(RuntimeException.class, () -> cdm.setCompartmentPropertyValue(knownCompartmentId, null,1000));
		assertThrows(RuntimeException.class, () -> cdm.setCompartmentPropertyValue(knownCompartmentId, unknownCompartmentPropertyId,1000));

	}

}
