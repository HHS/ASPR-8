package nucleus;

import util.ContractError;
import util.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 * 
 * @author Shawn Hatch
 *
 */
public enum NucleusError implements ContractError {

	ACCESS_VIOLATION("A contributed behavior is accessing locked state during a state change"),
	AGENT_ID_IN_USE("Agent id is currently in use by another agent"),
	CIRCULAR_PLUGIN_DEPENDENCIES("Circular plugin dependencies were found"),
	DUPLICATE_LABELER_ID_IN_EVENT_LABELER("Duplicate labeler id in labeler"),
	DUPLICATE_PLAN_KEY("There is an existing plan currently scheduled with the same key"),
	DUPLICATE_PLUGIN_ID("Duplicate plugin id"),
	DUPLICATE_RESOLVER_ID("Duplicate resolver id"),
	LABLER_GENERATED_LABEL_WITH_INCORRECT_EVENT_CLASS("Event labler generated a label with an incorrect event class"),
	LABLER_GENERATED_LABEL_WITH_INCORRECT_ID("Event labler generated a label with an incorrect event labeler id"),
	LABLER_GENERATED_LABEL_WITH_INCORRECT_PRIMARY_KEY("Event labler generated a label with an incorrect primary key"),
	MISSING_PLUGIN("A required plugin is missing"),
	NEGATIVE_AGENT_ID("Negative agent id"),
	NULL_AGENT_CONTEXT_CONSUMER("Null agent context consumer"),
	NULL_AGENT_ID("Null agent id"),
	NULL_DATA_VIEW("Null data view"),
	NULL_EVENT("Event is null"),
	NULL_EVENT_CLASS("Null event class"),
	NULL_EVENT_CLASS_IN_EVENT_LABEL("Event label returns a null event class"),
	NULL_EVENT_CLASS_IN_EVENT_LABELER("Event labeler returns a null event class"),
	NULL_EVENT_CONSUMER("Null event consumer"),
	NULL_EVENT_LABEL("Null event label"),
	NULL_EVENT_LABELER("Null event labeler"),
	NULL_LABELER_ID_IN_EVENT_LABEL("Event label returns a null event labeler id"),
	NULL_LABELER_ID_IN_EVENT_LABELER("Event labeler returns a null id"),
	NULL_PLAN("Null plan"),
	NULL_PLAN_KEY("Null planning key"),
	NULL_PLUGIN_ID("Null plugin id"),
	NULL_PLUGIN_CONTEXT_CONSUMER("Null plugin context consumer"),
	NULL_PRIMARY_KEY_VALUE("Null primary key value"),
	NULL_REPORT_CONTEXT_CONSUMER("Null report context consumer"),
	NULL_REPORT_ID("Null report id"),
	NULL_RESOLVER_ID("Null resolver id"),
	PAST_PLANNING_TIME("Plan execution time is in the past"),
	PLUGIN_INITIALIZATION_CLOSED("Plugin context is no longer valid"),
	REPEATED_EXECUTION("Attempted repeat execution of simulation engine"),
	REPORT_ID_IN_USE("Report id is currently in use by another report"),
	UNKNOWN_AGENT_ID("Agent id does not correspond to a known agent"),
	UNKNOWN_EVENT_LABEL("An event label does not match a registered event label"),
	UNKNOWN_EVENT_LABELER("The labeler id an event label does not match a registered event labeler"),
	NULL_CONTEXT("Null context"),
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
