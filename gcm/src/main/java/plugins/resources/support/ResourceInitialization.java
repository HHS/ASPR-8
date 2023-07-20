package plugins.resources.support;

import net.jcip.annotations.Immutable;

@Immutable
public final class ResourceInitialization {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((amount == null) ? 0 : amount.hashCode());
		result = prime * result + ((resourceId == null) ? 0 : resourceId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ResourceInitialization)) {
			return false;
		}
		ResourceInitialization other = (ResourceInitialization) obj;
		if (amount == null) {
			if (other.amount != null) {
				return false;
			}
		} else if (!amount.equals(other.amount)) {
			return false;
		}
		if (resourceId == null) {
			if (other.resourceId != null) {
				return false;
			}
		} else if (!resourceId.equals(other.resourceId)) {
			return false;
		}
		return true;
	}
	
	

}
