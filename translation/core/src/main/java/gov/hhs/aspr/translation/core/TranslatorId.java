package gov.hhs.aspr.translation.core;

public interface TranslatorId {
	/**
	 * Implementationn consistent with equals()
	 */
	@Override
	public int hashCode();

	/**
	 * Two Translator ids are equal if and only if they represent the same plugin
	 * bundle.
	 * Translator ids are generally implemented as static instances.
	 */
	@Override
	public boolean equals(Object obj);
}
