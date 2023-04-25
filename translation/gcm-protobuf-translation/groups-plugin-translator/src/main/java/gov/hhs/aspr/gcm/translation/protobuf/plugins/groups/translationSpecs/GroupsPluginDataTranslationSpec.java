package gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translationSpecs;

import java.util.List;
import java.util.Set;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.input.GroupIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.input.GroupInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.input.GroupMembershipInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.input.GroupPropertyDefinitionMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.input.GroupPropertyValueMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.input.GroupTypeIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.input.GroupsPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.input.PersonIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyDefinitionInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyDefinitionMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyValueMapInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.groups.GroupsPluginData;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupPropertyValue;
import plugins.groups.support.GroupTypeId;
import plugins.people.support.PersonId;
import plugins.util.properties.PropertyDefinition;

public class GroupsPluginDataTranslationSpec extends ProtobufTranslationSpec<GroupsPluginDataInput, GroupsPluginData> {

    @Override
    protected GroupsPluginData convertInputObject(GroupsPluginDataInput inputObject) {
        GroupsPluginData.Builder builder = GroupsPluginData.builder();

        // Add groups
        for (GroupInput groupInput : inputObject.getGroupsList()) {
            GroupTypeId groupTypeId = this.translationEnine.convertObject(groupInput.getGroupTypeId());
            GroupId groupId = this.translationEnine.convertObject(groupInput.getGroupId());

            builder.addGroup(groupId, groupTypeId);
        }

        // Add group type ids
        for (GroupTypeIdInput groupTypeIdInput : inputObject.getGroupTypeIdsList()) {
            GroupTypeId groupTypeId = this.translationEnine.convertObject(groupTypeIdInput);
            builder.addGroupTypeId(groupTypeId);
        }

        // Add group type property definitions
        for (GroupPropertyDefinitionMapInput groupPropertyDefinitionMapInput : inputObject
                .getGroupPropertyDefinitionsList()) {
            GroupTypeId groupTypeId = this.translationEnine
                    .convertObject(groupPropertyDefinitionMapInput.getGroupTypeId());
            for (PropertyDefinitionMapInput propertyDefinitionMapInput : groupPropertyDefinitionMapInput
                    .getPropertyDefinitionsList()) {

                GroupPropertyId groupPropertyId = this.translationEnine
                        .convertObject(propertyDefinitionMapInput.getPropertyId());
                PropertyDefinition propertyDefinition = this.translationEnine
                        .convertObject(propertyDefinitionMapInput.getPropertyDefinition());
                builder.defineGroupProperty(groupTypeId, groupPropertyId, propertyDefinition);
            }
        }

        // add group property values
        for (GroupPropertyValueMapInput groupPropertyValueMapInput : inputObject.getGroupPropertyValuesList()) {
            GroupId groupId = this.translationEnine.convertObject(groupPropertyValueMapInput.getGroupId());
            for (PropertyValueMapInput propertyValueMapInput : groupPropertyValueMapInput.getPropertyValueMapList()) {

                GroupPropertyId groupPropertyId = this.translationEnine
                        .getObjectFromAny(propertyValueMapInput.getPropertyId());
                Object propertyValue = this.translationEnine.getObjectFromAny(propertyValueMapInput.getPropertyValue());

                builder.setGroupPropertyValue(groupId, groupPropertyId, propertyValue);
            }

        }

        // add people to groups
        for (GroupMembershipInput groupMembershipInput : inputObject.getGroupMembershipsList()) {
            PersonId personId = this.translationEnine.convertObject(groupMembershipInput.getPersonId());

            for (GroupIdInput groupIdInput : groupMembershipInput.getGroupIdsList()) {
                GroupId groupId = this.translationEnine.convertObject(groupIdInput);
                builder.addPersonToGroup(groupId, personId);
            }
        }

        return builder.build();
    }

    @Override
    protected GroupsPluginDataInput convertAppObject(GroupsPluginData appObject) {
        GroupsPluginDataInput.Builder builder = GroupsPluginDataInput.newBuilder();

        // add group type ids
        for (GroupTypeId groupTypeId : appObject.getGroupTypeIds()) {
            GroupTypeIdInput groupTypeIdInput = this.translationEnine.convertObjectAsSafeClass(groupTypeId,
                    GroupTypeId.class);
            builder.addGroupTypeIds(groupTypeIdInput);
        }

        // add groups
        for (GroupId groupId : appObject.getGroupIds()) {

            GroupIdInput groupIdInput = this.translationEnine.convertObject(groupId);
            GroupTypeIdInput groupTypeIdInput = this.translationEnine.convertObjectAsSafeClass(
                    appObject.getGroupTypeId(groupId),
                    GroupTypeId.class);

            GroupInput groupInput = GroupInput.newBuilder()
                    .setGroupId(groupIdInput)
                    .setGroupTypeId(groupTypeIdInput)
                    .build();

            builder.addGroups(groupInput);
        }

        // add group type property definitions

        for (GroupTypeId groupTypeId : appObject.getGroupTypeIds()) {
            GroupPropertyDefinitionMapInput.Builder groupPropDefMapInputBuilder = GroupPropertyDefinitionMapInput
                    .newBuilder();

            GroupTypeIdInput groupTypeIdInput = this.translationEnine.convertObjectAsSafeClass(groupTypeId,
                    GroupTypeId.class);
            groupPropDefMapInputBuilder.setGroupTypeId(groupTypeIdInput);

            Set<GroupPropertyId> groupPropertyIds = appObject.getGroupPropertyIds(groupTypeId);

            for (GroupPropertyId groupPropertyId : groupPropertyIds) {
                PropertyDefinition propertyDefinition = appObject.getGroupPropertyDefinition(groupTypeId,
                        groupPropertyId);

                PropertyDefinitionInput propertyDefinitionInput = this.translationEnine.convertObject(propertyDefinition);

                PropertyDefinitionMapInput propertyDefInput = PropertyDefinitionMapInput.newBuilder()
                        .setPropertyDefinition(propertyDefinitionInput)
                        .setPropertyId(this.translationEnine.getAnyFromObject(groupPropertyId))
                        .build();

                groupPropDefMapInputBuilder.addPropertyDefinitions(propertyDefInput);
            }

            builder.addGroupPropertyDefinitions(groupPropDefMapInputBuilder.build());
        }
        // add group property values
        for (GroupId groupId : appObject.getGroupIds()) {
            GroupPropertyValueMapInput.Builder groupPropValMapBuilder = GroupPropertyValueMapInput.newBuilder();

            GroupIdInput groupIdInput = this.translationEnine.convertObject(groupId);
            groupPropValMapBuilder.setGroupId(groupIdInput);

            List<GroupPropertyValue> groupPropertyValues = appObject.getGroupPropertyValues(groupId);

            for (GroupPropertyValue groupPropertyValue : groupPropertyValues) {
                Object propertyValue = groupPropertyValue.value();

                PropertyValueMapInput propertyValueMapInput = PropertyValueMapInput.newBuilder()
                        .setPropertyValue(this.translationEnine.getAnyFromObject(propertyValue))
                        .setPropertyId(this.translationEnine.getAnyFromObject(groupPropertyValue.groupPropertyId()))
                        .build();

                groupPropValMapBuilder.addPropertyValueMap(propertyValueMapInput);
            }

            builder.addGroupPropertyValues(groupPropValMapBuilder.build());
        }

        // add people
        for (int i = 0; i < appObject.getPersonCount(); i++) {
            PersonId personId = new PersonId(i);
            List<GroupId> groupIds = appObject.getGroupsForPerson(personId);

            if (!groupIds.isEmpty()) {
                GroupMembershipInput.Builder groupMembershipBuilder = GroupMembershipInput.newBuilder();

                PersonIdInput personIdInput = this.translationEnine.convertObject(personId);
                groupMembershipBuilder.setPersonId(personIdInput);

                for (GroupId groupId : groupIds) {
                    groupMembershipBuilder.addGroupIds((GroupIdInput) this.translationEnine.convertObject(groupId));
                }
                builder.addGroupMemberships(groupMembershipBuilder.build());
            }

        }

        return builder.build();
    }

    @Override
    public Class<GroupsPluginData> getAppObjectClass() {
        return GroupsPluginData.class;
    }

    @Override
    public Class<GroupsPluginDataInput> getInputObjectClass() {
        return GroupsPluginDataInput.class;
    }

}