package gov.hhs.aspr.ms.gcm.lessons.plugins.family;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.lessons.plugins.people.PersonDataManager;
import gov.hhs.aspr.ms.gcm.lessons.plugins.people.PersonId;
import gov.hhs.aspr.ms.gcm.lessons.plugins.people.PersonRemovalEvent;
import gov.hhs.aspr.ms.gcm.nucleus.DataManager;
import gov.hhs.aspr.ms.gcm.nucleus.DataManagerContext;

public final class FamilyDataManager extends DataManager {

	private int masterFamilyId;
	private Map<FamilyId, Set<PersonId>> familyMap = new LinkedHashMap<>();
	private Map<PersonId, FamilyId> personMap = new LinkedHashMap<>();
	private PersonDataManager personDataManager;

	@Override
	public void init(DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);
		personDataManager = dataManagerContext.getDataManager(PersonDataManager.class);

		dataManagerContext.subscribe(PersonRemovalEvent.class, this::handlePersonRemovalEvent);
	}

	private void handlePersonRemovalEvent(DataManagerContext dataManagerContext,
			PersonRemovalEvent personRemovalEvent) {
		PersonId personId = personRemovalEvent.getPersonId();
		FamilyId familyId = personMap.remove(personId);
		if (familyId != null) {
			familyMap.get(familyId).remove(personId);
		}
		System.out.println(
				"Family Data Manager is removing person " + personId + " at time = " + dataManagerContext.getTime());
	}

	public FamilyId addFamily() {
		FamilyId familyId = new FamilyId(masterFamilyId++);
		familyMap.put(familyId, new LinkedHashSet<>());
		return familyId;
	}

	public boolean familyExists(FamilyId familyId) {
		return familyMap.keySet().contains(familyId);
	}

	public List<PersonId> getFamilyMembers(FamilyId familyId) {
		if (!familyExists(familyId)) {
			throw new RuntimeException("unknown family " + familyId);
		}
		return new ArrayList<>(familyMap.get(familyId));
	}

	public Optional<FamilyId> getFamilyId(PersonId personId) {
		if (!personDataManager.personExists(personId)) {
			throw new RuntimeException("unknown person " + personId);
		}
		FamilyId familyId = personMap.get(personId);
		return Optional.ofNullable(familyId);
	}

	public void addFamilyMember(PersonId personId, FamilyId familyId) {
		if (!personDataManager.personExists(personId)) {
			throw new RuntimeException("unknown person " + personId);
		}
		if (!familyExists(familyId)) {
			throw new RuntimeException("unknown family " + familyId);
		}

		FamilyId currentFamilyId = personMap.get(personId);
		if (currentFamilyId != null) {
			throw new RuntimeException("person " + personId + " is already assigned to family " + currentFamilyId);
		}
		familyMap.get(familyId).add(personId);
		personMap.put(personId, familyId);

	}

}
