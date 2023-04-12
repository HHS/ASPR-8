package plugins.stochastics.support;

import java.util.Arrays;

import net.jcip.annotations.ThreadSafe;
import util.errors.ContractException;

@ThreadSafe
public class WellState {

	private static class Data {
		boolean simple = true;
		long seed;
		int index;
		int[] vArray = new int[0];
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + index;
			result = prime * result + (int) (seed ^ (seed >>> 32));
			result = prime * result + (simple ? 1231 : 1237);
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
			if (simple != other.simple) {
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
			builder.append("Data [simple=");
			builder.append(simple);
			builder.append(", seed=");
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
			try {
				return new WellState(data);
			} finally {
				data = new Data();
			}
		}

		public Builder setSeed(long seed) {
			data.seed = seed;
			return this;
		}

		public Builder setInternals(int index, int[] vArray) {
			if (vArray != null) {
				if(vArray.length != 1391) {
					throw new ContractException(StochasticsError.ILLEGAL_SEED_ININITIAL_STATE);
				}
				if(index<0||index>1390) {
					throw new ContractException(StochasticsError.ILLEGAL_SEED_ININITIAL_STATE);
				}
				data.simple = false;
				data.index = index;
				data.vArray = Arrays.copyOf(vArray, vArray.length);
			}else {
				data.index = 0;
				data.vArray = new int[0];
				data.simple = true;
			}
			return this;
		}

	}

	private WellState(Data data) {
		this.data = data;
	}

	private final Data data;

	public boolean isSimple() {
		return data.simple;
	}

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
		builder2.append("Well44497bSeed [data=");
		builder2.append(data);
		builder2.append("]");
		return builder2.toString();
	}
	
	
}
