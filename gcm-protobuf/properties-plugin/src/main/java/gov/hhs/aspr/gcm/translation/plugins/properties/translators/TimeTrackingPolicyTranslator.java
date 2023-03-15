package gov.hhs.aspr.gcm.translation.plugins.properties.translators;

import com.google.protobuf.Descriptors.EnumDescriptor;

import gov.hhs.aspr.gcm.translation.core.AbstractEnumTranslator;

import com.google.protobuf.ProtocolMessageEnum;

import gov.hhs.aspr.gcm.translation.plugins.properties.input.TimeTrackingPolicyInput;
import plugins.util.properties.TimeTrackingPolicy;

public class TimeTrackingPolicyTranslator extends AbstractEnumTranslator<TimeTrackingPolicyInput, TimeTrackingPolicy> {

    @Override
    protected TimeTrackingPolicy convertInputObject(TimeTrackingPolicyInput inputObject) {
        return TimeTrackingPolicy.valueOf(inputObject.name());
    }

    @Override
    protected TimeTrackingPolicyInput convertSimObject(TimeTrackingPolicy simObject) {
        return TimeTrackingPolicyInput.valueOf(simObject.name());
    }

    @Override
    public EnumDescriptor getDescriptorForInputObject() {
        return TimeTrackingPolicyInput.getDescriptor();
    }

    @Override
    public EnumInstance getEnumInstance() {
        return new EnumInstance() {

            @Override
            public ProtocolMessageEnum getFromString(String string) {
                return TimeTrackingPolicyInput.valueOf(string);
            }

        };
    }

    @Override
    public Class<TimeTrackingPolicy> getSimObjectClass() {
        return TimeTrackingPolicy.class;
    }

    @Override
    public Class<TimeTrackingPolicyInput> getInputObjectClass() {
        return TimeTrackingPolicyInput.class;
    }

}
