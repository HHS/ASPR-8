package gov.hhs.aspr.ms.gcm.nucleus.testsupport.runcontinuityplugin;

import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.math3.util.Pair;

import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.nucleus.Plan;
import util.wrappers.MutableInteger;

public class RunContinuityActor implements Consumer<ActorContext>{
	private MutableInteger completionCount = new MutableInteger();
	private ActorContext actorContext;
	private final RunContinuityPluginData runContinuityPluginData;
	
	public RunContinuityActor(RunContinuityPluginData runContinuityPluginData) {
		this.runContinuityPluginData = runContinuityPluginData;
	}
	
	public void accept(ActorContext actorContext) {
		this.actorContext = actorContext;
		actorContext.setPlanDataConverter(RunContinuityPlanData.class, this::getConsumerFromPlanData);
		
		completionCount.setValue(runContinuityPluginData.getCompletionCount());
		
		if(!runContinuityPluginData.plansAreScheduled()) {
			List<Pair<Double, Consumer<ActorContext>>> consumers = runContinuityPluginData.getConsumers();
			for(int i = 0;i<consumers.size();i++) {
				Pair<Double, Consumer<ActorContext>> pair = consumers.get(i);
				double time = pair.getFirst();
				Consumer<ActorContext> consumer = pair.getSecond();
				
				RunContinuityPlanData continuityPluginData = new RunContinuityPlanData(i); 
				
				Plan<ActorContext> plan = Plan	.builder(ActorContext.class)//
						.setTime(time)//
						.setCallbackConsumer((c)->executePlan(consumer))//
						.setPlanData(continuityPluginData)//
						.build();				
				
				actorContext.addPlan(plan);
			}
		}		
		actorContext.subscribeToSimulationClose(this::recordState);
	}
	
	private Consumer<ActorContext> getConsumerFromPlanData(RunContinuityPlanData runContinuityPlanData){
		 Consumer<ActorContext> consumer = runContinuityPluginData.getConsumers().get(runContinuityPlanData.getId()).getSecond();
		 return (c)->executePlan(consumer);
	}
	
	private void executePlan(Consumer<ActorContext> consumer) {
		completionCount.increment();
		consumer.accept(actorContext);
	}
	
	private void recordState(ActorContext actorContext) {
		RunContinuityPluginData.Builder builder =
		RunContinuityPluginData.builder();
		
		builder.setCompletionCount(completionCount.getValue());
		List<Pair<Double, Consumer<ActorContext>>> consumers = runContinuityPluginData.getConsumers();
		for(Pair<Double, Consumer<ActorContext>> pair : consumers) {
			double time = pair.getFirst();
			Consumer<ActorContext> consumer = pair.getSecond();		
			builder.addContextConsumer(time, consumer);
		}
		builder.setPlansAreScheduled(true);
		actorContext.releaseOutput(builder.build());
	}
	
}