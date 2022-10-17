package plugins.partitions.support;

import java.util.Set;

import nucleus.Event;
import nucleus.SimulationContext;
import plugins.people.support.PersonId;

/**
 * A partition labeler creates an object label for a person. The labeler has an
 * object dimension which is a dimension within a partition and the label
 * occupies a point in that dimension.
 * 
 * @author Shawn Hatch
 *
 */

public interface Labeler {

	/**
	 * Returns the labeler sensitivities associated with this label 
	 */
	public Set<LabelerSensitivity<?>> getLabelerSensitivities();

	/**
	 * Returns the label for the person
	 */
	public Object getLabel(SimulationContext simulationContext, PersonId personId);
	
	/**
	 * Returns the label for the person based on the previous value recored in the event
	 */
	public Object getPastLabel(SimulationContext simulationContext, Event event);

	/**
	 * Returns the dimension for this labeler.
	 */
	public Object getDimension();
}
