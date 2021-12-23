package nucleus.testsupport;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import nucleus.DataView;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.ReportContext;
import nucleus.ReportEventConsumer;
import nucleus.ReportId;
import nucleus.ResolverContext;
import util.ContractError;
import util.ContractException;

/**
 * A mock implementation of the {@link ResolverContext} interface that allows
 * for client overrides to behaviors through a builder pattern.
 * 
 * @author Shawn Hatch
 *
 */

public final class MockReportContext implements ReportContext {

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

		public BiConsumer<Consumer<ReportContext>, Double> addPlanConsumer = (c, d) -> {
		};

		public Supplier<ReportId> getCurrentReportIdSupplier = () -> {
			return null;
		};

		public Consumer<Consumer<ReportContext>> subscribeToSimulationCloseConsumer = (c) -> {

		};

		public BiConsumer<Class<?>, ReportEventConsumer<?>> subscribeToClassConsumer = (c, r) -> {
			
		};

		public BiConsumer<EventLabel<?>, ReportEventConsumer<?>> subscribeToEventLabelConsumer = (c, r) -> {
		};

		public Consumer<EventLabeler<?>> addEventLabelerConsumer = (e) -> {
		};

	}

	private final Scaffold scaffold;

	private MockReportContext(Scaffold scaffold) {
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

		public MockReportContext build() {
			try {
				return new MockReportContext(scaffold);
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

		public Builder setAddPlanConsumer(BiConsumer<Consumer<ReportContext>, Double> addPlanConsumer) {
			scaffold.addPlanConsumer = addPlanConsumer;
			return this;
		}

		public Builder setSubscribeToSimulationCloseConsumer(Consumer<Consumer<ReportContext>> subscribeToSimulationCloseConsumer) {
			scaffold.subscribeToSimulationCloseConsumer = subscribeToSimulationCloseConsumer;
			return this;
		}

		public Builder setSubscribeToClassConsumer(BiConsumer<Class<?>, ReportEventConsumer<?>> subscribeToClassConsumer) {
			scaffold.subscribeToClassConsumer = subscribeToClassConsumer;
			return this;
		}

		public Builder setSubscribeToEventLabelConsumer(BiConsumer<EventLabel<?>, ReportEventConsumer<?>> subscribeToEventLabelConsumer) {
			scaffold.subscribeToEventLabelConsumer = subscribeToEventLabelConsumer;
			return this;
		}

		public Builder setAddEventLabelerConsumer(Consumer<EventLabeler<?>> addEventLabelerConsumer) {
			scaffold.addEventLabelerConsumer = addEventLabelerConsumer;
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
	public void addPlan(Consumer<ReportContext> plan, double planTime) {
		scaffold.addPlanConsumer.accept(plan, planTime);
	}

	@Override
	public ReportId getCurrentReportId() {
		return scaffold.getCurrentReportIdSupplier.get();
	}

	@Override
	public <T extends Event> void addEventLabeler(EventLabeler<T> eventLabeler) {
		scaffold.addEventLabelerConsumer.accept(eventLabeler);
	}

	@Override
	public <T extends Event> void subscribe(Class<T> eventClass, ReportEventConsumer<T> reportConsumer) {
		scaffold.subscribeToClassConsumer.accept(eventClass, reportConsumer);
	}

	@Override
	public <T extends Event> void subscribe(EventLabel<T> eventLabel, ReportEventConsumer<T> reportEventConsumer) {
		scaffold.subscribeToEventLabelConsumer.accept(eventLabel, reportEventConsumer);
	}

	@Override
	public void subscribeToSimulationClose(Consumer<ReportContext> closeHandler) {
		scaffold.subscribeToSimulationCloseConsumer.accept(closeHandler);
	}

}
