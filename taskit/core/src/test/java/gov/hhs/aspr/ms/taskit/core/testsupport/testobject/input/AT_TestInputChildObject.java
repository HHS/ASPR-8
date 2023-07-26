package gov.hhs.aspr.ms.taskit.core.testsupport.testobject.input;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.taskit.core.testsupport.testobject.TestInputChildObject;
import util.annotations.UnitTestConstructor;

public class AT_TestInputChildObject {
    @Test
    @UnitTestConstructor(target = TestInputChildObject.class, args = {})
    public void testConstructor() {
        assertNotNull(new TestInputChildObject());
    }
}
