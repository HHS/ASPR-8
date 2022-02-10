package plugins.globals.resolvers;

import java.util.function.Consumer;

import nucleus.AgentContext;
import nucleus.NucleusError;
import nucleus.DataManagerContext;
import plugins.components.datacontainers.ComponentDataView;
import plugins.components.events.ComponentConstructionEvent;
import plugins.components.support.ComponentError;
import plugins.globals.GlobalPlugin;
import plugins.globals.datacontainers.GlobalDataManager;
import plugins.globals.datacontainers.GlobalDataView;
import plugins.globals.events.mutation.GlobalComponentConstructionEvent;
import plugins.globals.events.mutation.GlobalPropertyValueAssignmentEvent;
import plugins.globals.events.observation.GlobalPropertyChangeObservationEvent;
import plugins.globals.initialdata.GlobalInitialData;
import plugins.globals.support.GlobalComponentId;
import plugins.globals.support.GlobalError;
import plugins.globals.support.GlobalPropertyId;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.PropertyError;
import util.ContractException;

/**
 * 
 * Provides event resolution for the {@linkplain GlobalPlugin}.
 * <P>
 * Creates, publishes and maintains the {@linkplain GlobalDataView}. Initializes
 * the data view from the {@linkplain GlobalInitialData} instance provided to
 * the plugin.
 * </P>
 * <P>
 * Creates dynamic global components.
 * </P>
 * 
 * <P>
 * Initializes all event labelers defined by
 * {@linkplain GlobalPropertyChangeObservationEvent}
 * </P>
 * 
 * 
 * <P>
 * Resolves the following events:
 * <ul>
 * 
 * 
 * 
 * <li>{@linkplain GlobalComponentConstructionEvent} <blockquote> Creates a
 * global component agent.  <BR>
 * <BR>
 * Throws {@link ContractException}
 *
 * <ul>
 * <li>{@link GlobalError.NULL_GLOBAL_COMPONENT_ID} if the global component id
 * is null
 * <li>{@link GlobalError.DUPLICATE_GLOBAL_COMPONENT_ID} if the global component
 * was previously asssigned to an agent
 * <li>{@link NucleusError.NULL_AGENT_CONTEXT_CONSUMER} if the agent context
 * consumers is null
 * 
 * </ul>
 * </blockquote></li>
 * 
 * 
 * 
 * <li>{@linkplain GlobalPropertyValueAssignmentEvent} <blockquote>Updates the
 * global property value and time in the {@linkplain GlobalDataView} and
 * generates a corresponding {@linkplain GlobalPropertyChangeObservationEvent}
 * 
 * <BR>
 * <BR>
 * Throws {@link ContractException}
 *
 * <li>{@link GlobalError.NULL_GLOBAL_PROPERTY_ID} if the global property id is
 * null
 * <li>{@link GlobalError.UNKNOWN_GLOBAL_PROPERTY_ID} if the global property id
 * is unknown
 * <li>{@link GlobalError.NULL_GLOBAL_PROPERTY_VALUE} if the property value is
 * null
 * <li>{@link PropertyError.IMMUTABLE_VALUE} if the global property definition
 * indicates the property is not mutable
 * <li>{@link PropertyError.INCOMPATIBLE_VALUE} if the property value is
 * incompatible with the property definition </blockquote></li>
 * 
 *
 * <ul>
 * </p>
 * 
 * 
 * 
 * @author Shawn Hatch
 *
 */

public final class GlobalPropertyResolver {
	private GlobalDataManager globalDataManager;
	private GlobalInitialData globalInitialData;
	private ComponentDataView componentDataView;

	/**
	 * Constructs this resolver.
	 * 
	 * @throws ContractException
	 *             <li>if the global initial data is null</li>
	 */
	public GlobalPropertyResolver(GlobalInitialData globalInitialData) {
		if (globalInitialData == null) {
			throw new ContractException(GlobalError.NULL_GLOBAL_INITIAL_DATA);
		}
		this.globalInitialData = globalInitialData;
	}

	public void init(final DataManagerContext dataManagerContext) {
		componentDataView = dataManagerContext.getDataView(ComponentDataView.class).get();
		dataManagerContext.addEventLabeler(GlobalPropertyChangeObservationEvent.getEventLabeler());

		dataManagerContext.subscribeToEventExecutionPhase(GlobalPropertyValueAssignmentEvent.class, this::handleGlobalPropertyValueAssignmentEventExecution);
		dataManagerContext.subscribeToEventValidationPhase(GlobalPropertyValueAssignmentEvent.class, this::handleGlobalPropertyValueAssignmentEventValidation);

		dataManagerContext.subscribeToEventExecutionPhase(GlobalComponentConstructionEvent.class, this::handleGlobalComponentConstructionEventExecution);
		dataManagerContext.subscribeToEventValidationPhase(GlobalComponentConstructionEvent.class, this::handleGlobalComponentConstructionEventValidation);

		globalDataManager = new GlobalDataManager(dataManagerContext.getSafeContext());

		for (GlobalComponentId globalComponentId : globalInitialData.getGlobalComponentIds()) {
			globalDataManager.addGlobalComponentId(globalComponentId);
		}

		for (GlobalPropertyId globalPropertyId : globalInitialData.getGlobalPropertyIds()) {
			PropertyDefinition globalPropertyDefinition = globalInitialData.getGlobalPropertyDefinition(globalPropertyId);
			globalDataManager.addGlobalPropertyDefinition(globalPropertyId, globalPropertyDefinition);
			final Object globalPropertyValue = globalInitialData.getGlobalPropertyValue(globalPropertyId);
			if (globalPropertyValue != null) {
				final PropertyDefinition propertyDefinition = globalDataManager.getGlobalPropertyDefinition(globalPropertyId);
				validateValueCompatibility(dataManagerContext, globalPropertyId, propertyDefinition, globalPropertyValue);
				globalDataManager.setGlobalPropertyValue(globalPropertyId, globalPropertyValue);
			}
		}

		for (GlobalComponentId globalComponentId : globalInitialData.getGlobalComponentIds()) {
			Consumer<AgentContext> consumer = globalInitialData.getGlobalComponentInitialBehavior(globalComponentId);
			dataManagerContext.resolveEvent(new ComponentConstructionEvent(globalComponentId, consumer));
		}

		dataManagerContext.publishDataView(new GlobalDataView(dataManagerContext, globalDataManager));
		globalInitialData = null;
	}

	private void handleGlobalComponentConstructionEventValidation(final DataManagerContext dataManagerContext, final GlobalComponentConstructionEvent globalComponentConstructionEvent) {
		validateCurrentAgentIsEventSource(dataManagerContext);
		validateGlobalComponentId(dataManagerContext, globalComponentConstructionEvent.getGlobalComponentId());
		validateConsumerNotNull(dataManagerContext, globalComponentConstructionEvent.getConsumer());
	}

	private void validateGlobalComponentId(final DataManagerContext dataManagerContext, GlobalComponentId globalComponentId) {
		if (globalComponentId == null) {
			dataManagerContext.throwContractException(GlobalError.NULL_GLOBAL_COMPONENT_ID);
		}

		if (componentDataView.containsComponentId(globalComponentId)) {
			dataManagerContext.throwContractException(GlobalError.DUPLICATE_GLOBAL_COMPONENT_ID);
		}
	}

	private void validateConsumerNotNull(final DataManagerContext dataManagerContext, Consumer<AgentContext> consumer) {
		if (consumer == null) {
			dataManagerContext.throwContractException(NucleusError.NULL_AGENT_CONTEXT_CONSUMER);
		}
	}

	private void handleGlobalComponentConstructionEventExecution(final DataManagerContext dataManagerContext, final GlobalComponentConstructionEvent globalComponentConstructionEvent) {
		GlobalComponentId globalComponentId = globalComponentConstructionEvent.getGlobalComponentId();
		Consumer<AgentContext> consumer = globalComponentConstructionEvent.getConsumer();
		globalDataManager.addGlobalComponentId(globalComponentId);
		dataManagerContext.resolveEvent(new ComponentConstructionEvent(globalComponentId, consumer));
	}

	private void handleGlobalPropertyValueAssignmentEventExecution(final DataManagerContext dataManagerContext, final GlobalPropertyValueAssignmentEvent globalPropertyValueAssignmentEvent) {
		final GlobalPropertyId globalPropertyId = globalPropertyValueAssignmentEvent.getGlobalPropertyId();
		final Object globalPropertyValue = globalPropertyValueAssignmentEvent.getGlobalPropertyValue();
		validateGlobalPropertyId(dataManagerContext, globalPropertyId);
		final Object oldPropertyValue = globalDataManager.getGlobalPropertyValue(globalPropertyId);
		globalDataManager.setGlobalPropertyValue(globalPropertyId, globalPropertyValue);
		dataManagerContext.resolveEvent(new GlobalPropertyChangeObservationEvent(globalPropertyId, oldPropertyValue, globalPropertyValue));
	}

	private void handleGlobalPropertyValueAssignmentEventValidation(final DataManagerContext dataManagerContext, final GlobalPropertyValueAssignmentEvent globalPropertyValueAssignmentEvent) {
		final GlobalPropertyId globalPropertyId = globalPropertyValueAssignmentEvent.getGlobalPropertyId();
		final Object globalPropertyValue = globalPropertyValueAssignmentEvent.getGlobalPropertyValue();

		validateGlobalPropertyId(dataManagerContext, globalPropertyId);
		validateGlobalPropertyValueNotNull(dataManagerContext, globalPropertyValue);
		final PropertyDefinition propertyDefinition = globalDataManager.getGlobalPropertyDefinition(globalPropertyId);
		validatePropertyMutability(dataManagerContext, propertyDefinition);
		validateValueCompatibility(dataManagerContext, globalPropertyId, propertyDefinition, globalPropertyValue);
	}

	private void validateGlobalPropertyId(final DataManagerContext dataManagerContext, final GlobalPropertyId globalPropertyId) {
		if (globalPropertyId == null) {
			dataManagerContext.throwContractException(GlobalError.NULL_GLOBAL_PROPERTY_ID);
		}

		if (!globalDataManager.globalPropertyIdExists(globalPropertyId)) {
			dataManagerContext.throwContractException(GlobalError.UNKNOWN_GLOBAL_PROPERTY_ID, globalPropertyId);
		}
	}

	private void validateGlobalPropertyValueNotNull(final DataManagerContext dataManagerContext, final Object propertyValue) {
		if (propertyValue == null) {
			dataManagerContext.throwContractException(GlobalError.NULL_GLOBAL_PROPERTY_VALUE);
		}
	}

	private void validatePropertyMutability(final DataManagerContext dataManagerContext, final PropertyDefinition propertyDefinition) {
		if (!propertyDefinition.propertyValuesAreMutable()) {
			dataManagerContext.throwContractException(PropertyError.IMMUTABLE_VALUE);
		}
	}

	private void validateValueCompatibility(final DataManagerContext dataManagerContext, final Object propertyId, final PropertyDefinition propertyDefinition, final Object propertyValue) {
		if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
			dataManagerContext.throwContractException(PropertyError.INCOMPATIBLE_VALUE,
					"Property value " + propertyValue + " is not of type " + propertyDefinition.getType().getName() + " and does not match definition of " + propertyId);
		}
	}

	private void validateCurrentAgentIsEventSource(final DataManagerContext dataManagerContext) {
		if (!dataManagerContext.currentAgentIsEventSource()) {
			dataManagerContext.throwContractException(ComponentError.AGENT_EXCLUSIVE_EVENT);
		}
	}

}
