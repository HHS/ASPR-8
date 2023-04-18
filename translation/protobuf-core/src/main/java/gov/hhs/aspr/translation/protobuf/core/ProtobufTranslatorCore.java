package gov.hhs.aspr.translation.protobuf.core;

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
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.ProtocolMessageEnum;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Parser;
import com.google.protobuf.util.JsonFormat.Printer;
import com.google.protobuf.util.JsonFormat.TypeRegistry;

import gov.hhs.aspr.translation.core.AbstractTranslatorSpec;
import gov.hhs.aspr.translation.core.TranslatorCore;
import gov.hhs.aspr.translation.protobuf.core.input.WrapperEnumValue;
import gov.hhs.aspr.translation.protobuf.core.translatorSpecs.PrimitiveTranslatorSpecs;

public class ProtobufTranslatorCore extends TranslatorCore {
    private final Data data;

    private ProtobufTranslatorCore(Data data) {
        super(data);
        this.data = data;
    }

    private static class Data extends TranslatorCore.Data {
        private final Map<String, Class<?>> typeUrlToClassMap = new LinkedHashMap<>();
        private final Set<FieldDescriptor> defaultValueFieldsToPrint = new LinkedHashSet<>();

        private Parser jsonParser;
        private Printer jsonPrinter;
        private boolean ignoringUnknownFields = true;
        private boolean includingDefaultValueFields = false;

        private Data() {
            super();
            this.typeUrlToClassMap.putAll(PrimitiveTranslatorSpecs.getPrimitiveTypeUrlToClassMap());
            this.classToTranslatorSpecMap.putAll(PrimitiveTranslatorSpecs.getPrimitiveInputTranslatorSpecMap());
            this.classToTranslatorSpecMap.putAll(PrimitiveTranslatorSpecs.getPrimitiveObjectTranslatorSpecMap());
            this.translatorSpecs.addAll(PrimitiveTranslatorSpecs.getPrimitiveObjectTranslatorSpecMap().values());
        }
    }

    public static class Builder extends TranslatorCore.Builder {
        private ProtobufTranslatorCore.Data data;
        private Set<Descriptor> descriptorSet = new LinkedHashSet<>();

        private Builder(ProtobufTranslatorCore.Data data) {
            super(data);
            this.data = data;
        }

        public TranslatorCore build() {
            TypeRegistry.Builder typeRegistryBuilder = TypeRegistry.newBuilder();
            this.descriptorSet.addAll(PrimitiveTranslatorSpecs.getPrimitiveDescriptors());

            this.descriptorSet.forEach((descriptor) -> {
                typeRegistryBuilder.add(descriptor);
            });

            TypeRegistry registry = typeRegistryBuilder.build();

            Parser parser = JsonFormat.parser().usingTypeRegistry(registry);
            if (this.data.ignoringUnknownFields) {
                parser = parser.ignoringUnknownFields();
            }
            this.data.jsonParser = parser;

            Printer printer = JsonFormat.printer().usingTypeRegistry(registry);

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

        @Override
        public <I, S> Builder addTranslatorSpec(AbstractTranslatorSpec<I, S> translatorSpec) {
            super.addTranslatorSpec(translatorSpec);

            populate(translatorSpec.getInputObjectClass());
            return this;
        }

        private void populate(Class<?> classRef) {
            String typeUrl;
            if (Enum.class.isAssignableFrom(classRef)) {
                typeUrl = getDefaultEnum(classRef).getDescriptorForType().getFullName();
                this.data.typeUrlToClassMap.putIfAbsent(typeUrl, classRef);
                return;
            }

            if (Message.class.isAssignableFrom(classRef)) {
                Message message = getDefaultMessage(classRef);
                typeUrl = message.getDescriptorForType().getFullName();

                this.data.typeUrlToClassMap.putIfAbsent(typeUrl, classRef);
                this.descriptorSet.add(message.getDescriptorForType());

                Set<FieldDescriptor> fieldDescriptors = message.getAllFields().keySet();

                if (fieldDescriptors.isEmpty()) {
                    return;
                }

                for (FieldDescriptor fieldDescriptor : fieldDescriptors) {
                    if (fieldDescriptor.getJavaType() == JavaType.MESSAGE) {
                        Message subMessage = (Message) message.getField(fieldDescriptor);
                        if (!(subMessage.getDescriptorForType() == Any.getDescriptor())) {
                            populate(subMessage.getClass());
                        }
                    }
                }
            }

        }

        private Message getDefaultMessage(Class<?> classRef) {
            try {
                Method method = classRef.getMethod("getDefaultInstance");
                return (Message) method.invoke(null);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        private ProtocolMessageEnum getDefaultEnum(Class<?> classRef) {
            try {
                Method method = classRef.getMethod("forNumber", int.class);
                return (ProtocolMessageEnum) method.invoke(null, 0);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                throw new RuntimeException(e);
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

    public <M extends U, U> void writeOutput(Writer writer, M simObject, Optional<Class<U>> superClass) {
        Message message;

        if (superClass.isPresent()) {
            message = convertSimObject(simObject, superClass.get());
        } else {
            message = convertSimObject(simObject);
        }
        writeOutput(writer, message);
    }

    private <U extends Message> void writeOutput(Writer writer, U message) {
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

    public <T, U> T readInput(Reader reader, Class<U> inputClassRef) {
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
            Message message = builder.build();
            if (this.debug) {
                printJsonToConsole(message);
            }
            return convertInputObject(message);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    public <M extends U, U> Any getAnyFromObject(M object, Class<U> superClass) {
        if (Enum.class.isAssignableFrom(object.getClass())) {
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
            return convertInputObject(getEnumFromMessage((WrapperEnumValue) anyMessage), superClass);
        }

        return convertInputObject(anyMessage, superClass);
    }

    public <T> T getObjectFromAny(Any anyValue) {
        Message anyMessage = getMessageFromAny(anyValue);

        if (anyMessage.getDescriptorForType() == WrapperEnumValue.getDescriptor()) {
            return convertInputObject(getEnumFromMessage((WrapperEnumValue) anyMessage));
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

    private Enum<?> getEnumFromMessage(WrapperEnumValue enumValue) {
        String typeUrl = enumValue.getEnumTypeUrl();
        String value = enumValue.getValue();

        if (this.data.typeUrlToClassMap.containsKey(typeUrl)) {
            Class<?> classRef = this.data.typeUrlToClassMap.get(typeUrl);

            try {
                return (Enum<?>) classRef.getMethod("valueOf", String.class).invoke(null, value);
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

        if (parts.length != 2) {
            throw new RuntimeException("Malformed type url");
        }

        String typeUrl = parts[1];
        Class<?> classRef = this.data.typeUrlToClassMap.get(typeUrl);
        Class<? extends Message> messageClassRef;

        if (!(Message.class.isAssignableFrom(classRef))) {
            throw new RuntimeException("No default instance was provided for: " + typeUrl);
        }
        messageClassRef = classRef.asSubclass(Message.class);

        try {
            Message unpackedMessage = anyValue.unpack(messageClassRef);
            return unpackedMessage;
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Unable To unpack any type to given class: " + classRef.getName(), e);
        }
    }

}
