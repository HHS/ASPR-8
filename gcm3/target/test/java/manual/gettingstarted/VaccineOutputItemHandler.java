package manual.gettingstarted;

import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public final class VaccineOutputItemHandler {

//	public void openSimulation(ScenarioId scenarioId, ReplicationId replicationId) {
//		System.out.println("Simulation has started: scenario = " + scenarioId + " replication = " + replicationId);
//	}
//
//	@Override
//	public void openExperiment(ExperimentProgressLog experimentProgressLog) {
//		System.out.println("Experiment has started");
//	}
//
//	@Override
//	public void closeSimulation(ScenarioId scenarioId, ReplicationId replicationId) {
//		System.out.println("Simulation has ended: scenario = " + scenarioId + " replication = " + replicationId);
//
//	}
//
//	@Override
//	public void closeExperiment() {
//		System.out.println("Experiment has ended");
//
//	}
//
//	@Override
//	public void handle(ScenarioId scenarioId, ReplicationId replicationId, Object output) {
//		VaccineInventoryOutputItem vaccineInventoryOutputItem = (VaccineInventoryOutputItem) output;
//
//		StringBuilder sb = new StringBuilder();
//
//		sb.append(scenarioId);
//		sb.append(replicationId);
//		sb.append(vaccineInventoryOutputItem.getTime());
//		sb.append(vaccineInventoryOutputItem.getRegionId());
//		sb.append(vaccineInventoryOutputItem.getVaccineInventory());
//
//		System.out.println(sb);
//
//	}
//
//	@Override
//	public Set<Class<?>> getHandledClasses() {
//		Set<Class<?>> result = new LinkedHashSet<>();
//		result.add(VaccineInventoryOutputItem.class);
//		return result;
//	}

}
