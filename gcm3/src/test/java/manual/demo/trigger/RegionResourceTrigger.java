package manual.demo.trigger;

import net.jcip.annotations.Immutable;
import plugins.regions.support.RegionId;
import plugins.resources.support.ResourceId;

@Immutable
public class RegionResourceTrigger {
	private final RegionId regionId;
	private final ResourceId resourceId;
	private final double threshold;

	public double getThreshold() {
		return threshold;
	}

	public RegionId getRegionId() {
		return regionId;
	}

	public ResourceId getResourceId() {
		return resourceId;
	}

	public RegionResourceTrigger(RegionId regionId, ResourceId resourceId, double threshold) {
		super();
		this.regionId = regionId;
		this.resourceId = resourceId;
		this.threshold = threshold;
	}
}
