package gov.hhs.aspr.ms.gcm.simulation.plugins.resources.support;

import java.util.Objects;

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

	/**
     * Standard implementation consistent with the {@link #equals(Object)} method
     */
	@Override
	public int hashCode() {
		return Objects.hash(resourceId, amount);
	}

	/**
     * Two {@link ResourceInitialization} instances are equal if and only if
     * their inputs are equal.
     */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ResourceInitialization other = (ResourceInitialization) obj;
		return Objects.equals(resourceId, other.resourceId) && Objects.equals(amount, other.amount);
	}

}
