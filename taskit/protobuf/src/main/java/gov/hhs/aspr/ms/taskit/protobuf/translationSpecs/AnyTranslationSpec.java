package gov.hhs.aspr.ms.taskit.protobuf.translationSpecs;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;

/**
 * TranslationSpec that defines how to convert from any Java Object to a
 * Protobuf {@link Any} type and vice versa
 */
public class AnyTranslationSpec extends ProtobufTranslationSpec<Any, Object> {

    @Override
    protected Object convertInputObject(Any inputObject) {
        String fullTypeUrl = inputObject.getTypeUrl();
        String[] parts = fullTypeUrl.split("/");

        if (parts.length != 2) {
            throw new RuntimeException("Malformed type url");
        }

        String typeUrl = parts[1];
        Class<?> classRef = this.translationEngine.getClassFromTypeUrl(typeUrl);
        Class<? extends Message> messageClassRef;

        if (!(Message.class.isAssignableFrom(classRef))) {
            throw new RuntimeException("Message is not assignable from " + classRef.getName());
        }

        messageClassRef = classRef.asSubclass(Message.class);

        return unpackMessage(inputObject, messageClassRef);
    }

    protected <U extends Message> Object unpackMessage(Any inputObject, Class<U> messageClassRef) {
        try {
            Message unpackedMessage = inputObject.unpack(messageClassRef);

            return this.translationEngine.convertObject(unpackedMessage);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Unable To unpack any type to given class: " + messageClassRef.getName(), e);
        }
    }

    @Override
    protected Any convertAppObject(Object appObject) {

        Message message;

        if (Enum.class.isAssignableFrom(appObject.getClass())) {
            message = this.translationEngine.convertObjectAsSafeClass(Enum.class.cast(appObject), Enum.class);
        }

        // in the event that the object was converted BEFORE calling this
        // translationSpec, there is no need to translate it again.
        else if (Message.class.isAssignableFrom(appObject.getClass())) {
            message = Message.class.cast(appObject);
        } else {
            message = this.translationEngine.convertObject(appObject);
        }

        return Any.pack(message);
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
