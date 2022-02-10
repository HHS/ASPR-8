package nucleus.testsupport;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import nucleus.DataManager;
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

}
