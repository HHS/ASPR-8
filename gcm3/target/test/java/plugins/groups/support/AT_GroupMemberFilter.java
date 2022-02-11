package plugins.groups.support;

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
import plugins.groups.datacontainers.PersonGroupDataView;
import plugins.groups.events.mutation.GroupCreationEvent;
import plugins.groups.events.mutation.GroupMembershipAdditionEvent;
import plugins.groups.events.observation.GroupMembershipAdditionObservationEvent;
import plugins.groups.events.observation.GroupMembershipRemovalObservationEvent;
import plugins.groups.testsupport.GroupsActionSupport;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.partitions.support.Filter;
import plugins.partitions.support.FilterSensitivity;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.stochastics.StochasticsDataManager;
import util.ContractException;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = GroupMemberFilter.class)
public class AT_GroupMemberFilter {

	@Test
	@UnitTestConstructor(args = { SimulationContext.class, GroupId.class })
	public void testConstructor() {

		GroupsActionSupport.testConsumer(100, 3, 10, 8499169041100865476L, (c) -> {
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			List<GroupId> groupIds = personGroupDataView.getGroupIds();
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
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_1));
			GroupId groupId = personGroupDataView.getLastIssuedGroupId().get();
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
	@UnitTestMethod(name = "evaluate", args = { SimulationContext.class, PersonId.class })
	public void testEvaluate() {
		
		GroupsActionSupport.testConsumer(100, 3, 10, 6248106595116941770L, (c) -> {
			RandomGenerator randomGenerator = c.getDataView(StochasticsDataManager.class).get().getRandomGenerator();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_3));
			GroupId groupId = personGroupDataView.getLastIssuedGroupId().get();
			Filter filter = new GroupMemberFilter(groupId);
			
			for (PersonId personId : personDataView.getPeople()) {
				if (randomGenerator.nextBoolean()) {
					c.resolveEvent(new GroupMembershipAdditionEvent(personId, groupId));
				}
			}

			for (PersonId personId : personDataView.getPeople()) {
				boolean expected = personGroupDataView.isGroupMember(groupId, personId);
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
