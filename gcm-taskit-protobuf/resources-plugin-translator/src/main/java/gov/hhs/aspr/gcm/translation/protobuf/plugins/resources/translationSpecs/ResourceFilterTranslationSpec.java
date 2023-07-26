package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.input.EqualityInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourceFilterInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourceIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.partitions.support.Equality;
import plugins.resources.support.ResourceFilter;
import plugins.resources.support.ResourceId;

public class ResourceFilterTranslationSpec extends ProtobufTranslationSpec<ResourceFilterInput, ResourceFilter> {

    @Override
    protected ResourceFilter convertInputObject(ResourceFilterInput inputObject) {
        ResourceId resourceId = this.translationEngine.convertObject(inputObject.getResourceId());
        Equality equality = this.translationEngine.convertObject(inputObject.getEquality());
        long resourceValue = inputObject.getResourceValue();

        return new ResourceFilter(resourceId, equality, resourceValue);
    }

    @Override
    protected ResourceFilterInput convertAppObject(ResourceFilter appObject) {
        ResourceIdInput resourceId = this.translationEngine.convertObjectAsSafeClass(appObject.getResourceId(),
                ResourceId.class);
        EqualityInput equality = this.translationEngine.convertObjectAsSafeClass(appObject.getEquality(),
                Equality.class);
        long resourceValue = appObject.getResourceValue();

        return ResourceFilterInput.newBuilder()
                .setResourceId(resourceId)
                .setEquality(equality)
                .setResourceValue(resourceValue)
                .build();
    }

    @Override
    public Class<ResourceFilter> getAppObjectClass() {
        return ResourceFilter.class;
    }

    @Override
    public Class<ResourceFilterInput> getInputObjectClass() {
        return ResourceFilterInput.class;
    }

}