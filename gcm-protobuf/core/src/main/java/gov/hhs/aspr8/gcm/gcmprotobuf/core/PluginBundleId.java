package gov.hhs.aspr8.gcm.gcmprotobuf.core;

public interface PluginBundleId {
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
