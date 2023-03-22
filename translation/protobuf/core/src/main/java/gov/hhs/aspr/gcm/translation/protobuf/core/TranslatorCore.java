package gov.hhs.aspr.gcm.translation.protobuf.core;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.protobuf.Any;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.ProtocolMessageEnum;
import com.google.protobuf.TypeRegistry;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Parser;
import com.google.protobuf.util.JsonFormat.Printer;

import gov.hhs.aspr.gcm.translation.core.input.WrapperEnumValue;
import gov.hhs.aspr.gcm.translation.protobuf.core.translatorSpecs.PrimitiveTranslatorSpecs;
import nucleus.PluginData;

public class TranslatorCore {

    private final Data data;
    private boolean debug = false;
    private boolean isInitialized = false;

    private TranslatorCore(Data data) {
        this.data = data;
    }

    private static class Data {
        private final Map<String, Message> descriptorMap = new LinkedHashMap<>();
        private final Map<String, ProtocolMessageEnum> typeUrlToEnumMap = new LinkedHashMap<>();
        private final Map<Class<?>, ITranslatorSpec> classToTranslatorSpecMap = new LinkedHashMap<>();
        private final Set<ITranslatorSpec> translatorSpecs = new LinkedHashSet<>();
        private final Set<FieldDescriptor> defaultValueFieldsToPrint = new LinkedHashSet<>();
        private TypeRegistry registry;
        private Parser jsonParser;
        private Printer jsonPrinter;
        private boolean ignoringUnknownFields = true;
        private boolean includingDefaultValueFields = false;

        private Data() {
            this.descriptorMap.putAll(PrimitiveTranslatorSpecs.getPrimitiveTypeUrlToMessageMap());
            this.classToTranslatorSpecMap.putAll(PrimitiveTranslatorSpecs.getPrimitiveInputTranslatorSpecMap());
            this.classToTranslatorSpecMap.putAll(PrimitiveTranslatorSpecs.getPrimitiveObjectTranslatorSpecMap());
            this.translatorSpecs.addAll(PrimitiveTranslatorSpecs.getPrimitiveObjectTranslatorSpecMap().values());
        }
    }

    public static class Builder {
        private Data data;

        private Builder(Data data) {
            this.data = data;
        }

        public TranslatorCore build() {
            TypeRegistry.Builder typeRegistryBuilder = TypeRegistry.newBuilder();

            this.data.descriptorMap.values().forEach((message) -> {
                typeRegistryBuilder.add(message.getDescriptorForType());
            });

            this.data.registry = typeRegistryBuilder.build();

            Parser parser = JsonFormat.parser().usingTypeRegistry(this.data.registry);
            if (this.data.ignoringUnknownFields) {
                parser = parser.ignoringUnknownFields();
            }
            this.data.jsonParser = parser;

            Printer printer = JsonFormat.printer().usingTypeRegistry(this.data.registry);

            if (!this.data.defaultValueFieldsToPrint.isEmpty()) {
                printer = printer.includingDefaultValueFields(this.data.defaultValueFieldsToPrint);
            }

            if (this.data.includingDefaultValueFields) {
                printer = printer.includingDefaultValueFields();
            }
            this.data.jsonPrinter = printer;

            return new TranslatorCore(this.data);
        }

        public Builder setIgnoringUnknownFields(boolean ignoringUnknownFields) {
            this.data.ignoringUnknownFields = ignoringUnknownFields;
            return this;
        }

        public Builder setIncludingDefaultValueFields(boolean includingDefaultValueFields) {
            this.data.includingDefaultValueFields = includingDefaultValueFields;
            return this;
        }

        public Builder addFieldToIncludeDefaultValue(FieldDescriptor fieldDescriptor) {
            this.data.defaultValueFieldsToPrint.add(fieldDescriptor);

            return this;
        }

        public Builder addDescriptor(Message message) {
            populate(message);
            return this;
        }

        public <I, S> Builder addTranslatorSpec(AbstractTranslatorSpec<I, S> translatorSpec) {
            this.data.classToTranslatorSpecMap.putIfAbsent(translatorSpec.getInputObjectClass(),
                    translatorSpec);
            this.data.classToTranslatorSpecMap.putIfAbsent(translatorSpec.getAppObjectClass(), translatorSpec);

            this.data.translatorSpecs.add(translatorSpec);

            if (translatorSpec.getDefaultInstanceForInputObject() instanceof Message) {
                populate((Message) translatorSpec.getDefaultInstanceForInputObject());
                return this;
            }

            if (translatorSpec.getDefaultInstanceForInputObject() instanceof ProtocolMessageEnum) {
                ProtocolMessageEnum messageEnum = (ProtocolMessageEnum) translatorSpec
                        .getDefaultInstanceForInputObject();
                this.data.typeUrlToEnumMap.putIfAbsent(messageEnum.getDescriptorForType().getFullName(), messageEnum);
                return this;
            }

            return this;

        }

        private void populate(Message message) {
            this.data.descriptorMap.putIfAbsent(message.getDescriptorForType().getFullName(), message);

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

    public void init() {
        this.data.translatorSpecs.forEach((translatorSpec) -> translatorSpec.init(this));

        this.isInitialized = true;
    }

    public boolean isInitialized() {
        return this.isInitialized;
    }

    public Parser getJsonParser() {
        return this.data.jsonParser;
    }

    public Printer getJsonPrinter() {
        return this.data.jsonPrinter;
    }

    public <T extends PluginData, U extends Message> void writeJson(Writer writer, T pluginData) {
        U message = convertSimObject(pluginData);
        writeJson(writer, message);
    }

    public <U extends Message> void writeJson(Writer writer, Object simObject) {
        U message = convertSimObject(simObject);
        writeJson(writer, message);
    }

    private <U extends Message> void writeJson(Writer writer, U message) {
        try {
            writer.write(this.data.jsonPrinter.print(message));
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void printJsonToConsole(Message message) {
        try {
            System.out.println(this.data.jsonPrinter.print(message));
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    public <T, U extends Message.Builder> T readJson(Reader reader, U builder) {
        JsonObject jsonObject = JsonParser.parseReader(new JsonReader(reader)).getAsJsonObject();
        return parseJson(jsonObject, builder);
    }

    private <T, U extends Message.Builder> T parseJson(JsonObject inputJson, U builder) {
        JsonObject jsonObject = inputJson.deepCopy();

        try {
            this.data.jsonParser.merge(jsonObject.toString(), builder);
            if (this.debug) {
                printJsonToConsole(builder.build());
            }
            return convertInputObject(builder.build());
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    public Any getAnyFromObject(Object object, Class<?> superClass) {
        if (Enum.class.isAssignableFrom(object.getClass())) {
            superClassCastCheck(object, superClass);
            return packMessage(getWrapperEnum(object));
        }
        return packMessage(convertSimObject(object, superClass));
    }

    public Any getAnyFromObject(Object object) {
        if (Enum.class.isAssignableFrom(object.getClass())) {
            return packMessage(getWrapperEnum(object));
        }
        return packMessage(convertSimObject(object));
    }

    public Any packMessage(Message messageToPack) {
        return Any.pack(messageToPack);
    }

    public <T> T getObjectFromAny(Any anyValue, Class<?> superClass) {
        Message anyMessage = getMessageFromAny(anyValue);

        if (anyMessage.getDescriptorForType() == WrapperEnumValue.getDescriptor()) {
            return convertInputEnum(getEnumFromMessage((WrapperEnumValue) anyMessage), superClass);
        }

        return convertInputObject(anyMessage, superClass);
    }

    public <T> T getObjectFromAny(Any anyValue) {
        Message anyMessage = getMessageFromAny(anyValue);

        if (anyMessage.getDescriptorForType() == WrapperEnumValue.getDescriptor()) {
            return convertInputEnum(getEnumFromMessage((WrapperEnumValue) anyMessage));
        }
        return convertInputObject(anyMessage);
    }

    private Message getWrapperEnum(Object object) {
        ProtocolMessageEnum messageEnum = convertSimObject(object);

        WrapperEnumValue wrapperEnumValue = WrapperEnumValue.newBuilder()
                .setValue(messageEnum.getValueDescriptor().getName())
                .setEnumTypeUrl(messageEnum.getDescriptorForType().getFullName()).build();

        return wrapperEnumValue;
    }

    private ProtocolMessageEnum getEnumFromMessage(WrapperEnumValue enumValue) {
        String typeUrl = enumValue.getEnumTypeUrl();
        String value = enumValue.getValue();

        if (this.data.typeUrlToEnumMap.containsKey(typeUrl)) {

            ProtocolMessageEnum defaultInstance = this.data.typeUrlToEnumMap.get(typeUrl);
            Class<? extends ProtocolMessageEnum> classRef = defaultInstance.getClass();

            try {
                return (ProtocolMessageEnum) classRef.getMethod("valueOf", String.class).invoke(null, value);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                    | NoSuchMethodException | SecurityException e) {
                throw new RuntimeException(e);
            }

        }

        throw new RuntimeException("Unable to find corrsponding Enum type for: " + typeUrl);
    }

    private Message getMessageFromAny(Any anyValue) {

        String fullTypeUrl = anyValue.getTypeUrl();
        String[] parts = fullTypeUrl.split("/");

        if (parts.length > 2) {
            throw new RuntimeException("Malformed type url");
        }

        String typeUrl = parts[1];
        Message message;
        Class<? extends Message> classRef;

        message = this.data.descriptorMap.get(typeUrl);

        if (message == null) {
            throw new RuntimeException("No default instance was provided for: " + typeUrl);
        }

        classRef = message.getClass();
        try {
            Message unpackedMessage = anyValue.unpack(classRef);
            return unpackedMessage;
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Unable To unpack any type to given class: " + classRef.getName(), e);
        }
    }

    public <T, U> T convertInputEnum(ProtocolMessageEnum inputEnum, Class<U> superClass) {
        T convertedEnum = convertInputEnum(inputEnum);

        // verify translated object can be casted to the super class
        superClassCastCheck(convertedEnum, superClass);

        return convertedEnum;
    }

    public <T> T convertInputEnum(ProtocolMessageEnum inputEnum) {
        return getTranslatorForClass(inputEnum.getClass()).convert(inputEnum);
    }

    public <T, U> T convertInputObject(Message inputObject, Class<U> superClass) {

        T convertInputObject = convertInputObject(inputObject);

        // verify translated object can be casted to the super class
        superClassCastCheck(convertInputObject, superClass);

        return convertInputObject;
    }

    public <T> T convertInputObject(Message inputObject) {
        return getTranslatorForClass(inputObject.getClass()).convert(inputObject);
    }

    public <T, U> T convertSimObject(Object simObject, Class<U> superClass) {

        superClassCastCheck(simObject, superClass);

        return getTranslatorForClass(superClass).convert(simObject);
    }

    public <T> T convertSimObject(Object simObject) {
        return getTranslatorForClass(simObject.getClass()).convert(simObject);
    }

    private <T> void superClassCastCheck(Object object, Class<T> superClass) {
        validateObjectNotNull(object);

        try {
            // verify object can be casted to the super class
            superClass.cast(object);
        } catch (ClassCastException e) {
            throw new RuntimeException("Unable to cast:" + object.getClass() + " to: " + superClass, e);
        }
    }

    private void validateObjectNotNull(Object object) {
        if (object == null) {
            throw new RuntimeException("Object is null");
        }
    }

    private ITranslatorSpec getTranslatorForClass(Class<?> classRef) {
        if (this.data.classToTranslatorSpecMap.containsKey(classRef)) {
            return this.data.classToTranslatorSpecMap.get(classRef);
        }
        throw new RuntimeException(
                "No TranslatorSpec was provided for message type: " + classRef.getName());
    }

    public Builder getCloneBuilder() {
        return new Builder(data);
    }

}
