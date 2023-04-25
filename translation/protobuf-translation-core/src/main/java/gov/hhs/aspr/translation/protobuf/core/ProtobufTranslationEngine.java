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

import gov.hhs.aspr.translation.core.TranslationSpec;
import gov.hhs.aspr.translation.protobuf.core.translationSpecs.PrimitiveTranslatorSpecs;
import gov.hhs.aspr.translation.core.TranslationEngine;

public class ProtobufTranslationEngine extends TranslationEngine {
    private final Data data;

    private ProtobufTranslationEngine(Data data) {
        super(data);
        this.data = data;
    }

    private static class Data extends TranslationEngine.Data {
        private final Map<String, Class<?>> typeUrlToClassMap = new LinkedHashMap<>();

        private Parser jsonParser;
        private Printer jsonPrinter;

        private Data() {
            super();
            this.typeUrlToClassMap.putAll(PrimitiveTranslatorSpecs.getPrimitiveTypeUrlToClassMap());
            this.classToTranslationSpecMap.putAll(PrimitiveTranslatorSpecs.getPrimitiveInputTranslatorSpecMap());
            this.classToTranslationSpecMap.putAll(PrimitiveTranslatorSpecs.getPrimitiveObjectTranslatorSpecMap());
            this.translationSpecs.addAll(PrimitiveTranslatorSpecs.getPrimitiveTranslatorSpecs());
        }
    }

    public static class Builder extends TranslationEngine.Builder {
        private ProtobufTranslationEngine.Data data;
        private Set<Descriptor> descriptorSet = new LinkedHashSet<>();
        private final Set<FieldDescriptor> defaultValueFieldsToPrint = new LinkedHashSet<>();
        private boolean ignoringUnknownFields = true;
        private boolean includingDefaultValueFields = false;

        private Builder(ProtobufTranslationEngine.Data data) {
            super(data);
            this.data = data;
        }

        public TranslationEngine build() {
            TypeRegistry.Builder typeRegistryBuilder = TypeRegistry.newBuilder();
            this.descriptorSet.addAll(PrimitiveTranslatorSpecs.getPrimitiveDescriptors());

            this.descriptorSet.forEach((descriptor) -> {
                typeRegistryBuilder.add(descriptor);
            });

            TypeRegistry registry = typeRegistryBuilder.build();

            Parser parser = JsonFormat.parser().usingTypeRegistry(registry);
            if (this.ignoringUnknownFields) {
                parser = parser.ignoringUnknownFields();
            }
            this.data.jsonParser = parser;

            Printer printer = JsonFormat.printer().usingTypeRegistry(registry);

            if (!this.defaultValueFieldsToPrint.isEmpty()) {
                printer = printer.includingDefaultValueFields(this.defaultValueFieldsToPrint);
            }

            if (this.includingDefaultValueFields) {
                printer = printer.includingDefaultValueFields();
            }
            this.data.jsonPrinter = printer;

            return new ProtobufTranslationEngine(this.data);
        }

        public Builder setIgnoringUnknownFields(boolean ignoringUnknownFields) {
            this.ignoringUnknownFields = ignoringUnknownFields;
            return this;
        }

        public Builder setIncludingDefaultValueFields(boolean includingDefaultValueFields) {
            this.includingDefaultValueFields = includingDefaultValueFields;
            return this;
        }

        public Builder addFieldToIncludeDefaultValue(FieldDescriptor fieldDescriptor) {
            this.defaultValueFieldsToPrint.add(fieldDescriptor);

            return this;
        }

        @Override
        public <I, S> Builder addTranslatorSpec(TranslationSpec<I, S> translationSpec) {
            super.addTranslatorSpec(translationSpec);

            populate(translationSpec.getInputObjectClass());
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

    public <U, M extends U> void writeOutput(Writer writer, M appObject, Optional<Class<U>> superClass) {
        Message message;

        if (superClass.isPresent()) {
            message = convertObjectAsSafeClass(appObject, superClass.get());
        } else {
            message = convertObject(appObject);
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
            return convertObject(message);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    public <U, M extends U> Any getAnyFromObjectAsSafeClass(M object, Class<U> parentClassRef) {
        U convertedObject = convertObjectAsSafeClass(object, parentClassRef);

        return convertObjectAsUnsafeClass(convertedObject, Any.class);
    }

    public Any getAnyFromObject(Object object) {
        return convertObjectAsUnsafeClass(object, Any.class);
    }

    public <T> T getObjectFromAny(Any anyValue) {
        return convertObject(anyValue);
    }

    public Class<?> getClassFromTypeUrl(String typeUrl) {
        if (this.data.typeUrlToClassMap.containsKey(typeUrl)) {
            return this.data.typeUrlToClassMap.get(typeUrl);
        }

        throw new RuntimeException("Unable to find corrsponding Enum type for: " + typeUrl);
    }

}
