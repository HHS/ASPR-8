package plugins.gcm.experiment;

import net.jcip.annotations.Immutable;
import util.IntId;

/**
 * Identifier for all replications
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public final class ReplicationId extends IntId {

	public ReplicationId(int id) {
		super(id);
	}
}
