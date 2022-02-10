package plugins.regions.datacontainers;

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

import nucleus.SimulationContext;
import nucleus.testsupport.MockSimulationContext;
import plugins.properties.support.PropertyDefinition;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.regions.testsupport.TestRegionId;
import plugins.regions.testsupport.TestRegionPropertyId;
import util.MultiKey;
import util.MutableDouble;
import util.MutableInteger;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = RegionDataManager.class)
public class AT_RegionDataManager {

	
	
	@Test
	@UnitTestConstructor(args = { SimulationContext.class })
	public void testConstructor() {
		// this test is covered by the remaining tests
	}

	@Test
	@UnitTestMethod(name = "addRegionId", args = { RegionId.class })
	public void testAddRegionId() {

		RegionDataManager regionDataManager = new RegionDataManager(MockSimulationContext.builder().build());

		// precondition check: if the region id is null
		assertThrows(RuntimeException.class, () -> regionDataManager.addRegionId(null));

		// precondition check: if the region was previously added
		regionDataManager.addRegionId(TestRegionId.REGION_1);
		assertThrows(RuntimeException.class, () -> regionDataManager.addRegionId(TestRegionId.REGION_1));

		// show that the region ids that are added can be retrieved
		RegionDataManager regionDataManager2 = new RegionDataManager(MockSimulationContext.builder().build());
		Set<RegionId> expectedRegionIds = new LinkedHashSet<>();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionDataManager2.addRegionId(testRegionId);
			expectedRegionIds.add(testRegionId);
		}
		assertEquals(expectedRegionIds, regionDataManager2.getRegionIds());

	}

	@Test
	@UnitTestMethod(name = "addRegionPropertyDefinition", args = { RegionId.class, RegionPropertyId.class, PropertyDefinition.class })
	public void testAddRegionPropertyDefinition() {
		
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		RegionDataManager regionDataManager = new RegionDataManager(mockSimulationContext);

		// create some objects to support the precondition checks
		RegionId rId = TestRegionId.REGION_1;
		RegionPropertyId rpID = TestRegionPropertyId.REGION_PROPERTY_1;
		PropertyDefinition pDef = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(5).build();
		PropertyDefinition badPropertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();

		// precondition check: if the region property was previously added
		regionDataManager.addRegionId(rId);
		regionDataManager.addRegionPropertyDefinition(rpID, pDef);
		assertThrows(RuntimeException.class, () -> regionDataManager.addRegionPropertyDefinition(rpID, pDef));

		/*
		 * precondition check: if the property definition does not contain a
		 * default
		 */
		assertThrows(RuntimeException.class, () -> regionDataManager.addRegionPropertyDefinition(rpID, badPropertyDefinition));

		// show that the region properties are added

		// collect the property definitions that we expect to find in the data
		// manager
		Set<MultiKey> expectedPropertyDefinitions = new LinkedHashSet<>();

		RegionDataManager rdm = new RegionDataManager(mockSimulationContext);

		for (TestRegionId testRegionId : TestRegionId.values()) {
			rdm.addRegionId(testRegionId);
		}

		int defaultValue = 0;
		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(defaultValue++).build();
			rdm.addRegionPropertyDefinition(testRegionPropertyId, propertyDefinition);
			expectedPropertyDefinitions.add(new MultiKey(testRegionPropertyId, propertyDefinition));
		}

		// collect the property definitions that are actually present
		Set<MultiKey> actualPropertyDefinitions = new LinkedHashSet<>();

		for (RegionPropertyId regionPropertyId : rdm.getRegionPropertyIds()) {
			PropertyDefinition propertyDefinition = rdm.getRegionPropertyDefinition(regionPropertyId);
			actualPropertyDefinitions.add(new MultiKey(regionPropertyId, propertyDefinition));
		}

		assertEquals(expectedPropertyDefinitions, actualPropertyDefinitions);

	}

	@Test
	@UnitTestMethod(name = "regionIdExists", args = { RegionId.class })
	public void testRegionIdExists() {
		RegionDataManager regionDataManager = new RegionDataManager(MockSimulationContext.builder().build());
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionDataManager.addRegionId(testRegionId);
		}
		// show that null region ids do not exist
		assertFalse(regionDataManager.regionIdExists(null));

		// show that the region ids added do exist
		for (TestRegionId testRegionId : TestRegionId.values()) {
			assertTrue(regionDataManager.regionIdExists(testRegionId));
		}

		// show that an unknown region id does not exist
		assertFalse(regionDataManager.regionIdExists(TestRegionId.getUnknownRegionId()));
	}

	@Test
	@UnitTestMethod(name = "regionPropertyIdExists", args = { RegionPropertyId.class })
	public void testRegionPropertyIdExists() {

		// add a set of region properties
		
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		RegionDataManager rdm = new RegionDataManager(mockSimulationContext);

		for (TestRegionId testRegionId : TestRegionId.values()) {
			rdm.addRegionId(testRegionId);
		}

		int defaultValue = 0;
		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(defaultValue++).build();
			rdm.addRegionPropertyDefinition(testRegionPropertyId, propertyDefinition);
		}

		// show that the property ids we added exist
		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			assertTrue(rdm.regionPropertyIdExists(testRegionPropertyId));
		}

		// show that null references return false
		assertFalse(rdm.regionPropertyIdExists(null));

		// show that unknown region property ids return false
		assertFalse(rdm.regionPropertyIdExists(TestRegionPropertyId.getUnknownRegionPropertyId()));

	}

	@Test
	@UnitTestMethod(name = "getRegionIds", args = {})
	public void testGetRegionIds() {
		RegionDataManager regionDataManager = new RegionDataManager(MockSimulationContext.builder().build());

		// show that the region ids that are added can be retrieved
		Set<RegionId> expectedRegionIds = new LinkedHashSet<>();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionDataManager.addRegionId(testRegionId);
			expectedRegionIds.add(testRegionId);
		}
		assertEquals(expectedRegionIds, regionDataManager.getRegionIds());
	}

	@Test
	@UnitTestMethod(name = "getRegionPropertyDefinition", args = { RegionPropertyId.class })
	public void testGetRegionPropertyDefinition() {

		// collect the property definitions that we expect to find in the data
		// manager
		Set<MultiKey> expectedPropertyDefinitions = new LinkedHashSet<>();
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		RegionDataManager rdm = new RegionDataManager(mockSimulationContext);
		
		for (TestRegionId testRegionId : TestRegionId.values()) {
			rdm.addRegionId(testRegionId);
		}

		int defaultValue = 0;
		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(defaultValue++).build();
			rdm.addRegionPropertyDefinition(testRegionPropertyId, propertyDefinition);
			expectedPropertyDefinitions.add(new MultiKey(testRegionPropertyId, propertyDefinition));
		}

		// collect the property definitions that are actually present
		Set<MultiKey> actualPropertyDefinitions = new LinkedHashSet<>();

		for (RegionPropertyId regionPropertyId : rdm.getRegionPropertyIds()) {
			PropertyDefinition propertyDefinition = rdm.getRegionPropertyDefinition(regionPropertyId);
			actualPropertyDefinitions.add(new MultiKey(regionPropertyId, propertyDefinition));
		}

		assertEquals(expectedPropertyDefinitions, actualPropertyDefinitions);

		// precondition: the region property id must exist
		assertNull(rdm.getRegionPropertyDefinition(null));
		assertNull(rdm.getRegionPropertyDefinition(TestRegionPropertyId.getUnknownRegionPropertyId()));
	}

	@Test
	@UnitTestMethod(name = "getRegionPropertyIds", args = {})
	public void testGetRegionPropertyIds() {
		// collect the property definitions that we expect to find in the data
		// manager
		Set<RegionPropertyId> expectedPropertyDefinitions = new LinkedHashSet<>();

		
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		RegionDataManager rdm = new RegionDataManager(mockSimulationContext);
		
		for (TestRegionId testRegionId : TestRegionId.values()) {
			rdm.addRegionId(testRegionId);
		}
		
		int defaultValue = 0;
		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(defaultValue++).build();
			rdm.addRegionPropertyDefinition(testRegionPropertyId, propertyDefinition);
			expectedPropertyDefinitions.add(testRegionPropertyId);
		}

		// collect the property definitions that are actually present
		Set<RegionPropertyId> actualPropertyDefinitions = rdm.getRegionPropertyIds();

		assertEquals(expectedPropertyDefinitions, actualPropertyDefinitions);

		// no precondition tests
	}

	@Test
	@UnitTestMethod(name = "getRegionPropertyTime", args = { RegionId.class, RegionPropertyId.class })
	public void testGetRegionPropertyTime() {

		MutableDouble time = new MutableDouble(0);
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().setTimeSupplier(()->time.getValue()).build();
		RegionDataManager rdm = new RegionDataManager(mockSimulationContext);

		for (TestRegionId testRegionId : TestRegionId.values()) {
			rdm.addRegionId(testRegionId);
		}

		int defaultValue = 0;
		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(defaultValue++).build();
			rdm.addRegionPropertyDefinition(testRegionPropertyId, propertyDefinition);
		}

		// show that the property times are currently zero
		for (RegionId regionId : rdm.getRegionIds()) {
			for (RegionPropertyId regionPropertyId : rdm.getRegionPropertyIds()) {
				double regionPropertyTime = rdm.getRegionPropertyTime(regionId, regionPropertyId);
				assertEquals(0, regionPropertyTime, 0);
			}
		}

		// show that changes to the property values properly reflect the time in
		// the mock context
		time.setValue(0);
		int newPropertyValue = 100;

		for (RegionId regionId : rdm.getRegionIds()) {
			for (RegionPropertyId regionPropertyId : rdm.getRegionPropertyIds()) {
				// get the current time for the property
				double previousRegionPropertyTime = rdm.getRegionPropertyTime(regionId, regionPropertyId);

				// move time forward and show that does not alter the
				// property time
				time.increment(0.01);
				
				double currentRegionPropertyTime = rdm.getRegionPropertyTime(regionId, regionPropertyId);
				assertEquals(previousRegionPropertyTime, currentRegionPropertyTime, 0);

				// change the property value and show that property time is
				// correct
				rdm.setRegionPropertyValue(regionId, regionPropertyId, newPropertyValue++);
				currentRegionPropertyTime = rdm.getRegionPropertyTime(regionId, regionPropertyId);
				assertEquals(mockSimulationContext.getTime(), currentRegionPropertyTime, 0);
			}
		}

		// precondition checks -- show all combinations of null or unknown
		// inputs generate a runtime exception
		RegionId unknownRegionId = TestRegionId.getUnknownRegionId();
		RegionId knownRegionId = TestRegionId.REGION_1;
		RegionPropertyId unknownRegionPropertyId = TestRegionPropertyId.getUnknownRegionPropertyId();
		RegionPropertyId knownRegionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1;

		assertThrows(RuntimeException.class, () -> rdm.getRegionPropertyTime(null, null));
		assertThrows(RuntimeException.class, () -> rdm.getRegionPropertyTime(null, unknownRegionPropertyId));
		assertThrows(RuntimeException.class, () -> rdm.getRegionPropertyTime(null, knownRegionPropertyId));
		assertThrows(RuntimeException.class, () -> rdm.getRegionPropertyTime(unknownRegionId, null));
		assertThrows(RuntimeException.class, () -> rdm.getRegionPropertyTime(unknownRegionId, unknownRegionPropertyId));
		assertThrows(RuntimeException.class, () -> rdm.getRegionPropertyTime(unknownRegionId, knownRegionPropertyId));
		assertThrows(RuntimeException.class, () -> rdm.getRegionPropertyTime(knownRegionId, null));
		assertThrows(RuntimeException.class, () -> rdm.getRegionPropertyTime(knownRegionId, unknownRegionPropertyId));

	}

	@Test
	@UnitTestMethod(name = "getRegionPropertyValue", args = { RegionId.class, RegionPropertyId.class })
	public void testGetRegionPropertyValue() {
		MutableDouble time = new MutableDouble(0);
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().setTimeSupplier(()->time.getValue()).build();
		RegionDataManager rdm = new RegionDataManager(mockSimulationContext);

		Map<MultiKey, MutableInteger> expectedValues = new LinkedHashMap<>();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			rdm.addRegionId(testRegionId);
		}

		int runningValue = 0;
		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(runningValue).build();
			rdm.addRegionPropertyDefinition(testRegionPropertyId, propertyDefinition);
			for (TestRegionId testRegionId : TestRegionId.values()) {
				expectedValues.put(new MultiKey(testRegionId, testRegionPropertyId), new MutableInteger(runningValue));
			}
			runningValue++;
		}

		// show that the property values are currently what we expect
		for (RegionId regionId : rdm.getRegionIds()) {
			for (RegionPropertyId regionPropertyId : rdm.getRegionPropertyIds()) {
				Integer propertyValue = rdm.getRegionPropertyValue(regionId, regionPropertyId);
				MutableInteger mutableInteger = expectedValues.get(new MultiKey(regionId, regionPropertyId));
				assertEquals(mutableInteger.getValue(), propertyValue.intValue());
			}
		}

		// show that changes to the property values occur
		time.setValue(0);

		for (RegionId regionId : rdm.getRegionIds()) {
			for (RegionPropertyId regionPropertyId : rdm.getRegionPropertyIds()) {

				// move time forward and show that does not alter the
				// property values
				time.increment(0.01);
				

				// change the property value and show that property time is
				// correct
				rdm.setRegionPropertyValue(regionId, regionPropertyId, runningValue);
				Integer currentValue = rdm.getRegionPropertyValue(regionId, regionPropertyId);
				assertEquals(runningValue, currentValue.intValue());
				runningValue++;
			}
		}

		// precondition checks -- show all combinations of null or unknown
		// inputs generate a runtime exception
		RegionId unknownRegionId = TestRegionId.getUnknownRegionId();
		RegionId knownRegionId = TestRegionId.REGION_1;
		RegionPropertyId unknownRegionPropertyId = TestRegionPropertyId.getUnknownRegionPropertyId();
		RegionPropertyId knownRegionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1;

		assertThrows(RuntimeException.class, () -> rdm.getRegionPropertyValue(null, null));
		assertThrows(RuntimeException.class, () -> rdm.getRegionPropertyValue(null, unknownRegionPropertyId));
		assertThrows(RuntimeException.class, () -> rdm.getRegionPropertyValue(null, knownRegionPropertyId));
		assertThrows(RuntimeException.class, () -> rdm.getRegionPropertyValue(unknownRegionId, null));
		assertThrows(RuntimeException.class, () -> rdm.getRegionPropertyValue(unknownRegionId, unknownRegionPropertyId));
		assertThrows(RuntimeException.class, () -> rdm.getRegionPropertyValue(unknownRegionId, knownRegionPropertyId));
		assertThrows(RuntimeException.class, () -> rdm.getRegionPropertyValue(knownRegionId, null));
		assertThrows(RuntimeException.class, () -> rdm.getRegionPropertyValue(knownRegionId, unknownRegionPropertyId));

	}

	// setRegionPropertyValue(RegionId, RegionPropertyId, Object)

	@Test
	@UnitTestMethod(name = "setRegionPropertyValue", args = { RegionId.class, RegionPropertyId.class, Object.class })
	public void testSetRegionPropertyValue() {
		MutableDouble time = new MutableDouble(0);
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().setTimeSupplier(()->time.getValue()).build();
		RegionDataManager rdm = new RegionDataManager(mockSimulationContext);
		int runningValue = 0;
		Map<MultiKey, MutableInteger> expectedValues = new LinkedHashMap<>();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			rdm.addRegionId(testRegionId);
		}

		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			runningValue++;
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(runningValue).build();
			rdm.addRegionPropertyDefinition(testRegionPropertyId, propertyDefinition);
			for (TestRegionId testRegionId : TestRegionId.values()) {
				expectedValues.put(new MultiKey(testRegionId, testRegionPropertyId), new MutableInteger(runningValue));
			}

		}

		// show that the property values are currently what we expect
		for (RegionId regionId : rdm.getRegionIds()) {
			for (RegionPropertyId regionPropertyId : rdm.getRegionPropertyIds()) {
				Integer propertyValue = rdm.getRegionPropertyValue(regionId, regionPropertyId);
				MutableInteger mutableInteger = expectedValues.get(new MultiKey(regionId, regionPropertyId));
				assertEquals(mutableInteger.getValue(), propertyValue.intValue());
			}
		}

		// show that changes to the property values occur
		time.setValue(0);

		for (RegionId regionId : rdm.getRegionIds()) {
			for (RegionPropertyId regionPropertyId : rdm.getRegionPropertyIds()) {

				// move time forward and show that does not alter the
				// property values
				time.increment(0.01);

				// change the property value and show that property time is
				// correct
				rdm.setRegionPropertyValue(regionId, regionPropertyId, runningValue);
				Integer currentValue = rdm.getRegionPropertyValue(regionId, regionPropertyId);
				assertEquals(runningValue, currentValue.intValue());
				runningValue++;
			}
		}

		// precondition checks -- show all combinations of null or unknown
		// inputs generate a runtime exception
		RegionId unknownRegionId = TestRegionId.getUnknownRegionId();
		RegionId knownRegionId = TestRegionId.REGION_1;
		RegionPropertyId unknownRegionPropertyId = TestRegionPropertyId.getUnknownRegionPropertyId();
		RegionPropertyId knownRegionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1;

		assertThrows(RuntimeException.class, () -> rdm.setRegionPropertyValue(null, null, 1000));
		assertThrows(RuntimeException.class, () -> rdm.setRegionPropertyValue(null, unknownRegionPropertyId, 1000));
		assertThrows(RuntimeException.class, () -> rdm.setRegionPropertyValue(null, knownRegionPropertyId, 1000));
		assertThrows(RuntimeException.class, () -> rdm.setRegionPropertyValue(unknownRegionId, null, 1000));
		assertThrows(RuntimeException.class, () -> rdm.setRegionPropertyValue(unknownRegionId, unknownRegionPropertyId, 1000));
		assertThrows(RuntimeException.class, () -> rdm.setRegionPropertyValue(unknownRegionId, knownRegionPropertyId, 1000));
		assertThrows(RuntimeException.class, () -> rdm.setRegionPropertyValue(knownRegionId, null, 1000));
		assertThrows(RuntimeException.class, () -> rdm.setRegionPropertyValue(knownRegionId, unknownRegionPropertyId, 1000));

	}

}
