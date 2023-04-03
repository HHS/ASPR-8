package lesson.plugins.model.actors.vaccineproducer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import lesson.plugins.model.support.ModelError;
import net.jcip.annotations.Immutable;
import nucleus.NucleusError;
import nucleus.PlanData;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import nucleus.PrioritizedPlanData;
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

		/**
		 * Adds a plan data.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain NucleusError.NULL_PLAN_DATA}</li> if the
		 *             plan data is null
		 */
		public Builder addPrioritizedPlanData(final PrioritizedPlanData prioritizedPlanData) {
			ensureDataMutability();
			validatePlanDataNotNull(prioritizedPlanData);
			data.prioritizedPlanDatas.add(prioritizedPlanData);
			return this;
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
			if (onOrder) {
				data.materialsOnOrder.add(materialId);
			} else {
				data.materialsOnOrder.remove(materialId);
			}
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
		private Set<MaterialId> materialsOnOrder = new LinkedHashSet<>();
		private MaterialsProducerId materialsProducerId;
		private double lastBatchAssemblyEndTime;
		private BatchId antigenBatchId;
		
		private final List<PrioritizedPlanData> prioritizedPlanDatas = new ArrayList<>();

		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			prioritizedPlanDatas.addAll(data.prioritizedPlanDatas);
			lastBatchAssemblyEndTime = data.lastBatchAssemblyEndTime;
			materialsProducerId = data.materialsProducerId;
			materialsOnOrder.addAll(data.materialsOnOrder);
			antigenBatchId = data.antigenBatchId;
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
			builder.append(", antigenBatchId=");
			builder.append(antigenBatchId);
			builder.append(", prioritizedPlanDatas=");
			builder.append(prioritizedPlanDatas);
			builder.append(", locked=");
			builder.append(locked);
			builder.append("]");
			return builder.toString();
		}

		

	}

	/**
	 * Returns a Builder instance
	 */
	public static Builder builder() {
		return new Builder(new Data());
	}

	private static void validatePlanDataNotNull(final PrioritizedPlanData prioritizedPlanData) {
		if (prioritizedPlanData == null) {
			throw new ContractException(NucleusError.NULL_PLAN_DATA);
		}
	}

	private final Data data;

	private VaccineProducerPluginData(final Data data) {
		this.data = data;
	}

	/**
	 * Returns the {@link PlanData} objects that are instances of the give class
	 * reference
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain NucleusError#NULL_CLASS_REFERENCE}</li> if
	 *             the class reference is null
	 * 
	 */
	public List<PrioritizedPlanData> getPrioritizedPlanDatas(final Class<?> classReference) {
		validateClassReferenceNotNull(classReference);
		List<PrioritizedPlanData> result = new ArrayList<>();
		for (PrioritizedPlanData prioritizedPlanData : data.prioritizedPlanDatas) {
			if (classReference.isAssignableFrom(prioritizedPlanData.getPlanData().getClass())) {
				result.add(prioritizedPlanData);
			}
		}

		return result;
	}

	public MaterialsProducerId getMaterialsProducerId() {
		return data.materialsProducerId;
	}

	private static void validateClassReferenceNotNull(final Class<?> classReference) {
		if (classReference == null) {
			throw new ContractException(NucleusError.NULL_CLASS_REFERENCE);
		}

	}

	@Override
	public PluginDataBuilder getCloneBuilder() {
		return new Builder(data);
	}

	@Override
	public PluginDataBuilder getEmptyBuilder() {
		return builder();
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
		return data.materialsOnOrder.contains(materialId);
	}
	
	public BatchId getAntigenBatchId() {		
		return data.antigenBatchId;		
	}

}
