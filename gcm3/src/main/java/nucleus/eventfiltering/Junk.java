package nucleus.eventfiltering;

import org.apache.commons.math3.util.Pair;

import nucleus.eventfiltering.IdentifiedFunction.Builder;
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
		Builder<PersonPropertyUpdateEvent> functionBuilder = IdentifiedFunction.builder(PersonPropertyUpdateEvent.class);

		IdentifiedFunction<PersonPropertyUpdateEvent> identifiedFunction = //
				functionBuilder//
								.setFunctionId(EventFunctionIds.PERSON_PROPERTY_ID)//
								.setEventFunction((e) -> e.getPersonPropertyId())//
								.build();

		filterBuilder.addFunctionValuePair(identifiedFunction, personPropertyId);
		identifiedFunction = //
				functionBuilder//
								.setFunctionId(EventFunctionIds.REGION_ID)//
								.setEventFunction((e) -> regionsDataManager.getPersonRegion(e.getPersonId()))//
								.build();

		filterBuilder.addFunctionValuePair(identifiedFunction, regionId);
		return filterBuilder.build();
	}

	public static void main(String[] args) {
		EventFilter<PersonPropertyUpdateEvent> eventFilter = new Junk().getEventFilter(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, TestRegionId.REGION_1);
		for (Pair<IdentifiedFunction<PersonPropertyUpdateEvent>, Object> pair : eventFilter.getEventFunctionPairs()) {
			IdentifiedFunction<PersonPropertyUpdateEvent> identifiedFunction = pair.getFirst();
			System.out.println(identifiedFunction.getEventFunctionId());			
			Object value = pair.getSecond();
			System.out.println("value = "+value);
		}
	}
}
