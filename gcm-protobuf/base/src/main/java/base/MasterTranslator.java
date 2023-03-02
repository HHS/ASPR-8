package base;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
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
import com.google.protobuf.TypeRegistry;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Parser;
import com.google.protobuf.util.JsonFormat.Printer;

import base.translators.PrimitiveTranslators;

public class MasterTranslator {

    protected final Data data;

    protected MasterTranslator(Data data) {
        this.data = data;
    }

    protected static class Data {
        protected final Map<Descriptor, Message> descriptorMap = new LinkedHashMap<>();
        protected final Map<Descriptor, ITranslator> descriptorToTranslatorMap = new LinkedHashMap<>();
        protected final Map<Class<?>, ITranslator> objectToTranslatorMap = new LinkedHashMap<>();
        protected TypeRegistry registry;
        protected Parser jsonParser;
        protected Printer jsonPrinter;
        protected boolean ignoringUnknownFields = true;
        protected boolean includingDefaultValueFields = true;

        protected Data() {
            this.descriptorMap.putAll(PrimitiveTranslators.getPrimitiveDescriptorToMessageMap());
            this.descriptorToTranslatorMap.putAll(PrimitiveTranslators.getPrimitiveDescriptorToTranslatorMap());
            this.objectToTranslatorMap.putAll(PrimitiveTranslators.getPrimitiveObjectToTranslatorMap());
        }
    }

    public static class Builder {
        protected Data data;

        protected Builder(Data data) {
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

        public Builder addDescriptor(Message message) {
            populate(message);
            return this;
        }

        public <I extends Message, S> Builder addCustomTranslator(AbstractTranslator<I, S> customTranslator) {
            this.data.descriptorToTranslatorMap.putIfAbsent(customTranslator.getDescriptorForInputObject(),
                    customTranslator);
            this.data.objectToTranslatorMap.putIfAbsent(customTranslator.getSimObjectClass(), customTranslator);
            populate(customTranslator.getDefaultInstanceForInputObject());
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
        this.data.descriptorToTranslatorMap.values().forEach((translator) -> translator.init(this));
        this.data.objectToTranslatorMap.values().forEach((translator) -> translator.init(this));
    }

    public Parser getJsonParser() {
        return this.data.jsonParser;
    }

    public Printer getJsonPrinter() {
        return this.data.jsonPrinter;
    }

    public MasterTranslator getCommonTranslator() {
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
        InputStream in = null;
        try {
            in = new FileInputStream(Paths.get(inputFileName).toFile());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        JsonReader jsonReader = new JsonReader(
                new InputStreamReader(in));
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

    public Object getObjectFromAny(Any anyValue) {

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
            throw new RuntimeException("No corresponding message definition was found for: " + typeUrl);
        }

        classRef = message.getClass();
        try {
            Message unpackedMessage = anyValue.unpack(classRef);
            return convertInputObject(unpackedMessage);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable To unpack any type to given class: " + classRef.getName());
        }
    }

    public Object convertInputObject(Message inputObject) {
        // check if there is a translation method avaiable
        if (this.data.descriptorToTranslatorMap.containsKey(inputObject.getDescriptorForType())) {
            return this.data.descriptorToTranslatorMap.get(inputObject.getDescriptorForType()).convert(inputObject);
        }
        throw new RuntimeException(
                "No conversion translator was provided for message type: "
                        + inputObject.getDescriptorForType().getName());
    }

    public Message convertSimObject(Object simObject) {
        if (this.data.objectToTranslatorMap.containsKey(simObject.getClass())) {
            return this.data.objectToTranslatorMap.get(simObject.getClass()).convert(simObject);
        }
        throw new RuntimeException(
                "No conversion translator was provided for message type: " + simObject.getClass().getName());
    }

    public Builder getCloneBuilder() {
        return new Builder(data);
    }

}
