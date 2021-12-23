package nucleus.testsupport.actionplugin;

import nucleus.Event;

/**
 * Event used by the Action Agent to associate its Agent Id with the alias
 * assigned to the agent.
 * 
 * @author Shawn Hatch
 *
 */
public final class AliasAssignmentEvent implements Event {

	private final Object alias;

	/**
	 * Constructs an AliasAssignmentEvent from the given alias
	 * 
	 * @param alias
	 */
	public AliasAssignmentEvent(Object alias) {
		this.alias = alias;
	}

	/**
	 * Returns the alias
	 */
	public Object getAlias() {
		return alias;
	}

	/**
	 * Returns a string of the form
	 * 
	 * AliasAssignmentEvent [alias= x]
	 * 
	 * where x is the alias value used to construct this event
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AliasAssignmentEvent [alias=");
		builder.append(alias);
		builder.append("]");
		return builder.toString();
	}

}
