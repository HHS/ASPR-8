package gov.hhs.aspr.ms.gcm.lessons.plugins.family.datamanagers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.lessons.plugins.family.FamilyPluginData;
import gov.hhs.aspr.ms.gcm.lessons.plugins.family.events.FamilyAdditionEvent;
import gov.hhs.aspr.ms.gcm.lessons.plugins.family.events.FamilyMemberShipAdditionEvent;
import gov.hhs.aspr.ms.gcm.lessons.plugins.family.support.FamilyId;
import gov.hhs.aspr.ms.gcm.lessons.plugins.person.datamanagers.PersonDataManager;
import gov.hhs.aspr.ms.gcm.lessons.plugins.person.events.PersonRemovalEvent;
import gov.hhs.aspr.ms.gcm.lessons.plugins.person.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.DataManager;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.DataManagerContext;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;

public final class FamilyDataManager extends DataManager {

	private int masterFamilyId;
	private int initialFamilyCount;
	private int maxFamilySize;
	private Map<FamilyId, Set<PersonId>> familyMap = new LinkedHashMap<>();
	private Map<PersonId, FamilyId> personMap = new LinkedHashMap<>();
	private PersonDataManager personDataManager;
	private final FamilyPluginData familyPluginData;
	private DataManagerContext dataManagerContext;

	public FamilyDataManager(FamilyPluginData familyPluginData) {
		this.familyPluginData = familyPluginData;
	}

	@Override
	public void init(DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);
		this.dataManagerContext = dataManagerContext;
		personDataManager = dataManagerContext.getDataManager(PersonDataManager.class);
		dataManagerContext.subscribe(PersonRemovalEvent.class, this::handlePersonRemovalEvent);
		this.initialFamilyCount = familyPluginData.getFamilyCount();
		this.maxFamilySize = familyPluginData.getMaxFamilySize();

		dataManagerContext.subscribe(FamilyAdditionMutationEvent.class, this::handleFamilyAdditionMutationEvent);
		dataManagerContext.subscribe(FamilyMemberShipAdditionMutationEvent.class,
				this::handleFamilyMemberShipAdditionMutationEvent);
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

	private static record FamilyAdditionMutationEvent(FamilyId familyId) implements Event {
	}

	public FamilyId addFamily() {
		FamilyId familyId = new FamilyId(masterFamilyId++);
		dataManagerContext.releaseMutationEvent(new FamilyAdditionMutationEvent(familyId));
		return familyId;
	}

	private void handleFamilyAdditionMutationEvent(DataManagerContext dataManagerContext,
			FamilyAdditionMutationEvent familyAdditionMutationEvent) {
		FamilyId familyId = familyAdditionMutationEvent.familyId();
		familyMap.put(familyId, new LinkedHashSet<>());
		dataManagerContext.releaseObservationEvent(new FamilyAdditionEvent(familyId));
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

	public int getFamilySize(FamilyId familyId) {
		if (!familyExists(familyId)) {
			throw new RuntimeException("unknown family " + familyId);
		}
		return familyMap.get(familyId).size();
	}

	public Set<FamilyId> getFamilyIds() {
		return new LinkedHashSet<>(familyMap.keySet());
	}

	public Optional<FamilyId> getFamilyId(PersonId personId) {
		if (!personDataManager.personExists(personId)) {
			throw new RuntimeException("unknown person " + personId);
		}
		FamilyId familyId = personMap.get(personId);
		return Optional.ofNullable(familyId);
	}

	public int getInitialFamilyCount() {
		return this.initialFamilyCount;
	}

	public int getMaxFamilySize() {
		return this.maxFamilySize;
	}

	private static record FamilyMemberShipAdditionMutationEvent(PersonId personId, FamilyId familyId) implements Event {
	}

	public void addFamilyMember(PersonId personId, FamilyId familyId) {
		dataManagerContext.releaseMutationEvent(new FamilyMemberShipAdditionMutationEvent(personId, familyId));
	}

	private void handleFamilyMemberShipAdditionMutationEvent(DataManagerContext dataManagerContext,
			FamilyMemberShipAdditionMutationEvent familyMemberShipAdditionMutationEvent) {
		PersonId personId = familyMemberShipAdditionMutationEvent.personId();
		FamilyId familyId = familyMemberShipAdditionMutationEvent.familyId();
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

		dataManagerContext.releaseObservationEvent(new FamilyMemberShipAdditionEvent(familyId, personId));
	}

}
