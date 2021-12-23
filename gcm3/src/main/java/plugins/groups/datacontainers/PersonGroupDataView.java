package plugins.groups.datacontainers;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import nucleus.Context;
import nucleus.DataView;
import nucleus.NucleusError;
import plugins.groups.support.GroupError;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupSampler;
import plugins.groups.support.GroupTypeId;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.properties.support.PropertyDefinition;
import plugins.stochastics.datacontainers.StochasticsDataView;
import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.support.StochasticsError;
import util.ContractException;

/**
 * Published data view that provides person group information
 *
 * @author Shawn Hatch
 *
 */
public final class PersonGroupDataView implements DataView {
	private StochasticsDataView stochasticsDataView;
	private PersonDataView personDataView;
	private final PersonGroupDataManager personGroupDataManager;
	private Context context;

	/**
	 * Constructs the data view from the given context and data manager.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain NucleusError#NULL_CONTEXT} if the context is
	 *             null</li>
	 *             <li>{@linkplain GroupError#NULL_GROUP_DATA_MANAGER} if the
	 *             group data manager is null</li>
	 * 
	 */
	public PersonGroupDataView(Context context, PersonGroupDataManager personGroupDataManager) {
		if (context == null) {
			throw new ContractException(NucleusError.NULL_CONTEXT);
		}
		if (personGroupDataManager == null) {
			throw new ContractException(GroupError.NULL_GROUP_DATA_MANAGER);
		}
		this.context = context;
		this.personGroupDataManager = personGroupDataManager;
		personDataView = context.getDataView(PersonDataView.class).get();
		stochasticsDataView = context.getDataView(StochasticsDataView.class).get();
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
		validateGroupTypeId(groupTypeId);
		return personGroupDataManager.getGroupCountForGroupType(groupTypeId);
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
		validatePersonExists(personId);
		validateGroupTypeId(groupTypeId);
		return personGroupDataManager.getGroupCountForGroupTypeAndPerson(groupTypeId, personId);
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
		validatePersonExists(personId);
		return personGroupDataManager.getGroupCountForPerson(personId);
	}

	/**
	 * Returns the set of group ids as a list.
	 */

	public List<GroupId> getGroupIds() {
		return personGroupDataManager.getGroupIds();
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
		validateGroupTypeId(groupTypeId);
		return personGroupDataManager.getGroupsForGroupType(groupTypeId);
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
		validatePersonExists(personId);
		validateGroupTypeId(groupTypeId);
		return personGroupDataManager.getGroupsForGroupTypeAndPerson(groupTypeId, personId);
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
		validatePersonExists(personId);
		return personGroupDataManager.getGroupsForPerson(personId);
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
		validateGroupExists(groupId);
		return personGroupDataManager.getGroupType(groupId);
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
		validatePersonExists(personId);
		return personGroupDataManager.getGroupTypeCountForPersonId(personId);
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
		validatePersonExists(personId);
		return personGroupDataManager.getGroupTypesForPerson(personId);
	}

	/**
	 * Returns a contacted person.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_ID} if the group id is
	 *             null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the group id
	 *             is unknown</li>
	 *             <li>{@linkplain GroupError#NULL_GROUP_SAMPLER} if the group
	 *             sampler is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the
	 *             excluded person contained in the sampler is not null and is
	 *             unknown</li>
	 *             <li>{@linkplain StochasticsError#UNKNOWN_RANDOM_NUMBER_GENERATOR_ID}
	 *             if the sampler contains an unknown random number generator
	 *             id</li>
	 */
	public Optional<PersonId> sampleGroup(final GroupId groupId, final GroupSampler groupSampler) {
		validateGroupExists(groupId);
		validateGroupSampler(groupSampler);
		return personGroupDataManager.sampleGroup(groupId, groupSampler);
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
		validateGroupExists(groupId);
		return personGroupDataManager.getPeopleForGroup(groupId);
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
		validateGroupTypeId(groupTypeId);
		return personGroupDataManager.getPeopleForGroupType(groupTypeId);
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
		validateGroupExists(groupId);
		return personGroupDataManager.getPersonCountForGroup(groupId);
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
		validateGroupTypeId(groupTypeId);
		return personGroupDataManager.getPersonCountForGroupType(groupTypeId);
	}

	/**
	 * Returns true if and only if the group exists. Null tolerant.
	 */
	public boolean groupExists(final GroupId groupId) {
		return personGroupDataManager.groupExists(groupId);
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
	public boolean isGroupMember(final GroupId groupId, final PersonId personId) {
		validatePersonExists(personId);
		validateGroupExists(groupId);
		return personGroupDataManager.isGroupMember(groupId, personId);
	}

	/**
	 * Returns the group type ids
	 */
	public <T extends GroupTypeId> Set<T> getGroupTypeIds() {
		return personGroupDataManager.getGroupTypeIds();
	}

	/**
	 * Returns true if and only if the group type id exists. Null tolerant.
	 */
	public boolean groupTypeIdExists(GroupTypeId groupTypeId) {
		return personGroupDataManager.groupTypeIdExists(groupTypeId);
	}

	/**
	 * Returns the last group id added to the simulation
	 */
	public Optional<GroupId> getLastIssuedGroupId() {
		return personGroupDataManager.getLastIssuedGroupId();
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
	 *             <li>{@linkplain GroupError#NULL_GROUP_PROPERTY_ID} if the
	 *             group property id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_PROPERTY_ID} if the
	 *             group property id is unknown</li>
	 */
	public PropertyDefinition getGroupPropertyDefinition(GroupTypeId groupTypeId, GroupPropertyId groupPropertyId) {
		validateGroupTypeId(groupTypeId);
		validateGroupPropertyId(groupTypeId, groupPropertyId);
		return personGroupDataManager.getGroupPropertyDefinition(groupTypeId, groupPropertyId);
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
		validateGroupTypeId(groupTypeId);
		return personGroupDataManager.getGroupPropertyIds(groupTypeId);
	}

	/**
	 * Returns the value of the group property.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_ID} if the group id is
	 *             null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the group id
	 *             is unknown</li>
	 *             <li>{@linkplain GroupError#NULL_GROUP_PROPERTY_ID} if the
	 *             group property id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_PROPERTY_ID} if the
	 *             group property id is unknown</li>
	 * 
	 */

	public <T> T getGroupPropertyValue(final GroupId groupId, GroupPropertyId groupPropertyId) {
		validateGroupExists(groupId);
		final GroupTypeId groupTypeId = getGroupType(groupId);
		validateGroupPropertyId(groupTypeId, groupPropertyId);
		return personGroupDataManager.getGroupPropertyValue(groupId, groupPropertyId);
	}

	/**
	 * Returns true if and only if the group property is defined. Null tolerant.
	 */
	public boolean getGroupPropertyExists(final GroupTypeId groupTypeId, GroupPropertyId groupPropertyId) {
		return personGroupDataManager.getGroupPropertyExists(groupTypeId, groupPropertyId);
	}

	/**
	 * Returns the value of the group property. 
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_ID} if the group id is
	 *             null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_ID} if the group id
	 *             is unknown</li> 
	 *             <li>{@linkplain GroupError#NULL_GROUP_PROPERTY_ID} if the
	 *             group property id is null</li>
	 *             <li>{@linkplain GroupError#UNKNOWN_GROUP_PROPERTY_ID} if the
	 *             group property id is unknown</li>             
	 * 
	 */
	public double getGroupPropertyTime(final GroupId groupId, GroupPropertyId groupPropertyId) {
		validateGroupExists(groupId);
		final GroupTypeId groupTypeId = getGroupType(groupId);
		validateGroupPropertyId(groupTypeId, groupPropertyId);
		return personGroupDataManager.getGroupPropertyTime(groupId, groupPropertyId);
	}

	private void validateGroupExists(final GroupId groupId) {
		if (groupId == null) {
			context.throwContractException(GroupError.NULL_GROUP_ID);
		}
		if (!personGroupDataManager.groupExists(groupId)) {
			context.throwContractException(GroupError.UNKNOWN_GROUP_ID);
		}
	}

	private void validateGroupPropertyId(final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId) {
		if (groupPropertyId == null) {
			context.throwContractException(GroupError.NULL_GROUP_PROPERTY_ID);
		}

		if (!personGroupDataManager.getGroupPropertyExists(groupTypeId, groupPropertyId)) {
			context.throwContractException(GroupError.UNKNOWN_GROUP_PROPERTY_ID);
		}
	}

	/*
	 * Precondition : group sampler info is not null
	 */
	private void validateGroupSampler(final GroupSampler groupSampler) {
		if (groupSampler == null) {
			context.throwContractException(GroupError.NULL_GROUP_SAMPLER);
		}
		if (groupSampler.getExcludedPerson().isPresent()) {
			final PersonId excludedPersonId = groupSampler.getExcludedPerson().get();
			validatePersonExists(excludedPersonId);
		}
		if (groupSampler.getRandomNumberGeneratorId().isPresent()) {
			final RandomNumberGeneratorId randomNumberGeneratorId = groupSampler.getRandomNumberGeneratorId().get();
			validateRandomNumberGeneratorId(randomNumberGeneratorId);
		}
	}

	private void validateGroupTypeId(final GroupTypeId groupTypeId) {

		if (groupTypeId == null) {
			context.throwContractException(GroupError.NULL_GROUP_TYPE_ID);
		}

		if (!personGroupDataManager.groupTypeIdExists(groupTypeId)) {
			context.throwContractException(GroupError.UNKNOWN_GROUP_TYPE_ID, groupTypeId);
		}

	}

	private void validatePersonExists(final PersonId personId) {
		if (personId == null) {
			context.throwContractException(PersonError.NULL_PERSON_ID);
		}
		if (!personDataView.personExists(personId)) {
			context.throwContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	private void validateRandomNumberGeneratorId(final RandomNumberGeneratorId randomNumberGeneratorId) {
		if (randomNumberGeneratorId == null) {
			context.throwContractException(StochasticsError.NULL_RANDOM_NUMBER_GENERATOR_ID);
		}
		final RandomGenerator randomGenerator = stochasticsDataView.getRandomGeneratorFromId(randomNumberGeneratorId);
		if (randomGenerator == null) {
			context.throwContractException(StochasticsError.UNKNOWN_RANDOM_NUMBER_GENERATOR_ID, randomNumberGeneratorId);
		}
	}

}
