package plugins.materials.support;

import net.jcip.annotations.Immutable;
import util.IntId;

/**
 * Identifier for all material stages
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public final class StageId extends IntId {

	public StageId(int id) {
		super(id);
	}

}
