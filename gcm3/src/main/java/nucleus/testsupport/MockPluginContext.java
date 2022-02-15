package nucleus.testsupport;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import nucleus.AgentContext;
import nucleus.AgentId;
import nucleus.DataManager;
import nucleus.PluginContext;
import nucleus.PluginData;
import nucleus.PluginId;

/**
 * A mock implementation of a {@link PluginContext} that allows for the
 * retrieval of plugin dependencies and data managers and agents recorded during
 * the initialization phase of a plugin.
 */
public final class MockPluginContext implements PluginContext {

	private Set<PluginId> pluginDependencies = new LinkedHashSet<>();

	private int masterAgentIdValue;

	@Override
	public void addPluginDependency(PluginId pluginId) {
		pluginDependencies.add(pluginId);
	}

	public Set<PluginId> getPluginDependencies() {
		return new LinkedHashSet<>(pluginDependencies);
	}

	private Set<DataManager> dataManagers = new LinkedHashSet<>();

	@Override
	public void addDataManager(DataManager dataManager) {
		dataManagers.add(dataManager);
	}

	public Set<DataManager> getDataManagers() {
		return new LinkedHashSet<>(dataManagers);
	}

	private Map<AgentId, Consumer<AgentContext>> agentMap = new LinkedHashMap<>();

	@Override
	public AgentId addAgent(Consumer<AgentContext> init) {
		AgentId agentId = new AgentId(masterAgentIdValue++);
		agentMap.put(agentId, init);
		return agentId;
	}

	public Map<AgentId, Consumer<AgentContext>> getAgents() {
		return new LinkedHashMap<>(agentMap);
	}

	private Map<Class<?>, PluginData> pluginDataMap = new LinkedHashMap<>();

	@SuppressWarnings("unchecked")
	@Override
	public <T extends PluginData> Optional<T> getPluginData(Class<T> pluginDataClass) {
		return Optional.ofNullable((T) pluginDataMap.get(pluginDataClass));
	}

	public void addPluginData(PluginData pluginData) {
		pluginDataMap.put(pluginData.getClass(), pluginData);
	}

}
