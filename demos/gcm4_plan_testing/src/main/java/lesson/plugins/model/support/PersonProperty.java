package lesson.plugins.model.support;

import plugins.personproperties.support.PersonPropertyId;

public enum PersonProperty implements PersonPropertyId {
	AGE, // the integer age of a person
	VACCINATED, // boolean vaccination status
	VACCINE_SCHEDULED, // boolean indicating whether a vaccination is scheduled for a person
	DISEASE_STATE, // IMMUNE,SUSCEPTIBLE, INFECTIOUS or RECOVERED
	;
	
}
