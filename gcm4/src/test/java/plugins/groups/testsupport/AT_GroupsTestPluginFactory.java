package plugins.groups.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import nucleus.ActorContext;
import nucleus.NucleusError;
import nucleus.Plugin;
import nucleus.PluginData;
import nucleus.PluginId;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestPluginId;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.groups.GroupsPluginId;
import plugins.groups.datamanagers.GroupsPluginData;
import plugins.groups.support.GroupError;
import plugins.groups.support.GroupId;
import plugins.groups.testsupport.GroupsTestPluginFactory.Factory;
import plugins.people.PeoplePluginData;
import plugins.people.PeoplePluginId;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.people.support.PersonRange;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.StochasticsPluginId;
import plugins.stochastics.support.StochasticsError;
import plugins.stochastics.support.WellState;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;
import util.wrappers.MultiKey;
import util.wrappers.MutableBoolean;

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
    @UnitTestMethod(target = GroupsTestPluginFactory.Factory.class, name = "getPlugins", args = {})
    public void testGetPlugins() {
        List<Plugin> plugins = GroupsTestPluginFactory.factory(0, 0, 0, 0, t -> {
        }).getPlugins();
        assertEquals(4, plugins.size());

        checkPluginExists(plugins, GroupsPluginId.PLUGIN_ID);
        checkPluginExists(plugins, PeoplePluginId.PLUGIN_ID);
        checkPluginExists(plugins, StochasticsPluginId.PLUGIN_ID);
        checkPluginExists(plugins, TestPluginId.PLUGIN_ID);
    }

    @Test
    @UnitTestMethod(target = GroupsTestPluginFactory.Factory.class, name = "setGroupsPluginData", args = {
            GroupsPluginData.class })
    public void testSetGroupsPluginData() {
        GroupsPluginData.Builder builder = GroupsPluginData.builder();

        builder.addGroup(new GroupId(0), TestGroupTypeId.GROUP_TYPE_1)
                .addGroup(new GroupId(1), TestGroupTypeId.GROUP_TYPE_2)
                .addGroup(new GroupId(2), TestGroupTypeId.GROUP_TYPE_1)
                .addGroupTypeId(TestGroupTypeId.GROUP_TYPE_1)
                .addGroupTypeId(TestGroupTypeId.GROUP_TYPE_2);

        GroupsPluginData groupsPluginData = builder.build();

        List<Plugin> plugins = GroupsTestPluginFactory
                .factory(0, 0, 0, 0, t -> {
                })
                .setGroupsPluginData(groupsPluginData)
                .getPlugins();

        checkPluginDataExists(plugins, groupsPluginData, GroupsPluginId.PLUGIN_ID);

        // precondition: groupsPluginData is not null
        ContractException contractException = assertThrows(ContractException.class,
                () -> GroupsTestPluginFactory
                        .factory(0, 0, 0, 0, t -> {
                        })
                        .setGroupsPluginData(null));
        assertEquals(GroupError.NULL_GROUP_PLUGIN_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GroupsTestPluginFactory.Factory.class, name = "setPeoplePluginData", args = {
            PeoplePluginData.class })
    public void testSetPeoplePluginData() {
        PeoplePluginData.Builder builder = PeoplePluginData.builder();
        builder.addPersonRange(new PersonRange(0, 99));
        PeoplePluginData peoplePluginData = builder.build();

        List<Plugin> plugins = GroupsTestPluginFactory
                .factory(0, 0, 0, 0, t -> {
                })
                .setPeoplePluginData(peoplePluginData)
                .getPlugins();

        checkPluginDataExists(plugins, peoplePluginData, PeoplePluginId.PLUGIN_ID);

        // precondition: peoplePluginData is not null
        ContractException contractException = assertThrows(ContractException.class,
                () -> GroupsTestPluginFactory
                        .factory(0, 0, 0, 0, t -> {
                        })
                        .setPeoplePluginData(null));
        assertEquals(PersonError.NULL_PEOPLE_PLUGIN_DATA, contractException.getErrorType());
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

        List<Plugin> plugins = GroupsTestPluginFactory
                .factory(0, 0, 0, 0, t -> {
                })
                .setStochasticsPluginData(stochasticsPluginData)
                .getPlugins();

        checkPluginDataExists(plugins, stochasticsPluginData, StochasticsPluginId.PLUGIN_ID);

        // precondition: stochasticsPluginData is not null
        ContractException contractException = assertThrows(ContractException.class,
                () -> GroupsTestPluginFactory
                        .factory(0, 0, 0, 0, t -> {
                        })
                        .setStochasticsPluginData(null));
        assertEquals(StochasticsError.NULL_STOCHASTICS_PLUGIN_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GroupsTestPluginFactory.class, name = "getStandardGroupsPluginData", args = { int.class,
            int.class, List.class, long.class })
    public void testGetStandardGroupsPluginData() {

        long seed = 6442469165497328184L;
        int initialPopulation = 100;
        int expectedGroupsPerPerson = 3;
        int expectedPeoplePerGroup = 5;

        int membershipCount = (int) FastMath.round(initialPopulation * expectedGroupsPerPerson);
        int groupCount = (int) FastMath.round(membershipCount / expectedPeoplePerGroup);

        List<PersonId> people = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            people.add(new PersonId(i));
        }

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
                    .getTestGroupPropertyIds(testGroupTypeId)) {
                groupBuilder.setGroupPropertyValue(groupId, testGroupPropertyId,
                        testGroupPropertyId.getRandomPropertyValue(randomGenerator));
            }
            testGroupTypeId = testGroupTypeId.next();
        }

        Set<MultiKey> groupMemeberships = new LinkedHashSet<>();
        while (groupMemeberships.size() < membershipCount) {
            PersonId personId = people.get(randomGenerator.nextInt(people.size()));
            GroupId groupId = groups.get(randomGenerator.nextInt(groups.size()));
            groupMemeberships.add(new MultiKey(groupId, personId));
        }

        for (MultiKey multiKey : groupMemeberships) {
            GroupId groupId = multiKey.getKey(0);
            PersonId personId = multiKey.getKey(1);
            groupBuilder.associatePersonToGroup(groupId, personId);
        }

        GroupsPluginData expectedPluginData = groupBuilder.build();

        GroupsPluginData actualPluginData = GroupsTestPluginFactory.getStandardGroupsPluginData(groupCount,
                membershipCount, people, seed);

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
        StochasticsPluginData actualPluginData = GroupsTestPluginFactory
                .getStandardStochasticsPluginData(seed);

        assertEquals(expectedPluginData, actualPluginData);
    }

}
