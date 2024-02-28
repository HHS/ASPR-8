package gov.hhs.aspr.ms.gcm.lessons.plugins.vaccine;

/**
 * Represents the initial vaccine count for a person 
 *
 */

import net.jcip.annotations.Immutable;

@Immutable
public final class VaccineInitialization {
	private final int vaccineCount;

	public VaccineInitialization(int vaccineCount) {
		super();
		this.vaccineCount = vaccineCount;
	}

	public int getVaccineCount() {
		return vaccineCount;
	}

}
