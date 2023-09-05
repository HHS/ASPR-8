package gov.hhs.aspr.ms.gcm.lessons.plugins.model.support;

import gov.hhs.aspr.ms.gcm.plugins.personproperties.support.PersonPropertyId;

public enum PersonProperty implements PersonPropertyId {
	AGE, // the integer age of a person
	WAITING_FOR_NEXT_DOSE,// boolean indicating that the person should not be vaccinated 
	VACCINATION_COUNT, // boolean vaccination status	
	DISEASE_STATE, // SUSCEPTIBLE, INFECTIOUS or RECOVERED
	;

}
