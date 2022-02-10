package nucleus;

import java.util.function.BiFunction;

import net.jcip.annotations.NotThreadSafe;

/**
 * A convenience class that implements an event labeler
 *
 * @author Shawn Hatch
 *
 */
@NotThreadSafe
public class SimpleEventLabeler<T extends Event> implements EventLabeler<T> {
	private final Class<T> eventClass;	
	private final BiFunction<SimulationContext, T, EventLabel<T>> labelMaker;
	private final EventLabelerId id;

	public SimpleEventLabeler(final EventLabelerId id, final Class<T> eventClass, BiFunction<SimulationContext, T, EventLabel<T>> labelMaker) {		
		this.eventClass = eventClass;
		this.labelMaker = labelMaker;
		this.id = id;
	}

	@Override
	public final Class<T> getEventClass() {
		return eventClass;
	}

	@Override
	public EventLabel<T> getEventLabel(SimulationContext simulationContext, T event) {
		return labelMaker.apply(simulationContext, event);
	}

	@Override
	public EventLabelerId getId() {
		return id;
	}

}
