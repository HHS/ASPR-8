package lesson;

import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;


import nucleus.ExperimentContext;
import nucleus.Plugin;
import nucleus.PluginData;
import nucleus.SimulationTime;

public class Serializer implements Consumer<ExperimentContext>{
	private final Path outputDirectory;
	
	public Serializer(Path outputDirectory) {
		this.outputDirectory = outputDirectory;
	}
	
	@Override
	public void accept(ExperimentContext experimentContext) {
		
		experimentContext.subscribeToExperimentOpen(this::handleExperimentOpen);
		experimentContext.subscribeToOutput(Plugin.class, this::handlePluginOuput);
		experimentContext.subscribeToOutput(SimulationTime.class, this::handleSimulationTimeOuput);
		experimentContext.subscribeToSimulationClose(this::handleSimulationClose);
	}
	
	private synchronized void  handleExperimentOpen(ExperimentContext experimentContext) {
		//create dirs for
		experimentContext.getScenarioCount();
	}
	
	private synchronized void handleSimulationClose(ExperimentContext experimentContext, Integer scenarioId) {
		
	}
	
	private synchronized void handlePluginOuput(ExperimentContext experimentContext, Integer scenarioId, Plugin plugin) {
		for(PluginData pluginData : plugin.getPluginDatas()) {
			System.out.println(pluginData.getClass().getCanonicalName());
		}
		
	}
	private synchronized void handleSimulationTimeOuput(ExperimentContext experimentContext, Integer scenarioId, SimulationTime simulationTime) {
		System.out.println(simulationTime+" for scenario "+ scenarioId);
	}

}
