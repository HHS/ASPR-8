package plugins.groups.support;

import net.jcip.annotations.Immutable;
import util.IntId;

/**
 * Identifier for all groups
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public final class GroupId extends IntId {

	public GroupId(int id) {
		super(id);
	}
}
