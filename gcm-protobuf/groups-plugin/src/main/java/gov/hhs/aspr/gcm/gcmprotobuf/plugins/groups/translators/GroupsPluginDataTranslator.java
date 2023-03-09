package gov.hhs.aspr.gcm.gcmprotobuf.plugins.groups.translators;

import java.util.List;
import java.util.Set;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.gcmprotobuf.core.AbstractTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.plugins.groups.simobjects.Group;
import gov.hhs.aspr.gcm.gcmprotobuf.properties.simobjects.PropertyDefinitionMap;
import gov.hhs.aspr.gcm.gcmprotobuf.properties.simobjects.PropertyValueMap;
import plugins.groups.GroupsPluginData;
import plugins.groups.input.GroupIdInput;
import plugins.groups.input.GroupInput;
import plugins.groups.input.GroupMembershipInput;
import plugins.groups.input.GroupPropertyDefinitionMapInput;
import plugins.groups.input.GroupPropertyValueMapInput;
import plugins.groups.input.GroupTypeIdInput;
import plugins.groups.input.GroupsPluginDataInput;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupPropertyValue;
import plugins.groups.support.GroupTypeId;
import plugins.people.input.PersonIdInput;
import plugins.people.support.PersonId;
import plugins.properties.input.PropertyDefinitionInput;
import plugins.properties.input.PropertyDefinitionMapInput;
import plugins.properties.input.PropertyValueMapInput;
import plugins.util.properties.PropertyDefinition;

public class GroupsPluginDataTranslator extends AbstractTranslator<GroupsPluginDataInput, GroupsPluginData> {

    @Override
    protected GroupsPluginData convertInputObject(GroupsPluginDataInput inputObject) {
        GroupsPluginData.Builder builder = GroupsPluginData.builder();

        // Add groups
        for (GroupInput groupInput : inputObject.getGroupsList()) {
            GroupTypeId groupTypeId = this.translator.convertInputObject(groupInput.getGroupTypeId());
            GroupId groupId = this.translator.convertInputObject(groupInput.getGroupId());

            builder.addGroup(groupId, groupTypeId);
        }

        // Add group type ids
        for (GroupTypeIdInput groupTypeIdInput : inputObject.getGroupTypeIdsList()) {
            GroupTypeId groupTypeId = this.translator.convertInputObject(groupTypeIdInput);
            builder.addGroupTypeId(groupTypeId);
        }

        // Add group type property definitions
        for (GroupPropertyDefinitionMapInput groupPropertyDefinitionMapInput : inputObject
                .getGroupPropertyDefinitionsList()) {
            GroupTypeId groupTypeId = this.translator
                    .convertInputObject(groupPropertyDefinitionMapInput.getGroupTypeId());
            for (PropertyDefinitionMapInput propertyDefinitionMapInput : groupPropertyDefinitionMapInput
                    .getPropertyDefinitionsList()) {
                PropertyDefinitionMap propertyDefinitionMap = this.translator
                        .convertInputObject(propertyDefinitionMapInput);

                GroupPropertyId groupPropertyId = (GroupPropertyId) propertyDefinitionMap.getPropertyId();
                PropertyDefinition propertyDefinition = propertyDefinitionMap.getPropertyDefinition();
                builder.defineGroupProperty(groupTypeId, groupPropertyId, propertyDefinition);
            }
        }

        // add group property values
        for (GroupPropertyValueMapInput groupPropertyValueMapInput : inputObject.getGroupPropertyValuesList()) {
            GroupId groupId = this.translator.convertInputObject(groupPropertyValueMapInput.getGroupId());
            for (PropertyValueMapInput propertyValueMapInput : groupPropertyValueMapInput.getPropertyValueMapList()) {
                PropertyValueMap propertyValueMap = this.translator.convertInputObject(propertyValueMapInput);

                GroupPropertyId groupPropertyId = (GroupPropertyId) propertyValueMap.getPropertyId();
                Object propertyValue = propertyValueMap.getPropertyValue();

                builder.setGroupPropertyValue(groupId, groupPropertyId, propertyValue);
            }

        }

        // add people to groups
        for (GroupMembershipInput groupMembershipInput : inputObject.getGroupMembershipsList()) {
            PersonId personId = this.translator.convertInputObject(groupMembershipInput.getPersonId());

            for (GroupIdInput groupIdInput : groupMembershipInput.getGroupIdsList()) {
                GroupId groupId = this.translator.convertInputObject(groupIdInput);
                builder.addPersonToGroup(groupId, personId);
            }
        }

        return builder.build();
    }

    @Override
    protected GroupsPluginDataInput convertSimObject(GroupsPluginData simObject) {
        GroupsPluginDataInput.Builder builder = GroupsPluginDataInput.newBuilder();

        // add group type ids
        for (GroupTypeId groupTypeId : simObject.getGroupTypeIds()) {
            GroupTypeIdInput groupTypeIdInput = this.translator.convertSimObject(groupTypeId, GroupTypeId.class);
            builder.addGroupTypeIds(groupTypeIdInput);
        }

        // add groups
        for (GroupId groupId : simObject.getGroupIds()) {
            Group group = new Group();

            group.setGroupId(groupId);
            group.setGroupTypeId(simObject.getGroupTypeId(groupId));

            builder.addGroups((GroupInput) this.translator.convertSimObject(group));
        }

        // add group type property definitions

        for (GroupTypeId groupTypeId : simObject.getGroupTypeIds()) {
            GroupPropertyDefinitionMapInput.Builder groupPropDefMapInputBuilder = GroupPropertyDefinitionMapInput
                    .newBuilder();

            groupPropDefMapInputBuilder
                    .setGroupTypeId((GroupTypeIdInput) this.translator.convertSimObject(groupTypeId, GroupTypeId.class));

            Set<GroupPropertyId> groupPropertyIds = simObject.getGroupPropertyIds(groupTypeId);

            for (GroupPropertyId groupPropertyId : groupPropertyIds) {
                PropertyDefinition propertyDefinition = simObject.getGroupPropertyDefinition(groupTypeId,
                        groupPropertyId);

                PropertyDefinitionMapInput propertyDefInput = PropertyDefinitionMapInput.newBuilder()
                        .setPropertyDefinition(
                                (PropertyDefinitionInput) this.translator.convertSimObject(propertyDefinition))
                        .setPropertyId(this.translator.getAnyFromObject(groupPropertyId, GroupPropertyId.class))
                        .build();

                groupPropDefMapInputBuilder.addPropertyDefinitions(propertyDefInput);
            }

            builder.addGroupPropertyDefinitions(groupPropDefMapInputBuilder.build());
        }
        // add group property values
        for (GroupId groupId : simObject.getGroupIds()) {
            GroupPropertyValueMapInput.Builder groupPropValMapBuilder = GroupPropertyValueMapInput.newBuilder();

            groupPropValMapBuilder.setGroupId((GroupIdInput) this.translator.convertSimObject(groupId));

            List<GroupPropertyValue> groupPropertyValues = simObject.getGroupPropertyValues(groupId);

            for (GroupPropertyValue groupPropertyValue : groupPropertyValues) {
                GroupPropertyId groupPropertyId = groupPropertyValue.groupPropertyId();
                Object propertyValue = groupPropertyValue.value();

                PropertyValueMap propertyValueMap = new PropertyValueMap();
                propertyValueMap.setPropertyId(groupPropertyId);
                propertyValueMap.setPropertyValue(propertyValue);

                groupPropValMapBuilder.addPropertyValueMap(
                        (PropertyValueMapInput) this.translator.convertSimObject(propertyValueMap));
            }

            builder.addGroupPropertyValues(groupPropValMapBuilder.build());
        }

        // add people
        for (int i = 0; i < simObject.getGroupMemberships().size(); i++) {
            List<GroupId> groupIds = simObject.getGroupMemberships().get(i);

            if (groupIds != null) {
                GroupMembershipInput.Builder groupMembershipBuilder = GroupMembershipInput.newBuilder();

                PersonIdInput personIdInput = this.translator.convertSimObject(new PersonId(i));
                groupMembershipBuilder.setPersonId(personIdInput);

                for (GroupId groupId : groupIds) {
                    groupMembershipBuilder.addGroupIds((GroupIdInput) this.translator.convertSimObject(groupId));
                }
                builder.addGroupMemberships(groupMembershipBuilder.build());
            }

        }

        return builder.build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return GroupsPluginDataInput.getDescriptor();
    }

    @Override
    public GroupsPluginDataInput getDefaultInstanceForInputObject() {
        return GroupsPluginDataInput.getDefaultInstance();
    }

    @Override
    public Class<GroupsPluginData> getSimObjectClass() {
        return GroupsPluginData.class;
    }

    @Override
    public Class<GroupsPluginDataInput> getInputObjectClass() {
        return GroupsPluginDataInput.class;
    }

}
