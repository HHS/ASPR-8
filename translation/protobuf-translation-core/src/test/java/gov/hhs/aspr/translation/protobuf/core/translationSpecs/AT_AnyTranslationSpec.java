package gov.hhs.aspr.translation.protobuf.core.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;

/**
 * TranslationSpec that defines how to convert from any Java Object to a
 * Protobuf {@link Any} type and vice versa
 */
public class AT_AnyTranslationSpec {

    @Test
    public void testConvertInputObject() {
        // String fullTypeUrl = inputObject.getTypeUrl();
        // String[] parts = fullTypeUrl.split("/");

        // if (parts.length != 2) {
        //     throw new RuntimeException("Malformed type url");
        // }

        // String typeUrl = parts[1];
        // Class<?> classRef = this.translationEngine.getClassFromTypeUrl(typeUrl);
        // Class<? extends Message> messageClassRef;

        // if (!(Message.class.isAssignableFrom(classRef))) {
        //     throw new RuntimeException("Message is not assignable from " + classRef.getName());
        // }

        // messageClassRef = classRef.asSubclass(Message.class);

        // try {
        //     Message unpackedMessage = inputObject.unpack(messageClassRef);

        //     return this.translationEngine.convertObject(unpackedMessage);
        // } catch (InvalidProtocolBufferException e) {
        //     throw new RuntimeException("Unable To unpack any type to given class: " + classRef.getName(), e);
        // }
    }

    @Test
    public void testConvertAppObject() {
        // if (Enum.class.isAssignableFrom(appObject.getClass())) {
        //     return Any.pack(this.translationEngine.convertObjectAsSafeClass(Enum.class.cast(appObject), Enum.class));
        // }

        // Message message;

        // // in the event that the object was converted BEFORE calling thsi
        // // translationSpec, there is no need to translate it again.
        // if (Message.class.isAssignableFrom(appObject.getClass())) {
        //     message = Message.class.cast(appObject);
        // } else {
        //     message = this.translationEngine.convertObject(appObject);
        // }
        // return Any.pack(message);
    }

    @Test
    public void testGetAppObjectClass() {
        AnyTranslationSpec anyTranslationSpec = new AnyTranslationSpec();

        assertEquals(Object.class, anyTranslationSpec.getAppObjectClass());
    }

    @Test
    public void testGetInputObjectClass() {
        AnyTranslationSpec anyTranslationSpec = new AnyTranslationSpec();

        assertEquals(Any.class, anyTranslationSpec.getInputObjectClass());
    }
}
