package lesson.plugins.person;

import java.util.LinkedHashSet;
import java.util.Set;

import nucleus.DataManager;
import nucleus.DataManagerContext;

public final class PersonDataManager extends DataManager{
	
	private int masterPersonId;
	
	private Set<PersonId> people = new LinkedHashSet<>();
	
	private DataManagerContext dataManagerContext;
	
	@Override
	public void init(DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);
		this.dataManagerContext = dataManagerContext;
	}
	
	public PersonId addPerson() {
		PersonId personId = new PersonId(masterPersonId++);
		people.add(personId);
		dataManagerContext.releaseEvent(new PersonAdditionEvent(personId));
		return personId;
	}
	
	public boolean personExists(PersonId personId) {
		return people.contains(personId);
	}
	
	public Set<PersonId> getPeople(){
		return new LinkedHashSet<>(people);
	}
	
	public void removePerson(PersonId personId) {
		if(!personExists(personId)) {
			throw new RuntimeException("person "+personId+" does not exist");
		}
		people.remove(personId);
		dataManagerContext.releaseEvent(new PersonRemovalEvent(personId));
	}
	
}
