package nucleus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SimulationStateCollector implements Consumer<ExperimentContext>{
	private final BiConsumer<Integer, List<Object>> outputConsumer;
	private final Consumer<ExperimentContext> experimentOpenConsumer;
	
	private final Map<Integer, List<Object>> observedOutputObjects = new LinkedHashMap<>(); 
	
	public SimulationStateCollector(BiConsumer<Integer, List<Object>> outputConsumer, Consumer<ExperimentContext> experimentOpenConsumer) {
		this.outputConsumer = outputConsumer;
		this.experimentOpenConsumer = experimentOpenConsumer;	
	}
	
	@Override
	public synchronized void accept(ExperimentContext experimentContext) {
		experimentContext.subscribeToExperimentOpen(this.experimentOpenConsumer);
		experimentContext.subscribeToOutput(Plugin.class, this::handlePluginOuput);
		experimentContext.subscribeToOutput(SimulationTime.class, this::handleSimulationTimeOuput);
		experimentContext.subscribeToSimulationClose(this::handleSimulationClose);
	}
	
	private synchronized void handleSimulationClose(ExperimentContext experimentContext, Integer scenarioId) {
		List<Object> list = observedOutputObjects.remove(scenarioId);
		if(list != null) {
			outputConsumer.accept(scenarioId, list);
		}
	}
	
	private synchronized void handlePluginOuput(ExperimentContext experimentContext, Integer scenarioId, Plugin plugin) {
		List<Object> list = observedOutputObjects.get(scenarioId);
		if(list == null) {
			list = new ArrayList<>();
			observedOutputObjects.put(scenarioId, list);
		}
		list.addAll(plugin.getPluginDatas());
	}
	private synchronized void handleSimulationTimeOuput(ExperimentContext experimentContext, Integer scenarioId, SimulationTime simulationTime) {
		List<Object> list = observedOutputObjects.get(scenarioId);
		if(list == null) {
			list = new ArrayList<>();
			observedOutputObjects.put(scenarioId, list);
		}
		list.add(simulationTime);
	}

}
