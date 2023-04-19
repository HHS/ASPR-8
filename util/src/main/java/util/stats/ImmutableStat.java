package util.stats;

import java.util.Optional;

import net.jcip.annotations.NotThreadSafe;
import net.jcip.annotations.ThreadSafe;

/**
 * A {@link Stat} implementor that is immutable and is constructed via the
 * contained builder class.
 * 
 *
 */
@ThreadSafe
public final class ImmutableStat implements Stat {

	/**
	 * A container for collecting the five characteristics of a Stat
	 * 
	 *
	 */
	private static class Data {
		private double mean;
		private double variance;
		private double max;
		private double min;
		private int size;

		public Data() {
		}

		public Data(Data data) {
			mean = data.mean;
			variance = data.variance;
			max = data.max;
			min = data.min;
			size = data.size;
		}
	}

	/**
	 * Returns a new Builder for {@link ImmutableStat}
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * A builder class for {@link ImmutableStat}
	 * 
	 *
	 */
	@NotThreadSafe
	public static class Builder {
		private Builder() {

		}

		private Data data = new Data();

		/**
		 * Builds the ImmutableStat
		 * 
		 * @throws IllegalArgumentException
		 *             <li>if the size is negative
		 *             <li>if the size value is one and the min mean and max are
		 *             not equal
		 *             <li>if the size value is one and the variance is not zero
		 *             <li>if the size value is greater than one and the min
		 *             exceeds the max
		 *             <li>if the size value is greater than one and the min
		 *             exceeds the mean
		 *             <li>if the size value is greater than one and the mean
		 *             exceeds the max
		 *             <li>if the size value is greater than one and the
		 *             variance is negative
		 */
		public ImmutableStat build() {
			validate();
			return new ImmutableStat(new Data(data));
		}

		/**
		 * Sets the mean
		 */
		public Builder setMean(double mean) {
			data.mean = mean;
			return this;
		}

		/**
		 * Sets the variance
		 */
		public Builder setVariance(double variance) {
			data.variance = variance;
			return this;
		}

		/**
		 * Sets the max
		 */
		public Builder setMax(double max) {
			data.max = max;
			return this;
		}

		/**
		 * Sets the min
		 */
		public Builder setMin(double min) {
			data.min = min;
			return this;
		}

		/**
		 * Sets the size
		 */
		public Builder setSize(int size) {
			data.size = size;
			return this;
		}

		/*
		 * Validates the content of the Stat
		 * 
		 * @throws IllegalArgumentException
		 * 
		 * <li>if the size is negative
		 * 
		 * <li>if the size value is one and the min mean and max are not equal
		 * 
		 * <li>if the size value is one and the variance is not zero
		 * 
		 * <li>if the size value is greater than one and the min exceeds the max
		 * 
		 * <li>if the size value is greater than one and the min exceeds the
		 * mean
		 * 
		 * <li>if the size value is greater than one and the mean exceeds the
		 * max
		 * 
		 * <li>if the size value is greater than one and the variance is
		 * negative
		 */
		private void validate() {
			if (data.size < 0) {
				throw new IllegalArgumentException("negative size");
			}
			if (data.size == 1) {
				// min, max and mean must be equal and variance must be zero
				if (data.min != data.max) {
					throw new IllegalArgumentException("size = 1 implies min=max");
				}
				if (data.min != data.mean) {
					throw new IllegalArgumentException("size = 1 implies min=mean=max");
				}
				if (data.variance != 0) {
					throw new IllegalArgumentException("size = 1 implies variance = 0");
				}
			} else if (data.size > 1) {
				if (data.min > data.max) {
					throw new IllegalArgumentException("min exceeds max");
				}
				if (data.min > data.mean) {
					throw new RuntimeException("min exceeds mean");
				}

				if (data.mean > data.max) {
					throw new IllegalArgumentException("mean exceeds max");
				}

				if (data.variance < 0) {
					throw new IllegalArgumentException("variance cannot be negative");
				}
			}
		}
	}

	private ImmutableStat(Data data) {
		this.mean = data.mean;
		this.min = data.min;
		this.max = data.max;
		this.variance = data.variance;
		this.standardDeviation = Math.sqrt(data.variance);
		this.size = data.size;
	}

	private final double mean;
	private final double variance;
	private final double standardDeviation;
	private final double max;
	private final double min;
	private final int size;

	@Override
	public Optional<Double> getMean() {
		if (size > 0) {
			return Optional.of(mean);
		}
		return Optional.empty();
	}

	@Override
	public Optional<Double> getVariance() {
		if (size > 0) {
			return Optional.of(variance);
		}
		return Optional.empty();
	}

	@Override
	public Optional<Double> getStandardDeviation() {
		if (size > 0) {
			return Optional.of(standardDeviation);
		}
		return Optional.empty();
	}

	@Override
	public Optional<Double> getMax() {
		if (size > 0) {
			return Optional.of(max);
		}
		return Optional.empty();
	}

	@Override
	public Optional<Double> getMin() {
		if (size > 0) {
			return Optional.of(min);
		}
		return Optional.empty();
	}

	@Override
	public int size() {
		return size;
	}

	/**
	 * Boilerplate implementation
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ImmutableStat [mean=");
		builder.append(mean);
		builder.append(", variance=");
		builder.append(variance);
		builder.append(", standardDeviation=");
		builder.append(standardDeviation);
		builder.append(", max=");
		builder.append(max);
		builder.append(", min=");
		builder.append(min);
		builder.append(", size=");
		builder.append(size);
		builder.append("]");
		return builder.toString();
	}

}
