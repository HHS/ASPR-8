package plugins.compartments.initialdata;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.jcip.annotations.Immutable;
import nucleus.AgentContext;
import nucleus.DataView;
import plugins.compartments.support.CompartmentError;
import plugins.compartments.support.CompartmentId;
import plugins.compartments.support.CompartmentPropertyId;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.PropertyError;
import plugins.properties.support.TimeTrackingPolicy;
import util.ContractException;

/**
 * An immutable container of the initial state of compartments. It contains:
 * <BR>
 * <ul>
 * <li>compartment ids</li>
 * <li>suppliers of consumers of {@linkplain AgentContext} for compartment
 * initialization</li>
 * <li>compartment property definitions: each compartment has its own set of
 * properties and all property definitions have default values</li>
 * <li>compartment property values</li>
 * <li>person compartment assignments</li>
 * <li>person compartment arrival time tracking policy</li>
 * </ul>
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public final class CompartmentInitialData implements DataView {

	/**
	 * Builder class for CompartmentInitialData
	 * 
	 * @author Shawn Hatch
	 *
	 */
	public static class Builder {
		private Data data = new Data();

		private Builder() {

		}

		/**
		 * Returns the {@link CompartmentInitialData} from the collected
		 * information supplied to this builder. Clears the builder's state.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain CompartmentError#UNKNOWN_COMPARTMENT_ID}</li>
		 *             if a person was associated with a compartment id that was
		 *             not properly added with an initial agent behavior.
		 * 
		 *             <li>{@linkplain CompartmentError#UNKNOWN_COMPARTMENT_ID}</li>
		 *             if a compartment property definition was associated with
		 *             a compartment id that was not properly added with an
		 *             initial agent behavior.
		 * 
		 *             <li>{@linkplain CompartmentError#UNKNOWN_COMPARTMENT_ID}</li>
		 *             if a compartment property value was associated with a
		 *             compartment id that was not properly added with an
		 *             initial agent behavior.
		 * 
		 *             <li>{@linkplain CompartmentError#UNKNOWN_COMPARTMENT_PROPERTY_ID}</li>
		 *             if a compartment property value was associated with a
		 *             compartment property id that was not defined
		 * 
		 *             <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE}</li> if
		 *             a compartment property value was associated with a
		 *             compartment and compartment property id that is
		 *             incompatible with the corresponding property definition.
		 * 
		 *             <li>{@linkplain CompartmentError#INSUFFICIENT_COMPARTMENT_PROPERTY_VALUE_ASSIGNMENT}</li>
		 *             if a compartment property definition does not have a
		 *             default value and there are no property values added to
		 *             replace that default.
		 * 
		 */
		public CompartmentInitialData build() {
			try {
				// set the compartment arrival tracking policy if it was not set
				// by the client
				if (data.compartmentArrivalTimeTrackingPolicy == null) {
					data.compartmentArrivalTimeTrackingPolicy = TimeTrackingPolicy.DO_NOT_TRACK_TIME;
				}

				/*
				 * For every compartment property definition that has a null
				 * default value, find a property value from the data to grant
				 * the definition a default value. We do this to eliminate
				 * repetitive checking.
				 */
				for (CompartmentId compartmentId : data.compartmentIds.keySet()) {
					Map<CompartmentPropertyId, PropertyDefinition> propertyDefinitions = data.compartmentPropertyDefinitions.get(compartmentId);
					if (propertyDefinitions != null) {
						for (CompartmentPropertyId compartmentPropertyId : propertyDefinitions.keySet()) {
							PropertyDefinition propertyDefinition = propertyDefinitions.get(compartmentPropertyId);
							if (!propertyDefinition.getDefaultValue().isPresent()) {
								Map<CompartmentPropertyId, Object> compartmentPropertyMap = data.compartmentPropertyValues.get(compartmentId);
								Object propertyValue = null;
								if (compartmentPropertyMap != null) {
									propertyValue = compartmentPropertyMap.get(compartmentPropertyId);
								}
								if (propertyValue != null) {
									propertyDefinition = PropertyDefinition	.builder().setDefaultValue(propertyDefinition.getType()).setDefaultValue(propertyValue)
																			.setTimeTrackingPolicy(propertyDefinition.getTimeTrackingPolicy())
																			.setPropertyValueMutability(propertyDefinition.propertyValuesAreMutable()).build();
									propertyDefinitions.put(compartmentPropertyId, propertyDefinition);
								}
							}
						}
					}
				}

				validateData(data);
				return new CompartmentInitialData(data);
			} finally {
				data = new Data();
			}
		}

		/**
		 * Defines a compartment property
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain CompartmentError#NULL_COMPARTMENT_ID}
		 *              if the compartment id is null</li>
		 * 
		 *             <li>{@linkplain CompartmentError#NULL_COMPARTMENT_PROPERTY_ID}
		 *             if the compartment property id is null </li>
		 * 
		 *             <li>{@linkplain CompartmentError#NULL_COMPARTMENT_PROPERTY_DEFINITION}
		 *             if the property definition is null </li>
		 *
		 *             <li>{@linkplain CompartmentError#DUPLICATE_COMPARTMENT_PROPERTY_DEFINITION_ASSIGNMENT}
		 *              if a property definition for the given compartment
		 *             id and property id was previously defined.</li>
		 * 
		 */
		public Builder defineCompartmentProperty(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId, final PropertyDefinition propertyDefinition) {
			validateCompartmentIdNotNull(compartmentId);
			validateCompartmentPropertyIdNotNull(compartmentPropertyId);
			validateCompartmentPropertyDefinitionNotNull(propertyDefinition);
			validateCompartmentPropertyIsNotDefined(data, compartmentId, compartmentPropertyId);
			Map<CompartmentPropertyId, PropertyDefinition> map = data.compartmentPropertyDefinitions.get(compartmentId);
			if (map == null) {
				map = new LinkedHashMap<>();
				data.compartmentPropertyDefinitions.put(compartmentId, map);
			}
			map.put(compartmentPropertyId, propertyDefinition);
			return this;
		}

		/**
		 * Adds the compartment id and its associated agent initial behavior.
		 * 
		 * @throws ContractException
		 * 
		 *             <li>{@linkplain CompartmentError#NULL_COMPARTMENT_ID}
		 *             </li>if the compartment id is null
		 * 
		 *             <li>{@linkplain CompartmentError#NULL_COMPARTMENT_INITIAL_BEHAVIOR_SUPPLIER}
		 *             </li>if the supplier is null
		 * 
		 *             <li>{@linkplain CompartmentError#DUPLICATE_COMPARTMENT_INITIAL_BEHAVIOR_ASSIGNMENT}
		 *             </li>if the compartment initial behavior was previously
		 *             defined
		 * 
		 */
		public Builder setCompartmentInitialBehaviorSupplier(final CompartmentId compartmentId, final Supplier<Consumer<AgentContext>> supplier) {
			validateCompartmentIdNotNull(compartmentId);
			validateCompartmentInitialBehaviorSupplierNotNull(supplier);
			validateCompartmentComponentClassNotAssigned(data, compartmentId);
			data.compartmentIds.put(compartmentId, supplier);
			return this;
		}

		/**
		 * Sets the compartment property value that overrides the default value
		 * of the corresponding property definition
		 * 
		 * @throws ContractException
		 * 
		 *             <li>{@linkplain CompartmentError#NULL_COMPARTMENT_ID}
		 *             </li>if the compartment id is null
		 * 
		 *             <li>{@linkplain CompartmentError#NULL_COMPARTMENT_PROPERTY_ID}
		 *             </li>if the compartment property id is null
		 * 
		 *             <li>{@linkplain CompartmentError#DUPLICATE_COMPARTMENT_PROPERTY_VALUE}
		 *             </li>if the compartment property value was previously
		 *             defined
		 * 
		 */
		public Builder setCompartmentPropertyValue(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId, final Object compartmentPropertyValue) {
			validateCompartmentIdNotNull(compartmentId);
			validateCompartmentPropertyIdNotNull(compartmentPropertyId);
			validateCompartmentPropertyValueNotSet(data, compartmentId, compartmentPropertyId);
			Map<CompartmentPropertyId, Object> propertyMap = data.compartmentPropertyValues.get(compartmentId);
			if (propertyMap == null) {
				propertyMap = new LinkedHashMap<>();
				data.compartmentPropertyValues.put(compartmentId, propertyMap);
			}
			propertyMap.put(compartmentPropertyId, compartmentPropertyValue);
			return this;
		}

		/**
		 * Sets the person's compartment
		 * 
		 * @throws ContractException
		 * 
		 *             <li>{@linkplain PersonError#NULL_PERSON_ID}</li>if the
		 *             person id is null
		 * 
		 *             <li>{@linkplain CompartmentError#NULL_COMPARTMENT_ID}
		 *             </li>if the compartment id is null
		 * 
		 *             <li>{@linkplain CompartmentError#DUPLICATE_PERSON_COMPARTMENT_ASSIGNMENT}
		 *             </li>if the person's compartment was previously defined
		 * 
		 */
		public Builder setPersonCompartment(final PersonId personId, final CompartmentId compartmentId) {
			validatePersonIdNotNull(personId);
			validateCompartmentIdNotNull(compartmentId);
			validatePersonCompartmentNotAssigned(data, personId);
			data.personCompartments.put(personId, compartmentId);
			return this;
		}

		/**
		 * Sets the tracking policy for compartment arrival times
		 * 
		 * @throws ContractException
		 * 
		 *             <li>{@linkplain CompartmentError#NULL_TIME_TRACKING_POLICY}</li>if
		 *             the timeTrackingPolicy is null
		 * 
		 *             <li>{@linkplain CompartmentError#DUPLICATE_TIME_TRACKING_POLICY}
		 *             </li>if the timeTrackingPolicy was previously defined
		 * 
		 */
		public Builder setPersonCompartmentArrivalTracking(final TimeTrackingPolicy timeTrackingPolicy) {
			validateTimeTrackingPolicyNotNull(timeTrackingPolicy);
			validatePersonCompartmentArrivalTrackingNotSet(data);
			data.compartmentArrivalTimeTrackingPolicy = timeTrackingPolicy;
			return this;
		}

	}

	private static class Data {

		private final Map<CompartmentId, Map<CompartmentPropertyId, PropertyDefinition>> compartmentPropertyDefinitions = new LinkedHashMap<>();

		private final Map<CompartmentId, Supplier<Consumer<AgentContext>>> compartmentIds = new LinkedHashMap<>();

		private final Map<PersonId, CompartmentId> personCompartments = new LinkedHashMap<>();

		private TimeTrackingPolicy compartmentArrivalTimeTrackingPolicy;

		private final Map<CompartmentId, Map<CompartmentPropertyId, Object>> compartmentPropertyValues = new LinkedHashMap<>();

	}

	/**
	 * Returns a new builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

	private static void validateCompartmentComponentClassNotAssigned(final Data data, final CompartmentId compartmentId) {
		if (data.compartmentIds.containsKey(compartmentId)) {
			throw new ContractException(CompartmentError.DUPLICATE_COMPARTMENT_INITIAL_BEHAVIOR_ASSIGNMENT);
		}
	}

	private static void validateCompartmentInitialBehaviorSupplierNotNull(final Supplier<Consumer<AgentContext>> supplier) {
		if (supplier == null) {
			throw new ContractException(CompartmentError.NULL_COMPARTMENT_INITIAL_BEHAVIOR_SUPPLIER);
		}
	}

	private static void validateCompartmentExists(final Data data, final CompartmentId compartmentId) {
		if (compartmentId == null) {
			throw new ContractException(CompartmentError.NULL_COMPARTMENT_ID);
		}
		if (!data.compartmentIds.containsKey(compartmentId)) {
			throw new ContractException(CompartmentError.UNKNOWN_COMPARTMENT_ID, compartmentId);
		}
	}

	private static void validatePersonExists(final Data data, final PersonId personId) {
		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
		if (!data.personCompartments.containsKey(personId)) {
			throw new ContractException(PersonError.UNKNOWN_PERSON_ID, personId);
		}
	}

	private static void validateCompartmentIdNotNull(final CompartmentId compartmentId) {
		if (compartmentId == null) {
			throw new ContractException(CompartmentError.NULL_COMPARTMENT_ID);
		}
	}

	private static void validateCompartmentPropertyDefinitionNotNull(final PropertyDefinition propertyDefinition) {
		if (propertyDefinition == null) {
			throw new ContractException(CompartmentError.NULL_COMPARTMENT_PROPERTY_DEFINITION);
		}
	}

	private static void validateCompartmentPropertyIdNotNull(final CompartmentPropertyId compartmentPropertyId) {
		if (compartmentPropertyId == null) {
			throw new ContractException(CompartmentError.NULL_COMPARTMENT_PROPERTY_ID);
		}
	}

	/*
	 * precondition : data and compartment id are valid
	 *
	 */
	private static void validateCompartmentPropertyIsDefined(final Data data, final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId) {
		final Map<CompartmentPropertyId, PropertyDefinition> map = data.compartmentPropertyDefinitions.get(compartmentId);
		if (map == null) {
			throw new ContractException(CompartmentError.UNKNOWN_COMPARTMENT_PROPERTY_ID);
		}
		if (!map.containsKey(compartmentPropertyId)) {
			throw new ContractException(CompartmentError.UNKNOWN_COMPARTMENT_PROPERTY_ID);
		}
	}

	private static void validateCompartmentPropertyIsNotDefined(final Data data, final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId) {
		final Map<CompartmentPropertyId, PropertyDefinition> map = data.compartmentPropertyDefinitions.get(compartmentId);
		if (map != null) {
			if (map.containsKey(compartmentPropertyId)) {
				throw new ContractException(CompartmentError.DUPLICATE_COMPARTMENT_PROPERTY_DEFINITION_ASSIGNMENT);
			}
		}
	}

	private static void validateCompartmentPropertyValueNotSet(final Data data, final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId) {
		final Map<CompartmentPropertyId, Object> propertyMap = data.compartmentPropertyValues.get(compartmentId);
		if (propertyMap != null) {
			if (propertyMap.containsKey(compartmentPropertyId)) {
				throw new ContractException(CompartmentError.DUPLICATE_COMPARTMENT_PROPERTY_VALUE, compartmentPropertyId + " = " + compartmentId);
			}
		}
	}

	private static void validateData(final Data data) {

		for (final PersonId personId : data.personCompartments.keySet()) {
			final CompartmentId compartmentId = data.personCompartments.get(personId);
			if (!data.compartmentIds.containsKey(compartmentId)) {
				throw new ContractException(CompartmentError.UNKNOWN_COMPARTMENT_ID, compartmentId + " in person compartment assignments");
			}
		}

		for (CompartmentId compartmentId : data.compartmentPropertyDefinitions.keySet()) {
			if (!data.compartmentIds.containsKey(compartmentId)) {
				throw new ContractException(CompartmentError.UNKNOWN_COMPARTMENT_ID, compartmentId + " in compartment property definitions");
			}
		}

		for (final CompartmentId compartmentId : data.compartmentPropertyValues.keySet()) {
			if (!data.compartmentIds.containsKey(compartmentId)) {
				throw new ContractException(CompartmentError.UNKNOWN_COMPARTMENT_ID, compartmentId + " in compartment property values");
			}
			Map<CompartmentPropertyId, PropertyDefinition> propDefMap = data.compartmentPropertyDefinitions.get(compartmentId);
			if (propDefMap == null) {
				/*
				 * there should be no values since there are not defined
				 * properties for this compartment
				 */
				throw new ContractException(CompartmentError.UNKNOWN_COMPARTMENT_PROPERTY_ID, compartmentId + " has no defined properties and so should not have property values");

			}

			final Map<CompartmentPropertyId, Object> propValueMap = data.compartmentPropertyValues.get(compartmentId);
			for (final CompartmentPropertyId compartmentPropertyId : propValueMap.keySet()) {
				final PropertyDefinition propertyDefinition = propDefMap.get(compartmentPropertyId);
				if (propertyDefinition == null) {
					throw new ContractException(CompartmentError.UNKNOWN_COMPARTMENT_PROPERTY_ID, compartmentPropertyId + " for compartment " + compartmentId);
				}
				final Object propertyValue = propValueMap.get(compartmentPropertyId);
				if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
					throw new ContractException(PropertyError.INCOMPATIBLE_VALUE, compartmentId + ":" + compartmentPropertyId + " = " + propertyValue);
				}
			}
		}

		/*
		 * Ensure that every property definition has a default value
		 */
		for (CompartmentId compartmentId : data.compartmentIds.keySet()) {
			Map<CompartmentPropertyId, PropertyDefinition> propertyDefinitions = data.compartmentPropertyDefinitions.get(compartmentId);
			if (propertyDefinitions != null) {
				for (CompartmentPropertyId compartmentPropertyId : propertyDefinitions.keySet()) {
					PropertyDefinition propertyDefinition = propertyDefinitions.get(compartmentPropertyId);
					if (!propertyDefinition.getDefaultValue().isPresent()) {
						throw new ContractException(CompartmentError.INSUFFICIENT_COMPARTMENT_PROPERTY_VALUE_ASSIGNMENT, compartmentId + ": " + compartmentPropertyId);
					}
				}
			}
		}

	}

	private static void validatePersonCompartmentArrivalTrackingNotSet(final Data data) {
		if (data.compartmentArrivalTimeTrackingPolicy != null) {
			throw new ContractException(CompartmentError.DUPLICATE_TIME_TRACKING_POLICY);
		}
	}

	private static void validatePersonCompartmentNotAssigned(final Data data, final PersonId personId) {
		if (data.personCompartments.containsKey(personId)) {
			throw new ContractException(CompartmentError.DUPLICATE_PERSON_COMPARTMENT_ASSIGNMENT, personId);
		}

	}

	private static void validatePersonIdNotNull(final PersonId personId) {
		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}
	}

	private static void validateTimeTrackingPolicyNotNull(final TimeTrackingPolicy timeTrackingPolicy) {
		if (timeTrackingPolicy == null) {
			throw new ContractException(CompartmentError.NULL_TIME_TRACKING_POLICY);
		}
	}

	private final Data data;

	private CompartmentInitialData(final Data data) {
		this.data = data;
	}

	/**
	 * Returns the Consumer of AgentContext associated with the compartment Id
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain CompartmentError#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null</li>
	 *             <li>{@linkplain CompartmentError#UNKNOWN_COMPARTMENT_ID} if
	 *             the compartment id is unknown</li>
	 */
	public Consumer<AgentContext> getCompartmentInitialBehavior(final CompartmentId compartmentId) {
		validateCompartmentExists(data, compartmentId);
		return data.compartmentIds.get(compartmentId).get();
	}

	/**
	 * Returns the set of {@link CompartmentId} values contained in this initial
	 * data. Each compartment id will correspond to a compartment agent that is
	 * automatically added to the simulation during initialization.
	 */
	public Set<CompartmentId> getCompartmentIds() {
		return new LinkedHashSet<>(data.compartmentIds.keySet());
	}

	/**
	 * Returns the {@link PropertyDefinition} for the given
	 * {@link CompartmentId} and {@link CompartmentPropertyId}.
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain CompartmentError#NULL_COMPARTMENT_ID}</li> if
	 *             the compartment id is null
	 *             <li>{@linkplain CompartmentError#UNKNOWN_COMPARTMENT_ID}</li>
	 *             if the compartment id is unknown
	 *             <li>{@linkplain CompartmentError#NULL_COMPARTMENT_PROPERTY_ID}
	 *             </li> if the compartment property id is null
	 *             <li>{@linkplain CompartmentError#UNKNOWN_COMPARTMENT_PROPERTY_ID}
	 *             </li> if the compartment property id is known
	 */
	public PropertyDefinition getCompartmentPropertyDefinition(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId) {
		validateCompartmentExists(data, compartmentId);
		validateCompartmentPropertyIdNotNull(compartmentPropertyId);
		validateCompartmentPropertyIsDefined(data, compartmentId, compartmentPropertyId);
		return data.compartmentPropertyDefinitions.get(compartmentId).get(compartmentPropertyId);
	}

	/**
	 * Returns the set of {@link CompartmentPropertyId} for the given
	 * {@link CompartmentId}.
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain CompartmentError#NULL_COMPARTMENT_ID}</li> if
	 *             the compartment id is null
	 *             <li>{@linkplain CompartmentError#UNKNOWN_COMPARTMENT_ID}</li>
	 *             if the compartment id is unknown
	 */
	@SuppressWarnings("unchecked")
	public <T extends CompartmentPropertyId> Set<T> getCompartmentPropertyIds(final CompartmentId compartmentId) {
		validateCompartmentExists(data, compartmentId);
		final Set<T> result = new LinkedHashSet<>();
		Map<CompartmentPropertyId, PropertyDefinition> map = data.compartmentPropertyDefinitions.get(compartmentId);
		if (map != null) {
			for (CompartmentPropertyId compartmentPropertyId : map.keySet()) {
				result.add((T) compartmentPropertyId);
			}
		}
		return result;
	}

	/**
	 * Returns the property value for the given {@link CompartmentId} and
	 * {@link CompartmentPropertyId}.
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain CompartmentError#NULL_COMPARTMENT_ID}</li> if
	 *             the compartment id is null
	 *             <li>{@linkplain CompartmentError#UNKNOWN_COMPARTMENT_ID}</li>
	 *             if the compartment id is unknown
	 *             <li>{@linkplain CompartmentError#NULL_COMPARTMENT_PROPERTY_ID}
	 *             </li> if the compartment property id is null
	 *             <li>{@linkplain CompartmentError#UNKNOWN_COMPARTMENT_PROPERTY_ID}
	 *             </li> if the compartment property id is known
	 */
	@SuppressWarnings("unchecked")
	public <T> T getCompartmentPropertyValue(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId) {
		validateCompartmentExists(data, compartmentId);
		validateCompartmentPropertyIdNotNull(compartmentPropertyId);
		validateCompartmentPropertyIsDefined(data, compartmentId, compartmentPropertyId);
		Object result = null;
		final Map<CompartmentPropertyId, Object> map = data.compartmentPropertyValues.get(compartmentId);
		if (map != null) {
			result = map.get(compartmentPropertyId);
		}

		if (result == null) {
			/*
			 * We have previously checked that every compartment property value
			 * is either present or is backed by a default value from its
			 * corresponding definition.
			 */
			final PropertyDefinition propertyDefinition = data.compartmentPropertyDefinitions.get(compartmentId).get(compartmentPropertyId);
			result = propertyDefinition.getDefaultValue().get();
		}
		return (T) result;
	}

	/**
	 * Returns the {@link CompartmentId} for the given {@link PersonId}.
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID}</li> if the
	 *             person id is null
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID}</li> if the
	 *             person id is unknown
	 */
	@SuppressWarnings("unchecked")
	public <T extends CompartmentId> T getPersonCompartment(final PersonId personId) {
		validatePersonExists(data, personId);
		return (T) data.personCompartments.get(personId);
	}

	/**
	 * Returns the {@link TimeTrackingPolicy}. Defaulted to
	 * {@link TimeTrackingPolicy#DO_NOT_TRACK_TIME} if not set in the builder.
	 * 
	 */
	public TimeTrackingPolicy getPersonCompartmentArrivalTrackingPolicy() {
		return data.compartmentArrivalTimeTrackingPolicy;
	}

	/**
	 * Returns the set of {@link PersonId} collected by the builder.
	 */
	public Set<PersonId> getPersonIds() {
		return new LinkedHashSet<>(data.personCompartments.keySet());
	}
}
