package gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.support;

import java.util.Arrays;
import java.util.Objects;

import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public class WellState {

	private static class Data {
		long seed;
		int index;
		int[] vArray;
		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			seed = data.seed;
			index = data.index;
			vArray = Arrays.copyOf(data.vArray, data.vArray.length);
			locked = data.locked;
		}

		/**
    	 * Standard implementation consistent with the {@link #equals(Object)} method
    	 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(vArray);
			result = prime * result + Objects.hash(seed, index);
			return result;
		}

		/**
    	 * Two {@link Data} instances are equal if and only if
    	 * their inputs are equal.
    	 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Data other = (Data) obj;
			return seed == other.seed && index == other.index && Arrays.equals(vArray, other.vArray);
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Data [seed=");
			builder.append(seed);
			builder.append(", index=");
			builder.append(index);
			builder.append(", vArray=");
			builder.append(Arrays.toString(vArray));
			builder.append("]");
			return builder.toString();
		}
	}

	public static Builder builder() {
		return new Builder(new Data());
	}

	public static class Builder {
		private Data data;

		private Builder(Data data) {
			this.data = data;
		}

		public WellState build() {
			if (!data.locked) {
				validateData();
			}
			ensureImmutability();

			if (data.vArray == null) {
				return new Well(data.seed).getWellState();
			}
			
			return new WellState(data);
		}

		public Builder setSeed(long seed) {
			ensureDataMutability();
			data.seed = seed;
			return this;
		}

		public Builder setInternals(int index, int[] vArray) {
			ensureDataMutability();
			if (vArray == null) {
				throw new ContractException(StochasticsError.ILLEGAL_SEED_ININITIAL_STATE);
			}
			if (vArray.length != 1391) {
				throw new ContractException(StochasticsError.ILLEGAL_SEED_ININITIAL_STATE);
			}
			if (index < 0 || index > 1390) {
				throw new ContractException(StochasticsError.ILLEGAL_SEED_ININITIAL_STATE);
			}
			data.index = index;
			data.vArray = Arrays.copyOf(vArray, vArray.length);
			return this;
		}

		private void ensureDataMutability() {
			if (data.locked) {
				data = new Data(data);
				data.locked = false;
			}
		}

		private void ensureImmutability() {
			if (!data.locked) {
				data.locked = true;
			}
		}

		private void validateData() {

		}

	}

	private WellState(Data data) {
		this.data = data;
	}

	private final Data data;

	public long getSeed() {
		return data.seed;
	}

	public int getIndex() {
		return data.index;
	}

	public int[] getVArray() {
		return Arrays.copyOf(data.vArray, data.vArray.length);
	}

	/**
	 * Standard implementation consistent with the {@link #equals(Object)} method
	 */
	@Override
	public int hashCode() {
		return Objects.hash(data);
	}

	/**
     * Two {@link WellState} instances are equal if and only if
     * their inputs are equal.
     */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		WellState other = (WellState) obj;
		return Objects.equals(data, other.data);
	}

	@Override
	public String toString() {
		StringBuilder builder2 = new StringBuilder();
		builder2.append("WellState [data=");
		builder2.append(data);
		builder2.append("]");
		return builder2.toString();
	}
	
	/**
	 * Returns a new builder instance that is pre-filled with the current state of
	 * this instance.
	 */
	public Builder toBuilder() {
		return new Builder(data);
	}

}
