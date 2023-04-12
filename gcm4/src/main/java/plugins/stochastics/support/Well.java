package plugins.stochastics.support;

import org.apache.commons.math3.random.Well44497b;

public class Well extends Well44497b {

	private static final long serialVersionUID = -6456786712498941886L;

	private final long seed;

	public Well(WellState wellState) {
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
		if (!(obj instanceof Well)) {
			return false;
		}
		Well other = (Well) obj;

		return getWellState().equals(other.getWellState());

	}

	public static boolean debug;

	private void reporIndex() {
		if (debug) {
			System.out.println("index = " + index);
		}
	}

	@Override
	public boolean nextBoolean() {
		
		boolean result =  super.nextBoolean();
		reporIndex();
		return result;
	}

	@Override
	public double nextDouble() {
		
		double result =  super.nextDouble();
		reporIndex();
		return result;

	}

	@Override
	public float nextFloat() {
		
		float result = super.nextFloat();
		reporIndex();
		return result;
	}

	@Override
	public int nextInt() {
		
		int result = super.nextInt();
		reporIndex();
		return result;
	}

	@Override
	public int nextInt(int n) {
		
		int result = super.nextInt(n);
		reporIndex();
		return result;
	}

	@Override
	public long nextLong() {
		
		long result = super.nextLong();
		reporIndex();
		return result;
	}

}
