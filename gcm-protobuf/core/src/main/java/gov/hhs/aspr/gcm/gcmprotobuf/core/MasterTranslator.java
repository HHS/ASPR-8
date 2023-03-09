package gov.hhs.aspr.gcm.gcmprotobuf.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.protobuf.Any;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.ProtocolMessageEnum;
import com.google.protobuf.TypeRegistry;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Parser;
import com.google.protobuf.util.JsonFormat.Printer;

import gov.hhs.aspr.gcm.gcmprotobuf.core.AbstractEnumTranslator.EnumInstance;
import gov.hhs.aspr.gcm.gcmprotobuf.core.input.WrapperEnumValue;
import gov.hhs.aspr.gcm.gcmprotobuf.core.translators.PrimitiveTranslators;
import nucleus.PluginData;

public class MasterTranslator {

    private final Data data;
    private boolean debug = true;
    private boolean isInitialized = false;

    private MasterTranslator(Data data) {
        this.data = data;
    }

    private static class Data {
        private final Map<Descriptor, Message> descriptorMap = new LinkedHashMap<>();
        private final Map<EnumDescriptor, EnumInstance> enumDescriptorMap = new LinkedHashMap<>();
        private final Map<String, EnumDescriptor> typeUrlToEnumDescriptor = new LinkedHashMap<>();
        private final Map<Class<?>, ITranslator> classToTranslatorMap = new LinkedHashMap<>();
        private final Set<ITranslator> translators = new LinkedHashSet<>();
        private final Set<FieldDescriptor> defaultValueFieldsToPrint = new LinkedHashSet<>();
        private TypeRegistry registry;
        private Parser jsonParser;
        private Printer jsonPrinter;
        private boolean ignoringUnknownFields = true;
        private boolean includingDefaultValueFields = false;

        private Data() {
            this.descriptorMap.putAll(PrimitiveTranslators.getPrimitiveDescriptorToMessageMap());
            this.classToTranslatorMap.putAll(PrimitiveTranslators.getPrimitiveInputTranslatorMap());
            this.classToTranslatorMap.putAll(PrimitiveTranslators.getPrimitiveObjectTranslatorMap());
            this.translators.addAll(PrimitiveTranslators.getPrimitiveObjectTranslatorMap().values());
        }
    }

    public static class Builder {
        private Data data;

        private Builder(Data data) {
            this.data = data;
        }

        public MasterTranslator build() {
            this.data.registry = TypeRegistry.newBuilder().add(this.data.descriptorMap.keySet()).build();

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

            return new MasterTranslator(this.data);
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

        public <I extends ProtocolMessageEnum, S> Builder addTranslator(AbstractEnumTranslator<I, S> translator) {
            this.data.classToTranslatorMap.putIfAbsent(translator.getInputObjectClass(),
                    translator);
            this.data.classToTranslatorMap.putIfAbsent(translator.getSimObjectClass(), translator);

            EnumDescriptor enumDescriptor = translator.getDescriptorForInputObject();

            this.data.enumDescriptorMap.putIfAbsent(enumDescriptor, translator.getEnumInstance());
            this.data.typeUrlToEnumDescriptor.putIfAbsent(enumDescriptor.getFullName(), enumDescriptor);

            this.data.translators.add(translator);
            return this;
        }

        public <I extends Message, S> Builder addTranslator(AbstractTranslator<I, S> translator) {
            this.data.classToTranslatorMap.putIfAbsent(translator.getInputObjectClass(),
                    translator);
            this.data.classToTranslatorMap.putIfAbsent(translator.getSimObjectClass(), translator);

            this.data.translators.add(translator);
            populate(translator.getDefaultInstanceForInputObject());
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

    public void init() {
        this.data.translators.forEach((translator) -> translator.init(this));

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

    public <T extends PluginData, U extends Message> void printJson(Writer writer, T pluginData) {
        U message = convertSimObject(pluginData);
        writeJson(writer, message);
    }

    public <U extends Message> void printJson(Writer writer, Object simObject) {
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

    public <T, U extends Message.Builder> T readJson(String inputFileName, U builder) {
        InputStream in = null;
        try {
            in = new FileInputStream(Paths.get(inputFileName).toFile());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        JsonReader jsonReader = new JsonReader(
                new InputStreamReader(in));
        JsonObject jsonObject = JsonParser.parseReader(jsonReader).getAsJsonObject();

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
        return Any.pack(convertSimObject(object, superClass));
    }

    public Any getAnyFromObject(Object object) {
        if (Enum.class.isAssignableFrom(object.getClass())) {
            ProtocolMessageEnum messageEnum = convertSimObject(object);
            EnumValueDescriptor enumValueDescriptor = messageEnum.getValueDescriptor();

            WrapperEnumValue wrapperEnumValue = WrapperEnumValue.newBuilder().setValue(enumValueDescriptor.getName())
                    .setEnumTypeUrl(messageEnum.getDescriptorForType().getFullName()).build();

            return Any.pack(wrapperEnumValue);
        }
        return Any.pack(convertSimObject(object));
    }

    public <T> T getObjectFromAny(Any anyValue, Class<?> superClass) {
        Message anyMessage = getMessageFromAny(anyValue);

        if (anyMessage.getDescriptorForType() == WrapperEnumValue.getDescriptor()) {
            WrapperEnumValue enumValue = (WrapperEnumValue) anyMessage;

            String typeUrl = enumValue.getEnumTypeUrl();
            String value = enumValue.getValue();

            if (this.data.typeUrlToEnumDescriptor.containsKey(typeUrl)) {
                EnumDescriptor enumDescriptor = this.data.typeUrlToEnumDescriptor.get(typeUrl);
                if (this.data.enumDescriptorMap.containsKey(enumDescriptor)) {
                    ProtocolMessageEnum messageEnum = this.data.enumDescriptorMap.get(enumDescriptor)
                            .getFromString(value);
                    return convertInputEnum(messageEnum, superClass);
                }
            }
        }

        return convertInputObject(anyMessage, superClass);
    }

    public <T> T getObjectFromAny(Any anyValue) {
        Message anyMessage = getMessageFromAny(anyValue);

        if (anyMessage.getDescriptorForType() == WrapperEnumValue.getDescriptor()) {
            WrapperEnumValue enumValue = (WrapperEnumValue) anyMessage;

            String typeUrl = enumValue.getEnumTypeUrl();
            String value = enumValue.getValue();

            if (this.data.typeUrlToEnumDescriptor.containsKey(typeUrl)) {
                EnumDescriptor enumDescriptor = this.data.typeUrlToEnumDescriptor.get(typeUrl);
                if (this.data.enumDescriptorMap.containsKey(enumDescriptor)) {
                    ProtocolMessageEnum messageEnum = this.data.enumDescriptorMap.get(enumDescriptor)
                            .getFromString(value);
                    return convertInputEnum(messageEnum);
                }
            }
        }
        return convertInputObject(anyMessage);
    }

    private Message getMessageFromAny(Any anyValue) {

        String typeUrl = anyValue.getTypeUrl();

        Message message;
        Class<? extends Message> classRef;

        try {
            Descriptor messageDescriptor = this.data.registry.getDescriptorForTypeUrl(typeUrl);
            message = this.data.descriptorMap.get(messageDescriptor);
            if (message == null) {
                throw new RuntimeException("No default instance was provided for: " + messageDescriptor.getName()
                        + ". This occurs when the above message type is defined in the same file as another message type who's descriptor is added to this Translator, "
                        + "but who's own descriptor is not explicitly added to this Translator. "
                        + "For example, if you had a proto file with two message definitions- "
                        + "Wombat and Cat and you added the descriptor for Cat to this Translator, "
                        + "the json parser for this Translator will internally also add the descriptor for Wombat, "
                        + "so the parser knows about Wombat, which allows the type to be properly parsed, "
                        + "but this Translator does not, thus resulting in a null message here.");
            }
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("No corresponding message definition was found for: " + typeUrl, e);
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

        U superEnum;
        try {
            // we want to make sure that the resulting object can be casted to the super
            // class
            // But we still want to return the actual class so that the reference does not
            // get changed.
            superEnum = superClass.cast(convertedEnum);
        } catch (ClassCastException e) {
            throw new RuntimeException("Unable to cast:" + convertedEnum.getClass() + " to: " + superClass, e);
        }

        if (superEnum == null) {
            throw new RuntimeException("Unable to cast:" + convertedEnum.getClass() + " to: " + superClass);
        }

        return convertedEnum;
    }

    public <T> T convertInputEnum(ProtocolMessageEnum inputEnum) {
        if (this.data.classToTranslatorMap.containsKey(inputEnum.getClass())) {
            return this.data.classToTranslatorMap.get(inputEnum.getClass()).convert(inputEnum);
        }
        throw new RuntimeException(
                "No conversion translator was provided for message type: "
                        + inputEnum.getDescriptorForType().getName());
    }

    public <T, U> T convertInputObject(Message inputObject, Class<U> superClass) {

        T convertInputObject = convertInputObject(inputObject);
        // check if there is a translation method avaiable

        U superSimObject;
        try {
            // we want to make sure that the resulting object can be casted to the super
            // class
            // But we still want to return the actual class so that the reference does not
            // get changed.
            superSimObject = superClass.cast(convertInputObject);
        } catch (ClassCastException e) {
            throw new RuntimeException("Unable to cast:" + convertInputObject.getClass() + " to: " + superClass, e);
        }

        if (superSimObject == null) {
            throw new RuntimeException("Unable to cast:" + convertInputObject.getClass() + " to: " + superClass);
        }

        return convertInputObject;

    }

    public <T> T convertInputObject(Message inputObject) {
        // check if there is a translation method avaiable
        if (this.data.classToTranslatorMap.containsKey(inputObject.getClass())) {
            return this.data.classToTranslatorMap.get(inputObject.getClass()).convert(inputObject);
        }
        throw new RuntimeException(
                "No conversion translator was provided for message type: "
                        + inputObject.getDescriptorForType().getName());
    }

    public <T, U> T convertSimObject(Object simObject, Class<U> superClass) {

        U superSimObject;

        try {
            // this cast ensures that the object is translated using the upper most marker
            // class, rather than the actual class
            superSimObject = superClass.cast(simObject);
        } catch (ClassCastException e) {
            throw new RuntimeException("Unable to cast:" + simObject.getClass() + " to: " + superClass, e);
        }

        if (superSimObject == null) {
            throw new RuntimeException("Unable to cast:" + simObject.getClass() + " to: " + superClass);
        }

        if (this.data.classToTranslatorMap.containsKey(superClass)) {
            return this.data.classToTranslatorMap.get(superClass).convert(simObject);
        }
        throw new RuntimeException(
                "No conversion translator was provided for message type: " + superClass.getName());
    }

    public <T> T convertSimObject(Object simObject) {
        if (this.data.classToTranslatorMap.containsKey(simObject.getClass())) {
            return this.data.classToTranslatorMap.get(simObject.getClass()).convert(simObject);
        }
        throw new RuntimeException(
                "No conversion translator was provided for message type: " + simObject.getClass().getName());
    }

    public Builder getCloneBuilder() {
        return new Builder(data);
    }

}
