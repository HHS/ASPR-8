package gov.hhs.aspr.ms.gcm.plugins.partitions.support;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;

/**
 * A {@link PopulationPartition} is the interface for implementors of
 * partitions, maintaining the people associated with the partition's cells by
 * handling individual mutation events.
 */
public interface PopulationPartition {

	/**
	 * Handles the addition of a person to the simulation. Preconditions : Should
	 * only be used to add people who have just been created and thus cannot already
	 * be members.
	 * 
	 * @throws RuntimeException if the person id is null
	 */
	public void attemptPersonAddition(PersonId personId);

	/**
	 * Handles the removal of a person from the simulation Precondition: Person must
	 * exist
	 */
	public void attemptPersonRemoval(PersonId personId);

	/**
	 * Handles the relevant data change to a person preconditions: the event must
	 * not be null
	 */
	public void handleEvent(Event event);

	/**
	 * Returns true if and only if the given {@link LabelSet} is compatible with
	 * this {@link PopulationPartition}. To be consistent, the {@link LabelSet} must
	 * not contain label values for label dimensions not contained in this
	 * partition. Precondition: label set may not be null
	 */
	public boolean validateLabelSetInfo(LabelSet labelSet);

	/**
	 * Returns the number of people contained in this {@link PopulationPartition}
	 */
	public int getPeopleCount();

	/**
	 * Returns the number of people contained in this {@link PopulationPartition}
	 * that are contained in cells that match the given {@link LabelSet}
	 */
	public int getPeopleCount(LabelSet labelSet);

	/**
	 * Returns the number of people contained in this {@link PopulationPartition}
	 * that are contained in cells that match the given {@link LabelSet}, mapped to
	 * each cell.
	 */
	public Map<LabelSet, Integer> getPeopleCountMap(LabelSet labelSet);

	/**
	 * Returns true if and only if the person is contained in this
	 * {@link PopulationPartition}
	 */
	public boolean contains(PersonId personId);

	/**
	 * Returns true if and only if the person is contained in this
	 * {@link PopulationPartition} under the cells consistent with the given
	 * {@link LabelSet}
	 */
	public boolean contains(PersonId personId, LabelSet labelSet);

	/**
	 * Returns the people contained in this {@link PopulationPartition} that are
	 * contained in cells that match the given {@link LabelSet} Precondition : the
	 * label set must be non-null and contain only compatible dimensions with this
	 * population partition.
	 */
	public List<PersonId> getPeople(LabelSet labelSet);

	/**
	 * Returns the people contained in this {@link PopulationPartition}
	 */
	public List<PersonId> getPeople();

	/**
	 * Returns a randomly chosen person identifier from the partition consistent
	 * with the partition sampler info. Note that the sampler must be consistent
	 * with the partition definition used to create this population partition. No
	 * precondition tests will be performed.
	 */
	public Optional<PersonId> samplePartition(final PartitionSampler partitionSampler);

	/**
	 * Returns an optional value by applying the given function to the label set
	 * associated with the person. If the person is not contained in the population
	 * partition the method returns an empty optional. Note that the
	 * labelSetFunction must be consistent with the partition definition used to
	 * create this population partition. No precondition tests will be performed.
	 */
	public <T> Optional<T> getPersonValue(LabelSetFunction<T> labelSetFunction, PersonId personId);

}
