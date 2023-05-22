package plugins.regions.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import nucleus.Event;
import nucleus.NucleusError;
import nucleus.SimulationContext;
import plugins.partitions.support.Labeler;
import plugins.partitions.support.LabelerSensitivity;
import plugins.people.support.PersonId;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.events.PersonRegionUpdateEvent;
import util.errors.ContractException;

/**
 * A labeler for regions. The dimension of the labeler is the
 * {@linkplain RegionId} class, the event that stimulates a label update is
 * {@linkplain PersonRegionUpdateEvent} and the labeling function is
 * composed from the given Function.
 * 
 *
 */
public abstract class RegionLabeler implements Labeler {

	
	
	protected abstract Object getLabelFromRegionId(RegionId regionId);

	private RegionsDataManager regionsDataManager;

	

	private Optional<PersonId> getPersonId(PersonRegionUpdateEvent personRegionUpdateEvent) {
		return Optional.of(personRegionUpdateEvent.personId());
	}

	@Override
	public final Set<LabelerSensitivity<?>> getLabelerSensitivities() {
		Set<LabelerSensitivity<?>> result = new LinkedHashSet<>();
		result.add(new LabelerSensitivity<PersonRegionUpdateEvent>(PersonRegionUpdateEvent.class, this::getPersonId));
		return result;
	}

	@Override
	public final Object getCurrentLabel(SimulationContext simulationContext, PersonId personId) {
		if (simulationContext == null) {
			throw new ContractException(NucleusError.NULL_SIMULATION_CONTEXT);
		}
		if (regionsDataManager == null) {
			regionsDataManager = simulationContext.getDataManager(RegionsDataManager.class);
		}
		RegionId regionId = regionsDataManager.getPersonRegion(personId);
		return getLabelFromRegionId(regionId);
	}

	@Override
	public final Object getId() {
		return RegionId.class;
	}

	@Override
	public final Object getPastLabel(SimulationContext simulationContext, Event event) {
		PersonRegionUpdateEvent personRegionUpdateEvent = (PersonRegionUpdateEvent) event;
		return getLabelFromRegionId(personRegionUpdateEvent.previousRegionId());
	}

}
