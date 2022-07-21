package nucleus.eventfiltering;

import nucleus.eventfiltering.EventFilter.FunctionValue;
import plugins.personproperties.events.PersonPropertyUpdateEvent;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionId;
import plugins.regions.testsupport.TestRegionId;

public class Junk {

	private static enum EventFunctionIds {
		PERSON_PROPERTY_ID, REGION_ID
	}

	private RegionsDataManager regionsDataManager;

	private EventFilter<PersonPropertyUpdateEvent> getEventFilter(PersonPropertyId personPropertyId, RegionId regionId) {
		EventFilter.Builder<PersonPropertyUpdateEvent> filterBuilder = EventFilter.builder(PersonPropertyUpdateEvent.class);
		filterBuilder.addFunctionValue(EventFunctionIds.PERSON_PROPERTY_ID, e -> e.getPersonPropertyId(), personPropertyId);
		filterBuilder.addFunctionValue(EventFunctionIds.REGION_ID, e -> regionsDataManager.getPersonRegion(e.getPersonId()), regionId);
		return filterBuilder.build();
	}

	public static void main(String[] args) {
		EventFilter<PersonPropertyUpdateEvent> eventFilter = new Junk().getEventFilter(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, TestRegionId.REGION_1);
		for(FunctionValue<PersonPropertyUpdateEvent> functionValue : eventFilter.getFunctionValues()) {
			System.out.println(functionValue.getFunctionId());
			System.out.println(functionValue.getTargetValue());
		}
	}
}
