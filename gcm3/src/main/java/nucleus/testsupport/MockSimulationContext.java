package nucleus.testsupport;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import nucleus.AgentContext;
import nucleus.AgentId;
import nucleus.DataManager;
import nucleus.Event;
import nucleus.EventLabeler;
import nucleus.SimulationContext;

/**
 * A mock implementation of the {@link SimulationContext} interface that allows
 * for client overrides to behaviors through a builder pattern.
 * 
 * @author Shawn Hatch
 *
 */
public class MockSimulationContext implements SimulationContext {

	private static class Scaffold {
		public Consumer<Object> releaseOutputConsumer = (o) -> {
		};

		public Function<Class<?>, ?> dataManagerFunction = (c) -> {
			return null;
		};

		public Supplier<Double> timeSupplier = () -> {
			return 0.0;
		};
		
		public Runnable haltRunable = () -> {
		};
		
		public Consumer<EventLabeler<?>> addEventLabelerConsumer = (e) -> {
		};
		
		public Function<AgentId, Boolean> agentExistsFunction = (a) -> {
			return false;
		};
		
		public Consumer<AgentId> removeAgentConsumer = (a) -> {
		};
		
		public Function<Consumer<AgentContext>, AgentId> addAgentFunction = (c) -> {
			return null;
		};

	}

	private final Scaffold scaffold;

	private MockSimulationContext(Scaffold scaffold) {
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

		private Builder() {}

		public MockSimulationContext build() {
			try {
				return new MockSimulationContext(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}
		
		public Builder setAgentExistsFunction(Function<AgentId, Boolean> agentExistsFunction) {
			scaffold.agentExistsFunction = agentExistsFunction;
			return this;
		}

		public Builder setReleaseOutputConsumer(Consumer<Object> releaseOutputConsumer) {
			scaffold.releaseOutputConsumer = releaseOutputConsumer;
			return this;
		}

		public Builder setDataManagerFunction(Function<Class<?>, ?> dataManagerFunction) {
			scaffold.dataManagerFunction = dataManagerFunction;
			return this;
		};

		public Builder setTimeSupplier(Supplier<Double> timeSupplier) {
			scaffold.timeSupplier = timeSupplier;
			return this;
		};

		public Builder setHaltRunable(Runnable haltRunable) {
			scaffold.haltRunable = haltRunable;
			return this;
		}

		public Builder setAddEventLabelerConsumer(Consumer<EventLabeler<?>> addEventLabelerConsumer) {
			scaffold.addEventLabelerConsumer = addEventLabelerConsumer;
			return this;
		}
		
		public Builder setRemoveAgentConsumer(Consumer<AgentId> removeAgentConsumer) {
			scaffold.removeAgentConsumer = removeAgentConsumer;
			return this;
		}
		
		public Builder setAddAgentFunction(Function<Consumer<AgentContext>, AgentId> addAgentFunction) {
			scaffold.addAgentFunction = addAgentFunction;
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
	public void halt() {
		scaffold.haltRunable.run();
		
	}

	@Override
	public <T extends Event> void addEventLabeler(EventLabeler<T> eventLabeler) {
		scaffold.addEventLabelerConsumer.accept(eventLabeler);
		
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

}
