package util.stats;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math3.util.FastMath;

public final class BinContainer {

	/**
	 * Constructs the {@link BinContainer} from the given binSize.
	 * 
	 */
	private BinContainer(MutableBinContainer mutableBinContainer) {
		this.lowIndex = mutableBinContainer.lowIndex;
		this.highIndex = mutableBinContainer.highIndex;
		this.binSize = mutableBinContainer.binSize;

		for (Integer key : mutableBinContainer.map.keySet()) {
			MutableBin mutableBin = mutableBinContainer.map.get(key);
			map.put(key, new Bin(mutableBin));
		}

		for (int i = lowIndex; i <= highIndex; i++) {
			Bin bin = map.get(i);
			if (bin == null) {
				map.put(i, new Bin(i * binSize, (i + 1) * binSize, 0));
			}
		}

	}

	/**
	 * Creates a builder for a bin container
	 * 
	 * @throws IllegalArgumentException
	 * <li>if the bin size was non-positive</li>
	 */
	public static Builder builder(double binSize) {
		return new Builder(binSize);
	}

	private final double binSize;

	private final Map<Integer, Bin> map = new TreeMap<>();

	private final int lowIndex;

	private final int highIndex;

	public static class Builder {
		private final double binSize;
		private MutableBinContainer mutableBinContainer;

		private Builder(double binSize) {
			this.binSize = binSize;
			mutableBinContainer = new MutableBinContainer(binSize);
		}

		/**
		 * Builds the {@link BinContainer} from the contributed values.
		 * 
		 */
		public BinContainer build() {
			try {
				return new BinContainer(mutableBinContainer);
			} finally {
				mutableBinContainer = new MutableBinContainer(binSize);
			}
		}

		/**
		 * Adds the given value by the given number of times.
		 * 
		 * @throws IllegalArgumentException
		 *             <li>if the count is negative
		 */
		public Builder addValue(double value, int count) {
			mutableBinContainer.addValue(value, count);
			return this;
		}

	}

	/**
	 * Represents a half open interval [lowerBound,upperBound) that contains a
	 * count of values.
	 * 
	 * @author Shawn Hatch
	 *
	 */

	public final static class Bin {
		private final double lowerBound;
		private final double upperBound;
		private final int count;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + count;
			long temp;
			temp = Double.doubleToLongBits(lowerBound);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(upperBound);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof Bin)) {
				return false;
			}
			Bin other = (Bin) obj;
			if (count != other.count) {
				return false;
			}
			if (Double.doubleToLongBits(lowerBound) != Double.doubleToLongBits(other.lowerBound)) {
				return false;
			}
			if (Double.doubleToLongBits(upperBound) != Double.doubleToLongBits(other.upperBound)) {
				return false;
			}
			return true;
		}

		/**
		 * Returns the number of data associated with this bin
		 */
		public int getCount() {
			return count;
		}

		/**
		 * Returns the lower bound of this bin
		 */
		public double getLowerBound() {
			return lowerBound;
		}

		/**
		 * Returns the upper bound of this bin
		 */
		public double getUpperBound() {
			return upperBound;
		}

		/**
		 * Constructs the Bin
		 * 
		 * @throws IllegalArgumentException
		 *             <li>if the lower bound exceeds the upper bound
		 *             <li>if the count is negative
		 */
		public Bin(double lowerBound, double upperBound, int count) {
			if (lowerBound > upperBound) {
				throw new IllegalArgumentException("lower bound exceeds upper bound");
			}

			if (count < 0) {
				throw new IllegalArgumentException("negative count");
			}

			this.lowerBound = lowerBound;
			this.upperBound = upperBound;
			this.count = count;
		}

		private Bin(MutableBin mutableBin) {
			this.lowerBound = mutableBin.lowerBound;
			this.upperBound = mutableBin.upperBound;
			this.count = mutableBin.count;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Bin [lowerBound=");
			builder.append(lowerBound);
			builder.append(", upperBound=");
			builder.append(upperBound);
			builder.append(", count=");
			builder.append(count);
			builder.append("]");
			return builder.toString();
		}
	}

	private final static class MutableBin {
		private final double lowerBound;
		private final double upperBound;
		private int count;

		private MutableBin(double lowerBound, double upperBound) {
			this.lowerBound = lowerBound;
			this.upperBound = upperBound;
		}

	}

	private static class MutableBinContainer {
		private MutableBinContainer(double binSize) {
			if (binSize <= 0) {
				throw new IllegalArgumentException("bin size must be positive");
			}
			this.binSize = binSize;
		}

		private final double binSize;

		private Map<Integer, MutableBin> map = new LinkedHashMap<>();

		private int lowIndex = Integer.MAX_VALUE;

		private int highIndex = Integer.MIN_VALUE;

		/**
		 * Adds a value to the container
		 * 
		 * @throws IllegalArgumentException
		 *             <li>if the count is negative</li>
		 */
		public void addValue(double value, int count) {
			if (count < 0) {
				throw new IllegalArgumentException("count cannot be negative");
			}
			int index = (int) FastMath.floor(value / binSize);
			MutableBin mutableBin = map.get(index);
			if (mutableBin == null) {
				lowIndex = FastMath.min(lowIndex, index);
				highIndex = FastMath.max(highIndex, index);
				mutableBin = new MutableBin(index * binSize, (index + 1) * binSize);
				map.put(index, mutableBin);
			}
			mutableBin.count += count;
		}
	}

	/**
	 * Returns the number of bins contained in this {@link BinContainer}
	 */
	public int binCount() {
		return highIndex - lowIndex + 1;
	}

	/**
	 * Returns the bin associated with the given index. Bins are indexed from 0
	 * to binCount-1.
	 */
	public Bin getBin(int index) {
		if (index < 0) {
			throw new RuntimeException("bin index out of bounds: " + index);
		}
		if (index > highIndex - lowIndex) {
			throw new RuntimeException("bin index out of bounds: " + index);
		}
		int adjustedIndex = lowIndex + index;
		return map.get(adjustedIndex);

	}

}
