package gov.hhs.aspr.gcm.translation.core.translatorSpecs;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.translation.core.ITranslatorSpec;
import gov.hhs.aspr.gcm.translation.core.input.WrapperEnumValue;

import com.google.protobuf.Message;

public class PrimitiveTranslatorSpecs {

    public final static BooleanTranslatorSpec BOOLEAN_TRANSLATOR = new BooleanTranslatorSpec();
    public final static Int32TranslatorSpec INT32_TRANSLATOR = new Int32TranslatorSpec();
    public final static UInt32TranslatorSpec UINT32_TRANSLATOR = new UInt32TranslatorSpec();
    public final static Int64TranslatorSpec INT64_TRANSLATOR = new Int64TranslatorSpec();
    public final static UInt64TranslatorSpec UINT64_TRANSLATOR = new UInt64TranslatorSpec();
    public final static StringTranslatorSpec STRING_TRANSLATOR = new StringTranslatorSpec();
    public final static FloatTranslatorSpec FLOAT_TRANSLATOR = new FloatTranslatorSpec();
    public final static DoubleTranslatorSpec DOUBLE_TRANSLATOR = new DoubleTranslatorSpec();

    public static Map<Descriptor, Message> getPrimitiveDescriptorToMessageMap() {
        Map<Descriptor, Message> map = new LinkedHashMap<>();

        map.put(BOOLEAN_TRANSLATOR.getDescriptorForInputObject(),BOOLEAN_TRANSLATOR.getDefaultInstanceForInputObject());
        map.put(INT32_TRANSLATOR.getDescriptorForInputObject(), INT32_TRANSLATOR.getDefaultInstanceForInputObject());
        map.put(UINT32_TRANSLATOR.getDescriptorForInputObject(), UINT32_TRANSLATOR.getDefaultInstanceForInputObject());
        map.put(INT64_TRANSLATOR.getDescriptorForInputObject(), INT64_TRANSLATOR.getDefaultInstanceForInputObject());
        map.put(UINT64_TRANSLATOR.getDescriptorForInputObject(), UINT64_TRANSLATOR.getDefaultInstanceForInputObject());
        map.put(STRING_TRANSLATOR.getDescriptorForInputObject(), STRING_TRANSLATOR.getDefaultInstanceForInputObject());
        map.put(FLOAT_TRANSLATOR.getDescriptorForInputObject(), FLOAT_TRANSLATOR.getDefaultInstanceForInputObject());
        map.put(DOUBLE_TRANSLATOR.getDescriptorForInputObject(), DOUBLE_TRANSLATOR.getDefaultInstanceForInputObject());
        map.put(WrapperEnumValue.getDescriptor(), WrapperEnumValue.getDefaultInstance());

        return map;
    }

    public static Map<Class<?>, ITranslatorSpec> getPrimitiveInputTranslatorSpecMap() {
        Map<Class<?>, ITranslatorSpec> map = new LinkedHashMap<>();

        map.put(BOOLEAN_TRANSLATOR.getInputObjectClass(), BOOLEAN_TRANSLATOR);
        map.put(INT32_TRANSLATOR.getInputObjectClass(), INT32_TRANSLATOR);
        map.put(UINT32_TRANSLATOR.getInputObjectClass(), UINT32_TRANSLATOR);
        map.put(INT64_TRANSLATOR.getInputObjectClass(), INT64_TRANSLATOR);
        map.put(UINT64_TRANSLATOR.getInputObjectClass(), UINT64_TRANSLATOR);
        map.put(STRING_TRANSLATOR.getInputObjectClass(), STRING_TRANSLATOR);
        map.put(FLOAT_TRANSLATOR.getInputObjectClass(), FLOAT_TRANSLATOR);
        map.put(DOUBLE_TRANSLATOR.getInputObjectClass(), DOUBLE_TRANSLATOR);

        return map;
    }

    public static Map<Class<?>, ITranslatorSpec> getPrimitiveObjectTranslatorSpecMap() {
        Map<Class<?>, ITranslatorSpec> map = new LinkedHashMap<>();

        // no java version of unsigned int nor unsigned long
        map.put(BOOLEAN_TRANSLATOR.getSimObjectClass(), BOOLEAN_TRANSLATOR);
        map.put(INT32_TRANSLATOR.getSimObjectClass(), INT32_TRANSLATOR);
        map.put(INT64_TRANSLATOR.getSimObjectClass(), INT64_TRANSLATOR);
        map.put(STRING_TRANSLATOR.getSimObjectClass(), STRING_TRANSLATOR);
        map.put(FLOAT_TRANSLATOR.getSimObjectClass(), FLOAT_TRANSLATOR);
        map.put(DOUBLE_TRANSLATOR.getSimObjectClass(), DOUBLE_TRANSLATOR);

        return map;
    }
}