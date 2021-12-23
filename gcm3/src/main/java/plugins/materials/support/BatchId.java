package plugins.materials.support;

import net.jcip.annotations.Immutable;
import util.IntId;

/**
 * Identifier for all batches
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public final class BatchId extends IntId {

	public BatchId(int id) {
		super(id);
	}
}
