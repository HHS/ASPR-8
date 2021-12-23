package util.stats;

import java.util.Optional;

/**
 * Stat contains the values of several useful statistical values for a sequence,
 * but not the values in the sequence.
 * 
 * @author Shawn Hatch
 *
 */
public interface Stat {

	/**
	 * Returns the mean of the sequence. Empty sequences do not have a defined
	 * mean and the Optional will reflect that no mean value is present.
	 */
	public Optional<Double> getMean();

	/**
	 * Returns the variance of the sequence. Empty sequences do not have a
	 * defined variance and the Optional will reflect that no variance value is
	 * present.
	 */
	public Optional<Double> getVariance();

	/**
	 * Returns the standard deviation of the sequence. Empty sequences do not
	 * have a defined standard deviation and the Optional will reflect that no
	 * standard deviation value is present.
	 */
	public Optional<Double> getStandardDeviation();

	/**
	 * Returns the max value of the sequence. Empty sequences do not have a
	 * defined max value and the Optional will reflect that no max value value
	 * is present.
	 */
	public Optional<Double> getMax();

	/**
	 * Returns the min value of the sequence. Empty sequences do not have a
	 * defined min value and the Optional will reflect that no min value value
	 * is present.
	 */
	public Optional<Double> getMin();

	/**
	 * Returns the number of values in the sequence.
	 */
	public int size();

}
