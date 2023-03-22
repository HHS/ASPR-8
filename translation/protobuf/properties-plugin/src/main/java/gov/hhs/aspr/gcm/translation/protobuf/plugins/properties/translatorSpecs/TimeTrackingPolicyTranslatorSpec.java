package gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.translatorSpecs;

import gov.hhs.aspr.gcm.translation.plugins.properties.input.TimeTrackingPolicyInput;
import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;
import plugins.util.properties.TimeTrackingPolicy;

public class TimeTrackingPolicyTranslatorSpec
        extends AbstractTranslatorSpec<TimeTrackingPolicyInput, TimeTrackingPolicy> {

    @Override
    protected TimeTrackingPolicy convertInputObject(TimeTrackingPolicyInput inputObject) {
        return TimeTrackingPolicy.valueOf(inputObject.name());
    }

    @Override
    protected TimeTrackingPolicyInput convertAppObject(TimeTrackingPolicy simObject) {
        return TimeTrackingPolicyInput.valueOf(simObject.name());
    }

    @Override
    public TimeTrackingPolicyInput getDefaultInstanceForInputObject() {
        return TimeTrackingPolicyInput.forNumber(0);
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
