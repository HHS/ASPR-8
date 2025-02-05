package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import java.util.List;

/**
 * A Dimension implementation based on Functions as level implementations.
 */
public final class FunctionalDimension implements Dimension {

	private final FunctionalDimensionData functionalDimensionData;

	public FunctionalDimension(FunctionalDimensionData functionalDimensionData) {
		this.functionalDimensionData = functionalDimensionData;
	}

	public DimensionData getDimensionData() {
		return functionalDimensionData;
	}

	@Override
	public List<String> getExperimentMetaData() {
		return functionalDimensionData.getMetaData();
	}

	@Override
	public int levelCount() {
		return functionalDimensionData.getLevelCount();
	}

	@Override
	public List<String> executeLevel(DimensionContext dimensionContext, int level) {
		return functionalDimensionData.getValue(level).apply(dimensionContext);
	}

	@Override
	public String toString() {
		return "FunctionalDimension [functionalDimensionData=" + functionalDimensionData + "]";
	}
}
