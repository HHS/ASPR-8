package plugins.components.datacontainers;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import nucleus.AgentId;
import nucleus.ResolverContext;
import plugins.components.ComponentPlugin;
import plugins.components.support.ComponentId;

/**
 * Mutable data manager that backs the {@linkplain ComponentDataView}. This data
 * manager is for internal use by the {@link ComponentPlugin} and should not be
 * published.
 * 
 * This data manager manages the association(one to one) between agents and
 * component ids. Such agents are referred to as components. Limited validation
 * of inputs are performed.
 * 
 * @author Shawn Hatch
 *
 */
public final class ComponentDataManager {

	private static class ComponentRecord {

		private final ComponentId componentId;
		private final AgentId agentId;

		public ComponentRecord(AgentId agentId, ComponentId componentId) {
			super();
			this.componentId = componentId;
			this.agentId = agentId;
		}
	}

	private Map<AgentId, ComponentRecord> agentMap = new LinkedHashMap<>();
	private Map<ComponentId, ComponentRecord> componentMap = new LinkedHashMap<>();

	private ComponentRecord getFocalComponentRecord() {
		AgentId currentAgentId = context.getCurrentAgentId();
		return this.agentMap.get(currentAgentId);
	}

	/**
	 * Returns the component id of the current agent in the resolver context.
	 * Returns null if that agent was not mapped to a component id or if the
	 * current agent is null;
	 */
	@SuppressWarnings("unchecked")
	public <T extends ComponentId> T getFocalComponentId() {
		ComponentRecord focalComponentRecord = getFocalComponentRecord();
		if (focalComponentRecord != null) {
			return (T) focalComponentRecord.componentId;
		}
		return null;
	}

	/**
	 * Returns the component ids added to this data manager
	 */
	public Set<ComponentId> getComponentIds() {
		Set<ComponentId> result = new LinkedHashSet<>();
		for (ComponentRecord componentRecord : agentMap.values()) {
			result.add(componentRecord.componentId);
		}
		return result;
	}

	/**
	 * Returns all component ids that are assignable to the given class
	 * 
	 * @throws RuntimeException
	 *             <li>if the component id class reference is null</li>
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <T extends ComponentId> Set<T> getComponentIds(Class<T> componentIdClass) {
		if (componentIdClass == null) {
			throw new RuntimeException("null component id class reference");
		}
		Set<T> result = new LinkedHashSet<>();
		for (ComponentId componentId : this.componentMap.keySet()) {
			if (componentIdClass.isAssignableFrom(componentId.getClass())) {
				result.add((T) componentId);
			}
		}
		return result;
	}

	/**
	 * Associates the agent id with the component id
	 * 
	 * @throws RuntimeException
	 *             <li>if the agent id is null</li>
	 *             <li>if the component id is null</li>
	 *             <li>if the agent id was previously associated with a
	 *             component id</li>
	 *             <li>if the component id was previously associated with an
	 *             agent id</li>
	 */
	public void addComponentData(AgentId agentId, ComponentId componentId) {
		if (agentId == null) {
			throw new RuntimeException("null agent id");
		}

		if (componentId == null) {
			throw new RuntimeException("null component id");
		}

		if (agentMap.containsKey(agentId)) {
			throw new RuntimeException("agent " + agentId + "previously associated with component " + agentMap.get(agentId).componentId);
		}
		if (componentMap.containsKey(componentId)) {
			throw new RuntimeException("component " + componentId + "previously associated with agent " + componentMap.get(componentId).agentId);
		}

		ComponentRecord componentRecord = new ComponentRecord(agentId, componentId);

		agentMap.put(agentId, componentRecord);

		componentMap.put(componentId, componentRecord);
	}

	private ResolverContext context;

	/**
	 * Creates a ComponentDataManager from the given ResolverContext
	 * 
	 */
	public ComponentDataManager(ResolverContext context) {
		this.context = context;
	}

	/**
	 * Returns the AgentId for the given ComponentId. Requires a non-null
	 * existing component id
	 * 
	 * @throws RuntimeException
	 *             <li>if the component id is null</li>
	 *             <li>if the component id is unknown</li>
	 */
	public AgentId getAgentId(ComponentId componentId) {
		return componentMap.get(componentId).agentId;
	}

	/**
	 * Returns true if and only if the component id exists. Null tolerant.
	 */
	public boolean containsComponentId(ComponentId componentId) {
		return componentMap.containsKey(componentId);
	}
}
