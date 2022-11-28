package plugins.people.events;

import org.junit.jupiter.api.Test;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest(target = PersonRemovalEvent.class)
public class AT_PersonRemovalEvent {
    
    @Test
    @UnitTestConstructor(args = {PersonId.class})
    public void testConstructor() {
        ContractException contractException = assertThrows(ContractException.class, () -> new PersonRemovalEvent(null));
        assertEquals(contractException.getErrorType(), PersonError.NULL_PERSON_ID);
    }

    @Test
    @UnitTestMethod(name = "toString", args = {})
    public void testToString() {
        PersonId personId = new PersonId(7);
        PersonRemovalEvent personRemovalEvent = new PersonRemovalEvent(personId);
        String personEventString = personRemovalEvent.toString();
        assertNotNull(personEventString);
        String expectedString = "PersonRemovalEvent [personId=" + personId + "]";
        assertEquals(personEventString, expectedString);
    }

    @Test
    @UnitTestMethod(name = "getPersonId", args = {})
    public void testGetPersonId() {
        PersonId personId = new PersonId(18);
        PersonRemovalEvent personRemovalEvent = new PersonRemovalEvent(personId);
        PersonId retrievedId = personRemovalEvent.getPersonId();
        assertNotNull(retrievedId);
        assertEquals(personId, retrievedId);
    }
}