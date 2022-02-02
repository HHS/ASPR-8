package plugins.groups.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.Context;
import nucleus.NucleusError;
import plugins.groups.datacontainers.PersonGroupDataView;
import plugins.groups.events.mutation.GroupCreationEvent;
import plugins.groups.events.observation.GroupMembershipAdditionObservationEvent;
import plugins.groups.events.observation.GroupMembershipRemovalObservationEvent;
import plugins.groups.testsupport.GroupsActionSupport;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.partitions.support.Equality;
import plugins.partitions.support.Filter;
import plugins.partitions.support.FilterSensitivity;
import plugins.partitions.support.PartitionError;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.stochastics.StochasticsDataView;
import util.ContractException;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = GroupTypesForPersonFilter.class)
public class AT_GroupTypesForPersonFilter {

	@Test
	@UnitTestConstructor(args = { Context.class, Equality.class, int.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "validate", args = {})
	public void testValidate() {
		//precondition tests
		
		//if the equality operator is null
		GroupsActionSupport.testConsumer(100, 3, 10, 1499199255771310930L, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> new GroupTypesForPersonFilter(null, 5).validate(c));
			assertEquals(PartitionError.NULL_EQUALITY_OPERATOR, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getFilterSensitivities", args = {})
	public void testGetFilterSensitivities() {

		GroupsActionSupport.testConsumer(100, 3, 10, 770617124373530907L, (c) -> {
			Filter filter = new GroupTypesForPersonFilter(Equality.EQUAL, 5);

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

		GroupsActionSupport.testConsumer(100, 3, 10, 2954287333801626073L, (c) -> {

			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			List<PersonId> people = personDataView.getPeople();
			RandomGenerator randomGenerator = c.getDataView(StochasticsDataView.class).get().getRandomGenerator();
			
			
			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_1));
			GroupId groupId1 = personGroupDataView.getLastIssuedGroupId().get();
			
			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_2));
			GroupId groupId2 = personGroupDataView.getLastIssuedGroupId().get();
			
			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_3));
			GroupId groupId3 = personGroupDataView.getLastIssuedGroupId().get();

			Filter filter = new GroupTypesForPersonFilter(Equality.EQUAL, 2);

			assertEquals(100,people.size());
			for (PersonId personId : people) {
				int typeCount = randomGenerator.nextInt(4);
				switch (typeCount) {
				case 0:
					break;
				case 1:
					c.resolveEvent(new GroupMembershipAdditionObservationEvent(personId, groupId1));
					
					break;
				case 2:
					c.resolveEvent(new GroupMembershipAdditionObservationEvent(personId, groupId1));
					c.resolveEvent(new GroupMembershipAdditionObservationEvent(personId, groupId2));					
					break;
				default:
					c.resolveEvent(new GroupMembershipAdditionObservationEvent(personId, groupId1));
					c.resolveEvent(new GroupMembershipAdditionObservationEvent(personId, groupId2));					
					c.resolveEvent(new GroupMembershipAdditionObservationEvent(personId, groupId3));
					break;
				}

			}

			for (PersonId personId : people) {
				boolean expected = personGroupDataView.getGroupTypeCountForPersonId(personId) == 2;
				boolean actual = filter.evaluate(c, personId);
				assertEquals(expected, actual);
			}

			/* precondition: if the context is null */
			ContractException contractException = assertThrows(ContractException.class, () -> filter.evaluate(null, new PersonId(0)));
			assertEquals(NucleusError.NULL_CONTEXT, contractException.getErrorType());

			/* precondition: if the person id is null */
			contractException = assertThrows(ContractException.class, () -> filter.evaluate(c, null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			/* precondition: if the person id is unknown */
			contractException = assertThrows(ContractException.class, () -> filter.evaluate(c, new PersonId(123412342)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		});
		
	}
}
