package plugins.materials.events;

import java.util.ArrayList;
import java.util.List;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.MaterialsError;
import plugins.materials.support.MaterialsProducerId;
import plugins.regions.events.RegionAdditionEvent;
import util.errors.ContractException;

/**
 * An event indicating that a materials producer has been added
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public class MaterialsProducerAdditionEvent implements Event {

	private static class Data {
		private MaterialsProducerId materialsProducerId;
		private List<Object> values = new ArrayList<>();
	}

	/**
	 * Returns a new Builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

	private final Data data;

	/**
	 * Builder class for {@link RegionAdditionEvent}
	 * 
	 * @author Shawn Hatch
	 *
	 */
	public static class Builder {

		private Data data = new Data();

		private Builder(){}
		
		private void validate() {
			if (data.materialsProducerId == null) {
				throw new ContractException(MaterialsError.NULL_MATERIALS_PRODUCER_ID);
			}
		}

		/**
		 * Builds the Region addition event from the inputs
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID} if the materials producer
		 *             id was not set</li>
		 */
		public MaterialsProducerAdditionEvent build() {
			try {
				validate();
				return new MaterialsProducerAdditionEvent(data);
			} finally {
				data = new Data();
			}
		}

		/**
		 * Sets the materials producer id
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID} if the materials producer
		 *             id is null</li>
		 */
		public Builder setMaterialsProducerId(MaterialsProducerId materialsProducerId) {
			if (materialsProducerId == null) {
				throw new ContractException(MaterialsError.NULL_MATERIALS_PRODUCER_ID);
			}
			data.materialsProducerId = materialsProducerId;
			return this;
		}

		/**
		 * Adds an auxiliary value to be used by observers of materials producer addition
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain MaterialsError#NULL_AUXILIARY_DATA} if the
		 *             value is null</li>
		 */
		public Builder addValue(Object value) {
			if (value == null) {
				throw new ContractException(MaterialsError.NULL_AUXILIARY_DATA);
			}
			data.values.add(value);
			return this;
		}
	}

	private MaterialsProducerAdditionEvent(Data data) {
		this.data = data;
	}

	/**
	 * Returns the region id.
	 */
	public MaterialsProducerId getMaterialsProducerId() {
		return data.materialsProducerId;
	}

	/**
	 * Returns the (non-null) auxiliary objects that are instances of the given
	 * class in the order of their addition to the builder.
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getValues(Class<T> c) {
		List<T> result = new ArrayList<>();
		for (Object value : data.values) {
			if (c.isAssignableFrom(value.getClass())) {
				result.add((T) value);
			}
		}
		return result;
	}


}
