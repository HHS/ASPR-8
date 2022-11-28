package plugins.personproperties.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import plugins.util.properties.PropertyError;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

@UnitTest(target = PersonPropertyDefinitionEvent.class)
public class AT_PersonPropertyDefinitionEvent {

    @Test
    @UnitTestConstructor(args = { PersonPropertyId.class })
    public void testConstructor() {
        PersonPropertyDefinitionEvent personPropertyDefinitionEvent = new PersonPropertyDefinitionEvent(
                TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK);

        assertNotNull(personPropertyDefinitionEvent);

        // precondition: person property id is null
        ContractException contractException = assertThrows(ContractException.class,
                () -> new PersonPropertyDefinitionEvent(null));
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(name="getPersonPropertyId", args={})
    public void testGetPersonPropertyId() {
        PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
        PersonPropertyDefinitionEvent personPropertyDefinitionEvent = new PersonPropertyDefinitionEvent(personPropertyId);

                assertNotNull(personPropertyDefinitionEvent);
                assertEquals(personPropertyId, personPropertyDefinitionEvent.getPersonPropertyId());
    }
}
