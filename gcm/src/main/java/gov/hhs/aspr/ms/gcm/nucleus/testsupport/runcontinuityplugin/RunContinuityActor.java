package gov.hhs.aspr.ms.gcm.nucleus.testsupport.runcontinuityplugin;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.apache.commons.math3.util.Pair;

import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.nucleus.Plan;

public class RunContinuityActor implements Consumer<ActorContext> {
	private final RunContinuityPluginData runContinuityPluginData;
	private Map<Integer, Pair<Double, Consumer<ActorContext>>> planMap = new LinkedHashMap<>();

	public RunContinuityActor(RunContinuityPluginData runContinuityPluginData) {
		this.runContinuityPluginData = runContinuityPluginData;
	}

	public void accept(ActorContext actorContext) {

		List<Pair<Double, Consumer<ActorContext>>> consumers = runContinuityPluginData.getConsumers();
		IntStream.range(0,consumers.size()).forEach((i)->{
			
			Pair<Double, Consumer<ActorContext>> pair = consumers.get(i);
			planMap.put(i, pair);
			double time = pair.getFirst();
			Consumer<ActorContext> consumer = pair.getSecond();

			RunContinuityPlanData continuityPluginData = new RunContinuityPlanData(i);

			Plan<ActorContext> plan = Plan.builder(ActorContext.class)//
					.setTime(time)//
					.setCallbackConsumer((c) -> {
						planMap.remove(i);
						consumer.accept(actorContext);	
					})//
					.setPlanData(continuityPluginData)//
					.build();

			actorContext.addPlan(plan);

		});

		actorContext.subscribeToSimulationClose(this::recordState);
	}

	private void recordState(ActorContext actorContext) {
		RunContinuityPluginData.Builder builder = RunContinuityPluginData.builder();		
		for (Pair<Double, Consumer<ActorContext>> pair : planMap.values()) {
			double time = pair.getFirst();
			Consumer<ActorContext> consumer = pair.getSecond();
			builder.addContextConsumer(time, consumer);
		}		
		actorContext.releaseOutput(builder.build());
	}

}
