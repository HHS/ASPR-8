package gov.hhs.aspr.ms.gcm.pipeline;

import gov.hhs.aspr.ms.gcm.nucleus.Plugin;

public interface IPluginPipeline extends IPipeline {
    Plugin getPlugin();
}
