package plugins.components.resolvers;

import java.util.function.Consumer;

import nucleus.AgentContext;
import nucleus.AgentId;
import nucleus.DataManagerContext;
import plugins.components.ComponentPlugin;
import plugins.components.datacontainers.ComponentDataManager;
import plugins.components.datacontainers.ComponentDataView;
import plugins.components.events.ComponentConstructionEvent;
import plugins.components.support.ComponentError;
import plugins.components.support.ComponentId;
import util.ContractException;

/**
 * 
 * Provides event resolution for the {@linkplain ComponentPlugin}.
 * <P>
 * Creates, publishes and maintains the {@linkplain ComponentDataView}.
 * </P>
 * <P>
 * Creates all compartment agents upon initialization.
 * </P>
 * 
 * 
 * 
 * <P>
 * Resolves the following events:
 * <ul>
 * 
 * <li>{@linkplain ComponentConstructionEvent} <blockquote> Creates an agent
 * from the event's agent context consumer using the available AgentId from
 * nucleus. Associates this agent with the event's ComponentId.  
 * 
 * 
 * <BR>
 * <BR>
 * Throws {@link ContractException}
 *
 * <ul>
 * <li>{@link ComponentError#RESOLVER_EXCLUSIVE_EVENT} if an agent generated the event
 * <li>{@link ComponentError#NULL_AGENT_INITIAL_BEHAVIOR_CONSUMER} if the event's agent context consumer is null
 * <li>{@link ComponentError#NULL_AGENT_ID} if the event's component id is
 * null
 * <li>{@link ComponentError#COMPONENT_ID_ALREADY_EXISTS} if the component id was
 * previously assigned to an agent 
 * </ul>
 * </blockquote></li>
 * </p>
 * 
 * 
 * 
 * @author Shawn Hatch
 *
 */

public final class ComponentResolver {

	private ComponentDataManager componentDataManager;
	

	private void handleComponentConstructionEventExecution(final DataManagerContext dataManagerContext, ComponentConstructionEvent componentConstructionEvent) {
		ComponentId componentId = componentConstructionEvent.getComponentId();
		Consumer<AgentContext> consumer = componentConstructionEvent.getConsumer();
		AgentId agentId = dataManagerContext.getAvailableAgentId();		
		dataManagerContext.addAgent(consumer, agentId);
		componentDataManager.addComponentData(agentId, componentId);
	}

	private void handleComponentConstructionEventValidation(final DataManagerContext dataManagerContext, ComponentConstructionEvent componentConstructionEvent) {
		ComponentId componentId = componentConstructionEvent.getComponentId();
		Consumer<AgentContext> consumer = componentConstructionEvent.getConsumer();
		validateCurrentAgentIsNotEventSource(dataManagerContext);
		validateNewComponentId(dataManagerContext, componentId);
		validateConsumerNotNull(dataManagerContext, consumer);
	}

	private void validateCurrentAgentIsNotEventSource(final DataManagerContext dataManagerContext) {
		if (dataManagerContext.currentAgentIsEventSource()) {
			dataManagerContext.throwContractException(ComponentError.RESOLVER_EXCLUSIVE_EVENT);
		}
	}

	private void validateConsumerNotNull(final DataManagerContext dataManagerContext, final Consumer<AgentContext> consumer) {
		if (consumer == null) {
			dataManagerContext.throwContractException(ComponentError.NULL_AGENT_INITIAL_BEHAVIOR_CONSUMER);
		}
	}

	private void validateNewComponentId(final DataManagerContext dataManagerContext, final ComponentId componentId) {
		if (componentId == null) {
			dataManagerContext.throwContractException(ComponentError.NULL_AGENT_ID);
		}

		if (componentDataManager.containsComponentId(componentId)) {
			dataManagerContext.throwContractException(ComponentError.COMPONENT_ID_ALREADY_EXISTS);
		}
	}
	/**
	 * Initial behavior of this resolver.
	 * <li>Subscribes to all handled events
	 * <li>Publishes the {@linkplain ComponentDataView}</li>
	 */
	public void init(final DataManagerContext dataManagerContext) {
		this.componentDataManager = new ComponentDataManager(dataManagerContext);		
		dataManagerContext.subscribeToEventExecutionPhase(ComponentConstructionEvent.class, this::handleComponentConstructionEventExecution);
		dataManagerContext.subscribeToEventValidationPhase(ComponentConstructionEvent.class, this::handleComponentConstructionEventValidation);
		dataManagerContext.publishDataView(new ComponentDataView(dataManagerContext.getSafeContext(), componentDataManager));
	}

}
