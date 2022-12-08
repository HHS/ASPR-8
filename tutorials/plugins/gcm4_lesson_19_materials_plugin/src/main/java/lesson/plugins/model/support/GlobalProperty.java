package lesson.plugins.model.support;

import plugins.globalproperties.support.GlobalPropertyId;

public enum GlobalProperty implements GlobalPropertyId {
	VACCINED_DOSES_PER_PERSON, // The number of vaccine doses per person on
								// average stored in the regions at the begining
								// of the simulation.
	SUSCEPTIBLE_POPULATION_PROPORTION, // the fraction of the population that is
	// susceptible
	INITIAL_INFECTIONS, // the number of adults initially infected
	MIN_INFECTIOUS_PERIOD, // the minimum number of days a person is infectious
	MAX_INFECTIOUS_PERIOD, // the maximum number of days a person is infectious
	POPULATION_SIZE, // the initial size of the population
	CHILD_POPULATION_PROPORTION, // the fraction of the population between the
	// ages of 0 and 18, inclusive
	SENIOR_POPULATION_PROPORTION, // the fraction of the population 65 and older
	R0, // the expected number of people a single person will infect if all
	// contacts are susceptible and transmission success is 100%
	AVERAGE_HOME_SIZE, // the average number of people per household
	AVERAGE_SCHOOL_SIZE, // the average number of student in a school
	AVERAGE_WORK_SIZE, // the average number of people per work place
	COMMUNITY_CONTACT_RATE, // the proportion of contacts that will be randomly
							// chosen from the entire population
	MANUFACTURE_VACCINE,// Triggers vaccine manufacture
	;
}
