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
import gov.hhs.aspr.translation.protobuf.core.translationSpecs.PrimitiveTranslationSpecs;
import gov.hhs.aspr.translation.core.TranslationEngine;

/**
 * Protobuf TranslationEngine that allows for conversion between POJOs and
 * Protobuf Messages
 */
public class ProtobufTranslationEngine extends TranslationEngine {
    private final Data data;

    private ProtobufTranslationEngine(Data data) {
        super(data);
        this.data = data;
    }

    private static class Data extends TranslationEngine.Data {
        // this is used specifically for Any message types to pack and unpack them
        private final Map<String, Class<?>> typeUrlToClassMap = new LinkedHashMap<>();

        // these two fields are used for reading and writing Protobuf Messages to/from
        // JSON
        private Parser jsonParser;
        private Printer jsonPrinter;

        private Data() {
            super();
            /**
             * automatically include all Primitive TranslationSpecs
             * 
             * Note that these TranslationSpecs are specifically used for converting
             * primitive types that are packed inside an Any Message
             * 
             * for example, a boolean wrapped in an Any Message is of type BoolValue, and so
             * there is a translationSpec to convert from a BoolValue to a boolean and vice
             * versa
             */
            this.typeUrlToClassMap.putAll(PrimitiveTranslationSpecs.getPrimitiveTypeUrlToClassMap());
            this.classToTranslationSpecMap.putAll(PrimitiveTranslationSpecs.getPrimitiveInputTranslatorSpecMap());
            this.classToTranslationSpecMap.putAll(PrimitiveTranslationSpecs.getPrimitiveObjectTranslatorSpecMap());
            this.translationSpecs.addAll(PrimitiveTranslationSpecs.getPrimitiveTranslatorSpecs());
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

        /**
         * returns a new instance of a ProtobufTranslationEngine
         * that has a jsonParser and jsonWriter that include all the typeUrls for all
         * added TranslationSpecs and their respective Protobuf Message types
         */
        @Override
        public ProtobufTranslationEngine build() {
            TypeRegistry.Builder typeRegistryBuilder = TypeRegistry.newBuilder();
            this.descriptorSet.addAll(PrimitiveTranslationSpecs.getPrimitiveDescriptors());

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

        /**
         * Whether the jsonParser should ignore fields in the JSON that don't exist in
         * the Protobuf Message.
         * defaults to true
         */
        public Builder setIgnoringUnknownFields(boolean ignoringUnknownFields) {
            this.ignoringUnknownFields = ignoringUnknownFields;
            return this;
        }

        /**
         * Whether the jsonWriter should blanket print all values that are default. The
         * default values can be found here:
         * {@linkplain https://protobuf.dev/programming-guides/proto3/#default}
         * 
         * defaults to false
         */
        public Builder setIncludingDefaultValueFields(boolean includingDefaultValueFields) {
            this.includingDefaultValueFields = includingDefaultValueFields;
            return this;
        }

        /**
         * Contrary to {@link Builder#setIncludingDefaultValueFields(boolean)} which
         * will either print all default values or not,
         * this will set a specific field to print the default value for
         */
        public Builder addFieldToIncludeDefaultValue(FieldDescriptor fieldDescriptor) {
            this.defaultValueFieldsToPrint.add(fieldDescriptor);

            return this;
        }

        /**
         * Overriden implementation of
         * {@link TranslationEngine.Builder#addTranslationSpec(TranslationSpec)}
         * 
         * that also populates the type urls for all Protobuf Message types that exist
         * within the translationSpec
         */
        @Override
        public <I, S> Builder addTranslationSpec(TranslationSpec<I, S> translationSpec) {
            super.addTranslationSpec(translationSpec);

            populate(translationSpec.getInputObjectClass());
            return this;
        }

        /**
         * checks the class to determine if it is a ProtocolMessageEnum or a Message and
         * if so, gets the Descriptor (which is akin to a class but for a Protobuf
         * Message) for it to get the full name and add the typeUrl to the internal
         * descriptorMap and typeUrlToClassMap
         */
        private void populate(Class<?> classRef) {
            String typeUrl;
            if (ProtocolMessageEnum.class.isAssignableFrom(classRef)) {
                typeUrl = getDefaultEnum(classRef.asSubclass(ProtocolMessageEnum.class)).getDescriptorForType()
                        .getFullName();
                this.data.typeUrlToClassMap.putIfAbsent(typeUrl, classRef);
                return;
            }

            if (Message.class.isAssignableFrom(classRef)) {
                Message message = getDefaultMessage(classRef.asSubclass(Message.class));
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

        /**
         * given a Class ref to a Protobuf Message, get the defaultInstance of it
         */
        private Message getDefaultMessage(Class<? extends Message> classRef) {
            try {
                Method method = classRef.getMethod("getDefaultInstance");
                return (Message) method.invoke(null);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * given a Class ref to a ProtocolMessageEnum, get the default value for it,
         * enum number 0 within the proto enum
         */
        private ProtocolMessageEnum getDefaultEnum(Class<? extends ProtocolMessageEnum> classRef) {
            try {
                Method method = classRef.getMethod("forNumber", int.class);
                return (ProtocolMessageEnum) method.invoke(null, 0);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Returns a new builder
     */
    public static Builder builder() {
        return new Builder(new Data());
    }

    /**
     * write output implementation
     * 
     * Will first convert the object, if needed
     */
    public <U, M extends U> void writeOutput(Writer writer, M appObject, Optional<Class<U>> superClass) {
        Message message;
        if (Message.class.isAssignableFrom(appObject.getClass())) {
            message = Message.class.cast(appObject);
        } else if (superClass.isPresent()) {
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

    public <T> T getObjectFromAny(Any anyValue) {
        return convertObject(anyValue);
    }

    public Any getAnyFromObject(Object object) {
        return convertObjectAsUnsafeClass(object, Any.class);
    }

    public <U, M extends U> Any getAnyFromObjectAsSafeClass(M object, Class<U> parentClassRef) {
        U convertedObject = convertObjectAsSafeClass(object, parentClassRef);

        return convertObjectAsUnsafeClass(convertedObject, Any.class);
    }

    public Class<?> getClassFromTypeUrl(String typeUrl) {
        if (this.data.typeUrlToClassMap.containsKey(typeUrl)) {
            return this.data.typeUrlToClassMap.get(typeUrl);
        }

        throw new RuntimeException("Unable to find corrsponding Enum type for: " + typeUrl);
    }

}
