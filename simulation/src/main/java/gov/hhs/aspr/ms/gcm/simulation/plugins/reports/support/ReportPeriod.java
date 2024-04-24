package gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support;

/**
 * An enumeration supporting {@link PeriodicReport} that represents the
 * periodicity of the report.
 */
public enum ReportPeriod {
	HOURLY, DAILY, END_OF_SIMULATION;
	
	public ReportPeriod next() {
		int index = (ordinal()+1) % ReportPeriod.values().length;
		return ReportPeriod.values()[index];		
	}
}
