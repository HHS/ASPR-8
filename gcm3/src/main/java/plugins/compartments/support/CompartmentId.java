package plugins.compartments.support;

import net.jcip.annotations.ThreadSafe;
import plugins.components.support.ComponentId;

/**
 * Marker interface for compartment identifiers. Each compartment id is
 * associated with a compartment agent in the simulation.
 * 
 * @author Shawn Hatch
 *
 */
@ThreadSafe
public interface CompartmentId extends ComponentId {

}
