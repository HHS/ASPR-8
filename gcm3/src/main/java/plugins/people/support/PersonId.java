package plugins.people.support;

import net.jcip.annotations.Immutable;
import util.IntId;

/**
 * Identifier for all people
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public final class PersonId extends IntId {

	public PersonId(int id) {
		super(id);
	}
}
