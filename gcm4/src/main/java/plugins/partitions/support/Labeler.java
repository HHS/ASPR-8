package plugins.partitions.support;

import java.util.Set;

import nucleus.Event;
import plugins.people.support.PersonId;

/**
 * A partition labeler creates an object label for a person. The labeler has an
 * object dimension which is a dimension within a partition and the label
 * occupies a point in that dimension.
 * 
 *
 */

public interface Labeler {

	/**
	 * Returns the labeler sensitivities associated with this label.  
	 */
	public Set<LabelerSensitivity<?>> getLabelerSensitivities();

	/**
	 * Returns the label for the person based on the person's current
	 * information
	 */
	public Object getCurrentLabel(PartitionsContext partitionsContext, PersonId personId);

	/**
	 * Returns the label for the person based on the previous value recorded in
	 * the event. 
	 * 
	 * Partitions can be built with an optional policy to record the
	 * cell location of each person in the partition's cells. This can require a
	 * significant amount of memory to track. To help reduce this cost, the
	 * policy can be set to not track the current cell for each person. However,
	 * when an observation event occurs that would have the partition move the
	 * person from one cell to another, the run-time cost of locating the
	 * person's current cell is high. To address this, the labeler is asked to
	 * determine the current label for the person based on the event's data.
	 * Person related events are expected to always carry the associated recent 
	 * value that was just updated.
	 */
	public Object getPastLabel(PartitionsContext partitionsContext, Event event);

	/**
	 * Returns the id for this labeler.
	 */
	public Object getId();
	
	/**
	 * Labelers are equal if they label every person identically
	 */
	@Override
	public int hashCode();

	/**
	 * Labelers are equal if they label every person identically
	 */
	@Override
	public boolean equals(Object obj);
}
