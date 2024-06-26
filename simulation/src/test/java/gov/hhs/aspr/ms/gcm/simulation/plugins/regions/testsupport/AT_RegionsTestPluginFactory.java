package gov.hhs.aspr.ms.gcm.simulation.plugins.regions.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.TestFactoryUtil;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestActorPlan;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestPluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestPluginId;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.PeoplePluginId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.datamanagers.PeoplePluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonRange;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.RegionsPluginId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.datamanagers.RegionsPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.reports.RegionPropertyReportPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.reports.RegionTransferReportPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support.RegionError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.testsupport.RegionsTestPluginFactory.Factory;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportPeriod;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.SimpleReportLabel;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.StochasticsPluginId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.datamanagers.StochasticsPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.support.StochasticsError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.support.WellState;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;
import gov.hhs.aspr.ms.util.wrappers.MutableBoolean;

public class AT_RegionsTestPluginFactory {

    @Test
    @UnitTestMethod(target = RegionsTestPluginFactory.class, name = "factory", args = { int.class, long.class,
            boolean.class, Consumer.class })
    public void testFactory_Consumer() {
        MutableBoolean executed = new MutableBoolean();
        Factory factory = RegionsTestPluginFactory.factory(100, 5785172948650781925L, true,
                c -> executed.setValue(true));

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

        Factory factory = RegionsTestPluginFactory.factory(100, 5166994853007999229L, true, testPluginData);
        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
        assertTrue(executed.getValue());
        // precondition: testPluginData is null
        TestPluginData nullTestPluginData = null;
        ContractException contractException = assertThrows(ContractException.class,
                () -> RegionsTestPluginFactory.factory(0, 0, true, nullTestPluginData));
        assertEquals(NucleusError.NULL_PLUGIN_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = RegionsTestPluginFactory.Factory.class, name = "getPlugins", args = {})
    public void testGetPlugins() {
        List<Plugin> plugins = RegionsTestPluginFactory.factory(0, 0, true, t -> {
        }).getPlugins();
        assertEquals(4, plugins.size());

        TestFactoryUtil.checkPluginExists(plugins, RegionsPluginId.PLUGIN_ID);
        TestFactoryUtil.checkPluginExists(plugins, PeoplePluginId.PLUGIN_ID);
        TestFactoryUtil.checkPluginExists(plugins, StochasticsPluginId.PLUGIN_ID);
        TestFactoryUtil.checkPluginExists(plugins, TestPluginId.PLUGIN_ID);
    }

    @Test
    @UnitTestMethod(target = RegionsTestPluginFactory.Factory.class, name = "setPeoplePluginData", args = {
            PeoplePluginData.class })
    public void testSetPeoplePluginData() {
        PeoplePluginData.Builder builder = PeoplePluginData.builder();
        builder.addPersonRange(new PersonRange(0, 99));
        PeoplePluginData peoplePluginData = builder.build();

        List<Plugin> plugins = RegionsTestPluginFactory.factory(0, 0, true, t -> {
        }).setPeoplePluginData(peoplePluginData).getPlugins();

        TestFactoryUtil.checkPluginDataExists(plugins, peoplePluginData, PeoplePluginId.PLUGIN_ID);

        // precondition: peoplePluginData is not null
        ContractException contractException = assertThrows(ContractException.class,
                () -> RegionsTestPluginFactory.factory(0, 0, true, t -> {
                }).setPeoplePluginData(null));
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

        TestFactoryUtil.checkPluginDataExists(plugins, regionsPluginData, RegionsPluginId.PLUGIN_ID);

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

        List<Plugin> plugins = RegionsTestPluginFactory.factory(0, 0, true, t -> {
        }).setStochasticsPluginData(stochasticsPluginData).getPlugins();

        TestFactoryUtil.checkPluginDataExists(plugins, stochasticsPluginData, StochasticsPluginId.PLUGIN_ID);

        // precondition: stochasticsPluginData is not null
        ContractException contractException = assertThrows(ContractException.class,
                () -> RegionsTestPluginFactory.factory(0, 0, true, t -> {
                }).setStochasticsPluginData(null));
        assertEquals(StochasticsError.NULL_STOCHASTICS_PLUGIN_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = RegionsTestPluginFactory.class, name = "getStandardPeoplePluginData", args = { int.class })
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
    @UnitTestMethod(target = RegionsTestPluginFactory.class, name = "getStandardRegionsPluginData", args = { List.class,
            boolean.class, long.class })
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

        for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.getTestRegionPropertyIds()) {
            PropertyDefinition propertyDefinition = testRegionPropertyId.getPropertyDefinition();
            regionPluginBuilder.defineRegionProperty(testRegionPropertyId, propertyDefinition);
            boolean hasDeaultValue = propertyDefinition.getDefaultValue().isPresent();

            if (!hasDeaultValue) {
                for (TestRegionId regionId : TestRegionId.values()) {
                    regionPluginBuilder.setRegionPropertyValue(regionId, testRegionPropertyId,
                            testRegionPropertyId.getRandomPropertyValue(randomGenerator));
                }
            }
        }

        for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId
                .getTestShuffledRegionPropertyIds(randomGenerator)) {
            PropertyDefinition propertyDefinition = testRegionPropertyId.getPropertyDefinition();
            boolean hasDeaultValue = propertyDefinition.getDefaultValue().isPresent();
            boolean setValue = randomGenerator.nextBoolean();

            if (hasDeaultValue && setValue) {
                for (TestRegionId regionId : TestRegionId.values()) {
                    regionPluginBuilder.setRegionPropertyValue(regionId, testRegionPropertyId,
                            propertyDefinition.getDefaultValue().get());
                }
            } else if (setValue) {
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
        RegionsPluginData actualPluginData = RegionsTestPluginFactory.getStandardRegionsPluginData(people, trackTimes,
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
        StochasticsPluginData actualPluginData = RegionsTestPluginFactory.getStandardStochasticsPluginData(seed);

        assertEquals(expectedPluginData, actualPluginData);
    }
    
    @Test
    @UnitTestMethod(target = RegionsTestPluginFactory.Factory.class, name = "setRegionPropertyReportPluginData", args = {
    		RegionPropertyReportPluginData.class })
    public void testSetRegionPropertyReportPluginData() {
    	RegionPropertyReportPluginData regionPropertyReportPluginData = RegionPropertyReportPluginData.builder()//
    	.setReportLabel(new SimpleReportLabel("RegionPropertyReport"))//
    	.setDefaultInclusion(true)//
    	.includeRegionProperty(TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE)//
    	.excludeRegionProperty(TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE)//
    	.build();//

        Factory factory = RegionsTestPluginFactory.factory(0, 0, true, t -> { });
        factory.setRegionPropertyReportPluginData(regionPropertyReportPluginData);
        List<Plugin> plugins = factory.getPlugins();
        
        TestFactoryUtil.checkPluginDataExists(plugins, regionPropertyReportPluginData, RegionsPluginId.PLUGIN_ID);
    }
    
    @Test
    @UnitTestMethod(target = RegionsTestPluginFactory.Factory.class, name = "setRegionTransferReportPluginData", args = {
    		RegionTransferReportPluginData.class })
    public void testSetRegionTransferReportPluginData() {
    	RegionTransferReportPluginData regionTransferReportPluginData = RegionTransferReportPluginData.builder()//
    	.setReportLabel(new SimpleReportLabel("RegionTransferReport"))
    	.setReportPeriod(ReportPeriod.DAILY)
    	.build();

        Factory factory = RegionsTestPluginFactory.factory(0, 0, true, t -> { });
        factory.setRegionTransferReportPluginData(regionTransferReportPluginData);
        List<Plugin> plugins = factory.getPlugins();
        
        TestFactoryUtil.checkPluginDataExists(plugins, regionTransferReportPluginData, RegionsPluginId.PLUGIN_ID);

        
    }
}
