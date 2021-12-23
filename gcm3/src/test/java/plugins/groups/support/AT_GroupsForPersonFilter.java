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
import plugins.people.support.PersonId;
import plugins.stochastics.datacontainers.StochasticsDataView;
import util.ContractException;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;


@UnitTest(target = GroupsForPersonFilter.class)
public class AT_GroupsForPersonFilter {

	@Test
	@UnitTestConstructor(args = { Context.class, Equality.class, int.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "validate", args = { Context.class, Equality.class, int.class })
	public void testValidate() {
		GroupsActionSupport.testConsumer(100, 3, 10, 5329703278551588697L, (c) -> {
			// precondition tests

			// if the equality operator is null
			ContractException contractException = assertThrows(ContractException.class, () -> new GroupsForPersonFilter(null, 5).validate(c));
			assertEquals(PartitionError.NULL_EQUALITY_OPERATOR, contractException.getErrorType());

		});
	}
	
	@Test
	@UnitTestMethod(name = "getFilterSensitivities", args = {})
	public void testGetFilterSensitivities() {
		GroupsActionSupport.testConsumer(100, 3, 10, 8314387061888020596L, (c) -> {
			Filter filter = new GroupsForPersonFilter(Equality.EQUAL, 5);

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

		GroupsActionSupport.testConsumer(100, 0, 10, 6164158277278234559L, (c) -> {
			RandomGenerator randomGenerator = c.getDataView(StochasticsDataView.class).get().getRandomGenerator();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();

			List<PersonId> people = personDataView.getPeople();

			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_1));
			GroupId groupId1 = personGroupDataView.getLastIssuedGroupId().get();

			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_2));
			GroupId groupId2 = personGroupDataView.getLastIssuedGroupId().get();

			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_3));
			GroupId groupId3 = personGroupDataView.getLastIssuedGroupId().get();

			Filter filter = new GroupsForPersonFilter(Equality.EQUAL, 2);

			assertEquals(100, people.size());

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
				boolean expected = personGroupDataView.getGroupCountForPerson(personId) == 2;
				boolean actual = filter.evaluate(c, personId);
				assertEquals(expected, actual);
			}

			/* precondition: if the context is null */
			ContractException contractException = assertThrows(ContractException.class, () -> filter.evaluate(null, new PersonId(0)));
			assertEquals(NucleusError.NULL_CONTEXT, contractException.getErrorType());

			/* precondition: if the person id is null */
			assertThrows(RuntimeException.class, () -> filter.evaluate(c, null));

			/* precondition: if the person id is unknown */
			assertThrows(RuntimeException.class, () -> filter.evaluate(c, new PersonId(123412342)));

		});

	}
}
