package lesson.plugins.model.actors.antigenproducer;

import gov.hhs.aspr.ms.gcm.nucleus.PlanData;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialId;

public final class MaterialReceiptPlanData implements PlanData {
	private final MaterialId materialId;
	private final double amount;
	private final double deliveryTime;

	public MaterialReceiptPlanData(MaterialId materialId, double amount, double deliveryTime) {
		super();
		this.materialId = materialId;
		this.amount = amount;
		this.deliveryTime = deliveryTime;
	}

	public MaterialId getMaterialId() {
		return materialId;
	}

	public double getAmount() {
		return amount;
	}

	public double getDeliveryTime() {
		return deliveryTime;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MaterialReceiptPlanData [materialId=");
		builder.append(materialId);
		builder.append(", amount=");
		builder.append(amount);
		builder.append(", deliveryTime=");
		builder.append(deliveryTime);
		builder.append("]");
		return builder.toString();
	}

}
