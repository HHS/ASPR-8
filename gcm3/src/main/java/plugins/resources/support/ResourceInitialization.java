package plugins.resources.support;

public class ResourceInitialization {
	private final ResourceId resourceId;
	private final Long amount;

	public ResourceInitialization(ResourceId resourceId, Long amount) {
		super();
		this.resourceId = resourceId;
		this.amount = amount;
	}

	public ResourceId getResourceId() {
		return resourceId;
	}

	public Long getAmount() {
		return amount;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ResourceAssignment [resourceId=");
		builder.append(resourceId);
		builder.append(", amount=");
		builder.append(amount);
		builder.append("]");
		return builder.toString();
	}

}
