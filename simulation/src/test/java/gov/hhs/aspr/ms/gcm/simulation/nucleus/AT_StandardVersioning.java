package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestField;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_StandardVersioning {

    @Test
    @UnitTestField(target = StandardVersioning.class, name = "VERSION")
    public void testVersion() {
        assertEquals("4.4.0", StandardVersioning.VERSION);
    }

    @Test
    @UnitTestMethod(target = StandardVersioning.class, name = "checkVersionSupported", args = { String.class })
    public void testCheckVersionSupported() {
        List<String> versions = Arrays.asList(StandardVersioning.VERSION);

        for (String version : versions) {
            assertTrue(StandardVersioning.checkVersionSupported(version));
            assertFalse(StandardVersioning.checkVersionSupported(version + "badVersion"));
            assertFalse(StandardVersioning.checkVersionSupported("badVersion"));
            assertFalse(StandardVersioning.checkVersionSupported(version + "0"));
            assertFalse(StandardVersioning.checkVersionSupported(version + ".0.0"));
        }
    }
}
