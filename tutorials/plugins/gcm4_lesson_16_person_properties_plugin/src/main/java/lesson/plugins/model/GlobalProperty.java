package lesson.plugins.model;

import plugins.globalproperties.support.GlobalPropertyId;

public enum GlobalProperty implements GlobalPropertyId {
	IMMUNITY_START_TIME, // the time in days until immunity person property is added
	IMMUNITY_PROBABILITY, // the probability that person will be immune when the immunity property is
							// added
	VACCINE_ATTEMPT_INTERVAL, // the maximum time between vaccine attempts
	EDUCATION_ATTEMPT_INTERVAL, // the maximum time between vaccine education attempts
	EDUCATION_SUCCESS_RATE, // the probability of changing the refusal person property per attempt
	POPULATION_SIZE, // the initial size of the population
	VACCINE_REFUSAL_PROBABILITY, // the probability that a person will refuse vaccination at the start of the
									// simulation
	SIMULATION_DURATION,// the total time the simulation will run

	;
}
