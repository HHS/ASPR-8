package gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.reports;

import java.util.LinkedHashSet;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.StandardVersioning;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.support.PersonPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.PeriodicReportPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportPeriod;
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

		public Data() {
			super();
		}

		public Data(Data data) {
			super(data);
			personPropertyIds.addAll(data.personPropertyIds);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((personPropertyIds == null) ? 0 : personPropertyIds.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Data)) {
				return false;
			}
			Data other = (Data) obj;
			if (personPropertyIds == null) {
				if (other.personPropertyIds != null) {
					return false;
				}
			} else if (!personPropertyIds.equals(other.personPropertyIds)) {
				return false;
			}
			return super.equals(other);
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
			return new PersonPropertyInteractionReportPluginData(data);
		}

		public Builder addPersonPropertyId(PersonPropertyId personPropertyId) {
			if (personPropertyId != null) {
				data.personPropertyIds.add(personPropertyId);
			}
			return this;
		}

		public Builder removePersonPropertyId(PersonPropertyId personPropertyId) {
			data.personPropertyIds.remove(personPropertyId);
			return this;
		}

		public Builder setReportLabel(ReportLabel reportLabel) {
			super.setReportLabel(reportLabel);
			return this;
		}

		public Builder setReportPeriod(ReportPeriod reportPeriod) {
			super.setReportPeriod(reportPeriod);
			return this;
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
	public Builder getCloneBuilder() {
		return new Builder(new Data(data));
	}

	public Set<PersonPropertyId> getPersonPropertyIds() {
		return new LinkedHashSet<>(data.personPropertyIds);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof PersonPropertyInteractionReportPluginData)) {
			return false;
		}
		PersonPropertyInteractionReportPluginData other = (PersonPropertyInteractionReportPluginData) obj;
		if (data == null) {
			if (other.data != null) {
				return false;
			}
		} else if (!data.equals(other.data)) {
			return false;
		}
		return true;
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
