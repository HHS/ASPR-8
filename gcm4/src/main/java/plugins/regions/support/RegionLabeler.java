package plugins.regions.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

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
public final class RegionLabeler implements Labeler {

	private final Function<RegionId, Object> regionLabelingFunction;

	private RegionsDataManager regionsDataManager;

	/**
	 * Creates the Region labeler from the given labeling function
	 * 
	 * 
	 */
	public RegionLabeler(Function<RegionId, Object> regionLabelingFunction) {
		this.regionLabelingFunction = regionLabelingFunction;
	}

	private Optional<PersonId> getPersonId(PersonRegionUpdateEvent personRegionUpdateEvent) {
		return Optional.of(personRegionUpdateEvent.personId());
	}

	@Override
	public Set<LabelerSensitivity<?>> getLabelerSensitivities() {
		Set<LabelerSensitivity<?>> result = new LinkedHashSet<>();
		result.add(new LabelerSensitivity<PersonRegionUpdateEvent>(PersonRegionUpdateEvent.class, this::getPersonId));
		return result;
	}

	@Override
	public Object getCurrentLabel(SimulationContext simulationContext, PersonId personId) {
		if (simulationContext == null) {
			throw new ContractException(NucleusError.NULL_SIMULATION_CONTEXT);
		}
		if (regionsDataManager == null) {
			regionsDataManager = simulationContext.getDataManager(RegionsDataManager.class);
		}
		RegionId regionId = regionsDataManager.getPersonRegion(personId);
		return regionLabelingFunction.apply(regionId);
	}

	@Override
	public Object getId() {
		return RegionId.class;
	}

	@Override
	public Object getPastLabel(SimulationContext simulationContext, Event event) {
		PersonRegionUpdateEvent personRegionUpdateEvent = (PersonRegionUpdateEvent) event;
		return regionLabelingFunction.apply(personRegionUpdateEvent.previousRegionId());
	}

}
