package plugins.compartments.events.observation;

import net.jcip.annotations.Immutable;
import nucleus.SimulationContext;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import nucleus.SimpleEventLabeler;
import plugins.compartments.datacontainers.CompartmentDataView;
import plugins.compartments.support.CompartmentError;
import plugins.compartments.support.CompartmentId;
import plugins.compartments.support.CompartmentPropertyId;
import util.ContractException;

/**
 * An observation event indicating that a compartment property has changed.
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public class CompartmentPropertyChangeObservationEvent implements Event {
	private final CompartmentId compartmentId;
	private final CompartmentPropertyId compartmentPropertyId;
	private final Object previousPropertyValue;
	private final Object currentPropertyValue;

	/**
	 * Creates this event from valid, non-null values.
	 * 
	 * 
	 */
	public CompartmentPropertyChangeObservationEvent(CompartmentId compartmentId, CompartmentPropertyId compartmentPropertyId, Object previousPropertyValue, Object currentPropertyValue) {
		super();
		this.compartmentId = compartmentId;
		this.compartmentPropertyId = compartmentPropertyId;
		this.previousPropertyValue = previousPropertyValue;
		this.currentPropertyValue = currentPropertyValue;
	}

	/**
	 * Returns the {@link CompartmentId} for this event
	 */
	public CompartmentId getCompartmentId() {
		return compartmentId;
	}

	/**
	 * Returns the {@link CompartmentPropertyId} for this event
	 */
	public CompartmentPropertyId getCompartmentPropertyId() {
		return compartmentPropertyId;
	}

	/**
	 * Returns the property value for the compartment immediately before the
	 * change
	 */
	public Object getPreviousPropertyValue() {
		return previousPropertyValue;
	}

	/**
	 * Returns the property value for the compartment immediately after the
	 * change
	 */
	public Object getCurrentPropertyValue() {
		return currentPropertyValue;
	}

	/**
	 * Standard string implementation
	 */
	@Override
	public String toString() {
		return "CompartmentPropertyChangeObservationEvent [compartmentId=" + compartmentId + ", compartmentPropertyId=" + compartmentPropertyId + ", previousPropertyValue=" + previousPropertyValue
				+ ", currentPropertyValue=" + currentPropertyValue + "]";
	}

	private static enum LabelerId implements EventLabelerId {
		COMPARTMENT_PROPERTY
	}

	private static void validateCompartmentProperty(SimulationContext simulationContext, final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId) {
		CompartmentDataView compartmentDataView = simulationContext.getDataView(CompartmentDataView.class).get();
		compartmentDataView.getCompartmentPropertyDefinition(compartmentId, compartmentPropertyId);
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link CompartmentPropertyChangeObservationEvent} events. Matches on
	 * compartment and property id.
	 * 
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain CompartmentError#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null</li>
	 *             <li>{@linkplain CompartmentError#UNKNOWN_COMPARTMENT_ID} if
	 *             the compartment id is not known</li>
	 *             <li>{@linkplain CompartmentError#NULL_COMPARTMENT_PROPERTY_ID}
	 *             if the compartment property id is null</li>
	 *             <li>{@linkplain CompartmentError#UNKNOWN_COMPARTMENT_PROPERTY_ID}
	 *             if the compartment property id is not associated with the
	 *             compartment</li>
	 */
	public static EventLabel<CompartmentPropertyChangeObservationEvent> getEventLabel(SimulationContext simulationContext, CompartmentId compartmentId, CompartmentPropertyId compartmentPropertyId) {
		validateCompartmentProperty(simulationContext, compartmentId, compartmentPropertyId);
		return new MultiKeyEventLabel<>(compartmentPropertyId, LabelerId.COMPARTMENT_PROPERTY, CompartmentPropertyChangeObservationEvent.class, compartmentId, compartmentPropertyId);

	}

	/**
	 * Returns an event labeler for
	 * {@link CompartmentPropertyChangeObservationEvent} events. Automatically
	 * added at initialization.
	 */
	public static EventLabeler<CompartmentPropertyChangeObservationEvent> getEventLabeler() {
		return new SimpleEventLabeler<>(LabelerId.COMPARTMENT_PROPERTY, CompartmentPropertyChangeObservationEvent.class, (context, event) -> new MultiKeyEventLabel<>(event.getCompartmentPropertyId(),
				LabelerId.COMPARTMENT_PROPERTY, CompartmentPropertyChangeObservationEvent.class, event.getCompartmentId(), event.getCompartmentPropertyId()));
	}
	
	@Override
	public Object getPrimaryKeyValue() {
		return compartmentPropertyId;
	}
}
