package nucleus.testsupport;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import nucleus.Context;
import nucleus.DataView;
import util.ContractError;
import util.ContractException;

/**
 * A mock implementation of the {@link Context} interface that allows
 * for client overrides to behaviors through a builder pattern.
 * 
 * @author Shawn Hatch
 *
 */
public class MockContext implements Context {

	private static class Scaffold {
		public Consumer<Object> releaseOutputConsumer = (o) -> {
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

	}

	private final Scaffold scaffold;

	private MockContext(Scaffold scaffold) {
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

		public MockContext build() {
			try {
				return new MockContext(scaffold);
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

}
