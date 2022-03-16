package plugins.people.actors;

import nucleus.ActorContext;
import nucleus.NucleusError;
import nucleus.util.ContractException;
import plugins.people.PeoplePluginData;
import plugins.people.PersonDataManager;
import plugins.people.support.BulkPersonConstructionData;

public class PeopleLoader {

	private final PeoplePluginData peoplePluginData;

	public PeopleLoader(PeoplePluginData peoplePluginData) {
		if (peoplePluginData == null) {
			throw new ContractException(NucleusError.NULL_PLUGIN_DATA);
		}
		this.peoplePluginData = peoplePluginData;

	}

	public void init(ActorContext actorContext) {
		PersonDataManager personDataManager = actorContext.getDataManager(PersonDataManager.class).get();
		for (BulkPersonConstructionData bulkPersonConstructionData : peoplePluginData.getBulkPersonConstructionDatas()) {
			personDataManager.addBulkPeople(bulkPersonConstructionData);			
		}
	}
}
