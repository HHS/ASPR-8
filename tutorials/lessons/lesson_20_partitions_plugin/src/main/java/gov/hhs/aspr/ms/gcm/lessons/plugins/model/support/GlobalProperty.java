package gov.hhs.aspr.ms.gcm.lessons.plugins.model.support;

import gov.hhs.aspr.ms.gcm.plugins.globalproperties.support.GlobalPropertyId;

public enum GlobalProperty implements GlobalPropertyId {

	VACCINATIONS_PER_DAY,
	TRANSMISSION_PROBABILTY,
	INFECTIOUS_CONTACT_RATE,
	MINIMUM_INFECTIOUS_PERIOD,
	MAXIMUM_INFECTIOUS_PERIOD,
	INITIAL_INFECTION_COUNT, // the number of people who are infectious at the begining of the simulation 
	POPULATION_SIZE, // the initial size of the population
	;
}
