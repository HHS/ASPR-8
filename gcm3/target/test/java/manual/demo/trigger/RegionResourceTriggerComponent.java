package manual.demo.trigger;

import manual.demo.identifiers.RegionProperty;
import plugins.gcm.agents.AbstractComponent;
import plugins.gcm.agents.Environment;
import plugins.gcm.agents.Plan;
import plugins.people.support.PersonId;
import plugins.regions.support.RegionId;
import plugins.resources.support.ResourceId;

public class RegionResourceTriggerComponent extends AbstractComponent {

	private static class FlagExaminationPlan implements Plan {

	}

	@Override
	public void executePlan(final Environment environment, final Plan plan) {
		examimeFlag(environment);
	}

	@Override
	public void init(Environment environment) {
		TriggerContainer triggerContainer = environment.getGlobalPropertyValue(TriggerContainer.TRIGGER_CONTAINER);
		RegionResourceTrigger regionResourceTrigger = triggerContainer.getTrigger(environment.getCurrentComponentId());
		environment.observeRegionPersonArrival(true, regionResourceTrigger.getRegionId());
		environment.observeRegionResourceChange(true, regionResourceTrigger.getRegionId(), regionResourceTrigger.getResourceId());
		environment.addPlan(new FlagExaminationPlan(), 0);

	}

	private void examimeFlag(final Environment environment) {
		TriggerContainer triggerContainer = environment.getGlobalPropertyValue(TriggerContainer.TRIGGER_CONTAINER);
		RegionResourceTrigger regionResourceTrigger = triggerContainer.getTrigger(environment.getCurrentComponentId());
		RegionId regionId = regionResourceTrigger.getRegionId();
		ResourceId resourceId = regionResourceTrigger.getResourceId();
		double threshold = regionResourceTrigger.getThreshold();

		boolean flag = environment.getRegionPropertyValue(regionId, RegionProperty.FLAG);
		long regionResourceLevel = environment.getRegionResourceLevel(regionId, resourceId);
		int regionPopulationCount = environment.getRegionPopulationCount(regionId);

		if (flag) {
			if ((regionResourceLevel / regionPopulationCount) > threshold) {
				environment.setRegionPropertyValue(regionId, RegionProperty.FLAG, false);
			}
		} else {
			if ((regionResourceLevel / regionPopulationCount) < threshold) {
				environment.setRegionPropertyValue(regionId, RegionProperty.FLAG, true);
			}
		}
	}

	@Override
	public void observeRegionResourceChange(final Environment environment, final RegionId regionId, final ResourceId resourceId) {
		examimeFlag(environment);
	}

	@Override
	protected void observePersonRegionChange(final Environment environment, final RegionId previousRegionId, final RegionId currentRegionId, final PersonId personId) {

		examimeFlag(environment);
	}

}
