package plugins.globals.initialdata;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.jcip.annotations.Immutable;
import nucleus.AgentContext;
import nucleus.DataView;
import plugins.globals.support.GlobalComponentId;
import plugins.globals.support.GlobalError;
import plugins.globals.support.GlobalPropertyId;
import plugins.properties.support.PropertyDefinition;
import util.ContractException;

/**
 * An immutable container of the initial state of global components and global
 * properties. It contains: <BR>
 * <ul>
 * <li>global component ids</li>
 * <li>suppliers of consumers of {@linkplain AgentContext} for global component
 * initialization</li>
 * <li>global property definitions</li>
 * <li>global property values</li> *
 * </ul>
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public final class GlobalInitialData implements DataView {

	private static void validateGlobalPropertyIsNotDefined(final Data data, final GlobalPropertyId globalPropertyId) {
		final PropertyDefinition propertyDefinition = data.globalPropertyDefinitions.get(globalPropertyId);
		if (propertyDefinition != null) {
			throw new ContractException(GlobalError.DUPLICATE_GLOBAL_PROPERTY_DEFINITION, globalPropertyId);
		}
	}

	private static void validateGlobalPropertyValueNotAssigned(final Data data, final GlobalPropertyId globalPropertyId) {
		if (data.globalPropertyValues.containsKey(globalPropertyId)) {
			throw new ContractException(GlobalError.DUPLICATE_GLOBAL_PROPERTY_VALUE_ASSIGNMENT, globalPropertyId);
		}
	}

	private static void validateGlobalComponentNotAssigned(Data data, GlobalComponentId globalComponentId) {
		if (data.globalComponentIds.containsKey(globalComponentId)) {
			throw new ContractException(GlobalError.DUPLICATE_GLOBAL_COMPONENT_INITIAL_BEHAVIOR_ASSIGNMENT);
		}
	}

	/**
	 * Builder class for GloblaInitialData
	 * 
	 * @author Shawn Hatch
	 *
	 */
	public static class Builder {
		private Data data = new Data();

		private Builder() {

		}

		/**
		 * Returns the {@link GlobalInitialData} from the collected information
		 * supplied to this builder. Clears the builder's state.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain GlobalError.UNKNOWN_GLOBAL_PROPERTY_ID}</li>
		 *             if a global property value was associated with a global
		 *             property id that was not defined
		 * 
		 *             <li>{@linkplain GlobalError#INCOMPATIBLE_VALUE}</li> if a
		 *             global property value was associated with a global
		 *             property id that is incompatible with the corresponding
		 *             property definition.
		 * 
		 *             <li>{@linkplain GlobalError#INSUFFICIENT_GLOBAL_PROPERTY_VALUE_ASSIGNMENT}</li>
		 *             if a global property definition does not have a default
		 *             value and there are no property values added to replace
		 *             that default.
		 */
		public GlobalInitialData build() {
			try {
				validateData(data);
				return new GlobalInitialData(data);
			} finally {
				data = new Data();
			}
		}

		/**
		 * Defines a global property
		 * 
		 * @throws ContractException
		 * 
		 * 
		 *             <li>{@linkplain GlobalError#NULL_GLOBAL_PROPERTY_ID}</li>
		 *             if the global property id is null
		 * 
		 *             <li>{@linkplain GlobalError#NULL_GLOBAL_PROPERTY_DEFINITION}
		 *             </li> if the property definition is null
		 *
		 *             <li>{@linkplain GlobalError#DUPLICATE_GLOBAL_PROPERTY_DEFINITION}
		 *             </li> if a property definition for the given global
		 *             property id was previously defined.
		 * 
		 */
		public Builder defineGlobalProperty(final GlobalPropertyId globalPropertyId, final PropertyDefinition propertyDefinition) {
			validateGlobalPropertyIdNotNull(globalPropertyId);
			validateGlobalPropertyDefinitionNotNull(propertyDefinition);
			validateGlobalPropertyIsNotDefined(data, globalPropertyId);
			data.globalPropertyDefinitions.put(globalPropertyId, propertyDefinition);
			return this;
		}

		/**
		 * Adds the global component id and its associated agent initial
		 * behavior.
		 * 
		 * @throws ContractException
		 * 
		 *             <li>{@linkplain GlobalError#NULL_GLOBAL_COMPONENT_ID}
		 *             </li>if the global component id is null
		 * 
		 *             <li>{@linkplain GlobalError#NULL_GLOBAL_COMPONENT_INITIAL_BEHVAVIOR_SUPPLIER}
		 *             </li>if the supplier is null
		 * 
		 *             <li>{@linkplain GlobalError#GlobalError.DUPLICATE_GLOBAL_COMPONENT_INITIAL_BEHAVIOR_ASSIGNMENT}
		 *             </li>if the global component initial behavior was
		 *             previously defined
		 * 
		 */
		public Builder setGlobalComponentInitialBehaviorSupplier(final GlobalComponentId globalComponentId, final Supplier<Consumer<AgentContext>> supplier) {
			validateGlobalComponentIdNotNull(globalComponentId);
			validateInitialBehaviorSupplierNotNull(supplier);
			validateGlobalComponentNotAssigned(data, globalComponentId);
			data.globalComponentIds.put(globalComponentId, supplier);
			return this;
		}

		/**
		 * Sets the global property value that overrides the default value of
		 * the corresponding property definition
		 * 
		 * @throws ContractException
		 * 
		 *             <li>{@linkplain GlobalError#NULL_GLOBAL_PROPERTY_ID}
		 *             </li>if the global property id is null
		 * 
		 *             <li>{@linkplain GlobalError#NULL_GLOBAL_PROPERTY_VALUE}
		 *             </li>if the global property value is null
		 * 
		 *             <li>{@linkplain GlobalError#DUPLICATE_GLOBAL_PROPERTY_VALUE_ASSIGNMENT}
		 *             </li>if the global property value was previously defined
		 *             for the given global property id
		 * 
		 */
		public Builder setGlobalPropertyValue(final GlobalPropertyId globalPropertyId, final Object propertyValue) {
			validateGlobalPropertyIdNotNull(globalPropertyId);
			validateGlobalPropertyValueNotNull(propertyValue);
			validateGlobalPropertyValueNotAssigned(data, globalPropertyId);
			data.globalPropertyValues.put(globalPropertyId, propertyValue);
			return this;
		}

	}

	private static class Data {

		private final Map<GlobalPropertyId, PropertyDefinition> globalPropertyDefinitions = new LinkedHashMap<>();

		private final Map<GlobalComponentId, Supplier<Consumer<AgentContext>>> globalComponentIds = new LinkedHashMap<>();

		private final Map<GlobalPropertyId, Object> globalPropertyValues = new LinkedHashMap<>();
	}

	/**
	 * Returns a Builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

	private static void validateInitialBehaviorSupplierNotNull(final Supplier<Consumer<AgentContext>> supplier) {
		if (supplier == null) {
			throw new ContractException(GlobalError.NULL_GLOBAL_COMPONENT_INITIAL_BEHVAVIOR_SUPPLIER);
		}
	}

	private static void validateData(final Data data) {
		for (final GlobalPropertyId globalPropertyId : data.globalPropertyValues.keySet()) {
			if (!data.globalPropertyDefinitions.containsKey(globalPropertyId)) {
				throw new ContractException(GlobalError.UNKNOWN_GLOBAL_PROPERTY_ID, globalPropertyId);
			}
		}
		for (final GlobalPropertyId globalPropertyId : data.globalPropertyValues.keySet()) {
			final Object propertyValue = data.globalPropertyValues.get(globalPropertyId);
			final PropertyDefinition propertyDefinition = data.globalPropertyDefinitions.get(globalPropertyId);
			if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
				throw new ContractException(GlobalError.INCOMPATIBLE_VALUE, globalPropertyId + " = " + propertyValue);
			}
		}

		/*
		 * For every global property definition that has a null default value,
		 * ensure that there is a corresponding global property value assignment
		 * and put that initial assignment on the property definition and repair
		 * the definition.
		 */
		for (GlobalPropertyId globalPropertyId : data.globalPropertyDefinitions.keySet()) {
			PropertyDefinition propertyDefinition = data.globalPropertyDefinitions.get(globalPropertyId);
			if (!propertyDefinition.getDefaultValue().isPresent()) {
				Object propertyValue = data.globalPropertyValues.get(globalPropertyId);
				if (propertyValue == null) {
					throw new ContractException(GlobalError.INSUFFICIENT_GLOBAL_PROPERTY_VALUE_ASSIGNMENT, globalPropertyId);
				}
				propertyDefinition = //
						PropertyDefinition	.builder()//
											.setPropertyValueMutability(propertyDefinition.propertyValuesAreMutable())//
											.setDefaultValue(propertyValue)//
											.setTimeTrackingPolicy(propertyDefinition.getTimeTrackingPolicy())//
											.setType(propertyDefinition.getType())//
											.build();//
				data.globalPropertyDefinitions.put(globalPropertyId, propertyDefinition);
			}
		}
	}

	private static void validateGlobalComponentIdNotNull(final GlobalComponentId globalComponentId) {
		if (globalComponentId == null) {
			throw new ContractException(GlobalError.NULL_GLOBAL_COMPONENT_ID);
		}
	}

	private static void validateGlobalPropertyDefinitionNotNull(final PropertyDefinition propertyDefinition) {
		if (propertyDefinition == null) {
			throw new ContractException(GlobalError.NULL_GLOBAL_PROPERTY_DEFINITION);
		}
	}

	private static void validateGlobalPropertyIdNotNull(final GlobalPropertyId globalPropertyId) {
		if (globalPropertyId == null) {
			throw new ContractException(GlobalError.NULL_GLOBAL_PROPERTY_ID);
		}
	}

	private static void validateGlobalPropertyValueNotNull(final Object propertyValue) {
		if (propertyValue == null) {
			throw new ContractException(GlobalError.NULL_GLOBAL_PROPERTY_VALUE);
		}
	}

	private final Data data;

	private GlobalInitialData(final Data data) {
		this.data = data;
	}

	private static void validategGlobalComponentIdExists(final Data data, final GlobalComponentId globalComponentId) {
		if (globalComponentId == null) {
			throw new ContractException(GlobalError.NULL_GLOBAL_COMPONENT_ID);
		}
		if (!data.globalComponentIds.containsKey(globalComponentId)) {
			throw new ContractException(GlobalError.UNKNOWN_GLOBAL_COMPONENT_ID, globalComponentId);
		}
	}

	/**
	 * Returns the Consumer of AgentContext associated with the global component
	 * id
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain GlobalError#NULL_GLOBAL_COMPONENT_ID} if the
	 *             global component id is null</li>
	 *             <li>{@linkplain GlobalError#UNKNOWN_GLOBAL_COMPONENT_ID} if
	 *             the global component id is unknown</li>
	 */
	public Consumer<AgentContext> getGlobalComponentInitialBehavior(final GlobalComponentId globalComponentId) {
		validategGlobalComponentIdExists(data, globalComponentId);
		return data.globalComponentIds.get(globalComponentId).get();
	}

	/**
	 * Returns the set of {@link GlobalComponentId} values contained in this
	 * initial data.
	 */
	@SuppressWarnings("unchecked")
	public <T extends GlobalComponentId> Set<T> getGlobalComponentIds() {
		final Set<T> result = new LinkedHashSet<>();
		for (final GlobalComponentId globalComponentId : data.globalComponentIds.keySet()) {
			result.add((T) globalComponentId);
		}
		return result;
	}

	/**
	 * Returns the {@link PropertyDefinition} for the given
	 * {@link GlobalPropertyId}.
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain GlobalError#NULL_GLOBAL_PROPERTY_ID}</li> if
	 *             the global property id is null
	 *             <li>{@linkplain GlobalError#UNKNOWN_GLOBAL_PROPERTY_ID}</li>
	 *             if the global property id is known
	 */
	public PropertyDefinition getGlobalPropertyDefinition(final GlobalPropertyId globalPropertyId) {
		validategGlobalPropertyIdExists(data, globalPropertyId);
		return data.globalPropertyDefinitions.get(globalPropertyId);
	}

	/**
	 * Returns the set of {@link GlobalPropertyId}
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <T extends GlobalPropertyId> Set<T> getGlobalPropertyIds() {
		final Set<T> result = new LinkedHashSet<>();
		for (final GlobalPropertyId globalPropertyId : data.globalPropertyDefinitions.keySet()) {
			result.add((T) globalPropertyId);
		}
		return result;
	}

	/**
	 * Returns the property value for the given {@link GlobalPropertyId}.
	 * 
	 * @throws ContractException
	 * 
	 * 
	 *             <li>{@linkplain GlobalError#NULL_GLOBAL_PROPERTY_ID}</li> if
	 *             the global property id is null
	 *             <li>{@linkplain GlobalError#UNKNOWN_GLOBAL_PROPERTY_ID}</li>
	 *             if the global property id is known
	 */
	@SuppressWarnings("unchecked")
	public <T> T getGlobalPropertyValue(final GlobalPropertyId globalPropertyId) {
		validategGlobalPropertyIdExists(data, globalPropertyId);
		return (T) data.globalPropertyValues.get(globalPropertyId);
	}

	private static void validategGlobalPropertyIdExists(final Data data, final GlobalPropertyId globalPropertyId) {
		if (globalPropertyId == null) {
			throw new ContractException(GlobalError.NULL_GLOBAL_PROPERTY_ID);
		}
		if (!data.globalPropertyDefinitions.containsKey(globalPropertyId)) {
			throw new ContractException(GlobalError.UNKNOWN_GLOBAL_PROPERTY_ID, globalPropertyId);
		}
	}

}
