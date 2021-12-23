package plugins.components.resolvers;

import java.util.function.Consumer;

import nucleus.AgentContext;
import nucleus.AgentId;
import nucleus.ResolverContext;
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
	

	private void handleComponentConstructionEventExecution(final ResolverContext resolverContext, ComponentConstructionEvent componentConstructionEvent) {
		ComponentId componentId = componentConstructionEvent.getComponentId();
		Consumer<AgentContext> consumer = componentConstructionEvent.getConsumer();
		AgentId agentId = resolverContext.getAvailableAgentId();		
		resolverContext.addAgent(consumer, agentId);
		componentDataManager.addComponentData(agentId, componentId);
	}

	private void handleComponentConstructionEventValidation(final ResolverContext resolverContext, ComponentConstructionEvent componentConstructionEvent) {
		ComponentId componentId = componentConstructionEvent.getComponentId();
		Consumer<AgentContext> consumer = componentConstructionEvent.getConsumer();
		validateCurrentAgentIsNotEventSource(resolverContext);
		validateNewComponentId(resolverContext, componentId);
		validateConsumerNotNull(resolverContext, consumer);
	}

	private void validateCurrentAgentIsNotEventSource(final ResolverContext resolverContext) {
		if (resolverContext.currentAgentIsEventSource()) {
			resolverContext.throwContractException(ComponentError.RESOLVER_EXCLUSIVE_EVENT);
		}
	}

	private void validateConsumerNotNull(final ResolverContext resolverContext, final Consumer<AgentContext> consumer) {
		if (consumer == null) {
			resolverContext.throwContractException(ComponentError.NULL_AGENT_INITIAL_BEHAVIOR_CONSUMER);
		}
	}

	private void validateNewComponentId(final ResolverContext resolverContext, final ComponentId componentId) {
		if (componentId == null) {
			resolverContext.throwContractException(ComponentError.NULL_AGENT_ID);
		}

		if (componentDataManager.containsComponentId(componentId)) {
			resolverContext.throwContractException(ComponentError.COMPONENT_ID_ALREADY_EXISTS);
		}
	}
	/**
	 * Initial behavior of this resolver.
	 * <li>Subscribes to all handled events
	 * <li>Publishes the {@linkplain ComponentDataView}</li>
	 */
	public void init(final ResolverContext resolverContext) {
		this.componentDataManager = new ComponentDataManager(resolverContext);		
		resolverContext.subscribeToEventExecutionPhase(ComponentConstructionEvent.class, this::handleComponentConstructionEventExecution);
		resolverContext.subscribeToEventValidationPhase(ComponentConstructionEvent.class, this::handleComponentConstructionEventValidation);
		resolverContext.publishDataView(new ComponentDataView(resolverContext.getSafeContext(), componentDataManager));
	}

}
