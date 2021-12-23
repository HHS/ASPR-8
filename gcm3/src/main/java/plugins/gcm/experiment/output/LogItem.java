package plugins.gcm.experiment.output;

import net.jcip.annotations.Immutable;
import plugins.gcm.experiment.ReplicationId;
import plugins.gcm.experiment.ScenarioId;

/**
 * An output implementor used to pseudo-logging of GCM items.
 * 
 * @author Shawn Hatch
 *
 */

@Immutable
public class LogItem  {

	private final LogStatus logStatus;
	private final String message;

	public LogStatus getLogStatus() {
		return logStatus;
	}

	public String getMessage() {
		return message;
	}

	public LogItem(ScenarioId scenarioId, ReplicationId replicationId, LogStatus logStatus, String message) {
		super();
		this.logStatus = logStatus;
		this.message = message;
	}

	@Override
	public String toString() {
		return "LogItem [logStatus=" + logStatus + ", message=" + message + "]";
	}

}
