package plugins.stochastics.support;

import org.apache.commons.math3.random.Well44497b;

public class WellRNG extends Well44497b {

	private static final long serialVersionUID = -6456786712498941886L;

	private final long seed;

	public WellRNG(WellState wellState) {
		super(wellState.getSeed());
		if (!wellState.isSimple()) {
			int[] vArray = wellState.getVArray();
			for (int i = 0; i < vArray.length; i++) {
				v[i] = vArray[i];
			}
			this.index = wellState.getIndex();
		}
		this.seed = wellState.getSeed();
	}

	public WellState getWellState() {
		return WellState.builder().setSeed(seed).setInternals(index, v).build();
	}

	@Override
	public int hashCode() {
		return getWellState().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof WellRNG)) {
			return false;
		}
		WellRNG other = (WellRNG) obj;
		
		return getWellState().equals(other.getWellState());
		
	}

}
