package gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.TimeTrackingPolicyInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.util.properties.TimeTrackingPolicy;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain TimeTrackingPolicyInput} and
 * {@linkplain TimeTrackingPolicy}
 */
public class TimeTrackingPolicyTranslationSpec
        extends ProtobufTranslationSpec<TimeTrackingPolicyInput, TimeTrackingPolicy> {

    @Override
    protected TimeTrackingPolicy convertInputObject(TimeTrackingPolicyInput inputObject) {
        return TimeTrackingPolicy.valueOf(inputObject.name());
    }

    @Override
    protected TimeTrackingPolicyInput convertAppObject(TimeTrackingPolicy appObject) {
        return TimeTrackingPolicyInput.valueOf(appObject.name());
    }

    @Override
    public Class<TimeTrackingPolicy> getAppObjectClass() {
        return TimeTrackingPolicy.class;
    }

    @Override
    public Class<TimeTrackingPolicyInput> getInputObjectClass() {
        return TimeTrackingPolicyInput.class;
    }

}
