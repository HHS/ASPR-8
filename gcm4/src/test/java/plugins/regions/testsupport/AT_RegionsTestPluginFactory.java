package plugins.regions.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import nucleus.NucleusError;
import nucleus.Plugin;
import nucleus.PluginData;
import nucleus.PluginId;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestPluginId;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.people.PeoplePluginData;
import plugins.people.PeoplePluginId;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.people.support.PersonRange;
import plugins.regions.RegionsPluginData;
import plugins.regions.RegionsPluginId;
import plugins.regions.support.RegionError;
import plugins.regions.testsupport.RegionsTestPluginFactory.Factory;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.StochasticsPluginId;
import plugins.stochastics.support.StochasticsError;
import plugins.stochastics.support.WellState;
import plugins.util.properties.PropertyDefinition;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;
import util.wrappers.MutableBoolean;

public class AT_RegionsTestPluginFactory {

    @Test
    @UnitTestMethod(target = RegionsTestPluginFactory.class, name = "factory", args = { int.class, long.class,
            boolean.class, Consumer.class })
    public void testFactory_Consumer() {
        MutableBoolean executed = new MutableBoolean();
        Factory factory = RegionsTestPluginFactory
                .factory(100, 5785172948650781925L, true, c -> executed.setValue(true));

        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
        assertTrue(executed.getValue());

        // precondition: consumer is null
        Consumer<ActorContext> nullConsumer = null;
        ContractException contractException = assertThrows(ContractException.class,
                () -> RegionsTestPluginFactory.factory(0, 0, true, nullConsumer));
        assertEquals(NucleusError.NULL_ACTOR_CONTEXT_CONSUMER, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = RegionsTestPluginFactory.class, name = "factory", args = { int.class, long.class,
            boolean.class, TestPluginData.class })
    public void testFactory_TestPluginData() {
        MutableBoolean executed = new MutableBoolean();
        TestPluginData.Builder pluginBuilder = TestPluginData.builder();
        pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, c -> executed.setValue(true)));
        TestPluginData testPluginData = pluginBuilder.build();

        Factory factory = RegionsTestPluginFactory
                .factory(100, 5166994853007999229L, true, testPluginData);
        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
        assertTrue(executed.getValue());
        // precondition: testPluginData is null
        TestPluginData nullTestPluginData = null;
        ContractException contractException = assertThrows(ContractException.class,
                () -> RegionsTestPluginFactory.factory(0, 0, true, nullTestPluginData));
        assertEquals(NucleusError.NULL_PLUGIN_DATA, contractException.getErrorType());
    }

    /*
     * Given a list of plugins, will show that the plugin with the given pluginId
     * exists, and exists EXACTLY once.
     */
    private Plugin checkPluginExists(List<Plugin> plugins, PluginId pluginId) {
        Plugin actualPlugin = null;
        for (Plugin plugin : plugins) {
            if (plugin.getPluginId().equals(pluginId)) {
                assertNull(actualPlugin);
                actualPlugin = plugin;
            }
        }

        assertNotNull(actualPlugin);

        return actualPlugin;
    }

    /**
     * Given a list of plugins, will show that the explicit plugindata for the given
     * pluginid exists, and exists EXACTLY once.
     */
    private <T extends PluginData> void checkPluginDataExists(List<Plugin> plugins, T expectedPluginData,
            PluginId pluginId) {
        Plugin actualPlugin = checkPluginExists(plugins, pluginId);
        List<PluginData> actualPluginDatas = actualPlugin.getPluginDatas();
        assertNotNull(actualPluginDatas);
        assertEquals(1, actualPluginDatas.size());
        PluginData actualPluginData = actualPluginDatas.get(0);
        assertTrue(expectedPluginData == actualPluginData);
    }

    @Test
    @UnitTestMethod(target = RegionsTestPluginFactory.Factory.class, name = "getPlugins", args = {})
    public void testGetPlugins() {
        List<Plugin> plugins = RegionsTestPluginFactory.factory(0, 0, true, t -> {
        }).getPlugins();
        assertEquals(4, plugins.size());

        checkPluginExists(plugins, RegionsPluginId.PLUGIN_ID);
        checkPluginExists(plugins, PeoplePluginId.PLUGIN_ID);
        checkPluginExists(plugins, StochasticsPluginId.PLUGIN_ID);
        checkPluginExists(plugins, TestPluginId.PLUGIN_ID);
    }

    @Test
    @UnitTestMethod(target = RegionsTestPluginFactory.Factory.class, name = "setPeoplePluginData", args = {
            PeoplePluginData.class })
    public void testSetPeoplePluginData() {
        PeoplePluginData.Builder builder = PeoplePluginData.builder();
        builder.addPersonRange(new PersonRange(0, 99));
        PeoplePluginData peoplePluginData = builder.build();

        List<Plugin> plugins = RegionsTestPluginFactory
                .factory(0, 0, true, t -> {
                })
                .setPeoplePluginData(peoplePluginData)
                .getPlugins();

        checkPluginDataExists(plugins, peoplePluginData, PeoplePluginId.PLUGIN_ID);

        // precondition: peoplePluginData is not null
        ContractException contractException = assertThrows(ContractException.class,
                () -> RegionsTestPluginFactory
                        .factory(0, 0, true, t -> {
                        })
                        .setPeoplePluginData(null));
        assertEquals(PersonError.NULL_PEOPLE_PLUGIN_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = RegionsTestPluginFactory.Factory.class, name = "setRegionsPluginData", args = {
            RegionsPluginData.class })
    public void testSetRegionsPluginData() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3111731594673998076L);
        int initialPopulation = 30;
        List<PersonId> people = new ArrayList<>();
        for (int i = 0; i < initialPopulation; i++) {
            people.add(new PersonId(i));
        }

        // add the region plugin
        RegionsPluginData.Builder regionPluginBuilder = RegionsPluginData.builder();
        for (TestRegionId regionId : TestRegionId.values()) {
            regionPluginBuilder.addRegion(regionId);
        }

        for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
            regionPluginBuilder.defineRegionProperty(testRegionPropertyId,
                    testRegionPropertyId.getPropertyDefinition());
        }

        for (TestRegionId regionId : TestRegionId.values()) {
            for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
                if (testRegionPropertyId.getPropertyDefinition().getDefaultValue().isEmpty()
                        || randomGenerator.nextBoolean()) {
                    Object randomPropertyValue = testRegionPropertyId.getRandomPropertyValue(randomGenerator);
                    regionPluginBuilder.setRegionPropertyValue(regionId, testRegionPropertyId, randomPropertyValue);
                }
            }
        }

        TestRegionId testRegionId = TestRegionId.REGION_1;
        for (PersonId personId : people) {
            regionPluginBuilder.addPerson(personId, testRegionId, 0.0);
            testRegionId = testRegionId.next();
        }

        regionPluginBuilder.setPersonRegionArrivalTracking(true);

        RegionsPluginData regionsPluginData = regionPluginBuilder.build();

        List<Plugin> plugins = RegionsTestPluginFactory.factory(0, 0, true, t -> {
        }).setRegionsPluginData(regionsPluginData).getPlugins();

        checkPluginDataExists(plugins, regionsPluginData, RegionsPluginId.PLUGIN_ID);

        // precondition: regionsPluginData is not null
        ContractException contractException = assertThrows(ContractException.class,
                () -> RegionsTestPluginFactory.factory(0, 0, true, t -> {
                }).setRegionsPluginData(null));
        assertEquals(RegionError.NULL_REGION_PLUGIN_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = RegionsTestPluginFactory.Factory.class, name = "setStochasticsPluginData", args = {
            StochasticsPluginData.class })
    public void testSetStochasticsPluginData() {
        StochasticsPluginData.Builder builder = StochasticsPluginData.builder();

        WellState wellState = WellState.builder().setSeed(1286485118818778304L).build();
        builder.setMainRNGState(wellState);

        StochasticsPluginData stochasticsPluginData = builder.build();

        List<Plugin> plugins = RegionsTestPluginFactory
                .factory(0, 0, true, t -> {
                })
                .setStochasticsPluginData(stochasticsPluginData)
                .getPlugins();

        checkPluginDataExists(plugins, stochasticsPluginData, StochasticsPluginId.PLUGIN_ID);

        // precondition: stochasticsPluginData is not null
        ContractException contractException = assertThrows(ContractException.class,
                () -> RegionsTestPluginFactory
                        .factory(0, 0, true, t -> {
                        })
                        .setStochasticsPluginData(null));
        assertEquals(StochasticsError.NULL_STOCHASTICS_PLUGIN_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = RegionsTestPluginFactory.class, name = "getStandardPeoplePluginData", args = {
            int.class })
    public void testGetStandardPeoplePluginData() {

        int initialPopulation = 100;

        PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();
        if (initialPopulation > 0) {
            peopleBuilder.addPersonRange(new PersonRange(0, initialPopulation - 1));
        }

        PeoplePluginData expectedPluginData = peopleBuilder.build();
        PeoplePluginData actualPluginData = RegionsTestPluginFactory.getStandardPeoplePluginData(initialPopulation);

        assertEquals(expectedPluginData, actualPluginData);
    }

    @Test
    @UnitTestMethod(target = RegionsTestPluginFactory.class, name = "getStandardRegionsPluginData", args = {
            List.class, boolean.class, long.class })
    public void testGetStandardRegionsPluginData() {

        long seed = 6178540698301704248L;
        int initialPopulation = 100;
        boolean trackTimes = true;
        List<PersonId> people = new ArrayList<>();
        for (int i = 0; i < initialPopulation; i++) {
            people.add(new PersonId(i));
        }

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

        RegionsPluginData.Builder regionPluginBuilder = RegionsPluginData.builder();
        for (TestRegionId regionId : TestRegionId.values()) {
            regionPluginBuilder.addRegion(regionId);
        }

        for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
            PropertyDefinition propertyDefinition = testRegionPropertyId.getPropertyDefinition();
            regionPluginBuilder.defineRegionProperty(testRegionPropertyId, propertyDefinition);
            if (propertyDefinition.getDefaultValue().isEmpty()) {
                for (TestRegionId regionId : TestRegionId.values()) {
                    regionPluginBuilder.setRegionPropertyValue(regionId, testRegionPropertyId,
                            testRegionPropertyId.getRandomPropertyValue(randomGenerator));
                }
            }

        }

        regionPluginBuilder.setPersonRegionArrivalTracking(trackTimes);
        TestRegionId testRegionId = TestRegionId.REGION_1;
        if (trackTimes) {
            for (PersonId personId : people) {
                regionPluginBuilder.addPerson(personId, testRegionId, 0.0);
                testRegionId = testRegionId.next();
            }
        } else {
            for (PersonId personId : people) {
                regionPluginBuilder.addPerson(personId, testRegionId);
                testRegionId = testRegionId.next();
            }
        }

        RegionsPluginData expectedPluginData = regionPluginBuilder.build();
        RegionsPluginData actualPluginData = RegionsTestPluginFactory.getStandardRegionsPluginData(people,
                trackTimes,
                seed);

        assertEquals(expectedPluginData, actualPluginData);
    }

    @Test
    @UnitTestMethod(target = RegionsTestPluginFactory.class, name = "getStandardStochasticsPluginData", args = {
            long.class })
    public void testGetStandardStochasticsPluginData() {
        long seed = 8184805053177550601L;

        WellState wellState = WellState.builder().setSeed(seed).build();

        StochasticsPluginData expectedPluginData = StochasticsPluginData.builder().setMainRNGState(wellState).build();
        StochasticsPluginData actualPluginData = RegionsTestPluginFactory
                .getStandardStochasticsPluginData(seed);

        assertEquals(expectedPluginData, actualPluginData);
    }
}
