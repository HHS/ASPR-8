package plugins.partitions.testsupport.attributes.resolvers;

import nucleus.DataManagerContext;
import plugins.partitions.testsupport.attributes.AttributesPlugin;
import plugins.partitions.testsupport.attributes.datacontainers.AttributesDataManager;
import plugins.partitions.testsupport.attributes.datacontainers.AttributesDataView;
import plugins.partitions.testsupport.attributes.events.mutation.AttributeValueAssignmentEvent;
import plugins.partitions.testsupport.attributes.events.observation.AttributeChangeObservationEvent;
import plugins.partitions.testsupport.attributes.initialdata.AttributeInitialData;
import plugins.partitions.testsupport.attributes.support.AttributeDefinition;
import plugins.partitions.testsupport.attributes.support.AttributeError;
import plugins.partitions.testsupport.attributes.support.AttributeId;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.events.observation.PersonImminentRemovalObservationEvent;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import util.ContractException;

/**
 * 
 * Provides event resolution for the {@linkplain AttributesPlugin}.
 * <P>
 * Creates, publishes and maintains the {@linkplain AttributesDataView}. Initializes the data view from
 * the {@linkplain AttributeInitialData} instance provided to the plugin.
 * </P>
 * 
 * <P>
 * Resolves the following events:
 * <ul>
 * 
 * 
 * 
 * <li>{@linkplain AttributeValueAssignmentEvent} <blockquote> Updates the
 * person's current attribute value in the
 * {@linkplain AttributesDataView}. Generates a corresponding
 * {@linkplain AttributeChangeObservationEvent} 
 * 
 * <BR>
 * <BR>
 * 
 * Throws {@link ContractException}
 *
 * <ul>
 * <li>{@link PersonError#NULL_PERSON_ID} if the person id is null</li>
 * <li>{@link PersonError#UNKNOWN_PERSON_ID} if the person id is unknown</li>
 * <li>{@link AttributeError#NULL_ATTRIBUTE_ID} if the attribute id is null</li>
 * <li>{@link AttributeError#UNKNOWN_ATTRIBUTE_ID} if the attribute id is unknown</li>
 * <li>{@link AttributeError#NULL_ATTRIBUTE_VALUE} if the attribute value is null</li>
 * <li>{@link AttributeError#INCOMPATIBLE_VALUE} if the attribute value is incompatible with the associated attribute definition</li>
 * </ul>
 * 
 * </blockquote></li>
 * 
 * <ul>
 * </p>
 * 
 * @author Shawn Hatch
 *
 */


public final class AttributesEventResolver {

	/**
	 * Constructs this AttributesEventResolver from the given AttributeInitialData
	 * 
	 * @throws ContractException
	 * <li>{@linkplain AttributeError#NULL_ATTRIBUTE_INITIAL_DATA} </li>
	 */
	public AttributesEventResolver(AttributeInitialData attributeInitialData) {
		if(attributeInitialData == null) {
			throw new ContractException(AttributeError.NULL_ATTRIBUTE_INITIAL_DATA);
		}
		this.attributeInitialData = attributeInitialData;
	}

	private AttributeInitialData attributeInitialData;

	private AttributesDataManager attributesDataManager;

	private void handlePersonImminentRemovalObservationEventValidation(final DataManagerContext dataManagerContext, final PersonImminentRemovalObservationEvent personImminentRemovalObservationEvent) {
		validatePersonExists(dataManagerContext, personImminentRemovalObservationEvent.getPersonId());
	}

	private void handlePersonImminentRemovalObservationEventExecution(final DataManagerContext dataManagerContext, final PersonImminentRemovalObservationEvent personImminentRemovalObservationEvent) {
		dataManagerContext.addPlan((context) -> attributesDataManager.handlePersonRemoval(personImminentRemovalObservationEvent.getPersonId()), dataManagerContext.getTime());
	}

	private PersonDataView personDataView;

	public void init(final DataManagerContext dataManagerContext) {

		attributesDataManager = new AttributesDataManager(dataManagerContext.getSafeContext());

		personDataView = dataManagerContext.getDataView(PersonDataView.class).get();

		dataManagerContext.addEventLabeler(AttributeChangeObservationEvent.getEventLabeler());
		
		dataManagerContext.subscribeToEventValidationPhase(AttributeValueAssignmentEvent.class, this::handleAttributeValueAssignmentEventValidation);
		dataManagerContext.subscribeToEventExecutionPhase(AttributeValueAssignmentEvent.class, this::handleAttributeValueAssignmentEventExecution);

		dataManagerContext.subscribeToEventValidationPhase(PersonImminentRemovalObservationEvent.class, this::handlePersonImminentRemovalObservationEventValidation);
		dataManagerContext.subscribeToEventExecutionPhase(PersonImminentRemovalObservationEvent.class, this::handlePersonImminentRemovalObservationEventExecution);

		for (AttributeId attributeId : attributeInitialData.getAttributeIds()) {
			AttributeDefinition attributeDefinition = attributeInitialData.getAttributeDefinition(attributeId);
			attributesDataManager.addAttribute(attributeId, attributeDefinition);
		}

		dataManagerContext.publishDataView(new AttributesDataView(dataManagerContext, attributesDataManager));
	}

	private void handleAttributeValueAssignmentEventValidation(final DataManagerContext dataManagerContext, final AttributeValueAssignmentEvent attributeValueAssignmentEvent) {
		final PersonId personId = attributeValueAssignmentEvent.getPersonId();
		final AttributeId attributeId = attributeValueAssignmentEvent.getAttributeId();
		final Object value = attributeValueAssignmentEvent.getValue();
		validatePersonExists(dataManagerContext, personId);
		validateAttributeId(dataManagerContext, attributeId);
		validateValueNotNull(dataManagerContext, value);
		final AttributeDefinition attributeDefinition = attributesDataManager.getAttributeDefinition(attributeId);
		validateValueCompatibility(dataManagerContext, attributeId, attributeDefinition, value);
	}

	private void handleAttributeValueAssignmentEventExecution(final DataManagerContext dataManagerContext, final AttributeValueAssignmentEvent attributeValueAssignmentEvent) {
		final PersonId personId = attributeValueAssignmentEvent.getPersonId();
		final AttributeId attributeId = attributeValueAssignmentEvent.getAttributeId();
		final Object value = attributeValueAssignmentEvent.getValue();
		Object previousValue = attributesDataManager.getAttributeValue(personId, attributeId);
		attributesDataManager.setAttributeValue(personId, attributeId, value);		
		dataManagerContext.resolveEvent(new AttributeChangeObservationEvent(personId, attributeId,previousValue, value));
	}

	private void validatePersonExists(final DataManagerContext dataManagerContext, final PersonId personId) {
		if (personId == null) {
			dataManagerContext.throwContractException(PersonError.NULL_PERSON_ID);
		}
		if (!personDataView.personExists(personId)) {
			dataManagerContext.throwContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	private static void validateValueNotNull(final DataManagerContext dataManagerContext, final Object value) {
		if (value == null) {
			dataManagerContext.throwContractException(AttributeError.NULL_ATTRIBUTE_VALUE);
		}
	}

	private static void validateValueCompatibility(final DataManagerContext dataManagerContext, final AttributeId attributeId, final AttributeDefinition attributeDefinition, final Object value) {
		if (!attributeDefinition.getType().isAssignableFrom(value.getClass())) {
			dataManagerContext.throwContractException(AttributeError.INCOMPATIBLE_VALUE,
					"Attribute value " + value + " is not of type " + attributeDefinition.getType().getName() + " and does not match definition of " + attributeId);
		}
	}

	private void validateAttributeId(DataManagerContext dataManagerContext, final AttributeId attributeId) {
		if (attributeId == null) {
			dataManagerContext.throwContractException(AttributeError.NULL_ATTRIBUTE_ID);
		}
		if (!attributesDataManager.attributeExists(attributeId)) {
			dataManagerContext.throwContractException(AttributeError.UNKNOWN_ATTRIBUTE_ID, attributeId);
		}
	}

}
