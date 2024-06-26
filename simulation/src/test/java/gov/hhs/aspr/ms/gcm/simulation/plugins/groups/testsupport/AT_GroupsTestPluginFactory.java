package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.TestFactoryUtil;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestActorPlan;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestPluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestPluginId;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.GroupsPluginId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.datamanagers.GroupsPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.reports.GroupPopulationReportPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.reports.GroupPropertyReportPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support.GroupError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support.GroupId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.testsupport.GroupsTestPluginFactory.Factory;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.PeoplePluginId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.datamanagers.PeoplePluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonRange;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportPeriod;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.SimpleReportLabel;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.StochasticsPluginId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.datamanagers.StochasticsPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.support.StochasticsError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.support.WellState;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.testsupport.TestRandomGeneratorId;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;
import gov.hhs.aspr.ms.util.wrappers.MultiKey;
import gov.hhs.aspr.ms.util.wrappers.MutableBoolean;

@Testable
public class AT_GroupsTestPluginFactory {

    @Test
    @UnitTestMethod(target = GroupsTestPluginFactory.class, name = "factory", args = { int.class, double.class,
            double.class, long.class, Consumer.class })
    public void testFactory_Consumer() {
        MutableBoolean executed = new MutableBoolean();
        Factory factory = GroupsTestPluginFactory.factory(100, 3, 5, 3765548905828391577L,
                c -> executed.setValue(true));
        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

        assertTrue(executed.getValue());

        // precondition: consumer is null
        Consumer<ActorContext> nullConsumer = null;
        ContractException contractException = assertThrows(ContractException.class,
                () -> GroupsTestPluginFactory.factory(0, 0, 0, 0, nullConsumer));
        assertEquals(NucleusError.NULL_ACTOR_CONTEXT_CONSUMER, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GroupsTestPluginFactory.class, name = "factory", args = { int.class, double.class,
            double.class, long.class, TestPluginData.class })
    public void testFactory_TestPluginData() {
        MutableBoolean executed = new MutableBoolean();
        TestPluginData.Builder pluginBuilder = TestPluginData.builder();
        pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, c -> executed.setValue(true)));
        TestPluginData testPluginData = pluginBuilder.build();

        Factory factory = GroupsTestPluginFactory.factory(100, 3, 5, 1937810385546394605L, testPluginData);
        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

        assertTrue(executed.getValue());

        // precondition: testPluginData is null
        TestPluginData nullTestPluginData = null;
        ContractException contractException = assertThrows(ContractException.class,
                () -> GroupsTestPluginFactory.factory(0, 0, 0, 0, nullTestPluginData));
        assertEquals(NucleusError.NULL_PLUGIN_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GroupsTestPluginFactory.Factory.class, name = "getPlugins", args = {})
    public void testGetPlugins() {
        List<Plugin> plugins = GroupsTestPluginFactory.factory(0, 0, 0, 0, t -> {
        }).getPlugins();
        assertEquals(4, plugins.size());

        TestFactoryUtil.checkPluginExists(plugins, GroupsPluginId.PLUGIN_ID);
        TestFactoryUtil.checkPluginExists(plugins, PeoplePluginId.PLUGIN_ID);
        TestFactoryUtil.checkPluginExists(plugins, StochasticsPluginId.PLUGIN_ID);
        TestFactoryUtil.checkPluginExists(plugins, TestPluginId.PLUGIN_ID);
    }

    @Test
    @UnitTestMethod(target = GroupsTestPluginFactory.Factory.class, name = "setGroupsPluginData", args = {
            GroupsPluginData.class })
    public void testSetGroupsPluginData() {
        GroupsPluginData.Builder builder = GroupsPluginData.builder();

        builder.addGroup(new GroupId(0), TestGroupTypeId.GROUP_TYPE_1)
                .addGroup(new GroupId(1), TestGroupTypeId.GROUP_TYPE_2)
                .addGroup(new GroupId(2), TestGroupTypeId.GROUP_TYPE_1).addGroupTypeId(TestGroupTypeId.GROUP_TYPE_1)
                .addGroupTypeId(TestGroupTypeId.GROUP_TYPE_2);

        GroupsPluginData groupsPluginData = builder.build();

        List<Plugin> plugins = GroupsTestPluginFactory.factory(0, 0, 0, 0, t -> {
        }).setGroupsPluginData(groupsPluginData).getPlugins();

        TestFactoryUtil.checkPluginDataExists(plugins, groupsPluginData, GroupsPluginId.PLUGIN_ID);

        // precondition: groupsPluginData is not null
        ContractException contractException = assertThrows(ContractException.class,
                () -> GroupsTestPluginFactory.factory(0, 0, 0, 0, t -> {
                }).setGroupsPluginData(null));
        assertEquals(GroupError.NULL_GROUP_PLUGIN_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GroupsTestPluginFactory.Factory.class, name = "setPeoplePluginData", args = {
            PeoplePluginData.class })
    public void testSetPeoplePluginData() {
        PeoplePluginData.Builder builder = PeoplePluginData.builder();
        builder.addPersonRange(new PersonRange(0, 99));
        PeoplePluginData peoplePluginData = builder.build();

        List<Plugin> plugins = GroupsTestPluginFactory.factory(0, 0, 0, 0, t -> {
        }).setPeoplePluginData(peoplePluginData).getPlugins();

        TestFactoryUtil.checkPluginDataExists(plugins, peoplePluginData, PeoplePluginId.PLUGIN_ID);

        // precondition: peoplePluginData is not null
        ContractException contractException = assertThrows(ContractException.class,
                () -> GroupsTestPluginFactory.factory(0, 0, 0, 0, t -> {
                }).setPeoplePluginData(null));
        assertEquals(PersonError.NULL_PEOPLE_PLUGIN_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GroupsTestPluginFactory.Factory.class, name = "setGroupPropertyReportPluginData", args = {
        GroupPropertyReportPluginData.class })
    public void testSetGroupPropertyReportPluginData() {
        GroupPropertyReportPluginData.Builder builder = GroupPropertyReportPluginData.builder()
                .setReportPeriod(ReportPeriod.DAILY).setReportLabel(new SimpleReportLabel("test"));

        boolean include = false;
        for (TestGroupPropertyId groupPropertyId : TestGroupPropertyId.values()) {
            if (include) {
                builder.includeGroupProperty(TestGroupTypeId.GROUP_TYPE_1, groupPropertyId);
            } else {
                builder.excludeGroupProperty(TestGroupTypeId.GROUP_TYPE_1, groupPropertyId);
            }

            include = !include;
        }

        GroupPropertyReportPluginData groupPropertyReportPluginData = builder.build();

        List<Plugin> plugins = GroupsTestPluginFactory.factory(0, 0, 0, 0, t -> {
        }).setGroupPropertyReportPluginData(groupPropertyReportPluginData).getPlugins();

        TestFactoryUtil.checkPluginDataExists(plugins, groupPropertyReportPluginData, GroupsPluginId.PLUGIN_ID);
        
        // precondition: peoplePluginData is not null
        ContractException contractException = assertThrows(ContractException.class,
                () -> GroupsTestPluginFactory.factory(0, 0, 0, 0, t -> {
                }).setGroupPropertyReportPluginData(null));
        assertEquals(GroupError.NULL_GROUP_PROPERTY_REPORT_PLUGIN_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GroupsTestPluginFactory.Factory.class, name = "setGroupPopulationReportPluginData", args = {
        GroupPopulationReportPluginData.class })
    public void testSetGroupPopulationReportPluginData() {
        GroupPopulationReportPluginData.Builder builder = GroupPopulationReportPluginData.builder()
                .setReportPeriod(ReportPeriod.DAILY).setReportLabel(new SimpleReportLabel("test"));

                GroupPopulationReportPluginData groupPopulationReportPluginData = builder.build();

        List<Plugin> plugins = GroupsTestPluginFactory.factory(0, 0, 0, 0, t -> {
        }).setGroupPopulationReportPluginData(groupPopulationReportPluginData).getPlugins();

        TestFactoryUtil.checkPluginDataExists(plugins, groupPopulationReportPluginData, GroupsPluginId.PLUGIN_ID);
        
        // precondition: peoplePluginData is not null
        ContractException contractException = assertThrows(ContractException.class,
                () -> GroupsTestPluginFactory.factory(0, 0, 0, 0, t -> {
                }).setGroupPopulationReportPluginData(null));
        assertEquals(GroupError.NULL_GROUP_POPULATION_REPORT_PLUGIN_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GroupsTestPluginFactory.Factory.class, name = "setStochasticsPluginData", args = {
            StochasticsPluginData.class })
    public void testSetStochasticsPluginData() {
        StochasticsPluginData.Builder builder = StochasticsPluginData.builder();
        WellState wellState = WellState.builder().setSeed(8478739978811865148L).build();
        builder.setMainRNGState(wellState);
        wellState = WellState.builder().setSeed(1336318114409771694L).build();
        builder.addRNG(TestRandomGeneratorId.BLITZEN, wellState);

        StochasticsPluginData stochasticsPluginData = builder.build();

        List<Plugin> plugins = GroupsTestPluginFactory.factory(0, 0, 0, 0, t -> {
        }).setStochasticsPluginData(stochasticsPluginData).getPlugins();

        TestFactoryUtil.checkPluginDataExists(plugins, stochasticsPluginData, StochasticsPluginId.PLUGIN_ID);

        // precondition: stochasticsPluginData is not null
        ContractException contractException = assertThrows(ContractException.class,
                () -> GroupsTestPluginFactory.factory(0, 0, 0, 0, t -> {
                }).setStochasticsPluginData(null));
        assertEquals(StochasticsError.NULL_STOCHASTICS_PLUGIN_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GroupsTestPluginFactory.class, name = "getStandardGroupsPluginData", args = { double.class,
            double.class, List.class, long.class })
    public void testGetStandardGroupsPluginData() {

        long seed = 6442469165497328184L;
        int initialPopulation = 100;
        int expectedGroupsPerPerson = 3;
        int expectedPeoplePerGroup = 5;

        List<PersonId> people = new ArrayList<>();
        for (int i = 0; i < initialPopulation; i++) {
            people.add(new PersonId(i));
        }
        int membershipCount = (int) FastMath.round(people.size() * expectedGroupsPerPerson);
        int groupCount = (int) FastMath.round(membershipCount / expectedPeoplePerGroup);
        membershipCount = FastMath.min(membershipCount, groupCount * people.size());

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
        GroupsPluginData.Builder groupBuilder = GroupsPluginData.builder();

        for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
            groupBuilder.addGroupTypeId(testGroupTypeId);
        }

        for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
            groupBuilder.defineGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId,
                    testGroupPropertyId.getPropertyDefinition());
        }

        List<GroupId> groups = new ArrayList<>();

        TestGroupTypeId testGroupTypeId = TestGroupTypeId.GROUP_TYPE_1;
        for (int i = 0; i < groupCount; i++) {
            GroupId groupId = new GroupId(i);
            groups.add(groupId);
            groupBuilder.addGroup(groupId, testGroupTypeId);
            for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId
                    .getShuffledTestGroupPropertyIds(testGroupTypeId, randomGenerator)) {
                PropertyDefinition propertyDefinition = testGroupPropertyId.getPropertyDefinition();
                boolean hasDefaultValue = propertyDefinition.getDefaultValue().isPresent();
                boolean setValue = randomGenerator.nextBoolean();

                if (!hasDefaultValue || setValue) {
                    groupBuilder.setGroupPropertyValue(groupId, testGroupPropertyId,
                            testGroupPropertyId.getRandomPropertyValue(randomGenerator));
                }
            }
            testGroupTypeId = testGroupTypeId.next();
        }

        List<MultiKey> groupMemeberships = new ArrayList<>();
        for (PersonId personId : people) {
            for (GroupId groupId : groups) {
                groupMemeberships.add(new MultiKey(groupId, personId));
            }
        }
        Collections.shuffle(groupMemeberships, new Random(randomGenerator.nextLong()));

        for (int i = 0; i < membershipCount; i++) {
            MultiKey multiKey = groupMemeberships.get(i);
            GroupId groupId = multiKey.getKey(0);
            PersonId personId = multiKey.getKey(1);
            groupBuilder.associatePersonToGroup(groupId, personId);
        }
        GroupsPluginData expectedPluginData = groupBuilder.build();

        GroupsPluginData actualPluginData = GroupsTestPluginFactory.getStandardGroupsPluginData(expectedGroupsPerPerson,
                expectedPeoplePerGroup, people, seed);

        assertEquals(expectedPluginData, actualPluginData);
    }

    @Test
    @UnitTestMethod(target = GroupsTestPluginFactory.class, name = "getStandardPeoplePluginData", args = { int.class })
    public void testGetStandardPeoplePluginData() {

        int initialPopulation = 100;

        PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();

        if (initialPopulation > 0) {
            peopleBuilder.addPersonRange(new PersonRange(0, initialPopulation - 1));
        }

        PeoplePluginData expectedPluginData = peopleBuilder.build();
        PeoplePluginData actualPluginData = GroupsTestPluginFactory.getStandardPeoplePluginData(initialPopulation);

        assertEquals(expectedPluginData, actualPluginData);
    }

    @Test
    @UnitTestMethod(target = GroupsTestPluginFactory.class, name = "getStandardStochasticsPluginData", args = {
            long.class })
    public void testGetStandardStochasticsPluginData() {
        long seed = 6072871729256538807L;
        WellState wellState = WellState.builder().setSeed(seed).build();

        StochasticsPluginData expectedPluginData = StochasticsPluginData.builder().setMainRNGState(wellState).build();
        StochasticsPluginData actualPluginData = GroupsTestPluginFactory.getStandardStochasticsPluginData(seed);

        assertEquals(expectedPluginData, actualPluginData);
    }

}
