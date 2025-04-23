package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.datamanagers.GroupsDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.events.GroupMembershipAdditionEvent;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.events.GroupMembershipRemovalEvent;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.testsupport.GroupsTestPluginFactory;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.testsupport.GroupsTestPluginFactory.Factory;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.testsupport.TestGroupTypeId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.FilterSensitivity;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.PartitionsContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.filters.Filter;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.TestPartitionsContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.datamanagers.StochasticsDataManager;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_GroupMemberFilter {

    @Test
    @UnitTestConstructor(target = GroupMemberFilter.class, args = { GroupId.class })
    public void testConstructor() {
        Factory factory = GroupsTestPluginFactory.factory(100, 3, 10, 8499169041100865476L, (c) -> {

            TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);

            GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
            List<GroupId> groupIds = groupsDataManager.getGroupIds();
            assertFalse(groupIds.isEmpty());
            for (GroupId groupId : groupIds) {
                final Filter filter = new GroupMemberFilter(groupId);
                assertNotNull(filter);
            }

            // precondition tests
            ContractException contractException = assertThrows(ContractException.class,
                    () -> new GroupMemberFilter(null).validate(testPartitionsContext));
            assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());
        });

        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
    }

    @Test
    @UnitTestMethod(target = GroupMemberFilter.class, name = "getFilterSensitivities", args = {})
    public void testGetFilterSensitivities() {

        Factory factory = GroupsTestPluginFactory.factory(100, 3, 10, 7283631979607042406L, (c) -> {
            GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);

            GroupId groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);

            Filter filter = new GroupMemberFilter(groupId);

            Set<Class<?>> expected = new LinkedHashSet<>();
            expected.add(GroupMembershipAdditionEvent.class);
            expected.add(GroupMembershipRemovalEvent.class);

            Set<FilterSensitivity<?>> filterSensitivities = filter.getFilterSensitivities();
            assertNotNull(filterSensitivities);
            assertEquals(filterSensitivities.size(), 2);

            Set<Class<?>> actual = new LinkedHashSet<>();
            for (FilterSensitivity<?> filterSensitivity : filterSensitivities) {
                Class<?> eventClass = filterSensitivity.getEventClass();
                actual.add(eventClass);
            }
            assertEquals(expected, actual);

        });

        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

    }

    @Test
    @UnitTestMethod(target = GroupMemberFilter.class, name = "evaluate", args = { PartitionsContext.class,
            PersonId.class })
    public void testEvaluate() {

        Factory factory = GroupsTestPluginFactory.factory(100, 3, 10, 6248106595116941770L, (c) -> {

            TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);

            RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();
            PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
            GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
            GroupId groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_3);
            Filter filter = new GroupMemberFilter(groupId);

            for (PersonId personId : peopleDataManager.getPeople()) {
                if (randomGenerator.nextBoolean()) {
                    groupsDataManager.addPersonToGroup(personId, groupId);
                }
            }

            for (PersonId personId : peopleDataManager.getPeople()) {
                boolean expected = groupsDataManager.isPersonInGroup(personId, groupId);
                boolean actual = filter.evaluate(testPartitionsContext, personId);
                assertEquals(expected, actual);
            }

            /* precondition: if the person id is null */
            ContractException contractException = assertThrows(ContractException.class,
                    () -> filter.evaluate(testPartitionsContext, null));
            assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

            /* precondition: if the person id is unknown */
            contractException = assertThrows(ContractException.class,
                    () -> filter.evaluate(testPartitionsContext, new PersonId(123412342)));
            assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
        });

        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
    }

    @Test
    @UnitTestMethod(target = GroupMemberFilter.class, name = "validate", args = { PartitionsContext.class })
    public void testValidate() {

        Factory factory = GroupsTestPluginFactory.factory(100, 3, 10, 8525809821136960274L, (c) -> {

            TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);

            GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
            GroupId groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_3);
            Filter filter = new GroupMemberFilter(groupId);

            // show that a properly defined filter validates and does not throw
            assertDoesNotThrow(() -> filter.validate(testPartitionsContext));

            /* precondition: if the groupId is null */
            ContractException contractException = assertThrows(ContractException.class,
                    () -> new GroupMemberFilter(null).validate(testPartitionsContext));
            assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

        });

        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
    }

    @Test
    @UnitTestMethod(target = GroupMemberFilter.class, name = "getGroupId", args = {})
    public void testGetGroupId() {
        for (int i = 0; i < 10; i++) {
            GroupId groupId = new GroupId(i);
            GroupMemberFilter groupMemberFilter = new GroupMemberFilter(groupId);
            assertEquals(groupId, groupMemberFilter.getGroupId());
        }
    }

    @Test
    @UnitTestMethod(target = GroupMemberFilter.class, name = "hashCode", args = {})
    public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(386190996593528301L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			GroupMemberFilter groupMemberFilter1 = getRandomGroupMemberFilter(seed);
			GroupMemberFilter groupMemberFilter2 = getRandomGroupMemberFilter(seed);

			assertEquals(groupMemberFilter1, groupMemberFilter2);
			assertEquals(groupMemberFilter1.hashCode(), groupMemberFilter2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			GroupMemberFilter pluginData = getRandomGroupMemberFilter(randomGenerator.nextLong());
			hashCodes.add(pluginData.hashCode());
		}

		assertEquals(100, hashCodes.size());
    }

    @Test
    @UnitTestMethod(target = GroupMemberFilter.class, name = "equals", args = { Object.class })
    public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1973382207275712576L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			GroupMemberFilter groupMemberFilter = getRandomGroupMemberFilter(randomGenerator.nextLong());
			assertFalse(groupMemberFilter.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			GroupMemberFilter groupMemberFilter = getRandomGroupMemberFilter(randomGenerator.nextLong());
			assertFalse(groupMemberFilter.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			GroupMemberFilter groupMemberFilter = getRandomGroupMemberFilter(randomGenerator.nextLong());
			assertTrue(groupMemberFilter.equals(groupMemberFilter));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			GroupMemberFilter groupMemberFilter1 = getRandomGroupMemberFilter(seed);
			GroupMemberFilter groupMemberFilter2 = getRandomGroupMemberFilter(seed);
			assertFalse(groupMemberFilter1 == groupMemberFilter2);
			for (int j = 0; j < 10; j++) {
				assertTrue(groupMemberFilter1.equals(groupMemberFilter2));
				assertTrue(groupMemberFilter2.equals(groupMemberFilter1));
			}
		}

		// different inputs yield unequal groupMemberFilters
		Set<GroupMemberFilter> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			GroupMemberFilter groupMemberFilter = getRandomGroupMemberFilter(randomGenerator.nextLong());
			set.add(groupMemberFilter);
		}
		assertEquals(100, set.size());
    }

    @Test
    @UnitTestMethod(target = GroupMemberFilter.class, name = "toString", args = {})
    public void testToString() {
        for (int i = 0; i < 10; i++) {
            GroupId groupId = new GroupId(i);
            GroupMemberFilter groupMemberFilter = new GroupMemberFilter(groupId);

            StringBuilder builder = new StringBuilder();
            builder.append("GroupMemberFilter [groupId=").append(groupId).append("]");

            assertEquals(builder.toString(), groupMemberFilter.toString());
        }
    }

    private GroupMemberFilter getRandomGroupMemberFilter(long seed) {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
        GroupId randomGroupId = new GroupId(randomGenerator.nextInt(Integer.MAX_VALUE));
        return new GroupMemberFilter(randomGroupId);
    }
}
