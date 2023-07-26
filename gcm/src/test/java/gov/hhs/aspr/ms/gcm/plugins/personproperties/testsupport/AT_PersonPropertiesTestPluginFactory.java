package gov.hhs.aspr.ms.gcm.plugins.personproperties.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.TestFactoryUtil;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestActorPlan;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestPluginData;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestPluginId;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.plugins.people.PeoplePluginId;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeoplePluginData;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonRange;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.PersonPropertiesPluginId;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.datamanagers.PersonPropertiesPluginData;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.reports.PersonPropertyInteractionReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.reports.PersonPropertyReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.support.PersonPropertyError;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.testsupport.PersonPropertiesTestPluginFactory.Factory;
import gov.hhs.aspr.ms.gcm.plugins.regions.RegionsPluginId;
import gov.hhs.aspr.ms.gcm.plugins.regions.datamanagers.RegionsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionError;
import gov.hhs.aspr.ms.gcm.plugins.regions.testsupport.TestRegionId;
import gov.hhs.aspr.ms.gcm.plugins.regions.testsupport.TestRegionPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportPeriod;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.SimpleReportLabel;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.StochasticsPluginId;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.StochasticsError;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.WellState;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;
import util.wrappers.MutableBoolean;

public class AT_PersonPropertiesTestPluginFactory {

    @Test
    @UnitTestMethod(target = PersonPropertiesTestPluginFactory.class, name = "factory", args = { int.class, long.class,
            Consumer.class })
    public void testFactory_Consumer() {
        MutableBoolean executed = new MutableBoolean();
        Factory factory = PersonPropertiesTestPluginFactory.factory(100, 4135374341935235561L,
                c -> executed.setValue(true));
        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
        assertTrue(executed.getValue());

        // precondition: consumer is null
        Consumer<ActorContext> nullConsumer = null;
        ContractException contractException = assertThrows(ContractException.class,
                () -> PersonPropertiesTestPluginFactory.factory(0, 0, nullConsumer));
        assertEquals(NucleusError.NULL_ACTOR_CONTEXT_CONSUMER, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = PersonPropertiesTestPluginFactory.class, name = "factory", args = { int.class, long.class,
            TestPluginData.class })
    public void testFactory_TestPluginData() {
        MutableBoolean executed = new MutableBoolean();
        TestPluginData.Builder pluginBuilder = TestPluginData.builder();
        pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, c -> executed.setValue(true)));
        TestPluginData testPluginData = pluginBuilder.build();

        Factory factory = PersonPropertiesTestPluginFactory.factory(100, 92376779979686632L, testPluginData);
        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

        assertTrue(executed.getValue());

        // precondition: testPluginData is null
        TestPluginData nullTestPluginData = null;
        ContractException contractException = assertThrows(ContractException.class,
                () -> PersonPropertiesTestPluginFactory.factory(0, 0, nullTestPluginData));
        assertEquals(NucleusError.NULL_PLUGIN_DATA, contractException.getErrorType());

    }

    @Test
    @UnitTestMethod(target = PersonPropertiesTestPluginFactory.Factory.class, name = "getPlugins", args = {})
    public void testGetPlugins() {
        List<Plugin> plugins = PersonPropertiesTestPluginFactory.factory(0, 0, t -> {
        }).getPlugins();
        assertEquals(5, plugins.size());

        TestFactoryUtil.checkPluginExists(plugins, PersonPropertiesPluginId.PLUGIN_ID);
        TestFactoryUtil.checkPluginExists(plugins, RegionsPluginId.PLUGIN_ID);
        TestFactoryUtil.checkPluginExists(plugins, PeoplePluginId.PLUGIN_ID);
        TestFactoryUtil.checkPluginExists(plugins, StochasticsPluginId.PLUGIN_ID);
        TestFactoryUtil.checkPluginExists(plugins, TestPluginId.PLUGIN_ID);
    }

    @Test
    @UnitTestMethod(target = PersonPropertiesTestPluginFactory.Factory.class, name = "setPersonPropertiesPluginData", args = {
            PersonPropertiesPluginData.class })
    public void testSetPersonPropertiesPluginData() {

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2764277826547948301L);
        PersonPropertiesPluginData.Builder personPropertyBuilder = PersonPropertiesPluginData.builder();

        List<PersonId> people = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            people.add(new PersonId(i));
        }

        for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
            personPropertyBuilder.definePersonProperty(testPersonPropertyId,
                    testPersonPropertyId.getPropertyDefinition(), 0.0, testPersonPropertyId.isTimeTracked());
        }
        for (PersonId personId : people) {
            for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
                boolean doesNotHaveDefaultValue = testPersonPropertyId.getPropertyDefinition().getDefaultValue()
                        .isEmpty();
                if (doesNotHaveDefaultValue || randomGenerator.nextBoolean()) {
                    Object randomPropertyValue = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
                    personPropertyBuilder.setPersonPropertyValue(personId, testPersonPropertyId, randomPropertyValue);
                }
            }
        }

        PersonPropertiesPluginData personPropertiesPluginData = personPropertyBuilder.build();

        List<Plugin> plugins = PersonPropertiesTestPluginFactory.factory(0, 0, t -> {
        }).setPersonPropertiesPluginData(personPropertiesPluginData).getPlugins();

        TestFactoryUtil.checkPluginDataExists(plugins, personPropertiesPluginData, PersonPropertiesPluginId.PLUGIN_ID);

        // precondition: personPropertiesPluginData is not null
        ContractException contractException = assertThrows(ContractException.class,
                () -> PersonPropertiesTestPluginFactory.factory(0, 0, t -> {
                }).setPersonPropertiesPluginData(null));
        assertEquals(PersonPropertyError.NULL_PERSON_PROPERTY_PLUGN_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = PersonPropertiesTestPluginFactory.Factory.class, name = "setPeoplePluginData", args = {
            PeoplePluginData.class })
    public void testSetPeoplePluginData() {
        PeoplePluginData.Builder builder = PeoplePluginData.builder();
        builder.addPersonRange(new PersonRange(0, 99));

        PeoplePluginData peoplePluginData = builder.build();

        List<Plugin> plugins = PersonPropertiesTestPluginFactory.factory(0, 0, t -> {
        }).setPeoplePluginData(peoplePluginData).getPlugins();

        TestFactoryUtil.checkPluginDataExists(plugins, peoplePluginData, PeoplePluginId.PLUGIN_ID);

        // precondition: peoplePluginData is not null
        ContractException contractException = assertThrows(ContractException.class,
                () -> PersonPropertiesTestPluginFactory.factory(0, 0, t -> {
                }).setPeoplePluginData(null));
        assertEquals(PersonError.NULL_PEOPLE_PLUGIN_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = PersonPropertiesTestPluginFactory.Factory.class, name = "setRegionsPluginData", args = {
            RegionsPluginData.class })
    public void testSetRegionsPluginData() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3277720775100432241L);
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

        List<Plugin> plugins = PersonPropertiesTestPluginFactory.factory(0, 0, t -> {
        }).setRegionsPluginData(regionsPluginData).getPlugins();

        TestFactoryUtil.checkPluginDataExists(plugins, regionsPluginData, RegionsPluginId.PLUGIN_ID);

        // precondition: regionsPluginData is not null
        ContractException contractException = assertThrows(ContractException.class,
                () -> PersonPropertiesTestPluginFactory.factory(0, 0, t -> {
                }).setRegionsPluginData(null));
        assertEquals(RegionError.NULL_REGION_PLUGIN_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = PersonPropertiesTestPluginFactory.Factory.class, name = "setStochasticsPluginData", args = {
            StochasticsPluginData.class })
    public void testSetStochasticsPluginData() {
        StochasticsPluginData.Builder builder = StochasticsPluginData.builder();

        WellState wellState = WellState.builder().setSeed(2758378374654665699L).build();
        builder.setMainRNGState(wellState);

        StochasticsPluginData stochasticsPluginData = builder.build();

        List<Plugin> plugins = PersonPropertiesTestPluginFactory.factory(0, 0, t -> {
        }).setStochasticsPluginData(stochasticsPluginData).getPlugins();

        TestFactoryUtil.checkPluginDataExists(plugins, stochasticsPluginData, StochasticsPluginId.PLUGIN_ID);

        // precondition: stochasticsPluginData is not null
        ContractException contractException = assertThrows(ContractException.class,
                () -> PersonPropertiesTestPluginFactory.factory(0, 0, t -> {
                }).setStochasticsPluginData(null));
        assertEquals(StochasticsError.NULL_STOCHASTICS_PLUGIN_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = PersonPropertiesTestPluginFactory.class, name = "getStandardPersonPropertiesPluginData", args = {
            List.class, long.class, double[].class })
    public void testGetStandardPersonPropertiesPluginData() {

        long seed = 4684903523797799712L;
        double[] propertyTime = new double[0];
        List<PersonId> people = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            people.add(new PersonId(i));
        }

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

        double actualPropertyTime = 0;
        if (propertyTime.length > 0) {
            actualPropertyTime = propertyTime[0];
        }

        PersonPropertiesPluginData.Builder personPropertyBuilder = PersonPropertiesPluginData.builder();

        for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
            personPropertyBuilder.definePersonProperty(testPersonPropertyId,
                    testPersonPropertyId.getPropertyDefinition(), 0.0, testPersonPropertyId.isTimeTracked());
        }

        for (PersonId personId : people) {

            for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId
                    .getShuffledPersonPropertyIds(randomGenerator)) {

                boolean hasDefault = testPersonPropertyId.getPropertyDefinition().getDefaultValue().isPresent();
                boolean setValue = randomGenerator.nextBoolean();

                if (!hasDefault || setValue) {
                    Object randomPropertyValue = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
                    personPropertyBuilder.setPersonPropertyValue(personId, testPersonPropertyId, randomPropertyValue);
                } else if (hasDefault && personId.getValue() % 5 == 0) {
                    personPropertyBuilder.setPersonPropertyValue(personId, testPersonPropertyId,
                            testPersonPropertyId.getPropertyDefinition().getDefaultValue().get());
                }

                if (testPersonPropertyId.isTimeTracked() && personId.getValue() % 5 == 0) {
                    personPropertyBuilder.setPersonPropertyTime(personId, testPersonPropertyId, actualPropertyTime);
                }
            }
        }

        PersonPropertiesPluginData expectedPluginData = personPropertyBuilder.build();
        PersonPropertiesPluginData actualPluginData = PersonPropertiesTestPluginFactory
                .getStandardPersonPropertiesPluginData(people, seed);

        assertEquals(expectedPluginData, actualPluginData);
    }

    @Test
    @UnitTestMethod(target = PersonPropertiesTestPluginFactory.class, name = "getStandardPeoplePluginData", args = {
            int.class })
    public void testGetStandardPeoplePluginData() {

        int initialPopulation = 100;

        PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();
        if (initialPopulation > 0) {
            peopleBuilder.addPersonRange(new PersonRange(0, initialPopulation - 1));
        }

        PeoplePluginData expectedPluginData = peopleBuilder.build();
        PeoplePluginData actualPluginData = PersonPropertiesTestPluginFactory
                .getStandardPeoplePluginData(initialPopulation);

        assertEquals(expectedPluginData, actualPluginData);
    }

    @Test
    @UnitTestMethod(target = PersonPropertiesTestPluginFactory.class, name = "getStandardRegionsPluginData", args = {
            List.class, long.class })
    public void testGetStandardRegionsPluginData() {

        long seed = 2729857981015931439L;
        int initialPopulation = 100;
        List<PersonId> people = new ArrayList<>();
        for (int i = 0; i < initialPopulation; i++) {
            people.add(new PersonId(i));
        }

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

        RegionsPluginData.Builder regionBuilder = RegionsPluginData.builder();
        // add the regions
        for (TestRegionId testRegionId : TestRegionId.values()) {
            regionBuilder.addRegion(testRegionId);
        }
        for (PersonId personId : people) {
            TestRegionId randomRegionId = TestRegionId.getRandomRegionId(randomGenerator);
            regionBuilder.addPerson(personId, randomRegionId);
        }

        RegionsPluginData expectedPluginData = regionBuilder.build();
        RegionsPluginData actualPluginData = PersonPropertiesTestPluginFactory.getStandardRegionsPluginData(people,
                seed);

        assertEquals(expectedPluginData, actualPluginData);
    }

    @Test
    @UnitTestMethod(target = PersonPropertiesTestPluginFactory.class, name = "getStandardStochasticsPluginData", args = {
            long.class })
    public void testGetStandardStochasticsPluginData() {
        long seed = 6072871729256538807L;

        WellState wellState = WellState.builder().setSeed(seed).build();

        StochasticsPluginData expectedPluginData = StochasticsPluginData.builder().setMainRNGState(wellState).build();
        StochasticsPluginData actualPluginData = PersonPropertiesTestPluginFactory
                .getStandardStochasticsPluginData(seed);

        assertEquals(expectedPluginData, actualPluginData);
    }
 

    @Test
    @UnitTestMethod(target = PersonPropertiesTestPluginFactory.Factory.class, name = "setPersonPropertyReportPluginData", args = {
    		PersonPropertyReportPluginData.class })
    public void testSetsetPersonPropertyReportPluginData() {
    	PersonPropertyReportPluginData personPropertyReportPluginData = PersonPropertyReportPluginData.builder()//
    	.setReportLabel(new SimpleReportLabel("report label"))//
    	.setReportPeriod(ReportPeriod.DAILY)//
    	.build();

        List<Plugin> plugins = PersonPropertiesTestPluginFactory.factory(0, 0, t -> {
        }).setPersonPropertyReportPluginData(personPropertyReportPluginData).getPlugins();

        TestFactoryUtil.checkPluginDataExists(plugins, personPropertyReportPluginData, PersonPropertiesPluginId.PLUGIN_ID);
    }

    
    @Test
    @UnitTestMethod(target = PersonPropertiesTestPluginFactory.Factory.class, name = "setPersonPropertyInteractionReportPluginData", args = {
    		PersonPropertyInteractionReportPluginData.class })
    public void testSetPersonPropertyInteractionReportPluginData() {
    	PersonPropertyInteractionReportPluginData personPropertyInteractionReportPluginData = PersonPropertyInteractionReportPluginData.builder()//
    	.addPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK)//
    	.addPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_5_INTEGER_MUTABLE_TRACK)//
    	.setReportLabel(new SimpleReportLabel("report label"))//
    	.setReportPeriod(ReportPeriod.DAILY)//
    	.build();

        List<Plugin> plugins = PersonPropertiesTestPluginFactory.factory(0, 0, t -> {
        }).setPersonPropertyInteractionReportPluginData(personPropertyInteractionReportPluginData).getPlugins();

        TestFactoryUtil.checkPluginDataExists(plugins, personPropertyInteractionReportPluginData, PersonPropertiesPluginId.PLUGIN_ID);
    }
    
    
    
}
