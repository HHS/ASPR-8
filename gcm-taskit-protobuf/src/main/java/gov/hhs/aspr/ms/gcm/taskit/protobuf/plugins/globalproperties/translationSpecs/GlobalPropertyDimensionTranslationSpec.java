package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.globalproperties.translationSpecs;

import com.google.protobuf.Any;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.globalproperties.support.input.GlobalPropertyDimensionInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.globalproperties.support.input.GlobalPropertyIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.globalproperties.support.GlobalPropertyDimension;
import plugins.globalproperties.support.GlobalPropertyId;

public class GlobalPropertyDimensionTranslationSpec
        extends ProtobufTranslationSpec<GlobalPropertyDimensionInput, GlobalPropertyDimension> {

    @Override
    protected GlobalPropertyDimension convertInputObject(GlobalPropertyDimensionInput inputObject) {
        GlobalPropertyDimension.Builder builder = GlobalPropertyDimension.builder();

        GlobalPropertyId globalPropertyId = this.translationEngine.convertObject(inputObject.getGlobalPropertyId());

        builder
                .setGlobalPropertyId(globalPropertyId)
                .setAssignmentTime(inputObject.getAssignmentTime());

        for (Any anyValue : inputObject.getValuesList()) {
            Object value = this.translationEngine.getObjectFromAny(anyValue);
            builder.addValue(value);
        }

        return builder.build();
    }

    @Override
    protected GlobalPropertyDimensionInput convertAppObject(GlobalPropertyDimension appObject) {
        GlobalPropertyDimensionInput.Builder builder = GlobalPropertyDimensionInput.newBuilder();

        GlobalPropertyIdInput globalPropertyIdInput = this.translationEngine
                .convertObjectAsSafeClass(appObject.getGlobalPropertyId(), GlobalPropertyId.class);

        builder
                .setGlobalPropertyId(globalPropertyIdInput)
                .setAssignmentTime(appObject.getAssignmentTime());

        for (Object objValue : appObject.getValues()) {
            builder.addValues(this.translationEngine.getAnyFromObject(objValue));
        }

        return builder.build();
    }

    @Override
    public Class<GlobalPropertyDimension> getAppObjectClass() {
        return GlobalPropertyDimension.class;
    }

    @Override
    public Class<GlobalPropertyDimensionInput> getInputObjectClass() {
        return GlobalPropertyDimensionInput.class;
    }

}
