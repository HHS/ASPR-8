package lesson.plugins.model.support;

import org.apache.commons.math3.random.RandomGenerator;

import plugins.personproperties.support.PersonPropertyId;

public enum PersonProperty implements PersonPropertyId {
	AGE, VACCINATED, VACCINE_SCHEDULED, DISEASE_STATE, CONTACT_COUNT;

	public static PersonProperty getRandomPersonProperty(RandomGenerator randomGenerator) {
		return PersonProperty.values()[randomGenerator.nextInt(PersonProperty.values().length)];
	}

	public Object getRandomPersonPropertyValue(RandomGenerator randomGenerator) {
		switch (this) {
		case AGE:
			return randomGenerator.nextInt(10);			
		case CONTACT_COUNT:
			return randomGenerator.nextInt(10);			
		case DISEASE_STATE:
			return DiseaseState.getRandomDiseaseState(randomGenerator);
		case VACCINATED:
			return randomGenerator.nextBoolean();
		case VACCINE_SCHEDULED:
			return randomGenerator.nextBoolean();
		default:
			throw new RuntimeException("unhandled case "+this);
		}
	}
}
