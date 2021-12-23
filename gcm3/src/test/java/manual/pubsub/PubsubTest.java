package manual.pubsub;

import manual.demo.identifiers.GroupType;
import manual.pubsub.compartments.Compartment;
import manual.pubsub.compartments.CompartmentComponent;
import manual.pubsub.globalcomponents.DeltaAgent;
import manual.pubsub.globalcomponents.DeltaId;
import manual.pubsub.globalcomponents.GlobalComponent;
import manual.pubsub.globalcomponents.GlobalProperty;
import manual.pubsub.globalcomponents.PopulationLoader;
import manual.pubsub.regions.Region;
import manual.pubsub.regions.RegionComponent;
import nucleus.Engine;
import nucleus.Engine.EngineBuilder;
import plugins.gcm.GCMMonolithicSupport;
import plugins.gcm.input.Scenario;
import plugins.gcm.input.ScenarioBuilder;
import plugins.gcm.input.UnstructuredScenarioBuilder;

public class PubsubTest {

	public static int COUNTER;

	private PubsubTest() {

	}

	private void execute() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		

		for (GroupType groupType : GroupType.values()) {
			scenarioBuilder.addGroupTypeId(groupType);
		}

		// scenarioBuilder.addGlobalComponentId(GlobalComponent.ALPHA,
		// AlphaAgent.class);
		// scenarioBuilder.addGlobalComponentId(GlobalComponent.BETA,
		// BetaAgent.class);
		// scenarioBuilder.addGlobalComponentId(GlobalComponent.GAMMA,
		// GammaAgent.class);
		scenarioBuilder.addGlobalComponentId(GlobalComponent.POPULATION_LOADER, () -> new PopulationLoader()::init);
		for (Region region : Region.values()) {
			scenarioBuilder.addRegionId(region, () -> new RegionComponent()::init);
		}
		for (Compartment compartment : Compartment.values()) {
			scenarioBuilder.addCompartmentId(compartment, () -> new CompartmentComponent()::init);
		}

		int deltaCount = 0;
		for (int i = 0; i < deltaCount; i++) {
			scenarioBuilder.addGlobalComponentId(new DeltaId(i), () -> new DeltaAgent()::init);
		}
		for (GlobalProperty globalProperty : GlobalProperty.values()) {
			scenarioBuilder.defineGlobalProperty(globalProperty, globalProperty.getPropertyDefinition());
		}
		Scenario scenario = scenarioBuilder.build();

		
		EngineBuilder engineBuilder =  GCMMonolithicSupport.getEngineBuilder(scenario, 34534534L);
		Engine engine = engineBuilder.build();

		// TimeElapser timeElapser = new TimeElapser();
		engine.execute();
		// System.out.println("Engine execute time " +
		// timeElapser.getElapsedSeconds() + " seconds");
		// System.out.println(timeElapser.getElapsedSeconds());
		// System.out.println("Delta Counter " + COUNTER);

		// System.out.println("PopulationPartitionImpl.KEY_COUNTER = "+
		// PopulationPartitionImpl.KEY_COUNTER);

	}

	public static void main(String[] args) {
		PubsubTest pubsubTest = new PubsubTest();
		for (int i = 0; i < 30; i++) {
			pubsubTest.execute();
		}

	}
}
