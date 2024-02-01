package gov.hhs.aspr.ms.gcm.plugins.globalproperties.datamanagers;

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
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.nucleus.DataManagerContext;
import gov.hhs.aspr.ms.gcm.nucleus.EventFilter;
import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.nucleus.Simulation;
import gov.hhs.aspr.ms.gcm.nucleus.SimulationState;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.runcontinuityplugin.RunContinuityPlugin;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.runcontinuityplugin.RunContinuityPluginData;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestActorPlan;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestOutputConsumer;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestPluginData;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.GlobalPropertiesPlugin;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.events.GlobalPropertyDefinitionEvent;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.events.GlobalPropertyUpdateEvent;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.support.GlobalPropertiesError;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.support.GlobalPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.support.GlobalPropertyInitialization;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.support.SimpleGlobalPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.testsupport.GlobalPropertiesTestPluginFactory;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.testsupport.GlobalPropertiesTestPluginFactory.Factory;
import gov.hhs.aspr.ms.gcm.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.testsupport.TestAuxiliaryGlobalPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.testsupport.TestGlobalPropertyId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;
import util.wrappers.MultiKey;

public final class AT_GlobalPropertiesDataManager {
    /**
     * Demonstrates that the data manager exhibits run continuity. The state of the
     * data manager is not effected by repeatedly starting and stopping the
     * simulation.
     */
    @Test
    @UnitTestMethod(target = GlobalPropertiesDataManager.class, name = "init", args = { DataManagerContext.class })
    public void testStateContinuity() {

        /*
         * Note that we are not testing the content of the plugin datas -- that is
         * covered by the other state tests. We show here only that the resulting plugin
         * data state is the same without regard to how we break up the run.
         */

        Set<String> pluginDatas = new LinkedHashSet<>();

        pluginDatas.add(testStateContinuity(1));
        pluginDatas.add(testStateContinuity(5));
        pluginDatas.add(testStateContinuity(10));

        assertEquals(1, pluginDatas.size());

    }

    /*
     * Returns the GlobalPropertiesPluginData resulting from several global
     * properties related events over several days. Attempt to stop and start the
     * simulation by the given number of increments.
     */
    private String testStateContinuity(int incrementCount) {

        String result = null;

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5369912793633438426L);

        /*
         * Build the RunContinuityPluginData with context consumers that will add and
         * set global property values
         */
        RunContinuityPluginData.Builder continuityBuilder = RunContinuityPluginData.builder();

        int taskTime = 0;

        for (int i = 0; i < 50; i++) {
            continuityBuilder.addContextConsumer(taskTime++, (c) -> {

                GlobalPropertiesDataManager globalPropertiesDataManager = c
                        .getDataManager(GlobalPropertiesDataManager.class);

                // try to add a new property definition
                List<TestGlobalPropertyId> candidates = new ArrayList<>();
                for (TestGlobalPropertyId globalPropertyId : TestGlobalPropertyId.values()) {
                    if (!globalPropertiesDataManager.globalPropertyIdExists(globalPropertyId)) {
                        candidates.add(globalPropertyId);
                    }
                }

                if (!candidates.isEmpty()) {
                    TestGlobalPropertyId testGlobalPropertyId = candidates
                            .get(randomGenerator.nextInt(candidates.size()));
                    PropertyDefinition propertyDefinition = testGlobalPropertyId.getPropertyDefinition();
                    GlobalPropertyInitialization globalPropertyInitialization;
                    if (propertyDefinition.getDefaultValue().isEmpty()) {
                        Object propertyValue = testGlobalPropertyId.getRandomPropertyValue(randomGenerator);
                        globalPropertyInitialization = //
                                GlobalPropertyInitialization.builder()//
                                        .setGlobalPropertyId(testGlobalPropertyId)//
                                        .setPropertyDefinition(propertyDefinition)//
                                        .setValue(propertyValue)//
                                        .build();
                    } else {
                        globalPropertyInitialization = //
                                GlobalPropertyInitialization.builder()//
                                        .setGlobalPropertyId(testGlobalPropertyId)//
                                        .setPropertyDefinition(propertyDefinition)//
                                        .build();
                    }
                    globalPropertiesDataManager.defineGlobalProperty(globalPropertyInitialization);

                }

                // find a global property to update

                candidates.clear();

                for (TestGlobalPropertyId globalPropertyId : TestGlobalPropertyId.values()) {
                    if (globalPropertiesDataManager.globalPropertyIdExists(globalPropertyId)) {
                        if (globalPropertiesDataManager.getGlobalPropertyDefinition(globalPropertyId)
                                .propertyValuesAreMutable()) {
                            candidates.add(globalPropertyId);
                        }
                    }
                }

                if (!candidates.isEmpty()) {
                    TestGlobalPropertyId testGlobalPropertyId = candidates
                            .get(randomGenerator.nextInt(candidates.size()));
                    Object propertyValue = testGlobalPropertyId.getRandomPropertyValue(randomGenerator);
                    globalPropertiesDataManager.setGlobalPropertyValue(testGlobalPropertyId, propertyValue);
                }

            });
        }

        continuityBuilder.addContextConsumer(taskTime++, (c) -> {
            GlobalPropertiesDataManager globalPropertiesDataManager = c
                    .getDataManager(GlobalPropertiesDataManager.class);
            c.releaseOutput(globalPropertiesDataManager.toString());
        });

        RunContinuityPluginData runContinuityPluginData = continuityBuilder.build();

        // Build an empty global properties plugin data for time zero
        GlobalPropertiesPluginData globalPropertiesPluginData = GlobalPropertiesPluginData.builder().build();

        // build the initial simulation state data -- time starts at zero
        SimulationState simulationState = SimulationState.builder().build();

        /*
         * Run the simulation in one day increments until all the plans in the run
         * continuity plugin data have been executed
         */
        double haltTime = 0;
        double maxTime = Double.NEGATIVE_INFINITY;
        for (Pair<Double, Consumer<ActorContext>> pair : runContinuityPluginData.getConsumers()) {
            Double time = pair.getFirst();
            maxTime = FastMath.max(maxTime, time);
        }
        double timeIncrement = maxTime / incrementCount;
        while (!runContinuityPluginData.allPlansComplete()) {
            haltTime += timeIncrement;

            // build the run continuity plugin
            Plugin runContinuityPlugin = RunContinuityPlugin.builder()//
                    .setRunContinuityPluginData(runContinuityPluginData)//
                    .build();

            // build the people plugin
            Plugin globalPropertiesPlugin = GlobalPropertiesPlugin.builder()//
                    .setGlobalPropertiesPluginData(globalPropertiesPluginData)//
                    .getGlobalPropertiesPlugin();

            TestOutputConsumer outputConsumer = new TestOutputConsumer();

            // execute the simulation so that it produces a people plugin data
            Simulation simulation = Simulation.builder()//
                    .addPlugin(globalPropertiesPlugin)//
                    .addPlugin(runContinuityPlugin)//
                    .setSimulationHaltTime(haltTime)//
                    .setRecordState(true)//
                    .setOutputConsumer(outputConsumer)//
                    .setSimulationState(simulationState)//
                    .build();//
            simulation.execute();

            // retrieve the people plugin data
            globalPropertiesPluginData = outputConsumer.getOutputItem(GlobalPropertiesPluginData.class).get();

            // retrieve the simulation state
            simulationState = outputConsumer.getOutputItem(SimulationState.class).get();

            // retrieve the run continuity plugin data
            runContinuityPluginData = outputConsumer.getOutputItem(RunContinuityPluginData.class).get();

            Optional<String> optional = outputConsumer.getOutputItem(String.class);
            if (optional.isPresent()) {
                result = optional.get();
            }
        }

        // show that the result is a large string
        assertNotNull(result);
        assertTrue(result.length() > 100);

        return result;

    }

    @Test
    @UnitTestConstructor(target = GlobalPropertiesDataManager.class, args = { GlobalPropertiesPluginData.class })
    public void testConstructor() {
        ContractException contractException = assertThrows(ContractException.class,
                () -> new GlobalPropertiesDataManager(null));
        assertEquals(GlobalPropertiesError.NULL_GLOBAL_PLUGIN_DATA, contractException.getErrorType());
    }

    /**
     * Demonstrates that the data manager's initial state reflects its plugin data
     */
    @Test
    @UnitTestMethod(target = GlobalPropertiesDataManager.class, name = "init", args = { DataManagerContext.class })
    public void testStateInitialization() {

        Map<GlobalPropertyId, Object> expectedPropertyValues = new LinkedHashMap<>();
        GlobalPropertiesPluginData.Builder globalsPluginBuilder = GlobalPropertiesPluginData.builder();
        long seed = 5100286389011347218L;
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

        for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
            PropertyDefinition propertyDefinition = testGlobalPropertyId.getPropertyDefinition();
            globalsPluginBuilder.defineGlobalProperty(testGlobalPropertyId, propertyDefinition, 0);
            if (propertyDefinition.getDefaultValue().isPresent()) {
                expectedPropertyValues.put(testGlobalPropertyId, propertyDefinition.getDefaultValue().get());
            } else {
                Object value = testGlobalPropertyId.getRandomPropertyValue(randomGenerator);
                globalsPluginBuilder.setGlobalPropertyValue(testGlobalPropertyId, value, 0);
                expectedPropertyValues.put(testGlobalPropertyId, value);
            }
        }
        // change two of the properties from their default values
        globalsPluginBuilder.setGlobalPropertyValue(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE, true, 0);
        expectedPropertyValues.put(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE, true);

        globalsPluginBuilder.setGlobalPropertyValue(TestGlobalPropertyId.GLOBAL_PROPERTY_2_INTEGER_MUTABLE, 456, 0);
        expectedPropertyValues.put(TestGlobalPropertyId.GLOBAL_PROPERTY_2_INTEGER_MUTABLE, 456);

        GlobalPropertiesPluginData globalPropertiesPluginData = globalsPluginBuilder.build();

        /*
         * show that the Global Plugin Data is reflected in the initial state of the
         * data manager
         */
        TestPluginData.Builder testPluginDataBuilder = TestPluginData.builder();

        testPluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
            // show that the data manager exists
            GlobalPropertiesDataManager globalPropertiesDataManager = c
                    .getDataManager(GlobalPropertiesDataManager.class);

            // show that the global property ids are present
            Set<GlobalPropertyId> globalPropertyIds = globalPropertiesDataManager.getGlobalPropertyIds();
            assertEquals(TestGlobalPropertyId.values().length, globalPropertyIds.size());
            for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
                assertTrue(globalPropertyIds.contains(testGlobalPropertyId));
            }

            for (GlobalPropertyId globalPropertyId : expectedPropertyValues.keySet()) {
                assertEquals(expectedPropertyValues.get(globalPropertyId),
                        globalPropertiesDataManager.getGlobalPropertyValue(globalPropertyId));
            }

        }));

        TestPluginData testPluginData = testPluginDataBuilder.build();
        Factory factory = GlobalPropertiesTestPluginFactory.factory(seed, testPluginData)
                .setGlobalPropertiesPluginData(globalPropertiesPluginData);
        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

    }

    /**
     * Demonstrates that the data manager produces plugin data that reflects its
     * final state
     */
    @Test
    @UnitTestMethod(target = GlobalPropertiesDataManager.class, name = "init", args = { DataManagerContext.class })
    public void testStateFinalization() {

        GlobalPropertiesPluginData.Builder globalsPluginBuilder = GlobalPropertiesPluginData.builder();
        GlobalPropertiesPluginData globalPropertiesPluginData = globalsPluginBuilder.build();

        // add a property definition
        PropertyDefinition propertyDefinition = TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE
                .getPropertyDefinition();
        GlobalPropertyInitialization globalPropertyInitialization = GlobalPropertyInitialization.builder()
                .setGlobalPropertyId(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE)
                .setPropertyDefinition(propertyDefinition).build();

        TestPluginData.Builder testPluginDataBuilder = TestPluginData.builder();

        // define property definition with the data manager
        testPluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
            GlobalPropertiesDataManager globalPropertiesDataManager = c
                    .getDataManager(GlobalPropertiesDataManager.class);
            globalPropertiesDataManager.defineGlobalProperty(globalPropertyInitialization);
            globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyInitialization.getGlobalPropertyId(),
                    true);
        }));

        // show that the plugin data contains what we defined
        TestPluginData testPluginData = testPluginDataBuilder.build();
        Factory factory = GlobalPropertiesTestPluginFactory.factory(5100286389011347218L, testPluginData)
                .setGlobalPropertiesPluginData(globalPropertiesPluginData);
        TestOutputConsumer testOutputConsumer = TestSimulation.builder().addPlugins(factory.getPlugins())
                .setSimulationHaltTime(2).setProduceSimulationStateOnHalt(true).build().execute();
        Map<GlobalPropertiesPluginData, Integer> outputItems = testOutputConsumer
                .getOutputItemMap(GlobalPropertiesPluginData.class);
        assertEquals(1, outputItems.size());
        GlobalPropertiesPluginData actualPluginData = outputItems.keySet().iterator().next();
        GlobalPropertiesPluginData expectedPluginData = GlobalPropertiesPluginData.builder()
                .defineGlobalProperty(globalPropertyInitialization.getGlobalPropertyId(),
                        globalPropertyInitialization.getPropertyDefinition(), 0)
                .setGlobalPropertyValue(globalPropertyInitialization.getGlobalPropertyId(), true, 0).build();
        assertEquals(expectedPluginData, actualPluginData);

        // show that the plugin data persists after multiple actions
        PropertyDefinition propertyDefinition2 = TestGlobalPropertyId.GLOBAL_PROPERTY_2_INTEGER_MUTABLE
                .getPropertyDefinition();
        GlobalPropertyInitialization globalPropertyInitialization2 = GlobalPropertyInitialization.builder()
                .setGlobalPropertyId(TestGlobalPropertyId.GLOBAL_PROPERTY_2_INTEGER_MUTABLE)
                .setPropertyDefinition(propertyDefinition2).build();

        PropertyDefinition propertyDefinition3 = TestGlobalPropertyId.GLOBAL_PROPERTY_3_DOUBLE_MUTABLE
                .getPropertyDefinition();
        GlobalPropertyInitialization globalPropertyInitialization3 = GlobalPropertyInitialization.builder()
                .setGlobalPropertyId(TestGlobalPropertyId.GLOBAL_PROPERTY_3_DOUBLE_MUTABLE).setValue(10.0)
                .setPropertyDefinition(propertyDefinition3).build();

        testPluginDataBuilder = TestPluginData.builder();

        testPluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
            GlobalPropertiesDataManager globalPropertiesDataManager = c
                    .getDataManager(GlobalPropertiesDataManager.class);
            globalPropertiesDataManager.defineGlobalProperty(globalPropertyInitialization2);
            globalPropertiesDataManager.defineGlobalProperty(globalPropertyInitialization3);
            globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyInitialization2.getGlobalPropertyId(), 5);
            globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyInitialization2.getGlobalPropertyId(), 3);
            globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyInitialization3.getGlobalPropertyId(),
                    14.5);
            globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyInitialization3.getGlobalPropertyId(),
                    32.8);
        }));

        testPluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
            GlobalPropertiesDataManager globalPropertiesDataManager = c
                    .getDataManager(GlobalPropertiesDataManager.class);
            globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyInitialization2.getGlobalPropertyId(), 15);
            globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyInitialization3.getGlobalPropertyId(),
                    15.9);
        }));

        testPluginData = testPluginDataBuilder.build();
        factory = GlobalPropertiesTestPluginFactory.factory(5100286389011347218L, testPluginData)
                .setGlobalPropertiesPluginData(globalPropertiesPluginData);
        testOutputConsumer = TestSimulation.builder().addPlugins(factory.getPlugins()).setSimulationHaltTime(2)
                .setProduceSimulationStateOnHalt(true).build().execute();
        outputItems = testOutputConsumer.getOutputItemMap(GlobalPropertiesPluginData.class);
        assertEquals(1, outputItems.size());
        actualPluginData = outputItems.keySet().iterator().next();
        expectedPluginData = GlobalPropertiesPluginData.builder()//
                .defineGlobalProperty(globalPropertyInitialization2.getGlobalPropertyId(),
                        globalPropertyInitialization2.getPropertyDefinition(), 0)//
                .defineGlobalProperty(globalPropertyInitialization3.getGlobalPropertyId(),
                        globalPropertyInitialization3.getPropertyDefinition(), 0)//
                .setGlobalPropertyValue(globalPropertyInitialization3.getGlobalPropertyId(), 15.9, 1)//
                .setGlobalPropertyValue(globalPropertyInitialization2.getGlobalPropertyId(), 15, 1)//
                .build();
        assertEquals(expectedPluginData, actualPluginData);
    }

    @Test
    @UnitTestMethod(target = GlobalPropertiesDataManager.class, name = "globalPropertyIdExists", args = {
            GlobalPropertyId.class })
    public void testGlobalPropertyIdExists() {

        Factory factory = GlobalPropertiesTestPluginFactory.factory(5100286389011347218L, (c) -> {
            GlobalPropertiesDataManager globalPropertiesDataManager = c
                    .getDataManager(GlobalPropertiesDataManager.class);
            for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
                assertTrue(globalPropertiesDataManager.globalPropertyIdExists(testGlobalPropertyId));
            }

            // show that a null global property id will return false
            assertFalse(globalPropertiesDataManager.globalPropertyIdExists(null));

            // show that an unknown global property id will return false
            assertFalse(globalPropertiesDataManager.globalPropertyIdExists(new SimpleGlobalPropertyId("bad prop")));
        });

        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
    }

    @Test
    @UnitTestMethod(target = GlobalPropertiesDataManager.class, name = "setGlobalPropertyValue", args = {
            GlobalPropertyId.class, Object.class })
    public void testSetGlobalPropertyValue() {

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7837412421821851663L);

        TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();
        TestGlobalPropertyId globalPropertyId = TestGlobalPropertyId.GLOBAL_PROPERTY_2_INTEGER_MUTABLE;

        // create some containers to hold the expected and actual observations
        List<MultiKey> expectedObservations = new ArrayList<>();
        List<MultiKey> actualObservations = new ArrayList<>();

        // have an observer record changes to the property
        pluginDataBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
            GlobalPropertiesDataManager globalPropertiesDataManager = c
                    .getDataManager(GlobalPropertiesDataManager.class);
            EventFilter<GlobalPropertyUpdateEvent> eventFilter = globalPropertiesDataManager
                    .getEventFilterForGlobalPropertyUpdateEvent(globalPropertyId);
            c.subscribe(eventFilter, (c2, e) -> {
                MultiKey multiKey = new MultiKey(c2.getTime(), e.globalPropertyId(), e.previousPropertyValue(),
                        e.currentPropertyValue());
                actualObservations.add(multiKey);
            });
        }));

        // Have the actor set the value of the global property 1 a few times
        pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
            GlobalPropertiesDataManager globalPropertiesDataManager = c
                    .getDataManager(GlobalPropertiesDataManager.class);
            Integer currentValue = globalPropertiesDataManager.getGlobalPropertyValue(globalPropertyId);
            Integer newValue = globalPropertyId.getRandomPropertyValue(randomGenerator);
            globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyId, newValue);
            expectedObservations.add(new MultiKey(c.getTime(), globalPropertyId, currentValue, newValue));
        }));

        pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
            GlobalPropertiesDataManager globalPropertiesDataManager = c
                    .getDataManager(GlobalPropertiesDataManager.class);
            Integer currentValue = globalPropertiesDataManager.getGlobalPropertyValue(globalPropertyId);
            Integer newValue = globalPropertyId.getRandomPropertyValue(randomGenerator);
            globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyId, newValue);
            expectedObservations.add(new MultiKey(c.getTime(), globalPropertyId, currentValue, newValue));

        }));

        pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(3, (c) -> {
            GlobalPropertiesDataManager globalPropertiesDataManager = c
                    .getDataManager(GlobalPropertiesDataManager.class);
            Integer currentValue = globalPropertiesDataManager.getGlobalPropertyValue(globalPropertyId);
            Integer newValue = globalPropertyId.getRandomPropertyValue(randomGenerator);
            globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyId, newValue);
            expectedObservations.add(new MultiKey(c.getTime(), globalPropertyId, currentValue, newValue));
        }));

        TestPluginData testPluginData = pluginDataBuilder.build();
        Factory factory = GlobalPropertiesTestPluginFactory.factory(5100286389011347218L, testPluginData);
        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

        // show that the observations were correct
        assertEquals(3, expectedObservations.size());
        assertEquals(expectedObservations.size(), actualObservations.size());
        assertEquals(new LinkedHashSet<>(expectedObservations), new LinkedHashSet<>(actualObservations));

        // precondition test: if the global property id is null
        ContractException contractException = assertThrows(ContractException.class, () -> {
            Factory factory2 = GlobalPropertiesTestPluginFactory.factory(5100286389011347218L, (c) -> {
                GlobalPropertiesDataManager globalPropertiesDataManager = c
                        .getDataManager(GlobalPropertiesDataManager.class);
                globalPropertiesDataManager.setGlobalPropertyValue(null, 15);
            });
            TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
        });
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

        // if the global property id is unknown
        contractException = assertThrows(ContractException.class, () -> {
            Factory factory2 = GlobalPropertiesTestPluginFactory.factory(5100286389011347218L, (c) -> {
                GlobalPropertiesDataManager globalPropertiesDataManager = c
                        .getDataManager(GlobalPropertiesDataManager.class);
                globalPropertiesDataManager.setGlobalPropertyValue(TestGlobalPropertyId.getUnknownGlobalPropertyId(),
                        15);
            });
            TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
        });
        assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

        // if the property value is null
        contractException = assertThrows(ContractException.class, () -> {
            Factory factory2 = GlobalPropertiesTestPluginFactory.factory(5100286389011347218L, (c) -> {
                GlobalPropertiesDataManager globalPropertiesDataManager = c
                        .getDataManager(GlobalPropertiesDataManager.class);
                globalPropertiesDataManager
                        .setGlobalPropertyValue(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE, null);
            });
            TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
        });
        assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());

        // if the global property definition indicates the property is not
        // mutable
        contractException = assertThrows(ContractException.class, () -> {
            Factory factory2 = GlobalPropertiesTestPluginFactory.factory(5100286389011347218L, (c) -> {
                GlobalPropertiesDataManager globalPropertiesDataManager = c
                        .getDataManager(GlobalPropertiesDataManager.class);
                globalPropertiesDataManager
                        .setGlobalPropertyValue(TestGlobalPropertyId.GLOBAL_PROPERTY_5_INTEGER_IMMUTABLE, 55);
            });
            TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
        });
        assertEquals(PropertyError.IMMUTABLE_VALUE, contractException.getErrorType());

        // if the property value is incompatible with the property definition
        contractException = assertThrows(ContractException.class, () -> {
            Factory factory2 = GlobalPropertiesTestPluginFactory.factory(5100286389011347218L, (c) -> {
                GlobalPropertiesDataManager globalPropertiesDataManager = c
                        .getDataManager(GlobalPropertiesDataManager.class);
                globalPropertiesDataManager
                        .setGlobalPropertyValue(TestGlobalPropertyId.GLOBAL_PROPERTY_2_INTEGER_MUTABLE, "value");
            });
            TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
        });
        assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

    }

    @Test
    @UnitTestMethod(target = GlobalPropertiesDataManager.class, name = "getGlobalPropertyValue", args = {
            GlobalPropertyId.class })
    public void testGetGlobalPropertyValue() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1059537118783693383L);

        // show that values can be retrieved
        Factory factory = GlobalPropertiesTestPluginFactory.factory(5100286389011347218L, (c) -> {
            GlobalPropertiesDataManager globalPropertiesDataManager = c
                    .getDataManager(GlobalPropertiesDataManager.class);

            for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
                PropertyDefinition propertyDefinition = globalPropertiesDataManager
                        .getGlobalPropertyDefinition(testGlobalPropertyId);
                if (propertyDefinition.propertyValuesAreMutable()) {
                    Object expectedValue = testGlobalPropertyId.getRandomPropertyValue(randomGenerator);
                    globalPropertiesDataManager.setGlobalPropertyValue(testGlobalPropertyId, expectedValue);
                    Object actualValue = globalPropertiesDataManager.getGlobalPropertyValue(testGlobalPropertyId);
                    assertEquals(expectedValue, actualValue);
                }
            }
        });
        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

        // precondition test : if the property id is null
        ContractException contractException = assertThrows(ContractException.class, () -> {
            Factory factory2 = GlobalPropertiesTestPluginFactory.factory(5100286389011347218L, (c) -> {
                GlobalPropertiesDataManager globalPropertiesDataManager = c
                        .getDataManager(GlobalPropertiesDataManager.class);
                globalPropertiesDataManager.getGlobalPropertyValue(null);
            });
            TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
        });
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

        // precondition test : if the property id is unknown
        contractException = assertThrows(ContractException.class, () -> {
            Factory factory2 = GlobalPropertiesTestPluginFactory.factory(5100286389011347218L, (c) -> {
                GlobalPropertiesDataManager globalPropertiesDataManager = c
                        .getDataManager(GlobalPropertiesDataManager.class);
                globalPropertiesDataManager.getGlobalPropertyValue(TestGlobalPropertyId.getUnknownGlobalPropertyId());
            });
            TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
        });
        assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

    }

    @Test
    @UnitTestMethod(target = GlobalPropertiesDataManager.class, name = "getGlobalPropertyTime", args = {
            GlobalPropertyId.class })
    public void testGetGlobalPropertyTime() {

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5323616867741088481L);

        TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

        IntStream.range(0, 10).forEach((i) -> {
            pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(i, (c) -> {
                GlobalPropertiesDataManager globalPropertiesDataManager = c
                        .getDataManager(GlobalPropertiesDataManager.class);
                TestGlobalPropertyId globalPropertyId = TestGlobalPropertyId.GLOBAL_PROPERTY_3_DOUBLE_MUTABLE;
                Double newValue = globalPropertyId.getRandomPropertyValue(randomGenerator);
                globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyId, newValue);
                double globalPropertyTime = globalPropertiesDataManager.getGlobalPropertyTime(globalPropertyId);
                assertEquals(c.getTime(), globalPropertyTime);
            }));
        });
        TestPluginData testPluginData = pluginDataBuilder.build();

        Factory factory = GlobalPropertiesTestPluginFactory.factory(5100286389011347218L, testPluginData);
        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

        ContractException contractException = assertThrows(ContractException.class, () -> {
            Factory factory2 = GlobalPropertiesTestPluginFactory.factory(5100286389011347218L, (c) -> {
                GlobalPropertiesDataManager globalPropertiesDataManager = c
                        .getDataManager(GlobalPropertiesDataManager.class);
                globalPropertiesDataManager.getGlobalPropertyTime(null);
            });
            TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
        });
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

        contractException = assertThrows(ContractException.class, () -> {
            Factory factory2 = GlobalPropertiesTestPluginFactory.factory(5100286389011347218L, (c) -> {
                GlobalPropertiesDataManager globalPropertiesDataManager = c
                        .getDataManager(GlobalPropertiesDataManager.class);
                globalPropertiesDataManager.getGlobalPropertyTime(TestGlobalPropertyId.getUnknownGlobalPropertyId());

            });
            TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
        });
        assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

    }

    @Test
    @UnitTestMethod(target = GlobalPropertiesDataManager.class, name = "getGlobalPropertyIds", args = {})
    public void testGetGlobalPropertyIds() {

        Factory factory = GlobalPropertiesTestPluginFactory.factory(5100286389011347218L, (c) -> {
            GlobalPropertiesDataManager globalPropertiesDataManager = c
                    .getDataManager(GlobalPropertiesDataManager.class);

            Set<GlobalPropertyId> expectedGlobalPropertyIds = new LinkedHashSet<>();
            for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
                expectedGlobalPropertyIds.add(testGlobalPropertyId);
            }
            assertEquals(expectedGlobalPropertyIds, globalPropertiesDataManager.getGlobalPropertyIds());
        });

        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
    }

    @Test
    @UnitTestMethod(target = GlobalPropertiesDataManager.class, name = "getGlobalPropertyDefinition", args = {
            GlobalPropertyId.class })
    public void testGetGlobalPropertyDefinition() {
        Factory factory = GlobalPropertiesTestPluginFactory.factory(5100286389011347218L, (c) -> {
            GlobalPropertiesDataManager globalPropertiesDataManager = c
                    .getDataManager(GlobalPropertiesDataManager.class);

            for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
                assertEquals(testGlobalPropertyId.getPropertyDefinition(),
                        globalPropertiesDataManager.getGlobalPropertyDefinition(testGlobalPropertyId));
            }
        });

        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

        // precondition : if the global property id is null
        ContractException contractException = assertThrows(ContractException.class, () -> {
            Factory factory2 = GlobalPropertiesTestPluginFactory.factory(5100286389011347218L, (c) -> {
                GlobalPropertiesDataManager globalPropertiesDataManager = c
                        .getDataManager(GlobalPropertiesDataManager.class);
                globalPropertiesDataManager.getGlobalPropertyDefinition(null);
            });
            TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
        });
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

        // precondition : if the global property id is unknown
        contractException = assertThrows(ContractException.class, () -> {
            Factory factory2 = GlobalPropertiesTestPluginFactory.factory(5100286389011347218L, (c) -> {
                GlobalPropertiesDataManager globalPropertiesDataManager = c
                        .getDataManager(GlobalPropertiesDataManager.class);
                globalPropertiesDataManager
                        .getGlobalPropertyDefinition(TestGlobalPropertyId.getUnknownGlobalPropertyId());
            });
            TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
        });
        assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

    }

    @Test
    @UnitTestMethod(target = GlobalPropertiesDataManager.class, name = "defineGlobalProperty", args = {
            GlobalPropertyInitialization.class })
    public void testDefineGlobalProperty() {

        Set<MultiKey> expectedObservations = new LinkedHashSet<>();
        Set<MultiKey> actualObservations = new LinkedHashSet<>();

        TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

        // show that new global properties can be defined
        pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

            double planTime = 1;
            for (TestAuxiliaryGlobalPropertyId auxPropertyId : TestAuxiliaryGlobalPropertyId.values()) {

                c.addPlan((c2) -> {
                    GlobalPropertiesDataManager globalPropertiesDataManager = c2
                            .getDataManager(GlobalPropertiesDataManager.class);
                    PropertyDefinition expectedPropertyDefinition = auxPropertyId.getPropertyDefinition();
                    GlobalPropertyInitialization globalPropertyInitialization = GlobalPropertyInitialization.builder()
                            .setGlobalPropertyId(auxPropertyId).setPropertyDefinition(expectedPropertyDefinition)
                            .build();
                    globalPropertiesDataManager.defineGlobalProperty(globalPropertyInitialization);

                    // show that the definition was added
                    PropertyDefinition actualPopertyDefinition = globalPropertiesDataManager
                            .getGlobalPropertyDefinition(auxPropertyId);
                    assertEquals(expectedPropertyDefinition, actualPopertyDefinition);

                    // record the expected observation
                    MultiKey multiKey = new MultiKey(c2.getTime(), auxPropertyId,
                            expectedPropertyDefinition.getDefaultValue().get());
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
            c.subscribe(EventFilter.builder(GlobalPropertyDefinitionEvent.class).build(), (c2, e) -> {
                // record the actual observation
                MultiKey multiKey = new MultiKey(c2.getTime(), e.globalPropertyId(), e.initialPropertyValue());
                actualObservations.add(multiKey);
            });
        }));

        /*
         * Have the observer show the the expected and actual observations match after
         * all the new property definitions have been added.
         */
        double planTime = TestAuxiliaryGlobalPropertyId.values().length + 1;
        pluginDataBuilder.addTestActorPlan("observer", new TestActorPlan(planTime, (c) -> {
            assertEquals(expectedObservations, actualObservations);
        }));

        TestPluginData testPluginData = pluginDataBuilder.build();
        List<Plugin> plugins = GlobalPropertiesTestPluginFactory.factory(5100286389011347218L, testPluginData)
                .getPlugins();
        TestSimulation.builder().addPlugins(plugins).build().execute();

        // precondition test: if the global property initialization is null
        ContractException contractException = assertThrows(ContractException.class, () -> {
            Factory factory = GlobalPropertiesTestPluginFactory.factory(5100286389011347218L, (c) -> {
                GlobalPropertiesDataManager globalPropertiesDataManager = c
                        .getDataManager(GlobalPropertiesDataManager.class);
                globalPropertiesDataManager.defineGlobalProperty(null);
            });
            TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
        });
        assertEquals(GlobalPropertiesError.NULL_GLOBAL_PROPERTY_INITIALIZATION, contractException.getErrorType());

        // precondition test: if the global property already exists
        contractException = assertThrows(ContractException.class, () -> {

            Factory factory = GlobalPropertiesTestPluginFactory.factory(5100286389011347218L, (c) -> {
                GlobalPropertiesDataManager globalPropertiesDataManager = c
                        .getDataManager(GlobalPropertiesDataManager.class);
                GlobalPropertyId globalPropertyId = TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE;
                PropertyDefinition propertyDefinition = TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE
                        .getPropertyDefinition();
                GlobalPropertyInitialization globalPropertyInitialization = //
                        GlobalPropertyInitialization.builder()//
                                .setGlobalPropertyId(globalPropertyId)//
                                .setPropertyDefinition(propertyDefinition)//
                                .build();
                globalPropertiesDataManager.defineGlobalProperty(globalPropertyInitialization);
            });
            TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
        });
        assertEquals(PropertyError.DUPLICATE_PROPERTY_DEFINITION, contractException.getErrorType());

    }

    private static class LocalGlobalPropertyId implements GlobalPropertyId {
        private final int id;

        public LocalGlobalPropertyId(int id) {
            this.id = id;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + id;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof LocalGlobalPropertyId)) {
                return false;
            }
            LocalGlobalPropertyId other = (LocalGlobalPropertyId) obj;
            if (id != other.id) {
                return false;
            }
            return true;
        }

    }

    @Test
    @UnitTestMethod(target = GlobalPropertiesDataManager.class, name = "getEventFilterForGlobalPropertyDefinitionEvent", args = {})
    public void testGetEventFilterForGlobalPropertyDefinitionEvent() {
        TestPluginData.Builder pluginBuilder = TestPluginData.builder();

        Set<MultiKey> expectedObservations = new LinkedHashSet<>();
        Set<MultiKey> actualObservations = new LinkedHashSet<>();

        /*
         * have an observer subscribe to global property definition events
         */
        pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
            GlobalPropertiesDataManager globalPropertiesDataManager = c
                    .getDataManager(GlobalPropertiesDataManager.class);
            EventFilter<GlobalPropertyDefinitionEvent> eventFilter = globalPropertiesDataManager
                    .getEventFilterForGlobalPropertyDefinitionEvent();
            assertNotNull(eventFilter);
            c.subscribe(eventFilter, (c2, e) -> {
                actualObservations.add(new MultiKey(c.getTime(), e.globalPropertyId()));
            });

        }));

        /*
         * Have an actor add several new global property definitions at various times.
         */

        PropertyDefinition propertyDefinition = PropertyDefinition.builder()//
                .setType(Integer.class)//
                .setDefaultValue(0)//
                .build();
        IntStream.range(1, 4).forEach((i) -> {
            pluginBuilder.addTestActorPlan("actor", new TestActorPlan(i, (c) -> {
                GlobalPropertiesDataManager globalPropertiesDataManager = c
                        .getDataManager(GlobalPropertiesDataManager.class);
                GlobalPropertyId globalPropertyId = new LocalGlobalPropertyId(i);

                GlobalPropertyInitialization globalPropertyInitialization = //

                        GlobalPropertyInitialization.builder()//
                                .setGlobalPropertyId(globalPropertyId)//
                                .setPropertyDefinition(propertyDefinition)//
                                .build();
                globalPropertiesDataManager.defineGlobalProperty(globalPropertyInitialization);
                expectedObservations.add(new MultiKey(c.getTime(), globalPropertyId));

            }));
        });

        /*
         * have the observer show that the expected and actual observations are equal
         */
        pluginBuilder.addTestActorPlan("observer", new TestActorPlan(4, (c) -> {
            assertEquals(3, expectedObservations.size());
            assertEquals(expectedObservations, actualObservations);
        }));

        TestPluginData testPluginData = pluginBuilder.build();
        Factory factory = GlobalPropertiesTestPluginFactory.factory(5100286389011347218L, testPluginData);

        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

    }

    @Test
    @UnitTestMethod(target = GlobalPropertiesDataManager.class, name = "getEventFilterForGlobalPropertyUpdateEvent", args = {})
    public void testGetEventFilterForGlobalPropertyUpdateEvent() {

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5410948605660305794L);
        TestPluginData.Builder pluginBuilder = TestPluginData.builder();

        Set<MultiKey> expectedObservations = new LinkedHashSet<>();
        Set<MultiKey> actualObservations = new LinkedHashSet<>();

        /*
         * have an observer subscribe to global property update events
         */
        pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
            GlobalPropertiesDataManager globalPropertiesDataManager = c
                    .getDataManager(GlobalPropertiesDataManager.class);
            EventFilter<GlobalPropertyUpdateEvent> eventFilter = globalPropertiesDataManager
                    .getEventFilterForGlobalPropertyUpdateEvent();
            assertNotNull(eventFilter);
            c.subscribe(eventFilter, (c2, e) -> {
                actualObservations.add(new MultiKey(c.getTime(), e.globalPropertyId(), e.currentPropertyValue()));
            });

        }));

        /*
         * Have an actor update several global property values
         */
        IntStream.range(1, 4).forEach((i) -> {
            pluginBuilder.addTestActorPlan("actor", new TestActorPlan(i, (c) -> {
                GlobalPropertiesDataManager globalPropertiesDataManager = c
                        .getDataManager(GlobalPropertiesDataManager.class);

                TestGlobalPropertyId testGlobalPropertyId = TestGlobalPropertyId
                        .getRandomMutableGlobalPropertyId(randomGenerator);
                Object propertyValue = testGlobalPropertyId.getRandomPropertyValue(randomGenerator);
                globalPropertiesDataManager.setGlobalPropertyValue(testGlobalPropertyId, propertyValue);

                expectedObservations.add(new MultiKey(c.getTime(), testGlobalPropertyId, propertyValue));

            }));
        });

        /*
         * have the observer show that the expected and actual observations are equal
         */
        pluginBuilder.addTestActorPlan("observer", new TestActorPlan(4, (c) -> {
            assertEquals(3, expectedObservations.size());
            assertEquals(expectedObservations, actualObservations);
        }));

        TestPluginData testPluginData = pluginBuilder.build();
        Factory factory = GlobalPropertiesTestPluginFactory.factory(5100286389011347218L, testPluginData);
        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

    }

    @Test
    @UnitTestMethod(target = GlobalPropertiesDataManager.class, name = "getEventFilterForGlobalPropertyUpdateEvent", args = {
            GlobalPropertyId.class })
    public void testGetEventFilterForGlobalPropertyUpdateEvent_property() {

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2014699212749132531L);
        TestPluginData.Builder pluginBuilder = TestPluginData.builder();

        Set<MultiKey> expectedObservations = new LinkedHashSet<>();
        Set<MultiKey> actualObservations = new LinkedHashSet<>();

        /*
         * have an observer subscribe to two of the global property update events that
         * correspond to mutable properties
         */
        pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
            GlobalPropertiesDataManager globalPropertiesDataManager = c
                    .getDataManager(GlobalPropertiesDataManager.class);
            EventFilter<GlobalPropertyUpdateEvent> eventFilter = globalPropertiesDataManager
                    .getEventFilterForGlobalPropertyUpdateEvent(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE);
            assertNotNull(eventFilter);
            c.subscribe(eventFilter, (c2, e) -> {
                actualObservations.add(new MultiKey(c.getTime(), e.globalPropertyId(), e.currentPropertyValue()));
            });

            eventFilter = globalPropertiesDataManager
                    .getEventFilterForGlobalPropertyUpdateEvent(TestGlobalPropertyId.GLOBAL_PROPERTY_2_INTEGER_MUTABLE);
            assertNotNull(eventFilter);
            c.subscribe(eventFilter, (c2, e) -> {
                actualObservations.add(new MultiKey(c.getTime(), e.globalPropertyId(), e.currentPropertyValue()));
            });

        }));

        /*
         * Have an actor update all of the mutable global property values
         */
        IntStream.range(1, 4).forEach((i) -> {
            pluginBuilder.addTestActorPlan("actor", new TestActorPlan(i, (c) -> {
                GlobalPropertiesDataManager globalPropertiesDataManager = c
                        .getDataManager(GlobalPropertiesDataManager.class);

                TestGlobalPropertyId testGlobalPropertyId = TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE;
                Object propertyValue = testGlobalPropertyId.getRandomPropertyValue(randomGenerator);
                globalPropertiesDataManager.setGlobalPropertyValue(testGlobalPropertyId, propertyValue);
                expectedObservations.add(new MultiKey(c.getTime(), testGlobalPropertyId, propertyValue));

                testGlobalPropertyId = TestGlobalPropertyId.GLOBAL_PROPERTY_2_INTEGER_MUTABLE;
                propertyValue = testGlobalPropertyId.getRandomPropertyValue(randomGenerator);
                globalPropertiesDataManager.setGlobalPropertyValue(testGlobalPropertyId, propertyValue);
                expectedObservations.add(new MultiKey(c.getTime(), testGlobalPropertyId, propertyValue));

                testGlobalPropertyId = TestGlobalPropertyId.GLOBAL_PROPERTY_3_DOUBLE_MUTABLE;
                propertyValue = testGlobalPropertyId.getRandomPropertyValue(randomGenerator);
                globalPropertiesDataManager.setGlobalPropertyValue(testGlobalPropertyId, propertyValue);
                // not that we do not add an expected value here

            }));
        });

        /*
         * have the observer show that the expected and actual observations are equal
         */
        pluginBuilder.addTestActorPlan("observer", new TestActorPlan(4, (c) -> {
            assertEquals(6, expectedObservations.size());
            assertEquals(expectedObservations, actualObservations);
        }));

        TestPluginData testPluginData = pluginBuilder.build();
        Factory factory = GlobalPropertiesTestPluginFactory.factory(5100286389011347218L, testPluginData);
        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

        // precondition test: if the global property id is null
        ContractException contractException = assertThrows(ContractException.class, () -> {
            Factory factory2 = GlobalPropertiesTestPluginFactory.factory(5100286389011347218L, (c) -> {
                GlobalPropertiesDataManager globalPropertiesDataManager = c
                        .getDataManager(GlobalPropertiesDataManager.class);
                GlobalPropertyId globalPropertyId = null;
                globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyId, new Object());
            });
            TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
        });
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

        // precondition test: if the global property id is not known
        contractException = assertThrows(ContractException.class, () -> {
            Factory factory2 = GlobalPropertiesTestPluginFactory.factory(5100286389011347218L, (c) -> {
                GlobalPropertiesDataManager globalPropertiesDataManager = c
                        .getDataManager(GlobalPropertiesDataManager.class);
                GlobalPropertyId globalPropertyId = TestGlobalPropertyId.getUnknownGlobalPropertyId();
                globalPropertiesDataManager.setGlobalPropertyValue(globalPropertyId, new Object());
            });
            TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
        });
        assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GlobalPropertiesDataManager.class, name = "getGlobalPropertyDefinitionTime", args = {
            GlobalPropertyId.class })
    public void testGetGlobalPropertyDefinitionTime() {
        Factory factory = GlobalPropertiesTestPluginFactory.factory(5100286389011347218L, (c) -> {
            GlobalPropertiesDataManager globalPropertiesDataManager = c
                    .getDataManager(GlobalPropertiesDataManager.class);

            for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
                assertEquals(0.0, globalPropertiesDataManager.getGlobalPropertyDefinitionTime(testGlobalPropertyId));
            }
        });

        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

        // precondition : if the global property id is null
        ContractException contractException = assertThrows(ContractException.class, () -> {
            Factory factory2 = GlobalPropertiesTestPluginFactory.factory(5100286389011347218L, (c) -> {
                GlobalPropertiesDataManager globalPropertiesDataManager = c
                        .getDataManager(GlobalPropertiesDataManager.class);
                globalPropertiesDataManager.getGlobalPropertyDefinitionTime(null);
            });
            TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
        });
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

        // precondition : if the global property id is unknown
        contractException = assertThrows(ContractException.class, () -> {
            Factory factory2 = GlobalPropertiesTestPluginFactory.factory(5100286389011347218L, (c) -> {
                GlobalPropertiesDataManager globalPropertiesDataManager = c
                        .getDataManager(GlobalPropertiesDataManager.class);
                globalPropertiesDataManager
                        .getGlobalPropertyDefinitionTime(TestGlobalPropertyId.getUnknownGlobalPropertyId());
            });
            TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
        });
        assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GlobalPropertiesDataManager.class, name = "toString", args = {})
    public void testToString() {
        Map<GlobalPropertyId, PropertyDefinition> globalPropertyDefinitions = new LinkedHashMap<>();
        Map<GlobalPropertyId, Object> globalPropertyValues = new LinkedHashMap<>();
        Map<GlobalPropertyId, Double> globalPropertyDefinitionTimes = new LinkedHashMap<>();
        Map<GlobalPropertyId, Double> globalPropertyTimes = new LinkedHashMap<>();

        GlobalPropertiesPluginData.Builder globalsPluginBuilder = GlobalPropertiesPluginData.builder();
        long seed = 5100286389011347218L;
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

        for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
            PropertyDefinition propertyDefinition = testGlobalPropertyId.getPropertyDefinition();
            globalsPluginBuilder.defineGlobalProperty(testGlobalPropertyId, propertyDefinition, 0);

            globalPropertyDefinitions.put(testGlobalPropertyId, propertyDefinition);
            globalPropertyDefinitionTimes.put(testGlobalPropertyId, 0.0);

            if (propertyDefinition.getDefaultValue().isEmpty()) {
                Object value = testGlobalPropertyId.getRandomPropertyValue(randomGenerator);
                globalsPluginBuilder.setGlobalPropertyValue(testGlobalPropertyId, value, 0);
                globalPropertyValues.put(testGlobalPropertyId, value);
                globalPropertyTimes.put(testGlobalPropertyId, 0.0);
            }
        }
        // change two of the properties from their default values
        globalsPluginBuilder.setGlobalPropertyValue(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE, true, 0);
        globalPropertyValues.put(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE, true);
        globalPropertyTimes.put(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE, 0.0);

        globalsPluginBuilder.setGlobalPropertyValue(TestGlobalPropertyId.GLOBAL_PROPERTY_2_INTEGER_MUTABLE, 456, 0);
        globalPropertyValues.put(TestGlobalPropertyId.GLOBAL_PROPERTY_2_INTEGER_MUTABLE, 456);
        globalPropertyTimes.put(TestGlobalPropertyId.GLOBAL_PROPERTY_2_INTEGER_MUTABLE, 0.0);

        GlobalPropertiesPluginData globalPropertiesPluginData = globalsPluginBuilder.build();

        TestPluginData.Builder testPluginDataBuilder = TestPluginData.builder();

        testPluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
            // show that the data manager exists
            GlobalPropertiesDataManager globalPropertiesDataManager = c
                    .getDataManager(GlobalPropertiesDataManager.class);

            StringBuilder builder = new StringBuilder();
            builder.append("GlobalPropertiesDataManager [globalPropertyDefinitions=");
            builder.append(globalPropertyDefinitions);
            builder.append(", globalPropertyDefinitionTimes=");
            builder.append(globalPropertyDefinitionTimes);
            builder.append(", globalPropertyValues=");
            builder.append(globalPropertyValues);
            builder.append(", globalPropertyTimes=");
            builder.append(globalPropertyTimes);
            builder.append("]");

            assertEquals(builder.toString(), globalPropertiesDataManager.toString());
        }));

        TestPluginData testPluginData = testPluginDataBuilder.build();
        Factory factory = GlobalPropertiesTestPluginFactory.factory(seed, testPluginData)
                .setGlobalPropertiesPluginData(globalPropertiesPluginData);
        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

    }
}
