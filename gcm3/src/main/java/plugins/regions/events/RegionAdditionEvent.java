package plugins.regions.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import util.errors.ContractException;
/**
 * An event indicating that a region has been added
 * 
 * @author Shawn Hatch
 *
 */

@Immutable
public class RegionAdditionEvent implements Event{
	
	private final RegionId regionId;

	/**
	 * Constructs the event
	 * 
	 * @throws ContractException
	 * <li>{@linkplain RegionError#NULL_REGION_ID} if the region id is null</li>
	 * 
	 */
	public RegionAdditionEvent(RegionId regionId) {
		if(regionId == null) {
			throw new ContractException(RegionError.NULL_REGION_ID);
		}
		this.regionId = regionId;
	}

	/**
	 * Returns the added region id.
	 */
	public RegionId getRegionId() {
		return regionId;
	}
	
	

}
