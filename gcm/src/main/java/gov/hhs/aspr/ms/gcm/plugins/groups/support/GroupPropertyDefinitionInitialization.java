package gov.hhs.aspr.ms.gcm.plugins.groups.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyError;
import net.jcip.annotations.Immutable;
import util.errors.ContractException;

/**
 * A class for defining a group property with an associated property id and
 * property values for extant groups.
 * 
 * 
 *
 * 
 */
@Immutable
public final class GroupPropertyDefinitionInitialization {

	private static class Data {
		GroupTypeId groupTypeId;
		GroupPropertyId groupPropertyId;
		PropertyDefinition propertyDefinition;
		List<Pair<GroupId, Object>> propertyValues = new ArrayList<>();

		public Data() {
		}

		public Data(Data data) {
			groupTypeId = data.groupTypeId;
			groupPropertyId = data.groupPropertyId;
			propertyDefinition = data.propertyDefinition;
			propertyValues.addAll(data.propertyValues);
		}
	}

	private final Data data;

	private GroupPropertyDefinitionInitialization(Data data) {
		this.data = data;
	}

	/**
	 * Returns a new builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder class for a GroupPropertyDefinitionInitialization
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

			if (data.groupPropertyId == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_ID);
			}

			if (data.groupTypeId == null) {
				throw new ContractException(GroupError.NULL_GROUP_TYPE_ID);
			}

			Class<?> type = data.propertyDefinition.getType();
			for (Pair<GroupId, Object> pair : data.propertyValues) {
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
		 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if no
		 *             group type id was assigned to the builder</li>
		 * 
		 */
		public GroupPropertyDefinitionInitialization build() {
			validate();
			return new GroupPropertyDefinitionInitialization(new Data(data));
		}

		/**
		 * Sets the group property id
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
		 *             property id is null</li>
		 */
		public Builder setPropertyId(GroupPropertyId groupPropertyId) {
			if (groupPropertyId == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_ID);
			}
			data.groupPropertyId = groupPropertyId;
			return this;
		}

		/**
		 * Sets the group type id
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the
		 *             group type id is null</li>
		 */
		public Builder setGroupTypeId(GroupTypeId groupTypeId) {
			if (groupTypeId == null) {
				throw new ContractException(GroupError.NULL_GROUP_TYPE_ID);
			}
			data.groupTypeId = groupTypeId;
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
		 *             <li>{@linkplain GroupError#NULL_GROUP_ID} if the group id
		 *             is null</li>
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_VALUE} if the
		 *             property value is null</li>
		 */

		public Builder addPropertyValue(GroupId groupId, Object value) {
			if (groupId == null) {
				throw new ContractException(GroupError.NULL_GROUP_ID);
			}

			if (value == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
			}

			data.propertyValues.add(new Pair<>(groupId, value));
			return this;
		}

	}

	/**
	 * Returns the (non-null)property id.
	 */
	public GroupPropertyId getPropertyId() {
		return data.groupPropertyId;
	}

	/**
	 * Returns the (non-null) group type id.
	 */
	public GroupTypeId getGroupTypeId() {
		return data.groupTypeId;
	}

	/**
	 * Returns the (non-null)property definition.
	 */
	public PropertyDefinition getPropertyDefinition() {
		return data.propertyDefinition;
	}

	/**
	 * Returns the list of (groupId,value) pairs collected by the builder in the
	 * order of their addition. All pairs have non-null entries and the values
	 * are compatible with the contained property definition. Duplicate
	 * assignments of values to the same group may be present.
	 */
	public List<Pair<GroupId, Object>> getPropertyValues() {
		return Collections.unmodifiableList(data.propertyValues);
	}

}
