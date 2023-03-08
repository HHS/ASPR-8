package gov.hhs.aspr8.gcm.gcmprotobuf.properties;

import gov.hhs.aspr8.gcm.gcmprotobuf.core.PluginBundleId;

public final class PropertiesPluginBundleId implements PluginBundleId {
    public final static PluginBundleId PLUGIN_BUNDLE_ID = new PropertiesPluginBundleId();

    private PropertiesPluginBundleId() {
    }
}
