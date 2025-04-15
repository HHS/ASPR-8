package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support;

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

import gov.hhs.aspr.ms.gcm.simulation.nucleus.NucleusError;
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
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.datamanagers.StochasticsDataManager;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_GroupsForPersonFilter {

    @Test
    @UnitTestConstructor(target = GroupsForPersonFilter.class, args = { Equality.class, int.class })
    public void testConstructor() {
        // nothing to test
    }

    @Test
    @UnitTestMethod(target = GroupsForPersonFilter.class, name = "validate", args = { PartitionsContext.class })
    public void testValidate() {
        Factory factory = GroupsTestPluginFactory.factory(100, 3, 10, 5329703278551588697L, (c) -> {
            // precondition tests

            TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);

            // if the equality operator is null
            ContractException contractException = assertThrows(ContractException.class,
                    () -> new GroupsForPersonFilter(null, 5).validate(testPartitionsContext));
            assertEquals(PartitionError.NULL_EQUALITY_OPERATOR, contractException.getErrorType());

        });

        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
    }

    @Test
    @UnitTestMethod(target = GroupsForPersonFilter.class, name = "getFilterSensitivities", args = {})
    public void testGetFilterSensitivities() {
        Factory factory = GroupsTestPluginFactory.factory(100, 3, 10, 8314387061888020596L, (c) -> {
            Filter filter = new GroupsForPersonFilter(Equality.EQUAL, 5);

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
    @UnitTestMethod(target = GroupsForPersonFilter.class, name = "evaluate", args = { PartitionsContext.class,
            PersonId.class })
    public void testEvaluate() {

        Factory factory = GroupsTestPluginFactory.factory(100, 0, 10, 6164158277278234559L, (c) -> {

            TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);

            RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();
            PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
            GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);

            List<PersonId> people = peopleDataManager.getPeople();

            GroupId groupId1 = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
            GroupId groupId2 = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_2);
            GroupId groupId3 = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_3);

            Filter filter = new GroupsForPersonFilter(Equality.EQUAL, 2);

            assertEquals(100, people.size());

            for (PersonId personId : people) {
                int typeCount = randomGenerator.nextInt(4);
                switch (typeCount) {
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
                boolean expected = groupsDataManager.getGroupCountForPerson(personId) == 2;
                boolean actual = filter.evaluate(testPartitionsContext, personId);
                assertEquals(expected, actual);
            }

            /* precondition: if the context is null */
            ContractException contractException = assertThrows(ContractException.class,
                    () -> filter.evaluate(null, new PersonId(0)));
            assertEquals(NucleusError.NULL_SIMULATION_CONTEXT, contractException.getErrorType());

            /* precondition: if the person id is null */
            assertThrows(RuntimeException.class, () -> filter.evaluate(testPartitionsContext, null));

            /* precondition: if the person id is unknown */
            assertThrows(RuntimeException.class, () -> filter.evaluate(testPartitionsContext, new PersonId(123412342)));

        });

        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
    }

    @Test
    @UnitTestMethod(target = GroupsForPersonFilter.class, name = "getEquality", args = {})
    public void testGetEquality() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8284890603319105493L);

        for (int i = 0; i < 10; i++) {
            Equality equality = Equality.getRandomEquality(randomGenerator);
            int groupCount = randomGenerator.nextInt(10);
            GroupsForPersonFilter filter = new GroupsForPersonFilter(equality, groupCount);

            assertEquals(equality, filter.getEquality());
        }
    }

    @Test
    @UnitTestMethod(target = GroupsForPersonFilter.class, name = "getGroupCount", args = {})
    public void testGetGroupCount() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5425834828181361952L);

        for (int i = 0; i < 10; i++) {
            Equality equality = Equality.getRandomEquality(randomGenerator);
            int groupCount = randomGenerator.nextInt(10);
            GroupsForPersonFilter filter = new GroupsForPersonFilter(equality, groupCount);

            assertEquals(groupCount, filter.getGroupCount());
        }
    }

    @Test
    @UnitTestMethod(target = GroupsForPersonFilter.class, name = "hashCode", args = {})
    public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(386824196593528301L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			GroupsForPersonFilter groupsForPersonFilter1 = getRandomGroupsForPersonFilter(seed);
			GroupsForPersonFilter groupsForPersonFilter2 = getRandomGroupsForPersonFilter(seed);

			assertEquals(groupsForPersonFilter1, groupsForPersonFilter2);
			assertEquals(groupsForPersonFilter1.hashCode(), groupsForPersonFilter2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			GroupsForPersonFilter groupsForPersonFilter = getRandomGroupsForPersonFilter(randomGenerator.nextLong());
			hashCodes.add(groupsForPersonFilter.hashCode());
		}

		assertEquals(100, hashCodes.size());
    }

    @Test
    @UnitTestMethod(target = GroupsForPersonFilter.class, name = "equals", args = { Object.class })
    public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1974882837275712576L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			GroupsForPersonFilter groupsForPersonFilter = getRandomGroupsForPersonFilter(randomGenerator.nextLong());
			assertFalse(groupsForPersonFilter.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			GroupsForPersonFilter groupsForPersonFilter = getRandomGroupsForPersonFilter(randomGenerator.nextLong());
			assertFalse(groupsForPersonFilter.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			GroupsForPersonFilter groupsForPersonFilter = getRandomGroupsForPersonFilter(randomGenerator.nextLong());
			assertTrue(groupsForPersonFilter.equals(groupsForPersonFilter));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			GroupsForPersonFilter groupsForPersonFilter1 = getRandomGroupsForPersonFilter(seed);
			GroupsForPersonFilter groupsForPersonFilter2 = getRandomGroupsForPersonFilter(seed);
			assertFalse(groupsForPersonFilter1 == groupsForPersonFilter2);
			for (int j = 0; j < 10; j++) {
				assertTrue(groupsForPersonFilter1.equals(groupsForPersonFilter2));
				assertTrue(groupsForPersonFilter2.equals(groupsForPersonFilter1));
			}
		}

		// different inputs yield unequal groupsForPersonFilters
		Set<GroupsForPersonFilter> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			GroupsForPersonFilter groupsForPersonFilter = getRandomGroupsForPersonFilter(randomGenerator.nextLong());
			set.add(groupsForPersonFilter);
		}
		assertEquals(100, set.size());
    }

    @Test
    @UnitTestMethod(target = GroupsForPersonFilter.class, name = "toString", args = {})
    public void testToString() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2394011517139293620L);
        for (int i = 0; i < 10; i++) {
            Equality equality = Equality.getRandomEquality(randomGenerator);
            int groupCount = randomGenerator.nextInt(10);
            GroupsForPersonFilter filter = new GroupsForPersonFilter(equality, groupCount);

            StringBuilder builder = new StringBuilder();
            builder.append("GroupsForPersonFilter [equality=");
            builder.append(equality);
            builder.append(", groupCount=");
            builder.append(groupCount);
            builder.append("]");

            assertEquals(builder.toString(), filter.toString());
        }
    }

    private GroupsForPersonFilter getRandomGroupsForPersonFilter(long seed) {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

        Equality[] equalityValues = Equality.values();
        Equality randomEquality = equalityValues[randomGenerator.nextInt(equalityValues.length)];

        int randomGroupCount = randomGenerator.nextInt(Integer.MAX_VALUE);

        return new GroupsForPersonFilter(randomEquality, randomGroupCount);
    }
}
