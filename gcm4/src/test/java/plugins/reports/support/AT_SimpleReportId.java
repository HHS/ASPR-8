package plugins.reports.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import plugins.globalproperties.support.SimpleGlobalPropertyId;
import tools.annotations.UnitTag;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

@UnitTest(target = SimpleReportId.class)
public class AT_SimpleReportId {

    @Test
    @UnitTestConstructor(args = {Object.class})
    public void testConstructor() {
        assertNotNull(new SimpleReportId(5));

        // show that a null report id is not thrown
        ContractException contractException = assertThrows(ContractException.class, () -> new SimpleReportId(null));
        assertEquals(contractException.getErrorType(), ReportError.NULL_REPORT_ID);

    }

    @Test
    @UnitTestMethod(name = "toString", args = {})
    public void testToString() {
        Object value = 325;
        SimpleReportId simpleReportId = new SimpleReportId(value);
        String expectedString = "SimpleReportId [value=" + value + "]";
        String actualString = simpleReportId.toString();

        assertEquals(expectedString, actualString);
    }

    @Test
    @UnitTestMethod(name = "hashCode", args = {})
    public void testHashCode() {
        // equal objects have equal hash codes
        for (int i = 0; i < 30; i++) {
            SimpleReportId s1 = new SimpleReportId(i);
            SimpleReportId s2 = new SimpleReportId(i);
            assertEquals(s1.hashCode(), s2.hashCode());
        }

        Set<Integer> hashCodes = new LinkedHashSet<>();
        for (int i = 0; i < 30; i++) {
            boolean unique = hashCodes.add(new SimpleReportId(i).hashCode());
            assertTrue(unique);
        }
    }

    @Test
    @UnitTestMethod(name = "equals", args = {Object.class}, tags = UnitTag.INCOMPLETE)
    public void testEquals() {
        Object value = 2;
        SimpleReportId id_1 = new SimpleReportId(2);
        SimpleReportId id_2 = new SimpleReportId(5);
        SimpleReportId id_3 = new SimpleReportId(2);
        SimpleReportId id_4 = new SimpleReportId("A");
        SimpleReportId id_5 = new SimpleReportId("A");
        SimpleReportId id_6 = new SimpleReportId("B");
        SimpleReportId id_7 = new SimpleReportId("A");
        SimpleGlobalPropertyId simpleGlobalPropertyId = new SimpleGlobalPropertyId(value);

        // should return false if the object is not a SimpleReportId
        assertNotEquals(id_1, simpleGlobalPropertyId);

        assertEquals(id_1, id_1); 		// testing reflexive property
        assertNotEquals(id_1, id_2);
        assertEquals(id_1, id_3);		// part of reflective property test
        assertNotEquals(id_1, id_4);
        assertNotEquals(id_1, id_5);
        assertNotEquals(id_1, id_6);

        assertNotEquals(id_2, id_1);
        assertEquals(id_2, id_2);
        assertNotEquals(id_2, id_3);
        assertNotEquals(id_2, id_4);
        assertNotEquals(id_2, id_5);
        assertNotEquals(id_2, id_6);

        assertEquals(id_3, id_1);		// part of reflective property test
        assertNotEquals(id_3, id_2);
        assertEquals(id_3, id_3);
        assertNotEquals(id_3, id_4);
        assertNotEquals(id_3, id_5);
        assertNotEquals(id_3, id_6);

        assertNotEquals(id_4, id_1);
        assertNotEquals(id_4, id_2);
        assertNotEquals(id_4, id_3);
        assertEquals(id_4, id_4);
        assertEquals(id_4, id_5);		// part of transitive property test
        assertNotEquals(id_4, id_6);
        assertEquals(id_4, id_7);		// part of transitive property test

        assertNotEquals(id_5, id_1);
        assertNotEquals(id_5, id_2);
        assertNotEquals(id_5, id_3);
        assertEquals(id_5, id_4);
        assertEquals(id_5, id_5);
        assertNotEquals(id_5, id_6);
        assertEquals(id_5, id_7);		// part of transitive property test

        assertNotEquals(id_6, id_1);
        assertNotEquals(id_6, id_2);
        assertNotEquals(id_6, id_3);
        assertNotEquals(id_6, id_4);
        assertNotEquals(id_6, id_5);
        assertEquals(id_6, id_6);

        // null tests
        assertNotEquals(id_1, null);
        assertNotEquals(id_2, null);
        assertNotEquals(id_3, null);
        assertNotEquals(id_4, null);
        assertNotEquals(id_5, null);
        assertNotEquals(id_6, null);
    }

}