package plugins.stochastics.testsupport;

import plugins.stochastics.support.RandomNumberGeneratorId;

public enum TestRandomGeneratorId implements RandomNumberGeneratorId {
	DASHER, DANCER, PRANCER, VIXEN, COMET, CUPID, DONNER, BLITZEN;

	public static RandomNumberGeneratorId getUnknownRandomNumberGeneratorId() {
		return new RandomNumberGeneratorId() {
		};
	}
}
