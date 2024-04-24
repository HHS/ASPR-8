package gov.hhs.aspr.ms.gcm.lessons;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import net.jcip.annotations.Immutable;

@Immutable
/* start code_ref=events_alpha_change_event|code_cap=An event to notify that the alpha property has been updated. */
public final class AlphaChangeEvent implements Event {

	private final int previousAlpha;

	private final int currentAlpha;

	public AlphaChangeEvent(int previousAlpha, int currentAlpha) {
		super();
		this.previousAlpha = previousAlpha;
		this.currentAlpha = currentAlpha;
	}

	public int getPreviousAlpha() {
		return previousAlpha;
	}

	public int getCurrentAlpha() {
		return currentAlpha;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AlphaChangeEvent [previousAlpha=");
		builder.append(previousAlpha);
		builder.append(", currentAlpha=");
		builder.append(currentAlpha);
		builder.append("]");
		return builder.toString();
	}

}
/* end */
