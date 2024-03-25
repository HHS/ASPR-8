package gov.hhs.aspr.ms.epifast.pipeline.pipelines;

import gov.hhs.aspr.ms.gcm.nucleus.Plugin;

public interface IPluginPipeline extends IPipeline {
    Plugin getPlugin();
}
