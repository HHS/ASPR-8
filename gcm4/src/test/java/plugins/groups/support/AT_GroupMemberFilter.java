package plugins.groups.support;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.SimulationContext;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.groups.datamanagers.GroupsDataManager;
import plugins.groups.events.GroupMembershipAdditionEvent;
import plugins.groups.events.GroupMembershipRemovalEvent;
import plugins.groups.testsupport.GroupsTestPluginFactory;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.partitions.support.Filter;
import plugins.partitions.support.FilterSensitivity;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.stochastics.StochasticsDataManager;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_GroupMemberFilter {

	@Test
	@UnitTestConstructor(target = GroupMemberFilter.class, args = { GroupId.class })
	public void testConstructor() {

		TestSimulation.executeSimulation(GroupsTestPluginFactory.factory(100, 3, 10, 8499169041100865476L, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			List<GroupId> groupIds = groupsDataManager.getGroupIds();
			assertFalse(groupIds.isEmpty());
			for (GroupId groupId : groupIds) {
				final Filter filter = new GroupMemberFilter(groupId);
				assertNotNull(filter);
			}

			// precondition tests
			ContractException contractException = assertThrows(ContractException.class,
					() -> new GroupMemberFilter(null).validate(c));
			assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());
		}).getPlugins());
	}

	@Test
	@UnitTestMethod(target = GroupMemberFilter.class, name = "getFilterSensitivities", args = {})
	public void testGetFilterSensitivities() {

		TestSimulation.executeSimulation(GroupsTestPluginFactory.factory(100, 3, 10, 7283631979607042406L, (c) -> {
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

		}).getPlugins());

	}

	@Test
	@UnitTestMethod(target = GroupMemberFilter.class, name = "evaluate", args = { SimulationContext.class,
			PersonId.class })
	public void testEvaluate() {

		TestSimulation.executeSimulation(GroupsTestPluginFactory.factory(100, 3, 10, 6248106595116941770L, (c) -> {
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
				boolean actual = filter.evaluate(c, personId);
				assertEquals(expected, actual);
			}

			/* precondition: if the person id is null */
			ContractException contractException = assertThrows(ContractException.class, () -> filter.evaluate(c, null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			/* precondition: if the person id is unknown */
			contractException = assertThrows(ContractException.class,
					() -> filter.evaluate(c, new PersonId(123412342)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());
		}).getPlugins());
	}

	@Test
	@UnitTestMethod(target = GroupMemberFilter.class, name = "validate", args = { SimulationContext.class })
	public void testValidate() {

		TestSimulation.executeSimulation(GroupsTestPluginFactory.factory(100, 3, 10, 8525809821136960274L, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			GroupId groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_3);
			Filter filter = new GroupMemberFilter(groupId);

			// show that a properly defined filter validates and does not throw
			assertDoesNotThrow(() -> filter.validate(c));

			/* precondition: if the groupId is null */
			ContractException contractException = assertThrows(ContractException.class,
					() -> new GroupMemberFilter(null).validate(c));
			assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

		}).getPlugins());
	}
}
