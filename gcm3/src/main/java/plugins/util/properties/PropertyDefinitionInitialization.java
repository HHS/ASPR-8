package plugins.util.properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import net.jcip.annotations.Immutable;
import util.errors.ContractException;

/**
 * A generic based class for defining a property with an associated property id
 * and property values for extant owners of the property.
 * 
 * 
 * @author Shawn Hatch
 *
 * @param <T>
 *            The type of the property id
 * @param <K>
 *            The type of the owners of the property
 */
@Immutable
public final class PropertyDefinitionInitialization<T, K> {

	private static class Data<N, P> {
		N propertyId;
		PropertyDefinition propertyDefinition;
		List<Pair<P, Object>> propertyValues = new ArrayList<>();
	}

	private final Data<T, K> data;

	private PropertyDefinitionInitialization(Data<T, K> data) {
		this.data = data;
	}

	/**
	 * Builder class for a PropertyDefinitionInitialization
	 * 
	 * @author Shawn Hatch
	 *
	 * @param <N>
	 *            The type of the property id
	 * @param <P>The
	 *            type of the owners of the property
	 */
	public final static class Builder<N, P> {

		private Data<N, P> data = new Data<>();

		private void validate() {
			if (data.propertyDefinition == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION);
			}

			if (data.propertyId == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_ID);
			}

			Class<?> type = data.propertyDefinition.getType();
			for (Pair<P, Object> pair : data.propertyValues) {
				Object value = pair.getSecond();
				if (!type.isAssignableFrom(value.getClass())) {
					String message = "Definition Type " + type.getName() + " is not compatible with value = " + value;
					throw new ContractException(PropertyError.INCOMPATIBLE_VALUE, message);
				}
			}
		}

		/**
		 * Constructs the PropertyDefinitionInitialization from the collected
		 * data
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION}
		 *             if no property definition was assigned to the
		 *             builder</li>
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if no
		 *             property id was assigned to the builder</li>
		 *             <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE} if a
		 *             collected property value is incompatible with the
		 *             property definition</li>
		 */
		public PropertyDefinitionInitialization<N, P> build() {
			try {
				validate();
				return new PropertyDefinitionInitialization<>(data);
			} finally {
				data = new Data<>();
			}
		}

		/**
		 * Sets the property id
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
		 *             property id is null</li>
		 */
		public Builder<N, P> setPropertyId(N propertyId) {
			if (propertyId == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_ID);
			}
			data.propertyId = propertyId;
			return this;
		}

		/**
		 * Sets the property definition
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION}
		 *             if the property definition is null</li>
		 */
		public Builder<N, P> setPropertyDefinition(PropertyDefinition propertyDefinition) {
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
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_OWNER} if the
		 *             property owner is null</li>
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_VALUE} if the
		 *             property value is null</li>
		 */

		public Builder<N, P> addPropertyValue(P propertyOwner, Object value) {
			if (propertyOwner == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_OWNER);
			}
			if (value == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
			}

			data.propertyValues.add(new Pair<>(propertyOwner, value));
			return this;
		}

	}

	/**
	 * Returns the (non-null)property id.
	 */
	public T getPropertyId() {
		return data.propertyId;
	}

	/**
	 * Returns the (non-null)property definition.
	 */
	public PropertyDefinition getPropertyDefinition() {
		return data.propertyDefinition;
	}

	/**
	 * Returns the list of (owner,value) pairs collected by the builder in the
	 * order of their addition. All pairs have non-null entries and the values
	 * are compatible with the contained property definition. Duplicate
	 * assignments of values to the same owner may be present.
	 */
	public List<Pair<K, Object>> getPropertyValues() {
		return Collections.unmodifiableList(data.propertyValues);
	}

}
