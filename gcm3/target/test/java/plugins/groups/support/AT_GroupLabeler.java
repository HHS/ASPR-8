package plugins.groups.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import nucleus.SimulationContext;
import plugins.groups.datacontainers.PersonGroupDataView;
import plugins.groups.events.observation.GroupMembershipAdditionObservationEvent;
import plugins.groups.events.observation.GroupMembershipRemovalObservationEvent;
import plugins.groups.testsupport.GroupsActionSupport;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.partitions.support.LabelerSensitivity;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import util.ContractException;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = GroupLabeler.class)
public final class AT_GroupLabeler {

	@Test
	@UnitTestConstructor(args = { Function.class })
	public void testConstructor() {
		assertNotNull(new GroupLabeler((g) -> null));
	}

	@Test
	@UnitTestMethod(name = "getLabelerSensitivities", args = {})
	public void testGetLabelerSensitivities() {

		Set<LabelerSensitivity<?>> labelerSensitivities = new GroupLabeler((g) -> null).getLabelerSensitivities();

		// show that we get back some labeler sensitivities
		assertNotNull(labelerSensitivities);

		// we expect exactly two
		assertEquals(2, labelerSensitivities.size());

		boolean groupMembershipAdditionObservationEventSensitivityFound = false;
		boolean groupMembershipRemovalObservationEventSensitivityFound = false;
		for (LabelerSensitivity<?> labelerSensitivity : labelerSensitivities) {
			if (labelerSensitivity.getEventClass() == GroupMembershipAdditionObservationEvent.class) {
				groupMembershipAdditionObservationEventSensitivityFound = true;
				PersonId personId = new PersonId(45253);

				Optional<PersonId> optional = labelerSensitivity.getPersonId(new GroupMembershipAdditionObservationEvent(personId, new GroupId(56)));
				assertTrue(optional.isPresent());
				PersonId actualPersonId = optional.get();
				assertEquals(personId, actualPersonId);

			} else if (labelerSensitivity.getEventClass() == GroupMembershipRemovalObservationEvent.class) {
				groupMembershipRemovalObservationEventSensitivityFound = true;
				PersonId personId = new PersonId(45253);

				Optional<PersonId> optional = labelerSensitivity.getPersonId(new GroupMembershipRemovalObservationEvent(personId, new GroupId(56)));
				assertTrue(optional.isPresent());
				PersonId actualPersonId = optional.get();
				assertEquals(personId, actualPersonId);

			} else {
				fail("unknown labeler sensitivity");
			}
		}

		// show that we found both labeler sensitivities
		assertTrue(groupMembershipAdditionObservationEventSensitivityFound);
		assertTrue(groupMembershipRemovalObservationEventSensitivityFound);

	}

	@Test
	@UnitTestMethod(name = "getLabel", args = { SimulationContext.class, PersonId.class })
	public void testGetLabel() {

		GroupsActionSupport.testConsumer(30, 3, 5, 5880749882920317232L, (c) -> {
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();

			Function<GroupTypeCountMap, Object> func = (g) -> {
				int result = 0;
				for (GroupTypeId groupTypeId : g.getGroupTypeIds()) {
					TestGroupTypeId testGroupTypeId = (TestGroupTypeId) groupTypeId;
					result += (testGroupTypeId.ordinal() + 1) * g.getGroupCount(groupTypeId);
				}
				return result;
			};

			GroupLabeler groupLabeler = new GroupLabeler(func);

			for (PersonId personId : personDataView.getPeople()) {
				GroupTypeCountMap.Builder builder = GroupTypeCountMap.builder();
				for(GroupTypeId groupTypeId : personGroupDataView.getGroupTypeIds()){
					builder.setCount(groupTypeId, personGroupDataView.getGroupCountForGroupTypeAndPerson(groupTypeId, personId));
				}
				GroupTypeCountMap groupTypeCountMap = builder.build();
				Object expectedLabel = func.apply(groupTypeCountMap);	
				Object actualLabel = groupLabeler.getLabel(c, personId);
				assertEquals(expectedLabel, actualLabel);
			}
			
			//precondition tests
			
			//if the person id is null
			ContractException contractException = assertThrows(ContractException.class,()-> groupLabeler.getLabel(c, null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
			
			//if the person id is unknown
			contractException = assertThrows(ContractException.class,()-> groupLabeler.getLabel(c, new PersonId(100000)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());


		});
	}

	@Test
	@UnitTestMethod(name = "getDimension", args = {})
	public void testGetDimension() {
		Function<GroupTypeCountMap, Object> f = (g) -> null;
		assertEquals(GroupTypeId.class, new GroupLabeler(f).getDimension());
	}

}
