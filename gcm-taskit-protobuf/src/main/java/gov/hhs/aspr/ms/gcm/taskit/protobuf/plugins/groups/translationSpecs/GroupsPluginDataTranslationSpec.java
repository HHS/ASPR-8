package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.translationSpecs;

import java.util.List;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.GroupIdInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.GroupInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.GroupPropertyDefinitionMapInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.GroupPropertyValueMapInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.GroupToPersonMembershipInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.GroupTypeIdInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.data.input.GroupsPluginDataInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.PersonToGroupMembershipInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionMapInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyValueMapInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import plugins.groups.datamanagers.GroupsPluginData;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupPropertyValue;
import plugins.groups.support.GroupTypeId;
import plugins.people.support.PersonId;
import plugins.util.properties.PropertyDefinition;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain GroupsPluginDataInput} and
 * {@linkplain GroupsPluginData}
 */
public class GroupsPluginDataTranslationSpec extends ProtobufTranslationSpec<GroupsPluginDataInput, GroupsPluginData> {

    @Override
    protected GroupsPluginData convertInputObject(GroupsPluginDataInput inputObject) {
        GroupsPluginData.Builder builder = GroupsPluginData.builder();

        // Add groups
        for (GroupInput groupInput : inputObject.getGroupsList()) {
            GroupTypeId groupTypeId = this.translationEngine.convertObject(groupInput.getGroupTypeId());
            GroupId groupId = this.translationEngine.convertObject(groupInput.getGroupId());

            builder.addGroup(groupId, groupTypeId);
        }

        // Add group type ids
        for (GroupTypeIdInput groupTypeIdInput : inputObject.getGroupTypeIdsList()) {
            GroupTypeId groupTypeId = this.translationEngine.convertObject(groupTypeIdInput);
            builder.addGroupTypeId(groupTypeId);
        }

        // Add group type property definitions
        for (GroupPropertyDefinitionMapInput groupPropertyDefinitionMapInput : inputObject
                .getGroupPropertyDefinitionsList()) {
            GroupTypeId groupTypeId = this.translationEngine
                    .convertObject(groupPropertyDefinitionMapInput.getGroupTypeId());
            for (PropertyDefinitionMapInput propertyDefinitionMapInput : groupPropertyDefinitionMapInput
                    .getPropertyDefinitionsList()) {

                GroupPropertyId groupPropertyId = this.translationEngine
                        .convertObject(propertyDefinitionMapInput.getPropertyId());
                PropertyDefinition propertyDefinition = this.translationEngine
                        .convertObject(propertyDefinitionMapInput.getPropertyDefinition());
                builder.defineGroupProperty(groupTypeId, groupPropertyId, propertyDefinition);
            }
        }

        // add group property values
        for (GroupPropertyValueMapInput groupPropertyValueMapInput : inputObject.getGroupPropertyValuesList()) {
            GroupId groupId = this.translationEngine.convertObject(groupPropertyValueMapInput.getGroupId());
            for (PropertyValueMapInput propertyValueMapInput : groupPropertyValueMapInput.getPropertyValueMapList()) {

                GroupPropertyId groupPropertyId = this.translationEngine
                        .getObjectFromAny(propertyValueMapInput.getPropertyId());
                Object propertyValue = this.translationEngine
                        .getObjectFromAny(propertyValueMapInput.getPropertyValue());

                builder.setGroupPropertyValue(groupId, groupPropertyId, propertyValue);
            }

        }

        for (PersonToGroupMembershipInput ptgMembership : inputObject.getPersonToGroupMembershipsList()) {
            PersonId personId = new PersonId(ptgMembership.getPersonId());

            for (int gId : ptgMembership.getGroupIdsList()) {
                builder.addGroupToPerson(new GroupId(gId), personId);
            }
        }

        for (GroupToPersonMembershipInput gtpMembership : inputObject.getGroupToPersonMembershipsList()) {
            GroupId groupId = new GroupId(gtpMembership.getGroupId());

            for (int pId : gtpMembership.getPersonIdsList()) {
                builder.addPersonToGroup(new PersonId(pId), groupId);
            }
        }

        builder.setNextGroupIdValue(inputObject.getNextGroupIdValue());

        return builder.build();
    }

    @Override
    protected GroupsPluginDataInput convertAppObject(GroupsPluginData appObject) {
        GroupsPluginDataInput.Builder builder = GroupsPluginDataInput.newBuilder();

        // add group type ids
        for (GroupTypeId groupTypeId : appObject.getGroupTypeIds()) {
            GroupTypeIdInput groupTypeIdInput = this.translationEngine.convertObjectAsSafeClass(groupTypeId,
                    GroupTypeId.class);
            builder.addGroupTypeIds(groupTypeIdInput);
        }

        // add groups
        for (GroupId groupId : appObject.getGroupIds()) {

            GroupIdInput groupIdInput = this.translationEngine.convertObject(groupId);
            GroupTypeIdInput groupTypeIdInput = this.translationEngine.convertObjectAsSafeClass(
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

            GroupTypeIdInput groupTypeIdInput = this.translationEngine.convertObjectAsSafeClass(groupTypeId,
                    GroupTypeId.class);
            groupPropDefMapInputBuilder.setGroupTypeId(groupTypeIdInput);

            Set<GroupPropertyId> groupPropertyIds = appObject.getGroupPropertyIds(groupTypeId);

            for (GroupPropertyId groupPropertyId : groupPropertyIds) {
                PropertyDefinition propertyDefinition = appObject.getGroupPropertyDefinition(groupTypeId,
                        groupPropertyId);

                PropertyDefinitionInput propertyDefinitionInput = this.translationEngine
                        .convertObject(propertyDefinition);

                PropertyDefinitionMapInput propertyDefInput = PropertyDefinitionMapInput.newBuilder()
                        .setPropertyDefinition(propertyDefinitionInput)
                        .setPropertyId(this.translationEngine.getAnyFromObject(groupPropertyId))
                        .build();

                groupPropDefMapInputBuilder.addPropertyDefinitions(propertyDefInput);
            }

            builder.addGroupPropertyDefinitions(groupPropDefMapInputBuilder.build());
        }
        // add group property values
        for (GroupId groupId : appObject.getGroupIds()) {
            GroupPropertyValueMapInput.Builder groupPropValMapBuilder = GroupPropertyValueMapInput.newBuilder();

            GroupIdInput groupIdInput = this.translationEngine.convertObject(groupId);
            groupPropValMapBuilder.setGroupId(groupIdInput);

            List<GroupPropertyValue> groupPropertyValues = appObject.getGroupPropertyValues(groupId);

            for (GroupPropertyValue groupPropertyValue : groupPropertyValues) {
                Object propertyValue = groupPropertyValue.value();

                PropertyValueMapInput propertyValueMapInput = PropertyValueMapInput.newBuilder()
                        .setPropertyValue(this.translationEngine.getAnyFromObject(propertyValue))
                        .setPropertyId(this.translationEngine.getAnyFromObject(groupPropertyValue.groupPropertyId()))
                        .build();

                groupPropValMapBuilder.addPropertyValueMap(propertyValueMapInput);
            }

            builder.addGroupPropertyValues(groupPropValMapBuilder.build());
        }

        // add people
        for (int i = 0; i < appObject.getPersonCount(); i++) {
            PersonId personId = new PersonId(i);
            List<GroupId> groupsForPerson = appObject.getGroupsForPerson(personId);

            if (!groupsForPerson.isEmpty()) {
                PersonToGroupMembershipInput.Builder groupMembershipBuilder = PersonToGroupMembershipInput.newBuilder();

                groupMembershipBuilder.setPersonId(i);

                for (GroupId groupId : groupsForPerson) {
                    groupMembershipBuilder.addGroupIds(groupId.getValue());
                }
                builder.addPersonToGroupMemberships(groupMembershipBuilder.build());
            }

        }

        for (int i = 0; i < appObject.getGroupCount(); i++) {
            GroupId groupId = new GroupId(i);
            List<PersonId> peopleInGroup = appObject.getPeopleForGroup(groupId);

            if (!peopleInGroup.isEmpty()) {
                GroupToPersonMembershipInput.Builder groupMembershipBuilder = GroupToPersonMembershipInput.newBuilder();

                groupMembershipBuilder.setGroupId(i);

                for (PersonId personId : peopleInGroup) {
                    groupMembershipBuilder.addPersonIds(personId.getValue());
                }
                builder.addGroupToPersonMemberships(groupMembershipBuilder.build());
            }
        }

        builder.setNextGroupIdValue(appObject.getNextGroupIdValue());

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
