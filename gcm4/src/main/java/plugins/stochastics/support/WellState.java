package plugins.stochastics.support;

import java.util.Arrays;

import net.jcip.annotations.ThreadSafe;
import util.errors.ContractException;

@ThreadSafe
public class WellState {

	private static class Data {
		long seed;
		int index;
		int[] vArray;

		public Data() {
		}

		public Data(Data data) {
			seed = data.seed;
			index = data.index;
			vArray = Arrays.copyOf(data.vArray, data.vArray.length);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + index;
			result = prime * result + (int) (seed ^ (seed >>> 32));
			result = prime * result + Arrays.hashCode(vArray);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Data)) {
				return false;
			}
			Data other = (Data) obj;
			if (index != other.index) {
				return false;
			}
			if (seed != other.seed) {
				return false;
			}
			if (!Arrays.equals(vArray, other.vArray)) {
				return false;
			}
			return true;
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
		return new Builder();
	}

	public static class Builder {
		private Data data = new Data();

		private Builder() {

		}

		public WellState build() {
			if (data.vArray == null) {
				return new Well(data.seed).getWellState();
			}
			return new WellState(new Data(data));
		}

		public Builder setSeed(long seed) {
			data.seed = seed;
			return this;
		}

		public Builder setInternals(int index, int[] vArray) {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof WellState)) {
			return false;
		}
		WellState other = (WellState) obj;
		if (data == null) {
			if (other.data != null) {
				return false;
			}
		} else if (!data.equals(other.data)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder2 = new StringBuilder();
		builder2.append("WellState [data=");
		builder2.append(data);
		builder2.append("]");
		return builder2.toString();
	}
	
	

}
