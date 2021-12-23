package plugins.groups.support;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.NotThreadSafe;
import util.ContractException;

/**
 * Represents the information to add a group, but not its relationship to
 * people.
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public final class GroupConstructionInfo {
	private final Scaffold scaffold;

	private static class Scaffold {
		private GroupTypeId groupTypeId;
		private Map<GroupPropertyId, Object> propertyValues = new LinkedHashMap<>();
	}

	/**
	 * Returns the group Type of the group
	 */
	@SuppressWarnings("unchecked")
	public <T extends GroupTypeId> T getGroupTypeId() {
		return (T) scaffold.groupTypeId;
	}

	/**
	 * Returns a map of the group property values for the group
	 */
	public Map<GroupPropertyId, Object> getPropertyValues() {
		return Collections.unmodifiableMap(scaffold.propertyValues);
	}

	/*
	 * Hidden constructor
	 */
	private GroupConstructionInfo(Scaffold scaffold) {
		this.scaffold = scaffold;
	}

	public static Builder builder() {
		return new Builder();
	}

	@NotThreadSafe
	public static class Builder {

		private Builder() {
		}

		private Scaffold scaffold = new Scaffold();

		private void validate() {
			if (scaffold.groupTypeId == null) {
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
			try {
				validate();
				return new GroupConstructionInfo(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
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
			scaffold.groupTypeId = groupTypeId;
			return this;
		}

		/**
		 * Sets the group property value.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain GroupError#NULL_GROUP_PROPERTY_ID} if the group property id is null</li>
		 *             <li>{@linkplain GroupError#NULL_GROUP_PROPERTY_VALUE} if the group property value is null</li>
		 */
		public Builder setGroupPropertyValue(GroupPropertyId groupPropertyId, Object groupPropertyValue) {
			if (groupPropertyId == null) {
				throw new ContractException(GroupError.NULL_GROUP_PROPERTY_ID);
			}
			if (groupPropertyValue == null) {
				throw new ContractException(GroupError.NULL_GROUP_PROPERTY_VALUE);
			}
			scaffold.propertyValues.put(groupPropertyId, groupPropertyValue);
			return this;
		}

	}

}
