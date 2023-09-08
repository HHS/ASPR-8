package gov.hhs.aspr.ms.gcm.lessons.plugins.model.support;

import org.apache.commons.math3.random.RandomGenerator;

public enum AgeGroup {

	CHILD, //
	ADULT_18_44, // adults 18 to 29 inclusive
	ADULT_45_64, // adults 30 to 54 inclusive
	SENIOR;// adults 55 and over

	public static AgeGroup getAgeGroup(int age) {
		if (age < 18) {
			return CHILD;
		}
		if (age < 45) {
			return ADULT_18_44;
		}
		if (age < 65) {
			return ADULT_45_64;
		}
		return SENIOR;
	}

	public static int getRandomAge(RandomGenerator randomGenerator) {
		double draw = randomGenerator.nextDouble();
		if (draw < 0.222) {
			// Under 18 years 22.2% (2021)
			return randomGenerator.nextInt(18);
		}
		if (draw < 0.581) {
			// 18–44 years 35.9% (2021)
			return randomGenerator.nextInt(27) + 18;
		}
		if (draw < 0.833) {
			// 45–64 years 25.2% (2021)
			return randomGenerator.nextInt(10) + 45;
		}
		// 65 and over 16.8% (2021)
		return randomGenerator.nextInt(21) + 65;

	}
}
