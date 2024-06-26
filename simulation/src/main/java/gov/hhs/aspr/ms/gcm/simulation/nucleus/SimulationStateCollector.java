package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SimulationStateCollector implements Consumer<ExperimentContext> {
	private final BiConsumer<Integer, List<Object>> outputConsumer;
	private final Consumer<ExperimentContext> experimentOpenConsumer;

	private final Map<Integer, List<Object>> observedOutputObjects = new LinkedHashMap<>();

	public SimulationStateCollector(BiConsumer<Integer, List<Object>> outputConsumer,
			Consumer<ExperimentContext> experimentOpenConsumer) {
		this.outputConsumer = outputConsumer;
		this.experimentOpenConsumer = experimentOpenConsumer;
	}

	@Override
	public synchronized void accept(ExperimentContext experimentContext) {
		experimentContext.subscribeToExperimentOpen(this.experimentOpenConsumer);
		experimentContext.subscribeToOutput(PluginData.class, this::handlePluginDataOuput);
		experimentContext.subscribeToOutput(SimulationState.class, this::handleSimulationTimeOuput);
		experimentContext.subscribeToSimulationClose(this::handleSimulationClose);
	}

	private synchronized void handleSimulationClose(ExperimentContext experimentContext, Integer scenarioId) {
		List<Object> list = observedOutputObjects.remove(scenarioId);
		if (list != null) {
			outputConsumer.accept(scenarioId, list);
		}
	}

	private synchronized void handlePluginDataOuput(ExperimentContext experimentContext, Integer scenarioId,
			PluginData pluginData) {
		List<Object> list = observedOutputObjects.get(scenarioId);
		if (list == null) {
			list = new ArrayList<>();
			observedOutputObjects.put(scenarioId, list);
		}
		list.add(pluginData);
	}

	private synchronized void handleSimulationTimeOuput(ExperimentContext experimentContext, Integer scenarioId,
			SimulationState simulationState) {
		List<Object> list = observedOutputObjects.get(scenarioId);
		if (list == null) {
			list = new ArrayList<>();
			observedOutputObjects.put(scenarioId, list);
		}
		list.add(simulationState);
	}

}
