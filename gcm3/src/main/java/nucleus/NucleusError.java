package nucleus;

import nucleus.util.ContractError;
import nucleus.util.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.  
 * 
 * @author Shawn Hatch
 *
 */
public enum NucleusError implements ContractError {

	AMBIGUOUS_DATA_MANAGER_CLASS("Multiple data manager matches found"),
	DATA_MANAGER_DUPLICATE_INITIALIZATION("Data manager was already initialized"),
	DATA_MANAGER_INITIALIZATION_FAILURE("Data manager base class was not properly initialized, be sure to call super()"),
	CIRCULAR_PLUGIN_DEPENDENCIES("Circular plugin dependencies were found"),	
	DUPLICATE_DATA_MANAGER_TYPE("Duplicate data manager type"),
	DUPLICATE_LABELER_ID_IN_EVENT_LABELER("Duplicate labeler id in labeler"),
	DUPLICATE_PLAN_KEY("There is an existing plan currently scheduled with the same key"),
	LABLER_GENERATED_LABEL_WITH_INCORRECT_EVENT_CLASS("Event labler generated a label with an incorrect event class"),
	LABLER_GENERATED_LABEL_WITH_INCORRECT_ID("Event labler generated a label with an incorrect event labeler id"),
	LABLER_GENERATED_LABEL_WITH_INCORRECT_PRIMARY_KEY("Event labler generated a label with an incorrect primary key"),
	NULL_ACTOR_CONTEXT_CONSUMER("Null actor context consumer"),
	NULL_ACTOR_ID("Null actor id"),
	NULL_DATA_MANAGER("Null data manager"),
	NULL_DATA_MANAGER_CLASS("Null data manager class"),
	NULL_EVENT("Event is null"),
	NULL_EVENT_CLASS("Null event class"),
	NULL_EVENT_CLASS_IN_EVENT_LABEL("Event label returns a null event class"),
	NULL_EVENT_CLASS_IN_EVENT_LABELER("Event labeler returns a null event class"),
	NULL_EVENT_CONSUMER("Null event consumer"),
	NULL_EVENT_LABEL("Null event label"),
	NULL_EVENT_LABELER("Null event labeler"),
	NULL_LABELER_ID_IN_EVENT_LABEL("Event label returns a null event labeler id"),
	NULL_LABELER_ID_IN_EVENT_LABELER("Event labeler returns a null id"),
	NULL_OUTPUT_ITEM("Null output"),
	NULL_PLAN("Null plan"),
	NULL_PLAN_KEY("Null planning key"),
	NULL_PLUGIN_DATA("Null plugin data"),
	NULL_PLUGIN_ID("Null plugin id"),
	NULL_PLUGIN("Null plugin"),
	NULL_PRIMARY_KEY_VALUE("Null primary key value"),
	NULL_SCENARIO_ID("Null scenario id"),
	PAST_PLANNING_TIME("Plan execution time is in the past"),
	PLUGIN_INITIALIZATION_CLOSED("Plugin context is no longer valid"),
	REPEATED_EXECUTION("Attempted repeat execution of simulation engine"),
	UNKNOWN_ACTOR_ID("Actor id does not correspond to a known actor"),
	UNKNOWN_EVENT_LABELER("The labeler id an event label does not match a registered event labeler"),
	UNKNOWN_SCENARIO_ID("Unknown scenario id"),
	SCENARIO_CANNOT_BE_EXECUTED("scenario cannot be executed"),
	NON_EXISTANT_SCEANARIO_PROGRESS("The scenario progress file does not exist, but is required when continuation from progress file is chosen"),
	UNREADABLE_SCEANARIO_PROGRESS("The scenario progress file is unreadable"),
	INCOMPATIBLE_SCEANARIO_PROGRESS("The scenario progress file is incompatible with the current experiment"),
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
