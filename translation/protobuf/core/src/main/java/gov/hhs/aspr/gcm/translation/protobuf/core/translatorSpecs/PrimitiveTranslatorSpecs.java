package gov.hhs.aspr.gcm.translation.protobuf.core.translatorSpecs;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.protobuf.Message;

import gov.hhs.aspr.gcm.translation.core.ITranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.core.input.WrapperEnumValue;

public class PrimitiveTranslatorSpecs {

    public final static BooleanTranslatorSpec BOOLEAN_TRANSLATOR_SPEC = new BooleanTranslatorSpec();
    public final static Int32TranslatorSpec INT32_TRANSLATOR_SPEC = new Int32TranslatorSpec();
    public final static UInt32TranslatorSpec UINT32_TRANSLATOR_SPEC = new UInt32TranslatorSpec();
    public final static Int64TranslatorSpec INT64_TRANSLATOR_SPEC = new Int64TranslatorSpec();
    public final static UInt64TranslatorSpec UINT64_TRANSLATOR_SPEC = new UInt64TranslatorSpec();
    public final static StringTranslatorSpec STRING_TRANSLATOR_SPEC = new StringTranslatorSpec();
    public final static FloatTranslatorSpec FLOAT_TRANSLATOR_SPEC = new FloatTranslatorSpec();
    public final static DoubleTranslatorSpec DOUBLE_TRANSLATOR_SPEC = new DoubleTranslatorSpec();
    public final static DateTranslatorSpec DATE_TRANSLATOR_SPEC = new DateTranslatorSpec();

    public static Map<String, Message> getPrimitiveTypeUrlToMessageMap() {
        Map<String, Message> map = new LinkedHashMap<>();

        map.put(BOOLEAN_TRANSLATOR_SPEC.getDefaultInstanceForInputObject().getDescriptorForType().getFullName(),BOOLEAN_TRANSLATOR_SPEC.getDefaultInstanceForInputObject());
        map.put(INT32_TRANSLATOR_SPEC.getDefaultInstanceForInputObject().getDescriptorForType().getFullName(), INT32_TRANSLATOR_SPEC.getDefaultInstanceForInputObject());
        map.put(UINT32_TRANSLATOR_SPEC.getDefaultInstanceForInputObject().getDescriptorForType().getFullName(), UINT32_TRANSLATOR_SPEC.getDefaultInstanceForInputObject());
        map.put(INT64_TRANSLATOR_SPEC.getDefaultInstanceForInputObject().getDescriptorForType().getFullName(), INT64_TRANSLATOR_SPEC.getDefaultInstanceForInputObject());
        map.put(UINT64_TRANSLATOR_SPEC.getDefaultInstanceForInputObject().getDescriptorForType().getFullName(), UINT64_TRANSLATOR_SPEC.getDefaultInstanceForInputObject());
        map.put(STRING_TRANSLATOR_SPEC.getDefaultInstanceForInputObject().getDescriptorForType().getFullName(), STRING_TRANSLATOR_SPEC.getDefaultInstanceForInputObject());
        map.put(FLOAT_TRANSLATOR_SPEC.getDefaultInstanceForInputObject().getDescriptorForType().getFullName(), FLOAT_TRANSLATOR_SPEC.getDefaultInstanceForInputObject());
        map.put(DOUBLE_TRANSLATOR_SPEC.getDefaultInstanceForInputObject().getDescriptorForType().getFullName(), DOUBLE_TRANSLATOR_SPEC.getDefaultInstanceForInputObject());
        map.put(DATE_TRANSLATOR_SPEC.getDefaultInstanceForInputObject().getDescriptorForType().getFullName(), DATE_TRANSLATOR_SPEC.getDefaultInstanceForInputObject());
        map.put(WrapperEnumValue.getDescriptor().getFullName(), WrapperEnumValue.getDefaultInstance());

        return map;
    }

    public static Map<Class<?>, ITranslatorSpec> getPrimitiveInputTranslatorSpecMap() {
        Map<Class<?>, ITranslatorSpec> map = new LinkedHashMap<>();

        map.put(BOOLEAN_TRANSLATOR_SPEC.getInputObjectClass(), BOOLEAN_TRANSLATOR_SPEC);
        map.put(INT32_TRANSLATOR_SPEC.getInputObjectClass(), INT32_TRANSLATOR_SPEC);
        map.put(UINT32_TRANSLATOR_SPEC.getInputObjectClass(), UINT32_TRANSLATOR_SPEC);
        map.put(INT64_TRANSLATOR_SPEC.getInputObjectClass(), INT64_TRANSLATOR_SPEC);
        map.put(UINT64_TRANSLATOR_SPEC.getInputObjectClass(), UINT64_TRANSLATOR_SPEC);
        map.put(STRING_TRANSLATOR_SPEC.getInputObjectClass(), STRING_TRANSLATOR_SPEC);
        map.put(FLOAT_TRANSLATOR_SPEC.getInputObjectClass(), FLOAT_TRANSLATOR_SPEC);
        map.put(DOUBLE_TRANSLATOR_SPEC.getInputObjectClass(), DOUBLE_TRANSLATOR_SPEC);
        map.put(DATE_TRANSLATOR_SPEC.getInputObjectClass(), DATE_TRANSLATOR_SPEC);

        return map;
    }

    public static Map<Class<?>, ITranslatorSpec> getPrimitiveObjectTranslatorSpecMap() {
        Map<Class<?>, ITranslatorSpec> map = new LinkedHashMap<>();

        // no java version of unsigned int nor unsigned long
        map.put(BOOLEAN_TRANSLATOR_SPEC.getAppObjectClass(), BOOLEAN_TRANSLATOR_SPEC);
        map.put(INT32_TRANSLATOR_SPEC.getAppObjectClass(), INT32_TRANSLATOR_SPEC);
        map.put(INT64_TRANSLATOR_SPEC.getAppObjectClass(), INT64_TRANSLATOR_SPEC);
        map.put(STRING_TRANSLATOR_SPEC.getAppObjectClass(), STRING_TRANSLATOR_SPEC);
        map.put(FLOAT_TRANSLATOR_SPEC.getAppObjectClass(), FLOAT_TRANSLATOR_SPEC);
        map.put(DOUBLE_TRANSLATOR_SPEC.getAppObjectClass(), DOUBLE_TRANSLATOR_SPEC);
        map.put(DATE_TRANSLATOR_SPEC.getAppObjectClass(), DATE_TRANSLATOR_SPEC);

        return map;
    }
}
