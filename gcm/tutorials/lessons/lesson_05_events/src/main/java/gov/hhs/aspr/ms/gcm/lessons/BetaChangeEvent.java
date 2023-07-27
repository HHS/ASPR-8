package gov.hhs.aspr.ms.gcm.lessons;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import net.jcip.annotations.Immutable;

@Immutable
/* start code_ref=events_beta_change_event */
public final class BetaChangeEvent implements Event {

	private final double previousBeta;

	private final double currentBeta;

	public BetaChangeEvent(double previousBeta, double currentBeta) {
		super();
		this.previousBeta = previousBeta;
		this.currentBeta = currentBeta;
	}

	public double getPreviousBeta() {
		return previousBeta;
	}

	public double getCurrentBeta() {
		return currentBeta;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BetaChangeEvent [previousBeta=");
		builder.append(previousBeta);
		builder.append(", currentBeta=");
		builder.append(currentBeta);
		builder.append("]");
		return builder.toString();
	}

}
/* end */
