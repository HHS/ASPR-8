package gov.hhs.aspr.translation.protobuf.core.translationSpecs;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorSpec;

public class AnyTranslatorSpec extends ProtobufTranslatorSpec<Any, Object> {

    @Override
    protected Object convertInputObject(Any inputObject) {
        String fullTypeUrl = inputObject.getTypeUrl();
        String[] parts = fullTypeUrl.split("/");

        if (parts.length != 2) {
            throw new RuntimeException("Malformed type url");
        }

        String typeUrl = parts[1];
        Class<?> classRef = this.translatorCore.getClassFromTypeUrl(typeUrl);
        Class<? extends Message> messageClassRef;

        if (!(Message.class.isAssignableFrom(classRef))) {
            throw new RuntimeException("Message is not assignable from " + classRef.getName());
        }

        messageClassRef = classRef.asSubclass(Message.class);

        try {
            Message unpackedMessage = inputObject.unpack(messageClassRef);

            return this.translatorCore.convertObject(unpackedMessage);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Unable To unpack any type to given class: " + classRef.getName(), e);
        }
    }

    @Override
    protected Any convertAppObject(Object appObject) {
        if (Enum.class.isAssignableFrom(appObject.getClass())) {
            return Any.pack(this.translatorCore.convertObjectAsSafeClass(Enum.class.cast(appObject), Enum.class));
        }
        return Any.pack(this.translatorCore.convertObject(appObject));
    }

    @Override
    public Class<Object> getAppObjectClass() {
        return Object.class;
    }

    @Override
    public Class<Any> getInputObjectClass() {
        return Any.class;
    }
}
