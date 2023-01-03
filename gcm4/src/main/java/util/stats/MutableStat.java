package util.stats;

import java.util.Collection;
import java.util.Optional;

import net.jcip.annotations.NotThreadSafe;

/**
 * Implements B.P. Welford's method for determining sample variance without
 * maintaining sample values. Corrects for a significant portion of the rounding
 * errors associated with a large number of sample values. Corrects for errors
 * that arise from high mean and low variance sample populations.
 * 
 * Adapted from https://www.johndcook.com/blog/standard_deviation/
 * 
 *
 */
@NotThreadSafe
public final class MutableStat implements Stat {

	public static Stat combineStatsCollection(Collection<? extends Stat> stats) {
		Stat[] result = new Stat[stats.size()];
		result = stats.toArray(result);
		return combineStats(result);
	}

	/**
	 * Combines several stats
	 * 
	 * @throws NullPointerException
	 *             <li>if the stats are null</li>
	 * 
	 */
	public static Stat combineStats(Stat... stats) {

		if (stats == null) {
			throw new NullPointerException();
		}
		for (Stat stat : stats) {
			if (stat == null) {
				throw new NullPointerException();
			}
		}

		if (stats.length == 0) {
			return new MutableStat();
		}

		double min = Double.MAX_VALUE;
		for (Stat stat : stats) {
			if (stat.getMin().isPresent()) {
				min = Math.min(min, stat.getMin().get());
			}
		}

		double max = Double.MIN_VALUE;
		for (Stat stat : stats) {
			if (stat.getMax().isPresent()) {
				max = Math.max(max, stat.getMax().get());
			}
		}

		int tn = 0;
		for (Stat stat : stats) {
			Math.addExact(tn, stat.size());
		}

		double t1 = 0;
		double t2 = 0;

		for (Stat stat : stats) {
			if (stat.size() > 0) {
				int sn = stat.size();
				double s1 = stat.getMean().get();
				s1 *= sn;
				double s2 = stat.getVariance().get();
				s2 *= sn;
				s2 *= sn;
				s2 += s1 * s1;
				s2 /= sn;
				t1 += s1;
				t2 += s2;
				tn += sn;
			}
		}

		double var = t2 * tn - t1 * t1;
		var /= tn;

		double mean = t1 / tn;

		MutableStat result = new MutableStat();
		result.min = min;
		result.max = max;
		result.count = tn;
		result.mean = new KahanSum();
		result.mean.add(mean);
		result.var = new KahanSum();
		result.var.add(var);

		return result;
	}

	public MutableStat() {

	}

	private double min;
	private double max;
	private KahanSum mean;
	private KahanSum var;

	private int count;

	public void clear() {
		count = 0;
	}

	/**
	 * Adds a value to this MutableStat
	 */
	public void add(double value) {
		// increment the count
		count++;
		/*
		 * if there is only one data point, set the five core value accordinly
		 */
		if (count == 1) {
			mean = new KahanSum();
			mean.add(value);
			var = new KahanSum();
			min = value;
			max = value;
		} else {
			/*
			 * update the min and max
			 */
			if (max < value) {
				max = value;
			}
			if (min > value) {
				min = value;
			}

			/*
			 * Using Welford's method we update the mean and variance. Both are
			 * kept in Kahan Sums rather than doubles to help reduce the
			 * accumulation of error.
			 */
			double oldMean = mean.getSum();
			mean.add((value - oldMean) / count);
			var.add((value - oldMean) * (value - mean.getSum()));
		}
	}

	@Override
	public Optional<Double> getMean() {
		if (count == 0) {
			return Optional.empty();
		}
		return Optional.of(mean.getSum());
	}

	@Override
	public Optional<Double> getVariance() {
		if (count == 0) {
			return Optional.empty();
		}
		if (count == 1) {
			return Optional.of(0d);
		}
		double result = (var.getSum() / count);
		return Optional.of(result);
	}

	@Override
	public Optional<Double> getStandardDeviation() {
		Optional<Double> optional = getVariance();
		if (optional.isPresent()) {
			return Optional.of(Math.sqrt(optional.get()));
		}
		return Optional.empty();
	}

	@Override
	public Optional<Double> getMax() {
		if (count == 0) {
			return Optional.empty();
		}
		return Optional.of(max);
	}

	@Override
	public Optional<Double> getMin() {
		if (count == 0) {
			return Optional.empty();
		}
		return Optional.of(min);
	}

	@Override
	public int size() {
		return count;
	}

	/**
	 * Boilerplate implementation
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MutableStat [getMean()=");
		builder.append(getMean());
		builder.append(", getVariance()=");
		builder.append(getVariance());
		builder.append(", getStandardDeviation()=");
		builder.append(getStandardDeviation());
		builder.append(", getMax()=");
		builder.append(getMax());
		builder.append(", getMin()=");
		builder.append(getMin());
		builder.append(", size()=");
		builder.append(size());
		builder.append("]");
		return builder.toString();
	}
}
