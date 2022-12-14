package lesson.plugins.model.support;

import org.apache.commons.math3.random.RandomGenerator;

public enum DiseaseState {
	IMMUNE, // the person is immune
	SUSCEPTIBLE,
	INFECTIOUS, // the person is infected
	RECOVERED;
	
	public static DiseaseState getRandomDiseaseState(RandomGenerator randomGenerator) {
		return DiseaseState.values()[randomGenerator.nextInt(DiseaseState.values().length)];
	}

}
