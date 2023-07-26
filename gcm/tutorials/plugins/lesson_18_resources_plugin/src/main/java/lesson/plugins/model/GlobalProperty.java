package lesson.plugins.model;

import gov.hhs.aspr.ms.gcm.plugins.globalproperties.support.GlobalPropertyId;

public enum GlobalProperty implements GlobalPropertyId {
	SUSCEPTIBLE_POPULATION_PROPORTION, // the fraction of the population that is susceptible
	MAXIMUM_SYMPTOM_ONSET_TIME, // the last time where any person will have onset of symptoms
	ANTIVIRAL_COVERAGE_TIME, // the amount of time for the antiviral to be effective
	ANTIVIRAL_SUCCESS_RATE, // the probability that the antiviral will be effective
	HOSPITAL_SUCCESS_WITH_ANTIVIRAL, // the probability that hospital treatment will be effective for people who
										// previously had antiviral treatment
	HOSPITAL_SUCCESS_WITHOUT_ANTIVIRAL, // the probability that hospital treatment will be effective for people who
										// previously had no antiviral treatment
	HOSPITAL_STAY_DURATION_MIN, // The minimum duration of a hospital stay
	HOSPITAL_STAY_DURATION_MAX, // The maximum duration of a hospital stay
	POPULATION_SIZE, // the number of people across all regions. Regions will not be uniformly
						// populated.
	HOSPITAL_BEDS_PER_PERSON, // The number of hospital beds per person on average stored in the regions.
	ANTIVIRAL_DOSES_PER_PERSON,// The number of antiviral doses per person on average stored in the regions.
	;
}
