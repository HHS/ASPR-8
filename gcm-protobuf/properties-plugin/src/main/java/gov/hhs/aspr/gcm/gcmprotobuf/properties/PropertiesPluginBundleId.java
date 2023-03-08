package gov.hhs.aspr.gcm.gcmprotobuf.properties;

import gov.hhs.aspr.gcm.gcmprotobuf.core.PluginBundleId;

public final class PropertiesPluginBundleId implements PluginBundleId {
    public final static PluginBundleId PLUGIN_BUNDLE_ID = new PropertiesPluginBundleId();

    private PropertiesPluginBundleId() {
    }
}
