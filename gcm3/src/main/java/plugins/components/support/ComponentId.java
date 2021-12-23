package plugins.components.support;

import net.jcip.annotations.ThreadSafe;

/**
 * Base marker interface for component identifiers. Should be extended for each
 * component type. Agents that are associated with component ids will have a
 * unique component id and are referred to as components. Component ids are
 * implemented by concrete classes that act as marker interfaces that define
 * types of agents that may have specific roles and limitations in the simulation.
 * 
 * @author Shawn Hatch
 *
 */
@ThreadSafe
public interface ComponentId {

}
