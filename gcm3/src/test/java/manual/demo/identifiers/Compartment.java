package manual.demo.identifiers;

import plugins.compartments.support.CompartmentId;

public enum Compartment implements CompartmentId {
	SUSCEPTIBLE, EXPOSED, INFECTED, DEAD, IMMUNE;
}
