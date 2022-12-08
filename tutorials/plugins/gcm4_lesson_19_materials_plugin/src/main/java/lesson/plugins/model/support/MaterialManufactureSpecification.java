package lesson.plugins.model.support;

import plugins.materials.support.BatchId;
import plugins.materials.support.MaterialId;

public final class AntigenMaterialRec {

	private static class Data {

		private MaterialId materialId;
		private boolean onOrder;
		private double deliveryAmount;
		private double deliveryDelay;
		private double stageAmount;
		private BatchId batchId;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Data data = new Data();
		
		private Builder() {
			
		}

		public AntigenMaterialRec build() {
			try {
				return new AntigenMaterialRec(data);
			} finally {
				data = new Data();
			}
		}

		public Builder setDeliveryAmount(double deliveryAmount) {
			data.deliveryAmount = deliveryAmount;
			return this;
		}
		public Builder setDeliveryDelay(double deliveryDelay) {
			data.deliveryDelay = deliveryDelay;
			return this;
		}
		public Builder setStageAmount(double stageAmount) {
			data.stageAmount = stageAmount;
			return this;
		}
		public Builder setMaterialId(MaterialId materialId) {
			data.materialId = materialId;
			return this;
		}
		public Builder setBatchId(BatchId batchId) {
			data.batchId = batchId;
			return this;
		}

	}

	private final Data data;

	private AntigenMaterialRec(Data data) {
		this.data = data;
	}

	public boolean isOnOrder() {
		return data.onOrder;
	}

	public double getDeliveryAmount() {
		return data.deliveryAmount;
	}

	public double getDeliveryDelay() {
		return data.deliveryDelay;
	}

	public double getStageAmount() {
		return data.stageAmount;
	}
	
	public MaterialId getMaterialId() {
		return data.materialId;
	}
	
	public void toggleOnOrder() {
		data.onOrder = !data.onOrder;
	}
	
	public BatchId getBatchId() {
		return data.batchId;
	}
	
	
}