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
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.Equality;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.FilterSensitivity;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.PartitionError;
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

public class AT_GroupsForPersonAndGroupTypeFilter {

    @Test
    @UnitTestConstructor(target = GroupsForPersonAndGroupTypeFilter.class, args = { GroupTypeId.class, Equality.class,
            int.class })
    public void testConstructor() {

        Factory factory = GroupsTestPluginFactory.factory(100, 3, 10, 5854778167265102928L, (c) -> {

            TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);

            final Filter filter = new GroupsForPersonAndGroupTypeFilter(TestGroupTypeId.GROUP_TYPE_1, Equality.EQUAL,
                    5);
            assertNotNull(filter);

            // precondition tests

            // if the group type id is null
            ContractException contractException = assertThrows(ContractException.class,
                    () -> new GroupsForPersonAndGroupTypeFilter(null, Equality.EQUAL, 5)
                            .validate(testPartitionsContext));
            assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

            // if the equality operator is null
            contractException = assertThrows(ContractException.class,
                    () -> new GroupsForPersonAndGroupTypeFilter(TestGroupTypeId.GROUP_TYPE_1, null, 5)
                            .validate(testPartitionsContext));
            assertEquals(PartitionError.NULL_EQUALITY_OPERATOR, contractException.getErrorType());

        });

        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
    }

    @Test
    @UnitTestMethod(target = GroupsForPersonAndGroupTypeFilter.class, name = "getFilterSensitivities", args = {})
    public void testGetFilterSensitivities() {

        Factory factory = GroupsTestPluginFactory.factory(100, 3, 10, 1469082977858605268L, (c) -> {
            Filter filter = new GroupsForPersonAndGroupTypeFilter(TestGroupTypeId.GROUP_TYPE_1, Equality.EQUAL, 5);

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
    @UnitTestMethod(target = GroupsForPersonAndGroupTypeFilter.class, name = "evaluate", args = {
            PartitionsContext.class, PersonId.class })
    public void testEvaluate() {

        Factory factory = GroupsTestPluginFactory.factory(100, 0, 10, 4592268926831796100L, (c) -> {

            TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);

            RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();
            GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
            PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
            List<PersonId> people = peopleDataManager.getPeople();

            GroupId groupId1 = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
            GroupId groupId2 = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
            GroupId groupId3 = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);

            Filter filter = new GroupsForPersonAndGroupTypeFilter(TestGroupTypeId.GROUP_TYPE_1, Equality.EQUAL, 2);

            assertEquals(100, people.size());

            for (PersonId personId : people) {
                int groupCount = randomGenerator.nextInt(4);
                switch (groupCount) {
                case 0:
                    break;
                case 1:
                    groupsDataManager.addPersonToGroup(personId, groupId1);
                    break;
                case 2:
                    groupsDataManager.addPersonToGroup(personId, groupId1);
                    groupsDataManager.addPersonToGroup(personId, groupId2);
                    break;
                default:
                    groupsDataManager.addPersonToGroup(personId, groupId1);
                    groupsDataManager.addPersonToGroup(personId, groupId2);
                    groupsDataManager.addPersonToGroup(personId, groupId3);
                    break;
                }

            }

            for (PersonId personId : people) {
                boolean expected = groupsDataManager.getGroupCountForGroupTypeAndPerson(TestGroupTypeId.GROUP_TYPE_1,
                        personId) == 2;
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
    @UnitTestMethod(target = GroupsForPersonAndGroupTypeFilter.class, name = "validate", args = {
            PartitionsContext.class })
    public void testValidate() {
        Factory factory = GroupsTestPluginFactory.factory(100, 0, 10, 3710154078488599088L, (c) -> {

            TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);

            GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);

            groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
            Filter filter = new GroupsForPersonAndGroupTypeFilter(TestGroupTypeId.GROUP_TYPE_1, Equality.EQUAL, 1);

            // show filter is valid when group type is valid and equality is
            // valid
            assertDoesNotThrow(() -> filter.validate(testPartitionsContext));

            // precondition: equality is null
            ContractException contractException = assertThrows(ContractException.class,
                    () -> new GroupsForPersonAndGroupTypeFilter(TestGroupTypeId.GROUP_TYPE_1, null, 2)
                            .validate(testPartitionsContext));
            assertEquals(PartitionError.NULL_EQUALITY_OPERATOR, contractException.getErrorType());

            // precondition: group type id is null
            contractException = assertThrows(ContractException.class,
                    () -> new GroupsForPersonAndGroupTypeFilter(null, Equality.EQUAL, 2)
                            .validate(testPartitionsContext));
            assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

            // precondition: group type id is unknown
            contractException = assertThrows(ContractException.class,
                    () -> new GroupsForPersonAndGroupTypeFilter(TestGroupTypeId.getUnknownGroupTypeId(), Equality.EQUAL,
                            2).validate(testPartitionsContext));
            assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

        });

        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
    }

    @Test
    @UnitTestMethod(target = GroupsForPersonAndGroupTypeFilter.class, name = "getEquality", args = {})
    public void testGetEquality() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8284890603319105493L);

        for (int i = 0; i < 10; i++) {
            Equality equality = Equality.getRandomEquality(randomGenerator);
            int groupCount = randomGenerator.nextInt(10);
            GroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
            GroupsForPersonAndGroupTypeFilter filter = new GroupsForPersonAndGroupTypeFilter(groupTypeId, equality,
                    groupCount);

            assertEquals(equality, filter.getEquality());
        }
    }

    @Test
    @UnitTestMethod(target = GroupsForPersonAndGroupTypeFilter.class, name = "getGroupCount", args = {})
    public void testGetGroupCount() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5425834828181361952L);

        for (int i = 0; i < 10; i++) {
            Equality equality = Equality.getRandomEquality(randomGenerator);
            int groupCount = randomGenerator.nextInt(10);
            GroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
            GroupsForPersonAndGroupTypeFilter filter = new GroupsForPersonAndGroupTypeFilter(groupTypeId, equality,
                    groupCount);

            assertEquals(groupCount, filter.getGroupCount());
        }
    }

    @Test
    @UnitTestMethod(target = GroupsForPersonAndGroupTypeFilter.class, name = "getGroupTypeId", args = {})
    public void testGetGroupTypeId() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6110751755905020120L);

        for (int i = 0; i < 10; i++) {
            Equality equality = Equality.getRandomEquality(randomGenerator);
            int groupCount = randomGenerator.nextInt(10);
            GroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
            GroupsForPersonAndGroupTypeFilter filter = new GroupsForPersonAndGroupTypeFilter(groupTypeId, equality,
                    groupCount);

            assertEquals(groupTypeId, filter.getGroupTypeId());
        }
    }

    @Test
    @UnitTestMethod(target = GroupsForPersonAndGroupTypeFilter.class, name = "hashCode", args = {})
    public void testHashCode() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(386194196580028301L);

        // equal objects have equal hash codes
        for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			GroupsForPersonAndGroupTypeFilter filter1 = getRandomGroupsForPersonAndGroupTypeFilter(seed);
			GroupsForPersonAndGroupTypeFilter filter2 = getRandomGroupsForPersonAndGroupTypeFilter(seed);

			assertEquals(filter1, filter2);
			assertEquals(filter1.hashCode(), filter2.hashCode());
        }

        // hash codes are reasonably distributed
        Set<Integer> hashCodes = new LinkedHashSet<>();
        for (int i = 0; i < 100; i++) {
			GroupsForPersonAndGroupTypeFilter filter = getRandomGroupsForPersonAndGroupTypeFilter(randomGenerator.nextLong());
			hashCodes.add(filter.hashCode());
        }

        assertEquals(100, hashCodes.size());
    }

    @Test
    @UnitTestMethod(target = GroupsForPersonAndGroupTypeFilter.class, name = "equals", args = { Object.class })
    public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1974335207275712576L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			GroupsForPersonAndGroupTypeFilter filter = getRandomGroupsForPersonAndGroupTypeFilter(randomGenerator.nextLong());
			assertFalse(filter.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			GroupsForPersonAndGroupTypeFilter filter = getRandomGroupsForPersonAndGroupTypeFilter(randomGenerator.nextLong());
			assertFalse(filter.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			GroupsForPersonAndGroupTypeFilter filter = getRandomGroupsForPersonAndGroupTypeFilter(randomGenerator.nextLong());
			assertTrue(filter.equals(filter));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			GroupsForPersonAndGroupTypeFilter filter1 = getRandomGroupsForPersonAndGroupTypeFilter(seed);
			GroupsForPersonAndGroupTypeFilter filter2 = getRandomGroupsForPersonAndGroupTypeFilter(seed);
			assertFalse(filter1 == filter2);
			for (int j = 0; j < 10; j++) {
				assertTrue(filter1.equals(filter2));
				assertTrue(filter2.equals(filter1));
			}
		}

		// different inputs yield unequal filters
		Set<GroupsForPersonAndGroupTypeFilter> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			GroupsForPersonAndGroupTypeFilter filter = getRandomGroupsForPersonAndGroupTypeFilter(randomGenerator.nextLong());
			set.add(filter);
		}
		assertEquals(100, set.size());
    }

    @Test
    @UnitTestMethod(target = GroupsForPersonAndGroupTypeFilter.class, name = "toString", args = {})
    public void testToString() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2394011517139293620L);
        for (int i = 0; i < 10; i++) {
            Equality equality = Equality.getRandomEquality(randomGenerator);
            int groupCount = randomGenerator.nextInt(10);
            GroupTypeId groupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
            GroupsForPersonAndGroupTypeFilter filter = new GroupsForPersonAndGroupTypeFilter(groupTypeId, equality,
                    groupCount);

            StringBuilder builder = new StringBuilder();
            builder.append("GroupsForPersonAndGroupTypeFilter [groupTypeId=");
            builder.append(groupTypeId);
            builder.append(", equality=");
            builder.append(equality);
            builder.append(", groupCount=");
            builder.append(groupCount);
            builder.append("]");

            assertEquals(builder.toString(), filter.toString());
        }
    }

    private GroupsForPersonAndGroupTypeFilter getRandomGroupsForPersonAndGroupTypeFilter(long seed) {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

        TestGroupTypeId randomGroupTypeId = TestGroupTypeId.getRandomGroupTypeId(randomGenerator);
        Equality randomEquality = Equality.getRandomEquality(randomGenerator);
        int groupCount = randomGenerator.nextInt(Integer.MAX_VALUE);

        return new GroupsForPersonAndGroupTypeFilter(randomGroupTypeId, randomEquality, groupCount);
    }
}
