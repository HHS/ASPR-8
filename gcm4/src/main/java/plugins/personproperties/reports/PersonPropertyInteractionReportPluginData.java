package plugins.personproperties.reports;

import java.util.LinkedHashSet;
import java.util.Set;

import net.jcip.annotations.ThreadSafe;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import plugins.personproperties.support.PersonPropertyId;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;

@ThreadSafe
public class PersonPropertyInteractionReportPluginData implements PluginData {

	private static class Data {
		private ReportLabel reportLabel;
		private ReportPeriod reportPeriod;
		private final Set<PersonPropertyId> personPropertyIds = new LinkedHashSet<>();
		public Data() {}
		public Data(Data data) {
			reportLabel = data.reportLabel;
			reportPeriod = data.reportPeriod;
			personPropertyIds.addAll(data.personPropertyIds);
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((personPropertyIds == null) ? 0 : personPropertyIds.hashCode());
			result = prime * result + ((reportLabel == null) ? 0 : reportLabel.hashCode());
			result = prime * result + ((reportPeriod == null) ? 0 : reportPeriod.hashCode());
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
			if (reportLabel == null) {
				if (other.reportLabel != null) {
					return false;
				}
			} else if (!reportLabel.equals(other.reportLabel)) {
				return false;
			}
			if (reportPeriod != other.reportPeriod) {
				return false;
			}
			return true;
		}
		
		
	}

	public static Builder builder() {
		return new Builder(new Data());
	}

	public static class Builder implements PluginDataBuilder {
		private Data data;

		private Builder(Data data) {
			this.data = data;
		}

		@Override
		public PersonPropertyInteractionReportPluginData build() {
			return new  PersonPropertyInteractionReportPluginData(new Data(data));
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
			data.reportLabel = reportLabel;
			return this;
		}

		public Builder setReportPeriod(ReportPeriod reportPeriod) {
			data.reportPeriod = reportPeriod;
			return this;
		}

	}

	private final Data data;

	private PersonPropertyInteractionReportPluginData(Data data) {
		this.data = data;
	}

	public ReportLabel getReportLabel() {
		return data.reportLabel;
	}

	public ReportPeriod getReportPeriod() {
		return data.reportPeriod;
	}

	public Set<PersonPropertyId> getPersonPropertyIds() {
		return new LinkedHashSet<>( data.personPropertyIds);
	}

	@Override
	public PluginDataBuilder getCloneBuilder() {
		return new Builder(new Data(data));
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

	
	
	
}
