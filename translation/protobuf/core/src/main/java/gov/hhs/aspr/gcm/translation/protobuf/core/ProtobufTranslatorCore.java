package gov.hhs.aspr.gcm.translation.protobuf.core;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
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
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Parser;
import com.google.protobuf.util.JsonFormat.Printer;
import com.google.protobuf.util.JsonFormat.TypeRegistry;

import gov.hhs.aspr.gcm.translation.core.AbstractTranslatorSpec;
import gov.hhs.aspr.gcm.translation.core.TranslatorCore;
import gov.hhs.aspr.gcm.translation.protobuf.core.input.WrapperEnumValue;
import gov.hhs.aspr.gcm.translation.protobuf.core.translatorSpecs.PrimitiveTranslatorSpecs;

public class ProtobufTranslatorCore extends TranslatorCore {
    private final ProtobufTranslatorCoreData data;

    private ProtobufTranslatorCore(ProtobufTranslatorCoreData data) {
        super(data);
        this.data = data;
    }


    private static class ProtobufTranslatorCoreData extends TranslatorCore.TranslatorCoreData {
        private final Map<String, Message> descriptorMap = new LinkedHashMap<>();
        private final Map<String, ProtocolMessageEnum> typeUrlToEnumMap = new LinkedHashMap<>();
        private final Set<FieldDescriptor> defaultValueFieldsToPrint = new LinkedHashSet<>();
        private TypeRegistry registry;
        private Parser jsonParser;
        private Printer jsonPrinter;
        private boolean ignoringUnknownFields = true;
        private boolean includingDefaultValueFields = false;

        private ProtobufTranslatorCoreData() {
            super();
            this.descriptorMap.putAll(PrimitiveTranslatorSpecs.getPrimitiveTypeUrlToMessageMap());
            this.classToTranslatorSpecMap.putAll(PrimitiveTranslatorSpecs.getPrimitiveInputTranslatorSpecMap());
            this.classToTranslatorSpecMap.putAll(PrimitiveTranslatorSpecs.getPrimitiveObjectTranslatorSpecMap());
            this.translatorSpecs.addAll(PrimitiveTranslatorSpecs.getPrimitiveObjectTranslatorSpecMap().values());
        }
    }

    public static class Builder extends TranslatorCore.Builder {
        private ProtobufTranslatorCoreData data;

        private Builder(ProtobufTranslatorCoreData data) {
            super(data);
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

            return new ProtobufTranslatorCore(this.data);
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

        @Override
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
        return new Builder(new ProtobufTranslatorCoreData());
    }
 
    public Parser getJsonParser() {
        return this.data.jsonParser;
    }

    public Printer getJsonPrinter() {
        return this.data.jsonPrinter;
    }

    public <M extends U, U> void writeJson(Writer writer, M simObject, Optional<Class<U>> superClass) {
        Message message;

        if (superClass.isPresent()) {
            message = convertSimObject(simObject, superClass.get());
        } else {
            message = convertSimObject(simObject);
        }
        writeJson(writer, message);
    }

    private <U extends Message> void writeJson(Writer writer, U message) {
        try {
            String messageToWrite = this.data.jsonPrinter.print(message);
            writer.write(messageToWrite);
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

    public <T, U> T readJson(Reader reader, Class<U> inputClassRef) {
        JsonObject jsonObject = JsonParser.parseReader(new JsonReader(reader)).getAsJsonObject();
        return parseJson(jsonObject, inputClassRef);
    }

    private <T, U> T parseJson(JsonObject inputJson, Class<U> inputClassRef) {
        JsonObject jsonObject = inputJson.deepCopy();

        Message.Builder builder;
        try {
            Method buildermethod = inputClassRef.getDeclaredMethod("newBuilder");

            builder = (com.google.protobuf.Message.Builder) buildermethod.invoke(null);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException e) {
            throw new RuntimeException("Failed to find method or failed to invoke method", e);
        }

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

    public <M extends U, U> Any getAnyFromObject(M object, Class<U> superClass) {
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

    private Any packMessage(Message messageToPack) {
        return Any.pack(messageToPack);
    }

    public <T extends U, U> T getObjectFromAny(Any anyValue, Class<U> superClass) {
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

    private <T extends U, U> T convertInputEnum(ProtocolMessageEnum inputEnum, Class<U> superClass) {
        T convertedEnum = convertInputEnum(inputEnum);

        // verify translated object can be casted to the super class
        superClassCastCheck(convertedEnum, superClass);

        return convertedEnum;
    }

    private <T> T convertInputEnum(ProtocolMessageEnum inputEnum) {
        return getTranslatorForClass(inputEnum.getClass()).convert(inputEnum);
    }
}
