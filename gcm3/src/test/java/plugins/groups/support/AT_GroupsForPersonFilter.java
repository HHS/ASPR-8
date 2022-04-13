package plugins.groups.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.NucleusError;
import nucleus.SimulationContext;
import nucleus.util.ContractException;
import plugins.groups.GroupDataManager;
import plugins.groups.events.GroupMembershipAdditionEvent;
import plugins.groups.events.GroupMembershipRemovalEvent;
import plugins.groups.testsupport.GroupsActionSupport;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.partitions.support.Equality;
import plugins.partitions.support.Filter;
import plugins.partitions.support.FilterSensitivity;
import plugins.partitions.support.PartitionError;
import plugins.people.PersonDataManager;
import plugins.people.support.PersonId;
import plugins.stochastics.StochasticsDataManager;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;


@UnitTest(target = GroupsForPersonFilter.class)
public class AT_GroupsForPersonFilter {

	@Test
	@UnitTestConstructor(args = { SimulationContext.class, Equality.class, int.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "validate", args = { SimulationContext.class, Equality.class, int.class })
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

		GroupsActionSupport.testConsumer(100, 0, 10, 6164158277278234559L, (c) -> {
			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).get().getRandomGenerator();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();

			List<PersonId> people = personDataManager.getPeople();

			GroupId groupId1 = groupDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			GroupId groupId2 = groupDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_2);
			GroupId groupId3 = groupDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_3);
			

			Filter filter = new GroupsForPersonFilter(Equality.EQUAL, 2);

			assertEquals(100, people.size());

			for (PersonId personId : people) {
				int typeCount = randomGenerator.nextInt(4);
				switch (typeCount) {
				case 0:
					break;
				case 1:
					groupDataManager.addPersonToGroup(personId,groupId1);					
					break;
				case 2:
					groupDataManager.addPersonToGroup(personId,groupId1);
					groupDataManager.addPersonToGroup(personId,groupId2);					
					break;
				default:
					groupDataManager.addPersonToGroup(personId,groupId1);
					groupDataManager.addPersonToGroup(personId,groupId2);					
					groupDataManager.addPersonToGroup(personId,groupId3);
					break;
				}

			}

			for (PersonId personId : people) {
				boolean expected = groupDataManager.getGroupCountForPerson(personId) == 2;
				boolean actual = filter.evaluate(c, personId);
				assertEquals(expected, actual);
			}

			/* precondition: if the context is null */
			ContractException contractException = assertThrows(ContractException.class, () -> filter.evaluate(null, new PersonId(0)));
			assertEquals(NucleusError.NULL_SIMULATION_CONTEXT, contractException.getErrorType());

			/* precondition: if the person id is null */
			assertThrows(RuntimeException.class, () -> filter.evaluate(c, null));

			/* precondition: if the person id is unknown */
			assertThrows(RuntimeException.class, () -> filter.evaluate(c, new PersonId(123412342)));

		});

	}
}
