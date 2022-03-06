package plugins.globals.events;

import net.jcip.annotations.Immutable;
import nucleus.SimulationContext;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import nucleus.SimpleEventLabeler;
import plugins.globals.GlobalDataManager;
import plugins.globals.support.GlobalError;
import plugins.globals.support.GlobalPropertyId;
import util.ContractException;

@Immutable
public class GlobalPropertyChangeObservationEvent implements Event {
	private final GlobalPropertyId globalPropertyId;
	private final Object previousPropertyValue;
	private final Object currentPropertyValue;

	/**
	 * Constructs the event.
	 * 
	 */
	public GlobalPropertyChangeObservationEvent(GlobalPropertyId globalPropertyId, Object previousPropertyValue, Object currentPropertyValue) {
		super();
		this.globalPropertyId = globalPropertyId;
		this.previousPropertyValue = previousPropertyValue;
		this.currentPropertyValue = currentPropertyValue;
	}

	/**
	 * Returns the global property id
	 */
	public GlobalPropertyId getGlobalPropertyId() {
		return globalPropertyId;
	}

	/**
	 * Returns the previous property value
	 */
	public Object getPreviousPropertyValue() {
		return previousPropertyValue;
	}

	/**
	 * Returns the current property value
	 */
	public Object getCurrentPropertyValue() {
		return currentPropertyValue;
	}

	/**
	 * Standard string implementation of the form
	 * 
	 * GlobalPropertyChangeObservationEvent [globalPropertyId=" +
	 * globalPropertyId + ", previousPropertyValue=" + previousPropertyValue +
	 * ", currentPropertyValue=" + currentPropertyValue + "]
	 */
	@Override
	public String toString() {
		return "GlobalPropertyChangeObservationEvent [globalPropertyId=" + globalPropertyId + ", previousPropertyValue=" + previousPropertyValue + ", currentPropertyValue=" + currentPropertyValue
				+ "]";
	}

	private static enum LabelerId implements EventLabelerId {
		PROPERTY
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GlobalPropertyChangeObservationEvent} events. Matches on global
	 * property id.
	 *
	 *
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain GlobalError#NULL_GLOBAL_PROPERTY_ID} if the
	 *             global property id is null</li>
	 *             <li>{@linkplain GlobalError#UNKNOWN_GLOBAL_PROPERTY_ID} if
	 *             the global property id is unknown</li>
	 */
	public static EventLabel<GlobalPropertyChangeObservationEvent> getEventLabel(SimulationContext simulationContext, GlobalPropertyId globalPropertyId) {
		validateGlobalProperty(simulationContext, globalPropertyId);
		return new MultiKeyEventLabel<>(globalPropertyId, LabelerId.PROPERTY, GlobalPropertyChangeObservationEvent.class, globalPropertyId);
	}

	/**
	 * Returns an event labeler for {@link GlobalPropertyChangeObservationEvent}
	 * events that the global property id.
	 */
	public static EventLabeler<GlobalPropertyChangeObservationEvent> getEventLabeler() {
		return new SimpleEventLabeler<>(LabelerId.PROPERTY, GlobalPropertyChangeObservationEvent.class,
				(context, event) -> new MultiKeyEventLabel<>(event.getGlobalPropertyId(), LabelerId.PROPERTY, GlobalPropertyChangeObservationEvent.class, event.getGlobalPropertyId()));
	}

	private static void validateGlobalProperty(SimulationContext simulationContext, GlobalPropertyId globalPropertyId) {
		simulationContext.getDataManager(GlobalDataManager.class).get().getGlobalPropertyDefinition(globalPropertyId);
	}

	/**
	 * Returns the global property id used to create this event
	 */
	@Override
	public Object getPrimaryKeyValue() {
		return globalPropertyId;
	}
}
