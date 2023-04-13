package lesson;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import nucleus.ExperimentContext;
import nucleus.PluginData;
import nucleus.SimulationState;

public final class StateCollector implements Consumer<ExperimentContext> {

	private final Map<Integer, List<Object>> observedOutputObjects = new LinkedHashMap<>();

	@Override
	public synchronized void accept(ExperimentContext experimentContext) {
		experimentContext.subscribeToOutput(PluginData.class, this::handlePluginDataOuput);
		experimentContext.subscribeToOutput(SimulationState.class, this::handleSimulationTimeOuput);
	}

	@SuppressWarnings("unchecked")
	public synchronized <T> Optional<T> get(Integer scenarioId, Class<T> c) {
		List<Object> list = observedOutputObjects.get(scenarioId);
		Object result = null;
		if (list != null) {
			for (Object object : list) {
				if (c.isAssignableFrom(object.getClass())) {
					if (result != null) {
						throw new RuntimeException("duplicate");
					}
					result = object;
				}
			}
		}
		return Optional.ofNullable((T) result);
	}

	@SuppressWarnings("unchecked")
	public synchronized <T> List<T> getAll(Integer scenarioId, Class<T> c) {
		List<T> result = new ArrayList<>();
		List<Object> list = observedOutputObjects.get(scenarioId);
		if (list != null) {
			for (Object object : list) {
				if (c.isAssignableFrom(object.getClass())) {
					result.add((T) object);
				}
			}
		}
		return result;
	}

	private synchronized void handlePluginDataOuput(ExperimentContext experimentContext, Integer scenarioId, PluginData pluginData) {
		List<Object> list = observedOutputObjects.get(scenarioId);
		if (list == null) {
			list = new ArrayList<>();
			observedOutputObjects.put(scenarioId, list);
		}
		list.add(pluginData);
	}

	private synchronized void handleSimulationTimeOuput(ExperimentContext experimentContext, Integer scenarioId, SimulationState simulationState) {
		List<Object> list = observedOutputObjects.get(scenarioId);
		if (list == null) {
			list = new ArrayList<>();
			observedOutputObjects.put(scenarioId, list);
		}
		list.add(simulationState);
	}

}
