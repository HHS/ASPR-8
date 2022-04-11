package plugins.materials.support;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.jcip.annotations.Immutable;

/**
 * Represents the information to fully specify a batch, but not its relationship
 * to stages
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public class BatchConstructionInfo {

	private BatchConstructionInfo(Scaffold scaffold) {
		this.scaffold = scaffold;
	}

	private final Scaffold scaffold;

	private static class Scaffold {
		private MaterialsProducerId materialsProducerId;
		private MaterialId materialId;
		private double amount;
		private Map<BatchPropertyId, Object> propertyValues = new LinkedHashMap<>();
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
		private Scaffold scaffold = new Scaffold();

		private Builder() {
		}

		/**
		 * Builds the BatchConstructionInfo from the collected data
		 */
		public BatchConstructionInfo build() {
			try {
				return new BatchConstructionInfo(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}

		/**
		 * Sets the amount. Defaulted to zero.
		 * 
		 */
		public Builder setAmount(double amount) {
			scaffold.amount = amount;
			return this;
		}

		/**
		 * Sets the material id. Defaulted to null.
		 */
		public Builder setMaterialId(MaterialId materialId) {
			scaffold.materialId = materialId;
			return this;
		}
		
		/**
		 * Sets the materials producer id. Defaulted to null.
		 */
		public Builder setMaterialsProducerId(MaterialsProducerId materialsProducerId) {
			scaffold.materialsProducerId = materialsProducerId;
			return this;
		}

		/**
		 * Sets a property value;
		 *
		 */
		public Builder setPropertyValue(BatchPropertyId batchPropertyId, Object propertyValue) {
			scaffold.propertyValues.put(batchPropertyId, propertyValue);
			return this;
		}
	}

	/**
	 * Return the material id of the new batch
	 */
	public MaterialId getMaterialId() {
		return scaffold.materialId;
	}
	
	/**
	 * Return the materials producer id of the new batch
	 */
	public MaterialsProducerId getMaterialsProducerId() {
		return scaffold.materialsProducerId;
	}

	/**
	 * Returns the amount of the new batch
	 */
	public double getAmount() {
		return scaffold.amount;
	}

	/**
	 * Returns a map of the batch property values of the new batch
	 */
	public Map<BatchPropertyId, Object> getPropertyValues() {
		return Collections.unmodifiableMap(scaffold.propertyValues);
	}

}
