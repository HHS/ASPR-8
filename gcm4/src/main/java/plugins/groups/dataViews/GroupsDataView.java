package plugins.groups.dataViews;

import java.util.List;
import java.util.Set;

import nucleus.DataView;
import plugins.groups.datamanagers.GroupsDataManager;
import plugins.groups.support.GroupError;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupTypeId;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import util.errors.ContractException;

/**
 * Data view of the GroupsDataManager
 *
 */

public final class GroupsDataView implements DataView {

	private final GroupsDataManager groupsDataManager;

	/**
	 * Constructs this view from the corresponding data manager
	 * 
	 */
	public GroupsDataView(GroupsDataManager groupsDataManager) {
		this.groupsDataManager = groupsDataManager;
	}

	/**
	 * Returns the number of groups there are for a particular group type.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is unknown</li>
	 */
	public int getGroupCountForGroupType(final GroupTypeId groupTypeId) {

		return groupsDataManager.getGroupCountForGroupType(groupTypeId);
	}

	/**
	 * Returns the number of groups associated with the given person where each
	 * group has the given group type.
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is unknown</li>
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is unknown</li>
	 */
	public int getGroupCountForGroupTypeAndPerson(final GroupTypeId groupTypeId, final PersonId personId) {
		return groupsDataManager.getGroupCountForGroupTypeAndPerson(groupTypeId, personId);
	}

	/**
	 * Returns the number of groups associated with the given person. The person
	 * id must be non-null and non-negative.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is unknown</li>
	 */
	public int getGroupCountForPerson(final PersonId personId) {
		return groupsDataManager.getGroupCountForPerson(personId);
	}

	/**
	 * Returns the set of group ids as a list.
	 */
	public List<GroupId> getGroupIds() {
		return groupsDataManager.getGroupIds();
	}

	/**
	 * Returns the property definition for the given group type id and group
	 * property id
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is unknown</li>
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the group
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             group property id is unknown</li>
	 */
	public PropertyDefinition getGroupPropertyDefinition(GroupTypeId groupTypeId, GroupPropertyId groupPropertyId) {
		return groupsDataManager.getGroupPropertyDefinition(groupTypeId, groupPropertyId);
	}

	/**
	 * Returns true if and only if there is a property definition associated
	 * with the given group type id and group property id. Accepts all values.
	 */
	public boolean getGroupPropertyExists(final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId) {
		return groupsDataManager.getGroupPropertyExists(groupTypeId, groupPropertyId);
	}

	/**
	 * Returns the set of group property ids for the given group type id
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is unknown</li>
	 * 
	 */
	public <T extends GroupPropertyId> Set<T> getGroupPropertyIds(GroupTypeId groupTypeId) {
		return groupsDataManager.getGroupPropertyIds(groupTypeId);
	}

	/**
	 * Returns the value of the group property.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_ID} if the group id is
	 *             null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the group id
	 *             is unknown</li>
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the group
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             group property id is unknown</li>
	 * 
	 */
	public double getGroupPropertyTime(final GroupId groupId, GroupPropertyId groupPropertyId) {
		return groupsDataManager.getGroupPropertyTime(groupId, groupPropertyId);
	}

	/**
	 * Returns the value of the group property.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_ID} if the group id is
	 *             null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the group id
	 *             is unknown</li>
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the group
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             group property id is unknown</li>
	 * 
	 */

	public <T> T getGroupPropertyValue(final GroupId groupId, GroupPropertyId groupPropertyId) {
		return groupsDataManager.getGroupPropertyValue(groupId, groupPropertyId);
	}

	/**
	 * Returns the set groupIds associated with the given group type id as a
	 * list
	 *
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is unknown</li>
	 *
	 */
	public List<GroupId> getGroupsForGroupType(final GroupTypeId groupTypeId) {
		return groupsDataManager.getGroupsForGroupType(groupTypeId);
	}

	/**
	 * Returns the set of group ids associated with the given person and group
	 * type as a list.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is unknown</li>
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is unknown</li>
	 * 
	 */
	public List<GroupId> getGroupsForGroupTypeAndPerson(final GroupTypeId groupTypeId, final PersonId personId) {
		return getGroupsForGroupTypeAndPerson(groupTypeId, personId);
	}

	/**
	 * Returns the set group ids associated the the given person as a list.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is unknown</li>
	 * 
	 */
	public List<GroupId> getGroupsForPerson(final PersonId personId) {
		return groupsDataManager.getGroupsForPerson(personId);
	}

	/**
	 * Returns the group type for the given group.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_ID} if the group id is
	 *             null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the group id
	 *             is unknown</li>
	 */
	public <T extends GroupTypeId> T getGroupType(final GroupId groupId) {
		return groupsDataManager.getGroupType(groupId);
	}

	/**
	 * Return the number of group types associated with the person via their
	 * group memberships.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is unknown</li>
	 */
	public int getGroupTypeCountForPersonId(final PersonId personId) {
		return groupsDataManager.getGroupTypeCountForPersonId(personId);
	}

	/**
	 * Returns the group type ids
	 */
	public <T extends GroupTypeId> Set<T> getGroupTypeIds() {
		return groupsDataManager.getGroupTypeIds();
	}

	/**
	 * Returns the set group types associated with the person's groups as a
	 * list.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is unknown</li>
	 */
	public <T extends GroupTypeId> List<T> getGroupTypesForPerson(final PersonId personId) {
		return groupsDataManager.getGroupTypesForPerson(personId);
	}

	/**
	 * Returns the set of people who are in the given group as a list.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_ID} if the group id is
	 *             null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the group id
	 *             is unknown</li>
	 */
	public List<PersonId> getPeopleForGroup(final GroupId groupId) {
		return groupsDataManager.getPeopleForGroup(groupId);
	}

	/**
	 * Returns a list of unique person ids for the given group type(i.e. all
	 * people in groups having that type). Group type id must be valid.
	 * 
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is unknown</li>
	 */
	public List<PersonId> getPeopleForGroupType(final GroupTypeId groupTypeId) {
		return groupsDataManager.getPeopleForGroupType(groupTypeId);
	}

	/**
	 * Returns the number of people in the given group.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_ID} if the group id is
	 *             null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the group id
	 *             is unknown</li>
	 */
	public int getPersonCountForGroup(final GroupId groupId) {
		return groupsDataManager.getPersonCountForGroup(groupId);
	}

	/**
	 * Returns the number of people who are associated with groups having the
	 * given group type.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is unknown</li>
	 */
	public int getPersonCountForGroupType(final GroupTypeId groupTypeId) {
		return groupsDataManager.getPersonCountForGroupType(groupTypeId);
	}

	/**
	 * Returns true if and only if the group exists. Null tolerant.
	 */
	public boolean groupExists(final GroupId groupId) {
		return groupsDataManager.groupExists(groupId);
	}

	/**
	 * Returns true if and only if the group type exists. Null tolerant.
	 */
	public boolean groupTypeIdExists(final GroupTypeId groupTypeId) {
		return groupsDataManager.groupTypeIdExists(groupTypeId);
	}

	/**
	 * Returns true if and only if the person is in the group.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_ID} if the group id is
	 *             null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the group id
	 *             is unknown</li>
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is unknown</li>
	 */
	public boolean isPersonInGroup(final PersonId personId, final GroupId groupId) {
		return groupsDataManager.isPersonInGroup(personId, groupId);
	}
}
