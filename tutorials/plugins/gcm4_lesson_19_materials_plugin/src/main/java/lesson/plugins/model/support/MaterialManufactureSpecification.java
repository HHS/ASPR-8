package lesson.plugins.model.support;

import plugins.materials.support.BatchId;
import plugins.materials.support.MaterialId;

public final class MaterialManufactureSpecification {

	public static class Builder {
		private Data data = new Data();

		private Builder() {

		}

		public MaterialManufactureSpecification build() {
			return new MaterialManufactureSpecification(new Data(data));
		}

		public Builder setBatchId(final BatchId batchId) {
			data.batchId = batchId;
			return this;
		}

		public Builder setDeliveryAmount(final double deliveryAmount) {
			data.deliveryAmount = deliveryAmount;
			return this;
		}

		public Builder setDeliveryDelay(final double deliveryDelay) {
			data.deliveryDelay = deliveryDelay;
			return this;
		}

		public Builder setMaterialId(final MaterialId materialId) {
			data.materialId = materialId;
			return this;
		}

		public Builder setStageAmount(final double stageAmount) {
			data.stageAmount = stageAmount;
			return this;
		}

	}

	private static class Data {

		private MaterialId materialId;
		private boolean onOrder;
		private double deliveryAmount;
		private double deliveryDelay;
		private double stageAmount;
		private BatchId batchId;

		public Data() {
		}

		public Data(Data data) {
			materialId = data.materialId;
			onOrder = data.onOrder;
			deliveryAmount = data.deliveryAmount;
			deliveryDelay = data.deliveryDelay;
			stageAmount = data.stageAmount;
			data.batchId = batchId;
		}
	}

	public static Builder builder() {
		return new Builder();
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

}