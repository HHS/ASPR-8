package plugins.people.events;

import org.junit.jupiter.api.Test;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
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
}