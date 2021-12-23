package plugins.support;

import java.util.Arrays;

import org.apache.commons.math3.random.RandomGenerator;

import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialId;

public enum XTestMaterialId implements MaterialId {
	MATERIAL_1("BProp_1_1", "BProp_1_2"), MATERIAL_2("BProp_2_1"), MATERIAL_3("BProp_3_1", "BProp_3_2", "BProp_3_3"), MATERIAL_4("BProp_4_1", "BProp_4_2"), MATERIAL_5("BProp_5_1", "BProp_5_2", "BProp_5_3");

	private static class TestBatchPropertyId implements BatchPropertyId {
		private final String id;

		private TestBatchPropertyId(String id) {
			this.id = id;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("TestBatchPropertyId [id=");
			builder.append(id);
			builder.append("]");
			return builder.toString();
		}

	}

	private BatchPropertyId[] batchPropertyIds;

	private XTestMaterialId(String... strings) {

		batchPropertyIds = new BatchPropertyId[strings.length];
		for (int i = 0; i < strings.length; i++) {
			batchPropertyIds[i] = new TestBatchPropertyId(strings[i]);
		}
	}

	public BatchPropertyId[] getBatchPropertyIds() {
		return Arrays.copyOf(batchPropertyIds, batchPropertyIds.length);
	}

	public static XTestMaterialId getRandomMaterialId(final RandomGenerator randomGenerator) {
		return XTestMaterialId.values()[randomGenerator.nextInt(XTestMaterialId.values().length)];
	}

	public static int size() {
		return values().length;
	}

	private XTestMaterialId next;

	public XTestMaterialId next() {
		if (next == null) {
			next = XTestMaterialId.values()[(ordinal() + 1) % XTestMaterialId.values().length];
		}
		return next;
	}

	/**
	 * Returns a new {@link MaterialId} instance.
	 */
	public static MaterialId getUnknownMaterialId() {
		return new MaterialId() {
		};
	}

	/**
	 * Returns a new {@link BatchPropertyId} instance.
	 */
	public static BatchPropertyId getUnknownBatchPropertyId() {
		return new BatchPropertyId() {
		};
	}
}
