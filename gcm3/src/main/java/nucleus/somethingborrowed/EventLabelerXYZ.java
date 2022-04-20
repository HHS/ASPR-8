package nucleus.somethingborrowed;

import java.util.function.BiFunction;

import nucleus.Event;
import nucleus.SimulationContext;

/**
 * A generics-based class that is used to filter event observations.
 * 
 * See {@linkplain EventLabelX} for details.
 * 
 * @author Shawn Hatch 
 */
public final class EventLabelerX<T extends Event> {
	private final BiFunction<SimulationContext, T, EventLabelX<T>> labelMaker;
	private final Object id;

	/**
	 * Constructs the event labeler from the given labeler id, event class and
	 * function for producing a label from an event.
	 */
	public EventLabelerX(final Object id,	
			BiFunction<SimulationContext, T, EventLabelX<T>> labelMaker) {	
		this.labelMaker = labelMaker;
		this.id = id;
	}
	
	@SuppressWarnings("unchecked")
	public EventLabelX<T> getEventLabel(SimulationContext simulationContext, Event event) {
		return labelMaker.apply(simulationContext, (T)event);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof EventLabelerX)) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		EventLabelerX other = (EventLabelerX) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}
	
}
