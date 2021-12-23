package manual.gettingstarted;

import plugins.regions.support.RegionId;

public final class VaccineInventoryOutputItem {

	private final double time;

	private final long vaccineInventory;

	private final RegionId regionId;

	public VaccineInventoryOutputItem(final double time, final RegionId regionId, final long vaccineInventory) {
		this.time = time;
		this.regionId = regionId;
		this.vaccineInventory = vaccineInventory;
	}

	public long getVaccineInventory() {
		return vaccineInventory;
	}

	public RegionId getRegionId() {
		return regionId;
	}

	public double getTime() {
		return time;
	}

}
