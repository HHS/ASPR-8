package plugins.groups.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.SimulationContext;
import plugins.groups.datamanagers.GroupsDataManager;
import plugins.groups.events.GroupMembershipAdditionEvent;
import plugins.groups.events.GroupMembershipRemovalEvent;
import plugins.groups.testsupport.GroupsActionSupport;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.partitions.support.Equality;
import plugins.partitions.support.Filter;
import plugins.partitions.support.FilterSensitivity;
import plugins.partitions.support.PartitionError;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.stochastics.StochasticsDataManager;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

@UnitTest(target = GroupsForPersonAndGroupTypeFilter.class)
public class AT_GroupsForPersonAndGroupTypeFilter {

	@Test
	@UnitTestConstructor(args = { SimulationContext.class, GroupTypeId.class, Equality.class, int.class })
	public void testConstructor() {

		GroupsActionSupport.testConsumer(100, 3, 10, 5854778167265102928L, (c) -> {

			final Filter filter = new GroupsForPersonAndGroupTypeFilter(TestGroupTypeId.GROUP_TYPE_1, Equality.EQUAL, 5);
			assertNotNull(filter);

			// precondition tests

			// if the group type id is null
			ContractException contractException = assertThrows(ContractException.class, () -> new GroupsForPersonAndGroupTypeFilter(null, Equality.EQUAL, 5).validate(c));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

			// if the equality operator is null
			contractException = assertThrows(ContractException.class, () -> new GroupsForPersonAndGroupTypeFilter(TestGroupTypeId.GROUP_TYPE_1, null, 5).validate(c));
			assertEquals(PartitionError.NULL_EQUALITY_OPERATOR, contractException.getErrorType());

		});

	}
	
	@Test
	@UnitTestMethod(name = "getFilterSensitivities", args = {})
	public void testGetFilterSensitivities() {

		GroupsActionSupport.testConsumer(100, 3, 10, 1469082977858605268L, (c) -> {
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
		
	}

	@Test
	@UnitTestMethod(name = "evaluate", args = { SimulationContext.class, PersonId.class })
	public void testEvaluate() {
		
		GroupsActionSupport.testConsumer(100, 0, 10, 4592268926831796100L, (c) -> {
			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();

			GroupId groupId1 = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			GroupId groupId2 = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			GroupId groupId3 = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			

			Filter filter = new GroupsForPersonAndGroupTypeFilter(TestGroupTypeId.GROUP_TYPE_1, Equality.EQUAL, 2);

			assertEquals(100,people.size());
			
			for (PersonId personId : people) {
				int groupCount = randomGenerator.nextInt(4);
				switch (groupCount) {
				case 0:
					break;
				case 1:
					groupsDataManager.addPersonToGroup(personId,groupId1);										
					break;
				case 2:
					groupsDataManager.addPersonToGroup(personId,groupId1);
					groupsDataManager.addPersonToGroup(personId,groupId2);					
					break;
				default:
					groupsDataManager.addPersonToGroup(personId,groupId1);
					groupsDataManager.addPersonToGroup(personId,groupId2);
					groupsDataManager.addPersonToGroup(personId,groupId3);					
					break;
				}

			}

			for (PersonId personId : people) {
				boolean expected = groupsDataManager.getGroupCountForGroupTypeAndPerson(TestGroupTypeId.GROUP_TYPE_1, personId) == 2;
				boolean actual = filter.evaluate(c, personId);
				assertEquals(expected, actual);
			}

			/* precondition: if the person id is null */
			ContractException contractException = assertThrows(ContractException.class, () -> filter.evaluate(c, null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			/* precondition: if the person id is unknown */
			contractException = assertThrows(ContractException.class, () -> filter.evaluate(c, new PersonId(123412342)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		});		
	}
}
