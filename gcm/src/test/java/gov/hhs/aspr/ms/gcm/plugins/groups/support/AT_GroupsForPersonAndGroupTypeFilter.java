package gov.hhs.aspr.ms.gcm.plugins.groups.support;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

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
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

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
        GroupsForPersonAndGroupTypeFilter filter1 = new GroupsForPersonAndGroupTypeFilter(TestGroupTypeId.GROUP_TYPE_1,
                Equality.EQUAL, 1);
        GroupsForPersonAndGroupTypeFilter filter2 = new GroupsForPersonAndGroupTypeFilter(TestGroupTypeId.GROUP_TYPE_1,
                Equality.EQUAL, 0);
        GroupsForPersonAndGroupTypeFilter filter3 = new GroupsForPersonAndGroupTypeFilter(TestGroupTypeId.GROUP_TYPE_1,
                Equality.NOT_EQUAL, 1);
        GroupsForPersonAndGroupTypeFilter filter4 = new GroupsForPersonAndGroupTypeFilter(TestGroupTypeId.GROUP_TYPE_2,
                Equality.EQUAL, 1);
        GroupsForPersonAndGroupTypeFilter filter5 = new GroupsForPersonAndGroupTypeFilter(TestGroupTypeId.GROUP_TYPE_2,
                Equality.NOT_EQUAL, 1);
        GroupsForPersonAndGroupTypeFilter filter6 = new GroupsForPersonAndGroupTypeFilter(TestGroupTypeId.GROUP_TYPE_1,
                Equality.NOT_EQUAL, 0);
        GroupsForPersonAndGroupTypeFilter filter7 = new GroupsForPersonAndGroupTypeFilter(TestGroupTypeId.GROUP_TYPE_2,
                Equality.EQUAL, 0);
        GroupsForPersonAndGroupTypeFilter filter8 = new GroupsForPersonAndGroupTypeFilter(TestGroupTypeId.GROUP_TYPE_2,
                Equality.NOT_EQUAL, 0);
        GroupsForPersonAndGroupTypeFilter filter9 = new GroupsForPersonAndGroupTypeFilter(TestGroupTypeId.GROUP_TYPE_1,
                Equality.EQUAL, 1);

        assertEquals(filter1.hashCode(), filter1.hashCode());

        assertNotEquals(filter1.hashCode(), filter2.hashCode());
        assertNotEquals(filter1.hashCode(), filter3.hashCode());
        assertNotEquals(filter1.hashCode(), filter4.hashCode());
        assertNotEquals(filter1.hashCode(), filter5.hashCode());
        assertNotEquals(filter1.hashCode(), filter6.hashCode());
        assertNotEquals(filter1.hashCode(), filter7.hashCode());
        assertNotEquals(filter1.hashCode(), filter8.hashCode());

        assertNotEquals(filter2.hashCode(), filter3.hashCode());
        assertNotEquals(filter2.hashCode(), filter4.hashCode());
        assertNotEquals(filter2.hashCode(), filter5.hashCode());
        assertNotEquals(filter2.hashCode(), filter6.hashCode());
        assertNotEquals(filter2.hashCode(), filter7.hashCode());
        assertNotEquals(filter2.hashCode(), filter8.hashCode());

        assertNotEquals(filter3.hashCode(), filter4.hashCode());
        assertNotEquals(filter3.hashCode(), filter5.hashCode());
        assertNotEquals(filter3.hashCode(), filter6.hashCode());
        assertNotEquals(filter3.hashCode(), filter7.hashCode());
        assertNotEquals(filter3.hashCode(), filter8.hashCode());

        assertNotEquals(filter4.hashCode(), filter5.hashCode());
        assertNotEquals(filter4.hashCode(), filter6.hashCode());
        assertNotEquals(filter4.hashCode(), filter7.hashCode());
        assertNotEquals(filter4.hashCode(), filter8.hashCode());

        assertNotEquals(filter5.hashCode(), filter6.hashCode());
        assertNotEquals(filter5.hashCode(), filter7.hashCode());
        assertNotEquals(filter5.hashCode(), filter8.hashCode());

        assertNotEquals(filter6.hashCode(), filter7.hashCode());
        assertNotEquals(filter6.hashCode(), filter8.hashCode());

        assertNotEquals(filter7.hashCode(), filter8.hashCode());

        assertEquals(filter1.hashCode(), filter9.hashCode());
    }

    @Test
    @UnitTestMethod(target = GroupsForPersonAndGroupTypeFilter.class, name = "equals", args = { Object.class })
    public void testEquals() {
        GroupsForPersonAndGroupTypeFilter filter1 = new GroupsForPersonAndGroupTypeFilter(TestGroupTypeId.GROUP_TYPE_1,
                Equality.EQUAL, 1);
        GroupsForPersonAndGroupTypeFilter filter2 = new GroupsForPersonAndGroupTypeFilter(TestGroupTypeId.GROUP_TYPE_1,
                Equality.EQUAL, 0);
        GroupsForPersonAndGroupTypeFilter filter3 = new GroupsForPersonAndGroupTypeFilter(TestGroupTypeId.GROUP_TYPE_1,
                Equality.NOT_EQUAL, 1);
        GroupsForPersonAndGroupTypeFilter filter4 = new GroupsForPersonAndGroupTypeFilter(TestGroupTypeId.GROUP_TYPE_2,
                Equality.EQUAL, 1);
        GroupsForPersonAndGroupTypeFilter filter5 = new GroupsForPersonAndGroupTypeFilter(TestGroupTypeId.GROUP_TYPE_2,
                Equality.NOT_EQUAL, 1);
        GroupsForPersonAndGroupTypeFilter filter6 = new GroupsForPersonAndGroupTypeFilter(TestGroupTypeId.GROUP_TYPE_1,
                Equality.NOT_EQUAL, 0);
        GroupsForPersonAndGroupTypeFilter filter7 = new GroupsForPersonAndGroupTypeFilter(TestGroupTypeId.GROUP_TYPE_2,
                Equality.EQUAL, 0);
        GroupsForPersonAndGroupTypeFilter filter8 = new GroupsForPersonAndGroupTypeFilter(TestGroupTypeId.GROUP_TYPE_2,
                Equality.NOT_EQUAL, 0);
        GroupsForPersonAndGroupTypeFilter filter9 = new GroupsForPersonAndGroupTypeFilter(TestGroupTypeId.GROUP_TYPE_1,
                Equality.EQUAL, 1);

        assertEquals(filter1, filter1);

        assertNotEquals(filter1, null);

        assertNotEquals(filter1, new Object());

        assertNotEquals(filter1, filter2);
        assertNotEquals(filter1, filter3);
        assertNotEquals(filter1, filter4);
        assertNotEquals(filter1, filter5);
        assertNotEquals(filter1, filter6);
        assertNotEquals(filter1, filter7);
        assertNotEquals(filter1, filter8);

        assertNotEquals(filter2, filter3);
        assertNotEquals(filter2, filter4);
        assertNotEquals(filter2, filter5);
        assertNotEquals(filter2, filter6);
        assertNotEquals(filter2, filter7);
        assertNotEquals(filter2, filter8);

        assertNotEquals(filter3, filter4);
        assertNotEquals(filter3, filter5);
        assertNotEquals(filter3, filter6);
        assertNotEquals(filter3, filter7);
        assertNotEquals(filter3, filter8);

        assertNotEquals(filter4, filter5);
        assertNotEquals(filter4, filter6);
        assertNotEquals(filter4, filter7);
        assertNotEquals(filter4, filter8);

        assertNotEquals(filter5, filter6);
        assertNotEquals(filter5, filter7);
        assertNotEquals(filter5, filter8);

        assertNotEquals(filter6, filter7);
        assertNotEquals(filter6, filter8);

        assertNotEquals(filter7, filter8);

        assertEquals(filter1, filter9);
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
}
