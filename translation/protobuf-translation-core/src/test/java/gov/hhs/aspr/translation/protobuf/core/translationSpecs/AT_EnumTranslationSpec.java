package gov.hhs.aspr.translation.protobuf.core.translationSpecs;

import java.lang.reflect.InvocationTargetException;

import com.google.protobuf.Any;
import com.google.protobuf.ProtocolMessageEnum;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import gov.hhs.aspr.translation.protobuf.core.input.WrapperEnumValue;

/**
 * TranslationSpec that defines how to convert from any Java Enum to a
 * Protobuf {@link WrapperEnumValue} type and vice versa
 * 
 * <li>Note: A {@link WrapperEnumValue} is specifically used to wrap a Enum
 * into a Protobuf {@link Any} type, since a Protobuf {@link Any} type does
 * not natively support enums, only primitives and other Protobuf Messages
 */
@SuppressWarnings("rawtypes")
public class AT_EnumTranslationSpec extends ProtobufTranslationSpec<WrapperEnumValue, Enum> {

    @Override
    protected Enum convertInputObject(WrapperEnumValue inputObject) {
        String typeUrl = inputObject.getEnumTypeUrl();
        String value = inputObject.getValue();

        Class<?> classRef = this.translationEngine.getClassFromTypeUrl(typeUrl);

        try {
            Enum inputInput = (Enum<?>) classRef.getMethod("valueOf", String.class).invoke(null, value);
            return this.translationEngine.convertObject(inputInput);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected WrapperEnumValue convertAppObject(Enum appObject) {
        ProtocolMessageEnum messageEnum = this.translationEngine.convertObject(appObject);

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
