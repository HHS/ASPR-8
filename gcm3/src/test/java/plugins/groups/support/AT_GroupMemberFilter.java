package plugins.groups.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.naming.Context;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import annotations.UnitTest;
import annotations.UnitTestConstructor;
import annotations.UnitTestMethod;
import nucleus.util.ContractException;
import plugins.groups.GroupDataManager;
import plugins.groups.events.GroupMembershipAdditionObservationEvent;
import plugins.groups.events.GroupMembershipRemovalObservationEvent;
import plugins.groups.testsupport.GroupsActionSupport;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.partitions.support.Filter;
import plugins.partitions.support.FilterSensitivity;
import plugins.people.PersonDataManager;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.stochastics.StochasticsDataManager;

@UnitTest(target = GroupMemberFilter.class)
public class AT_GroupMemberFilter {

	@Test
	@UnitTestConstructor(args = { Context.class, GroupId.class })
	public void testConstructor() {

		GroupsActionSupport.testConsumer(100, 3, 10, 8499169041100865476L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			List<GroupId> groupIds = groupDataManager.getGroupIds();
			assertFalse(groupIds.isEmpty());
			for (GroupId groupId : groupIds) {
				final Filter filter = new GroupMemberFilter(groupId);
				assertNotNull(filter);
			}
			

			// precondition tests
			ContractException contractException = assertThrows(ContractException.class, () -> new GroupMemberFilter(null).validate(c));
			assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());
		});		
	}

	@Test
	@UnitTestMethod(name = "getFilterSensitivities", args = {})
	public void testGetFilterSensitivities() {
		
		GroupsActionSupport.testConsumer(100, 3, 10, 7283631979607042406L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			
			GroupId groupId = groupDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			 
			Filter filter = new GroupMemberFilter(groupId);

			Set<Class<?>> expected = new LinkedHashSet<>();
			expected.add(GroupMembershipAdditionObservationEvent.class);
			expected.add(GroupMembershipRemovalObservationEvent.class);

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
	@UnitTestMethod(name = "evaluate", args = { Context.class, PersonId.class })
	public void testEvaluate() {
		
		GroupsActionSupport.testConsumer(100, 3, 10, 6248106595116941770L, (c) -> {
			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).get().getRandomGenerator();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			GroupId groupId = groupDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_3);
			Filter filter = new GroupMemberFilter(groupId);
			
			for (PersonId personId : personDataManager.getPeople()) {
				if (randomGenerator.nextBoolean()) {
					groupDataManager.addPersonToGroup(groupId, personId);					
				}
			}

			for (PersonId personId : personDataManager.getPeople()) {
				boolean expected = groupDataManager.isGroupMember(groupId, personId);
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
