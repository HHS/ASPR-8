package gov.hhs.aspr.ms.gcm.lessons.plugins.model.support;

import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.support.GlobalPropertyId;

public enum GlobalProperty implements GlobalPropertyId {

	VACCINATOR_TYPE,//the type of vaccinator used by the model--of type VaccinatorType
	VACCINATIONS_PER_DAY,//number of vaccinations per day allowed
	TRANSMISSION_PROBABILITY,//base probability of disease transmission per infectious contact
	INFECTIOUS_CONTACT_RATE,//number of potentially infectious contacts per day for an infectious person
	MINIMUM_INFECTIOUS_PERIOD,//minimum number of days that a person is infectious
	MAXIMUM_INFECTIOUS_PERIOD,//maximum number of days that a person is infectious
	INITIAL_INFECTION_COUNT, // the number of people who are infectious at the beginning of the simulation
	INTER_VACCINATION_DELAY_TIME,//the number of days require between vaccination
	POPULATION_SIZE, // the initial size of the population
	;
}
