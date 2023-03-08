package core.translators;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Message;

import core.ITranslator;

public class PrimitiveTranslators {

    public final static BooleanTranslator BOOLEAN_TRANSLATOR = new BooleanTranslator();
    public final static Int32Translator INT32_TRANSLATOR = new Int32Translator();
    public final static UInt32Translator UINT32_TRANSLATOR = new UInt32Translator();
    public final static Int64Translator INT64_TRANSLATOR = new Int64Translator();
    public final static UInt64Translator UINT64_TRANSLATOR = new UInt64Translator();
    public final static StringTranslator STRING_TRANSLATOR = new StringTranslator();
    public final static FloatTranslator FLOAT_TRANSLATOR = new FloatTranslator();
    public final static DoubleTranslator DOUBLE_TRANSLATOR = new DoubleTranslator();

    public static Map<Descriptor, Message> getPrimitiveDescriptorToMessageMap() {
        Map<Descriptor, Message> map = new LinkedHashMap<>();

        map.put(BOOLEAN_TRANSLATOR.getDescriptorForInputObject(),
                BOOLEAN_TRANSLATOR.getDefaultInstanceForInputObject());
        map.put(INT32_TRANSLATOR.getDescriptorForInputObject(), INT32_TRANSLATOR.getDefaultInstanceForInputObject());
        map.put(UINT32_TRANSLATOR.getDescriptorForInputObject(), UINT32_TRANSLATOR.getDefaultInstanceForInputObject());
        map.put(INT64_TRANSLATOR.getDescriptorForInputObject(), INT64_TRANSLATOR.getDefaultInstanceForInputObject());
        map.put(UINT64_TRANSLATOR.getDescriptorForInputObject(), UINT64_TRANSLATOR.getDefaultInstanceForInputObject());
        map.put(STRING_TRANSLATOR.getDescriptorForInputObject(), STRING_TRANSLATOR.getDefaultInstanceForInputObject());
        map.put(FLOAT_TRANSLATOR.getDescriptorForInputObject(), FLOAT_TRANSLATOR.getDefaultInstanceForInputObject());
        map.put(DOUBLE_TRANSLATOR.getDescriptorForInputObject(), DOUBLE_TRANSLATOR.getDefaultInstanceForInputObject());

        return map;
    }

    public static Map<Descriptor, ITranslator> getPrimitiveDescriptorToTranslatorMap() {
        Map<Descriptor, ITranslator> map = new LinkedHashMap<>();

        map.put(BOOLEAN_TRANSLATOR.getDescriptorForInputObject(), BOOLEAN_TRANSLATOR);
        map.put(INT32_TRANSLATOR.getDescriptorForInputObject(), INT32_TRANSLATOR);
        map.put(UINT32_TRANSLATOR.getDescriptorForInputObject(), UINT32_TRANSLATOR);
        map.put(INT64_TRANSLATOR.getDescriptorForInputObject(), INT64_TRANSLATOR);
        map.put(UINT64_TRANSLATOR.getDescriptorForInputObject(), UINT64_TRANSLATOR);
        map.put(STRING_TRANSLATOR.getDescriptorForInputObject(), STRING_TRANSLATOR);
        map.put(FLOAT_TRANSLATOR.getDescriptorForInputObject(), FLOAT_TRANSLATOR);
        map.put(DOUBLE_TRANSLATOR.getDescriptorForInputObject(), DOUBLE_TRANSLATOR);

        return map;
    }

    public static Map<Class<?>, ITranslator> getPrimitiveObjectToTranslatorMap() {
        Map<Class<?>, ITranslator> map = new LinkedHashMap<>();

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
