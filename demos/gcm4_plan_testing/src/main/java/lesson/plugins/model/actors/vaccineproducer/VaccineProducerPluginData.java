package lesson.plugins.model.actors.vaccineproducer;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import lesson.plugins.model.support.ModelError;
import net.jcip.annotations.Immutable;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import plugins.materials.support.BatchId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsError;
import plugins.materials.support.MaterialsProducerId;
import util.errors.ContractException;

/**
 * An immutable container of the initial state of actor plans
 * 
 *
 */
@Immutable
public final class VaccineProducerPluginData implements PluginData {

	/**
	 * Builder class for ActorPluginData
	 * 
	 *
	 */
	public static class Builder implements PluginDataBuilder {
		private Data data;

		private Builder(Data data) {
			this.data = data;
		}

		/**
		 * Returns the {@link ActorPluginData} from the collected information
		 * supplied to this builder. Clears the builder's state.
		 *
		 * 
		 */
		public VaccineProducerPluginData build() {
			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new VaccineProducerPluginData(data);
		}


		public Builder setLastBatchAssemblyEndTime(double lastBatchAssemblyEndTime) {
			ensureDataMutability();
			data.lastBatchAssemblyEndTime = lastBatchAssemblyEndTime;
			return this;
		}

		public Builder setAntigenBatchId(BatchId antigenBatchId) {
			ensureDataMutability();
			data.antigenBatchId = antigenBatchId;
			return this;
		}

		public Builder setMaterialsProducerId(MaterialsProducerId materialsProducerId) {
			ensureDataMutability();
			data.materialsProducerId = materialsProducerId;
			return this;
		}

		public Builder setOrderStatus(MaterialId materialId, boolean onOrder) {
			ensureDataMutability();
			if (materialId == null) {
				throw new ContractException(MaterialsError.NULL_MATERIAL_ID);
			}
			data.materialsOnOrder.put(materialId, onOrder);
			return this;
		}

		public Builder setDeliveryAmount(MaterialId materialId, double deliveryAmount) {
			ensureDataMutability();
			if (materialId == null) {
				throw new ContractException(MaterialsError.NULL_MATERIAL_ID);
			}
			data.deliveryAmounts.put(materialId, deliveryAmount);
			return this;
		}

		public Builder setDeliveryDelay(MaterialId materialId, double deliveryDelay) {
			ensureDataMutability();
			if (materialId == null) {
				throw new ContractException(MaterialsError.NULL_MATERIAL_ID);
			}
			data.deliveryDelays.put(materialId, deliveryDelay);
			return this;
		}

		public Builder setStageAmount(MaterialId materialId, double stageAmount) {
			ensureDataMutability();
			if (materialId == null) {
				throw new ContractException(MaterialsError.NULL_MATERIAL_ID);
			}
			data.stageAmounts.put(materialId, stageAmount);
			return this;
		}

		public Builder addMaterialId(MaterialId materialId) {
			ensureDataMutability();
			if (materialId == null) {
				throw new ContractException(MaterialsError.NULL_MATERIAL_ID);
			}
			data.materialIds.add(materialId);
			return this;
		}

		public Builder setMaterialBatchId(MaterialId materialId, BatchId batchId) {
			ensureDataMutability();
			if (materialId == null) {
				throw new ContractException(MaterialsError.NULL_MATERIAL_ID);
			}
			data.materialBatchIds.put(materialId, batchId);
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
			if (data.materialsProducerId == null) {
				throw new ContractException(MaterialsError.NULL_MATERIALS_PRODUCER_ID);
			}
			if (data.lastBatchAssemblyEndTime < 0) {
				throw new ContractException(ModelError.ANTIGEN_PRODUCER_LAST_BATCH_ASSEMBLY_END_TIME);
			}

			if (!data.materialsOnOrder.keySet().equals(data.materialIds)) {
				throw new ContractException(ModelError.MATERIALS_MISMATCH);
			}
			
			if (!data.deliveryAmounts.keySet().equals(data.materialIds)) {
				throw new ContractException(ModelError.MATERIALS_MISMATCH);
			}
			
			if (!data.deliveryDelays.keySet().equals(data.materialIds)) {
				throw new ContractException(ModelError.MATERIALS_MISMATCH);
			}
			
			if (!data.stageAmounts.keySet().equals(data.materialIds)) {
				throw new ContractException(ModelError.MATERIALS_MISMATCH);
			}
			
			if (!data.materialBatchIds.keySet().equals(data.materialIds)) {
				throw new ContractException(ModelError.MATERIALS_MISMATCH);
			}
		}

	}

	private static class Data {
		private Set<MaterialId> materialIds = new LinkedHashSet<>();
		private Map<MaterialId, Boolean> materialsOnOrder = new LinkedHashMap<>();
		private Map<MaterialId, Double> deliveryAmounts = new LinkedHashMap<>();
		private Map<MaterialId, Double> deliveryDelays = new LinkedHashMap<>();
		private Map<MaterialId, Double> stageAmounts = new LinkedHashMap<>();
		private Map<MaterialId, BatchId> materialBatchIds = new LinkedHashMap<>();
		private MaterialsProducerId materialsProducerId;
		private double lastBatchAssemblyEndTime;
		private BatchId antigenBatchId;

		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			materialIds.addAll(data.materialIds);
			lastBatchAssemblyEndTime = data.lastBatchAssemblyEndTime;
			materialsProducerId = data.materialsProducerId;
			materialsOnOrder.putAll(data.materialsOnOrder);
			antigenBatchId = data.antigenBatchId;
			deliveryAmounts.putAll(data.deliveryAmounts);
			deliveryDelays.putAll(data.deliveryDelays);
			stageAmounts.putAll(data.stageAmounts);
			materialBatchIds.putAll(data.materialBatchIds);
			locked = data.locked;
		}

	}

	/**
	 * Returns a Builder instance
	 */
	public static Builder builder() {
		return new Builder(new Data());
	}


	private final Data data;

	private VaccineProducerPluginData(final Data data) {
		this.data = data;
	}


	public MaterialsProducerId getMaterialsProducerId() {
		return data.materialsProducerId;
	}


	@Override
	public PluginDataBuilder getCloneBuilder() {
		return new Builder(data);
	}

	public double getLastBatchAssemblyEndTime() {
		return data.lastBatchAssemblyEndTime;
	}

	@Override
	public String toString() {
		StringBuilder builder2 = new StringBuilder();
		builder2.append("AntigenProducerPluginData [data=");
		builder2.append(data);
		builder2.append("]");
		return builder2.toString();
	}

	public boolean getOrderStatus(MaterialId materialId) {
		Boolean result = data.materialsOnOrder.get(materialId);
		if (result == null) {
			result = false;
		}
		return result;
	}

	public BatchId getAntigenBatchId() {
		return data.antigenBatchId;
	}

	public double getDeliveryAmount(MaterialId materialId) {

		Double result = data.deliveryAmounts.get(materialId);
		if (result == null) {
			result = 0.0;
		}
		return result;
	}

	public double getDeliveryDelay(MaterialId materialId) {
		Double result = data.deliveryDelays.get(materialId);
		if (result == null) {
			result = 0.0;
		}
		return result;
	}

	public double getStageAmount(MaterialId materialId) {
		Double result = data.stageAmounts.get(materialId);
		if (result == null) {
			result = 0.0;
		}
		return result;
	}

	public BatchId getMaterialBatchId(MaterialId materialId) {
		return data.materialBatchIds.get(materialId);
	}

	public Set<MaterialId> getMaterialIds() {
		return new LinkedHashSet<>(data.materialIds);
	}
}
