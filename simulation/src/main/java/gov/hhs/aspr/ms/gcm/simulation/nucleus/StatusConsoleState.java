package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import net.jcip.annotations.ThreadSafe;

/**
 * Thread safe state container for the Experiment Status Console
 */
@ThreadSafe
public final class StatusConsoleState {
	private boolean immediateErrorReporting;
	private boolean reportScenarioProgress;
	private int stackTraceReportLimit;
	private int lastReportedCompletionPercentage;
	private int immediateStackTraceCount;

	public synchronized boolean immediateErrorReporting() {
		return immediateErrorReporting;
	}

	public synchronized void setImmediateErrorReporting(boolean immediateErrorReportingx) {
		this.immediateErrorReporting = immediateErrorReportingx;
	}

	public synchronized boolean reportScenarioProgress() {
		return reportScenarioProgress;
	}

	public synchronized void setReportScenarioProgress(boolean reportScenarioProgressx) {
		this.reportScenarioProgress = reportScenarioProgressx;
	}

	public synchronized int getStackTraceReportLimit() {
		return stackTraceReportLimit;
	}

	public synchronized void setStackTraceReportLimit(int stackTraceReportLimitx) {
		this.stackTraceReportLimit = stackTraceReportLimitx;
	}

	public synchronized int getLastReportedCompletionPercentage() {
		return lastReportedCompletionPercentage;
	}

	public synchronized void setLastReportedCompletionPercentage(int lastReportPercentage) {
		this.lastReportedCompletionPercentage = lastReportPercentage;
	}

	public synchronized int getImmediateStackTraceCount() {
		return immediateStackTraceCount;
	}

	public synchronized void incrementImmediateStackTraceCount() {
		this.immediateStackTraceCount++;
	}

}