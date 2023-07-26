package gov.hhs.aspr.ms.gcm.plugins.groups.support;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.FilterSensitivity;
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
        GroupMemberFilter groupMemberFilter1 = new GroupMemberFilter(new GroupId(0));
        GroupMemberFilter groupMemberFilter2 = new GroupMemberFilter(new GroupId(1));
        GroupMemberFilter groupMemberFilter3 = new GroupMemberFilter(new GroupId(0));

        assertEquals(groupMemberFilter1.hashCode(), groupMemberFilter1.hashCode());

        assertNotEquals(groupMemberFilter1.hashCode(), groupMemberFilter2.hashCode());
        assertNotEquals(groupMemberFilter2.hashCode(), groupMemberFilter3.hashCode());

        assertEquals(groupMemberFilter1.hashCode(), groupMemberFilter3.hashCode());
    }

    @Test
    @UnitTestMethod(target = GroupMemberFilter.class, name = "equals", args = { Object.class })
    public void testEquals() {
        GroupMemberFilter groupMemberFilter1 = new GroupMemberFilter(new GroupId(0));
        GroupMemberFilter groupMemberFilter2 = new GroupMemberFilter(new GroupId(1));
        GroupMemberFilter groupMemberFilter3 = new GroupMemberFilter(new GroupId(0));

        assertEquals(groupMemberFilter1, groupMemberFilter1);

        assertNotEquals(groupMemberFilter1, null);

        assertNotEquals(groupMemberFilter1, new Object());

        assertNotEquals(groupMemberFilter1, groupMemberFilter2);
        assertNotEquals(groupMemberFilter2, groupMemberFilter3);

        assertEquals(groupMemberFilter1, groupMemberFilter3);
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
}
