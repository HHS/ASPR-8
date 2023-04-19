package plugins.groups.support;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.NotThreadSafe;
import plugins.util.properties.PropertyError;
import util.errors.ContractException;

/**
 * Represents the information to add a group, but not its relationship to
 * people.
 * 
 *
 */
@Immutable
public final class GroupConstructionInfo {
	private final Data data;

	private static class Data {
		private GroupTypeId groupTypeId;
		private Map<GroupPropertyId, Object> propertyValues = new LinkedHashMap<>();

		public Data() {
		}

		public Data(Data data) {
			groupTypeId = data.groupTypeId;
			propertyValues.putAll(data.propertyValues);
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
		return new Builder();
	}

	@NotThreadSafe
	public static class Builder {

		private Builder() {
		}

		private Data data = new Data();

		private void validate() {
			if (data.groupTypeId == null) {
				throw new ContractException(GroupError.NULL_GROUP_TYPE_ID);
			}
		}

		/**
		 * Builds the {@link GroupConstructionInfo} from the collected data
		 * 
		 * @throws ContractException;
		 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if no
		 *             group type id was collected</li>
		 */
		public GroupConstructionInfo build() {
			validate();
			return new GroupConstructionInfo(new Data(data));
		}

		/**
		 * Sets the group type id
		 * 
		 * @throws ContractException
		 *             if the group type id is null
		 */
		public Builder setGroupTypeId(GroupTypeId groupTypeId) {
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
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
		 *             group property id is null</li>
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_VALUE} if the
		 *             group property value is null</li>
		 */
		public Builder setGroupPropertyValue(GroupPropertyId groupPropertyId, Object groupPropertyValue) {
			if (groupPropertyId == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_ID);
			}
			if (groupPropertyValue == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
			}
			data.propertyValues.put(groupPropertyId, groupPropertyValue);
			return this;
		}

	}

}
