package plugins.regions.support;

import net.jcip.annotations.ThreadSafe;
import plugins.components.support.ComponentId;

/**
 * Marker interface for region identifiers. Each region id is
 * associated with a region agent in the simulation.
 * 
 * @author Shawn Hatch
 *
 */
@ThreadSafe
public interface RegionId extends ComponentId {

}
