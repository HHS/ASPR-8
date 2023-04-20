package plugins.materials.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import net.jcip.annotations.Immutable;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import util.errors.ContractException;

/**
 * A class for defining a material producer property with an associated property
 * id and property values for extant materials producers.
 * 
 * 
 *
 * 
 */
@Immutable
public final class MaterialsProducerPropertyDefinitionInitialization {

	private static class Data {
		MaterialsProducerPropertyId materialsProducerPropertyId;
		PropertyDefinition propertyDefinition;
		List<Pair<MaterialsProducerId, Object>> propertyValues = new ArrayList<>();

		public Data() {
		}

		public Data(Data data) {
			materialsProducerPropertyId = data.materialsProducerPropertyId;
			propertyDefinition = data.propertyDefinition;
			propertyValues.addAll(data.propertyValues);
		}
	}

	private final Data data;

	private MaterialsProducerPropertyDefinitionInitialization(Data data) {
		this.data = data;
	}

	/**
	 * Returns a new Builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder class for a MaterialsProducerPropertyDefinitionInitialization
	 * 
	 *
	 * 
	 */
	public final static class Builder {

		private Builder() {
		}

		private Data data = new Data();

		private void validate() {
			if (data.propertyDefinition == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION);
			}

			if (data.materialsProducerPropertyId == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_ID);
			}

			Class<?> type = data.propertyDefinition.getType();
			for (Pair<MaterialsProducerId, Object> pair : data.propertyValues) {
				Object value = pair.getSecond();
				if (!type.isAssignableFrom(value.getClass())) {
					String message = "Definition Type " + type.getName() + " is not compatible with value = " + value;
					throw new ContractException(PropertyError.INCOMPATIBLE_VALUE, message);
				}
			}
		}

		/**
		 * Constructs the MaterialsProducerPropertyDefinitionInitialization from
		 * the collected data
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION}
		 *             if no property definition was assigned to the
		 *             builder</li>
		 * 
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if no
		 *             property id was assigned to the builder</li>
		 * 
		 *             <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE} if a
		 *             collected property value is incompatible with the
		 *             property definition</li>
		 */
		public MaterialsProducerPropertyDefinitionInitialization build() {
			validate();
			return new MaterialsProducerPropertyDefinitionInitialization(new Data(data));
		}

		/**
		 * Sets the materials producer property id
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
		 *             materials producer propertyId id is null</li>
		 */
		public Builder setMaterialsProducerPropertyId(MaterialsProducerPropertyId materialsProducerPropertyId) {
			if (materialsProducerPropertyId == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_ID);
			}
			data.materialsProducerPropertyId = materialsProducerPropertyId;
			return this;
		}

		/**
		 * Sets the property definition
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION}
		 *             if the property definition is null</li>
		 */
		public Builder setPropertyDefinition(PropertyDefinition propertyDefinition) {
			if (propertyDefinition == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION);
			}
			data.propertyDefinition = propertyDefinition;
			return this;
		}

		/**
		 * Adds a property value
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID}
		 *             if the material producer id is null</li>
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_VALUE} if the
		 *             property value is null</li>
		 */

		public Builder addPropertyValue(MaterialsProducerId materialsProducerId, Object value) {
			if (materialsProducerId == null) {
				throw new ContractException(MaterialsError.NULL_MATERIALS_PRODUCER_ID);
			}
			if (value == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
			}

			data.propertyValues.add(new Pair<>(materialsProducerId, value));
			return this;
		}

	}

	/**
	 * Returns the (non-null) materials producer property id.
	 */
	public MaterialsProducerPropertyId getMaterialsProducerPropertyId() {
		return data.materialsProducerPropertyId;
	}

	/**
	 * Returns the (non-null) property definition.
	 */
	public PropertyDefinition getPropertyDefinition() {
		return data.propertyDefinition;
	}

	/**
	 * Returns the list of (MaterialsProducerId,value) pairs collected by the
	 * builder in the order of their addition. All pairs have non-null entries
	 * and the values are compatible with the contained property definition.
	 * Duplicate assignments of values to the same materials producer may be
	 * present.
	 */
	public List<Pair<MaterialsProducerId, Object>> getPropertyValues() {
		return Collections.unmodifiableList(data.propertyValues);
	}

}
