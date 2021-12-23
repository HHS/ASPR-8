package plugins.gcm.experiment.output;

import net.jcip.annotations.Immutable;

/**
 * Status type for {@link LogItem}
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public enum LogStatus {
	INFO, ERROR, DEBUG, TRACE
}
