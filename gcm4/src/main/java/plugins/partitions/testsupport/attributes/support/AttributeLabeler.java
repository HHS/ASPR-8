package plugins.partitions.testsupport.attributes.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import nucleus.Event;
import nucleus.SimulationContext;
import plugins.partitions.support.Labeler;
import plugins.partitions.support.LabelerSensitivity;
import plugins.partitions.testsupport.attributes.AttributesDataManager;
import plugins.partitions.testsupport.attributes.events.AttributeUpdateEvent;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;

/**
 * A labeler for attributes. The dimension of the labeler is the given
 * {@linkplain AttributeId}, the event that stimulates a label update is
 * {@linkplain AttributeUpdateEvent} and the labeling function is composed from
 * the given Function.
 * 
 *
 */
public abstract class AttributeLabeler implements Labeler {

	private final AttributeId attributeId;
	
	private AttributesDataManager attributesDataManager;

	protected abstract Object getLabelFromValue(Object value);

	public AttributeLabeler(AttributeId attributeId) {
		this.attributeId = attributeId;
	}

	private Optional<PersonId> getPersonId(AttributeUpdateEvent attributeUpdateEvent) {
		PersonId result = null;
		if (attributeUpdateEvent.attributeId().equals(attributeId)) {
			result = attributeUpdateEvent.personId();
		}
		return Optional.ofNullable(result);
	}

	/**
	 * Returns one LabelerSensitivity of AttributeUpdateEvent
	 */
	@Override
	public final Set<LabelerSensitivity<?>> getLabelerSensitivities() {
		Set<LabelerSensitivity<?>> result = new LinkedHashSet<>();
		result.add(new LabelerSensitivity<AttributeUpdateEvent>(AttributeUpdateEvent.class, this::getPersonId));
		return result;
	}

	/**
	 * Returns the label for the person
	 * 
	 * precondition: the context should not be null
	 * 
	 * @throwsContractException
	 *                          <li>{@linkplain PersonError#NULL_PERSON_ID} if
	 *                          the person id is null
	 *                          <li>{@linkplain PersonError#UNKNOWN_PERSON_ID}
	 *                          if the person id is unknown
	 */
	@Override
	public final Object getCurrentLabel(SimulationContext simulationContext, PersonId personId) {
		if (attributesDataManager == null) {
			attributesDataManager = simulationContext.getDataManager(AttributesDataManager.class);
		}
		Object value = attributesDataManager.getAttributeValue(personId, attributeId);
		return getLabelFromValue(value);
	}

	/**
	 * Returns the attribute id as the dimension
	 */
	@Override
	public final Object getId() {
		return attributeId;
	}

	@Override
	public final Object getPastLabel(SimulationContext simulationContext, Event event) {
		AttributeUpdateEvent attributeUpdateEvent = (AttributeUpdateEvent) event;
		return getLabelFromValue(attributeUpdateEvent.previousValue());
	}

}
