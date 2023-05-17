package plugins.people.support;

import org.junit.jupiter.api.Test;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

import static org.junit.jupiter.api.Assertions.*;

public class AT_PersonRange {

    @Test
    @UnitTestMethod(target = PersonRange.class, name = "personRange", args = {})
    public void testPersonRange() {
        int lowPersonId = 5;
        int highPersonId = 15;
        Integer nullInt = null;

        PersonRange personRange = new PersonRange(lowPersonId, highPersonId);
        assertNotNull(personRange);

        // precondition test: illegal person range
        ContractException contractException = assertThrows(ContractException.class, () -> new PersonRange(10, 7));
        assertEquals(PersonError.ILLEGAL_PERSON_RANGE, contractException.getErrorType());

    }

}
