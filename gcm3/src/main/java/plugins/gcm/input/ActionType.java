package plugins.gcm.input;

/**
 * An enumeration that corresponds to types of data actions used in scenario and
 * experiment construction. Many of the actions in this enumeration correspond
 * directly with mutations allowed by the environment. However, many do not and
 * represent the establishment of the initial state of the simulation such as
 * the setting of various memory policies and the definitions of properties.
 * 
 * @author Shawn Hatch
 *
 */
public enum ActionType {

	// stochastics plugin
	RANDOM_NUMBER_GENERATOR_ID_ADDITION("random number generator id addition"),

	// components plugin

	// properties plugin

	// reports plugin
	REPORT_ID_ADDITION("report_id_addition"),

	// globals plugin
	GLOBAL_COMPONENT_ID_ADDITION("global_component_id_addition"),
	GLOBAL_PROPERTY_VALUE_ASSIGNMENT("global_property"), // experiment
	GLOBAL_PROPERTY_DEFINITION("global_property_definition"),

	// people plugin
	PERSON_ID_ADDITION("person_id_addition"),

	// partitions plugin

	// regions plugin
	REGION_PROPERTY_DEFINITION("region_property_definition"),
	REGION_PROPERTY_VALUE_ASSIGNMENT("region_property"), // experiment
	PERSON_REGION_ARRIVAL_TRACKING_ASSIGNMENT("person_region_arrival_tracking_assignment"),
	REGION_COMPONENT_ID_ADDITION("region_component_id_addition"),

	// compartments plugin
	COMPARTMENT_COMPONENT_ID_ADDITION("compartment_component_id_addition"),
	COMPARTMENT_PROPERTY_VALUE_ASSIGNMENT("compartment_property"), // experiment
	PERSON_COMPARTMENT_ARRIVAL_TRACKING_ASSIGNMENT("person_compartment_arrival_tracking_assignment"),
	COMPARTMENT_PROPERTY_DEFINITION("compartment_property_definition"),

	// person properties plugin
	PERSON_PROPERTY_DEFINITION("person_property_definition"),
	PERSON_PROPERTY_VALUE_ASSIGNMENT("person_property"), // experiment

	// groups plugin
	GROUP_MEMBERSHIP_ASSIGNMENT("group_membership_assignment"),
	GROUP_PROPERTY_DEFINITION("group_property_definition"),
	GROUP_PROPERTY_VALUE_ASSIGNMENT("group_property_value_assignment"), // experiment
	GROUP_ID_ADDITION("group_id_addition"),
	GROUP_TYPE_ID_ADDITION("group_type_id_addition"),

	// resources plugin
	RESOURCE_ID_ADDITION("resource_id_addition"),
	RESOURCE_PROPERTY_VALUE_ASSIGNMENT("resource_property"), // experiment
	PERSON_RESOURCE_ASSIGNMENT("person_resource"), // experiment
	REGION_RESOURCE_ASSIGNMENT("region_resource"), // experiment
	RESOURCE_TIME_TRACKING_ASSIGNMENT("resource_time_tracking_assignment"),
	RESOURCE_PROPERTY_DEFINITION("resource_property_definition"),

	// materials plugin
	MATERIAL_ID_ADDITION("material_id_addition"),
	MATERIALS_PRODUCER_PROPERTY_VALUE_ASSIGNMENT("materials_producer_property_value_assignment"), // experiment
	BATCH_PROPERTY_VALUE_ASSIGNMENT("batch_property_value_assignment"), // experiment
	MATERIALS_PRODUCER_RESOURCE_ASSIGNMENT("materials_producer_resource_assignment"), // experiment
	STAGE_MEMBERSHIP_ASSIGNMENT("stage_membership_assignment"),
	BATCH_PROPERTY_DEFINITION("batch_property_definition"),
	MATERIALS_PRODUCER_PROPERTY_DEFINITION("materials_producer_property_definition"),
	MATERIALS_PRODUCER_COMPONENT_ID_ADDITION("materials_producer_component_id_addition"),
	STAGE_ID_ADDITION("stage_id_addition"),
	BATCH_ID_ADDITION("batch_id_addition"),

	// gcm plugin

	;

	private final String descriptor;

	private ActionType(String descriptor) {
		this.descriptor = descriptor;
	}

	@Override
	public String toString() {
		return descriptor;
	}
}