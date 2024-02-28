package gov.hhs.aspr.ms.gcm.plugins.groups.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.plugins.groups.datamanagers.GroupsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.groups.events.GroupMembershipAdditionEvent;
import gov.hhs.aspr.ms.gcm.plugins.groups.events.GroupMembershipRemovalEvent;
import gov.hhs.aspr.ms.gcm.plugins.groups.testsupport.GroupsTestPluginFactory;
import gov.hhs.aspr.ms.gcm.plugins.groups.testsupport.GroupsTestPluginFactory.Factory;
import gov.hhs.aspr.ms.gcm.plugins.groups.testsupport.TestGroupTypeId;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.Equality;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.FilterSensitivity;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PartitionError;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PartitionsContext;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.filters.Filter;
import gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.TestPartitionsContext;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsDataManager;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_GroupTypesForPersonFilter {

    @Test
    @UnitTestConstructor(target = GroupTypesForPersonFilter.class, args = { Equality.class, int.class })
    public void testConstructor() {
        // nothing to test
    }

    @Test
    @UnitTestMethod(target = GroupTypesForPersonFilter.class, name = "validate", args = { PartitionsContext.class })
    public void testValidate() {
        // precondition tests

        // if the equality operator is null
        Factory factory = GroupsTestPluginFactory.factory(100, 3, 10, 1499199255771310930L, (c) -> {
            TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);
            ContractException contractException = assertThrows(ContractException.class,
                    () -> new GroupTypesForPersonFilter(null, 5).validate(testPartitionsContext));
            assertEquals(PartitionError.NULL_EQUALITY_OPERATOR, contractException.getErrorType());
        });

        TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

    }

    @Test
    @UnitTestMethod(target = GroupTypesForPersonFilter.class, name = "getFilterSensitivities", args = {})
    public void testGetFilterSensitivities() {

        Factory factory = GroupsTestPluginFactory.factory(100, 3, 10, 770617124373530907L, (c) -> {
            Filter filter = new GroupTypesForPersonFilter(Equality.EQUAL, 5);

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
    @UnitTestMethod(target = GroupTypesForPersonFilter.class, name = "evaluate", args = { PartitionsContext.class,
            PersonId.class })
    public void testEvaluate() {

        Factory factory = GroupsTestPluginFactory.factory(100, 3, 10, 2954287333801626073L, (c) -> {
            TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);
            GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
            PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
            List<PersonId> people = peopleDataManager.getPeople();
            RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();

            GroupId groupId1 = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
            GroupId groupId2 = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_2);
            GroupId groupId3 = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_3);

            Filter filter = new GroupTypesForPersonFilter(Equality.EQUAL, 2);

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
                boolean expected = groupsDataManager.getGroupTypeCountForPersonId(personId) == 2;
                boolean actual = filter.evaluate(testPartitionsContext, personId);
                assertEquals(expected, actual);
            }

            /* precondition: if the context is null */
            ContractException contractException = assertThrows(ContractException.class,
                    () -> filter.evaluate(null, new PersonId(0)));
            assertEquals(NucleusError.NULL_SIMULATION_CONTEXT, contractException.getErrorType());

            /* precondition: if the person id is null */
            contractException = assertThrows(ContractException.class,
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
    @UnitTestMethod(target = GroupTypesForPersonFilter.class, name = "getEquality", args = {})
    public void testGetEquality() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5482382141715795467L);

        for (int i = 0; i < 10; i++) {
            Equality equality = Equality.getRandomEquality(randomGenerator);
            int groupTypeCount = randomGenerator.nextInt(10);
            GroupTypesForPersonFilter filter = new GroupTypesForPersonFilter(equality, groupTypeCount);

            assertEquals(equality, filter.getEquality());
        }
    }

    @Test
    @UnitTestMethod(target = GroupTypesForPersonFilter.class, name = "getGroupTypeCount", args = {})
    public void testGetGroupTypeCount() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7992636052948538952L);

        for (int i = 0; i < 10; i++) {
            Equality equality = Equality.getRandomEquality(randomGenerator);
            int groupTypeCount = randomGenerator.nextInt(10);
            GroupTypesForPersonFilter filter = new GroupTypesForPersonFilter(equality, groupTypeCount);

            assertEquals(groupTypeCount, filter.getGroupTypeCount());
        }
    }

    @Test
    @UnitTestMethod(target = GroupTypesForPersonFilter.class, name = "hashCode", args = {})
    public void testHashCode() {
        GroupTypesForPersonFilter filter1 = new GroupTypesForPersonFilter(Equality.EQUAL, 2);
        GroupTypesForPersonFilter filter2 = new GroupTypesForPersonFilter(Equality.EQUAL, 5);
        GroupTypesForPersonFilter filter3 = new GroupTypesForPersonFilter(Equality.NOT_EQUAL, 2);
        GroupTypesForPersonFilter filter4 = new GroupTypesForPersonFilter(Equality.NOT_EQUAL, 5);
        GroupTypesForPersonFilter filter5 = new GroupTypesForPersonFilter(Equality.EQUAL, 2);

        assertEquals(filter1.hashCode(), filter1.hashCode());

        assertNotEquals(filter1.hashCode(), filter2.hashCode());
        assertNotEquals(filter1.hashCode(), filter3.hashCode());
        assertNotEquals(filter1.hashCode(), filter4.hashCode());

        assertNotEquals(filter2.hashCode(), filter3.hashCode());
        assertNotEquals(filter2.hashCode(), filter4.hashCode());

        assertNotEquals(filter3.hashCode(), filter4.hashCode());

        assertEquals(filter1.hashCode(), filter5.hashCode());
    }

    @Test
    @UnitTestMethod(target = GroupTypesForPersonFilter.class, name = "equals", args = { Object.class })
    public void testEquals() {
        GroupTypesForPersonFilter filter1 = new GroupTypesForPersonFilter(Equality.EQUAL, 2);
        GroupTypesForPersonFilter filter2 = new GroupTypesForPersonFilter(Equality.EQUAL, 5);
        GroupTypesForPersonFilter filter3 = new GroupTypesForPersonFilter(Equality.NOT_EQUAL, 2);
        GroupTypesForPersonFilter filter4 = new GroupTypesForPersonFilter(Equality.NOT_EQUAL, 5);
        GroupTypesForPersonFilter filter5 = new GroupTypesForPersonFilter(Equality.EQUAL, 2);

        assertEquals(filter1, filter1);

        assertNotEquals(filter1, null);

        assertNotEquals(filter1, new Object());

        assertNotEquals(filter1, filter2);
        assertNotEquals(filter1, filter3);
        assertNotEquals(filter1, filter4);

        assertNotEquals(filter2, filter3);
        assertNotEquals(filter2, filter4);

        assertNotEquals(filter3, filter4);

        assertEquals(filter1, filter5);
    }

    @Test
    @UnitTestMethod(target = GroupTypesForPersonFilter.class, name = "toString", args = {})
    public void testToString() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2394011517139293620L);
        for (int i = 0; i < 10; i++) {
            Equality equality = Equality.getRandomEquality(randomGenerator);
            int groupTypeCount = randomGenerator.nextInt(10);
            GroupTypesForPersonFilter filter = new GroupTypesForPersonFilter(equality, groupTypeCount);

            StringBuilder builder = new StringBuilder();
            builder.append("GroupTypesForPersonFilter [equality=");
            builder.append(equality);
            builder.append(", groupTypeCount=");
            builder.append(groupTypeCount);
            builder.append("]");

            assertEquals(builder.toString(), filter.toString());
        }
    }
}
