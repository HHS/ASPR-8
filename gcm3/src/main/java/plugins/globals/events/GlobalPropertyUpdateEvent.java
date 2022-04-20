package plugins.globals.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import nucleus.SimulationContext;
import nucleus.util.ContractException;
import plugins.globals.GlobalDataManager;
import plugins.globals.support.GlobalError;
import plugins.globals.support.GlobalPropertyId;

/**
 * 
 * An event released by the global data manager whenever a global property is
 * changed.
 * 
 * @author Shawn Hatch
 *
 */

@Immutable
public class GlobalPropertyUpdateEvent implements Event {
	private final GlobalPropertyId globalPropertyId;
	private final Object previousPropertyValue;
	private final Object currentPropertyValue;

	/**
	 * Constructs the event.
	 * 
	 */
	public GlobalPropertyUpdateEvent(GlobalPropertyId globalPropertyId, Object previousPropertyValue, Object currentPropertyValue) {
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
	 * GlobalPropertyUpdateEvent [globalPropertyId=" + globalPropertyId + ",
	 * previousPropertyValue=" + previousPropertyValue + ",
	 * currentPropertyValue=" + currentPropertyValue + "]
	 */
	@Override
	public String toString() {
		return "GlobalPropertyUpdateEvent [globalPropertyId=" + globalPropertyId + ", previousPropertyValue=" + previousPropertyValue + ", currentPropertyValue=" + currentPropertyValue + "]";
	}

	private static enum LabelerId implements EventLabelerId {
		PROPERTY
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link GlobalPropertyUpdateEvent} events. Matches on global property id.
	 *
	 *
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain GlobalError#NULL_GLOBAL_PROPERTY_ID} if the
	 *             global property id is null</li>
	 *             <li>{@linkplain GlobalError#UNKNOWN_GLOBAL_PROPERTY_ID} if
	 *             the global property id is unknown</li>
	 */
	public static EventLabel<GlobalPropertyUpdateEvent> getEventLabel(SimulationContext simulationContext, GlobalPropertyId globalPropertyId) {
		validateGlobalProperty(simulationContext, globalPropertyId);
		return new MultiKeyEventLabel<>(globalPropertyId, LabelerId.PROPERTY, GlobalPropertyUpdateEvent.class, globalPropertyId);
	}

	/**
	 * Returns an event labeler for {@link GlobalPropertyUpdateEvent} events
	 * that the global property id.
	 */
	public static EventLabeler<GlobalPropertyUpdateEvent> getEventLabeler() {
		return EventLabeler	.builder(GlobalPropertyUpdateEvent.class)//
							.setEventLabelerId(LabelerId.PROPERTY)//
							.setLabelFunction((context, event) -> getEventLabel(context, event.getGlobalPropertyId()))//
							.build();
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
