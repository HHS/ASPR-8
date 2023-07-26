package lesson.plugins.model.actors.antigenproducer;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.nucleus.PluginData;
import gov.hhs.aspr.ms.gcm.nucleus.PluginDataBuilder;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.BatchId;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialId;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialsError;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialsProducerId;
import lesson.plugins.model.support.ModelError;
import net.jcip.annotations.Immutable;
import util.errors.ContractException;

/**
 * An immutable container of the initial state of actor plans
 * 
 *
 */
@Immutable
public final class AntigenProducerPluginData implements PluginData {

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
		public AntigenProducerPluginData build() {
			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new AntigenProducerPluginData(data);
		}

		public Builder setLastBatchAssemblyEndTime(double lastBatchAssemblyEndTime) {
			ensureDataMutability();
			data.lastBatchAssemblyEndTime = lastBatchAssemblyEndTime;
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

		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			materialIds.addAll(data.materialIds);
			lastBatchAssemblyEndTime = data.lastBatchAssemblyEndTime;
			materialsProducerId = data.materialsProducerId;
			materialsOnOrder.putAll(data.materialsOnOrder);
			deliveryAmounts.putAll(data.deliveryAmounts);
			deliveryDelays.putAll(data.deliveryDelays);
			stageAmounts.putAll(data.stageAmounts);
			materialBatchIds.putAll(data.materialBatchIds);
			locked = data.locked;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Data [materialsOnOrder=");
			builder.append(materialsOnOrder);
			builder.append(", materialsProducerId=");
			builder.append(materialsProducerId);
			builder.append(", lastBatchAssemblyEndTime=");
			builder.append(lastBatchAssemblyEndTime);
			builder.append(", locked=");
			builder.append(locked);
			builder.append("]");
			return builder.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((deliveryAmounts == null) ? 0 : deliveryAmounts.hashCode());
			result = prime * result + ((deliveryDelays == null) ? 0 : deliveryDelays.hashCode());
			long temp;
			temp = Double.doubleToLongBits(lastBatchAssemblyEndTime);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result + (locked ? 1231 : 1237);
			result = prime * result + ((materialBatchIds == null) ? 0 : materialBatchIds.hashCode());
			result = prime * result + ((materialIds == null) ? 0 : materialIds.hashCode());
			result = prime * result + ((materialsOnOrder == null) ? 0 : materialsOnOrder.hashCode());
			result = prime * result + ((materialsProducerId == null) ? 0 : materialsProducerId.hashCode());
			result = prime * result + ((stageAmounts == null) ? 0 : stageAmounts.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Data)) {
				return false;
			}
			Data other = (Data) obj;
			if (deliveryAmounts == null) {
				if (other.deliveryAmounts != null) {
					return false;
				}
			} else if (!deliveryAmounts.equals(other.deliveryAmounts)) {
				return false;
			}
			if (deliveryDelays == null) {
				if (other.deliveryDelays != null) {
					return false;
				}
			} else if (!deliveryDelays.equals(other.deliveryDelays)) {
				return false;
			}
			if (Double.doubleToLongBits(lastBatchAssemblyEndTime) != Double
					.doubleToLongBits(other.lastBatchAssemblyEndTime)) {
				return false;
			}
			if (locked != other.locked) {
				return false;
			}
			if (materialBatchIds == null) {
				if (other.materialBatchIds != null) {
					return false;
				}
			} else if (!materialBatchIds.equals(other.materialBatchIds)) {
				return false;
			}
			if (materialIds == null) {
				if (other.materialIds != null) {
					return false;
				}
			} else if (!materialIds.equals(other.materialIds)) {
				return false;
			}
			if (materialsOnOrder == null) {
				if (other.materialsOnOrder != null) {
					return false;
				}
			} else if (!materialsOnOrder.equals(other.materialsOnOrder)) {
				return false;
			}
			if (materialsProducerId == null) {
				if (other.materialsProducerId != null) {
					return false;
				}
			} else if (!materialsProducerId.equals(other.materialsProducerId)) {
				return false;
			}
			if (stageAmounts == null) {
				if (other.stageAmounts != null) {
					return false;
				}
			} else if (!stageAmounts.equals(other.stageAmounts)) {
				return false;
			}
			return true;
		}

	}

	/**
	 * Returns a Builder instance
	 */
	public static Builder builder() {
		return new Builder(new Data());
	}

	private final Data data;

	private AntigenProducerPluginData(final Data data) {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof AntigenProducerPluginData)) {
			return false;
		}
		AntigenProducerPluginData other = (AntigenProducerPluginData) obj;
		if (data == null) {
			if (other.data != null) {
				return false;
			}
		} else if (!data.equals(other.data)) {
			return false;
		}
		return true;
	}

}
