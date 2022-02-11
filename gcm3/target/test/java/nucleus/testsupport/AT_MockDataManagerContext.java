package nucleus.testsupport;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import util.annotations.UnitTest;

@UnitTest(target = MockDataManagerContext.class)
public class AT_MockDataManagerContext {
	
	@Test
	@Disabled
	public void test() {
		
	}
	
//	builder()
//	addAgent(Consumer<AgentContext>, AgentId)
//	addEventLabeler(EventLabeler<T>)
//	addKeyedPassivePlan(Consumer<DataManagerContext>, double, Object)
//	addKeyedPlan(Consumer<DataManagerContext>, double, Object)
//	addPassivePlan(Consumer<DataManagerContext>, double)
//	addPlan(Consumer<DataManagerContext>, double)
//	agentExists(AgentId)
//	getCurrentAgentId()
//	getDataManager(Class<T>)
//	getPlan(Object)
//	getPlanKeys()
//	getPlanTime(Object)
//	getTime()
//	halt()
//	releaseOutput(Object)
//	removeAgent(AgentId)
//	removePlan(Object)
//	resolveEvent(Event)
//	subscribersExistForEvent(Class<? extends Event>)
//	subscribeToEventExecutionPhase(Class<T>, DataManagerEventConsumer<T>)
//	subscribeToEventPostPhase(Class<T>, DataManagerEventConsumer<T>)
//	unSubscribeToEvent(Class<? extends Event>)
	
//	build()
//	setAddAgentConsumer(BiConsumer<Consumer<AgentContext>, AgentId>)
//	setAddEventLabelerConsumer(Consumer<EventLabeler<?>>)
//	setAddKeyedPlanConsumer(TriConsumer<Consumer<DataManagerContext>, Double, Object>)
//	setAddPassiveKeyedPlanConsumer(TriConsumer<Consumer<DataManagerContext>, Double, Object>)
//	setAddPassivePlanConsumer(BiConsumer<Consumer<DataManagerContext>, Double>)
//	setAddPlanConsumer(BiConsumer<Consumer<DataManagerContext>, Double>)
//	setAgentExistsFunction(Function<AgentId, Boolean>)
//	setDataViewFunction(Function<Class<?>, ?>)
//	setGetCurrentAgentIdSupplier(Supplier<AgentId>)
//	setGetPlanFunction(Function<Object, Consumer<? extends DataManagerContext>>)
//	setGetPlanKeysSupplier(Supplier<List<Object>>)
//	setGetPlanTimeFunction(Function<Object, Double>)
//	setHaltRunable(Runnable)
//	setQueueEventForResolutionConsumer(Consumer<Event>)
//	setReleaseOutputConsumer(Consumer<Object>)
//	setRemoveAgentConsumer(Consumer<AgentId>)
//	setRemovePlanFunction(Function<Object, Object>)
//	setSubscribersExistForEventFunction(Function<Class<? extends Event>, Boolean>)
//	setSubscribeToEventExecutionPhaseConsumer(BiConsumer<Class<?>, DataManagerEventConsumer<?>>)
//	setSubscribeToEventPostPhaseConsumer(BiConsumer<Class<?>, DataManagerEventConsumer<?>>)
//	setTimeSupplier(Supplier<Double>)
//	setUnSubscribeToEventConsumer(Consumer<Class<? extends Event>>)	

}
