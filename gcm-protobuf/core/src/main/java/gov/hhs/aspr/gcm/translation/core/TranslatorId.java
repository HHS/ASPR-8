package gov.hhs.aspr.gcm.translation.core;

public interface TranslatorId {
	/**
	 * Implementationn consistent with equals()
	 */
	@Override
	public int hashCode();

	/**
	 * Two plugin bundle ids are equal if and only if they represent the same plugin
	 * bundle.
	 * Plugin bundle ids are generally implemented as static instances.
	 */
	@Override
	public boolean equals(Object obj);
}
