package lesson.plugins.vaccine;

/**
 * Represents the initial vaccine count for a person 
 * @author Shawn Hatch
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
