package plugins.stochastics.testsupport;

import plugins.stochastics.support.RandomNumberGeneratorId;
/**
 * Enumeration that provides a variety of RandomNumberGeneratorId values for testing purposes
 * 
 * @author Shawn Hatch
 */
public enum TestRandomGeneratorId implements RandomNumberGeneratorId {
	DASHER, DANCER, PRANCER, VIXEN, COMET, CUPID, DONNER, BLITZEN;

	public static RandomNumberGeneratorId getUnknownRandomNumberGeneratorId() {
		return new RandomNumberGeneratorId() {
		};
	}
}
