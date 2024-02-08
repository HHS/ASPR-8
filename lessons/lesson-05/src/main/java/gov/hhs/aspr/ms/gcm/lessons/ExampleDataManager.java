package gov.hhs.aspr.ms.gcm.lessons;

import gov.hhs.aspr.ms.gcm.nucleus.DataManager;
import gov.hhs.aspr.ms.gcm.nucleus.DataManagerContext;
import gov.hhs.aspr.ms.gcm.nucleus.Event;

public final class ExampleDataManager extends DataManager {

	private int alpha = 7;
	private double beta = 1.2345;
	private DataManagerContext dataManagerContext;

	@Override
	public void init(DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);
		this.dataManagerContext = dataManagerContext;
		dataManagerContext.subscribe(AlphaChangeMutationEvent.class, this::handleAlphaChangeMutationEvent);
		dataManagerContext.subscribe(BetaChangeMutationEvent.class, this::handleBetaChangeMutationEvent);
	}

	public int getAlpha() {
		return alpha;
	}

	public double getBeta() {
		return beta;
	}

	/* start code_ref=events_intro_to_event_generation|code_cap=The alpha and beta updates are managed via private mutation events.*/
	private static record AlphaChangeMutationEvent(int alpha) implements Event {
	}

	public void setAlpha(int alpha) {
		dataManagerContext.releaseMutationEvent(new AlphaChangeMutationEvent(alpha));
	}

	private void handleAlphaChangeMutationEvent(DataManagerContext dataManagerContext,
			AlphaChangeMutationEvent alphaChangeMutationEvent) {
		int alpha = alphaChangeMutationEvent.alpha();
		int previousValue = this.alpha;
		this.alpha = alpha;
		dataManagerContext.releaseObservationEvent(new AlphaChangeEvent(previousValue, this.alpha));
	}

	private static record BetaChangeMutationEvent(double beta) implements Event {
	}

	public void setBeta(double beta) {
		dataManagerContext.releaseMutationEvent(new BetaChangeMutationEvent(beta));
	}

	private void handleBetaChangeMutationEvent(DataManagerContext dataManagerContext,
			BetaChangeMutationEvent betaChangeMutationEvent) {
		double beta = betaChangeMutationEvent.beta();
		double previousValue = this.beta;
		this.beta = beta;
		dataManagerContext.releaseObservationEvent(new BetaChangeEvent(previousValue, this.beta));
	}
	/* end */
}
