package gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.reports;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.StandardVersioning;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.support.PersonPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.PeriodicReportPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportPeriod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public class PersonPropertyInteractionReportPluginData extends PeriodicReportPluginData {

	private final Data data;

	private PersonPropertyInteractionReportPluginData(Data data) {
		super(data);
		this.data = data;
	}

	private static class Data extends PeriodicReportPluginData.Data {
		private final Set<PersonPropertyId> personPropertyIds = new LinkedHashSet<>();
		private boolean locked;

		private Data() {
			super();
		}

		private Data(Data data) {
			super(data);
			personPropertyIds.addAll(data.personPropertyIds);
			locked = data.locked;
		}

		/**
    	 * Standard implementation consistent with the {@link #equals(Object)} method
    	 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + Objects.hash(personPropertyIds);
			return result;
		}

		/**
    	 * Two {@link Data} instances are equal if and only if
    	 * their inputs are equal.
    	 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!super.equals(obj)) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Data other = (Data) obj;
			return Objects.equals(personPropertyIds, other.personPropertyIds);
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append(super.toString());
			builder.append(", personPropertyIds=");
			builder.append(personPropertyIds);
			builder.append("]");
			return builder.toString();
		}
	}

	public static class Builder extends PeriodicReportPluginData.Builder {
		private Data data;

		private Builder(Data data) {
			super(data);
			this.data = data;
		}

		@Override
		public PersonPropertyInteractionReportPluginData build() {
			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new PersonPropertyInteractionReportPluginData(data);
		}

		public Builder addPersonPropertyId(PersonPropertyId personPropertyId) {
			ensureDataMutability();
			if (personPropertyId != null) {
				data.personPropertyIds.add(personPropertyId);
			}
			return this;
		}

		public Builder removePersonPropertyId(PersonPropertyId personPropertyId) {
			ensureDataMutability();
			data.personPropertyIds.remove(personPropertyId);
			return this;
		}

		public Builder setReportLabel(ReportLabel reportLabel) {
			ensureDataMutability();
			super.setReportLabel(reportLabel);
			return this;
		}

		public Builder setReportPeriod(ReportPeriod reportPeriod) {
			ensureDataMutability();
			super.setReportPeriod(reportPeriod);
			return this;
		}

		private void ensureDataMutability() {
			if (data.locked) {
				data = new Data(data);
				data.locked = false;
			}
		}

		private void ensureImmutability() {
			if (!data.locked) {
				data.locked = true;
			}
		}

		private void validateData() {
			if (data.reportLabel == null) {
				throw new ContractException(ReportError.NULL_REPORT_LABEL);
			}
			if (data.reportPeriod == null) {
				throw new ContractException(ReportError.NULL_REPORT_PERIOD);
			}
		}
	}

	public static Builder builder() {
		return new Builder(new Data());
	}

	/**
	 * Returns the current version of this Simulation Plugin, which is equal to the
	 * version of the GCM Simulation
	 */
	public String getVersion() {
		return StandardVersioning.VERSION;
	}

	/**
	 * Given a version string, returns whether the version is a supported version or
	 * not.
	 */
	public static boolean checkVersionSupported(String version) {
		return StandardVersioning.checkVersionSupported(version);
	}

	@Override
	public Builder toBuilder() {
		return new Builder(data);
	}

	public Set<PersonPropertyId> getPersonPropertyIds() {
		return new LinkedHashSet<>(data.personPropertyIds);
	}

	/**
     * Standard implementation consistent with the {@link #equals(Object)} method
     */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(data);
		return result;
	}

	/**
     * Two {@link PersonPropertyInteractionReportPluginData} instances are equal if and only if
     * their inputs are equal.
     */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		PersonPropertyInteractionReportPluginData other = (PersonPropertyInteractionReportPluginData) obj;
		return Objects.equals(data, other.data);
	}

	@Override
	public String toString() {
		StringBuilder builder2 = new StringBuilder();
		builder2.append("PersonPropertyInteractionReportPluginData [data=");
		builder2.append(data);
		builder2.append("]");
		return builder2.toString();
	}
}
