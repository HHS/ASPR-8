package plugins.people.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

@UnitTest(target = PersonAdditionEvent.class)
public class AT_PersonAdditionEvent {

    @Test
    @UnitTestConstructor(args = {PersonId.class})
    public void testConstructor() {
        ContractException contractException = assertThrows(ContractException.class, () -> new PersonAdditionEvent(null));
        assertEquals(contractException.getErrorType(), PersonError.NULL_PERSON_ID);
    }

    @Test
    @UnitTestMethod(name = "getPersonId", args = {})
    public void testGetPersonId() {
        PersonId personId = new PersonId(3);
        PersonAdditionEvent personAdditionEvent = new PersonAdditionEvent(personId);
        PersonId retrievedId = personAdditionEvent.getPersonId();
        assertNotNull(retrievedId);
        assertEquals(personId, retrievedId);
    }

}