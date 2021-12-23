package plugins.gcm.experiment;

import net.jcip.annotations.Immutable;
import util.IntId;

/**
 * Identifier for all scenarios
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public final class ScenarioId extends IntId {

	public ScenarioId(int id) {
		super(id);
	}
}
