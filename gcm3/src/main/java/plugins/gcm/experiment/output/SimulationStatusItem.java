package plugins.gcm.experiment.output;

import net.jcip.annotations.Immutable;

/**
 * An {@link OutputItem} that records the execution time and success of a
 * simulation execution.
 * 
 * Instances are constructed through the included builder class.
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public final class SimulationStatusItem {

	private final double duration;

	private final boolean successful;

	/*
	 * private constructor used only by the builder
	 */
	public SimulationStatusItem(double duration, boolean successful) {
		if (duration < 0) {
			throw new RuntimeException("negative duration");
		}
		this.duration = duration;
		this.successful = successful;
	}

	/**
	 * Returns the duration of the simulation's execution
	 */
	public double getDuration() {
		return duration;
	}

	/**
	 * Returns true if and only if the simulation completed successfully
	 */
	public boolean successful() {
		return successful;
	}

	@Override
	public String toString() {
		return "SimulationStatusItem [duration=" + duration + ", successful=" + successful + "]";
	}

}
