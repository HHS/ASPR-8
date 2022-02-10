package plugins.components.datacontainers;

import nucleus.AgentId;
import nucleus.SimulationContext;
import nucleus.DataView;
import plugins.components.support.ComponentError;
import plugins.components.support.ComponentId;
import util.ContractException;

/**
 * Published data view that provides component information.
 * 
 * @author Shawn Hatch
 *
 */
public final class ComponentDataView implements DataView {
	private final ComponentDataManager componentDataManager;
	private final SimulationContext simulationContext;

	public ComponentDataView(SimulationContext simulationContext, ComponentDataManager componentDataManager) {
		this.componentDataManager = componentDataManager;
		this.simulationContext = simulationContext;
	}

	/**
	 * Returns the id of the Component agent that is currently in focus. Returns
	 * null is there is no focal component.
	 */
	public <T extends ComponentId> T getFocalComponentId() {
		return componentDataManager.getFocalComponentId();
	}

	/**
	 * Returns the AgentId for the given ComponentId
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain ComponentError#NULL_AGENT_ID} if the
	 *             component id is null</li>
	 *             <li>{@linkplain ComponentError#UNKNOWN_COMPONENT_ID} if the
	 *             component id is unknown</li>
	 * 
	 */
	public AgentId getAgentId(ComponentId componentId) {
		validateComponentId(componentId);
		return componentDataManager.getAgentId(componentId);
	}

	private void validateComponentId(ComponentId componentId) {

		if (componentId == null) {
			simulationContext.throwContractException(ComponentError.NULL_AGENT_ID);
		}

		if (!componentDataManager.containsComponentId(componentId)) {
			simulationContext.throwContractException(ComponentError.UNKNOWN_COMPONENT_ID);
		}
	}

	/**
	 * Returns true if and only if the component id exists. Null tolerant.
	 */
	public boolean containsComponentId(ComponentId componentId) {
		return componentDataManager.containsComponentId(componentId);
	}

}
