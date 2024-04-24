package gov.hhs.aspr.ms.gcm.lessons.plugins.people;

import java.util.LinkedHashSet;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.DataManager;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.DataManagerContext;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;

/* start code_ref=plugin_dependencies_defining_a_person_data_manager|code_cap=The PersonDataManager manages people by adding people, removing people and releasing  person removal events.*/
public final class PersonDataManager extends DataManager {

	private int masterPersonId;

	private Set<PersonId> people = new LinkedHashSet<>();

	private DataManagerContext dataManagerContext;

	@Override
	public void init(DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);
		this.dataManagerContext = dataManagerContext;
		dataManagerContext.subscribe(PersonRemovalMutationEvent.class, this::handlePersonRemovalMutationEvent);
	}

	public PersonId addPerson() {
		PersonId personId = new PersonId(masterPersonId++);
		people.add(personId);
		return personId;
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
/* end */