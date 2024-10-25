package gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.ReportContext;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * The abstract base class for reports that aggregate reporting aligned to a
 * {@link ReportPeriod}. The periodic report continually schedules reporting on
 * a regular cycle, invoking the flush method and allowing descendant
 * implementors to release collected data. This report registers via the context
 * to be alerted when the simulation terminates and will perform a final flush
 * invocation if the report was not already flushed at that time.
 */
public abstract class PeriodicReport {

	/**
	 * Creates the periodic report from the given report period
	 * 
	 * @throws ContractException
	 *                           <ul>
	 *                           <li>{@linkplain ReportError#NULL_REPORT_PERIOD} if
	 *                           the report period is null</li>
	 *                           <li>{@linkplain ReportError#NULL_REPORT_LABEL} if
	 *                           the report period is null</li>
	 *                           </ul>
	 */
	public PeriodicReport(ReportLabel reportLabel, ReportPeriod reportPeriod) {
		if (reportPeriod == null) {
			throw new ContractException(ReportError.NULL_REPORT_PERIOD);
		}
		this.reportPeriod = reportPeriod;

		if (reportLabel == null) {
			throw new ContractException(ReportError.NULL_REPORT_LABEL);
		}
		this.reportLabel = reportLabel;
	}

	private ReportPeriod reportPeriod;

	private ReportLabel reportLabel;

	/*
	 * The day value to be used in report lines
	 */
	private Integer reportingDay = 0;

	/*
	 * The hour value to be used in report lines
	 */
	private Integer reportingHour = 0;

	/**
	 * Adds the time field column(s) to the given {@link ReportHeader.Builder} as
	 * appropriate to the {@link ReportPeriod} specified during construction. DAILY
	 * : Day HOURLY : Day, Hour END_OF_SIMULATION has no header additions
	 */
	protected final ReportHeader.Builder addTimeFieldHeaders(ReportHeader.Builder reportHeaderBuilder) {
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

	protected final ReportLabel getReportLabel() {
		return reportLabel;
	}

	protected final ReportPeriod getReportPeriod() {
		return reportPeriod;
	}

	/**
	 * Places the current reporting day and report hour on the report as appropriate
	 * to the {@link ReportPeriod} specified during construction.
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
			reportItemBuilder.addValue(reportingHour);
			break;
		default:
			throw new RuntimeException("unknown report period " + reportPeriod);
		}
	}

	/**
	 * Subscribes to simulation close. Initializes periodic flushing of report
	 * contents with the first flush scheduled for one time period from simulation
	 * start. Descendant implementors of PeriodicReport must implement prepare()
	 * which is invoked by init().
	 * 
	 * @throws ContractException if the report context is null
	 */
	public final void init(ReportContext reportContext) {

		if (reportContext == null) {
			throw new ContractException(ReportError.NULL_CONTEXT);
		}

		reportContext.subscribeToSimulationClose(this::close);

		reportingDay = (int) reportContext.getTime();
		reportingHour = (int) (24 * (reportContext.getTime() - reportingDay));
		if (reportingHour > 23) {
			reportingHour = 23;
		}

		prepare(reportContext);

			if (reportPeriod != ReportPeriod.END_OF_SIMULATION) {
				incrementReportingTimeFields();
				reportContext.addPlan(this::executePlan, getNextPlanTime());
			}
	}

	/**
	 * Called by the init() to allow descendant report classes to initialize. The
	 * init() will invoke a flush() command after the prepare()
	 */
	protected abstract void prepare(ReportContext reportContext);

	private void close(final ReportContext reportContext) {
		if (lastFlushTime == null || reportContext.getTime() > lastFlushTime) {
			flush(reportContext);
		}
	}

	/**
	 * Provides descendant implementors the opportunity to releases report items
	 * from the data stored during the time since the last invocation of flush().
	 */
	protected abstract void flush(final ReportContext reportContext);

	private Double lastFlushTime;

	private double getNextPlanTime() {
		switch (reportPeriod) {
		case DAILY:
			return reportingDay;

		case HOURLY:
			return reportingDay + (double) (reportingHour) / 24;

		default:
			throw new RuntimeException("unhandled report period " + reportPeriod);
		}
	}

	private void incrementReportingTimeFields() {
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
		case END_OF_SIMULATION:
			// do nothing
			break;
		default:
			throw new RuntimeException("unhandled report period " + reportPeriod);
		}
	}

	private void executePlan(final ReportContext reportContext) {
		lastFlushTime = reportContext.getTime();
		flush(reportContext);
		incrementReportingTimeFields();
		reportContext.addPlan(this::executePlan, getNextPlanTime());
	}
}
