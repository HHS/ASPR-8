package gov.hhs.aspr.ms.gcm.lessons.plugins.model.support;

import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.BatchId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.MaterialId;

public final class MaterialManufactureSpecification {

	public static class Builder {
		private Data data;

		private Builder(final Data data) {
			this.data = data;
		}

		public MaterialManufactureSpecification build() {
			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new MaterialManufactureSpecification(data);
		}

		public Builder setBatchId(final BatchId batchId) {
			ensureDataMutability();
			data.batchId = batchId;
			return this;
		}

		public Builder setDeliveryAmount(final double deliveryAmount) {
			ensureDataMutability();
			data.deliveryAmount = deliveryAmount;
			return this;
		}

		public Builder setDeliveryDelay(final double deliveryDelay) {
			ensureDataMutability();
			data.deliveryDelay = deliveryDelay;
			return this;
		}

		public Builder setMaterialId(final MaterialId materialId) {
			ensureDataMutability();
			data.materialId = materialId;
			return this;
		}

		public Builder setStageAmount(final double stageAmount) {
			ensureDataMutability();
			data.stageAmount = stageAmount;
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

	private static class Data {

		private MaterialId materialId;
		private boolean onOrder;
		private double deliveryAmount;
		private double deliveryDelay;
		private double stageAmount;
		private BatchId batchId;
		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			materialId = data.materialId;
			onOrder = data.onOrder;
			deliveryAmount = data.deliveryAmount;
			deliveryDelay = data.deliveryDelay;
			stageAmount = data.stageAmount;
			batchId = data.batchId;
			locked = data.locked;
		}
	}

	public static Builder builder() {
		return new Builder(new Data());
	}

	private final Data data;

	private MaterialManufactureSpecification(final Data data) {
		this.data = data;
	}

	public BatchId getBatchId() {
		return data.batchId;
	}

	public double getDeliveryAmount() {
		return data.deliveryAmount;
	}

	public double getDeliveryDelay() {
		return data.deliveryDelay;
	}

	public MaterialId getMaterialId() {
		return data.materialId;
	}

	public double getStageAmount() {
		return data.stageAmount;
	}

	public boolean isOnOrder() {
		return data.onOrder;
	}

	public void toggleOnOrder() {
		data.onOrder = !data.onOrder;
	}

	/**
	 * Returns a new builder instance that is pre-filled with the current state of
	 * this instance.
	 */
	public Builder toBuilder() {
		return new Builder(data);
	}

}