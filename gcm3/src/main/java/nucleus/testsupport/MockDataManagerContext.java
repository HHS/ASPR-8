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
import nucleus.Context;
import nucleus.DataView;
import nucleus.Event;
import nucleus.EventLabeler;
import nucleus.ReportContext;
import nucleus.ReportId;
import nucleus.ResolverContext;
import nucleus.ResolverEventConsumer;
import nucleus.ResolverId;
import util.ContractError;
import util.ContractException;
import util.TriConsumer;

/**
 * A mock implementation of the {@link ResolverContext} interface that allows
 * for client overrides to behaviors through a builder pattern.
 * 
 * @author Shawn Hatch
 *
 */

public final class MockResolverContext implements ResolverContext {

	private static class Scaffold {
		public Consumer<Object>	releaseOutputConsumer = (o) -> {
		};

		public Function<Class<?>, ?> dataViewFunction = (c) -> {
			return null;
		};

		public Supplier<Double> timeSupplier = () -> {
			return 0.0;
		};

		public Consumer<ContractError> contractErrorConsumer = (c) -> {
			throw new ContractException(c);
		};

		public BiConsumer<ContractError, Object> detailedContractErrorConsumer = (c, d) -> {
			throw new ContractException(c, d);
		};

		public BiConsumer<Consumer<ResolverContext>, Double> addPlanConsumer = (c, d) -> {
		};


		public TriConsumer<Consumer<ResolverContext>, Double, Object> addKeyedPlanConsumer = (c, d, k) -> {
		};


		public Function<Object, Consumer<? extends ResolverContext>> getPlanFunction = (k) -> {
			return null;
		};


		public Function<Object, Double> getPlanTimeFunction = (o) -> 0.0;

		public Function<Object, Object> removePlanFunction = (o) -> null;

		public Supplier<List<Object>> getPlanKeysSupplier = () -> {
			return new ArrayList<>();
		};

		public Supplier<Boolean> currentAgentIsEventSourceSupplier = () -> {
			return false;
		};

		public Consumer<Event> queueEventForResolutionConsumer = (e) -> {

		};

		public Supplier<AgentId> getAvailableAgentIdSupplier = () -> {
			return null;
		};

		public Supplier<AgentId> getCurrentAgentIdSupplier = () -> {
			return null;
		};

		public Supplier<ResolverId> getCurrentResolverIdSupplier = () -> {
			return null;
		};

		public Function<AgentId, Boolean> agentExistsFunction = (a) -> {
			return false;
		};

		public BiConsumer<Consumer<AgentContext>, AgentId> addAgentConsumer = (c, a) -> {

		};

		public Consumer<AgentId> removeAgentConsumer = (a) -> {
		};


		public BiConsumer<ReportId, Consumer<ReportContext>> addReportConsumer = (r, c) -> {
		};

		public Runnable haltRunable = () -> {
		};

		public BiConsumer<Class<?>, ResolverEventConsumer<?>> subscribeToEventValidationPhaseConsumer = (c, r) -> {
		};

		public BiConsumer<Class<?>, ResolverEventConsumer<?>> subscribeToEventExecutionPhaseConsumer = (c, r) -> {
		};


		public BiConsumer<Class<?>, ResolverEventConsumer<?>> subscribeToEventPostPhaseConsumer = (c, r) -> {
		};

		public Consumer<Class<? extends Event>> unSubscribeToEventConsumer = (c) -> {

		};

		public Consumer<EventLabeler<?>> addEventLabelerConsumer = (e) -> {
		};

		public Supplier<Context> getSafeContextSupplier = () -> {
			return new Context() {

				@Override
				public void releaseOutput(Object output) {

				}

				@Override
				public <T extends DataView> Optional<T> getDataView(Class<T> dataViewClass) {
					return Optional.empty();
				}

				@Override
				public double getTime() {
					return 0;
				}

				@Override
				public void throwContractException(ContractError recoverableError) {
					throw new ContractException(recoverableError);
				}

				@Override
				public void throwContractException(ContractError recoverableError, Object details) {
					throw new ContractException(recoverableError, details);

				}
			};
		};

		public Function<Class<? extends Event>, Boolean> subscribersExistForEventFunction = (c) -> {
			return false;
		};

		public Consumer<DataView> publishDataViewConsumer = (d) -> {

		};

	}

	private final Scaffold scaffold;

	private MockResolverContext(Scaffold scaffold) {
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

		public MockResolverContext build() {
			try {
				return new MockResolverContext(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}

		public Builder setReleaseOutputConsumer(Consumer<Object> releaseOutputConsumer) {
			scaffold.releaseOutputConsumer = releaseOutputConsumer;
			return this;
		}

		public Builder setDataViewFunction(Function<Class<?>, ?> dataViewFunction) {
			scaffold.dataViewFunction = dataViewFunction;
			return this;
		};

		public Builder setTimeSupplier(Supplier<Double> timeSupplier) {
			scaffold.timeSupplier = timeSupplier;
			return this;
		};

		public Builder setContractErrorConsumer(Consumer<ContractError> contractErrorConsumer) {
			scaffold.contractErrorConsumer = contractErrorConsumer;
			return this;
		};

		public Builder setDetailedContractErrorConsumer(BiConsumer<ContractError, Object> detailedContractErrorConsumer) {
			scaffold.detailedContractErrorConsumer = detailedContractErrorConsumer;
			return this;
		}

		public Builder setAddPlanConsumer(BiConsumer<Consumer<ResolverContext>, Double> addPlanConsumer) {
			scaffold.addPlanConsumer = addPlanConsumer;
			return this;
		}

		public Builder setAddKeyedPlanConsumer(TriConsumer<Consumer<ResolverContext>, Double, Object> addKeyedPlanConsumer) {
			scaffold.addKeyedPlanConsumer = addKeyedPlanConsumer;
			return this;
		}

		public Builder setGetPlanFunction(Function<Object, Consumer<? extends ResolverContext>> getPlanFunction) {
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

		public Builder setCurrentAgentIsEventSourceSupplier(Supplier<Boolean> currentAgentIsEventSourceSupplier) {
			scaffold.currentAgentIsEventSourceSupplier = currentAgentIsEventSourceSupplier;
			return this;
		}

		public Builder setQueueEventForResolutionConsumer(Consumer<Event> queueEventForResolutionConsumer) {
			scaffold.queueEventForResolutionConsumer = queueEventForResolutionConsumer;
			return this;
		}

		public Builder setGetAvailableAgentIdSupplier(Supplier<AgentId> getAvailableAgentIdSupplier) {
			scaffold.getAvailableAgentIdSupplier = getAvailableAgentIdSupplier;
			return this;
		}

		public Builder setGetCurrentAgentIdSupplier(Supplier<AgentId> getCurrentAgentIdSupplier) {
			scaffold.getCurrentAgentIdSupplier = getCurrentAgentIdSupplier;
			return this;
		}

		public Builder setGetCurrentResolverIdSupplier(Supplier<ResolverId> getCurrentResolverIdSupplier) {
			scaffold.getCurrentResolverIdSupplier = getCurrentResolverIdSupplier;
			return this;
		}

		public Builder setAgentExistsFunction(Function<AgentId, Boolean> agentExistsFunction) {
			scaffold.agentExistsFunction = agentExistsFunction;
			return this;
		}

		public Builder setAddAgentConsumer(BiConsumer<Consumer<AgentContext>, AgentId> addAgentConsumer) {
			scaffold.addAgentConsumer = addAgentConsumer;
			return this;
		}

		public Builder setRemoveAgentConsumer(Consumer<AgentId> removeAgentConsumer) {
			scaffold.removeAgentConsumer = removeAgentConsumer;
			return this;
		}

		public Builder setAddReportConsumer(BiConsumer<ReportId, Consumer<ReportContext>> addReportConsumer) {
			scaffold.addReportConsumer = addReportConsumer;
			return this;
		}

		public Builder setHaltRunable(Runnable haltRunable) {
			scaffold.haltRunable = haltRunable;
			return this;
		}

		public Builder setSubscribeToEventValidationPhaseConsumer(BiConsumer<Class<?>, ResolverEventConsumer<?>> subscribeToEventValidationPhaseConsumer) {
			scaffold.subscribeToEventValidationPhaseConsumer = subscribeToEventValidationPhaseConsumer;
			return this;
		}

		public Builder setSubscribeToEventExecutionPhaseConsumer(BiConsumer<Class<?>, ResolverEventConsumer<?>> subscribeToEventExecutionPhaseConsumer) {
			scaffold.subscribeToEventExecutionPhaseConsumer = subscribeToEventExecutionPhaseConsumer;
			return this;
		}

		public Builder setSubscribeToEventPostPhaseConsumer(BiConsumer<Class<?>, ResolverEventConsumer<?>> subscribeToEventPostPhaseConsumer) {
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

		public Builder setGetSafeContextSupplier(Supplier<Context> getSafeContextSupplier) {
			scaffold.getSafeContextSupplier = getSafeContextSupplier;
			return this;
		}

		public Builder setSubscribersExistForEventFunction(Function<Class<? extends Event>, Boolean> subscribersExistForEventFunction) {
			scaffold.subscribersExistForEventFunction = subscribersExistForEventFunction;
			return this;
		}

		public Builder setPublishDataViewConsumer(Consumer<DataView> publishDataViewConsumer) {
			scaffold.publishDataViewConsumer = publishDataViewConsumer;
			return this;
		}

	}

	@Override
	public void releaseOutput(Object output) {
		scaffold.releaseOutputConsumer.accept(output);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends DataView> Optional<T> getDataView(Class<T> dataViewClass) {
		return Optional.ofNullable((T) scaffold.dataViewFunction.apply(dataViewClass));
	}

	@Override
	public double getTime() {
		return scaffold.timeSupplier.get();
	}

	@Override
	public void throwContractException(ContractError recoverableError) {
		scaffold.contractErrorConsumer.accept(recoverableError);
	}

	@Override
	public void throwContractException(ContractError recoverableError, Object details) {
		scaffold.detailedContractErrorConsumer.accept(recoverableError, details);
	}

	@Override
	public void addPlan(Consumer<ResolverContext> plan, double planTime) {
		scaffold.addPlanConsumer.accept(plan, planTime);
	}

	@Override
	public void addPlan(Consumer<ResolverContext> plan, double planTime, Object key) {
		scaffold.addKeyedPlanConsumer.accept(plan, planTime, key);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Consumer<ResolverContext>> T getPlan(Object key) {
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
	public boolean currentAgentIsEventSource() {
		return scaffold.currentAgentIsEventSourceSupplier.get();
	}

	@Override
	public void queueEventForResolution(Event event) {
		scaffold.queueEventForResolutionConsumer.accept(event);
	}

	@Override
	public AgentId getAvailableAgentId() {
		return scaffold.getAvailableAgentIdSupplier.get();
	}

	@Override
	public AgentId getCurrentAgentId() {
		return scaffold.getCurrentAgentIdSupplier.get();
	}

	@Override
	public ResolverId getCurrentResolverId() {
		return scaffold.getCurrentResolverIdSupplier.get();
	}

	@Override
	public boolean agentExists(AgentId agentId) {
		return scaffold.agentExistsFunction.apply(agentId);
	}

	@Override
	public void addAgent(Consumer<AgentContext> init, AgentId agentId) {
		scaffold.addAgentConsumer.accept(init, agentId);
	}

	@Override
	public void removeAgent(AgentId agentId) {
		scaffold.removeAgentConsumer.accept(agentId);
	}

	@Override
	public void addReport(ReportId reportId, Consumer<ReportContext> init) {
		scaffold.addReportConsumer.accept(reportId, init);
	}

	@Override
	public void halt() {
		scaffold.haltRunable.run();
	}

	@Override
	public <T extends Event> void subscribeToEventValidationPhase(Class<T> eventClass, ResolverEventConsumer<T> resolverConsumer) {
		scaffold.subscribeToEventValidationPhaseConsumer.accept(eventClass, resolverConsumer);
	}

	@Override
	public <T extends Event> void subscribeToEventExecutionPhase(Class<T> eventClass, ResolverEventConsumer<T> resolverConsumer) {
		scaffold.subscribeToEventExecutionPhaseConsumer.accept(eventClass, resolverConsumer);
	}

	@Override
	public <T extends Event> void subscribeToEventPostPhase(Class<T> eventClass, ResolverEventConsumer<T> resolverConsumer) {
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
	public Context getSafeContext() {
		return scaffold.getSafeContextSupplier.get();
	}

	@Override
	public boolean subscribersExistForEvent(Class<? extends Event> eventClass) {
		return scaffold.subscribersExistForEventFunction.apply(eventClass);
	}

	@Override
	public void publishDataView(DataView dataView) {
		scaffold.publishDataViewConsumer.accept(dataView);
	}

}
