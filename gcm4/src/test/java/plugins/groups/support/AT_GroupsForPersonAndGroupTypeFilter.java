package plugins.groups.support;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.testsupport.testplugin.TestSimulation;
import plugins.groups.datamanagers.GroupsDataManager;
import plugins.groups.events.GroupMembershipAdditionEvent;
import plugins.groups.events.GroupMembershipRemovalEvent;
import plugins.groups.testsupport.GroupsTestPluginFactory;
import plugins.groups.testsupport.GroupsTestPluginFactory.Factory;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.partitions.support.Equality;
import plugins.partitions.support.FilterSensitivity;
import plugins.partitions.support.PartitionError;
import plugins.partitions.support.PartitionsContext;
import plugins.partitions.support.filters.Filter;
import plugins.partitions.testsupport.TestPartitionsContext;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.stochastics.StochasticsDataManager;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

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
					() -> new GroupsForPersonAndGroupTypeFilter(null, Equality.EQUAL, 5).validate(testPartitionsContext));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

			// if the equality operator is null
			contractException = assertThrows(ContractException.class,
					() -> new GroupsForPersonAndGroupTypeFilter(TestGroupTypeId.GROUP_TYPE_1, null, 5).validate(testPartitionsContext));
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
			ContractException contractException = assertThrows(ContractException.class, () -> filter.evaluate(testPartitionsContext, null));
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
					() -> new GroupsForPersonAndGroupTypeFilter(TestGroupTypeId.GROUP_TYPE_1, null, 2).validate(testPartitionsContext));
			assertEquals(PartitionError.NULL_EQUALITY_OPERATOR, contractException.getErrorType());

			// precondition: group type id is null
			contractException = assertThrows(ContractException.class,
					() -> new GroupsForPersonAndGroupTypeFilter(null, Equality.EQUAL, 2).validate(testPartitionsContext));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

			// precondition: group type id is unknown
			contractException = assertThrows(ContractException.class,
					() -> new GroupsForPersonAndGroupTypeFilter(TestGroupTypeId.getUnknownGroupTypeId(), Equality.EQUAL,
							2).validate(testPartitionsContext));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

		});
		
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}
}
