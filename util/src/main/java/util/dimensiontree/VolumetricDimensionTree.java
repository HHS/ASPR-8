package util.dimensiontree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.FastMath;

public class VolumetricDimensionTree<T> {
	private final static int DEFAULTLEAFSIZE = 15;

	private static class DimensionTreeRec<T> {
		double maxRadius;
		DimensionTree<LocationRec<T>> dimensionTree;

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("DimensionTreeRec [maxRadius=");
			builder.append(maxRadius);
			builder.append(", dimensionTree=\n");
			builder.append(dimensionTree);
			builder.append("\n]");
			return builder.toString();
		}

	}

	private static class InitialTreeSettings {
		private double[] lowerBounds;
		private double[] upperBounds;
		private int leafSize = DEFAULTLEAFSIZE;
		private boolean fastRemovals = false;
	}

	// public static <T> Builder<T> builder(Class<T> c) {
	// return new Builder<>();
	// }

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private InitialTreeSettings initialTreeSettings = new InitialTreeSettings();

		private Builder() {
		}

		public Builder setFastRemovals(boolean fastRemovals) {
			initialTreeSettings.fastRemovals = fastRemovals;
			return this;
		}

		public Builder setLeafSize(int leafSize) {
			initialTreeSettings.leafSize = leafSize;
			return this;
		}

		public Builder setLowerBounds(double[] lowerBounds) {
			initialTreeSettings.lowerBounds = Arrays.copyOf(lowerBounds, lowerBounds.length);
			return this;
		}

		public Builder setUpperBounds(double[] upperBounds) {
			initialTreeSettings.upperBounds = Arrays.copyOf(upperBounds, upperBounds.length);
			return this;
		}

		public <T> VolumetricDimensionTree<T> build() {

			try {
				return new VolumetricDimensionTree<>(initialTreeSettings);
			} finally {
				initialTreeSettings = new InitialTreeSettings();
			}

		}
	}

	private static class LocationRec<K> {

		final double[] position;
		final double radius;
		final K location;

		public LocationRec(double[] position, double radius, K location) {
			this.position = position;
			this.location = location;
			this.radius = radius;
		}

		// public double squareDistance(double[] position) {
		// double result = 0;
		// for (int i = 0; i < position.length; i++) {
		// double delta = (position[i] - this.position[i]);
		// result += delta * delta;
		// }
		// return result;
		// }

		public boolean containsPosition(double[] position, double radius) {
			double square_distance = 0;
			for (int i = 0; i < position.length; i++) {
				double d = this.position[i] - position[i];
				square_distance += d * d;
			}

			double combinedRadius = this.radius + radius;
			return square_distance < combinedRadius * combinedRadius;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("LocationRec [position=");
			builder.append(Arrays.toString(position));
			builder.append(", radius=");
			builder.append(radius);
			builder.append(", location=");
			builder.append(location);
			builder.append("]");
			return builder.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((location == null) ? 0 : location.hashCode());
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
			if (!(obj instanceof LocationRec)) {
				return false;
			}
			@SuppressWarnings("rawtypes")
			LocationRec other = (LocationRec) obj;
			if (location == null) {
				if (other.location != null) {
					return false;
				}
			} else if (!location.equals(other.location)) {
				return false;
			}
			return true;
		}

	}

	private final InitialTreeSettings initialTreeSettings;

	private Map<Integer, DimensionTreeRec<T>> treeMap = new LinkedHashMap<>();

	private DimensionTreeRec<T> defaultDimensionTreeRec;

	private VolumetricDimensionTree(InitialTreeSettings initialTreeSettings) {
		this.initialTreeSettings = initialTreeSettings;
		defaultDimensionTreeRec = new DimensionTreeRec<>();
		defaultDimensionTreeRec.dimensionTree = DimensionTree	.builder()//
																.setLowerBounds(initialTreeSettings.lowerBounds)//
																.setUpperBounds(initialTreeSettings.upperBounds)//
																.setLeafSize(initialTreeSettings.leafSize)//
																.setFastRemovals(initialTreeSettings.fastRemovals)//
																.build();//

		defaultDimensionTreeRec.maxRadius = 0;
	}

	private Integer getIndexFromRadius(double radius) {

		try {
			return (int) (FastMath.ceil(FastMath.log(radius)));
		} catch (Exception e) {
			return null;
		}
	}

	public void add(double[] position, double radius, T t) {

		// first, determine the DimensionRec we are to use
		Integer index = getIndexFromRadius(radius);

		DimensionTreeRec<T> dimensionTreeRec;
		if (index == null) {
			dimensionTreeRec = defaultDimensionTreeRec;
		} else {
			dimensionTreeRec = treeMap.get(index);
			if (dimensionTreeRec == null) {
				dimensionTreeRec = new DimensionTreeRec<>();

				dimensionTreeRec.dimensionTree = DimensionTree	.builder().setLowerBounds(initialTreeSettings.lowerBounds)//
																.setUpperBounds(initialTreeSettings.upperBounds)//
																.setLeafSize(initialTreeSettings.leafSize)//
																.setFastRemovals(initialTreeSettings.fastRemovals)//
																.build();//

				dimensionTreeRec.maxRadius = FastMath.exp(index);
				treeMap.put(index, dimensionTreeRec);
			}
		}
		LocationRec<T> locationRec = new LocationRec<>(position.clone(), radius, t);
		dimensionTreeRec.dimensionTree.add(position, locationRec);
	}

	/**
	 * Removes the member from this tree using the suggested radius to narrow
	 * the search. If the given radius is different from any radius used to
	 * store the object, there is no guarantee that the object will be removed.
	 * This is not guaranteed to remove all occurrences of the member if
	 * multiple radii and positions were used to add the member.
	 */
	public boolean remove(double radius, T t) {

		// first, determine the DimensionRec we are to use
		Integer index = getIndexFromRadius(radius);

		DimensionTreeRec<T> dimensionTreeRec = null;
		if (index == null) {
			dimensionTreeRec = defaultDimensionTreeRec;
		} else {
			dimensionTreeRec = treeMap.get(index);
		}
		if (dimensionTreeRec != null) {
			LocationRec<T> locationRec = new LocationRec<>(null, radius, t);
			return dimensionTreeRec.dimensionTree.remove(locationRec);
		}
		return false;
	}

	/**
	 * Removes all occurences of the given member from this tree
	 */
	public boolean remove(T t) {
		LocationRec<T> locationRec = new LocationRec<>(null, 0, t);
		boolean result = defaultDimensionTreeRec.dimensionTree.remove(locationRec);
		for (DimensionTreeRec<T> dimensionTreeRec : treeMap.values()) {
			result |= dimensionTreeRec.dimensionTree.remove(locationRec);
		}
		return result;
	}

	public boolean contains(T t) {
		LocationRec<T> locationRec = new LocationRec<>(null, 0, t);
		boolean result = defaultDimensionTreeRec.dimensionTree.contains(locationRec);
		if (!result) {
			for (DimensionTreeRec<T> dimensionTreeRec : treeMap.values()) {
				result |= dimensionTreeRec.dimensionTree.contains(locationRec);
				if (result) {
					break;
				}
			}
		}
		return result;
	}

	public List<T> getAll() {
		List<T> result = new ArrayList<>();
		for (DimensionTreeRec<T> dimensionTreeRec : treeMap.values()) {
			for (LocationRec<T> nodeWrapper : dimensionTreeRec.dimensionTree.getAll()) {
				result.add(nodeWrapper.location);
			}
		}
		return result;
	}

	public List<T> getMembersInSphere(double radius, double[] position) {
		List<T> result = new ArrayList<>();
		for (LocationRec<T> locationRec : defaultDimensionTreeRec.dimensionTree.getMembersInSphere(radius, position)) {
			result.add(locationRec.location);
		}
		for (DimensionTreeRec<T> dimensionTreeRec : treeMap.values()) {
			List<LocationRec<T>> potentialIntersections = dimensionTreeRec.dimensionTree.getMembersInSphere(radius + dimensionTreeRec.maxRadius, position);
			for (LocationRec<T> locationRec : potentialIntersections) {
				if (locationRec.containsPosition(position, radius)) {
					result.add(locationRec.location);
				}
			}
		}
		return result;
	}
}
