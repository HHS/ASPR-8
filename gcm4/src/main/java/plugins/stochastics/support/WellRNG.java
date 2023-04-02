package plugins.stochastics.support;

import org.apache.commons.math3.random.Well44497b;

public class CopyableWell44497b extends Well44497b {

	private static final long serialVersionUID = -6456786712498941886L;

	private final long seed;

	public CopyableWell44497b(Well44497bSeed well44497bSeed) {
		super(well44497bSeed.getSeed());
		if (!well44497bSeed.isSimple()) {
			int[] vArray = well44497bSeed.getVArray();
			for (int i = 0; i < vArray.length; i++) {
				v[i] = vArray[i];
			}
			this.index = well44497bSeed.getIndex();
		}
		this.seed = well44497bSeed.getSeed();
	}

	public Well44497bSeed getWell44497bSeed() {
		return Well44497bSeed.builder().setSeed(seed).setInternals(index, v).build();
	}

}
