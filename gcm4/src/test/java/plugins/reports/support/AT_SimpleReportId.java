package plugins.reports.support;

import org.junit.jupiter.api.Test;
import plugins.globalproperties.support.SimpleGlobalPropertyId;
import tools.annotations.UnitTag;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest(target = SimpleReportId.class)
public class AT_SimpleReportId {

    @Test
    @UnitTestConstructor(args = {Object.class})
    public void testConstructor() {
        Object value = 1;

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
    @UnitTestMethod(name = "equals", args = {Object.class}, tags = UnitTag.INCOMPLETE)
    public void testEquals() {
        Object value = 730;
        SimpleReportId simpleReportId1 = new SimpleReportId(value);
        SimpleReportId simpleReportId2 = new SimpleReportId(value);
        SimpleGlobalPropertyId simpleGlobalPropertyId = new SimpleGlobalPropertyId(value);

        // show that objects created with the same value are equal
        assertTrue(simpleReportId1.equals(simpleReportId2));

        // should return false if the object is not a SimpleReportId
        assertFalse(simpleReportId1.equals(simpleGlobalPropertyId));
    }

}