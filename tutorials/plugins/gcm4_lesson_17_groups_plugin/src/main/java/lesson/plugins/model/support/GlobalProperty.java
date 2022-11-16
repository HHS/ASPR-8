package lesson.plugins.model;

import plugins.globalproperties.support.GlobalPropertyId;

public enum GlobalProperty implements GlobalPropertyId {
	SUSCEPTIBLE_POPULATION_PROPORTION,// the fraction of the population that is susceptible
	INITIAL_INFECTIONS,
	MIN_INFECTIOUS_PERIOD,
	MAX_INFECTIOUS_PERIOD,
	POPULATION_SIZE,
	CHILD_POPULATION_PROPORTION,
	SENIOR_POPULATION_PROPORTION,
	R0,
	AVERAGE_HOME_SIZE,
	AVERAGE_SCHOOL_SIZE,
	AVERAGE_WORK_SIZE,
	;
}
