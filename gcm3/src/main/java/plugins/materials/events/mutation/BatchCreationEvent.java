package plugins.materials.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.MaterialId;

/**
 * Creates a new batch from a material and amount.
 *
 */
@Immutable
public final class BatchCreationEvent implements Event {

	private final MaterialId materialId;

	private final double amount;
	
	public BatchCreationEvent(MaterialId materialId, double amount) {
		super();
		this.materialId = materialId;
		this.amount = amount;
	}

	public MaterialId getMaterialId() {
		return materialId;
	}

	public double getAmount() {
		return amount;
	}

}
