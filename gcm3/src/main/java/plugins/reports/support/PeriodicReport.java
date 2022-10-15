package plugins.reports.support;

import java.util.function.BiConsumer;

import nucleus.ActorContext;
import nucleus.Event;
import nucleus.EventFilter;
import nucleus.NucleusError;
import util.errors.ContractException;

/**
 * The abstract base class for reports that aggregate reporting aligned to a
 * {@link ReportPeriod}. The periodic report continually schedules reporting on
 * a regular cycle, invoking the flush method and allowing descendant
 * implementors to release collected data. This report registers via the context
 * to be alerted when the simulation terminates and will perform a final flush
 * invocation. This can result in the duplication of time values for last data
 * released.
 *
 * @author Shawn Hatch
 *
 */
public abstract class PeriodicReport {
	
	private ActorContext actorContext;

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
	 * DAILY : Day
	 * 
	 * HOURLY : Day, Hour
	 * 
	 * END_OF_SIMULATION has no header additions
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
	 * simulation start. Descendant implementors of PeriodicReport must invoke
	 * super.init().
	 * 
	 * @throws ContractException
	 *             <li>if the report context is null</li>
	 * 
	 */
	public void init(ActorContext actorContext) {

		if (actorContext == null) {
			throw new ContractException(ReportError.NULL_CONTEXT);
		}
		this.actorContext = actorContext;

		actorContext.subscribeToSimulationClose(this::close);

		if (reportPeriod != ReportPeriod.END_OF_SIMULATION) {
			setNextPlanTime();
			actorContext.addPassivePlan(this::executePlan, nextPlanTime);
		}
	}

	private void close(final ActorContext actorContext) {
		if (lastFlushTime == null || actorContext.getTime() > lastFlushTime) {
			lastFlushTime = actorContext.getTime();
			flush(actorContext);
		}
	}

	/**
	 * Provides descendant implementors the opportunity to releases report items
	 * from the data stored during the time since the last invocation of
	 * flush().
	 */
	protected abstract void flush(final ActorContext actorContext);

	private double nextPlanTime;
	private Double lastFlushTime;

	private void setNextPlanTime() {
		switch (reportPeriod) {
		case DAILY:
			nextPlanTime = (reportingDay + 1);
			break;
		case HOURLY:
			nextPlanTime = reportingDay + (double) (reportingHour + 1) / 24;
			break;
		default:
			throw new RuntimeException("unhandled report period " + reportPeriod);
		}
	}

	/**
	 * Returns a wrapped version of the given consumer that will ensure proper
	 * flushing when events are received at the same time as a flushing plan,
	 * but happen to execute before the plan. Descendant implementors of
	 * PeriodicReport should use this wrapper when subscribing to events.
	 */
	private <T extends Event> BiConsumer<ActorContext, T> getFlushingConsumer(BiConsumer<ActorContext, T> eventConsumer) {
		return (c, t) -> {
			if (c.getTime() >= nextPlanTime) {
				if (lastFlushTime == null || c.getTime() > lastFlushTime) {
					lastFlushTime = c.getTime();
					flush(c);
				}
			}
			eventConsumer.accept(c, t);
		};
	}

	private void executePlan(final ActorContext actorContext) {
		if (lastFlushTime == null || actorContext.getTime() > lastFlushTime) {
			lastFlushTime = actorContext.getTime();
			flush(actorContext);
		}

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

		setNextPlanTime();
		actorContext.addPassivePlan(this::executePlan, nextPlanTime);

	}


	/**
	 * Subscribes the report to the given event filter via the actor context
	 * while enforcing the flushing of report items as needed. Events of the
	 * type T are processed by the event filter. If the event passes the filter
	 * the event will be consumed by the supplied event consumer.
	 * 
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_EVENT_FILTER} if the event
	 *             filter is null
	 *             <li>{@link NucleusError#NULL_EVENT_CONSUMER} if the event
	 *             consumer is null
	 */
	protected final <T extends Event> void subscribe(EventFilter<T> eventFilter, BiConsumer<ActorContext, T> eventConsumer) {
		actorContext.subscribe(eventFilter, getFlushingConsumer(eventConsumer));
	}

	/**
	 * Subscribes the report to the given event via the actor context while
	 * enforcing the flushing of report items as needed.
	 * 
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_EVENT_CLASS} if the event class
	 *             is null
	 *             <li>{@link NucleusError#NULL_EVENT_CONSUMER} if the event
	 *             consumer is null
	 * 
	 */
	protected final <T extends Event> void subscribe(Class<T> eventClass, BiConsumer<ActorContext, T> eventConsumer) {
		actorContext.subscribe(eventClass, getFlushingConsumer(eventConsumer));
	}

}
