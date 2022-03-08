package plugins.reports.support;


import nucleus.ActorContext;
import nucleus.util.ContractException;

/**
 * The abstract base class for reports that aggregate reporting aligned to a
 * {@link ReportPeriod}. At the end of each reporting period this report invokes
 * the flush method, allowing descendant implementors to release collected data.
 * This report registers via the context to be alerted when the simulation
 * terminates and will perform a final flush invocation. This can result in the
 * duplication of time values for last data released.
 *
 * @author Shawn Hatch
 *
 */
public abstract class PeriodicReport {

	/**
	 * Creates the periodic report from the given report period
	 * 
	 * @throws ContractException
	 *             <li>if the report period is null</li>
	 */
	public PeriodicReport(ReportId reportId, ReportPeriod reportPeriod) {
		if (reportPeriod == null) {
			throw new ContractException(ReportError.NULL_REPORT_PERIOD);
		}
		this.reportPeriod = reportPeriod;
		
		if (reportId == null) {
			throw new ContractException(ReportError.NULL_REPORT_ID);
		}
		this.reportId = reportId;
	}

	/*
	 * Assume a daily report period and let it be overridden
	 */
	private ReportPeriod reportPeriod = ReportPeriod.DAILY;
	
	private ReportId reportId;

	/*
	 * The day value to be used in report lines
	 */
	private Integer reportingDay = 0;

	/*
	 * The hour value to be used in report lines
	 */
	private Integer reportingHour = 0;

	/**
	 * Adds the time field column(s) to the given {@link ReportHeaderBuilder} as
	 * appropriate to the {@link ReportPeriod} specified during construction.
	 * 
	 * DAILY :  Day
	 * 
	 * HOURLY : Day, Hour
	 * 
	 * END_OF_SIMULATION has no header additions
	 */
	protected ReportHeader.Builder addTimeFieldHeaders(ReportHeader.Builder reportHeaderBuilder) {
		switch (reportPeriod) {
		case DAILY:
			reportHeaderBuilder.add("day");
			break;
		case END_OF_SIMULATION:
			// do nothing
			break;
		case HOURLY:
			reportHeaderBuilder.add("day");
			reportHeaderBuilder.add("hour");
			break;
		default:
			throw new RuntimeException("unknown report period " + reportPeriod);
		}
		return reportHeaderBuilder;
	}
	
	protected final ReportId getReportId() {
		return reportId;
	}

	/**
	 * Places the current reporting day and report hour on the report as
	 * appropriate to the {@link ReportPeriod} specified during construction.
	 *
	 */
	protected final void fillTimeFields(final ReportItem.Builder reportItemBuilder) {

		switch (reportPeriod) {
		case DAILY:
			reportItemBuilder.addValue(reportingDay);
			break;
		case END_OF_SIMULATION:
			// do nothing
			break;
		case HOURLY:
			reportItemBuilder.addValue(reportingDay);
			reportItemBuilder.addValue(reportingHour % 24);
			break;
		default:
			throw new RuntimeException("unknown report period " + reportPeriod);
		}
	}

	/**
	 * Subscribes to simulation close. Initializes periodic flushing of report
	 * contents with the first flush scheduled for one time period from
	 * simulation start.
	 * 
	 * @throws ContractException
	 * <li>if the report context is null</li>
	 * 
	 */
	public void init(ActorContext actorContext) {
		
		if(actorContext == null) {
			throw new ContractException(ReportError.NULL_CONTEXT);
		}
		
		actorContext.subscribeToSimulationClose(this::close);
		
		if (reportPeriod != ReportPeriod.END_OF_SIMULATION) {
			actorContext.addPassivePlan(this::executePlan, getNextPlanTime());
		}
	}

	private void close(final ActorContext actorContext) {
		flush(actorContext);
	}

	/**
	 * Provides descendant implementors the opportunity to releases report items
	 * from the data stored during the time since the last invocation of
	 * flush(); Flush is invoked
	 */
	protected abstract void flush(final ActorContext actorContext);

	private double getNextPlanTime() {
		switch (reportPeriod) {
		case DAILY:
			return (reportingDay + 1);
		case HOURLY:
			return reportingDay + (double)(reportingHour + 1) / 24;
		default:
			throw new RuntimeException("unhandled report period " + reportPeriod);
		}
	}

	private void executePlan(final ActorContext actorContext) {

		flush(actorContext);

		switch (reportPeriod) {
		case DAILY:
			reportingDay++;
			break;
		case HOURLY:
			reportingHour++;
			if (reportingHour == 24) {
				reportingHour = 0;
				reportingDay++;
			}
			break;
		default:
			throw new RuntimeException("unhandled report period " + reportPeriod);
		}

		actorContext.addPassivePlan(this::executePlan, getNextPlanTime());

	}
}
