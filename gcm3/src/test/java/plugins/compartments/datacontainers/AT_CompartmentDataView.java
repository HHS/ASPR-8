package plugins.compartments.datacontainers;

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

import nucleus.NucleusError;
import nucleus.testsupport.MockContext;
import plugins.compartments.support.CompartmentError;
import plugins.compartments.support.CompartmentId;
import plugins.compartments.support.CompartmentPropertyId;
import plugins.compartments.testsupport.TestCompartmentId;
import plugins.compartments.testsupport.TestCompartmentPropertyId;
import plugins.properties.support.PropertyDefinition;
import util.ContractException;
import util.MultiKey;
import util.MutableDouble;
import util.MutableInteger;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = CompartmentDataView.class)
public class AT_CompartmentDataView {
	
	@Test
	@UnitTestConstructor(args = { Context.class, CompartmentDataManager.class })
	public void testConstructor() {
		// precondition tests
		MockContext mockContext = MockContext.builder().build();
		CompartmentDataManager compartmentDataManager = new CompartmentDataManager(mockContext);
		
		ContractException contractException = assertThrows(ContractException.class, ()->new CompartmentDataView(null, compartmentDataManager));
		assertEquals(NucleusError.NULL_CONTEXT, contractException.getErrorType());
		
		contractException = assertThrows(ContractException.class, ()->new CompartmentDataView(mockContext, null));
		assertEquals(CompartmentError.NULL_COMPARTMENT_DATA_MANAGER, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "compartmentIdExists", args = { CompartmentId.class })
	public void testCompartmentIdExists() {
		MockContext mockContext = MockContext.builder().build();
		CompartmentDataManager compartmentDataManager = new CompartmentDataManager(mockContext);
		CompartmentDataView compartmentDataView = new CompartmentDataView(mockContext, compartmentDataManager);

		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			compartmentDataManager.addCompartmentId(testCompartmentId);
		}
		// show that null compartment ids do not exist
		assertFalse(compartmentDataView.compartmentIdExists(null));

		// show that the compartment ids added do exist
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			assertTrue(compartmentDataView.compartmentIdExists(testCompartmentId));
		}

		// show that an unknown compartment id does not exist
		assertFalse(compartmentDataView.compartmentIdExists(TestCompartmentId.getUnknownCompartmentId()));
	}

	@Test
	@UnitTestMethod(name = "getCompartmentIds", args = {})
	public void testGetCompartmentIds() {
		MockContext mockContext = MockContext.builder().build();
		CompartmentDataManager compartmentDataManager = new CompartmentDataManager(mockContext);
		CompartmentDataView compartmentDataView = new CompartmentDataView(mockContext, compartmentDataManager);

		// show that the compartment ids that are added can be retrieved
		Set<CompartmentId> expectedCompartmentIds = new LinkedHashSet<>();
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			compartmentDataManager.addCompartmentId(testCompartmentId);
			expectedCompartmentIds.add(testCompartmentId);
		}
		assertEquals(expectedCompartmentIds, compartmentDataView.getCompartmentIds());
	}

	@Test
	@UnitTestMethod(name = "getCompartmentPropertyDefinition", args = { CompartmentId.class, CompartmentPropertyId.class })
	public void testGetCompartmentPropertyDefinition() {
		// collect the property definitions that we expect to find in the data
		// manager
		Set<MultiKey> expectedPropertyDefinitions = new LinkedHashSet<>();

		MockContext mockContext = MockContext.builder()
				.setContractErrorConsumer((c)->{throw new ContractException(c);})
				.setDetailedContractErrorConsumer((c,d)->{throw new ContractException(c);})
				.build();
		CompartmentDataManager compartmentDataManager = new CompartmentDataManager(mockContext);
		CompartmentDataView compartmentDataView = new CompartmentDataView(mockContext, compartmentDataManager);

		int defaultValue = 0;
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			compartmentDataManager.addCompartmentId(testCompartmentId);
			
			for (CompartmentPropertyId testCompartmentPropertyId : TestCompartmentPropertyId.getTestCompartmentPropertyIds(testCompartmentId)) {
				PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(defaultValue++).build();
				compartmentDataManager.addCompartmentPropertyDefinition(testCompartmentId, testCompartmentPropertyId, propertyDefinition);
				expectedPropertyDefinitions.add(new MultiKey(testCompartmentId, testCompartmentPropertyId, propertyDefinition));
			}
		}

		// collect the property definitions that are actually present
		Set<MultiKey> actualPropertyDefinitions = new LinkedHashSet<>();
		for (CompartmentId compartmentId : compartmentDataView.getCompartmentIds()) {
			for (CompartmentPropertyId compartmentPropertyId : compartmentDataView.getCompartmentPropertyIds(compartmentId)) {
				PropertyDefinition propertyDefinition = compartmentDataView.getCompartmentPropertyDefinition(compartmentId, compartmentPropertyId);
				actualPropertyDefinitions.add(new MultiKey(compartmentId, compartmentPropertyId, propertyDefinition));
			}
		}

		assertEquals(expectedPropertyDefinitions, actualPropertyDefinitions);

		// precondition checks

		CompartmentPropertyId compartmentPropertyId = TestCompartmentPropertyId.COMPARTMENT_PROPERTY_1_1;

		// if the compartment id is null
		ContractException contractException = assertThrows(ContractException.class, () -> compartmentDataView.getCompartmentPropertyDefinition(null, compartmentPropertyId));
		assertEquals(CompartmentError.NULL_COMPARTMENT_ID, contractException.getErrorType());

		// if the compartment id is unknown
		contractException = assertThrows(ContractException.class, () -> compartmentDataView.getCompartmentPropertyDefinition(TestCompartmentId.getUnknownCompartmentId(), compartmentPropertyId));
		assertEquals(CompartmentError.UNKNOWN_COMPARTMENT_ID, contractException.getErrorType());

		// if the compartment property id is null
		contractException = assertThrows(ContractException.class, () -> compartmentDataView.getCompartmentPropertyDefinition(TestCompartmentId.COMPARTMENT_1, null));
		assertEquals(CompartmentError.NULL_COMPARTMENT_PROPERTY_ID, contractException.getErrorType());

		// if the compartment property id is unknown
		contractException = assertThrows(ContractException.class,
				() -> compartmentDataView.getCompartmentPropertyDefinition(TestCompartmentId.COMPARTMENT_1, TestCompartmentPropertyId.getUnknownCompartmentPropertyId()));
		assertEquals(CompartmentError.UNKNOWN_COMPARTMENT_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "getCompartmentPropertyIds", args = { CompartmentId.class })
	public void testGetCompartmentPropertyIds() {
		// collect the property definitions that we expect to find in the data
		// manager
		Set<MultiKey> expectedPropertyDefinitions = new LinkedHashSet<>();
		MockContext mockContext = MockContext.builder()
				.setContractErrorConsumer((c)->{throw new ContractException(c);})
				.setDetailedContractErrorConsumer((c,d)->{throw new ContractException(c);})
				.build();
		CompartmentDataManager compartmentDataManager = new CompartmentDataManager(mockContext);
		CompartmentDataView compartmentDataView = new CompartmentDataView(mockContext, compartmentDataManager);

		int defaultValue = 0;
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			compartmentDataManager.addCompartmentId(testCompartmentId);

			for (CompartmentPropertyId testCompartmentPropertyId : TestCompartmentPropertyId.getTestCompartmentPropertyIds(testCompartmentId)) {
				PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(defaultValue++).build();
				compartmentDataManager.addCompartmentPropertyDefinition(testCompartmentId, testCompartmentPropertyId, propertyDefinition);
				expectedPropertyDefinitions.add(new MultiKey(testCompartmentId, testCompartmentPropertyId));
			}
		}

		// collect the property definitions that are actually present
		Set<MultiKey> actualPropertyDefinitions = new LinkedHashSet<>();
		for (CompartmentId compartmentId : compartmentDataView.getCompartmentIds()) {
			for (CompartmentPropertyId compartmentPropertyId : compartmentDataView.getCompartmentPropertyIds(compartmentId)) {
				actualPropertyDefinitions.add(new MultiKey(compartmentId, compartmentPropertyId));
			}
		}

		assertEquals(expectedPropertyDefinitions, actualPropertyDefinitions);

		// precondition tests

		// if the compartment id is null
		ContractException contractException = assertThrows(ContractException.class, () -> compartmentDataView.getCompartmentPropertyIds(null));
		assertEquals(CompartmentError.NULL_COMPARTMENT_ID, contractException.getErrorType());

		// if the compartment id is not known
		contractException = assertThrows(ContractException.class, () -> compartmentDataView.getCompartmentPropertyIds(TestCompartmentId.getUnknownCompartmentId()));
		assertEquals(CompartmentError.UNKNOWN_COMPARTMENT_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getCompartmentPropertyValue", args = { CompartmentId.class, CompartmentPropertyId.class })
	public void testGetCompartmentPropertyValue() {
		MutableDouble time = new MutableDouble(0);	
		MockContext mockContext = MockContext.builder()
				.setContractErrorConsumer((c)->{throw new ContractException(c);})
				.setDetailedContractErrorConsumer((c,d)->{throw new ContractException(c);})
				.setTimeSupplier(()->time.getValue()).build();
		CompartmentDataManager cdm = new CompartmentDataManager(mockContext);
		CompartmentDataView compartmentDataView = new CompartmentDataView(mockContext, cdm);

		int runningValue = 0;
		Map<MultiKey, MutableInteger> expectedValues = new LinkedHashMap<>();
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			cdm.addCompartmentId(testCompartmentId);
			
			for (CompartmentPropertyId testCompartmentPropertyId : TestCompartmentPropertyId.getTestCompartmentPropertyIds(testCompartmentId)) {
				runningValue++;
				PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(runningValue).build();
				cdm.addCompartmentPropertyDefinition(testCompartmentId, testCompartmentPropertyId, propertyDefinition);
				expectedValues.put(new MultiKey(testCompartmentId, testCompartmentPropertyId), new MutableInteger(runningValue));
			}
		}

		// show that the property values are currently what we expect
		for (CompartmentId compartmentId : compartmentDataView.getCompartmentIds()) {
			for (CompartmentPropertyId compartmentPropertyId : compartmentDataView.getCompartmentPropertyIds(compartmentId)) {
				Integer propertyValue = compartmentDataView.getCompartmentPropertyValue(compartmentId, compartmentPropertyId);
				MutableInteger mutableInteger = expectedValues.get(new MultiKey(compartmentId, compartmentPropertyId));
				assertEquals(mutableInteger.getValue(), propertyValue.intValue());
			}
		}

		// show that changes to the property values occur
		time.setValue(0);

		for (CompartmentId compartmentId : compartmentDataView.getCompartmentIds()) {
			for (CompartmentPropertyId compartmentPropertyId : compartmentDataView.getCompartmentPropertyIds(compartmentId)) {

				// move time forward and show that does not alter the
				// property values
				time.increment(0.01);

				// change the property value and show that property time is
				// correct
				cdm.setCompartmentPropertyValue(compartmentId, compartmentPropertyId, runningValue);
				Integer currentValue = compartmentDataView.getCompartmentPropertyValue(compartmentId, compartmentPropertyId);
				assertEquals(runningValue, currentValue.intValue());
				runningValue++;
			}
		}

		// precondition checks

		CompartmentId unknownCompartmentId = TestCompartmentId.getUnknownCompartmentId();
		CompartmentId knownCompartmentId = TestCompartmentId.COMPARTMENT_1;
		CompartmentPropertyId unknownCompartmentPropertyId = TestCompartmentPropertyId.getUnknownCompartmentPropertyId();
		CompartmentPropertyId knownCompartmentPropertyId = TestCompartmentPropertyId.COMPARTMENT_PROPERTY_1_1;

		// if the compartment id is null
		ContractException contractException = assertThrows(ContractException.class, () -> compartmentDataView.getCompartmentPropertyValue(null, knownCompartmentPropertyId));
		assertEquals(CompartmentError.NULL_COMPARTMENT_ID, contractException.getErrorType());

		// if the compartment id is not known
		contractException = assertThrows(ContractException.class, () -> compartmentDataView.getCompartmentPropertyValue(unknownCompartmentId, knownCompartmentPropertyId));
		assertEquals(CompartmentError.UNKNOWN_COMPARTMENT_ID, contractException.getErrorType());

		// if the compartment property id is null
		contractException = assertThrows(ContractException.class, () -> compartmentDataView.getCompartmentPropertyValue(knownCompartmentId, null));
		assertEquals(CompartmentError.NULL_COMPARTMENT_PROPERTY_ID, contractException.getErrorType());

		// if the compartment property id is not associated with the compartment
		contractException = assertThrows(ContractException.class, () -> compartmentDataView.getCompartmentPropertyValue(knownCompartmentId, unknownCompartmentPropertyId));
		assertEquals(CompartmentError.UNKNOWN_COMPARTMENT_PROPERTY_ID, contractException.getErrorType());

	}

	@Test	
	@UnitTestMethod(name = "getCompartmentPropertyTime", args = { CompartmentId.class, CompartmentPropertyId.class })
	public void testGetCompartmentPropertyTime() {

		MutableDouble time = new MutableDouble(0);
		MockContext mockContext = MockContext.builder()
				.setContractErrorConsumer((c)->{throw new ContractException(c);})
				.setDetailedContractErrorConsumer((c,d)->{throw new ContractException(c);})
				.setTimeSupplier(()->time.getValue()).build();
		CompartmentDataManager cdm = new CompartmentDataManager(mockContext);
		CompartmentDataView compartmentDataView = new CompartmentDataView(mockContext, cdm);

		int defaultValue = 0;
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			cdm.addCompartmentId(testCompartmentId);
			
			for (CompartmentPropertyId testCompartmentPropertyId : TestCompartmentPropertyId.getTestCompartmentPropertyIds(testCompartmentId)) {
				PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(defaultValue++).build();
				cdm.addCompartmentPropertyDefinition(testCompartmentId, testCompartmentPropertyId, propertyDefinition);
			}
		}

		// show that the property times are currently zero
		for (CompartmentId compartmentId : compartmentDataView.getCompartmentIds()) {
			for (CompartmentPropertyId compartmentPropertyId : compartmentDataView.getCompartmentPropertyIds(compartmentId)) {
				double compartmentPropertyTime = compartmentDataView.getCompartmentPropertyTime(compartmentId, compartmentPropertyId);
				assertEquals(0, compartmentPropertyTime, 0);
			}
		}

		// show that changes to the property values properly reflect the time in
		// the mock context
		time.setValue(0);
		int newPropertyValue = 100;

		for (CompartmentId compartmentId : compartmentDataView.getCompartmentIds()) {
			for (CompartmentPropertyId compartmentPropertyId : compartmentDataView.getCompartmentPropertyIds(compartmentId)) {
				// get the current time for the property
				double previousCompartmentPropertyTime = compartmentDataView.getCompartmentPropertyTime(compartmentId, compartmentPropertyId);

				// move time forward and show that does not alter the
				// property time
				time.increment(0.01);
				double currentCompartmentPropertyTime = compartmentDataView.getCompartmentPropertyTime(compartmentId, compartmentPropertyId);
				assertEquals(previousCompartmentPropertyTime, currentCompartmentPropertyTime, 0);

				// change the property value and show that property time is
				// correct
				cdm.setCompartmentPropertyValue(compartmentId, compartmentPropertyId, newPropertyValue++);
				currentCompartmentPropertyTime = compartmentDataView.getCompartmentPropertyTime(compartmentId, compartmentPropertyId);
				assertEquals(mockContext.getTime(), currentCompartmentPropertyTime, 0);
			}
		}

		// precondition checks -- show all combinations of null or unknown
		// inputs generate a runtime exception
		CompartmentId unknownCompartmentId = TestCompartmentId.getUnknownCompartmentId();
		CompartmentId knownCompartmentId = TestCompartmentId.COMPARTMENT_1;
		CompartmentPropertyId unknownCompartmentPropertyId = TestCompartmentPropertyId.getUnknownCompartmentPropertyId();
		CompartmentPropertyId knownCompartmentPropertyId = TestCompartmentPropertyId.COMPARTMENT_PROPERTY_1_1;

		// if the compartment id is null
		ContractException contractException = assertThrows(ContractException.class, () -> compartmentDataView.getCompartmentPropertyTime(null, knownCompartmentPropertyId));
		assertEquals(CompartmentError.NULL_COMPARTMENT_ID, contractException.getErrorType());

		// if the compartment id is not known
		contractException = assertThrows(ContractException.class, () -> compartmentDataView.getCompartmentPropertyTime(unknownCompartmentId, knownCompartmentPropertyId));
		assertEquals(CompartmentError.UNKNOWN_COMPARTMENT_ID, contractException.getErrorType());

		// if the compartment property id is null
		contractException = assertThrows(ContractException.class, () -> compartmentDataView.getCompartmentPropertyTime(knownCompartmentId, null));
		assertEquals(CompartmentError.NULL_COMPARTMENT_PROPERTY_ID, contractException.getErrorType());

		// if the compartment property id is not associated with the compartment
		contractException = assertThrows(ContractException.class, () -> compartmentDataView.getCompartmentPropertyTime(knownCompartmentId, unknownCompartmentPropertyId));
		assertEquals(CompartmentError.UNKNOWN_COMPARTMENT_PROPERTY_ID, contractException.getErrorType());

	}
}
