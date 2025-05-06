package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import gov.hhs.aspr.ms.util.errors.ContractError;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 */
public enum NucleusError implements ContractError {
	ACCESS_VIOLATION("A contributed behavior is accessing locked state during a state change"),//
	AMBIGUOUS_DATA_MANAGER_CLASS("Multiple data manager matches found"),//
	AMBIGUOUS_PLUGIN_DATA_BUILDER_CLASS("Multiple plugin data builder matches found"),//
	AMBIGUOUS_PLUGIN_DATA_CLASS("Multiple plugin data object matches found"),//
	CIRCULAR_PLUGIN_DEPENDENCIES("Circular plugin dependencies were found"),//
	DATA_MANAGER_ACCESS_VIOLATION("A data manager is attempting to access another data manager that is incompatible with the plugin dependencies"),//
	DATA_MANAGER_ATTEMPTING_MUTATION("A data manager is attempting to mutate data state after event flow has stopped"),//
	DATA_MANAGER_DUPLICATE_INITIALIZATION("Data manager was already initialized"),//
	DATA_MANAGER_INITIALIZATION_FAILURE("Data manager base class was not properly initialized, be sure to call super()"),//
	DIMENSION_LABEL_MISMATCH("The number of scenario labels provided by a dimension level do not match the number of labels in the dimension's meta data"),//
	DUPLICATE_DATA_MANAGER_TYPE("Duplicate data manager type"),// 
	DUPLICATE_DIMENSION_LEVEL_NAME("Duplicate Dimension Level Name"),//
	DUPLICATE_EVENT_SUBSCRIPTION("An event subscription duplicates an existing event subscription"),//
	DUPLICATE_EXPERIMENT_OPEN("Duplicate opening of experiment"),//
	DUPLICATE_PLUGIN("There are two or more plugins with the same id"),//
	INCOMPATIBLE_SCEANARIO_PROGRESS("The scenario progress file is incompatible with the current experiment"),//
	INVALID_DIMENSION_LEVEL("Invalid Dimension Level"),//
	INVALID_PLAN_ARRIVAL_ID("The given plan id is invalid. It either needs to be >=0, or -1, and nothing else"),//
	MISSING_PLUGIN("A plugin is missing"),//
	MISSING_SIM_HALT_TIME("Simulation halt time must be set when simulation state is being recorded at the end of a simulation run"),//
	NEGATIVE_THREAD_COUNT("Negative thread count"),//
	NON_EXISTANT_SCEANARIO_PROGRESS("The scenario progress file does not exist, but is required when continuation from progress file is chosen"),//
	NULL_ACTOR_CONTEXT_CONSUMER("Null actor context consumer"),//
	NULL_ACTOR_ID("Null actor id"),//
	NULL_BASE_DATE("Null base date"),//
	NULL_CLASS_REFERENCE("Null class reference"),//
	NULL_DATA_MANAGER("Null data manager"),//
	NULL_DATA_MANAGER_CLASS("Null data manager class"),//
	NULL_DATA_MANAGER_CONTEXT_CONSUMER("Null data manager context consumer"),//
	NULL_DIMENSION_LEVEL_NAME("Null Dimension level name"), //
	NULL_EVENT("Event is null"),//
	NULL_EVENT_CLASS("Null event class"),//
	NULL_EVENT_CONSUMER("Null event consumer"),//
	NULL_EVENT_FILTER("Null event filter"),//
	NULL_EXPERIMENT_CONTEXT_CONSUMER("Null experiment context consumer"),//
	NULL_FUNCTION("Null function"),//
	NULL_FUNCTION_ID("Null function id"),//
	NULL_FUNCTION_VALUE("Null event function value"),//
	NULL_IDENTIFIABLE_FUNCTION("Null identifiable function"),//
	NULL_META_DATA("Null meta data"),//
	NULL_OUTPUT_HANDLER("Null output item handler"),//
	NULL_OUTPUT_ITEM("Null output"),//
	NULL_PLAN("Null plan"),//
	NULL_PLAN_CONSUMER("Null plan consumer"),//
	NULL_PLUGIN("Null plugin"),//
	NULL_PLUGIN_CONTEXT("Null plugin context"),//
	NULL_PLUGIN_DATA("Null plugin data"),//
	NULL_PLUGIN_DATA_CLASS("Null plugin data class"),//
	NULL_PLUGIN_ID("Null plugin id"),//
	NULL_PLUGIN_INITIALIZER("Null plugin initializer"),//
	NULL_PLUGINS("Null collection of plugins"),//
	NULL_REPORT_CONTEXT_CONSUMER("Null report context consumer"),//
	NULL_SCENARIO_ID("Null scenario id"),//
	NULL_SCENARIO_PROGRESS_FILE("Null scenario progress file"),//
	NULL_SIMULATION_CONTEXT("Null simulation context"),//
	NULL_SIMULATION_STATE("Null simulation state data"),//
	OBSERVATION_EVENT_IMPROPER_RELEASE("An observation event is being released during a mutation by a data manager without the use of a corresponding mutation event"),//
	PAST_PLANNING_TIME("Plan execution time is in the past"),//
	PLANNING_QUEUE_ACTIVE("The planning queue is still active and plans cannot be retrieved"),//
	PLANNING_QUEUE_CLOSED("The planning phase of the simulation is over and plans may not be added"),//
	PLUGIN_INITIALIZATION_CLOSED("Plugin context is no longer valid"),//
	REPEATED_EXECUTION("Attempted repeat execution of simulation engine"),//
	REPORT_ATTEMPTING_MUTATION("A report is attempting to mutate data state"),//
	SCENARIO_CANNOT_BE_EXECUTED("Scenario cannot be executed"),//
	SIM_HALT_TIME_TOO_EARLY("When a simulation halt time is non-negative, it must be greater than or equal to the start time of the simulation"),//
	UNCLOSABLE_EXPERIMENT("Cannot close an experiment not in the open state"),//
	UNKNOWN_ACTOR_ID("Actor id does not correspond to a known actor"),//
	UNKNOWN_DATA_MANAGER("Unknown data manager"),//
	UNKNOWN_DIMENSION_LEVEL_NAME("The given level name is not known"),//
	UNKNOWN_FUNCTION_ID("Unknown event function id"),//
	UNKNOWN_PLUGIN_DATA_BUILDER_CLASS("The plugin data builder class was not found"),//
	UNKNOWN_PLUGIN_DATA_CLASS("The plugin data class was not found"),//
	UNKNOWN_SCENARIO_ID("Unknown scenario id"),//
	UNREADABLE_SCEANARIO_PROGRESS("The scenario progress file is unreadable"),;//

	private final String description;

	private NucleusError(final String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
