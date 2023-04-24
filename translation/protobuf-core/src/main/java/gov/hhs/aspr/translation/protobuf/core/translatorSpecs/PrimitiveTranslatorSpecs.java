package gov.hhs.aspr.translation.protobuf.core.translatorSpecs;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.google.protobuf.BoolValue;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.StringValue;
import com.google.protobuf.UInt32Value;
import com.google.protobuf.UInt64Value;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.type.Date;

import gov.hhs.aspr.translation.core.ITranslatorSpec;
import gov.hhs.aspr.translation.protobuf.core.input.WrapperEnumValue;

public class PrimitiveTranslatorSpecs {

    private final static BooleanTranslatorSpec BOOLEAN_TRANSLATOR_SPEC = new BooleanTranslatorSpec();
    private final static Int32TranslatorSpec INT32_TRANSLATOR_SPEC = new Int32TranslatorSpec();
    private final static UInt32TranslatorSpec UINT32_TRANSLATOR_SPEC = new UInt32TranslatorSpec();
    private final static Int64TranslatorSpec INT64_TRANSLATOR_SPEC = new Int64TranslatorSpec();
    private final static UInt64TranslatorSpec UINT64_TRANSLATOR_SPEC = new UInt64TranslatorSpec();
    private final static StringTranslatorSpec STRING_TRANSLATOR_SPEC = new StringTranslatorSpec();
    private final static FloatTranslatorSpec FLOAT_TRANSLATOR_SPEC = new FloatTranslatorSpec();
    private final static DoubleTranslatorSpec DOUBLE_TRANSLATOR_SPEC = new DoubleTranslatorSpec();
    private final static DateTranslatorSpec DATE_TRANSLATOR_SPEC = new DateTranslatorSpec();
    private final static EnumTranslatorSpec ENUM_TRANSLATOR_SPEC = new EnumTranslatorSpec();
    private final static AnyTranslatorSpec ANY_TRANSLATOR_SPEC = new AnyTranslatorSpec();

    public static Set<Descriptor> getPrimitiveDescriptors() {
        Set<Descriptor> set = new LinkedHashSet<>();

        set.add(BoolValue.getDefaultInstance().getDescriptorForType());
        set.add(Int32Value.getDefaultInstance().getDescriptorForType());
        set.add(UInt32Value.getDefaultInstance().getDescriptorForType());
        set.add(Int64Value.getDefaultInstance().getDescriptorForType());
        set.add(UInt64Value.getDefaultInstance().getDescriptorForType());
        set.add(StringValue.getDefaultInstance().getDescriptorForType());
        set.add(FloatValue.getDefaultInstance().getDescriptorForType());
        set.add(DoubleValue.getDefaultInstance().getDescriptorForType());
        set.add(Date.getDefaultInstance().getDescriptorForType());
        set.add(WrapperEnumValue.getDefaultInstance().getDescriptorForType());

        return set;
    }

    public static Set<ITranslatorSpec> getPrimitiveTranslatorSpecs() {
        Set<ITranslatorSpec> set = new LinkedHashSet<>();

        set.addAll(getPrimitiveInputTranslatorSpecMap().values());
        set.addAll(getPrimitiveObjectTranslatorSpecMap().values());

        return set;
    }

    public static Map<String, Class<?>> getPrimitiveTypeUrlToClassMap() {
        Map<String, Class<?>> map = new LinkedHashMap<>();

        map.put(BoolValue.getDefaultInstance().getDescriptorForType().getFullName(),
                BOOLEAN_TRANSLATOR_SPEC.getInputObjectClass());
        map.put(Int32Value.getDefaultInstance().getDescriptorForType().getFullName(),
                INT32_TRANSLATOR_SPEC.getInputObjectClass());
        map.put(UInt32Value.getDefaultInstance().getDescriptorForType().getFullName(),
                UINT32_TRANSLATOR_SPEC.getInputObjectClass());
        map.put(Int64Value.getDefaultInstance().getDescriptorForType().getFullName(),
                INT64_TRANSLATOR_SPEC.getInputObjectClass());
        map.put(UInt64Value.getDefaultInstance().getDescriptorForType().getFullName(),
                UINT64_TRANSLATOR_SPEC.getInputObjectClass());
        map.put(StringValue.getDefaultInstance().getDescriptorForType().getFullName(),
                STRING_TRANSLATOR_SPEC.getInputObjectClass());
        map.put(FloatValue.getDefaultInstance().getDescriptorForType().getFullName(),
                FLOAT_TRANSLATOR_SPEC.getInputObjectClass());
        map.put(DoubleValue.getDefaultInstance().getDescriptorForType().getFullName(),
                DOUBLE_TRANSLATOR_SPEC.getInputObjectClass());
        map.put(Date.getDefaultInstance().getDescriptorForType().getFullName(),
                DATE_TRANSLATOR_SPEC.getInputObjectClass());
        map.put(WrapperEnumValue.getDefaultInstance().getDescriptorForType().getFullName(),
                ENUM_TRANSLATOR_SPEC.getInputObjectClass());

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
        map.put(ENUM_TRANSLATOR_SPEC.getInputObjectClass(), ENUM_TRANSLATOR_SPEC);
        map.put(ANY_TRANSLATOR_SPEC.getInputObjectClass(), ANY_TRANSLATOR_SPEC);

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
        map.put(ENUM_TRANSLATOR_SPEC.getAppObjectClass(), ENUM_TRANSLATOR_SPEC);

        return map;
    }
}
