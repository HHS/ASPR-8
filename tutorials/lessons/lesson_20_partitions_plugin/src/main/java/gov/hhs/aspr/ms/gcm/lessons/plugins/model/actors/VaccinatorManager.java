package gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.GlobalProperty;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.VaccinatorType;
import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;

public class VaccinatorManager {
	public void init(ActorContext actorContext) {
		GlobalPropertiesDataManager globalPropertiesDataManager = actorContext.getDataManager(GlobalPropertiesDataManager.class);
		VaccinatorType vaccinatorType =
		globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.VACCINATOR_TYPE);
		switch (vaccinatorType) {
		case PARTITION:
			actorContext.addActor(new PartitionVaccinator()::init);
			break;
		case EVENT:
			actorContext.addActor(new EventVaccinator()::init);
			break;
		case INSPECTION:
			actorContext.addActor(new InspectionVaccinator()::init);
			break;
		default:
			throw new RuntimeException("unhandled case "+vaccinatorType);						
		}
	}
}
