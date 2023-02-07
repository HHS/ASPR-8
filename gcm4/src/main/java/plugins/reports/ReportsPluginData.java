package plugins.reports;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.jcip.annotations.Immutable;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import nucleus.ReportContext;
import plugins.reports.support.ReportError;
import util.errors.ContractException;

/**
 * An immutable container of the initial state of report actors. It contains: <BR>
 * <ul>
 * <li>report labels</li>
 * <li>suppliers of consumers of {@linkplain ReportContext} for report
 * initialization</li>
 * </ul>
 * 
 *
 */
@Immutable
public final class ReportsPluginData implements PluginData {

	private static class Data {
		private Set<Supplier<Consumer<ReportContext>>> reports = new LinkedHashSet<>();

		public Data() {
		}

		public Data(Data data) {
			//this.reports.addAll(data.reports);
			this.reports.addAll(data.reports);
		}
	}

	public final static Builder builder() {
		return new Builder(new Data());
	}

	public final static class Builder implements PluginDataBuilder {
		private Data data;
		
		private boolean dataIsMutable;
		
		private void ensureDataMutability() {
			if(!dataIsMutable) {
				data = new Data(data);
				dataIsMutable = true;
			}
		}
		
		private Builder(Data data) {
			this.data = data;
		}

		/**
		 * Returns the {@link ReportsInitialData} from the input collected by
		 * this builder. Clears the state of the builder.
		 */
		public ReportsPluginData build() {
			try {
				return new ReportsPluginData(data);
			} finally {
				data = new Data();
			}
		}

		/**
		 * Adds the report via its id and it initial behavior as supplied by the
		 * given supplier.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain ReportError#NULL_SUPPLIER} if the
		 *             supplier is null</li>
		 */
		public Builder addReport(Supplier<Consumer<ReportContext>> supplier) {
			ensureDataMutability();
			if (supplier == null) {
				throw new ContractException(ReportError.NULL_SUPPLIER);
			}
			data.reports.add(supplier);
			return this;
		}

	}

	private final Data data;

	private ReportsPluginData(Data data) {
		this.data = data;
	}

	/**
	 * Returns the Consumers of ReportContext 
	 *             
	 */
	public Set<Consumer<ReportContext>> getReports() {
		Set<Consumer<ReportContext>> result=  new LinkedHashSet<>();
		for(Supplier<Consumer<ReportContext>> supplier : data.reports) {
			result.add(supplier.get());
		}
		return result;
	}

	@Override
	public PluginDataBuilder getCloneBuilder() {
		return new Builder(data);
	}

}
