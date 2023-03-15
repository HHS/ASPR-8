package lesson;

import java.util.function.Consumer;


import nucleus.ExperimentContext;
import nucleus.Plugin;
import nucleus.SimulationTime;

public class Serializer implements Consumer<ExperimentContext>{

	@Override
	public void accept(ExperimentContext experimentContext) {
		
		experimentContext.subscribeToExperimentOpen(this::handleExperimentOpen);
		experimentContext.subscribeToOutput(Plugin.class, this::handlePluginOuput);
		experimentContext.subscribeToOutput(SimulationTime.class, this::handleSimulationTimeOuput);
		experimentContext.subscribeToSimulationClose(this::handleSimulationClose);
	}
	
	private synchronized void  handleExperimentOpen(ExperimentContext experimentContext) {
		
	}
	
	private synchronized void handleSimulationClose(ExperimentContext experimentContext, Integer scenarioId) {
		
	}
	
	private synchronized void handlePluginOuput(ExperimentContext experimentContext, Integer scenarioId, Plugin plugin) {
		System.out.println(plugin.getPluginId()+" for scenario "+scenarioId);
	}
	private synchronized void handleSimulationTimeOuput(ExperimentContext experimentContext, Integer scenarioId, SimulationTime simulationTime) {
		System.out.println(simulationTime+" for scenario "+ scenarioId);
	}

}
