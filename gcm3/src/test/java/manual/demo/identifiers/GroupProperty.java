package manual.demo.identifiers;

import plugins.groups.support.GroupPropertyId;
import plugins.properties.support.PropertyDefinition;

public enum GroupProperty implements GroupPropertyId {

	SHARED_IMMUNITY(GroupType.HOME, PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(Boolean.FALSE).build());

	private final GroupType groupType;
	private final PropertyDefinition propertyDefinition;

	private GroupProperty(GroupType groupType, PropertyDefinition propertyDefinition) {

		this.groupType = groupType;
		this.propertyDefinition = propertyDefinition;
	}

	public GroupType getGroupType() {
		return groupType;
	}

	public PropertyDefinition getPropertyDefinition() {
		return propertyDefinition;
	}

}
