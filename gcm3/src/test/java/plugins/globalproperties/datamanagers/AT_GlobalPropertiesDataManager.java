package plugins.globalproperties.datamanagers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.DataManagerContext;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.NucleusError;
import nucleus.Plugin;
import nucleus.Simulation;
import nucleus.testsupport.testplugin.ScenarioPlanCompletionObserver;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestError;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import plugins.globalproperties.GlobalPropertiesPlugin;
import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.globalproperties.events.GlobalPropertyDefinitionEvent;
import plugins.globalproperties.events.GlobalPropertyUpdateEvent;
import plugins.globalproperties.support.GlobalPropertiesError;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.globalproperties.support.GlobalPropertyInitialization;
import plugins.globalproperties.support.SimpleGlobalPropertyId;
import plugins.globalproperties.testsupport.GlobalsPropertiesActionSupport;
import plugins.globalproperties.testsupport.TestAuxiliaryGlobalPropertyId;
import plugins.globalproperties.testsupport.TestGlobalPropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;
import util.wrappers.MultiKey;

@UnitTest(target = GlobalPropertiesDataManager.class)

public final class AT_GlobalPropertiesDataManager {

	/////////////////////////////////
	// from the resolver
	////////////////////
	@Test
	@UnitTestConstructor(args = { GlobalPropertiesPluginData.class })
	public void testConstructor() {
		ContractException contractException = assertThrows(ContractException.class, () -> new GlobalPropertiesDataManager(null));
		assertEquals(GlobalPropertiesError.NULL_GLOBAL_PLUGIN_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testInit() {

		/*
		 * show that the event labelers for GlobalPropertyUpdateEvent were added
		 */
		GlobalsPropertiesActionSupport.testConsumer((c) -> {
			EventLabeler<GlobalPropertyUpdateEvent> eventLabeler = GlobalPropertyUpdateEvent.getEventLabeler();
			assertNotNull(eventLabeler);
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());
		});

		Map<GlobalPropertyId, Object> expectedPropertyValues = new LinkedHashMap<>();
		GlobalPropertiesPluginData.Builder globalsPluginBuilder = GlobalPropertiesPluginData.builder();
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			PropertyDefinition propertyDefinition = testGlobalPropertyId.getPropertyDefinition();
			globalsPluginBuilder.defineGlobalProperty(testGlobalPropertyId, propertyDefinition);
			expectedPropertyValues.put(testGlobalPropertyId, propertyDefinition.getDefaultValue().get());
		}
		// change two of the properties from their default values
		globalsPluginBuilder.setGlobalPropertyValue(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE, true);
		expectedPropertyValues.put(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE, true);

		globalsPluginBuilder.setGlobalPropertyValue(TestGlobalPropertyId.GLOBAL_PROPERTY_2_INTEGER_MUTABLE, 456);
		expectedPropertyValues.put(TestGlobalPropertyId.GLOBAL_PROPERTY_2_INTEGER_MUTABLE, 456);

		GlobalPropertiesPluginData globalPropertiesPluginData = globalsPluginBuilder.build();
		Plugin globalsPlugin = GlobalPropertiesPlugin.getPlugin(globalPropertiesPluginData);

		/*
		 * show that the Global Plugin Data is reflected in the initial state of
		 * the data manager
		 */
		TestPluginData.Builder testPluginDataBuilder = TestPluginData.builder();

		testPluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			// show that the data manager exists
			GlobalPropertiesDataManager globalPropertiesDataManager = c.getDataManager(GlobalPropertiesDataManager.class);

			// show that the global property ids are present
			Set<GlobalPropertyId> globalPropertyIds = globalPropertiesDataManager.getGlobalPropertyIds();
			assertEquals(TestGlobalPropertyId.values().length, globalPropertyIds.size());
			for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
				assertTrue(globalPropertyIds.contains(testGlobalPropertyId));
			}

			for (GlobalPropertyId globalPropertyId : expectedPropertyValues.keySet()) {
				assertEquals(expectedPropertyValues.get(globalPropertyId), globalPropertiesDataManager.getGlobalPropertyValue(globalPropertyId));
			}

		}));

		TestPluginData testPluginData = testPluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();
		Simulation	.builder()//
					.addPlugin(globalsPlugin)//
					.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput).addPlugin(testPlugin)//
					.build()//
					.execute();//

		// show that all actions were executed
		if (!scenarioPlanCompletionObserver.allPlansExecuted()) {
			throw new ContractException(TestError.TEST_EXECUTION_FAILURE);
		}

	}

	//////////////////////////
	// from the old data manager
	////////////////////////////////

	@Test
	@UnitTestMethod(name = "globalPropertyIdExists", args = { GlobalPropertyId.class })
	public void testGlobalPropertyIdExists() {

		GlobalsPropertiesActionSupport.testConsumer((c) -> {
			GlobalPropertiesDataManager globalPropertiesDataManager = c.getDataManager(GlobalPropertiesDataManager.class);
			for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
				assertTrue(globalPropertiesDataManager.globalPropertyIdExists(testGlobalPropertyId));
			}

			// show that a null global property id will return false
			assertFalse(globalPropertiesDataManager.globalPropertyIdExists(null));

			// show that an unknown global property id will return false
			assertFalse(globalPropertiesDataManager.globalPropertyIdExists(new SimpleGlobalPropertyId("bad prop")));
		});

	}

	///////////////////
	// from the old data view
	//////////////////

	@Test
	@UnitTestMethod(name = "setGlobalPropertyValue", args = { GlobalPropertyId.class, Object.class })
	public void testSetGlobalPropertyValue() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7837412421821851663L);

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();
		TestGlobalPropertyId globalPropertyId = TestGlobalPropertyId.GLOBAL_PROPERTY_2_INTEGER_MUTABLE;

		// create some containers to hold the expected and actual observations
		List<MultiKey> expectedObservations = new ArrayList<>();
		List<MultiKey> actualObservations = new ArrayList<>();

		// have an observer record changes to the property
		pluginDataBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			EventLabel<GlobalPropertyUpdateEvent> eventLabel = GlobalPropertyUpdateEvent.getEventLabel(c, globalPropertyId);
			c.subscribe(eventLabel, (c2, e) -> {
				MultiKey multiKey = new MultiKey(c2.getTime(), e.getGlobalPropertyId(), e.getPreviousPropertyValue(), e.getCurrentPropertyValue());
				actualObservations.add(multiKey);
			});
		}));

		// Have the actor set the value of the global property 1 a few times
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			GlobalPropertiesDataManager globalPropertiesDataManager = c.getDataManager(GlobalPropertiesDataManager.class);
			Integer currentValue = globalPropertiesDataManager.getGlobalPropertyValue(globalPropertyId);
			Integer newValue = globalPropertyId.getRandomPropertyValue(randomGenerator);
			globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyId, newValue);
			expectedObservations.add(new MultiKey(c.getTime(), globalPropertyId, currentValue, newValue));

		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			GlobalPropertiesDataManager globalPropertiesDataManager = c.getDataManager(GlobalPropertiesDataManager.class);
			Integer currentValue = globalPropertiesDataManager.getGlobalPropertyValue(globalPropertyId);
			Integer newValue = globalPropertyId.getRandomPropertyValue(randomGenerator);
			globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyId, newValue);
			expectedObservations.add(new MultiKey(c.getTime(), globalPropertyId, currentValue, newValue));

		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(3, (c) -> {
			GlobalPropertiesDataManager globalPropertiesDataManager = c.getDataManager(GlobalPropertiesDataManager.class);
			Integer currentValue = globalPropertiesDataManager.getGlobalPropertyValue(globalPropertyId);
			Integer newValue = globalPropertyId.getRandomPropertyValue(randomGenerator);
			globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyId, newValue);
			expectedObservations.add(new MultiKey(c.getTime(), globalPropertyId, currentValue, newValue));
		}));

		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		GlobalsPropertiesActionSupport.testConsumers(testPlugin);

		// show that the observations were correct
		assertTrue(expectedObservations.size() > 0);
		assertEquals(expectedObservations.size(), actualObservations.size());
		assertEquals(new LinkedHashSet<>(expectedObservations), new LinkedHashSet<>(actualObservations));

		// precondition test: if the global property id is null
		GlobalsPropertiesActionSupport.testConsumer((c) -> {
			GlobalPropertiesDataManager globalPropertiesDataManager = c.getDataManager(GlobalPropertiesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> globalPropertiesDataManager.setGlobalPropertyValue(null, 15));
			assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
		});

		// if the global property id is unknown
		GlobalsPropertiesActionSupport.testConsumer((c) -> {
			GlobalPropertiesDataManager globalPropertiesDataManager = c.getDataManager(GlobalPropertiesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> globalPropertiesDataManager.setGlobalPropertyValue(TestGlobalPropertyId.getUnknownGlobalPropertyId(), 15));
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
		});

		// if the property value is null
		GlobalsPropertiesActionSupport.testConsumer((c) -> {
			GlobalPropertiesDataManager globalPropertiesDataManager = c.getDataManager(GlobalPropertiesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> globalPropertiesDataManager.setGlobalPropertyValue(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE, null));
			assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());
		});

		// if the global property definition indicates the property is not
		// mutable
		GlobalsPropertiesActionSupport.testConsumer((c) -> {
			GlobalPropertiesDataManager globalPropertiesDataManager = c.getDataManager(GlobalPropertiesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> globalPropertiesDataManager.setGlobalPropertyValue(TestGlobalPropertyId.GLOBAL_PROPERTY_5_INTEGER_IMMUTABLE, 55));
			assertEquals(PropertyError.IMMUTABLE_VALUE, contractException.getErrorType());
		});

		// if the property value is incompatible with the property definition
		GlobalsPropertiesActionSupport.testConsumer((c) -> {
			GlobalPropertiesDataManager globalPropertiesDataManager = c.getDataManager(GlobalPropertiesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> globalPropertiesDataManager.setGlobalPropertyValue(TestGlobalPropertyId.GLOBAL_PROPERTY_2_INTEGER_MUTABLE, "value"));
			assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getGlobalPropertyValue", args = { GlobalPropertyId.class })
	public void testGetGlobalPropertyValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1059537118783693383L);

		// show that values can be retrieved
		GlobalsPropertiesActionSupport.testConsumer((c) -> {
			GlobalPropertiesDataManager globalPropertiesDataManager = c.getDataManager(GlobalPropertiesDataManager.class);

			for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
				PropertyDefinition propertyDefinition = globalPropertiesDataManager.getGlobalPropertyDefinition(testGlobalPropertyId);
				if (propertyDefinition.propertyValuesAreMutable()) {
					Object expectedValue = testGlobalPropertyId.getRandomPropertyValue(randomGenerator);
					globalPropertiesDataManager.setGlobalPropertyValue(testGlobalPropertyId, expectedValue);
					Object actualValue = globalPropertiesDataManager.getGlobalPropertyValue(testGlobalPropertyId);
					assertEquals(expectedValue, actualValue);
				}
			}
		});

		// precondition test : if the property id is null
		GlobalsPropertiesActionSupport.testConsumer((c) -> {
			GlobalPropertiesDataManager globalPropertiesDataManager = c.getDataManager(GlobalPropertiesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> globalPropertiesDataManager.getGlobalPropertyValue(null));
			assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
		});

		// precondition test : if the property id is unknown
		GlobalsPropertiesActionSupport.testConsumer((c) -> {
			GlobalPropertiesDataManager globalPropertiesDataManager = c.getDataManager(GlobalPropertiesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> globalPropertiesDataManager.getGlobalPropertyValue(TestGlobalPropertyId.getUnknownGlobalPropertyId()));
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getGlobalPropertyTime", args = { GlobalPropertyId.class })
	public void testGetGlobalPropertyTime() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5323616867741088481L);

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		IntStream.range(0, 10).forEach((i) -> {
			pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(i, (c) -> {
				GlobalPropertiesDataManager globalPropertiesDataManager = c.getDataManager(GlobalPropertiesDataManager.class);
				TestGlobalPropertyId globalPropertyId = TestGlobalPropertyId.GLOBAL_PROPERTY_3_DOUBLE_MUTABLE;
				Double newValue = globalPropertyId.getRandomPropertyValue(randomGenerator);
				globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyId, newValue);
				double globalPropertyTime = globalPropertiesDataManager.getGlobalPropertyTime(globalPropertyId);
				assertEquals(c.getTime(), globalPropertyTime);
			}));
		});
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		GlobalsPropertiesActionSupport.testConsumers(testPlugin);

		GlobalsPropertiesActionSupport.testConsumer((c) -> {
			GlobalPropertiesDataManager globalPropertiesDataManager = c.getDataManager(GlobalPropertiesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> globalPropertiesDataManager.getGlobalPropertyTime(null));
			assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
		});

		GlobalsPropertiesActionSupport.testConsumer((c) -> {
			GlobalPropertiesDataManager globalPropertiesDataManager = c.getDataManager(GlobalPropertiesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> globalPropertiesDataManager.getGlobalPropertyTime(TestGlobalPropertyId.getUnknownGlobalPropertyId()));
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "getGlobalPropertyIds", args = {})
	public void testGetGlobalPropertyIds() {
		GlobalsPropertiesActionSupport.testConsumer((c) -> {
			GlobalPropertiesDataManager globalPropertiesDataManager = c.getDataManager(GlobalPropertiesDataManager.class);

			Set<GlobalPropertyId> expectedGlobalPropertyIds = new LinkedHashSet<>();
			for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
				expectedGlobalPropertyIds.add(testGlobalPropertyId);
			}
			assertEquals(expectedGlobalPropertyIds, globalPropertiesDataManager.getGlobalPropertyIds());
		});
	}

	@Test
	@UnitTestMethod(name = "getGlobalPropertyDefinition", args = { GlobalPropertyId.class })
	public void testGetGlobalPropertyDefinition() {

		GlobalsPropertiesActionSupport.testConsumer((c) -> {
			GlobalPropertiesDataManager globalPropertiesDataManager = c.getDataManager(GlobalPropertiesDataManager.class);

			for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
				assertEquals(testGlobalPropertyId.getPropertyDefinition(), globalPropertiesDataManager.getGlobalPropertyDefinition(testGlobalPropertyId));
			}

		});

		// precondition : if the global property id is null
		GlobalsPropertiesActionSupport.testConsumer((c) -> {
			GlobalPropertiesDataManager globalPropertiesDataManager = c.getDataManager(GlobalPropertiesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> globalPropertiesDataManager.getGlobalPropertyDefinition(null));
			assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		});

		// precondition : if the global property id is unknown
		GlobalsPropertiesActionSupport.testConsumer((c) -> {
			GlobalPropertiesDataManager globalPropertiesDataManager = c.getDataManager(GlobalPropertiesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class,
					() -> globalPropertiesDataManager.getGlobalPropertyDefinition(TestGlobalPropertyId.getUnknownGlobalPropertyId()));
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "defineGlobalProperty", args = { GlobalPropertyId.class, PropertyDefinition.class })
	public void testDefineGlobalProperty() {
		
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// show that new global properties can be defined
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

			double planTime = 1;
			for (TestAuxiliaryGlobalPropertyId auxPropertyId : TestAuxiliaryGlobalPropertyId.values()) {

				c.addPlan((c2) -> {
					GlobalPropertiesDataManager globalPropertiesDataManager = c2.getDataManager(GlobalPropertiesDataManager.class);
					PropertyDefinition expectedPropertyDefinition = auxPropertyId.getPropertyDefinition();
					GlobalPropertyInitialization globalPropertyInitialization = GlobalPropertyInitialization.builder().setGlobalPropertyId(auxPropertyId).setPropertyDefinition(expectedPropertyDefinition).build();
					globalPropertiesDataManager.defineGlobalProperty(globalPropertyInitialization);

					// show that the definition was added
					PropertyDefinition actualPopertyDefinition = globalPropertiesDataManager.getGlobalPropertyDefinition(auxPropertyId);
					assertEquals(expectedPropertyDefinition, actualPopertyDefinition);

					// record the expected observation
					MultiKey multiKey = new MultiKey(c2.getTime(), auxPropertyId, expectedPropertyDefinition.getDefaultValue().get());
					expectedObservations.add(multiKey);

					// show that the property has the correct initial value
					Object expectedValue = expectedPropertyDefinition.getDefaultValue().get();
					Object actualValue = globalPropertiesDataManager.getGlobalPropertyValue(auxPropertyId);
					assertEquals(expectedValue, actualValue);

					// show that the property has the correct initial time
					double expectedTime = c2.getTime();
					double actualTime = globalPropertiesDataManager.getGlobalPropertyTime(auxPropertyId);
					assertEquals(expectedTime, actualTime);

				}, planTime++);
			}
		}));

		// have an observer collect the observations
		pluginDataBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			c.subscribe(GlobalPropertyDefinitionEvent.class, (c2, e) -> {
				// record the actual observation
				MultiKey multiKey = new MultiKey(c2.getTime(), e.globalPropertyId(), e.initialPropertyValue());
				actualObservations.add(multiKey);
			});
		}));

		/*
		 * Have the observer show the the expected and actual observations match
		 * after all the new property definitions have been added.
		 */
		double planTime = TestAuxiliaryGlobalPropertyId.values().length + 1;
		pluginDataBuilder.addTestActorPlan("observer", new TestActorPlan(planTime, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		GlobalsPropertiesActionSupport.testConsumers(testPlugin);


		
		// precondition test: if the global property initialization is null
		GlobalsPropertiesActionSupport.testConsumer((c) -> {
			GlobalPropertiesDataManager globalPropertiesDataManager = c.getDataManager(GlobalPropertiesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> globalPropertiesDataManager.defineGlobalProperty(null));
			assertEquals(GlobalPropertiesError.NULL_GLOBAL_PROPERTY_INITIALIZATION, contractException.getErrorType());
		});

		// precondition test: if the global property already exists
		GlobalsPropertiesActionSupport.testConsumer((c) -> {
			GlobalPropertiesDataManager globalPropertiesDataManager = c.getDataManager(GlobalPropertiesDataManager.class);
			GlobalPropertyId globalPropertyId = TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE;
			PropertyDefinition propertyDefinition = TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE.getPropertyDefinition();
			GlobalPropertyInitialization globalPropertyInitialization =//
					GlobalPropertyInitialization.builder()//
					.setGlobalPropertyId(globalPropertyId)//
					.setPropertyDefinition(propertyDefinition)//
					.build();

			
			ContractException contractException = assertThrows(ContractException.class, () -> globalPropertiesDataManager.defineGlobalProperty(globalPropertyInitialization));
			assertEquals(PropertyError.DUPLICATE_PROPERTY_DEFINITION, contractException.getErrorType());
		});

	}

}
