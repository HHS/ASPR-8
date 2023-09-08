package gov.hhs.aspr.ms.gcm.lessons.plugins.model.support;

import gov.hhs.aspr.ms.gcm.plugins.globalproperties.support.GlobalPropertyId;

public enum GlobalProperty implements GlobalPropertyId {
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
	TELEWORK_INFECTION_THRESHOLD, // the total infection density that triggers
									// some work places to institute telework
									// mode
	TELEWORK_PROBABILTY, // the probability that a work place will convert to
							// telework mode once telework is allowed
	SCHOOL_COHORT_INFECTION_THRESHOLD, // the infection density within a school
										// that triggers the school moving to a
										// split cohort
	SCHOOL_CLOSURE_INFECTION_THRESHOLD,// the infection density within a school
										// cohort that triggers the closure of
										// that cohort.
}
