package nucleus.testsupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import nucleus.AgentContext;
import nucleus.AgentId;
import nucleus.DataManager;
import nucleus.DataManagerContext;
import nucleus.DataManagerEventConsumer;
import nucleus.DataManagerId;
import nucleus.Event;
import nucleus.EventLabeler;
import util.TriConsumer;

/**
 * A mock implementation of the {@link DataManagerContext} interface that allows
 * for client overrides to behaviors through a builder pattern.
 * 
 * @author Shawn Hatch
 *
 */

public final class MockDataManagerContext implements DataManagerContext {

	private static class Scaffold {
		public Consumer<Object>	releaseOutputConsumer = (o) -> {
		};

		public Function<Class<?>, ?> dataManagerFunction = (c) -> {
			return null;
		};

		public Supplier<Double> timeSupplier = () -> {
			return 0.0;
		};

		

		public BiConsumer<Consumer<DataManagerContext>, Double> addPlanConsumer = (c, d) -> {
		};

		
		public BiConsumer<Consumer<DataManagerContext>, Double> addPassivePlanConsumer = (c, d) -> {
		};


		public TriConsumer<Consumer<DataManagerContext>, Double, Object> addKeyedPlanConsumer = (c, d, k) -> {
		};

		public TriConsumer<Consumer<DataManagerContext>, Double, Object> addPassiveKeyedPlanConsumer = (c, d, k) -> {
		};

		public Function<Object, Consumer<? extends DataManagerContext>> getPlanFunction = (k) -> {
			return null;
		};


		public Function<Object, Double> getPlanTimeFunction = (o) -> 0.0;

		public Function<Object, Object> removePlanFunction = (o) -> null;

		public Supplier<List<Object>> getPlanKeysSupplier = () -> {
			return new ArrayList<>();
		};

		
		
		

		public Consumer<Event> queueEventForResolutionConsumer = (e) -> {

		};

		public Supplier<AgentId> getCurrentAgentIdSupplier = () -> {
			return null;
		};

		

		public Function<AgentId, Boolean> agentExistsFunction = (a) -> {
			return false;
		};
		
		public Supplier<DataManagerId> dataManagerIdSupplier = ()->{
			return null;
		};

		public Function<Consumer<AgentContext>, AgentId> addAgentFunction = (c) -> {
			return null;
		};

		public Consumer<AgentId> removeAgentConsumer = (a) -> {
		};


		

		public Runnable haltRunable = () -> {
		};

		

		public BiConsumer<Class<?>, DataManagerEventConsumer<?>> subscribeToEventExecutionPhaseConsumer = (c, r) -> {
		};


		public BiConsumer<Class<?>, DataManagerEventConsumer<?>> subscribeToEventPostPhaseConsumer = (c, r) -> {
		};

		public Consumer<Class<? extends Event>> unSubscribeToEventConsumer = (c) -> {

		};

		public Consumer<EventLabeler<?>> addEventLabelerConsumer = (e) -> {
		};

		public Function<Class<? extends Event>, Boolean> subscribersExistForEventFunction = (c) -> {
			return false;
		};

		
	}

	private final Scaffold scaffold;

	private MockDataManagerContext(Scaffold scaffold) {
		this.scaffold = scaffold;
	}

	/**
	 * Returns a Builder instance. If use defaults is true, then the default
	 * return values of null, 0, false etc are returned for suppliers and
	 * functions. Contract exceptions are thrown for consumers that designed to
	 * throw such exceptions. If use defaults is false, all non-overridden
	 * behaviors throw {@link UnsupportedOperationException} exceptions on
	 * invocation.
	 */
	public static Builder builder() {
		return new Builder();
	}

	public final static class Builder {
		private Scaffold scaffold = new Scaffold();

		public MockDataManagerContext build() {
			try {
				return new MockDataManagerContext(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}

		public Builder setReleaseOutputConsumer(Consumer<Object> releaseOutputConsumer) {
			scaffold.releaseOutputConsumer = releaseOutputConsumer;
			return this;
		}

		public Builder setDataViewFunction(Function<Class<?>, ?> dataViewFunction) {
			scaffold.dataManagerFunction = dataViewFunction;
			return this;
		};

		public Builder setTimeSupplier(Supplier<Double> timeSupplier) {
			scaffold.timeSupplier = timeSupplier;
			return this;
		};

		public Builder setAddPlanConsumer(BiConsumer<Consumer<DataManagerContext>, Double> addPlanConsumer) {
			scaffold.addPlanConsumer = addPlanConsumer;
			return this;
		}

		public Builder setAddKeyedPlanConsumer(TriConsumer<Consumer<DataManagerContext>, Double, Object> addKeyedPlanConsumer) {
			scaffold.addKeyedPlanConsumer = addKeyedPlanConsumer;
			return this;
		}

		public Builder setAddPassivePlanConsumer(BiConsumer<Consumer<DataManagerContext>, Double> addPlanConsumer) {
			scaffold.addPassivePlanConsumer = addPlanConsumer;
			return this;
		}

		public Builder setAddPassiveKeyedPlanConsumer(TriConsumer<Consumer<DataManagerContext>, Double, Object> addKeyedPlanConsumer) {
			scaffold.addPassiveKeyedPlanConsumer = addKeyedPlanConsumer;
			return this;
		}
		
		public Builder setGetPlanFunction(Function<Object, Consumer<? extends DataManagerContext>> getPlanFunction) {
			scaffold.getPlanFunction = getPlanFunction;
			return this;
		}

		public Builder setGetPlanTimeFunction(Function<Object, Double> getPlanTimeFunction) {
			scaffold.getPlanTimeFunction = getPlanTimeFunction;
			return this;
		}

		public Builder setRemovePlanFunction(Function<Object, Object> removePlanFunction) {
			scaffold.removePlanFunction = removePlanFunction;
			return this;
		}

		public Builder setGetPlanKeysSupplier(Supplier<List<Object>> getPlanKeysSupplier) {
			scaffold.getPlanKeysSupplier = getPlanKeysSupplier;
			return this;
		}

		

		public Builder setQueueEventForResolutionConsumer(Consumer<Event> queueEventForResolutionConsumer) {
			scaffold.queueEventForResolutionConsumer = queueEventForResolutionConsumer;
			return this;
		}

		

		public Builder setGetCurrentAgentIdSupplier(Supplier<AgentId> getCurrentAgentIdSupplier) {
			scaffold.getCurrentAgentIdSupplier = getCurrentAgentIdSupplier;
			return this;
		}
		

		public Builder setAgentExistsFunction(Function<AgentId, Boolean> agentExistsFunction) {
			scaffold.agentExistsFunction = agentExistsFunction;
			return this;
		}

		public Builder setDataManagerIdSupplier(Supplier<DataManagerId> dataManagerIdSupplier) {
			scaffold.dataManagerIdSupplier = dataManagerIdSupplier;
			return this;
		}
		
		public Builder setAddAgentFunction(Function<Consumer<AgentContext>, AgentId> addAgentFunction) {
			scaffold.addAgentFunction = addAgentFunction;
			return this;
		}

		public Builder setRemoveAgentConsumer(Consumer<AgentId> removeAgentConsumer) {
			scaffold.removeAgentConsumer = removeAgentConsumer;
			return this;
		}

		public Builder setHaltRunable(Runnable haltRunable) {
			scaffold.haltRunable = haltRunable;
			return this;
		}

		public Builder setSubscribeToEventExecutionPhaseConsumer(BiConsumer<Class<?>, DataManagerEventConsumer<?>> subscribeToEventExecutionPhaseConsumer) {
			scaffold.subscribeToEventExecutionPhaseConsumer = subscribeToEventExecutionPhaseConsumer;
			return this;
		}

		public Builder setSubscribeToEventPostPhaseConsumer(BiConsumer<Class<?>, DataManagerEventConsumer<?>> subscribeToEventPostPhaseConsumer) {
			scaffold.subscribeToEventPostPhaseConsumer = subscribeToEventPostPhaseConsumer;
			return this;
		}

		public Builder setUnSubscribeToEventConsumer(Consumer<Class<? extends Event>> unSubscribeToEventConsumer) {
			scaffold.unSubscribeToEventConsumer = unSubscribeToEventConsumer;
			return this;
		}

		public Builder setAddEventLabelerConsumer(Consumer<EventLabeler<?>> addEventLabelerConsumer) {
			scaffold.addEventLabelerConsumer = addEventLabelerConsumer;
			return this;
		}


		public Builder setSubscribersExistForEventFunction(Function<Class<? extends Event>, Boolean> subscribersExistForEventFunction) {
			scaffold.subscribersExistForEventFunction = subscribersExistForEventFunction;
			return this;
		}

	}

	@Override
	public void releaseOutput(Object output) {
		scaffold.releaseOutputConsumer.accept(output);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends DataManager> Optional<T> getDataManager(Class<T> dataManagerClass) {
		return Optional.ofNullable((T) scaffold.dataManagerFunction.apply(dataManagerClass));
	}


	@Override
	public double getTime() {
		return scaffold.timeSupplier.get();
	}

	@Override
	public void addPlan(Consumer<DataManagerContext> plan, double planTime) {
		scaffold.addPlanConsumer.accept(plan, planTime);
	}
	
	@Override
	public void addPassivePlan(Consumer<DataManagerContext> plan, double planTime) {
		scaffold.addPassivePlanConsumer.accept(plan, planTime);
	}

	@Override
	public void addKeyedPlan(Consumer<DataManagerContext> plan, double planTime, Object key) {
		scaffold.addKeyedPlanConsumer.accept(plan, planTime, key);
	}

	@Override
	public void addKeyedPassivePlan(Consumer<DataManagerContext> plan, double planTime, Object key) {
		scaffold.addPassiveKeyedPlanConsumer.accept(plan, planTime, key);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Consumer<DataManagerContext>> T getPlan(Object key) {
		return (T) scaffold.getPlanFunction.apply(key);
	}

	@Override
	public double getPlanTime(Object key) {
		return scaffold.getPlanTimeFunction.apply(key);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Optional<T> removePlan(Object key) {
		return Optional.ofNullable((T) scaffold.removePlanFunction.apply(key));
	}

	@Override
	public List<Object> getPlanKeys() {
		return scaffold.getPlanKeysSupplier.get();
	}


	@Override
	public void resolveEvent(Event event) {
		scaffold.queueEventForResolutionConsumer.accept(event);
	}

	

	@Override
	public Optional<AgentId> getCurrentAgentId() {
		return Optional.ofNullable(scaffold.getCurrentAgentIdSupplier.get());
	}

	@Override
	public boolean agentExists(AgentId agentId) {
		return scaffold.agentExistsFunction.apply(agentId);
	}

	@Override
	public AgentId addAgent(Consumer<AgentContext> consumer) {
		return scaffold.addAgentFunction.apply(consumer);
	}

	@Override
	public void removeAgent(AgentId agentId) {
		scaffold.removeAgentConsumer.accept(agentId);
	}

	@Override
	public void halt() {
		scaffold.haltRunable.run();
	}

	@Override
	public <T extends Event> void subscribeToEventExecutionPhase(Class<T> eventClass, DataManagerEventConsumer<T> resolverConsumer) {
		scaffold.subscribeToEventExecutionPhaseConsumer.accept(eventClass, resolverConsumer);
	}

	@Override
	public <T extends Event> void subscribeToEventPostPhase(Class<T> eventClass, DataManagerEventConsumer<T> resolverConsumer) {
		scaffold.subscribeToEventPostPhaseConsumer.accept(eventClass, resolverConsumer);
	}

	@Override
	public void unSubscribeToEvent(Class<? extends Event> eventClass) {
		scaffold.unSubscribeToEventConsumer.accept(eventClass);
	}

	@Override
	public <T extends Event> void addEventLabeler(EventLabeler<T> eventLabeler) {
		scaffold.addEventLabelerConsumer.accept(eventLabeler);
	}

	@Override
	public boolean subscribersExistForEvent(Class<? extends Event> eventClass) {
		return scaffold.subscribersExistForEventFunction.apply(eventClass);
	}

	@Override
	public DataManagerId getDataManagerId() {		
		return scaffold.dataManagerIdSupplier.get();
	}

}
