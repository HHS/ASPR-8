package common;

import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.protobuf.Any;
import com.google.protobuf.BoolValue;
import com.google.protobuf.BytesValue;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import com.google.protobuf.TypeRegistry;
import com.google.protobuf.UInt32Value;
import com.google.protobuf.UInt64Value;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Parser;
import com.google.protobuf.util.JsonFormat.Printer;

import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.TimeTrackingPolicy;

public class CommonTranslator implements ITranslator {
    private static Map<Descriptor, Message> getPrimitiveDescriptors() {
        Map<Descriptor, Message> map = new LinkedHashMap<>();

        map.put(BoolValue.getDescriptor(), BoolValue.getDefaultInstance());
        map.put(Int32Value.getDescriptor(), Int32Value.getDefaultInstance());
        map.put(UInt32Value.getDescriptor(), UInt32Value.getDefaultInstance());
        map.put(Int64Value.getDescriptor(), Int64Value.getDefaultInstance());
        map.put(UInt64Value.getDescriptor(), UInt64Value.getDefaultInstance());
        map.put(StringValue.getDescriptor(), StringValue.getDefaultInstance());
        map.put(BytesValue.getDescriptor(), BytesValue.getDefaultInstance());
        map.put(FloatValue.getDescriptor(), FloatValue.getDefaultInstance());
        map.put(DoubleValue.getDescriptor(), DoubleValue.getDefaultInstance());

        return map;
    }

    private Data data;

    private CommonTranslator(Data data) {
        this.data = data;
    }

    private static class Data {
        private final Map<Descriptor, Message> descriptorMap = new LinkedHashMap<>();
        private TypeRegistry registry;
        private Parser jsonParser;
        private Printer jsonPrinter;

        private Data() {
            this.descriptorMap.putAll(getPrimitiveDescriptors());
        }
    }

    public static class Builder implements ITranslatorBuilder {
        private Data data;

        private Builder(Data data) {
            this.data = data;
        }

        public CommonTranslator build() {
            this.data.registry = TypeRegistry.newBuilder().add(this.data.descriptorMap.keySet()).build();
            this.data.jsonParser = JsonFormat.parser().usingTypeRegistry(this.data.registry);
            this.data.jsonPrinter = JsonFormat.printer().includingDefaultValueFields()
                    .usingTypeRegistry(this.data.registry);
            return new CommonTranslator(this.data);
        }

        public Builder addDescriptor(Message message) {
            populate(message);
            return this;
        }

        private void populate(Message message) {
            this.data.descriptorMap.putIfAbsent(message.getDescriptorForType(), message);

            Set<FieldDescriptor> fieldDescriptors = message.getAllFields().keySet();

            if (fieldDescriptors.isEmpty()) {
                return;
            }

            for (FieldDescriptor fieldDescriptor : fieldDescriptors) {
                if (fieldDescriptor.getJavaType() == JavaType.MESSAGE) {
                    Message subMessage = (Message) message.getField(fieldDescriptor);
                    if (!(subMessage.getDescriptorForType() == Any.getDescriptor())) {
                        populate(subMessage);
                    }
                }
            }
        }
    }

    public static Builder builder() {
        return new Builder(new Data());
    }

    public Parser getJsonParser() {
        return this.data.jsonParser;
    }

    public Printer getJsonPrinter() {
        return this.data.jsonPrinter;
    }

    public CommonTranslator getCommonTranslator() {
        return this;
    }

    public void printJson(Message message) {
        try {
            System.out.println(this.data.jsonPrinter.print(message));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public <T extends Message, U extends Message.Builder> T parseJson(String inputFileName, U builder) {
        JsonReader jsonReader = new JsonReader(
                new InputStreamReader(this.getClass().getResourceAsStream(inputFileName)));
        JsonObject jsonObject = JsonParser.parseReader(jsonReader).getAsJsonObject();

        return parseJson(jsonObject, builder);
    }

    @SuppressWarnings("unchecked")
    public <T extends Message, U extends Message.Builder> T parseJson(JsonObject inputJson, U builder) {
        JsonObject jsonObject = inputJson.deepCopy();

        try {
            this.data.jsonParser.merge(jsonObject.toString(), builder);
            return (T) builder.build();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Object getValueFromAny(Any anyValue) {

        String typeUrl = anyValue.getTypeUrl();

        Message message;
        Class<? extends Message> classRef;

        try {
            message = this.data.descriptorMap.get(this.data.registry.getDescriptorForTypeUrl(typeUrl));
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("No corresponding message definition was found for: " + typeUrl);
        }

        classRef = message.getClass();
        try {
            Message unpackedMessage = anyValue.unpack(classRef);
            Descriptor unpackedMessageDescriptor = unpackedMessage.getDescriptorForType();
            // check if it is a primitive type and return that value
            if (getPrimitiveDescriptors().keySet().contains(unpackedMessageDescriptor)) {
                // wrapper messages have a singular field assigned to value 1
                FieldDescriptor primitiveFieldDescriptor = unpackedMessage.getDescriptorForType().findFieldByNumber(1);
                return unpackedMessage.getField(primitiveFieldDescriptor);
            }

            // TODO: require a callback to convert into a non message object
            // otherwise return the message as is
            return unpackedMessage;

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable To unpack any type to given class: " + classRef.getName());
        }
    }

    public Object getObjectFromInput(Any value) {
        return (Object) getValueFromAny(value);
    }

    public PropertyDefinition convertInputToPropertyDefinition(PropertyDefinitionInput input) {
        PropertyDefinition.Builder builder = PropertyDefinition.builder();

        builder.setPropertyValueMutability(input.getPropertyValuesAreMutable());
        builder.setTimeTrackingPolicy(TimeTrackingPolicy.valueOf(input.getTimeTrackingPolicy().toString()));

        if (input.hasDefaultValue()) {
            Object defaultValue = getObjectFromInput(input.getDefaultValue());
            builder.setDefaultValue(defaultValue);
            builder.setType(defaultValue.getClass());
        } else {
            String type = input.getType();
            Class<?> classType;
            try {
                classType = Class.forName(type);
                builder.setType(classType);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException("Unable to find class for type: " + type);
            }
        }

        return builder.build();
    }

    private static Any setToPrimitiveType(Object value) {
        if (String.class.isAssignableFrom(value.getClass())) {
            return Any.pack(StringValue.of((String) value));
        }
        if (int.class.isAssignableFrom(value.getClass())) {
            return Any.pack(Int32Value.of((int) value));
        }
        if (long.class.isAssignableFrom(value.getClass())) {
            return Any.pack(Int64Value.of((long) value));
        }
        if (double.class.isAssignableFrom(value.getClass())) {
            return Any.pack(DoubleValue.of((double) value));
        }
        if (float.class.isAssignableFrom(value.getClass())) {
            return Any.pack(FloatValue.of((float) value));
        }
        if (boolean.class.isAssignableFrom(value.getClass())) {
            return Any.pack(BoolValue.of((boolean) value));
        }
        throw new RuntimeException(
                "Unable to set value to Primitive type");
    }

    public static Any getInputTypeFromObject(Object value) {
        return setToPrimitiveType(value);
    }

    public static Any getInputTypeFromObject(Message messageWithValue) {
        return Any.pack(messageWithValue);
    }

    public static Any getInputTypeFromObject(Object value, MessageBuilderCB cb) {
        return Any.pack(cb.makeMessage(value));
    }

}
