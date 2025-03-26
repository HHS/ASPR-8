package gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.support;

import java.util.Arrays;

import org.apache.commons.math3.random.Well44497b;

public class Well extends Well44497b {

	private static final long serialVersionUID = -6456786712498941886L;

	private final long seed;

	public Well(WellState wellState) {
		super(wellState.getSeed());

		int[] vArray = wellState.getVArray();
		for (int i = 0; i < vArray.length; i++) {
			v[i] = vArray[i];
		}
		this.index = wellState.getIndex();

		this.seed = wellState.getSeed();
	}

	Well(long seed) {
		super(seed);
		this.seed = seed;
	}

	public WellState getWellState() {
		return WellState.builder().setSeed(seed).setInternals(index, v).build();
	}

	/**
     * Standard implementation consistent with the {@link #equals(Object)} method.
	 * Forwards to the respective WellState.
     */
	@Override
	public int hashCode() {
		return getWellState().hashCode();
	}

	/**
     * Two {@link Well} instances are equal if and only if
     * their inputs are equal. Forwards to the respective WellState.
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
		Well other = (Well) obj;
		return getWellState().equals(other.getWellState());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Well [seed=");
		builder.append(seed);
		builder.append(", index=");
		builder.append(index);
		builder.append(", v=");
		builder.append(Arrays.toString(v));
		builder.append("]");
		return builder.toString();
	}

}
