package gov.hhs.aspr.ms.gcm.lessons.plugins.person.datamanagers;

import java.util.LinkedHashSet;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.lessons.plugins.person.events.PersonAdditionEvent;
import gov.hhs.aspr.ms.gcm.lessons.plugins.person.events.PersonRemovalEvent;
import gov.hhs.aspr.ms.gcm.lessons.plugins.person.support.PersonId;
import gov.hhs.aspr.ms.gcm.nucleus.DataManager;
import gov.hhs.aspr.ms.gcm.nucleus.DataManagerContext;
import gov.hhs.aspr.ms.gcm.nucleus.Event;

public final class PersonDataManager extends DataManager {

	private int masterPersonId;

	private Set<PersonId> people = new LinkedHashSet<>();

	private DataManagerContext dataManagerContext;

	@Override
	public void init(DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);
		this.dataManagerContext = dataManagerContext;
		dataManagerContext.subscribe(PersonAdditionMutationEvent.class, this::handlePersonAdditionMutationEvent);
		dataManagerContext.subscribe(PersonRemovalMutationEvent.class, this::handlePersonRemovalMutationEvent);
	}

	private static record PersonAdditionMutationEvent(PersonId personId) implements Event {
	}

	public PersonId addPerson() {
		PersonId personId = new PersonId(masterPersonId++);
		dataManagerContext.releaseMutationEvent(new PersonAdditionMutationEvent(personId));
		return personId;
	}

	private void handlePersonAdditionMutationEvent(DataManagerContext dataManagerContext,
			PersonAdditionMutationEvent personAdditionMutationEvent) {
		PersonId personId = personAdditionMutationEvent.personId();
		people.add(personId);
		dataManagerContext.releaseObservationEvent(new PersonAdditionEvent(personId));
	}

	public boolean personExists(PersonId personId) {
		return people.contains(personId);
	}

	public Set<PersonId> getPeople() {
		return new LinkedHashSet<>(people);
	}

	private static record PersonRemovalMutationEvent(PersonId personId) implements Event {
	}

	public void removePerson(PersonId personId) {
		dataManagerContext.releaseMutationEvent(new PersonRemovalMutationEvent(personId));
	}

	private void handlePersonRemovalMutationEvent(DataManagerContext dataManagerContext,
			PersonRemovalMutationEvent personRemovalMutationEvent) {
		PersonId personId = personRemovalMutationEvent.personId();
		if (!personExists(personId)) {
			throw new RuntimeException("person " + personId + " does not exist");
		}
		people.remove(personId);
		dataManagerContext.releaseObservationEvent(new PersonRemovalEvent(personId));

	}

}
