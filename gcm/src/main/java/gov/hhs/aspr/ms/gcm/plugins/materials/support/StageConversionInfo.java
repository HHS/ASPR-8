package gov.hhs.aspr.ms.gcm.plugins.materials.support;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyError;
import net.jcip.annotations.Immutable;
import util.errors.ContractException;

/**
 * Represents the information to fully specify the conversion of a stage into a
 * batch
 */
@Immutable
public class StageConversionInfo {

	private StageConversionInfo(Data data) {
		this.data = data;
	}

	private final Data data;

	private static class Data {
		private StageId stageId;
		private MaterialId materialId;
		private double amount;
		private Map<BatchPropertyId, Object> propertyValues = new LinkedHashMap<>();

		public Data() {
		}

		public Data(Data data) {
			stageId = data.stageId;
			materialId = data.materialId;
			amount = data.amount;
			propertyValues.putAll(data.propertyValues);
		}
	}

	/**
	 * Returns a builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * A builder class for BatchConstructionInfo
	 */
	public static class Builder {
		private Data data = new Data();

		private Builder() {
		}

		private void validate() {
			if (data.materialId == null) {
				throw new ContractException(MaterialsError.NULL_MATERIAL_ID);
			}
			if (data.stageId == null) {
				throw new ContractException(MaterialsError.NULL_STAGE_ID);
			}
		}

		/**
		 * Builds the BatchConstructionInfo from the collected data
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if
		 *                           the material id was not set</li>
		 *                           <li>{@linkplain MaterialsError#NULL_STAGE_ID} if
		 *                           the stage id was not set</li>
		 *                           </ul>
		 */
		public StageConversionInfo build() {
			validate();
			return new StageConversionInfo(new Data(data));
		}

		/**
		 * Sets the amount. Defaulted to zero.
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain MaterialsError#NEGATIVE_MATERIAL_AMOUNT}
		 *                           * if the amount is negative</li>
		 *                           <li>{@linkplain MaterialsError#NON_FINITE_MATERIAL_AMOUNT}
		 *                           if the amount is not finite</li>
		 *                           </ul>
		 */
		public Builder setAmount(double amount) {
			if (!Double.isFinite(amount)) {
				throw new ContractException(MaterialsError.NON_FINITE_MATERIAL_AMOUNT);
			}
			if (amount < 0) {
				throw new ContractException(MaterialsError.NEGATIVE_MATERIAL_AMOUNT);
			}

			data.amount = amount;
			return this;
		}

		/**
		 * Sets the material id. Defaulted to null.
		 * 
		 * @throws ContractException {@linkplain MaterialsError#NULL_MATERIAL_ID} if the
		 *                           material id is null
		 */
		public Builder setMaterialId(MaterialId materialId) {
			if (materialId == null) {
				throw new ContractException(MaterialsError.NULL_MATERIAL_ID);
			}
			data.materialId = materialId;
			return this;
		}

		/**
		 * Sets the stager id. Defaulted to null.
		 * 
		 * @throws ContractException
		 *                           <ul><li>{@linkplain MaterialsError#NULL_STAGE_ID} if
		 *                           the stage id is null</li></ul>
		 */
		public Builder setStageId(StageId stageId) {
			if (stageId == null) {
				throw new ContractException(MaterialsError.NULL_STAGE_ID);
			}
			data.stageId = stageId;
			return this;
		}

		/**
		 * Sets a property value;
		 *
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
		 *                           the property id is null</li>
		 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_VALUE}
		 *                           if the property value is null</li>
		 *                           </ul>
		 */
		public Builder setPropertyValue(BatchPropertyId batchPropertyId, Object propertyValue) {
			if (batchPropertyId == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_ID);
			}
			if (propertyValue == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
			}
			data.propertyValues.put(batchPropertyId, propertyValue);
			return this;
		}
	}

	/**
	 * Return the material id of the new batch
	 */
	public MaterialId getMaterialId() {
		return data.materialId;
	}

	/**
	 * Return the stage id being converted into a batch
	 */
	public StageId getStageId() {
		return data.stageId;
	}

	/**
	 * Returns the amount of the new batch
	 */
	public double getAmount() {
		return data.amount;
	}

	/**
	 * Returns a map of the batch property values of the new batch
	 */
	public Map<BatchPropertyId, Object> getPropertyValues() {
		return Collections.unmodifiableMap(data.propertyValues);
	}

}
