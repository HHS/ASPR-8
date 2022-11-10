package lesson.plugins.model;

import plugins.personproperties.support.PersonPropertyId;

public enum PersonProperty implements PersonPropertyId {
	IMMUNE,//the person is immune
	INFECTED,//the person is immune
	TREATED_WITH_ANTIVIRAL,//the person received antiviral treatment
	HOSPITALIZED,//the person received hospital treatment
	DEAD_IN_HOSPITAL,//the person dies in the hospital
	DEAD_IN_HOME,//the person dies in the home
	;
}
