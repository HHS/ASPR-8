package nucleus;

import util.errors.ContractError;
import util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 * 
 *
 */
public enum NucleusError implements ContractError {
	ACCESS_VIOLATION("A contributed behavior is accessing locked state during a state change"),
	DATA_MANAGER_ACCESS_VIOLATION("A data manager is attempting to access another data manager that is incompatible with the plugin dependencies"),
	AMBIGUOUS_DATA_MANAGER_CLASS("Multiple data manager matches found"),
	AMBIGUOUS_PLUGIN_DATA_BUILDER_CLASS("Multiple plugin data builder matches found"),
	AMBIGUOUS_PLUGIN_DATA_CLASS("Multiple plugin data object matches found"),
	CIRCULAR_PLUGIN_DEPENDENCIES("Circular plugin dependencies were found"),
	DATA_MANAGER_DUPLICATE_INITIALIZATION("Data manager was already initialized"),
	DATA_MANAGER_INITIALIZATION_FAILURE("Data manager base class was not properly initialized, be sure to call super()"),
	DIMENSION_LABEL_MISMATCH("The number of scenario labels provided by a dimension level do not match the number of labels in the dimension's meta data"),	
	DUPLICATE_DATA_MANAGER_TYPE("Duplicate data manager type"),
	DUPLICATE_DATA_VIEW_TYPE("Duplicate data view type"),
	DUPLICATE_EVENT_SUBSCRIPTION("An event subscription duplicates an existing event subscription"),
	DUPLICATE_META_SUBSCRIPTION("An meta subscription duplicates an existing meta subscription"),
	DUPLICATE_PLAN_PRIORITY("A plan priority was duplicated during the initialization phase of the simulation when plans from a previous simulation execution can be added to the current simulation"),
	DUPLICATE_EXPERIMENT_OPEN("Duplicate opening of experiment"),
	DUPLICATE_LABELER_ID_IN_EVENT_LABELER("Duplicate labeler id in labeler"),
	DUPLICATE_PLAN_KEY("There is an existing plan currently scheduled with the same key"),
	DUPLICATE_PLUGIN("There are two or more plugins with the same id"),	
	INCOMPATIBLE_SCEANARIO_PROGRESS("The scenario progress file is incompatible with the current experiment"),
	LABLER_GENERATED_LABEL_WITH_INCORRECT_EVENT_CLASS("Event labler generated a label with an incorrect event class"),
	LABLER_GENERATED_LABEL_WITH_INCORRECT_ID("Event labler generated a label with an incorrect event labeler id"),
	LABLER_GENERATED_LABEL_WITH_INCORRECT_PRIMARY_KEY("Event labler generated a label with an incorrect primary key"),
	MISSING_PLUGIN("A plugin is missing"),
	NEGATIVE_THREAD_COUNT("Negative thread count"),
	NON_EXISTANT_SCEANARIO_PROGRESS("The scenario progress file does not exist, but is required when continuation from progress file is chosen"),
	NULL_ACTOR_CONTEXT_CONSUMER("Null actor context consumer"),	
	NULL_REPORT_CONTEXT_CONSUMER("Null report context consumer"),
	NULL_ACTOR_ID("Null actor id"),
	NULL_BASE_DATE("Null base date"),
	NULL_DATA_MANAGER("Null data manager"),
	NULL_DATA_VIEW("Null data view"),
	NULL_DATA_MANAGER_CLASS("Null data manager class"),
	NULL_DATA_VIEW_CLASS("Null data view class"),
	NULL_DATA_MANAGER_CONTEXT_CONSUMER("Null data manager context consumer"),
	NULL_DATA_MANAGER_STATE_CONTEXT_CONSUMER("Null data manager state context consumer"),
	NULL_REPORT_STATE_CONTEXT_CONSUMER("Null report state context consumer"),
	NULL_ACTOR_STATE_CONTEXT_CONSUMER("Null actor state context consumer"),
	NULL_CLASS_REFERENCE("Null class reference"),
	NULL_EVENT("Event is null"),	
	NULL_EVENT_CLASS("Null event class"),
	NULL_EVENT_CONSUMER("Null event consumer"),
	NULL_EVENT_LABEL("Null event label"),
	NULL_EVENT_LABEL_FUNCTION("Null event label function"),
	NULL_EVENT_LABEL_KEY("Null event label key"),
	NULL_EVENT_LABELER("Null event labeler"),
	NULL_EVENT_LABELER_ID("Null event labeler id"),
	NULL_EXPERIMENT_CONTEXT_CONSUMER("Null experiment context consumer"),
	NULL_LABELER_ID_IN_EVENT_LABEL("Event label returns a null event labeler id"),
	NULL_LABELER_ID_IN_EVENT_LABELER("Event labeler returns a null id"),
	NULL_META_DATA("Null meta data"),
	NULL_OUTPUT_HANDLER("Null output item handler"),
	NULL_OUTPUT_ITEM("Null output"),
	NULL_PLAN("Null plan"),
	NULL_PLAN_DATA("Null plan data"),
	NULL_PLAN_QUEUE_DATA("Null plan queue data"),
	NULL_PLANNER("Null planner type"),
	NULL_PLAN_KEY("Null planning key"),
	NULL_PLUGIN("Null plugin"),
	NULL_PLUGINS("Null collection of plugins"),
	NULL_PLUGIN_CONTEXT("Null plugin context"),
	NULL_PLUGIN_DATA("Null plugin data"),
	NULL_PLUGIN_DATA_BUILDER("A null plugin data builder instance was added to the context"),
	NULL_PLUGIN_DATA_CLASS("Null plugin data class"),
	NULL_PLUGIN_ID("Null plugin id"),
	NULL_PLUGIN_INITIALIZER("Null plugin initializer"),
	NULL_PRIMARY_KEY_VALUE("Null primary key value"),
	NULL_SCENARIO_ID("Null scenario id"),
	NULL_SCENARIO_PROGRESS_FILE("Null scenario progress file"),
NULL_SIMULATION_CONTEXT("Null simulation context"),
	NULL_SIMULATION_TIME("Null simulation time"),
	PAST_PLANNING_TIME("Plan execution time is in the past"),
	PLANNING_QUEUE_CLOSED("The planning phase of the simulation is over and plans may not be added"),
	PLANNING_QUEUE_ARRIVAL_INVALID("The planning queue arrival id must exceed the arrival id values for all stored plans"),
	PLANNING_QUEUE_TIME("A planning time for a stored plan happens before the start time of the simulation"),
	PLUGIN_INITIALIZATION_CLOSED("Plugin context is no longer valid"),
	REPEATED_EXECUTION("Attempted repeat execution of simulation engine"),
	REPORT_ATTEMPTING_MUTATION("A report is attempting to mutate data state"),
	DATA_MANAGER_ATTEMPTING_MUTATION("A data manager is attempting to mutate data state after event flow has stopped"),
	SCENARIO_CANNOT_BE_EXECUTED("Scenario cannot be executed"),
	SCENARIO_FAILED("Scenario failed to complete execution"),	
	MISSING_SIM_HALT_TIME("Simulation halt time must be set to a non-negative value when simulation state is being recorded at the end of a simulation run"),
	SIM_HALT_TIME_TOO_EARLY("When a simulation halt time is non-negative, it must be greater than or equal to the start time of the simulation"),
	TERMINAL_PLAN_DATA_ACCESS_VIOLATION("There is no access to terminal plan data until the simulation closes"),
	UNCLOSABLE_EXPERIMENT("Cannot close an experiment not in the open state"),
	UNKNOWN_ACTOR_ID("Actor id does not correspond to a known actor"),
	UNKNOWN_DATA_MANAGER("Unknown data manager"),
	UNKNOWN_DATA_VIEW("Unknown data view"),
	UNKNOWN_EVENT_LABELER("The labeler id an event label does not match a registered event labeler"),
	UNKNOWN_PLUGIN_DATA_BUILDER_CLASS("The plugin data builder class was not found"),	
	UNKNOWN_SCENARIO_ID("Unknown scenario id"),
	UNREADABLE_SCEANARIO_PROGRESS("The scenario progress file is unreadable"),
	OBSERVATION_EVENT_IMPROPER_RELEASE("An observation event is being released during a mutation by a data manager without the use of a corresponding mutation event"),
	UNKNOWN_FUNCTION_ID("Unknown event function id"),
	NULL_FUNCTION_ID("Null function id"),
	NULL_FUNCTION_VALUE("Null event function value"),
	NULL_FUNCTION("Null function"),
	NULL_IDENTIFIABLE_FUNCTION("Null identifiable function"),
	NULL_EVENT_FILTER("Null event filter"),
	;

	private final String description;

	private NucleusError(final String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
