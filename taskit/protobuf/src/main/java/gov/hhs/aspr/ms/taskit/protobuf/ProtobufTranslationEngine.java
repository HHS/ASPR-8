package gov.hhs.aspr.ms.taskit.protobuf;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.protobuf.Any;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.ProtocolMessageEnum;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Parser;
import com.google.protobuf.util.JsonFormat.Printer;
import com.google.protobuf.util.JsonFormat.TypeRegistry;

import gov.hhs.aspr.ms.taskit.core.CoreTranslationError;
import gov.hhs.aspr.ms.taskit.core.TranslationEngine;
import gov.hhs.aspr.ms.taskit.core.TranslationSpec;
import gov.hhs.aspr.ms.taskit.protobuf.translationSpecs.AnyTranslationSpec;
import util.errors.ContractException;

/**
 * Protobuf TranslationEngine that allows for conversion between POJOs and
 * Protobuf Messages, extends {@link TranslationEngine}
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
         * 
         * @throws ContractException
         *                           <ul>
         *                           <li>{@link ProtobufCoreTranslationError#INVALID_INPUT_CLASS}
         *                           <li>if the given inputClassRef is not assingable
         *                           from
         *                           {@linkplain Message} nor
         *                           {@linkplain ProtocolMessageEnum}</li>
         *                           <li>{@link ProtobufCoreTranslationError#INVALID_TRANSLATION_SPEC}
         *                           <li>if the given translation spec is not assignable
         *                           from
         *                           {@linkplain ProtobufTranslationSpec}</li>
         *                           </ul>
         */
        @Override
        public <I, S> Builder addTranslationSpec(TranslationSpec<I, S> translationSpec) {
            if (!ProtobufTranslationSpec.class.isAssignableFrom(translationSpec.getClass())) {
                throw new ContractException(ProtobufCoreTranslationError.INVALID_TRANSLATION_SPEC);
            }
            super.addTranslationSpec(translationSpec);

            populate(translationSpec.getInputObjectClass());
            return this;
        }

        /**
         * checks the class to determine if it is a ProtocolMessageEnum or a Message and
         * if so, gets the Descriptor (which is akin to a class but for a Protobuf
         * Message) for it to get the full name and add the typeUrl to the internal
         * descriptorMap and typeUrlToClassMap
         * 
         * @throws ContractException
         *                           <ul>
         *                           <li>{@link ProtobufCoreTranslationError#INVALID_INPUT_CLASS}
         *                           <li>if the given inputClassRef is not assingable
         *                           from
         *                           {@linkplain Message} nor
         *                           {@linkplain ProtocolMessageEnum}</li>
         *                           </ul>
         */
        protected <U> void populate(Class<U> classRef) {
            String typeUrl;
            if (ProtocolMessageEnum.class.isAssignableFrom(classRef) && ProtocolMessageEnum.class != classRef) {
                typeUrl = getDefaultEnum(classRef.asSubclass(ProtocolMessageEnum.class)).getDescriptorForType()
                        .getFullName();
                this.data.typeUrlToClassMap.putIfAbsent(typeUrl, classRef);
                return;
            }

            if (Message.class.isAssignableFrom(classRef) && Message.class != classRef) {
                Message message = getDefaultMessage(classRef.asSubclass(Message.class));
                typeUrl = message.getDescriptorForType().getFullName();

                this.data.typeUrlToClassMap.putIfAbsent(typeUrl, classRef);
                this.descriptorSet.add(message.getDescriptorForType());
                return;
            }

            throw new ContractException(ProtobufCoreTranslationError.INVALID_INPUT_CLASS);
        }

        /**
         * given a Class ref to a Protobuf Message, get the defaultInstance of it
         */
        protected <U extends Message> U getDefaultMessage(Class<U> classRef) {
            try {
                Method method = classRef.getMethod("getDefaultInstance");
                Object obj = method.invoke(null);
                return classRef.cast(obj);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * given a Class ref to a ProtocolMessageEnum, get the default value for it,
         * enum number 0 within the proto enum
         */
        protected <U extends ProtocolMessageEnum> U getDefaultEnum(Class<U> classRef) {
            try {
                Method method = classRef.getMethod("forNumber", int.class);
                Object obj = method.invoke(null, 0);
                return classRef.cast(obj);
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

    protected Parser getJsonParser() {
        return this.data.jsonParser;
    }

    protected Printer getJsonPrinter() {
        return this.data.jsonPrinter;
    }

    protected void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * <li>write output implementation
     * 
     * <li>Will first convert the object, if needed
     * and then use the jsonPrinter to take the the converted object and write it
     * to a JSON string
     * and then pass that string to the writer to writer the JSON to an output file
     * 
     * @param <U> the type of the optional parent class of the appObject
     * @param <M> the type of the appObject
     * 
     * @throws RuntimeException
     *                          if there is an IOException during writing
     */
    protected <U, M extends U> void writeOutput(Writer writer, M appObject, Optional<Class<U>> superClass) {
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

    /**
     * takes a protobuf message, parses it into a JSON string and then writes the
     * JSON string to the output file for which the writer is defined.
     * 
     * <li>if debug is enabled in this class, it will also print the resulting
     * output to the
     * console</li>
     * 
     * @param <U> the type of the Message
     * 
     * @throws RuntimeException
     *                          if there is an IOException during writing
     */
    private <U extends Message> void writeOutput(Writer writer, U message) {
        try {
            String messageToWrite = this.data.jsonPrinter.print(message);

            if (debug) {
                printJsonToConsole(messageToWrite);
            }

            writer.write(messageToWrite);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void printJsonToConsole(String jsonString) {
        System.out.println(jsonString);
    }

    /**
     * Given a reader and a classRef, will read the JSON from the reader, parse it
     * into a JSON Object,
     * merge the resulting JSON Object into the equivalent Protobuf Message and then
     * convert
     * that Protobuf Message to the equivalent AppObject
     * 
     * <li>if debug is set on this class, will also print the resulting read in
     * Protobuf Message to console
     * 
     * @param <U> the type of the inputClass
     * @param <T> the return type
     * 
     * @throws RuntimeException
     *                           <ul>
     *                           <li>if there is an issue getting the builder method
     *                           from
     *                           the inputClassRef
     *                           </li>
     *                           <li>if there is an issue merging the read in JSON
     *                           object into the resulting Protobuf Message builder
     *                           </li>
     *                           </ul>
     * 
     * @throws ContractException
     *                           <ul>
     *                           <li>{@linkplain ProtobufCoreTranslationError#INVALID_READ_INPUT_CLASS_REF}
     *                           if the given inputClassRef is not assingable from
     *                           {@linkplain Message}</li>
     *                           </ul>
     */
    protected <T, U> T readInput(Reader reader, Class<U> inputClassRef) {
        if (!Message.class.isAssignableFrom(inputClassRef)) {
            throw new ContractException(ProtobufCoreTranslationError.INVALID_READ_INPUT_CLASS_REF);
        }

        JsonObject jsonObject = JsonParser.parseReader(new JsonReader(reader)).getAsJsonObject();
        return parseJson(jsonObject, inputClassRef.asSubclass(Message.class));
    }

    /**
     * Given a jsonObject and a inputClassRef, creates a builder for the inputClass
     * type and then merges the JSON object into the resulting builder
     * 
     * <li>if debug is set on this class, will also print the resulting read in
     * Protobuf Message to console
     * 
     * @param <T> the return type
     * @param <U> the type of the inputClass, that is a child of {@link Message}
     * 
     * @throws RuntimeException
     *                          <ul>
     *                          <li>if there is an issue getting the builder method
     *                          from
     *                          the inputClassRef
     *                          </li>
     *                          <li>if there is an issue merging the read in JSON
     *                          object into the resulting Protobuf Message builder
     *                          </li>
     *                          </ul>
     */
    protected <T, U extends Message> T parseJson(JsonObject inputJson, Class<U> inputClassRef) {
        JsonObject jsonObject = inputJson.deepCopy();

        Message.Builder builder = getBuilderForMessage(inputClassRef);

        try {
            this.data.jsonParser.merge(jsonObject.toString(), builder);

            Message message = builder.build();
            if (debug) {
                printJsonToConsole(this.data.jsonPrinter.print(message));
            }

            return convertObject(message);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    protected <U> Message.Builder getBuilderForMessage(Class<U> messageClass) {

        Method[] messageMethods = messageClass.getDeclaredMethods();

        List<Method> newBuilderMethods = new ArrayList<>();
        for (Method method : messageMethods) {
            if (method.getName().equals("newBuilder")) {
                newBuilderMethods.add(method);
            }
        }

        if (newBuilderMethods.isEmpty()) {
            throw new RuntimeException("The method \"newBuilder\" does not exist");
        }

        for (Method method : newBuilderMethods) {
            if (method.getParameterCount() == 0) {
                try {
                    return (com.google.protobuf.Message.Builder) method.invoke(null);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        throw new RuntimeException(
                "\"newBuilder\" method exists, but it requires arugments, when it is expected to require 0 arugments");
    }

    /**
     * Given an object of type {@link Any}, will convert it to the resulting object
     * 
     * <li>Will ultimately use the {@link AnyTranslationSpec} to accomplish this
     * 
     * @param <T> the return type
     */
    public <T> T getObjectFromAny(Any anyValue) {
        return convertObject(anyValue);
    }

    /**
     * Given an object , will convert it to an {@link Any} type
     * 
     * <li>Will use the {@link AnyTranslationSpec} to accomplish this
     */
    public Any getAnyFromObject(Object object) {
        return convertObjectAsUnsafeClass(object, Any.class);
    }

    /**
     * Given an object , will convert it to an {@link Any} type
     * 
     * <li>This method call differs from {@link #getAnyFromObject(Object)} in that
     * it will first convert the object using the safe parent class by calling
     * {@link #convertObjectAsSafeClass(Object, Class)}
     * and will then use the {@link AnyTranslationSpec} to wrap the resulting
     * converted object in an {@link Any}
     * 
     * @param <U> the parent Class
     * @param <M> the object class
     * 
     * @throws ContractException
     *                           <ul>
     *                           <li>{@linkplain CoreTranslationError#UNKNOWN_TRANSLATION_SPEC}
     *                           if no translationSpec was provided for the given
     *                           parentClassRef
     */
    public <U, M extends U> Any getAnyFromObjectAsSafeClass(M object, Class<U> parentClassRef) {
        U convertedObject = convertObjectAsSafeClass(object, parentClassRef);

        return convertObjectAsUnsafeClass(convertedObject, Any.class);
    }

    /**
     * Given a typeUrl, returns the associated Protobuf Message type Class, if it
     * has been previously provided
     * 
     * @throws ContractException
     *                           <ul>
     *                           <li>{@linkplain ProtobufCoreTranslationError#UNKNOWN_TYPE_URL}
     *                           if the given type url does not exist. This could be
     *                           because the type url was never provided or the type
     *                           url itself is malformed
     */
    public Class<?> getClassFromTypeUrl(String typeUrl) {
        if (this.data.typeUrlToClassMap.containsKey(typeUrl)) {
            return this.data.typeUrlToClassMap.get(typeUrl);
        }

        throw new ContractException(ProtobufCoreTranslationError.UNKNOWN_TYPE_URL,
                "Unable to find corrsponding class for: " + typeUrl);
    }

}
