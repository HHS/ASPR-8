package gov.hhs.aspr.translation.protobuf.core.translationSpecs;

import java.lang.reflect.InvocationTargetException;

import com.google.protobuf.ProtocolMessageEnum;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import gov.hhs.aspr.translation.protobuf.core.input.WrapperEnumValue;

@SuppressWarnings("rawtypes")
public class EnumTranslatorSpec extends ProtobufTranslationSpec<WrapperEnumValue, Enum> {

    @Override
    protected Enum convertInputObject(WrapperEnumValue inputObject) {
        String typeUrl = inputObject.getEnumTypeUrl();
        String value = inputObject.getValue();

        Class<?> classRef = this.translatorCore.getClassFromTypeUrl(typeUrl);

        try {
            Enum inputInput = (Enum<?>) classRef.getMethod("valueOf", String.class).invoke(null, value);
            return this.translatorCore.convertObject(inputInput);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected WrapperEnumValue convertAppObject(Enum appObject) {
        ProtocolMessageEnum messageEnum = this.translatorCore.convertObject(appObject);

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
