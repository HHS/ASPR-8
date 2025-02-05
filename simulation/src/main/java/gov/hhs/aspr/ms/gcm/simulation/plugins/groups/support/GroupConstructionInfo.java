package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.NotThreadSafe;

/**
 * Represents the information to add a group, but not its relationship to
 * people.
 */
@Immutable
public final class GroupConstructionInfo {
	private final Data data;

	private static class Data {
		private GroupTypeId groupTypeId;
		private Map<GroupPropertyId, Object> propertyValues = new LinkedHashMap<>();
		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			groupTypeId = data.groupTypeId;
			propertyValues.putAll(data.propertyValues);
			locked = data.locked;
		}
	}

	/**
	 * Returns the group Type of the group
	 */
	@SuppressWarnings("unchecked")
	public <T extends GroupTypeId> T getGroupTypeId() {
		return (T) data.groupTypeId;
	}

	/**
	 * Returns a map of the group property values for the group
	 */
	public Map<GroupPropertyId, Object> getPropertyValues() {
		return Collections.unmodifiableMap(data.propertyValues);
	}

	/*
	 * Hidden constructor
	 */
	private GroupConstructionInfo(Data data) {
		this.data = data;
	}

	public static Builder builder() {
		return new Builder(new Data());
	}

	@NotThreadSafe
	public static class Builder {
		private Data data;

		private Builder(Data data) {
			this.data = data;
		}

		private void validate() {
			if (data.groupTypeId == null) {
				throw new ContractException(GroupError.NULL_GROUP_TYPE_ID);
			}
		}

		/**
		 * Builds the {@link GroupConstructionInfo} from the collected data
		 * 
		 * @throws ContractException {@linkplain GroupError#NULL_GROUP_TYPE_ID} if no
		 *                           group type id was collected
		 */
		public GroupConstructionInfo build() {
			if (!data.locked) {
				validate();
			}
			ensureImmutability();
			return new GroupConstructionInfo(data);
		}

		/**
		 * Sets the group type id
		 * 
		 * @throws ContractException if the group type id is null
		 */
		public Builder setGroupTypeId(GroupTypeId groupTypeId) {
			ensureDataMutability();
			if (groupTypeId == null) {
				throw new ContractException(GroupError.NULL_GROUP_TYPE_ID);
			}
			data.groupTypeId = groupTypeId;
			return this;
		}

		/**
		 * Sets the group property value.
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
		 *                           the group property id is null</li>
		 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_VALUE}
		 *                           if the group property value is null</li>
		 *                           </ul>
		 */
		public Builder setGroupPropertyValue(GroupPropertyId groupPropertyId, Object groupPropertyValue) {
			ensureDataMutability();
			if (groupPropertyId == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_ID);
			}
			if (groupPropertyValue == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
			}
			data.propertyValues.put(groupPropertyId, groupPropertyValue);
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

	}

	/**
	 * Returns a new builder instance that is pre-filled with the current state of
	 * this instance.
	 */
	public Builder toBuilder() {
		return new Builder(data);
	}

}
