package plugins.compartments.initialdata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import nucleus.AgentContext;
import plugins.compartments.initialdata.CompartmentInitialData.Builder;
import plugins.compartments.support.CompartmentError;
import plugins.compartments.support.CompartmentId;
import plugins.compartments.support.CompartmentPropertyId;
import plugins.compartments.testsupport.TestCompartmentId;
import plugins.compartments.testsupport.TestCompartmentPropertyId;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.PropertyError;
import plugins.properties.support.TimeTrackingPolicy;
import util.ContractException;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

/**
 * Test unit for {@linkplain CompartmentInitialData}. Tests for
 * CompartmentInitialData.Builder are limited to precondition tests and are
 * otherwise covered via the class level tests. Note that the builder does not
 * impose any ordering on the invocation of its methods and many validation
 * tests are deferred to the build invocation.
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = CompartmentInitialData.class)
public class AT_CompartmentInitialData {

	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		// show that we can create a builder
		assertNotNull(CompartmentInitialData.builder());
	}

	@Test
	@UnitTestMethod(name = "getCompartmentIds", args = {})
	public void testGetCompartmentIds() {
		// use the test compartment ids
		Set<CompartmentId> expectedCompartmentIds = new LinkedHashSet<>();
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			expectedCompartmentIds.add(testCompartmentId);
		}

		// show that the compartments added are present
		Builder builder = CompartmentInitialData.builder();
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			builder.setCompartmentInitialBehaviorSupplier(testCompartmentId, () -> (c) -> {
			});
		}
		CompartmentInitialData compartmentInitialData = builder.build();

		Set<CompartmentId> actualCompartmentIds = compartmentInitialData.getCompartmentIds();
		assertEquals(expectedCompartmentIds, actualCompartmentIds);
	}

	@Test
	@UnitTestMethod(name = "getCompartmentInitialBehavior", args = { CompartmentId.class })
	public void testGetCompartmentInitialBehavior() {

		Builder builder = CompartmentInitialData.builder();
		/*
		 * Add consumers associated with compartments
		 */
		Map<CompartmentId, Consumer<AgentContext>> expectedConsumers = new LinkedHashMap<>();
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			Consumer<AgentContext> consumer = (c) -> {
			};
			expectedConsumers.put(testCompartmentId, consumer);
			Supplier<Consumer<AgentContext>> supplier = () -> consumer;
			builder.setCompartmentInitialBehaviorSupplier(testCompartmentId, supplier);
		}
		CompartmentInitialData compartmentInitialData = builder.build();

		/*
		 * Show that the consumers retrieved by compartment id were the ones
		 * that had been added to the builder
		 */

		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			Consumer<AgentContext> actualConsumer = compartmentInitialData.getCompartmentInitialBehavior(testCompartmentId);
			Consumer<AgentContext> expectedConsumer = expectedConsumers.get(testCompartmentId);
			assertEquals(expectedConsumer, actualConsumer);
		}

		// precondition tests

		// null compartment id
		ContractException contractException = assertThrows(ContractException.class, () -> compartmentInitialData.getCompartmentInitialBehavior(null));
		assertEquals(CompartmentError.NULL_COMPARTMENT_ID, contractException.getErrorType());

		// unknown compartment id
		contractException = assertThrows(ContractException.class, () -> compartmentInitialData.getCompartmentInitialBehavior(TestCompartmentId.getUnknownCompartmentId()));
		assertEquals(CompartmentError.UNKNOWN_COMPARTMENT_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getCompartmentPropertyDefinition", args = { CompartmentId.class, CompartmentPropertyId.class })
	public void testGetCompartmentPropertyDefinition() {
		Builder builder = CompartmentInitialData.builder();
		/*
		 * Place the various compartment/property pairs defined in
		 * TestCompartmentId into the builder and associate them with distinct
		 * property definitions. Each property definition will differ by its
		 * initial value.
		 */
		int defaultValue = 0;
		Map<CompartmentId, Map<CompartmentPropertyId, PropertyDefinition>> expectedDefinitions = new LinkedHashMap<>();
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			builder.setCompartmentInitialBehaviorSupplier(testCompartmentId, () -> (c) -> {
			});
			Map<CompartmentPropertyId, PropertyDefinition> map = new LinkedHashMap<>();
			expectedDefinitions.put(testCompartmentId, map);

			for (CompartmentPropertyId compartmentPropertyId : TestCompartmentPropertyId.getTestCompartmentPropertyIds(testCompartmentId)) {
				PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(defaultValue++).build();
				map.put(compartmentPropertyId, propertyDefinition);
				builder.defineCompartmentProperty(testCompartmentId, compartmentPropertyId, propertyDefinition);
			}
		}

		// build the compartment initial data
		CompartmentInitialData compartmentInitialData = builder.build();

		/*
		 * Retrieve all of the property defintions in the compartment inital
		 * data and place them in a map for comparison.
		 */
		Map<CompartmentId, Map<CompartmentPropertyId, PropertyDefinition>> actualDefinitions = new LinkedHashMap<>();
		for (CompartmentId compartmentId : compartmentInitialData.getCompartmentIds()) {
			Map<CompartmentPropertyId, PropertyDefinition> map = new LinkedHashMap<>();
			actualDefinitions.put(compartmentId, map);
			Set<CompartmentPropertyId> compartmentPropertyIds = compartmentInitialData.getCompartmentPropertyIds(compartmentId);
			for (CompartmentPropertyId compartmentPropertyId : compartmentPropertyIds) {
				PropertyDefinition propertyDefinition = compartmentInitialData.getCompartmentPropertyDefinition(compartmentId, compartmentPropertyId);
				map.put(compartmentPropertyId, propertyDefinition);
			}
		}

		// show that the two maps are equal
		assertEquals(expectedDefinitions, actualDefinitions);

		// precondition tests

		// create some valid inputs to help with the precondition tests
		CompartmentId validCompartmentId = TestCompartmentId.COMPARTMENT_1;
		CompartmentPropertyId validCompartmentPropertyId = TestCompartmentPropertyId.COMPARTMENT_PROPERTY_1_1;

		// if the compartment id is null
		ContractException contractException = assertThrows(ContractException.class, () -> compartmentInitialData.getCompartmentPropertyDefinition(null, validCompartmentPropertyId));
		assertEquals(CompartmentError.NULL_COMPARTMENT_ID, contractException.getErrorType());

		// if the compartment id is unknown
		contractException = assertThrows(ContractException.class,
				() -> compartmentInitialData.getCompartmentPropertyDefinition(TestCompartmentId.getUnknownCompartmentId(), validCompartmentPropertyId));
		assertEquals(CompartmentError.UNKNOWN_COMPARTMENT_ID, contractException.getErrorType());

		// if the compartment property id is null
		contractException = assertThrows(ContractException.class, () -> compartmentInitialData.getCompartmentPropertyDefinition(validCompartmentId, null));
		assertEquals(CompartmentError.NULL_COMPARTMENT_PROPERTY_ID, contractException.getErrorType());

		// if the compartment property id is unknown
		contractException = assertThrows(ContractException.class,
				() -> compartmentInitialData.getCompartmentPropertyDefinition(validCompartmentId, TestCompartmentPropertyId.getUnknownCompartmentPropertyId()));
		assertEquals(CompartmentError.UNKNOWN_COMPARTMENT_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getCompartmentPropertyIds", args = { CompartmentId.class })
	public void testGetCompartmentPropertyIds() {
		Builder builder = CompartmentInitialData.builder();
		/*
		 * Place the various compartment/property pairs defined in
		 * TestCompartmentId into the builder and associate them with distinct
		 * property definitions.
		 */

		Map<CompartmentId, Set<CompartmentPropertyId>> expectedPropertyIds = new LinkedHashMap<>();
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			builder.setCompartmentInitialBehaviorSupplier(testCompartmentId, () -> (c) -> {
			});
			Set<CompartmentPropertyId> set = new LinkedHashSet<>();
			expectedPropertyIds.put(testCompartmentId, set);

			for (CompartmentPropertyId compartmentPropertyId : TestCompartmentPropertyId.getTestCompartmentPropertyIds(testCompartmentId)) {

				PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(0).build();
				set.add(compartmentPropertyId);
				builder.defineCompartmentProperty(testCompartmentId, compartmentPropertyId, propertyDefinition);
			}
		}

		// build the compartment initial data
		CompartmentInitialData compartmentInitialData = builder.build();

		/*
		 * Retrieve all of the property defintions in the compartment inital
		 * data and place them in a map for comparison.
		 */
		Map<CompartmentId, Set<CompartmentPropertyId>> actualPropertyIds = new LinkedHashMap<>();
		for (CompartmentId compartmentId : compartmentInitialData.getCompartmentIds()) {
			Set<CompartmentPropertyId> compartmentPropertyIds = compartmentInitialData.getCompartmentPropertyIds(compartmentId);
			actualPropertyIds.put(compartmentId, compartmentPropertyIds);
		}

		// show that the two sets are equal
		assertEquals(expectedPropertyIds, actualPropertyIds);

		// precondition tests

		// if the compartment id is null
		ContractException contractException = assertThrows(ContractException.class, () -> compartmentInitialData.getCompartmentPropertyIds(null));
		assertEquals(CompartmentError.NULL_COMPARTMENT_ID, contractException.getErrorType());

		// if the compartment id is unknown
		contractException = assertThrows(ContractException.class, () -> compartmentInitialData.getCompartmentPropertyIds(TestCompartmentId.getUnknownCompartmentId()));
		assertEquals(CompartmentError.UNKNOWN_COMPARTMENT_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getCompartmentPropertyValue", args = { CompartmentId.class, CompartmentPropertyId.class })
	public void testGetCompartmentPropertyValue() {
		Builder builder = CompartmentInitialData.builder();
		/*
		 * Place the various compartment/property pairs defined in
		 * TestCompartmentId into the builder and associate them with distinct
		 * property values. Each property value will be unique.
		 */
		int propertyValue = 0;
		Map<CompartmentId, Map<CompartmentPropertyId, Object>> expectedPropertyValues = new LinkedHashMap<>();
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			builder.setCompartmentInitialBehaviorSupplier(testCompartmentId, () -> (c) -> {
			});
			Map<CompartmentPropertyId, Object> map = new LinkedHashMap<>();
			expectedPropertyValues.put(testCompartmentId, map);
			
			for (CompartmentPropertyId compartmentPropertyId : TestCompartmentPropertyId.getTestCompartmentPropertyIds(testCompartmentId)) {

				PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(0).build();
				map.put(compartmentPropertyId, propertyValue);
				builder.defineCompartmentProperty(testCompartmentId, compartmentPropertyId, propertyDefinition);
				builder.setCompartmentPropertyValue(testCompartmentId, compartmentPropertyId, propertyValue);
				propertyValue++;
			}
		}

		// build the compartment initial data
		CompartmentInitialData compartmentInitialData = builder.build();

		/*
		 * Retrieve all of the property values in the compartment inital data
		 * and place them in a map for comparison.
		 */
		Map<CompartmentId, Map<CompartmentPropertyId, Object>> actualPropertyValues = new LinkedHashMap<>();
		for (CompartmentId compartmentId : compartmentInitialData.getCompartmentIds()) {
			Map<CompartmentPropertyId, Object> map = new LinkedHashMap<>();
			actualPropertyValues.put(compartmentId, map);
			Set<CompartmentPropertyId> compartmentPropertyIds = compartmentInitialData.getCompartmentPropertyIds(compartmentId);
			for (CompartmentPropertyId compartmentPropertyId : compartmentPropertyIds) {
				Object compartmentPropertyValue = compartmentInitialData.getCompartmentPropertyValue(compartmentId, compartmentPropertyId);
				map.put(compartmentPropertyId, compartmentPropertyValue);
			}
		}

		// show that the two maps are equal
		assertEquals(expectedPropertyValues, actualPropertyValues);

		// precondition tests

		// create some valid inputs to help with the precondition tests
		CompartmentId validCompartmentId = TestCompartmentId.COMPARTMENT_1;
		CompartmentPropertyId validCompartmentPropertyId = TestCompartmentPropertyId.COMPARTMENT_PROPERTY_1_2;

		// if the compartment id is null
		ContractException contractException = assertThrows(ContractException.class, () -> compartmentInitialData.getCompartmentPropertyValue(null, validCompartmentPropertyId));
		assertEquals(CompartmentError.NULL_COMPARTMENT_ID, contractException.getErrorType());

		// if the compartment id is unknown
		contractException = assertThrows(ContractException.class, () -> compartmentInitialData.getCompartmentPropertyValue(TestCompartmentId.getUnknownCompartmentId(), validCompartmentPropertyId));
		assertEquals(CompartmentError.UNKNOWN_COMPARTMENT_ID, contractException.getErrorType());

		// if the compartment property id is null
		contractException = assertThrows(ContractException.class, () -> compartmentInitialData.getCompartmentPropertyValue(validCompartmentId, null));
		assertEquals(CompartmentError.NULL_COMPARTMENT_PROPERTY_ID, contractException.getErrorType());

		// if the compartment property id is unknown
		contractException = assertThrows(ContractException.class,
				() -> compartmentInitialData.getCompartmentPropertyValue(validCompartmentId, TestCompartmentPropertyId.getUnknownCompartmentPropertyId()));
		assertEquals(CompartmentError.UNKNOWN_COMPARTMENT_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "getPersonCompartment", args = { PersonId.class })
	public void testGetPersonCompartment() {
		Builder builder = CompartmentInitialData.builder();

		/*
		 * Add the compartments
		 */
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			builder.setCompartmentInitialBehaviorSupplier(testCompartmentId, () -> (c) -> {
			});
		}

		/*
		 * Place people in compartments
		 */
		Map<PersonId, CompartmentId> expectedCompartmentAssignments = new LinkedHashMap<>();

		TestCompartmentId testCompartmentId = TestCompartmentId.COMPARTMENT_1;
		for (int i = 0; i < 100; i++) {
			PersonId personId = new PersonId(i);
			testCompartmentId = testCompartmentId.next();
			expectedCompartmentAssignments.put(personId, testCompartmentId);
			builder.setPersonCompartment(personId, testCompartmentId);
		}

		// build the compartment initial data
		CompartmentInitialData compartmentInitialData = builder.build();

		/*
		 * Retrieve the people and their compartments for comparison
		 */
		Map<PersonId, CompartmentId> actualCompartmentAssignments = new LinkedHashMap<>();
		for (PersonId personId : compartmentInitialData.getPersonIds()) {
			CompartmentId compartment = compartmentInitialData.getPersonCompartment(personId);
			actualCompartmentAssignments.put(personId, compartment);
		}

		// show that the two maps are equal
		assertEquals(expectedCompartmentAssignments, actualCompartmentAssignments);

		// precondition tests

		// if the person id is null
		ContractException contractException = assertThrows(ContractException.class, () -> compartmentInitialData.getPersonCompartment(null));
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		// if the person id is unknown
		contractException = assertThrows(ContractException.class, () -> compartmentInitialData.getPersonCompartment(new PersonId(10000)));
		assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "getPersonCompartmentArrivalTrackingPolicy", args = {})
	public void testGetPersonCompartmentArrivalTrackingPolicy() {

		Builder builder = CompartmentInitialData.builder();

		for (TimeTrackingPolicy timeTrackingPolicy : TimeTrackingPolicy.values()) {
			builder.setPersonCompartmentArrivalTracking(timeTrackingPolicy);
			CompartmentInitialData compartmentInitialData = builder.build();
			assertEquals(timeTrackingPolicy, compartmentInitialData.getPersonCompartmentArrivalTrackingPolicy());
		}
	}

	@Test
	@UnitTestMethod(name = "getPersonIds", args = {})
	public void testGetPersonIds() {
		Set<PersonId> expectedPersonIds = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			PersonId personId = new PersonId(i * i + 7);
			expectedPersonIds.add(personId);
		}

		Builder builder = CompartmentInitialData.builder();
		builder.setCompartmentInitialBehaviorSupplier(TestCompartmentId.COMPARTMENT_1, () -> (c) -> {
		});
		for (PersonId personId : expectedPersonIds) {
			builder.setPersonCompartment(personId, TestCompartmentId.COMPARTMENT_1);
		}
		CompartmentInitialData compartmentInitialData = builder.build();
		Set<PersonId> actualPersonIds = compartmentInitialData.getPersonIds();
		assertEquals(expectedPersonIds, actualPersonIds);

	}

	@Test
	@UnitTestMethod(target = CompartmentInitialData.Builder.class, name = "build", args = {})
	public void testBuild() {
		Builder builder = CompartmentInitialData.builder();
		// show the builder does not return null
		assertNotNull(builder.build());

		// precondition tests

		/*
		 * if a person was associated with a compartment id that was not
		 * properly added with an initial agent behavior.
		 */
		ContractException contractException = assertThrows(ContractException.class, () -> builder.setPersonCompartment(new PersonId(8), TestCompartmentId.COMPARTMENT_1).build());
		assertEquals(CompartmentError.UNKNOWN_COMPARTMENT_ID, contractException.getErrorType());

		/*
		 * if a compartment property definition was associated with a
		 * compartment id that was not properly added with an initial agent
		 * behavior.
		 */
		CompartmentPropertyId compartmentPropertyId = TestCompartmentPropertyId.COMPARTMENT_PROPERTY_1_2;
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setDefaultValue(0).setType(Integer.class).build();
		builder.defineCompartmentProperty(TestCompartmentId.COMPARTMENT_1, compartmentPropertyId, propertyDefinition);
		contractException = assertThrows(ContractException.class, () -> builder.build());
		assertEquals(CompartmentError.UNKNOWN_COMPARTMENT_ID, contractException.getErrorType());

		/*
		 * if a compartment property value was associated with a compartment id
		 * that was not properly added with an initial agent behavior.
		 */
		builder.defineCompartmentProperty(TestCompartmentId.COMPARTMENT_1, compartmentPropertyId, propertyDefinition);
		builder.setCompartmentPropertyValue(TestCompartmentId.COMPARTMENT_1, compartmentPropertyId, 5);
		contractException = assertThrows(ContractException.class, () -> builder.build());
		assertEquals(CompartmentError.UNKNOWN_COMPARTMENT_ID, contractException.getErrorType());

		/*
		 * if a compartment property value was associated with a compartment
		 * property id that was not defined
		 */
		builder.setCompartmentInitialBehaviorSupplier(TestCompartmentId.COMPARTMENT_1, () -> (c) -> {
		});
		builder.setCompartmentPropertyValue(TestCompartmentId.COMPARTMENT_1, compartmentPropertyId, 5);
		contractException = assertThrows(ContractException.class, () -> builder.build());
		assertEquals(CompartmentError.UNKNOWN_COMPARTMENT_PROPERTY_ID, contractException.getErrorType());

		/*
		 * if a compartment property value was associated with a compartment and
		 * compartment property id that is incompatible with the corresponding
		 * property definition.
		 */
		builder.setCompartmentInitialBehaviorSupplier(TestCompartmentId.COMPARTMENT_1, () -> (c) -> {
		});
		builder.defineCompartmentProperty(TestCompartmentId.COMPARTMENT_1, compartmentPropertyId, propertyDefinition);
		builder.setCompartmentPropertyValue(TestCompartmentId.COMPARTMENT_1, compartmentPropertyId, "invalid value");
		contractException = assertThrows(ContractException.class, () -> builder.build());
		assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

		/*
		 * if a compartment property definition does not have a default value
		 * and there are no property values added to replace that default.
		 */
		builder.setCompartmentInitialBehaviorSupplier(TestCompartmentId.COMPARTMENT_1, () -> (c) -> {
		});
		propertyDefinition = PropertyDefinition.builder().setType(Double.class).build();
		builder.defineCompartmentProperty(TestCompartmentId.COMPARTMENT_1, compartmentPropertyId, propertyDefinition);
		contractException = assertThrows(ContractException.class, () -> builder.build());
		assertEquals(CompartmentError.INSUFFICIENT_COMPARTMENT_PROPERTY_VALUE_ASSIGNMENT, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = CompartmentInitialData.Builder.class, name = "defineCompartmentProperty", args = { CompartmentId.class, CompartmentPropertyId.class, PropertyDefinition.class })
	public void testDefineCompartmentProperty() {
		Builder builder = CompartmentInitialData.builder();

		TestCompartmentId compartmentId = TestCompartmentId.COMPARTMENT_1;
		TestCompartmentPropertyId compartmentPropertyId = TestCompartmentPropertyId.COMPARTMENT_PROPERTY_1_1;
		PropertyDefinition propertyDefinition = compartmentPropertyId.getPropertyDefinition();

		// if the compartment id is null
		ContractException contractException = assertThrows(ContractException.class, () -> builder.defineCompartmentProperty(null, compartmentPropertyId, propertyDefinition));
		assertEquals(CompartmentError.NULL_COMPARTMENT_ID, contractException.getErrorType());

		// if the compartment property id is null
		contractException = assertThrows(ContractException.class, () -> builder.defineCompartmentProperty(compartmentId, null, propertyDefinition));
		assertEquals(CompartmentError.NULL_COMPARTMENT_PROPERTY_ID, contractException.getErrorType());

		// if the property definition is null
		contractException = assertThrows(ContractException.class, () -> builder.defineCompartmentProperty(compartmentId, compartmentPropertyId, null));
		assertEquals(CompartmentError.NULL_COMPARTMENT_PROPERTY_DEFINITION, contractException.getErrorType());

		/*
		 * if a property definition for the given compartment id and property id
		 * was previously defined.
		 */
		builder.defineCompartmentProperty(compartmentId, compartmentPropertyId, propertyDefinition);
		contractException = assertThrows(ContractException.class, () -> builder.defineCompartmentProperty(compartmentId, compartmentPropertyId, propertyDefinition));
		assertEquals(CompartmentError.DUPLICATE_COMPARTMENT_PROPERTY_DEFINITION_ASSIGNMENT, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = CompartmentInitialData.Builder.class, name = "setCompartmentInitialBehaviorSupplier", args = { CompartmentId.class, CompartmentPropertyId.class,
			PropertyDefinition.class })
	public void testSetCompartmentInitialBehaviorSupplier() {
		Builder builder = CompartmentInitialData.builder();
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_1;
		Supplier<Consumer<AgentContext>> supplier = () -> (c) -> {
		};

		// if the compartment id is null
		ContractException contractException = assertThrows(ContractException.class, () -> builder.setCompartmentInitialBehaviorSupplier(null, supplier));
		assertEquals(CompartmentError.NULL_COMPARTMENT_ID, contractException.getErrorType());

		// if the supplier is null
		contractException = assertThrows(ContractException.class, () -> builder.setCompartmentInitialBehaviorSupplier(compartmentId, null));
		assertEquals(CompartmentError.NULL_COMPARTMENT_INITIAL_BEHAVIOR_SUPPLIER, contractException.getErrorType());

		// if the compartment initial behavior was previously defined
		builder.setCompartmentInitialBehaviorSupplier(compartmentId, () -> (c) -> {
		});
		contractException = assertThrows(ContractException.class, () -> builder.setCompartmentInitialBehaviorSupplier(compartmentId, supplier));
		assertEquals(CompartmentError.DUPLICATE_COMPARTMENT_INITIAL_BEHAVIOR_ASSIGNMENT, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = CompartmentInitialData.Builder.class, name = "setCompartmentPropertyValue", args = { CompartmentId.class, CompartmentPropertyId.class, Object.class })
	public void testSetCompartmentPropertyValue() {
		Builder builder = CompartmentInitialData.builder();

		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_1;
		Supplier<Consumer<AgentContext>> supplier = () -> (c) -> {
		};
		CompartmentPropertyId compartmentPropertyId = TestCompartmentPropertyId.COMPARTMENT_PROPERTY_1_1;
		Object validValue = 5;
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setDefaultValue(0).setType(Integer.class).build();

		builder.setCompartmentInitialBehaviorSupplier(compartmentId, supplier);
		builder.defineCompartmentProperty(compartmentId, compartmentPropertyId, propertyDefinition);

		// if the compartment id is null
		ContractException contractException = assertThrows(ContractException.class, () -> builder.setCompartmentPropertyValue(null, compartmentPropertyId, validValue));
		assertEquals(CompartmentError.NULL_COMPARTMENT_ID, contractException.getErrorType());

		// if the compartment property id is null
		contractException = assertThrows(ContractException.class, () -> builder.setCompartmentPropertyValue(compartmentId, null, validValue));
		assertEquals(CompartmentError.NULL_COMPARTMENT_PROPERTY_ID, contractException.getErrorType());

		// if the compartment property value was previously defined
		builder.setCompartmentPropertyValue(compartmentId, compartmentPropertyId, validValue);
		contractException = assertThrows(ContractException.class, () -> builder.setCompartmentPropertyValue(compartmentId, compartmentPropertyId, validValue));
		assertEquals(CompartmentError.DUPLICATE_COMPARTMENT_PROPERTY_VALUE, contractException.getErrorType());

		CompartmentInitialData compartmentInitialData = builder.build();
		Integer compartmentPropertyValue = compartmentInitialData.getCompartmentPropertyValue(compartmentId, compartmentPropertyId);
		assertEquals(validValue, compartmentPropertyValue.intValue());

		// Note: Invalid values will not throw an exception and are caught
		// during the build invocation.
	}

	@Test
	@UnitTestMethod(target = CompartmentInitialData.Builder.class, name = "setPersonCompartment", args = { PersonId.class, CompartmentId.class })
	public void testSetPersonCompartment() {
		Builder builder = CompartmentInitialData.builder();

		PersonId personId = new PersonId(45);
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_1;

		// if the person id is null
		ContractException contractException = assertThrows(ContractException.class, () -> builder.setPersonCompartment(null, compartmentId));
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		// if the compartment id is null
		contractException = assertThrows(ContractException.class, () -> builder.setPersonCompartment(personId, null));
		assertEquals(CompartmentError.NULL_COMPARTMENT_ID, contractException.getErrorType());

		// if the person's compartment was previously defined
		builder.setPersonCompartment(personId, compartmentId);
		contractException = assertThrows(ContractException.class, () -> builder.setPersonCompartment(personId, compartmentId));
		assertEquals(CompartmentError.DUPLICATE_PERSON_COMPARTMENT_ASSIGNMENT, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = CompartmentInitialData.Builder.class, name = "setPersonCompartmentArrivalTracking", args = { TimeTrackingPolicy.class })
	public void testSetPersonCompartmentArrivalTracking() {
		Builder builder = CompartmentInitialData.builder();

		// if the timeTrackingPolicy is null
		ContractException contractException = assertThrows(ContractException.class, () -> builder.setPersonCompartmentArrivalTracking(null));
		assertEquals(CompartmentError.NULL_TIME_TRACKING_POLICY, contractException.getErrorType());

		// if the timeTrackingPolicy was previously defined
		builder.setPersonCompartmentArrivalTracking(TimeTrackingPolicy.TRACK_TIME);
		contractException = assertThrows(ContractException.class, () -> builder.setPersonCompartmentArrivalTracking(TimeTrackingPolicy.DO_NOT_TRACK_TIME));
		assertEquals(CompartmentError.DUPLICATE_TIME_TRACKING_POLICY, contractException.getErrorType());

	}

}
