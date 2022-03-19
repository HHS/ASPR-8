package plugins.partitions.testsupport.attributes.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import nucleus.Event;
import nucleus.SimulationContext;
import plugins.partitions.support.Labeler;
import plugins.partitions.support.LabelerSensitivity;
import plugins.partitions.testsupport.attributes.AttributesDataManager;
import plugins.partitions.testsupport.attributes.events.AttributeChangeObservationEvent;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;

/**
 * A labeler for attributes. The dimension of the labeler is the given
 * {@linkplain AttributeId}, the event that stimulates a label update is
 * {@linkplain AttributeChangeObservationEvent} and the labeling function is
 * composed from the given Function.
 * 
 * @author Shawn Hatch
 *
 */
public final class AttributeLabeler implements Labeler {

	private final AttributeId attributeId;
	private final Function<Object, Object> attributeValueLabelingFunction;
	private AttributesDataManager attributesDataManager;

	public AttributeLabeler(AttributeId attributeId, Function<Object, Object> attributeValueLabelingFunction) {
		this.attributeId = attributeId;
		this.attributeValueLabelingFunction = attributeValueLabelingFunction;
	}

	private Optional<PersonId> getPersonId(AttributeChangeObservationEvent attributeChangeObservationEvent) {
		PersonId result = null;
		if (attributeChangeObservationEvent.getAttributeId().equals(attributeId)) {
			result = attributeChangeObservationEvent.getPersonId();
		}
		return Optional.ofNullable(result);
	}

	/**
	 * Returns one LabelerSensitivity of AttributeChangeObservationEvent
	 */
	@Override
	public Set<LabelerSensitivity<?>> getLabelerSensitivities() {
		Set<LabelerSensitivity<?>> result = new LinkedHashSet<>();
		result.add(new LabelerSensitivity<AttributeChangeObservationEvent>(AttributeChangeObservationEvent.class, this::getPersonId));
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
	 *                          if the compartment id is unknown
	 */
	@Override
	public Object getLabel(SimulationContext simulationContext, PersonId personId) {
		if (attributesDataManager == null) {
			attributesDataManager = simulationContext.getDataManager(AttributesDataManager.class).get();
		}
		Object value = attributesDataManager.getAttributeValue(personId, attributeId);
		return attributeValueLabelingFunction.apply(value);
	}

	/**
	 * Returns the attribute id as the dimension
	 */
	@Override
	public Object getDimension() {
		return attributeId;
	}

	@Override
	public Object getPastLabel(SimulationContext simulationContext, Event event) {
		AttributeChangeObservationEvent attributeChangeObservationEvent = (AttributeChangeObservationEvent)event;
		return attributeValueLabelingFunction.apply(attributeChangeObservationEvent.getPreviousValue());
	}

}
