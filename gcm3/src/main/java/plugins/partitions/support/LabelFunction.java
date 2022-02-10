package plugins.partitions.support;

import nucleus.SimulationContext;
import plugins.people.support.PersonId;

public interface LabelFunction {
	public Object getLabel(SimulationContext simulationContext, PersonId personId);
}