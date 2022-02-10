package plugins.regions.datacontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;

import org.junit.jupiter.api.Test;

import nucleus.testsupport.MockSimulationContext;
import plugins.properties.support.PropertyDefinition;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.regions.testsupport.TestRegionId;
import plugins.regions.testsupport.TestRegionPropertyId;
import util.ContractException;
import util.MultiKey;
import util.MutableDouble;
import util.MutableInteger;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = RegionDataView.class)
public class AT_RegionDataView {
	
	
	@Test
	@UnitTestConstructor(args = { Context.class, RegionDataManager.class })
	public void testConstructor() {
		// this test is covered by the remaining tests
	}

	@Test
	@UnitTestMethod(name = "regionIdExists", args = { RegionId.class })
	public void testRegionIdExists() {
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		RegionDataManager regionDataManager = new RegionDataManager(mockSimulationContext);
		RegionDataView regionDataView = new RegionDataView(mockSimulationContext, regionDataManager);

		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionDataManager.addRegionId(testRegionId);
		}
		// show that null region ids do not exist
		assertFalse(regionDataView.regionIdExists(null));

		// show that the region ids added do exist
		for (TestRegionId testRegionId : TestRegionId.values()) {
			assertTrue(regionDataView.regionIdExists(testRegionId));
		}

		// show that an unknown region id does not exist
		assertFalse(regionDataView.regionIdExists(TestRegionId.getUnknownRegionId()));
	}

	@Test
	@UnitTestMethod(name = "getRegionIds", args = {})
	public void testGetRegionIds() {
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		RegionDataManager regionDataManager = new RegionDataManager(mockSimulationContext);
		RegionDataView regionDataView = new RegionDataView(mockSimulationContext, regionDataManager);

		// show that the region ids that are added can be retrieved
		Set<RegionId> expectedRegionIds = new LinkedHashSet<>();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionDataManager.addRegionId(testRegionId);
			expectedRegionIds.add(testRegionId);
		}
		assertEquals(expectedRegionIds, regionDataView.getRegionIds());
	}

	@Test
	@UnitTestMethod(name = "getRegionPropertyDefinition", args = { RegionPropertyId.class })
	public void testGetRegionPropertyDefinition() {
		// collect the property definitions that we expect to find in the data
		// manager
		Set<MultiKey> expectedPropertyDefinitions = new LinkedHashSet<>();

		
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder()
				.setContractErrorConsumer((c)->{throw new ContractException(c);})
				.setDetailedContractErrorConsumer((c,d)->{throw new ContractException(c);})
				.build();
		RegionDataManager regionDataManager = new RegionDataManager(mockSimulationContext);
		RegionDataView regionDataView = new RegionDataView(mockSimulationContext, regionDataManager);

		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionDataManager.addRegionId(testRegionId);
		}

		int defaultValue = 0;
		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(defaultValue++).build();
			regionDataManager.addRegionPropertyDefinition(testRegionPropertyId, propertyDefinition);
			expectedPropertyDefinitions.add(new MultiKey(testRegionPropertyId, propertyDefinition));
		}

		// collect the property definitions that are actually present
		// Set<MultiKey> actualPropertyDefinitions = new LinkedHashSet<>();
		Set<MultiKey> actualPropertyDefinitions = new LinkedHashSet<>();
		for (RegionPropertyId regionPropertyId : regionDataView.getRegionPropertyIds()) {
			PropertyDefinition propertyDefinition = regionDataView.getRegionPropertyDefinition(regionPropertyId);
			actualPropertyDefinitions.add(new MultiKey(regionPropertyId, propertyDefinition));
		}

		assertEquals(expectedPropertyDefinitions, actualPropertyDefinitions);

		// precondition checks

		// if the region property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> regionDataView.getRegionPropertyDefinition(null));
		assertEquals(RegionError.NULL_REGION_PROPERTY_ID, contractException.getErrorType());

		// if the region property id is unknown
		contractException = assertThrows(ContractException.class, () -> regionDataView.getRegionPropertyDefinition(TestRegionPropertyId.getUnknownRegionPropertyId()));
		assertEquals(RegionError.UNKNOWN_REGION_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "getRegionPropertyIds", args = {})
	public void testGetRegionPropertyIds() {
		// collect the property definitions that we expect to find in the data
		// manager
		Set<MultiKey> expectedPropertyDefinitions = new LinkedHashSet<>();
		
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder().build();
		RegionDataManager regionDataManager = new RegionDataManager(mockSimulationContext);
		RegionDataView regionDataView = new RegionDataView(mockSimulationContext, regionDataManager);

		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionDataManager.addRegionId(testRegionId);
		}
		int defaultValue = 0;
		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(defaultValue++).build();
			regionDataManager.addRegionPropertyDefinition(testRegionPropertyId, propertyDefinition);
			expectedPropertyDefinitions.add(new MultiKey(testRegionPropertyId, propertyDefinition));
		}

		// collect the property definitions that are actually present
		Set<MultiKey> actualPropertyDefinitions = new LinkedHashSet<>();

		for (RegionPropertyId regionPropertyId : regionDataView.getRegionPropertyIds()) {
			PropertyDefinition regionPropertyDefinition = regionDataView.getRegionPropertyDefinition(regionPropertyId);
			actualPropertyDefinitions.add(new MultiKey(regionPropertyId, regionPropertyDefinition));
		}

		assertEquals(expectedPropertyDefinitions, actualPropertyDefinitions);

		// no precondition tests

	}

	@Test
	@UnitTestMethod(name = "getRegionPropertyValue", args = { RegionId.class, RegionPropertyId.class })
	public void testGetRegionPropertyValue() {
		MutableDouble time = new MutableDouble(0);
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder()
				.setContractErrorConsumer((c)->{throw new ContractException(c);})
				.setDetailedContractErrorConsumer((c,d)->{throw new ContractException(c);})
				.setTimeSupplier(()->time.getValue()).build();
		RegionDataManager rdm = new RegionDataManager(mockSimulationContext);
		RegionDataView regionDataView = new RegionDataView(mockSimulationContext, rdm);

		Map<MultiKey, MutableInteger> expectedValues = new LinkedHashMap<>();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			rdm.addRegionId(testRegionId);
		}

		int runningValue = 0;

		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			runningValue++;
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(runningValue).build();
			rdm.addRegionPropertyDefinition(testRegionPropertyId, propertyDefinition);
			for (TestRegionId testRegionId : TestRegionId.values()) {
				expectedValues.put(new MultiKey(testRegionId, testRegionPropertyId), new MutableInteger(runningValue));
			}
		}

		// show that the property values are currently what we expect
		for (RegionId regionId : regionDataView.getRegionIds()) {
			for (RegionPropertyId regionPropertyId : regionDataView.getRegionPropertyIds()) {
				Integer propertyValue = regionDataView.getRegionPropertyValue(regionId, regionPropertyId);
				MutableInteger mutableInteger = expectedValues.get(new MultiKey(regionId, regionPropertyId));
				assertEquals(mutableInteger.getValue(), propertyValue.intValue());
			}
		}

		// show that changes to the property values occur
		time.setValue(0);

		for (RegionId regionId : regionDataView.getRegionIds()) {
			for (RegionPropertyId regionPropertyId : regionDataView.getRegionPropertyIds()) {

				// move time forward and show that does not alter the
				// property values
				time.increment(0.01);
		

				// change the property value and show that property time is
				// correct
				rdm.setRegionPropertyValue(regionId, regionPropertyId, runningValue);
				Integer currentValue = regionDataView.getRegionPropertyValue(regionId, regionPropertyId);
				assertEquals(runningValue, currentValue.intValue());
				runningValue++;
			}
		}

		// precondition checks

		RegionId unknownRegionId = TestRegionId.getUnknownRegionId();
		RegionId knownRegionId = TestRegionId.REGION_1;
		RegionPropertyId unknownRegionPropertyId = TestRegionPropertyId.getUnknownRegionPropertyId();
		RegionPropertyId knownRegionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1;

		// if the region id is null
		ContractException contractException = assertThrows(ContractException.class, () -> regionDataView.getRegionPropertyValue(null, knownRegionPropertyId));
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		// if the region id is not known
		contractException = assertThrows(ContractException.class, () -> regionDataView.getRegionPropertyValue(unknownRegionId, knownRegionPropertyId));
		assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		// if the region property id is null
		contractException = assertThrows(ContractException.class, () -> regionDataView.getRegionPropertyValue(knownRegionId, null));
		assertEquals(RegionError.NULL_REGION_PROPERTY_ID, contractException.getErrorType());

		// if the region property id is not associated with the region
		contractException = assertThrows(ContractException.class, () -> regionDataView.getRegionPropertyValue(knownRegionId, unknownRegionPropertyId));
		assertEquals(RegionError.UNKNOWN_REGION_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getRegionPropertyTime", args = { RegionId.class, RegionPropertyId.class })
	public void testGetRegionPropertyTime() {

		MutableDouble time = new MutableDouble(0);
		MockSimulationContext mockSimulationContext = MockSimulationContext.builder()
				.setContractErrorConsumer((c)->{throw new ContractException(c);})
				.setDetailedContractErrorConsumer((c,d)->{throw new ContractException(c);})
				.setTimeSupplier(()->time.getValue()).build();
		RegionDataManager rdm = new RegionDataManager(mockSimulationContext);
		RegionDataView regionDataView = new RegionDataView(mockSimulationContext, rdm);

		for (TestRegionId testRegionId : TestRegionId.values()) {
			rdm.addRegionId(testRegionId);
		}

		int defaultValue = 0;

		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(defaultValue++).build();
			rdm.addRegionPropertyDefinition(testRegionPropertyId, propertyDefinition);

		}
		// show that the property times are currently zero
		for (RegionId regionId : regionDataView.getRegionIds()) {
			for (RegionPropertyId regionPropertyId : regionDataView.getRegionPropertyIds()) {
				double regionPropertyTime = regionDataView.getRegionPropertyTime(regionId, regionPropertyId);
				assertEquals(0, regionPropertyTime, 0);
			}
		}

		// show that changes to the property values properly reflect the time in
		// the mock context
		time.setValue(0);
		int newPropertyValue = 100;

		for (RegionId regionId : regionDataView.getRegionIds()) {
			for (RegionPropertyId regionPropertyId : regionDataView.getRegionPropertyIds()) {
				// get the current time for the property
				double previousRegionPropertyTime = regionDataView.getRegionPropertyTime(regionId, regionPropertyId);

				// move time forward and show that does not alter the
				// property time
				time.increment(0.01);
				double currentRegionPropertyTime = regionDataView.getRegionPropertyTime(regionId, regionPropertyId);
				assertEquals(previousRegionPropertyTime, currentRegionPropertyTime, 0);

				// change the property value and show that property time is
				// correct
				rdm.setRegionPropertyValue(regionId, regionPropertyId, newPropertyValue++);
				currentRegionPropertyTime = regionDataView.getRegionPropertyTime(regionId, regionPropertyId);
				assertEquals(mockSimulationContext.getTime(), currentRegionPropertyTime, 0);
			}
		}

		// precondition checks -- show all combinations of null or unknown
		// inputs generate a runtime exception
		RegionId unknownRegionId = TestRegionId.getUnknownRegionId();
		RegionId knownRegionId = TestRegionId.REGION_1;
		RegionPropertyId unknownRegionPropertyId = TestRegionPropertyId.getUnknownRegionPropertyId();
		RegionPropertyId knownRegionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1;

		// if the region id is null
		ContractException contractException = assertThrows(ContractException.class, () -> regionDataView.getRegionPropertyTime(null, knownRegionPropertyId));
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		// if the region id is not known
		contractException = assertThrows(ContractException.class, () -> regionDataView.getRegionPropertyTime(unknownRegionId, knownRegionPropertyId));
		assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

		// if the region property id is null
		contractException = assertThrows(ContractException.class, () -> regionDataView.getRegionPropertyTime(knownRegionId, null));
		assertEquals(RegionError.NULL_REGION_PROPERTY_ID, contractException.getErrorType());

		// if the region property id is not associated with the region
		contractException = assertThrows(ContractException.class, () -> regionDataView.getRegionPropertyTime(knownRegionId, unknownRegionPropertyId));
		assertEquals(RegionError.UNKNOWN_REGION_PROPERTY_ID, contractException.getErrorType());

	}
}
