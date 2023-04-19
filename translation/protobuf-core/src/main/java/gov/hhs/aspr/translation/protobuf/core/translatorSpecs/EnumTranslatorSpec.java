package gov.hhs.aspr.translation.protobuf.core.translatorSpecs;

import java.lang.reflect.InvocationTargetException;

import com.google.protobuf.ProtocolMessageEnum;

import gov.hhs.aspr.translation.protobuf.core.AbstractProtobufTranslatorSpec;
import gov.hhs.aspr.translation.protobuf.core.input.WrapperEnumValue;

@SuppressWarnings("rawtypes")
public class EnumTranslatorSpec extends AbstractProtobufTranslatorSpec<WrapperEnumValue, Enum> {

    @Override
    protected Enum convertInputObject(WrapperEnumValue inputObject) {
        String typeUrl = inputObject.getEnumTypeUrl();
        String value = inputObject.getValue();

        Class<?> classRef = this.translator.getClassFromTypeUrl(typeUrl);

        try {
            Enum inputInput = (Enum<?>) classRef.getMethod("valueOf", String.class).invoke(null, value);
            return this.translator.convertInputObject(inputInput);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected WrapperEnumValue convertAppObject(Enum simObject) {
        ProtocolMessageEnum messageEnum = this.translator.convertSimObject(simObject);

        WrapperEnumValue wrapperEnumValue = WrapperEnumValue.newBuilder()
                .setValue(messageEnum.getValueDescriptor().getName())
                .setEnumTypeUrl(messageEnum.getDescriptorForType().getFullName()).build();

        return wrapperEnumValue;
    }

    @Override
    public Class<Enum> getAppObjectClass() {
        return Enum.class;
    }

    @Override
    public Class<WrapperEnumValue> getInputObjectClass() {
        return WrapperEnumValue.class;
    }
}
